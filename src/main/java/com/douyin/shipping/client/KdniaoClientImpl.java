package com.douyin.shipping.client;

import com.douyin.shipping.config.ApiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 快递鸟HTTP客户端实现
 * 只负责封装HTTP请求和基础参数（签名、EBusinessID等）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KdniaoClientImpl implements KdniaoClient {

    private final ApiConfig apiConfig;

    @Override
    public String sendRequest(String requestData, String requestType) {
        try {
            Map<String, Object> params = buildRequestParams(requestData, requestType);
            return sendPost(apiConfig.getKdniao().getReqUrl(), params);
        } catch (Exception e) {
            log.error("快递鸟API请求异常: requestType={}, error={}", requestType, e.getMessage(), e);
            return "{\"Success\":false,\"Reason\":\"请求异常: " + e.getMessage() + "\"}";
        }
    }

    /**
     * 构建请求参数
     * 参考Demo的remoteRequest方法
     */
    private Map<String, Object> buildRequestParams(String requestData, String requestType) throws Exception {
        ApiConfig.KdniaoConfig config = apiConfig.getKdniao();

        Map<String, Object> params = new HashMap<>(5);
        params.put("EBusinessID", config.getEBusinessId());
        params.put("RequestType", requestType);
        params.put("RequestData", requestData);
        // 生成签名：DataSign = URLEncode(Base64(MD5(RequestData + ApiKey)))
        String dataSign = generateDataSign(requestData, config.getApiKey());
        params.put("DataSign", dataSign);
        params.put("DataType", 2);

        return params;
    }

    /**
     * 生成数据签名
     * 快递鸟签名规则：URLEncode(Base64(MD5(请求内容(未编码)+ApiKey)))
     * 与Demo的encrypt方法完全一致
     * 步骤：
     * 1. 拼接字符串和密钥：content + apiKey
     * 2. 对拼接后的字符串进行MD5加密（32位小写）
     * 3. 对MD5结果进行Base64编码
     * 4. 对Base64结果进行URL编码
     */
    private String generateDataSign(String content, String apiKey) throws Exception {
        String signStr;
        if (apiKey != null) {
            signStr = base64Encode(md5(content + apiKey, StandardCharsets.UTF_8.name()), StandardCharsets.UTF_8.name());
        } else {
            signStr = base64Encode(md5(content, StandardCharsets.UTF_8.name()), StandardCharsets.UTF_8.name());
        }
        // 调试日志
        log.debug("签名调试 - Base64结果: {}", signStr);
        // 最后进行URL编码
        String urlEncodedSign = URLEncoder.encode(signStr, StandardCharsets.UTF_8.name());
        log.debug("签名调试 - URL编码后: {}", urlEncodedSign);
        return urlEncodedSign;
    }

    /**
     * MD5加密，返回32位小写
     * 与Demo的MD5方法完全一致
     *
     * @param content 内容
     * @param charset 编码方式
     * @return 32位小写MD5字符串
     */
    private String md5(String content, String charset) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(content.getBytes(charset));
        byte[] result = md.digest();
        StringBuilder sb = new StringBuilder(32);
        for (byte b : result) {
            int val = b & 0xff;
            if (val <= 0xf) {
                sb.append("0");
            }
            sb.append(Integer.toHexString(val));
        }
        return sb.toString().toLowerCase();
    }

    /**
     * Base64编码
     * 与Demo的base64方法完全一致
     *
     * @param content 内容
     * @param charset 编码方式
     * @return Base64编码字符串
     * @throws UnsupportedEncodingException
     */
    private String base64Encode(String content, String charset) throws UnsupportedEncodingException {
        return Base64.getEncoder().encodeToString(content.getBytes(charset));
    }

    /**
     * 发送POST请求
     * 与Demo的sendPost方法完全一致
     * 注意：由于DataSign在generateDataSign中已经做了URL编码，
     * 此处对所有参数值再次进行URL编码（与Demo行为一致）
     */
    private String sendPost(String url, Map<String, Object> params) {
        OutputStreamWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try {
            URL realUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // POST方法
            conn.setRequestMethod("POST");
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.connect();
            // 获取URLConnection对象对应的输出流
            out = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8.name());
            // 发送请求参数
            if (params != null) {
                StringBuilder param = new StringBuilder();
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    if (param.length() > 0) {
                        param.append("&");
                    }
                    param.append(entry.getKey());
                    param.append("=");
                    // 对参数值进行URL编码（与Demo一致）
                    param.append(URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8.name()));
                    log.debug("请求参数 {}: {}", entry.getKey(), entry.getValue());
                }
                log.debug("请求参数字符串: {}", param.toString());
                out.write(param.toString());
            }
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8.name()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            log.error("发送POST请求出现异常: {}", e.getMessage(), e);
        } finally {
            // 使用finally块来关闭输出流、输入流
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                log.error("关闭流异常: {}", ex.getMessage());
            }
        }
        return result.toString();
    }
}
