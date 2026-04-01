package com.douyin.shipping.client;

/**
 * 快递鸟HTTP客户端接口
 * 只负责封装HTTP请求和基础参数（签名、EBusinessID等）
 */
public interface KdniaoClient {

    /**
     * 快递鸟API请求类型常量
     */
    interface RequestType {
        /** 即时查询 */
        String QUERY_TRACKING = "8002";
        /** 轨迹订阅 */
        String SUBSCRIBE_TRACKING = "1008";
        /** 单号识别 */
        String RECOGNIZE_SHIPPER = "2002";
    }

    /**
     * 发送快递鸟API请求
     *
     * @param requestData 业务请求数据（JSON字符串）
     * @param requestType 请求类型，使用 {@link RequestType} 常量
     * @return API响应JSON字符串
     */
    String sendRequest(String requestData, String requestType);
}
