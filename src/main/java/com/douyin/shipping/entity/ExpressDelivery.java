package com.douyin.shipping.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 快递运单主表
 */
@Data
public class ExpressDelivery {

    private Long id;

    /**
     * 快递鸟商户ID
     */
    private String ebusinessId;

    /**
     * 快递公司编码
     */
    private String shipperCode;

    /**
     * 物流单号
     */
    private String logisticCode;

    /**
     * 订单编号
     */
    private String orderCode;

    /**
     * 物流状态
     * 2-在途中 3-已签收 4-问题件 5-已取件 6-待派送 7-派送中 8-待取件
     */
    private Integer state;

    /**
     * 物流状态扩展码
     */
    private String stateEx;

    /**
     * 最新轨迹位置
     */
    private String location;

    /**
     * 派送员姓名
     */
    private String deliveryManName;

    /**
     * 派送员电话
     */
    private String deliveryManTel;

    /**
     * 预计送达时间
     */
    private LocalDateTime estimatedDeliveryTime;

    /**
     * 首次查询时间
     */
    private LocalDateTime firstQueryTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;

    /**
     * 是否已订阅物流推送 0-否 1-是
     */
    private Integer isSubscribed = 0;

    /**
     * 订阅回调地址
     */
    private String callbackUrl;

    /**
     * 备注信息
     */
    private String remark;
}
