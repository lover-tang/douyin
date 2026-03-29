package com.douyin.shipping.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 面单转换请求
 */
@Data
public class LabelConvertRequest {

    /** 订单ID */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /** 淘宝快递公司 */
    @NotBlank(message = "淘宝快递公司不能为空")
    private String taobaoLogisticsCompany;

    /** 淘宝快递单号 */
    @NotBlank(message = "淘宝快递单号不能为空")
    private String taobaoTrackingNo;

    /** 发件人姓名（你的店铺名或个人名） */
    @NotBlank(message = "发件人姓名不能为空")
    private String senderName;

    /** 发件人电话 */
    @NotBlank(message = "发件人电话不能为空")
    private String senderPhone;

    /** 发件人地址 */
    @NotBlank(message = "发件人地址不能为空")
    private String senderAddress;
}
