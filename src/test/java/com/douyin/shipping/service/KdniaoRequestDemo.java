package com.douyin.shipping.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 快递鸟API原始请求测试类
 * 使用原生HTTP请求调用快递鸟接口（用于测试和对比）
 *
 * @author kdniao
 * @date 2025/07/21 11:44
 */
@Slf4j
public class KdniaoRequestDemo {

    /**
     * 字符编码
     **/
    private static final String charset = "UTF-8";

    /**
     * 客户编码，快递鸟提供，注意保管，不要泄漏
     **/
    private static final String customerCode = "1916295";
    /**
     * 客户秘钥，快递鸟提供，注意保管，不要泄漏
     **/
    private static final String appKey = "05d4550f-9b33-42b6-8480-51ac90c6d2b4";
    /**
     * 接口请求地址（沙箱环境）
     **/
    private static final String sandRequestUrl = "http://183.62.170.46:8660/api/dist";
    /**
     * 接口请求地址（正式环境）
     **/
    private static final String prodRequestUrl = "https://api.kdniao.com/api/dist";

    /**
     * 测试快递查询接口(8002)
     * 使用原始URL请求方式，仅供参考
     */
    @Test
    public void testQueryTracking() throws Exception {
        // 为方便理解使用原始URL请求，此处案例仅供参考。真实使用可按贵司需要进行自行业务封装，或使用第三方工具包如OkHttp、Hutool工具类等

        // 快递查询
        // 组装应用级参数，详细字段说明参考接口文档
        String requestData = "{\"LogisticCode\":\"9815069645647\"}";
        String result = remoteRequest(InterfaceType.R8002.getCode(), requestData);
        log.info("{}结果：{}", InterfaceType.R8002.getDesc(), result);
        System.out.println(InterfaceType.R8002.getDesc() + "结果：" + result + "\n");
    }

    /**
     * 远程请求
     *
     * @param interfaceType 接口码
     * @param requestData   应用级参数
     * @return java.lang.String
     * @author kdniao
     * @date 2025/07/21 17:14
     **/
    private String remoteRequest(String interfaceType, String requestData) throws Exception {
        // 组装系统级参数
        Map<String, Object> params = new HashMap<>(5);
        params.put("EBusinessID", customerCode);
        params.put("RequestType", interfaceType);
        params.put("RequestData", requestData);
        // 生成签名：DataSign = URLEncode(Base64(MD5(RequestData + ApiKey)))
        String dataSign = encrypt(requestData, appKey, charset);
        params.put("DataSign", dataSign);
        params.put("DataType", 2);
        // 以form表单形式提交post请求，post请求体中包含了应用级参数和系统级参数
        return sendPost(prodRequestUrl, params);
    }

    /**
     * MD5加密
     * str 内容
     * charset 编码方式
     *
     * @throws Exception
     */
    @SuppressWarnings("unused")
    private String MD5(String str, String charset) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(str.getBytes(charset));
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
     * base64编码
     * str 内容
     * charset 编码方式
     *
     * @throws UnsupportedEncodingException
     */
    private String base64(String str, String charset) throws UnsupportedEncodingException {
        return Base64.getEncoder().encodeToString(str.getBytes(charset));
    }

    @SuppressWarnings("unused")
    private String urlEncoder(String str) throws UnsupportedEncodingException {
        return URLEncoder.encode(str, charset);
    }

    /**
     * Sign签名生成
     * <p>
     * 1、数据内容签名，加密方法为：把(请求内容(未编码)+ApiKey)进行MD5加密--32位小写，然后Base64编码，最后进行URL(utf-8)编码
     * <p>
     *
     * @param content  内容
     * @param keyValue ApiKey
     * @param charset  编码方式
     * @return java.lang.String dataSign签名
     * @author kdniao
     * @date 2024/12/20 15:25
     **/
    private String encrypt(String content, String keyValue, String charset) throws Exception {
        String signStr;
        if (keyValue != null) {
            signStr = base64(MD5(content + keyValue, charset), charset);
        } else {
            signStr = base64(MD5(content, charset), charset);
        }
        // 最后进行URL编码
        return URLEncoder.encode(signStr, charset);
    }

    /**
     * 向指定 URL 发送POST方法的请求
     * url 发送请求的 URL
     * params 请求的参数集合
     *
     * @return 远程资源的响应结果
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
            out = new OutputStreamWriter(conn.getOutputStream(), charset);
            // 发送请求参数
            if (params != null) {
                StringBuilder param = new StringBuilder();
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    if (param.length() > 0) {
                        param.append("&");
                    }
                    param.append(entry.getKey());
                    param.append("=");
                    // 对参数值进行URL编码
                    param.append(URLEncoder.encode(String.valueOf(entry.getValue()), charset));
                    System.out.println(entry.getKey() + ":" + entry.getValue());
                }
                System.out.println("param:" + param.toString());
                out.write(param.toString());
            }
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), charset));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result.toString();
    }

    /**
     * 接口类型
     *
     * @author kdniao
     * @date 2024/12/20 15:52
     **/
    public enum InterfaceType {

        R1001("1001", "预约取件"),
        R1004("1004", "取消订单"),
        R1007("1007", "电子面单"),
        R1147("1147", "回收单号"),
        R1150("1150", "电子面单追加子单"),
        R1157("1157", "获取电子面单模板"),

        R1002("1002", "即时查询"),
        R1008("1008", "物流跟踪"),
        R8001("8001", "在途监控（即时）"),
        R8002("8002", "快递查询"),
        R8003("8003", "轨迹地图（即时）"),
        R8008("8008", "在途监控（订阅）"),
        R8005("8005", "轨迹地图（订阅）"),
        R6001("6001", "智能地址解析"),
        R6002("6002", "网点查询2.0"),
        R6003("6003", "快递号码识别"),
        R6004("6004", "预计到达时间"),
        R6006("6006", "预计到达时间(发货后)"),
        R6016("6016", "预计到达时间(发货后-按单)"),
        R6018("6018", "预计到达时间-订阅"),
        R6005("6005", "物流拦截"),
        R1161("1161", "签单回传"),
        R1158("1158", "快递可服务性"),
        R6007("6007", "整车节点"),
        R6008("6008", "整车规划"),
        R6009("6009", "整车画线"),
        R2002("2002", "单号识别");

        private String code;
        private String desc;

        InterfaceType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }
}
