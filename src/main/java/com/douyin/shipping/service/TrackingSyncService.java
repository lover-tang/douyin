package com.douyin.shipping.service;

import com.douyin.shipping.dto.TrackingRequest;
import com.douyin.shipping.entity.Order;
import com.douyin.shipping.entity.TrackingInfo;
import com.douyin.shipping.repository.LogisticsMappingRepository;
import com.douyin.shipping.repository.OrderRepository;
import com.douyin.shipping.repository.TrackingInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 轨迹同步服务
 *
 * 核心功能：
 * 1. 手动添加物流轨迹信息
 * 2. 从快递鸟获取轨迹并同步到抖店
 * 3. 定时同步任务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingSyncService {

    private final OrderRepository orderRepository;
    private final TrackingInfoRepository trackingInfoRepository;
    private final LogisticsMappingRepository logisticsMappingRepository;
    private final DouyinApiService douyinApiService;
    private final KdniaoApiService kdniaoApiService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 手动添加轨迹信息
     */
    @Transactional
    public List<TrackingInfo> addTrackingInfo(TrackingRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("订单不存在: " + request.getOrderId()));

        List<TrackingInfo> savedList = new ArrayList<>();
        for (TrackingRequest.TrackingItem item : request.getTrackingItems()) {
            TrackingInfo info = new TrackingInfo();
            info.setOrderId(order.getId());
            info.setTrackingNo(order.getTaobaoTrackingNo() != null ? order.getTaobaoTrackingNo() : "");
            info.setLogisticsCompany(order.getTaobaoLogisticsCompany());
            info.setTrackingTime(LocalDateTime.parse(item.getTrackingTime(), FORMATTER));
            info.setDescription(item.getDescription());
            info.setLocation(item.getLocation());
            info.setTrackingStatus(item.getTrackingStatus());
            info.setSynced(false);
            savedList.add(trackingInfoRepository.save(info));
        }

        // 更新订单状态
        if (order.getStatus() < 3) {
            order.setStatus(3); // 轨迹同步中
        }
        orderRepository.save(order);

        log.info("添加轨迹信息成功: orderId={}, count={}", order.getId(), savedList.size());
        return savedList;
    }

    /**
     * 获取订单的轨迹信息
     */
    public List<TrackingInfo> getTrackingByOrderId(Long orderId) {
        return trackingInfoRepository.findByOrderIdOrderByTrackingTimeDesc(orderId);
    }

    /**
     * 手动同步单个订单的轨迹到抖店
     */
    @Transactional
    public int syncOrderTracking(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在: " + orderId));

        List<TrackingInfo> unsyncedList = trackingInfoRepository.findByOrderIdAndSyncedFalse(orderId);
        if (unsyncedList.isEmpty()) {
            log.info("没有待同步的轨迹: orderId={}", orderId);
            return 0;
        }

        // 获取抖店物流公司编码
        String douyinLogisticsCode = getDouyinLogisticsCode(order.getTaobaoLogisticsCompany());
        if (douyinLogisticsCode == null || douyinLogisticsCode.isEmpty()) {
            log.error("未找到抖店物流编码: taobaoLogisticsCompany={}", order.getTaobaoLogisticsCompany());
            return 0;
        }

        // 构建轨迹列表
        List<DouyinApiService.TrackItem> trackItems = new ArrayList<>();
        for (TrackingInfo info : unsyncedList) {
            DouyinApiService.TrackItem item = new DouyinApiService.TrackItem();
            item.setTime(info.getTrackingTime().format(FORMATTER));
            item.setDescription(info.getDescription());
            item.setStatus(convertTrackingStatus(info.getTrackingStatus()));
            item.setLocation(info.getLocation());
            trackItems.add(item);
        }

        // 调用抖店API同步轨迹
        boolean success = douyinApiService.syncTracking(
                order.getDouyinOrderNo(),
                douyinLogisticsCode,
                order.getDouyinTrackingNo(),
                trackItems
        );

        int syncCount = 0;
        if (success) {
            for (TrackingInfo info : unsyncedList) {
                info.setSynced(true);
                info.setSyncTime(LocalDateTime.now());
                info.setSyncResult("同步成功");
                trackingInfoRepository.save(info);
                syncCount++;
            }
            log.info("轨迹同步到抖店成功: orderId={}, syncCount={}", orderId, syncCount);
        } else {
            for (TrackingInfo info : unsyncedList) {
                info.setSyncResult("同步失败: 抖店API调用失败");
                trackingInfoRepository.save(info);
            }
            log.error("轨迹同步到抖店失败: orderId={}", orderId);
        }

        // 更新订单最后同步时间
        order.setLastSyncTime(LocalDateTime.now());
        // 检查是否有签收状态的轨迹
        boolean hasSigned = unsyncedList.stream()
                .anyMatch(t -> "SIGNED".equals(t.getTrackingStatus()));
        if (hasSigned) {
            order.setStatus(4); // 已签收
        }
        orderRepository.save(order);

        return syncCount;
    }

    /**
     * 批量同步所有待同步的轨迹
     */
    @Transactional
    public int syncAllPendingTracking() {
        List<Order> syncOrders = orderRepository.findByTrackingSyncEnabledTrue();
        int totalSynced = 0;

        for (Order order : syncOrders) {
            try {
                totalSynced += syncOrderTracking(order.getId());
            } catch (Exception e) {
                log.error("批量同步失败: orderId={}, error={}", order.getId(), e.getMessage());
            }
        }

        log.info("批量同步完成: totalSynced={}", totalSynced);
        return totalSynced;
    }

    /**
     * 定时同步任务（每5分钟执行一次）
     */
    @Scheduled(cron = "${tracking.sync.cron:0 */5 * * * ?}")
    public void scheduledSync() {
        log.info("开始定时轨迹同步...");
        int synced = syncAllPendingTracking();
        log.info("定时轨迹同步完成, 同步数量: {}", synced);
    }

    /**
     * 从快递鸟获取最新轨迹
     */
    @Transactional
    public List<TrackingInfo> fetchTaobaoTracking(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在: " + orderId));

        if (order.getTaobaoTrackingNo() == null || order.getTaobaoTrackingNo().isEmpty()) {
            log.warn("订单没有快递单号: orderId={}", orderId);
            return trackingInfoRepository.findByOrderIdOrderByTrackingTimeDesc(orderId);
        }

        // 获取快递鸟物流公司编码
        String kdniaoShipperCode = getKdniaoShipperCode(order.getTaobaoLogisticsCompany());
        if (kdniaoShipperCode == null || kdniaoShipperCode.isEmpty()) {
            log.error("未找到快递鸟物流编码: taobaoLogisticsCompany={}", order.getTaobaoLogisticsCompany());
            return trackingInfoRepository.findByOrderIdOrderByTrackingTimeDesc(orderId);
        }

        // 调用快递鸟API查询轨迹
        KdniaoApiService.TrackingResult result = kdniaoApiService.queryTracking(
                kdniaoShipperCode,
                order.getTaobaoTrackingNo()
        );

        if (result.isSuccess() && result.getTraces() != null && !result.getTraces().isEmpty()) {
            List<TrackingInfo> savedList = new ArrayList<>();

            for (KdniaoApiService.TraceItem trace : result.getTraces()) {
                // 检查该轨迹是否已存在
                boolean exists = trackingInfoRepository
                        .findByOrderIdOrderByTrackingTimeDesc(orderId)
                        .stream()
                        .anyMatch(t -> t.getDescription() != null &&
                                t.getDescription().equals(trace.getAcceptStation()));

                if (!exists) {
                    TrackingInfo info = new TrackingInfo();
                    info.setOrderId(orderId);
                    info.setTrackingNo(order.getTaobaoTrackingNo());
                    info.setLogisticsCompany(order.getTaobaoLogisticsCompany());
                    info.setTrackingTime(LocalDateTime.parse(trace.getAcceptTime(), FORMATTER));
                    info.setDescription(trace.getAcceptStation());
                    info.setLocation(trace.getLocation());
                    info.setTrackingStatus(convertKdniaoState(result.getState()));
                    info.setSynced(false);
                    savedList.add(trackingInfoRepository.save(info));
                }
            }

            log.info("从快递鸟获取轨迹成功: orderId={}, newTraceCount={}", orderId, savedList.size());

            // 更新订单状态
            if (!savedList.isEmpty() && order.getStatus() < 3) {
                order.setStatus(3); // 轨迹同步中
                orderRepository.save(order);
            }

            // 如果已签收，更新订单状态
            if (result.isSigned() && order.getStatus() < 4) {
                order.setStatus(4); // 已签收
                orderRepository.save(order);
            }
        } else {
            log.warn("从快递鸟获取轨迹失败或暂无轨迹: orderId={}, message={}",
                    orderId, result.getMessage());
        }

        return trackingInfoRepository.findByOrderIdOrderByTrackingTimeDesc(orderId);
    }

    /**
     * 订阅快递鸟轨迹推送
     */
    public boolean subscribeTracking(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在: " + orderId));

        if (order.getTaobaoTrackingNo() == null || order.getTaobaoTrackingNo().isEmpty()) {
            log.warn("订单没有快递单号，无法订阅: orderId={}", orderId);
            return false;
        }

        String kdniaoShipperCode = getKdniaoShipperCode(order.getTaobaoLogisticsCompany());
        if (kdniaoShipperCode == null || kdniaoShipperCode.isEmpty()) {
            log.error("未找到快递鸟物流编码: taobaoLogisticsCompany={}", order.getTaobaoLogisticsCompany());
            return false;
        }

        return kdniaoApiService.subscribeTracking(
                kdniaoShipperCode,
                order.getTaobaoTrackingNo(),
                order.getDouyinOrderNo()
        );
    }

    /**
     * 获取抖店物流编码
     */
    private String getDouyinLogisticsCode(String taobaoLogisticsCompany) {
        if (taobaoLogisticsCompany == null || taobaoLogisticsCompany.isEmpty()) {
            return null;
        }
        return logisticsMappingRepository.findByTaobaoName(taobaoLogisticsCompany)
                .map(mapping -> mapping.getDouyinCode())
                .orElse(null);
    }

    /**
     * 获取快递鸟物流编码
     */
    private String getKdniaoShipperCode(String taobaoLogisticsCompany) {
        if (taobaoLogisticsCompany == null || taobaoLogisticsCompany.isEmpty()) {
            return null;
        }
        return logisticsMappingRepository.findByTaobaoName(taobaoLogisticsCompany)
                .map(mapping -> mapping.getKdniaoCode())
                .orElse(null);
    }

    /**
     * 转换轨迹状态为抖店格式
     */
    private String convertTrackingStatus(String status) {
        if (status == null) return "IN_TRANSIT";
        return switch (status) {
            case "PICKUP" -> "COLLECTED";
            case "SIGNED" -> "SIGNED";
            case "DELIVERING" -> "DELIVERING";
            case "EXCEPTION" -> "EXCEPTION";
            default -> "IN_TRANSIT";
        };
    }

    /**
     * 转换快递鸟状态码为系统状态
     */
    private String convertKdniaoState(String state) {
        return switch (state) {
            case "0" -> "NO_RECORD";
            case "1" -> "PICKUP";
            case "2" -> "IN_TRANSIT";
            case "3" -> "SIGNED";
            case "4" -> "EXCEPTION";
            case "5" -> "DELIVERING";
            case "6" -> "RETURNING";
            default -> "IN_TRANSIT";
        };
    }

    /**
     * 获取待同步轨迹数量
     */
    public long getPendingSyncCount() {
        return trackingInfoRepository.countBySyncedFalse();
    }

    /**
     * 删除轨迹
     */
    @Transactional
    public void deleteTracking(Long trackingId) {
        trackingInfoRepository.deleteById(trackingId);
    }
}
