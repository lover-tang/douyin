package com.douyin.shipping.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 轨迹信息请求（手动添加轨迹）
 */
@Data
public class TrackingRequest {

    /** 订单ID */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /** 轨迹列表 */
    private List<TrackingItem> trackingItems;

    @Data
    public static class TrackingItem {
        /** 轨迹时间 yyyy-MM-dd HH:mm:ss */
        @NotBlank(message = "轨迹时间不能为空")
        private String trackingTime;

        /** 轨迹描述 */
        @NotBlank(message = "轨迹描述不能为空")
        private String description;

        /** 位置 */
        private String location;

        /** 状态: COLLECTED, IN_TRANSIT, DELIVERING, SIGNED, EXCEPTION */
        private String trackingStatus;
    }
}
