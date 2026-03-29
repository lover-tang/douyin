package com.douyin.shipping.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 物流轨迹实体
 */
@Data
@Entity
@Table(name = "t_tracking_info")
public class TrackingInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联订单ID */
    @Column(nullable = false)
    private Long orderId;

    /** 快递单号（淘宝） */
    @Column(nullable = false)
    private String trackingNo;

    /** 物流公司 */
    private String logisticsCompany;

    /** 轨迹时间 */
    @Column(nullable = false)
    private LocalDateTime trackingTime;

    /** 轨迹描述 */
    @Column(nullable = false, length = 500)
    private String description;

    /** 轨迹位置 */
    private String location;

    /**
     * 轨迹状态:
     * COLLECTED-已揽收, IN_TRANSIT-运输中, DELIVERING-派送中, SIGNED-已签收, EXCEPTION-异常
     */
    private String trackingStatus;

    /** 是否已同步到抖音 */
    @Column(nullable = false)
    private Boolean synced = false;

    /** 同步时间 */
    private LocalDateTime syncTime;

    /** 同步结果说明 */
    private String syncResult;

    @CreationTimestamp
    private LocalDateTime createTime;
}
