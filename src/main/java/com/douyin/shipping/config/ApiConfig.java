package com.douyin.shipping.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * API配置类
 * 配置抖店和快递鸟的API参数
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "api")
public class ApiConfig {

    /**
     * 抖店API配置
     */
    private DouyinConfig douyin = new DouyinConfig();

    /**
     * 快递鸟API配置
     */
    private KdniaoConfig kdniao = new KdniaoConfig();

    @Data
    public static class DouyinConfig {
        /**
         * 应用Key
         */
        private String appKey;

        /**
         * 应用密钥
         */
        private String appSecret;

        /**
         * 店铺ID
         */
        private String shopId;

        /**
         * API网关地址
         */
        private String gatewayUrl = "https://openapi-fxg.jinritemai.com";

        /**
         * 是否沙箱环境
         */
        private boolean sandbox = false;

        /**
         * 沙箱环境地址
         */
        private String sandboxUrl = "https://openapi-sandbox.jinritemai.com";
    }

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
