package com.douyin.shipping.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.douyin.shipping.config.ApiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * 快递鸟API服务
 * 实现快递轨迹查询和订阅功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KdniaoApiService {

    private final ApiConfig apiConfig;

    /**
     * 即时查询物流轨迹
     * API: 1002 即时查询接口
     *
     * @param shipperCode 快递公司编码
     * @param logisticCode 快递单号
     * @return 轨迹信息
     */
    public TrackingResult queryTracking(String shipperCode, String logisticCode) {
        try {
            String requestData = "{'OrderCode':'','ShipperCode':'" + shipperCode + "','LogisticCode':'" + logisticCode + "'}";

            Map<String, String> params = buildRequestParams(requestData, "1002");
            String response = sendPost(apiConfig.getKdniao().getReqUrl(), params);

            JSONObject jsonResponse = JSON.parseObject(response);

            TrackingResult result = new TrackingResult();
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
                        shipperCode, logisticCode, traceList.size());
            } else {
                log.warn("快递鸟轨迹查询失败: shipperCode={}, logisticCode={}, reason={}",
                        shipperCode, logisticCode, result.getMessage());
            }

            return result;
        } catch (Exception e) {
            log.error("快递鸟轨迹查询异常: shipperCode={}, logisticCode={}, error={}",
                    shipperCode, logisticCode, e.getMessage(), e);
            TrackingResult result = new TrackingResult();
            result.setSuccess(false);
            result.setMessage("查询异常: " + e.getMessage());
            return result;
        }
    }

    /**
     * 订阅物流轨迹
     * API: 1008 轨迹订阅接口
     * 订阅后，快递鸟会主动推送轨迹变化到callbackUrl
     *
     * @param shipperCode  快递公司编码
     * @param logisticCode 快递单号
     * @param orderCode    订单编号（可选）
     * @return 是否订阅成功
     */
    public boolean subscribeTracking(String shipperCode, String logisticCode, String orderCode) {
        try {
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("ShipperCode", shipperCode);
            requestMap.put("LogisticCode", logisticCode);
            if (orderCode != null && !orderCode.isEmpty()) {
                requestMap.put("OrderCode", orderCode);
            }
            // 设置回调地址（如果配置了）
            if (apiConfig.getKdniao().getCallbackUrl() != null &&
                !apiConfig.getKdniao().getCallbackUrl().isEmpty()) {
                requestMap.put("CallBack", apiConfig.getKdniao().getCallbackUrl());
            }

            String requestData = JSON.toJSONString(requestMap);
            Map<String, String> params = buildRequestParams(requestData, "1008");
            String response = sendPost(apiConfig.getKdniao().getReqUrl(), params);

            JSONObject jsonResponse = JSON.parseObject(response);
            boolean success = jsonResponse.getBooleanValue("Success");

            if (success) {
                log.info("快递鸟轨迹订阅成功: shipperCode={}, logisticCode={}", shipperCode, logisticCode);
            } else {
                String reason = jsonResponse.getString("Reason");
                log.warn("快递鸟轨迹订阅失败: shipperCode={}, logisticCode={}, reason={}",
                        shipperCode, logisticCode, reason);
            }

            return success;
        } catch (Exception e) {
            log.error("快递鸟轨迹订阅异常: shipperCode={}, logisticCode={}, error={}",
                    shipperCode, logisticCode, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 批量订阅物流轨迹
     *
     * @param items 订阅项列表
     * @return 订阅结果
     */
    public Map<String, Object> batchSubscribeTracking(List<SubscribeItem> items) {
        Map<String, Object> result = new HashMap<>();
        List<String> successList = new ArrayList<>();
        List<String> failList = new ArrayList<>();

        for (SubscribeItem item : items) {
            boolean success = subscribeTracking(
                    item.getShipperCode(),
                    item.getLogisticCode(),
                    item.getOrderCode()
            );
            if (success) {
                successList.add(item.getLogisticCode());
            } else {
                failList.add(item.getLogisticCode());
            }
        }

        result.put("success", successList);
        result.put("fail", failList);
        result.put("successCount", successList.size());
        result.put("failCount", failList.size());

        return result;
    }

    /**
     * 单号识别
     * API: 2002 单号识别接口
     * 根据快递单号自动识别快递公司
     *
     * @param logisticCode 快递单号
     * @return 可能的快递公司列表
     */
    public List<Map<String, String>> recognizeShipper(String logisticCode) {
        try {
            String requestData = "{'LogisticCode':'" + logisticCode + "'}";
            Map<String, String> params = buildRequestParams(requestData, "2002");
            String response = sendPost(apiConfig.getKdniao().getReqUrl(), params);

            JSONObject jsonResponse = JSON.parseObject(response);

            if (jsonResponse.getBooleanValue("Success")) {
                JSONArray shippers = jsonResponse.getJSONArray("Shippers");
                List<Map<String, String>> result = new ArrayList<>();
                if (shippers != null) {
                    for (int i = 0; i < shippers.size(); i++) {
                        JSONObject shipper = shippers.getJSONObject(i);
                        Map<String, String> map = new HashMap<>();
                        map.put("shipperCode", shipper.getString("ShipperCode"));
                        map.put("shipperName", shipper.getString("ShipperName"));
                        result.add(map);
                    }
                }
                return result;
            }
        } catch (Exception e) {
            log.error("快递鸟单号识别异常: logisticCode={}, error={}", logisticCode, e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    /**
     * 处理快递鸟推送的轨迹数据
     * 当订阅的快递有轨迹更新时，快递鸟会推送数据到callbackUrl
     *
     * @param pushData 推送的JSON数据
     * @return 处理后的轨迹列表
     */
    public List<TraceItem> handlePushTracking(String pushData) {
        try {
            JSONObject jsonData = JSON.parseObject(pushData);
            JSONArray data = jsonData.getJSONArray("Data");

            List<TraceItem> traces = new ArrayList<>();
            if (data != null && !data.isEmpty()) {
                JSONObject firstItem = data.getJSONObject(0);
                JSONArray tracesArray = firstItem.getJSONArray("Traces");

                if (tracesArray != null) {
                    for (int i = 0; i < tracesArray.size(); i++) {
                        JSONObject trace = tracesArray.getJSONObject(i);
                        TraceItem item = new TraceItem();
                        item.setAcceptTime(trace.getString("AcceptTime"));
                        item.setAcceptStation(trace.getString("AcceptStation"));
                        item.setRemark(trace.getString("Remark"));
                        item.setLocation(trace.getString("Location"));
                        traces.add(item);
                    }
                }
            }

            log.info("处理快递鸟推送轨迹: traceCount={}", traces.size());
            return traces;
        } catch (Exception e) {
            log.error("处理快递鸟推送轨迹异常: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 构建请求参数
     */
    private Map<String, String> buildRequestParams(String requestData, String requestType) throws Exception {
        ApiConfig.KdniaoConfig config = apiConfig.getKdniao();

        Map<String, String> params = new HashMap<>();
        params.put("RequestData", requestData);
        params.put("EBusinessID", config.getEBusinessId());
        params.put("RequestType", requestType);
        params.put("DataSign", generateDataSign(requestData, config.getApiKey()));
        params.put("DataType", "2"); // 2表示JSON格式

        return params;
    }

    /**
     * 生成数据签名
     * 快递鸟签名规则：base64(MD5(请求内容(未编码)+ApiKey))
     */
    private String generateDataSign(String requestData, String apiKey) throws Exception {
        String dataSign = requestData + apiKey;
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] md5Bytes = md.digest(dataSign.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(md5Bytes);
    }

    /**
     * 发送POST请求
     */
    private String sendPost(String url, Map<String, String> params) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

            // 构建表单参数
            StringBuilder formData = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (formData.length() > 0) {
                    formData.append("&");
                }
                formData.append(entry.getKey()).append("=")
                        .append(java.net.URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }

            httpPost.setEntity(new StringEntity(formData.toString(), StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
        }
    }

    /**
     * 获取快递状态描述
     */
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
     * 轨迹查询结果
     */
    @lombok.Data
    public static class TrackingResult {
        private boolean success;
        private String logisticCode;
        private String shipperCode;
        private String shipperName;
        private String state;
        private String message;
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
    }

    /**
     * 轨迹项
     */
    @lombok.Data
    public static class TraceItem {
        private String acceptTime;
        private String acceptStation;
        private String remark;
        private String location;
    }

    /**
     * 订阅项
     */
    @lombok.Data
    public static class SubscribeItem {
        private String shipperCode;
        private String logisticCode;
        private String orderCode;
    }
}
