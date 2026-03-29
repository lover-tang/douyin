package com.douyin.shipping.controller;

import com.douyin.shipping.dto.ApiResult;
import com.douyin.shipping.dto.TrackingRequest;
import com.douyin.shipping.entity.TrackingInfo;
import com.douyin.shipping.service.TrackingFetchService;
import com.douyin.shipping.service.TrackingSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingSyncController {

    private final TrackingSyncService trackingSyncService;
    private final TrackingFetchService trackingFetchService;

    /**
     * 添加轨迹信息
     */
    @PostMapping
    public ApiResult<List<TrackingInfo>> addTracking(@RequestBody TrackingRequest request) {
        try {
            return ApiResult.success(trackingSyncService.addTrackingInfo(request));
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 获取订单的轨迹信息
     */
    @GetMapping("/order/{orderId}")
    public ApiResult<List<TrackingInfo>> getTrackingByOrderId(@PathVariable Long orderId) {
        return ApiResult.success(trackingSyncService.getTrackingByOrderId(orderId));
    }

    /**
     * 同步单个订单的轨迹到抖音
     */
    @PostMapping("/sync/{orderId}")
    public ApiResult<Map<String, Object>> syncOrderTracking(@PathVariable Long orderId) {
        try {
            int count = trackingSyncService.syncOrderTracking(orderId);
            Map<String, Object> result = new HashMap<>();
            result.put("syncedCount", count);
            result.put("orderId", orderId);
            return ApiResult.success(result);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 批量同步所有待同步轨迹
     */
    @PostMapping("/sync/all")
    public ApiResult<Map<String, Object>> syncAllTracking() {
        try {
            int count = trackingSyncService.syncAllPendingTracking();
            Map<String, Object> result = new HashMap<>();
            result.put("syncedCount", count);
            return ApiResult.success(result);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 从淘宝获取最新轨迹
     */
    @PostMapping("/fetch/{orderId}")
    public ApiResult<List<TrackingInfo>> fetchTaobaoTracking(@PathVariable Long orderId) {
        try {
            return ApiResult.success(trackingSyncService.fetchTaobaoTracking(orderId));
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 删除轨迹
     */
    @DeleteMapping("/{trackingId}")
    public ApiResult<Void> deleteTracking(@PathVariable Long trackingId) {
        try {
            trackingSyncService.deleteTracking(trackingId);
            return ApiResult.success();
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 获取待同步数量
     */
    @GetMapping("/pending-count")
    public ApiResult<Long> getPendingSyncCount() {
        return ApiResult.success(trackingSyncService.getPendingSyncCount());
    }

    /**
     * 订阅快递鸟轨迹推送
     */
    @PostMapping("/subscribe/{orderId}")
    public ApiResult<Map<String, Object>> subscribeTracking(@PathVariable Long orderId) {
        try {
            boolean success = trackingSyncService.subscribeTracking(orderId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("orderId", orderId);
            return ApiResult.success(result);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 手动拉取并同步轨迹
     */
    @PostMapping("/fetch-sync/{orderId}")
    public ApiResult<Map<String, Object>> fetchAndSyncTracking(@PathVariable Long orderId) {
        try {
            String message = trackingFetchService.fetchAndSyncOrderTracking(orderId);
            Map<String, Object> result = new HashMap<>();
            result.put("message", message);
            result.put("orderId", orderId);
            return ApiResult.success(result);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 批量拉取轨迹
     */
    @PostMapping("/batch-fetch")
    public ApiResult<Map<String, Object>> batchFetchTracking(@RequestBody List<Long> orderIds) {
        try {
            String message = trackingFetchService.batchFetchTracking(orderIds);
            Map<String, Object> result = new HashMap<>();
            result.put("message", message);
            return ApiResult.success(result);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }
}
