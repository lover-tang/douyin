package com.douyin.shipping.client.dto;

import com.douyin.shipping.client.enums.KdniaoShipperCode;
import lombok.Data;

/**
 * 轨迹订阅请求
 */
@Data
public class SubscribeRequest {

    /**
     * 快递公司编码
     * 建议使用 {@link KdniaoShipperCode} 枚举
     */
    private String shipperCode;

    /**
     * 快递单号
     */
    private String logisticCode;

    /**
     * 订单编号（可选）
     */
    private String orderCode;

    /**
     * 使用枚举设置快递公司编码
     *
     * @param shipperCode 快递公司编码枚举
     */
    public void setShipperCode(KdniaoShipperCode shipperCode) {
        if (shipperCode != null) {
            this.shipperCode = shipperCode.getCode();
        }
    }
}
