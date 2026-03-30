package com.douyin.shipping.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * API配置类
 * 配置快递鸟API参数
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "api")
public class ApiConfig {

    /**
     * 快递鸟API配置
     */
    private KdniaoConfig kdniao = new KdniaoConfig();

    @Data
    public static class KdniaoConfig {
        /**
         * 商户ID
         */
        private String eBusinessId;

        /**
         * API Key
         */
        private String apiKey;

        /**
         * 请求地址
         */
        private String reqUrl = "https://api.kdniao.com/Ebusiness/EbusinessOrderHandle.aspx";

        /**
         * 轨迹订阅回调地址（可选，用于接收快递鸟推送的轨迹）
         */
        private String callbackUrl;
    }
}
