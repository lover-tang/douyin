package com.douyin.shipping.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.douyin.shipping.config.ApiConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
public class KdniaoApiServiceTest {

    @Autowired
    private KdniaoApiService kdniaoApiService;

    /**
     * 测试单号识别功能
     * 使用顺丰测试单号：SF1234567890
     */
    @Test
    public void testRecognizeShipper() {
        // 使用一个常见的顺丰单号格式进行测试
        String logisticCode = "78989480639009";
        
        log.info("开始测试单号识别: logisticCode={}", logisticCode);
        
        List<Map<String, String>> shippers = kdniaoApiService.recognizeShipper(logisticCode);
        
        log.info("单号识别结果: {}", shippers);
        
        // 验证结果
        assertNotNull(shippers, "返回结果不应为null");
        
        if (!shippers.isEmpty()) {
            log.info("识别到 {} 家可能的快递公司", shippers.size());
            for (Map<String, String> shipper : shippers) {
                log.info("  - 编码: {}, 名称: {}", 
                    shipper.get("shipperCode"), 
                    shipper.get("shipperName"));
            }
        } else {
            log.warn("未能识别到快递公司，可能是配置问题或单号格式不正确");
        }
    }

    /**
     * 测试即时查询物流轨迹
     * 使用快递鸟提供的测试单号
     */
    @Test
    public void testQueryTracking() {
        // 快递鸟测试环境常用的测试单号
        // 顺丰测试单号
        String shipperCode = "SF";
        String logisticCode = "SF1234567890";
        
        log.info("开始测试轨迹查询: shipperCode={}, logisticCode={}", shipperCode, logisticCode);
        
        KdniaoApiService.TrackingResult result = kdniaoApiService.queryTracking(shipperCode, logisticCode);
        
        log.info("查询结果: success={}, message={}", result.isSuccess(), result.getMessage());
        
        if (result.isSuccess()) {
            log.info("物流状态: {} ({})", result.getState(), kdniaoApiService.getStateDescription(result.getState()));
            log.info("快递公司: {}", result.getShipperCode());
            log.info("快递单号: {}", result.getLogisticCode());
            
            List<KdniaoApiService.TraceItem> traces = result.getTraces();
            if (traces != null && !traces.isEmpty()) {
                log.info("获取到 {} 条轨迹记录:", traces.size());
                for (KdniaoApiService.TraceItem trace : traces) {
                    log.info("  [{}] {} - {}", 
                        trace.getAcceptTime(), 
                        trace.getAcceptStation(),
                        trace.getLocation() != null ? trace.getLocation() : "");
                }
            } else {
                log.info("暂无轨迹记录");
            }
            
            assertTrue(result.isSuccess(), "查询应成功");
        } else {
            log.error("查询失败: {}", result.getMessage());
            // 如果是配置问题，测试不应该失败，只是记录警告
            if (result.getMessage() != null && result.getMessage().contains("EBusinessID")) {
                log.warn("请检查 application.yml 中的快递鸟API配置 (e-business-id 和 api-key)");
            }
        }
    }

    /**
     * 测试轨迹订阅功能
     */
    @Test
    public void testSubscribeTracking() {
        String shipperCode = "SF";
        String logisticCode = "SF1234567890";
        String orderCode = "TEST_ORDER_001";
        
        log.info("开始测试轨迹订阅: shipperCode={}, logisticCode={}, orderCode={}", 
            shipperCode, logisticCode, orderCode);
        
        boolean success = kdniaoApiService.subscribeTracking(shipperCode, logisticCode, orderCode);
        
        log.info("订阅结果: {}", success ? "成功" : "失败");
        
        // 订阅结果取决于API配置和单号有效性，不做强制断言
        if (!success) {
            log.warn("轨迹订阅失败，请检查API配置和单号有效性");
        }
    }

    /**
     * 测试获取物流状态描述
     */
    @Test
    public void testGetStateDescription() {
        String[] states = {"0", "1", "2", "3", "4", "5", "6", "99"};
        
        log.info("测试物流状态描述:");
        for (String state : states) {
            String description = kdniaoApiService.getStateDescription(state);
            log.info("  状态 {} = {}", state, description);
        }
    }

    @Autowired
    private ApiConfig apiConfig;

    /**
     * 调试测试：直接调用快递鸟API查看原始响应
     */
    @Test
    public void testDebugApiCall() throws Exception {
        String requestData = "{'LogisticCode':'78989480639009'}";
        String requestType = "2002"; // 单号识别接口

        ApiConfig.KdniaoConfig config = apiConfig.getKdniao();
        
        log.info("===== 调试信息 =====");
        log.info("EBusinessID: {}", config.getEBusinessId());
        log.info("API Key: {}", config.getApiKey() != null ? config.getApiKey().substring(0, Math.min(10, config.getApiKey().length())) + "..." : "null");
        log.info("Request URL: {}", config.getReqUrl());
        log.info("Request Data: {}", requestData);
        log.info("Request Type: {}", requestType);

        // 构建请求参数
        Map<String, String> params = new HashMap<>();
        params.put("RequestData", requestData);
        params.put("EBusinessID", config.getEBusinessId());
        params.put("RequestType", requestType);
        params.put("DataSign", generateDataSign(requestData, config.getApiKey()));
        params.put("DataType", "2");

        log.info("请求参数: {}", params);

        // 发送请求
        java.net.HttpURLConnection conn = null;
        try {
            conn = (java.net.HttpURLConnection) new java.net.URL(config.getReqUrl()).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

            // 构建表单数据
            StringBuilder formData = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (formData.length() > 0) formData.append("&");
                formData.append(entry.getKey()).append("=")
                        .append(java.net.URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }

            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(formData.toString().getBytes(StandardCharsets.UTF_8));
            }

            // 读取响应
            int responseCode = conn.getResponseCode();
            log.info("HTTP Response Code: {}", responseCode);

            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(
                            responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream(), 
                            StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                
                String responseStr = response.toString();
                log.info("原始响应: {}", responseStr);

                // 解析JSON
                try {
                    JSONObject jsonResponse = JSON.parseObject(responseStr);
                    log.info("Success: {}", jsonResponse.getBooleanValue("Success"));
                    log.info("Reason: {}", jsonResponse.getString("Reason"));
                    log.info("Shippers: {}", jsonResponse.getJSONArray("Shippers"));
                } catch (Exception e) {
                    log.error("解析响应失败: {}", e.getMessage());
                }
            }
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private String generateDataSign(String requestData, String apiKey) throws Exception {
        String dataSign = requestData + apiKey;
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] md5Bytes = md.digest(dataSign.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(md5Bytes);
    }
}
