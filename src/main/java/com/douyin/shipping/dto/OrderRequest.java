package com.douyin.shipping.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 订单创建/更新请求
 */
@Data
public class OrderRequest {

    /** 抖音订单号 */
    @NotBlank(message = "抖音订单号不能为空")
    private String douyinOrderNo;

    /** 淘宝订单号 */
    private String taobaoOrderNo;

    /** 商品名称 */
    private String productName;

    /** 收件人姓名 */
    @NotBlank(message = "收件人姓名不能为空")
    private String receiverName;

    /** 收件人电话 */
    @NotBlank(message = "收件人电话不能为空")
    private String receiverPhone;

    /** 收件人省份 */
    private String receiverProvince;

    /** 收件人城市 */
    private String receiverCity;

    /** 收件人区县 */
    private String receiverDistrict;

    /** 收件人详细地址 */
    @NotBlank(message = "收件人地址不能为空")
    private String receiverAddress;

    /** 备注 */
    private String remark;
}
