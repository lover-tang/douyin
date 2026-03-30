CREATE TABLE `express_delivery` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `ebusiness_id` VARCHAR(32) NOT NULL COMMENT '快递鸟商户ID',
    `shipper_code` VARCHAR(16) NOT NULL COMMENT '快递公司编码(ZTO/SF等)',
    `logistic_code` VARCHAR(64) NOT NULL COMMENT '物流单号',
    `order_code` VARCHAR(64) DEFAULT NULL COMMENT '订单编号(商户系统)',
    `state` TINYINT DEFAULT NULL COMMENT '物流状态(2在途中/3已签收/4问题件/5已取件/6待派送/7派送中/8待取件)',
    `state_ex` VARCHAR(8) DEFAULT NULL COMMENT '物流状态扩展码',
    `location` VARCHAR(64) DEFAULT NULL COMMENT '最新轨迹位置',
    `delivery_man_name` VARCHAR(32) DEFAULT NULL COMMENT '派送员姓名',
    `delivery_man_tel` VARCHAR(32) DEFAULT NULL COMMENT '派送员电话',
    `estimated_delivery_time` DATETIME DEFAULT NULL COMMENT '预计送达时间',
    `first_query_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '首次查询时间',
    `last_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `is_subscribed` TINYINT DEFAULT 0 COMMENT '是否已订阅物流推送(0否/1是)',
    `callback_url` VARCHAR(256) DEFAULT NULL COMMENT '订阅回调地址',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注信息',
    INDEX `idx_logistic_code` (`logistic_code`),
    INDEX `idx_shipper_code` (`shipper_code`),
    INDEX `idx_state` (`state`),
    INDEX `idx_last_update_time` (`last_update_time`),
    UNIQUE KEY `uk_logistic_shipper` (`logistic_code`, `shipper_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='快递运单主表';

CREATE TABLE `express_track` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `express_id` BIGINT NOT NULL COMMENT '关联快递主表ID',
    `logistic_code` VARCHAR(64) NOT NULL COMMENT '物流单号',
    `shipper_code` VARCHAR(16) NOT NULL COMMENT '快递公司编码',
    `action_code` VARCHAR(8) DEFAULT NULL COMMENT '操作类型码(1揽收/2发往/202派送中/204到达/412代收点存放)',
    `accept_time` DATETIME NOT NULL COMMENT '轨迹发生时间',
    `accept_station` VARCHAR(512) NOT NULL COMMENT '轨迹描述信息',
    `location` VARCHAR(64) DEFAULT NULL COMMENT '轨迹所在城市',
    `track_order` INT DEFAULT 0 COMMENT '轨迹顺序(从小到大表示时间顺序)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    INDEX `idx_express_id` (`express_id`),
    INDEX `idx_logistic_code` (`logistic_code`),
    INDEX `idx_accept_time` (`accept_time`),
    INDEX `idx_location` (`location`),
    UNIQUE KEY `uk_logistic_time` (`logistic_code`, `accept_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='快递轨迹明细表';

CREATE TABLE `express_subscription` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `express_id` BIGINT NOT NULL COMMENT '关联快递主表ID',
    `logistic_code` VARCHAR(64) NOT NULL COMMENT '物流单号',
    `shipper_code` VARCHAR(16) NOT NULL COMMENT '快递公司编码',
    `callback_url` VARCHAR(256) NOT NULL COMMENT '回调地址',
    `subscription_status` TINYINT DEFAULT 1 COMMENT '订阅状态(0已取消/1订阅中/2已过期)',
    `last_push_time` DATETIME DEFAULT NULL COMMENT '最后一次推送时间',
    `push_success_count` INT DEFAULT 0 COMMENT '推送成功次数',
    `push_fail_count` INT DEFAULT 0 COMMENT '推送失败次数',
    `subscribe_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '订阅时间',
    `expire_time` DATETIME DEFAULT NULL COMMENT '订阅过期时间',
    INDEX `idx_express_id` (`express_id`),
    INDEX `idx_logistic_code` (`logistic_code`),
    INDEX `idx_subscription_status` (`subscription_status`),
    INDEX `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流订阅记录表';
