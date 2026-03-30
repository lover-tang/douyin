package com.douyin.shipping.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流订阅记录表
 */
@Data
public class ExpressSubscription {

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
     * 回调地址
     */
    private String callbackUrl;

    /**
     * 订阅状态 0-已取消 1-订阅中 2-已过期
     */
    private Integer subscriptionStatus = 1;

    /**
     * 最后一次推送时间
     */
    private LocalDateTime lastPushTime;

    /**
     * 推送成功次数
     */
    private Integer pushSuccessCount = 0;

    /**
     * 推送失败次数
     */
    private Integer pushFailCount = 0;

    /**
     * 订阅时间
     */
    private LocalDateTime subscribeTime;

    /**
     * 订阅过期时间
     */
    private LocalDateTime expireTime;
}
