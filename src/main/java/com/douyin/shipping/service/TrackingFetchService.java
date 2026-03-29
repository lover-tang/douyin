package com.douyin.shipping.service;

import com.douyin.shipping.entity.Order;
import com.douyin.shipping.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 轨迹拉取服务
 * 定时从快递鸟拉取轨迹并同步到抖店
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingFetchService {

    private final OrderRepository orderRepository;
    private final TrackingSyncService trackingSyncService;

    /**
     * 定时拉取轨迹任务（每10分钟执行一次）
     * 从快递鸟查询所有已发货订单的最新轨迹
     */
    @Scheduled(cron = "${tracking.fetch.cron:0 */10 * * * ?}")
    public void scheduledFetchTracking() {
        log.info("开始定时拉取轨迹...");

        // 获取所有已发货且启用了轨迹同步的订单
        List<Order> orders = orderRepository.findByTrackingSyncEnabledTrue();
        int fetchCount = 0;
        int syncCount = 0;

        for (Order order : orders) {
            try {
                // 跳过没有快递单号的订单
                if (order.getTaobaoTrackingNo() == null || order.getTaobaoTrackingNo().isEmpty()) {
                    continue;
                }

                // 跳过已签收的订单
                if (order.getStatus() != null && order.getStatus() >= 4) {
                    continue;
                }

                // 从快递鸟拉取轨迹
                trackingSyncService.fetchTaobaoTracking(order.getId());
                fetchCount++;

                // 同步轨迹到抖店
                int synced = trackingSyncService.syncOrderTracking(order.getId());
                syncCount += synced;

                // 避免请求过快，添加延迟
                Thread.sleep(500);

            } catch (Exception e) {
                log.error("拉取轨迹失败: orderId={}, error={}", order.getId(), e.getMessage());
            }
        }

        log.info("定时拉取轨迹完成: 拉取订单数={}, 同步轨迹数={}", fetchCount, syncCount);
    }

    /**
     * 手动拉取指定订单的轨迹
     *
     * @param orderId 订单ID
     * @return 拉取结果
     */
    public String fetchAndSyncOrderTracking(Long orderId) {
        try {
            // 拉取轨迹
            trackingSyncService.fetchTaobaoTracking(orderId);

            // 同步到抖店
            int synced = trackingSyncService.syncOrderTracking(orderId);

            return String.format("拉取完成，同步 %d 条轨迹到抖店", synced);
        } catch (Exception e) {
            log.error("手动拉取轨迹失败: orderId={}, error={}", orderId, e.getMessage());
            return "拉取失败: " + e.getMessage();
        }
    }

    /**
     * 批量拉取轨迹
     *
     * @param orderIds 订单ID列表
     * @return 拉取结果
     */
    public String batchFetchTracking(List<Long> orderIds) {
        int successCount = 0;
        int failCount = 0;

        for (Long orderId : orderIds) {
            try {
                trackingSyncService.fetchTaobaoTracking(orderId);
                trackingSyncService.syncOrderTracking(orderId);
                successCount++;

                // 避免请求过快
                Thread.sleep(300);
            } catch (Exception e) {
                log.error("批量拉取轨迹失败: orderId={}, error={}", orderId, e.getMessage());
                failCount++;
            }
        }

        return String.format("批量拉取完成: 成功 %d 个, 失败 %d 个", successCount, failCount);
    }
}
