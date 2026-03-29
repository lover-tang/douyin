package com.douyin.shipping.service;

import com.douyin.shipping.dto.LabelConvertRequest;
import com.douyin.shipping.entity.LabelConversion;
import com.douyin.shipping.entity.LogisticsMapping;
import com.douyin.shipping.entity.Order;
import com.douyin.shipping.repository.LabelConversionRepository;
import com.douyin.shipping.repository.LogisticsMappingRepository;
import com.douyin.shipping.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 面单转换服务
 * 
 * 核心功能：将淘宝物流信息转换为抖音平台所需的格式
 * 1. 物流公司名称映射（淘宝名称 -> 抖音编码）
 * 2. 快递单号绑定（将淘宝单号关联到抖音订单）
 * 3. 发件人信息替换（淘宝卖家信息 -> 你的店铺信息）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LabelConversionService {

    private final OrderRepository orderRepository;
    private final LabelConversionRepository labelConversionRepository;
    private final LogisticsMappingRepository logisticsMappingRepository;
    private final DouyinApiService douyinApiService;
    private final TrackingSyncService trackingSyncService;

    /**
     * 执行面单转换
     */
    @Transactional
    public LabelConversion convertLabel(LabelConvertRequest request) {
        // 1. 查找订单
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("订单不存在: " + request.getOrderId()));

        // 2. 物流公司映射
        String douyinLogisticsCompany = request.getTaobaoLogisticsCompany();
        String douyinLogisticsCode = "";
        Optional<LogisticsMapping> mappingOpt = logisticsMappingRepository
                .findByTaobaoName(request.getTaobaoLogisticsCompany());
        if (mappingOpt.isPresent()) {
            LogisticsMapping mapping = mappingOpt.get();
            douyinLogisticsCompany = mapping.getDouyinName();
            douyinLogisticsCode = mapping.getDouyinCode();
        }

        // 3. 创建/更新面单转换记录
        LabelConversion conversion = labelConversionRepository.findByOrderId(order.getId())
                .orElse(new LabelConversion());

        conversion.setOrderId(order.getId());
        conversion.setDouyinOrderNo(order.getDouyinOrderNo());
        conversion.setTaobaoLogisticsCompany(request.getTaobaoLogisticsCompany());
        conversion.setTaobaoTrackingNo(request.getTaobaoTrackingNo());
        conversion.setDouyinLogisticsCompany(douyinLogisticsCompany);
        // 无货源模式下，抖音快递单号通常与淘宝一致
        conversion.setDouyinTrackingNo(request.getTaobaoTrackingNo());
        conversion.setSenderName(request.getSenderName());
        conversion.setSenderPhone(request.getSenderPhone());
        conversion.setSenderAddress(request.getSenderAddress());
        conversion.setReceiverName(order.getReceiverName());
        conversion.setReceiverPhone(order.getReceiverPhone());
        String fullAddress = (order.getReceiverProvince() != null ? order.getReceiverProvince() : "")
                + (order.getReceiverCity() != null ? order.getReceiverCity() : "")
                + (order.getReceiverDistrict() != null ? order.getReceiverDistrict() : "")
                + order.getReceiverAddress();
        conversion.setReceiverAddress(fullAddress);
        conversion.setStatus(1); // 已转换

        labelConversionRepository.save(conversion);

        // 4. 更新订单信息
        order.setTaobaoLogisticsCompany(request.getTaobaoLogisticsCompany());
        order.setTaobaoTrackingNo(request.getTaobaoTrackingNo());
        order.setDouyinLogisticsCompany(douyinLogisticsCompany);
        order.setDouyinTrackingNo(request.getTaobaoTrackingNo());
        order.setLabelConverted(true);
        order.setTrackingSyncEnabled(true);
        if (order.getStatus() < 2) {
            order.setStatus(2); // 已发货
        }
        orderRepository.save(order);

        // 5. 调用抖店API发货
        boolean shipSuccess = false;
        if (douyinLogisticsCode != null && !douyinLogisticsCode.isEmpty()) {
            shipSuccess = douyinApiService.shipOrder(
                    order.getDouyinOrderNo(),
                    douyinLogisticsCode,
                    request.getTaobaoTrackingNo(),
                    douyinLogisticsCompany
            );

            if (shipSuccess) {
                log.info("抖店订单发货成功: orderId={}", order.getId());
                // 订阅快递鸟轨迹推送
                trackingSyncService.subscribeTracking(order.getId());
            } else {
                log.error("抖店订单发货失败: orderId={}", order.getId());
            }
        } else {
            log.warn("未找到抖店物流编码，跳过发货: taobaoLogisticsCompany={}",
                    request.getTaobaoLogisticsCompany());
        }

        log.info("面单转换成功: 抖音订单={}, 淘宝单号={}, 抖音物流={}, 发货结果={}",
                order.getDouyinOrderNo(), request.getTaobaoTrackingNo(),
                douyinLogisticsCompany, shipSuccess ? "成功" : "失败");

        return conversion;
    }

    /**
     * 批量面单转换
     */
    @Transactional
    public int batchConvert(List<LabelConvertRequest> requests) {
        int successCount = 0;
        for (LabelConvertRequest request : requests) {
            try {
                convertLabel(request);
                successCount++;
            } catch (Exception e) {
                log.error("面单转换失败, orderId={}: {}", request.getOrderId(), e.getMessage());
            }
        }
        return successCount;
    }

    /**
     * 查询面单转换记录
     */
    public LabelConversion getConversionByOrderId(Long orderId) {
        return labelConversionRepository.findByOrderId(orderId).orElse(null);
    }

    /**
     * 获取所有面单转换记录
     */
    public List<LabelConversion> getAllConversions() {
        return labelConversionRepository.findAll();
    }

    /**
     * 获取物流公司映射列表
     */
    public List<LogisticsMapping> getLogisticsMappings() {
        return logisticsMappingRepository.findByEnabledTrue();
    }

    /**
     * 添加物流公司映射
     */
    @Transactional
    public LogisticsMapping addLogisticsMapping(LogisticsMapping mapping) {
        return logisticsMappingRepository.save(mapping);
    }

    /**
     * 删除物流公司映射
     */
    @Transactional
    public void deleteLogisticsMapping(Long id) {
        logisticsMappingRepository.deleteById(id);
    }
}
