package com.douyin.shipping.service;

import com.douyin.shipping.client.dto.*;

import java.util.List;

/**
 * 快递鸟业务服务接口
 * 负责处理业务逻辑和RequestData对象封装
 */
public interface KdniaoService {

    /**
     * 即时查询物流轨迹
     *
     * @param request 查询请求参数（业务属性）
     * @return 轨迹查询结果
     */
    TrackingResponse queryTracking(TrackingRequest request);

    /**
     * 订阅物流轨迹推送
     *
     * @param request 订阅请求参数（业务属性）
     * @return 是否订阅成功
     */
    boolean subscribeTracking(SubscribeRequest request);

    /**
     * 批量订阅物流轨迹
     *
     * @param requests 订阅请求列表（业务属性）
     * @return 批量订阅结果
     */
    BatchSubscribeResponse batchSubscribeTracking(List<SubscribeRequest> requests);

    /**
     * 单号识别（自动识别快递公司）
     *
     * @param request 单号识别请求（业务属性）
     * @return 识别结果列表
     */
    List<ShipperRecognizeResponse> recognizeShipper(ShipperRecognizeRequest request);

    /**
     * 获取物流状态描述
     *
     * @param state 状态码
     * @return 状态描述
     */
    String getStateDescription(String state);
}
