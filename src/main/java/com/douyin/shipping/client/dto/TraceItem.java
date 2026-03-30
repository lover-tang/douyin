package com.douyin.shipping.client.dto;

import lombok.Data;

/**
 * 轨迹项
 */
@Data
public class TraceItem {

    /**
     * 轨迹时间
     */
    private String acceptTime;

    /**
     * 轨迹描述
     */
    private String acceptStation;

    /**
     * 备注
     */
    private String remark;

    /**
     * 地点
     */
    private String location;
}
