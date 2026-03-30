package com.douyin.shipping.client.dto;

import com.douyin.shipping.client.enums.KdniaoShipperCode;
import lombok.Data;

import java.util.List;

/**
 * 轨迹查询响应
 */
@Data
public class TrackingResponse {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 快递单号
     */
    private String logisticCode;

    /**
     * 快递公司编码
     */
    private String shipperCode;

    /**
     * 快递公司名称
     */
    private String shipperName;

    /**
     * 物流状态码
     */
    private String state;

    /**
     * 消息/错误信息
     */
    private String message;

    /**
     * 轨迹列表
     */
    private List<TraceItem> traces;

    /**
     * 是否已签收
     */
    public boolean isSigned() {
        return "3".equals(state);
    }

    /**
     * 是否异常
     */
    public boolean isException() {
        return "4".equals(state) || "6".equals(state);
    }

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
