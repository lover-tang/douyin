package com.douyin.shipping.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.douyin.shipping.config.ApiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 抖店API服务
 * 实现抖店开放平台接口调用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DouyinApiService {

    private final ApiConfig apiConfig;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 订单发货
     * 调用抖店订单发货接口：order.logisticsAdd
     *
     * @param orderId         抖店订单号
     * @param logisticsCode   物流公司编码
     * @param trackingNo      快递单号
     * @param logisticsCompany 物流公司名称
     * @return 是否成功
     */
    public boolean shipOrder(String orderId, String logisticsCode, String trackingNo, String logisticsCompany) {
        try {
            String method = "order.logisticsAdd";

            Map<String, Object> params = new HashMap<>();
            params.put("order_id", orderId);
            params.put("logistics_code", logisticsCode);
            params.put("tracking_no", trackingNo);
            params.put("company", logisticsCompany);
            // 发货时间
            params.put("ship_time", LocalDateTime.now().format(FORMATTER));

            JSONObject response = callApi(method, params);

            if (response != null && response.getIntValue("err_no") == 0) {
                log.info("抖店订单发货成功: orderId={}, logisticsCode={}, trackingNo={}",
                        orderId, logisticsCode, trackingNo);
                return true;
            } else {
                String errorMsg = response != null ? response.getString("message") : "未知错误";
                log.error("抖店订单发货失败: orderId={}, error={}", orderId, errorMsg);
                return false;
            }
        } catch (Exception e) {
            log.error("抖店订单发货异常: orderId={}, error={}", orderId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 批量订单发货
     * 调用抖店批量发货接口：order.batchLogisticsAdd
     *
     * @param shipments 发货信息列表
     * @return 发货结果
     */
    public Map<String, Object> batchShipOrders(List<ShipmentInfo> shipments) {
        Map<String, Object> result = new HashMap<>();
        List<String> successList = new ArrayList<>();
        List<String> failList = new ArrayList<>();

        for (ShipmentInfo shipment : shipments) {
            boolean success = shipOrder(
                    shipment.getOrderId(),
                    shipment.getLogisticsCode(),
                    shipment.getTrackingNo(),
                    shipment.getLogisticsCompany()
            );
            if (success) {
                successList.add(shipment.getOrderId());
            } else {
                failList.add(shipment.getOrderId());
            }
        }

        result.put("success", successList);
        result.put("fail", failList);
        result.put("successCount", successList.size());
        result.put("failCount", failList.size());

        return result;
    }

    /**
     * 同步物流轨迹到抖店
     * 调用抖店物流轨迹同步接口：logistics.track
     *
     * @param orderId         抖店订单号
     * @param logisticsCode   物流公司编码
     * @param trackingNo      快递单号
     * @param tracks          轨迹列表
     * @return 是否成功
     */
    public boolean syncTracking(String orderId, String logisticsCode, String trackingNo, List<TrackItem> tracks) {
        try {
            String method = "logistics.track";

            Map<String, Object> params = new HashMap<>();
            params.put("order_id", orderId);
            params.put("logistics_code", logisticsCode);
            params.put("tracking_no", trackingNo);

            // 构建轨迹数据
            List<Map<String, Object>> trackList = new ArrayList<>();
            for (TrackItem track : tracks) {
                Map<String, Object> trackMap = new HashMap<>();
                trackMap.put("time", track.getTime());
                trackMap.put("desc", track.getDescription());
                trackMap.put("status", track.getStatus());
                trackList.add(trackMap);
            }
            params.put("tracks", trackList);

            JSONObject response = callApi(method, params);

            if (response != null && response.getIntValue("err_no") == 0) {
                log.info("抖店轨迹同步成功: orderId={}, trackingNo={}, trackCount={}",
                        orderId, trackingNo, tracks.size());
                return true;
            } else {
                String errorMsg = response != null ? response.getString("message") : "未知错误";
                log.error("抖店轨迹同步失败: orderId={}, error={}", orderId, errorMsg);
                return false;
            }
        } catch (Exception e) {
            log.error("抖店轨迹同步异常: orderId={}, error={}", orderId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取订单物流信息
     * 调用抖店订单物流查询接口：order.getOrderLogistics
     *
     * @param orderId 抖店订单号
     * @return 物流信息
     */
    public JSONObject getOrderLogistics(String orderId) {
        try {
            String method = "order.getOrderLogistics";
            Map<String, Object> params = new HashMap<>();
            params.put("order_id", orderId);

            return callApi(method, params);
        } catch (Exception e) {
            log.error("获取抖店订单物流信息失败: orderId={}, error={}", orderId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取物流公司列表
     * 调用抖店物流公司查询接口：logistics.list
     *
     * @return 物流公司列表
     */
    public List<Map<String, String>> getLogisticsCompanies() {
        try {
            String method = "logistics.list";
            JSONObject response = callApi(method, new HashMap<>());

            if (response != null && response.getIntValue("err_no") == 0) {
                List<Map<String, String>> companies = new ArrayList<>();
                // 解析物流公司列表
                // 实际数据结构根据抖店API返回调整
                return companies;
            }
        } catch (Exception e) {
            log.error("获取抖店物流公司列表失败: {}", e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    /**
     * 调用抖店API通用方法
     */
    private JSONObject callApi(String method, Map<String, Object> params) throws Exception {
        String url = buildRequestUrl(method, params);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");

            // 如果有请求体参数，添加到body
            if (!params.isEmpty()) {
                String jsonBody = JSON.toJSONString(params);
                httpPost.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
            }

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                return JSON.parseObject(responseBody);
            }
        }
    }

    /**
     * 构建请求URL（包含签名）
     */
    private String buildRequestUrl(String method, Map<String, Object> params) throws Exception {
        ApiConfig.DouyinConfig config = apiConfig.getDouyin();
        String baseUrl = config.isSandbox() ? config.getSandboxUrl() : config.getGatewayUrl();

        // 构建通用参数
        Map<String, String> commonParams = new TreeMap<>();
        commonParams.put("app_key", config.getAppKey());
        commonParams.put("method", method);
        commonParams.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        commonParams.put("v", "2");
        commonParams.put("sign_method", "hmac-sha256");

        // 如果有店铺ID，添加到参数
        if (config.getShopId() != null && !config.getShopId().isEmpty()) {
            commonParams.put("shop_id", config.getShopId());
        }

        // 构建签名字符串
        String sign = generateSign(commonParams, config.getAppSecret());
        commonParams.put("sign", sign);

        // 构建URL
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("/").append(method.replace(".", "/"));
        urlBuilder.append("?");

        boolean first = true;
        for (Map.Entry<String, String> entry : commonParams.entrySet()) {
            if (!first) {
                urlBuilder.append("&");
            }
            urlBuilder.append(entry.getKey()).append("=")
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            first = false;
        }

        return urlBuilder.toString();
    }

    /**
     * 生成HMAC-SHA256签名
     */
    private String generateSign(Map<String, String> params, String appSecret) throws Exception {
        // 按参数名排序并拼接
        StringBuilder paramStr = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (paramStr.length() > 0) {
                paramStr.append("&");
            }
            paramStr.append(entry.getKey()).append("=").append(entry.getValue());
        }

        // 使用HMAC-SHA256签名
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKey);
        byte[] bytes = mac.doFinal(paramStr.toString().getBytes(StandardCharsets.UTF_8));

        // 转换为十六进制
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    /**
     * 发货信息
     */
    @lombok.Data
    public static class ShipmentInfo {
        private String orderId;
        private String logisticsCode;
        private String trackingNo;
        private String logisticsCompany;
    }

    /**
     * 轨迹项
     */
    @lombok.Data
    public static class TrackItem {
        private String time;
        private String description;
        private String status;
        private String location;
    }
}
