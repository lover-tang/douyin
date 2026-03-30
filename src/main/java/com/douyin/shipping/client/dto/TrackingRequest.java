package com.douyin.shipping.client.dto;

import com.douyin.shipping.client.enums.KdniaoShipperCode;
import lombok.Data;

/**
 * 轨迹查询请求
 */
@Data
public class TrackingRequest {

    /**
     * 快递公司编码（可选，如果为空则自动识别）
     * 建议使用 {@link KdniaoShipperCode} 枚举
     */
    private String shipperCode;

    /**
     * 快递单号（必填）
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
