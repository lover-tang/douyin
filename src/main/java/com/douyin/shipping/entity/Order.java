package com.douyin.shipping.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 订单实体 - 关联抖音订单和淘宝采购单
 */
@Data
@Entity
@Table(name = "t_order")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 抖音订单号 */
    @Column(nullable = false, unique = true)
    private String douyinOrderNo;

    /** 淘宝订单号 */
    private String taobaoOrderNo;

    /** 商品名称 */
    private String productName;

    /** 收件人姓名（抖音买家） */
    @Column(nullable = false)
    private String receiverName;

    /** 收件人电话 */
    @Column(nullable = false)
    private String receiverPhone;

    /** 收件人省份 */
    private String receiverProvince;

    /** 收件人城市 */
    private String receiverCity;

    /** 收件人区县 */
    private String receiverDistrict;

    /** 收件人详细地址 */
    @Column(nullable = false)
    private String receiverAddress;

    /** 淘宝物流公司 */
    private String taobaoLogisticsCompany;

    /** 淘宝快递单号 */
    private String taobaoTrackingNo;

    /** 抖音物流公司 */
    private String douyinLogisticsCompany;

    /** 抖音快递单号（转换后） */
    private String douyinTrackingNo;

    /**
     * 订单状态:
     * 0-待采购, 1-已采购待发货, 2-已发货(面单已转换), 3-轨迹同步中, 4-已签收, 5-异常
     */
    @Column(nullable = false)
    private Integer status = 0;

    /** 面单是否已转换 */
    private Boolean labelConverted = false;

    /** 轨迹是否同步开启 */
    private Boolean trackingSyncEnabled = false;

    /** 最后同步时间 */
    private LocalDateTime lastSyncTime;

    /** 备注 */
    private String remark;

    @CreationTimestamp
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;
}
