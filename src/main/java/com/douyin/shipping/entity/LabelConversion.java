package com.douyin.shipping.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 面单转换记录
 */
@Data
@Entity
@Table(name = "t_label_conversion")
public class LabelConversion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联订单ID */
    @Column(nullable = false)
    private Long orderId;

    /** 抖音订单号 */
    @Column(nullable = false)
    private String douyinOrderNo;

    /** 淘宝快递公司 */
    private String taobaoLogisticsCompany;

    /** 淘宝快递单号 */
    private String taobaoTrackingNo;

    /** 转换后抖音快递公司 */
    private String douyinLogisticsCompany;

    /** 转换后抖音快递单号 */
    private String douyinTrackingNo;

    /** 发件人姓名（你的店铺） */
    private String senderName;

    /** 发件人电话 */
    private String senderPhone;

    /** 发件人地址 */
    private String senderAddress;

    /** 收件人姓名 */
    private String receiverName;

    /** 收件人电话 */
    private String receiverPhone;

    /** 收件人地址 */
    private String receiverAddress;

    /**
     * 转换状态: 0-待转换, 1-已转换, 2-转换失败
     */
    @Column(nullable = false)
    private Integer status = 0;

    /** 失败原因 */
    private String failReason;

    @CreationTimestamp
    private LocalDateTime createTime;
}
