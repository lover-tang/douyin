package com.douyin.shipping.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 快递轨迹明细表
 */
@Data
public class ExpressTrack {

    private Long id;

    /**
     * 关联快递主表ID
     */
    private Long expressId;

    /**
     * 物流单号
     */
    private String logisticCode;

    /**
     * 快递公司编码
     */
    private String shipperCode;

    /**
     * 操作类型码
     * 1-揽收 2-发往 202-派送中 204-到达 412-代收点存放
     */
    private String actionCode;

    /**
     * 轨迹发生时间
     */
    private LocalDateTime acceptTime;

    /**
     * 轨迹描述信息
     */
    private String acceptStation;

    /**
     * 轨迹所在城市
     */
    private String location;

    /**
     * 轨迹顺序
     */
    private Integer trackOrder = 0;

    /**
     * 记录创建时间
     */
    private LocalDateTime createTime;
}
