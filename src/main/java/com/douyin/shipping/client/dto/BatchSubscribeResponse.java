package com.douyin.shipping.client.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量订阅响应
 */
@Data
public class BatchSubscribeResponse {

    /**
     * 成功的快递单号列表
     */
    private List<String> successList;

    /**
     * 失败的快递单号列表
     */
    private List<String> failList;

    /**
     * 成功数量
     */
    private int successCount;

    /**
     * 失败数量
     */
    private int failCount;
}
