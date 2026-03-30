package com.douyin.shipping.client.dto;

import lombok.Data;

/**
 * 单号识别请求
 */
@Data
public class ShipperRecognizeRequest {

    /**
     * 快递单号
     */
    private String logisticCode;
}
