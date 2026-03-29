package com.douyin.shipping.dto;

import lombok.Data;

/**
 * 仪表盘统计数据
 */
@Data
public class DashboardStats {
    private long totalOrders;
    private long pendingOrders;
    private long shippedOrders;
    private long syncingOrders;
    private long completedOrders;
    private long exceptionOrders;
    private long pendingLabelConvert;
    private long pendingTrackingSync;
}
