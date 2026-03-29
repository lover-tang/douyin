package com.douyin.shipping.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.douyin.shipping.service.KdniaoApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 快递鸟回调接口
 * 用于接收快递鸟推送的物流轨迹数据
 */
@Slf4j
@RestController
@RequestMapping("/api/kdniao")
@RequiredArgsConstructor
public class KdniaoCallbackController {

    private final KdniaoApiService kdniaoApiService;

    /**
     * 接收快递鸟推送的轨迹数据
     * 快递鸟会主动POST数据到这个地址
     *
     * Request Data Format (快递鸟推送):
     * {
     *   "Data": [
     *     {
     *       "LogisticCode": "SF1234567890",
     *       "ShipperCode": "SF",
     *       "Traces": [
     *         {
     *           "AcceptTime": "2024-01-01 12:00:00",
     *           "AcceptStation": "【北京】【朝阳】快件已被【北京朝阳太平庄营业点】揽收",
     *           "Remark": ""
     *         }
     *       ]
     *     }
     *   ]
     * }
     */
    @PostMapping("/callback")
    public String handleCallback(@RequestBody String body,
                                  @RequestHeader("Content-Type") String contentType) {
        log.info("收到快递鸟回调, contentType={}", contentType);

        try {
            // 验证请求来源（可选：检查签名、IP白名单等）
            // 简单验证：检查Content-Type是否为application/x-www-form-urlencoded
            if (!contentType.contains("application/x-www-form-urlencoded")) {
                log.warn("无效的Content-Type: {}", contentType);
                return "FAIL";
            }

            // 解析推送数据
            JSONObject jsonData = JSON.parseObject(body);
            JSONArray data = jsonData.getJSONArray("Data");

            if (data == null || data.isEmpty()) {
                log.warn("快递鸟推送数据为空");
                return "FAIL";
            }

            int processedCount = 0;

            // 处理每一条推送数据
            for (int i = 0; i < data.size(); i++) {
                JSONObject item = data.getJSONObject(i);
                String logisticCode = item.getString("LogisticCode");
                String shipperCode = item.getString("ShipperCode");

                log.info("处理快递鸟推送: shipperCode={}, logisticCode={}", shipperCode, logisticCode);

                // 从快递鸟获取轨迹列表
                List<KdniaoApiService.TraceItem> traces =
                        kdniaoApiService.handlePushTracking(body);

                if (!traces.isEmpty()) {
                    log.info("从推送中解析到{}条轨迹: logisticCode={}", traces.size(), logisticCode);
                    processedCount++;
                }
            }

            log.info("快递鸟回调处理完成: processedCount={}", processedCount);
            return "SUCCESS";

        } catch (Exception e) {
            log.error("处理快递鸟回调异常: {}", e.getMessage(), e);
            return "FAIL";
        }
    }

    /**
     * 测试回调接口
     */
    @GetMapping("/callback/test")
    public String testCallback() {
        return "Kdniao callback endpoint is working";
    }
}
