package com.douyin.shipping.client.dto;

import com.douyin.shipping.client.enums.KdniaoShipperCode;
import lombok.Data;

/**
 * 单号识别响应
 */
@Data
public class ShipperRecognizeResponse {

    /**
     * 快递公司编码
     */
    private String shipperCode;

    /**
     * 快递公司名称
     */
    private String shipperName;

    /**
     * 获取快递公司编码枚举
     *
     * @return 快递公司编码枚举，未找到返回null
     */
    public KdniaoShipperCode getShipperCodeEnum() {
        return KdniaoShipperCode.getByCode(this.shipperCode);
    }

    /**
     * 获取快递公司名称（优先从枚举获取）
     *
     * @return 快递公司名称
     */
    public String getShipperNameFromEnum() {
        KdniaoShipperCode code = getShipperCodeEnum();
        return code != null ? code.getName() : this.shipperName;
    }
}
