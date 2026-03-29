package com.douyin.shipping.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 物流公司映射表 - 淘宝物流公司与抖音物流公司的对应关系
 */
@Data
@Entity
@Table(name = "t_logistics_mapping")
public class LogisticsMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 淘宝物流公司名称 */
    @Column(nullable = false, unique = true)
    private String taobaoName;

    /** 淘宝物流公司编码 */
    private String taobaoCode;

    /** 抖音物流公司名称 */
    @Column(nullable = false)
    private String douyinName;

    /** 抖音物流公司编码 */
    private String douyinCode;

    /** 快递鸟物流公司编码 */
    private String kdniaoCode;

    /** 是否启用 */
    @Column(nullable = false)
    private Boolean enabled = true;

    @CreationTimestamp
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;
}
