package com.douyin.shipping.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.douyin.shipping.client.KdniaoClient;
import com.douyin.shipping.client.dto.*;
import com.douyin.shipping.config.ApiConfig;
import com.douyin.shipping.service.KdniaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 快递鸟业务服务实现
 * 负责处理业务逻辑和RequestData对象封装
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KdniaoServiceImpl implements KdniaoService {

    private final KdniaoClient kdniaoClient;
    private final ApiConfig apiConfig;

    @Override
    public TrackingResponse queryTracking(TrackingRequest request) {
        try {
            // 构建快递鸟API请求参数（只包含LogisticCode，参考官方示例）
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("LogisticCode", request.getLogisticCode());

            String requestData = JSON.toJSONString(requestMap);
            String response = kdniaoClient.sendRequest(requestData, KdniaoClient.RequestType.QUERY_TRACKING);

            // 解析响应并转换为通用DTO
            return parseTrackingResponse(response);
        } catch (Exception e) {
            log.error("快递鸟轨迹查询异常: logisticCode={}, error={}",
                    request.getLogisticCode(), e.getMessage(), e);
            TrackingResponse result = new TrackingResponse();
            result.setSuccess(false);
            result.setMessage("查询异常: " + e.getMessage());
            return result;
        }
    }

    @Override
    public boolean subscribeTracking(SubscribeRequest request) {
        try {
            // 构建快递鸟API请求参数
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("ShipperCode", request.getShipperCode());
            requestMap.put("LogisticCode", request.getLogisticCode());
            if (request.getOrderCode() != null && !request.getOrderCode().isEmpty()) {
                requestMap.put("OrderCode", request.getOrderCode());
            }
            // 设置回调地址（如果配置了）
            if (apiConfig.getKdniao().getCallbackUrl() != null &&
                !apiConfig.getKdniao().getCallbackUrl().isEmpty()) {
                requestMap.put("CallBack", apiConfig.getKdniao().getCallbackUrl());
            }

            String requestData = JSON.toJSONString(requestMap);
            String response = kdniaoClient.sendRequest(requestData, KdniaoClient.RequestType.SUBSCRIBE_TRACKING);

            JSONObject jsonResponse = JSON.parseObject(response);
            boolean success = jsonResponse.getBooleanValue("Success");

            if (success) {
                log.info("快递鸟轨迹订阅成功: shipperCode={}, logisticCode={}",
                        request.getShipperCode(), request.getLogisticCode());
            } else {
                String reason = jsonResponse.getString("Reason");
                log.warn("快递鸟轨迹订阅失败: shipperCode={}, logisticCode={}, reason={}",
                        request.getShipperCode(), request.getLogisticCode(), reason);
            }

            return success;
        } catch (Exception e) {
            log.error("快递鸟轨迹订阅异常: shipperCode={}, logisticCode={}, error={}",
                    request.getShipperCode(), request.getLogisticCode(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public BatchSubscribeResponse batchSubscribeTracking(List<SubscribeRequest> requests) {
        List<String> successList = new ArrayList<>();
        List<String> failList = new ArrayList<>();

        for (SubscribeRequest request : requests) {
            boolean success = subscribeTracking(request);
            if (success) {
                successList.add(request.getLogisticCode());
            } else {
                failList.add(request.getLogisticCode());
            }
        }

        BatchSubscribeResponse response = new BatchSubscribeResponse();
        response.setSuccessList(successList);
        response.setFailList(failList);
        response.setSuccessCount(successList.size());
        response.setFailCount(failList.size());

        return response;
    }

    @Override
    public List<ShipperRecognizeResponse> recognizeShipper(ShipperRecognizeRequest request) {
        try {
            // 构建快递鸟API请求参数
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("LogisticCode", request.getLogisticCode());

            String requestData = JSON.toJSONString(requestMap);
            String response = kdniaoClient.sendRequest(requestData, KdniaoClient.RequestType.RECOGNIZE_SHIPPER);

            // 解析响应并转换为通用DTO
            return parseShipperRecognizeResponse(response);
        } catch (Exception e) {
            log.error("快递鸟单号识别异常: logisticCode={}, error={}",
                    request.getLogisticCode(), e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public String getStateDescription(String state) {
        Map<String, String> stateMap = new HashMap<>();
        stateMap.put("0", "无轨迹");
        stateMap.put("1", "已揽收");
        stateMap.put("2", "在途中");
        stateMap.put("3", "已签收");
        stateMap.put("4", "问题件");
        stateMap.put("5", "派件中");
        stateMap.put("6", "退回中");
        return stateMap.getOrDefault(state, "未知状态");
    }

    /**
     * 解析轨迹查询响应
     */
    private TrackingResponse parseTrackingResponse(String response) {
        JSONObject jsonResponse = JSON.parseObject(response);

        TrackingResponse result = new TrackingResponse();
        result.setSuccess(jsonResponse.getBooleanValue("Success"));
        result.setLogisticCode(jsonResponse.getString("LogisticCode"));
        result.setShipperCode(jsonResponse.getString("ShipperCode"));
        result.setState(jsonResponse.getString("State"));
        result.setMessage(jsonResponse.getString("Reason"));

        if (result.isSuccess()) {
            JSONArray traces = jsonResponse.getJSONArray("Traces");
            List<TraceItem> traceList = new ArrayList<>();
            if (traces != null) {
                for (int i = 0; i < traces.size(); i++) {
                    JSONObject trace = traces.getJSONObject(i);
                    TraceItem item = new TraceItem();
                    item.setAcceptTime(trace.getString("AcceptTime"));
                    item.setAcceptStation(trace.getString("AcceptStation"));
                    item.setRemark(trace.getString("Remark"));
                    item.setLocation(trace.getString("Location"));
                    traceList.add(item);
                }
            }
            result.setTraces(traceList);
            log.info("快递鸟轨迹查询成功: shipperCode={}, logisticCode={}, traceCount={}",
                    result.getShipperCode(), result.getLogisticCode(), traceList.size());
        } else {
            log.warn("快递鸟轨迹查询失败: logisticCode={}, reason={}",
                    result.getLogisticCode(), result.getMessage());
        }

        return result;
    }

    /**
     * 解析单号识别响应
     */
    private List<ShipperRecognizeResponse> parseShipperRecognizeResponse(String response) {
        JSONObject jsonResponse = JSON.parseObject(response);
        List<ShipperRecognizeResponse> result = new ArrayList<>();

        if (jsonResponse.getBooleanValue("Success")) {
            JSONArray shippers = jsonResponse.getJSONArray("Shippers");
            if (shippers != null) {
                for (int i = 0; i < shippers.size(); i++) {
                    JSONObject shipper = shippers.getJSONObject(i);
                    ShipperRecognizeResponse item = new ShipperRecognizeResponse();
                    item.setShipperCode(shipper.getString("ShipperCode"));
                    item.setShipperName(shipper.getString("ShipperName"));
                    result.add(item);
                }
            }
        }

        return result;
    }
}
