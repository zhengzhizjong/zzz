-- ============================================================
-- 忠济堂中医养生连锁管理系统 - 全量建表脚本
-- 版本: V1.0.0
-- 数据库: zjt_dev
-- 字符集: utf8mb4 | 排序: utf8mb4_unicode_ci
-- 说明: 包含系统全部45张业务表
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;

-- ============================================================
-- 创建数据库（如不存在）
-- ============================================================
CREATE DATABASE IF NOT EXISTS `zjt_dev`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `zjt_dev`;

-- ============================================================
-- 1. store_info - 门店表
-- ============================================================
DROP TABLE IF EXISTS `store_info`;
CREATE TABLE `store_info` (
  `id`                  BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`           BIGINT        NOT NULL                COMMENT '租户ID',
  `store_id`            BIGINT                                COMMENT '门店ID(自关联,本表即门店)',
  `store_no`            VARCHAR(32)   NOT NULL                COMMENT '门店编号',
  `store_name`          VARCHAR(128)  NOT NULL                COMMENT '门店名称',
  `store_type`          TINYINT       DEFAULT 1               COMMENT '门店类型:1直营 2加盟',
  `brand_id`            BIGINT                                COMMENT '品牌ID',
  `province_code`       VARCHAR(20)                           COMMENT '省份编码',
  `city_code`           VARCHAR(20)                           COMMENT '城市编码',
  `district_code`       VARCHAR(20)                           COMMENT '区县编码',
  `address`             VARCHAR(256)                          COMMENT '详细地址',
  `latitude`            DECIMAL(10,8)                         COMMENT '纬度',
  `longitude`           DECIMAL(11,8)                         COMMENT '经度',
  `contact_name`        VARCHAR(64)                           COMMENT '联系人姓名',
  `contact_phone`       VARCHAR(20)                           COMMENT '联系电话',
  `business_start_time` TIME                                  COMMENT '营业开始时间',
  `business_end_time`   TIME                                  COMMENT '营业结束时间',
  `room_count`          INT                                   COMMENT '房间数',
  `technician_count`    INT                                   COMMENT '技师数',
  `region_id`           BIGINT                                COMMENT '区域ID',
  `status`              TINYINT       DEFAULT 1               COMMENT '门店状态:1营业中 2休息中 3装修中 4已关闭',
  `created_at`          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`          VARCHAR(64)                           COMMENT '创建人',
  `updated_by`          VARCHAR(64)                           COMMENT '更新人',
  `is_deleted`          TINYINT       NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_store_no` (`store_no`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店表';

-- ============================================================
-- 2. store_room - 房间表
-- ============================================================
DROP TABLE IF EXISTS `store_room`;
CREATE TABLE `store_room` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`   BIGINT       NOT NULL                COMMENT '租户ID',
  `store_id`    BIGINT                               COMMENT '门店ID',
  `room_no`     VARCHAR(32)  NOT NULL                COMMENT '房间编号',
  `room_name`   VARCHAR(64)  NOT NULL                COMMENT '房间名称',
  `room_type`   TINYINT                              COMMENT '房间类型:1普通 2VIP 3套间',
  `status`      TINYINT      DEFAULT 1               COMMENT '房间状态:1空闲 2使用中 3维护中',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`  VARCHAR(64)                          COMMENT '创建人',
  `updated_by`  VARCHAR(64)                          COMMENT '更新人',
  `is_deleted`  TINYINT      NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房间表';

-- ============================================================
-- 3. store_technician - 技师表
-- ============================================================
DROP TABLE IF EXISTS `store_technician`;
CREATE TABLE `store_technician` (
  `id`                   BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`            BIGINT        NOT NULL                COMMENT '租户ID',
  `store_id`             BIGINT                                COMMENT '门店ID',
  `employee_id`          BIGINT        NOT NULL                COMMENT '员工ID',
  `technician_no`        VARCHAR(32)   NOT NULL                COMMENT '技师编号',
  `skill_level`          TINYINT                               COMMENT '技能等级:1初级 2中级 3高级 4专家',
  `skilled_items`        VARCHAR(512)                          COMMENT '擅长项目(JSON)',
  `default_schedule`     VARCHAR(50)                           COMMENT '默认班次',
  `month_service_count`  INT           DEFAULT 0               COMMENT '本月服务次数',
  `month_revenue`        DECIMAL(12,2) DEFAULT 0               COMMENT '本月营收',
  `month_rating`         DECIMAL(3,2)                          COMMENT '本月评分',
  `total_service_count`  INT           DEFAULT 0               COMMENT '累计服务次数',
  `status`               TINYINT       DEFAULT 1               COMMENT '技师状态:1在职 2休息 3离职',
  `is_online`            TINYINT       DEFAULT 0               COMMENT '是否在岗:0休息 1在岗',
  `created_at`           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`           VARCHAR(64)                           COMMENT '创建人',
  `updated_by`           VARCHAR(64)                           COMMENT '更新人',
  `is_deleted`           TINYINT       NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_technician_no` (`technician_no`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技师表';

-- ============================================================
-- 4. store_technician_skill - 技师技能表
-- ============================================================
DROP TABLE IF EXISTS `store_technician_skill`;
CREATE TABLE `store_technician_skill` (
  `id`               BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`        BIGINT NOT NULL                COMMENT '租户ID',
  `store_id`         BIGINT                         COMMENT '门店ID',
  `technician_id`    BIGINT NOT NULL                COMMENT '技师ID',
  `service_item_id`  BIGINT NOT NULL                COMMENT '服务项目ID',
  `proficiency`      TINYINT                        COMMENT '熟练度:1入门 2熟练 3精通',
  `created_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`       VARCHAR(64)                    COMMENT '创建人',
  `updated_by`       VARCHAR(64)                    COMMENT '更新人',
  `is_deleted`       TINYINT NOT NULL DEFAULT 0     COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技师技能表';

-- ============================================================
-- 5. store_schedule - 排班表
-- ============================================================
DROP TABLE IF EXISTS `store_schedule`;
CREATE TABLE `store_schedule` (
  `id`             BIGINT    NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`      BIGINT    NOT NULL                COMMENT '租户ID',
  `store_id`       BIGINT                            COMMENT '门店ID',
  `employee_id`    BIGINT    NOT NULL                COMMENT '员工ID',
  `schedule_date`  DATE      NOT NULL                COMMENT '排班日期',
  `start_time`     TIME      NOT NULL                COMMENT '上班时间',
  `end_time`       TIME      NOT NULL                COMMENT '下班时间',
  `schedule_type`  TINYINT                           COMMENT '排班类型:1上班 2休息 3请假',
  `created_at`     DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`     DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`     VARCHAR(64)                       COMMENT '创建人',
  `updated_by`     VARCHAR(64)                       COMMENT '更新人',
  `is_deleted`     TINYINT   NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排班表';

-- ============================================================
-- 6. store_technician_promotion - 技师推广表
-- ============================================================
DROP TABLE IF EXISTS `store_technician_promotion`;
CREATE TABLE `store_technician_promotion` (
  `id`                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`         BIGINT        NOT NULL                COMMENT '租户ID',
  `store_id`          BIGINT                                COMMENT '门店ID',
  `technician_id`     BIGINT        NOT NULL                COMMENT '技师ID',
  `promo_code`        VARCHAR(16)   NOT NULL                COMMENT '推广码',
  `share_link`        VARCHAR(512)                          COMMENT '分享链接',
  `click_count`       INT           DEFAULT 0               COMMENT '点击次数',
  `register_count`    INT           DEFAULT 0               COMMENT '注册人数',
  `visit_count`       INT           DEFAULT 0               COMMENT '到店人数',
  `order_count`       INT           DEFAULT 0               COMMENT '下单人数',
  `commission_earned` DECIMAL(12,2) DEFAULT 0               COMMENT '已结算佣金',
  `commission_pending`DECIMAL(12,2) DEFAULT 0               COMMENT '待结算佣金',
  `status`            TINYINT       DEFAULT 1               COMMENT '状态:1启用 2停用',
  `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`        VARCHAR(64)                           COMMENT '创建人',
  `updated_by`        VARCHAR(64)                           COMMENT '更新人',
  `is_deleted`        TINYINT       NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_promo_code` (`promo_code`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技师推广表';

-- ============================================================
-- 7. store_time_slot_config - 时段配置表
-- ============================================================
DROP TABLE IF EXISTS `store_time_slot_config`;
CREATE TABLE `store_time_slot_config` (
  `id`                     BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`              BIGINT   NOT NULL                COMMENT '租户ID',
  `store_id`               BIGINT   NOT NULL                COMMENT '门店ID',
  `business_start_time`    TIME     NOT NULL                COMMENT '营业开始时间',
  `business_end_time`      TIME     NOT NULL                COMMENT '营业结束时间',
  `slot_duration_minutes`  INT      NOT NULL DEFAULT 60     COMMENT '时段时长(分钟)',
  `rest_start_time`        TIME                             COMMENT '休息开始时间',
  `rest_end_time`          TIME                             COMMENT '休息结束时间',
  `status`                 TINYINT  DEFAULT 1               COMMENT '状态:1启用 2停用',
  `created_at`             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`             VARCHAR(64)                      COMMENT '创建人',
  `updated_by`             VARCHAR(64)                      COMMENT '更新人',
  `is_deleted`             TINYINT  NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='时段配置表';

-- ============================================================
-- 8. content_service_item - 服务项目表
-- ============================================================
DROP TABLE IF EXISTS `content_service_item`;
CREATE TABLE `content_service_item` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`        BIGINT        NOT NULL                COMMENT '租户ID',
  `store_id`         BIGINT                                COMMENT '门店ID',
  `item_no`          VARCHAR(32)   NOT NULL                COMMENT '项目编号',
  `item_name`        VARCHAR(128)  NOT NULL                COMMENT '项目名称',
  `category_id`      BIGINT                                COMMENT '分类ID',
  `category_name`    VARCHAR(64)                           COMMENT '分类名称',
  `price`            DECIMAL(10,2) NOT NULL                COMMENT '售价',
  `cost_price`       DECIMAL(10,2)                         COMMENT '成本价',
  `duration_minutes` INT           NOT NULL DEFAULT 60     COMMENT '时长(分钟)',
  `commission_type`  TINYINT       DEFAULT 1               COMMENT '提成类型:1固定 2比例',
  `commission_value` DECIMAL(10,2) DEFAULT 0               COMMENT '提成值',
  `is_package`       TINYINT       DEFAULT 0               COMMENT '是否套餐:0否 1是',
  `is_ai_recommended`TINYINT       DEFAULT 0               COMMENT '是否AI推荐:0否 1是',
  `tags`             VARCHAR(256)                          COMMENT '标签',
  `description`      TEXT                                  COMMENT '项目描述',
  `status`           TINYINT       DEFAULT 1               COMMENT '状态:1上架 2下架 3维护中',
  `sort_order`       INT           DEFAULT 0               COMMENT '排序',
  `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`       VARCHAR(64)                           COMMENT '创建人',
  `updated_by`       VARCHAR(64)                           COMMENT '更新人',
  `is_deleted`       TINYINT       NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_item_no` (`item_no`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务项目表';

-- ============================================================
-- 9. trade_appointment - 预约表
-- ============================================================
DROP TABLE IF EXISTS `trade_appointment`;
CREATE TABLE `trade_appointment` (
  `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`           BIGINT       NOT NULL                COMMENT '租户ID',
  `store_id`            BIGINT                               COMMENT '门店ID',
  `appointment_no`      VARCHAR(32)  NOT NULL                COMMENT '预约编号',
  `member_id`           BIGINT                               COMMENT '会员ID',
  `member_name`         VARCHAR(64)                          COMMENT '会员姓名',
  `member_phone`        VARCHAR(20)                          COMMENT '会员电话',
  `service_item_id`     BIGINT       NOT NULL                COMMENT '服务项目ID',
  `service_item_name`   VARCHAR(128)                         COMMENT '服务项目名称',
  `technician_id`       BIGINT                               COMMENT '技师ID(不指定时为NULL)',
  `technician_name`     VARCHAR(64)                          COMMENT '技师姓名',
  `room_id`             BIGINT                               COMMENT '房间ID',
  `appointment_date`    DATE         NOT NULL                COMMENT '预约日期',
  `appointment_time`    TIME         NOT NULL                COMMENT '预约时间',
  `time_slot`           VARCHAR(20)  NOT NULL                COMMENT '时段(如14:00-15:00)',
  `duration_minutes`    INT          DEFAULT 60              COMMENT '服务时长(分钟)',
  `start_time`          DATETIME                             COMMENT '服务开始时间',
  `end_time`            DATETIME                             COMMENT '服务结束时间',
  `status`              TINYINT      DEFAULT 1               COMMENT '预约状态:1待确认 2已确认 3服务中 4已完成 5已取消 6超时未到 7已修改',
  `cancel_reason`       VARCHAR(256)                         COMMENT '取消原因',
  `modify_reason`       VARCHAR(256)                         COMMENT '修改原因',
  `source_channel`      VARCHAR(32)                          COMMENT '来源渠道',
  `source_type`         TINYINT                              COMMENT '来源类型:1客户自约 2前台代约 3技师代约',
  `consultation_notes`  TEXT                                 COMMENT '咨询备注',
  `version`             INT          NOT NULL DEFAULT 0      COMMENT '乐观锁版本号',
  `created_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`          VARCHAR(64)                          COMMENT '创建人',
  `updated_by`          VARCHAR(64)                          COMMENT '更新人',
  `is_deleted`          TINYINT      NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_appointment_no` (`appointment_no`),
  UNIQUE KEY `uk_lock` (`tenant_id`, `store_id`, `technician_id`, `appointment_date`, `time_slot`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约表';

-- ============================================================
-- 10. trade_appointment_lock - 预约锁档表
-- ============================================================
DROP TABLE IF EXISTS `trade_appointment_lock`;
CREATE TABLE `trade_appointment_lock` (
  `id`             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`      BIGINT      NOT NULL                COMMENT '租户ID',
  `store_id`       BIGINT                              COMMENT '门店ID',
  `technician_id`  BIGINT                              COMMENT '技师ID',
  `lock_date`      DATE        NOT NULL                COMMENT '锁定日期',
  `time_slot`      VARCHAR(20) NOT NULL                COMMENT '时段',
  `member_id`      BIGINT                              COMMENT '会员ID',
  `session_id`     VARCHAR(64)                         COMMENT '会话ID',
  `expire_at`      DATETIME    NOT NULL                COMMENT '过期时间',
  `status`         TINYINT     DEFAULT 1               COMMENT '锁状态:1锁定中 2已确认 3已过期',
  `created_at`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`     VARCHAR(64)                         COMMENT '创建人',
  `updated_by`     VARCHAR(64)                         COMMENT '更新人',
  `is_deleted`     TINYINT     NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lock_slot` (`tenant_id`, `store_id`, `technician_id`, `lock_date`, `time_slot`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约锁档表';

-- ============================================================
-- 11. trade_order - 订单表
-- ============================================================
DROP TABLE IF EXISTS `trade_order`;
CREATE TABLE `trade_order` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`        BIGINT        NOT NULL                COMMENT '租户ID',
  `store_id`         BIGINT                                COMMENT '门店ID',
  `order_no`         VARCHAR(32)   NOT NULL                COMMENT '订单编号',
  `order_type`       TINYINT       NOT NULL                COMMENT '订单类型:1散客 2会员 3疗程扣次 4充值 5退款',
  `member_id`        BIGINT                                COMMENT '会员ID',
  `member_name`      VARCHAR(64)                           COMMENT '会员姓名',
  `total_amount`     DECIMAL(10,2) DEFAULT 0               COMMENT '订单总金额',
  `discount_amount`  DECIMAL(10,2) DEFAULT 0               COMMENT '优惠金额',
  `paid_amount`      DECIMAL(10,2) DEFAULT 0               COMMENT '实付金额',
  `points_deduct_amount` DECIMAL(10,2) DEFAULT 0           COMMENT '积分抵扣金额',
  `payment_method`   TINYINT                               COMMENT '支付方式:1微信 2支付宝 3现金 4储值 5积分',
  `payment_status`   TINYINT       DEFAULT 0               COMMENT '支付状态:0待支付 1已支付 2部分退款 3已退款',
  `payment_time`     DATETIME                              COMMENT '支付时间',
  `transaction_id`   VARCHAR(64)                           COMMENT '交易流水号',
  `appointment_id`   BIGINT                                COMMENT '预约ID',
  `technician_id`    BIGINT                                COMMENT '技师ID',
  `source_channel`   VARCHAR(32)                           COMMENT '来源渠道',
  `remark`           VARCHAR(512)                          COMMENT '备注',
  `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`       VARCHAR(64)                           COMMENT '创建人',
  `updated_by`       VARCHAR(64)                           COMMENT '更新人',
  `is_deleted`       TINYINT       NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ============================================================
-- 12. trade_order_item - 订单明细表
-- ============================================================
DROP TABLE IF EXISTS `trade_order_item`;
CREATE TABLE `trade_order_item` (
  `id`                 BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`          BIGINT        NOT NULL                COMMENT '租户ID',
  `store_id`           BIGINT                                COMMENT '门店ID',
  `order_id`           BIGINT        NOT NULL                COMMENT '订单ID',
  `service_item_id`    BIGINT        NOT NULL                COMMENT '服务项目ID',
  `service_item_name`  VARCHAR(128)  NOT NULL                COMMENT '服务项目名称',
  `technician_id`      BIGINT                                COMMENT '技师ID',
  `technician_name`    VARCHAR(64)                           COMMENT '技师姓名',
  `room_id`            BIGINT                                COMMENT '房间ID',
  `unit_price`         DECIMAL(10,2) DEFAULT 0               COMMENT '单价',
  `quantity`           INT           DEFAULT 1               COMMENT '数量',
  `total_amount`       DECIMAL(10,2) DEFAULT 0               COMMENT '总金额',
  `discount_amount`    DECIMAL(10,2) DEFAULT 0               COMMENT '优惠金额',
  `service_start_time` DATETIME                              COMMENT '服务开始时间',
  `service_end_time`   DATETIME                              COMMENT '服务结束时间',
  `created_at`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`         VARCHAR(64)                           COMMENT '创建人',
  `updated_by`         VARCHAR(64)                           COMMENT '更新人',
  `is_deleted`         TINYINT       NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

-- ============================================================
-- 13. trade_payment - 支付记录表
-- ============================================================
DROP TABLE IF EXISTS `trade_payment`;
CREATE TABLE `trade_payment` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`       BIGINT        NOT NULL                COMMENT '租户ID',
  `store_id`        BIGINT                                COMMENT '门店ID',
  `payment_no`      VARCHAR(32)   NOT NULL                COMMENT '支付编号',
  `order_id`        BIGINT        NOT NULL                COMMENT '订单ID',
  `payment_method`  TINYINT       NOT NULL                COMMENT '支付方式:1微信 2支付宝 3现金 4储值 5积分',
  `amount`          DECIMAL(10,2) NOT NULL                COMMENT '支付金额',
  `transaction_id`  VARCHAR(64)                           COMMENT '交易流水号',
  `payment_status`  TINYINT       DEFAULT 0               COMMENT '支付状态:0待支付 1成功 2失败 3已退款',
  `payment_time`    DATETIME                              COMMENT '支付时间',
  `callback_data`   TEXT                                  COMMENT '回调数据',
  `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`      VARCHAR(64)                           COMMENT '创建人',
  `updated_by`      VARCHAR(64)                           COMMENT '更新人',
  `is_deleted`      TINYINT       NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- ============================================================
-- 14. trade_treatment_card - 疗程卡表
-- ============================================================
DROP TABLE IF EXISTS `trade_treatment_card`;
CREATE TABLE `trade_treatment_card` (
  `id`                    BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`             BIGINT        NOT NULL                COMMENT '租户ID',
  `store_id`              BIGINT                                COMMENT '门店ID',
  `card_no`               VARCHAR(32)   NOT NULL                COMMENT '疗程卡编号',
  `member_id`             BIGINT        NOT NULL                COMMENT '会员ID',
  `treatment_package_id`  BIGINT                                COMMENT '疗程套餐ID',
  `card_name`             VARCHAR(128)  NOT NULL                COMMENT '疗程卡名称',
  `total_uses`            INT           NOT NULL                COMMENT '总次数',
  `used_uses`             INT           DEFAULT 0               COMMENT '已用次数',
  `remaining_uses`        INT           NOT NULL                COMMENT '剩余次数',
  `purchase_amount`       DECIMAL(10,2) NOT NULL                COMMENT '购买金额',
  `unit_price`            DECIMAL(10,2)                         COMMENT '单次价格',
  `purchase_time`         DATETIME      NOT NULL                COMMENT '购买时间',
  `expire_time`           DATE                                  COMMENT '过期时间',
  `activation_time`       DATETIME                              COMMENT '激活时间',
  `status`                TINYINT       DEFAULT 1               COMMENT '状态:1未激活 2正常使用 3已用完 4已过期 5已退款',
  `remark`                VARCHAR(256)                          COMMENT '备注',
  `created_at`            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`            VARCHAR(64)                           COMMENT '创建人',
  `updated_by`            VARCHAR(64)                           COMMENT '更新人',
  `is_deleted`            TINYINT       NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_card_no` (`card_no`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='疗程卡表';

-- ============================================================
-- 15. trade_treatment_card_usage - 疗程使用记录表
-- ============================================================
DROP TABLE IF EXISTS `trade_treatment_card_usage`;
CREATE TABLE `trade_treatment_card_usage` (
  `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`  BIGINT   NOT NULL                COMMENT '租户ID',
  `store_id`   BIGINT                           COMMENT '门店ID',
  `card_id`    BIGINT   NOT NULL                COMMENT '疗程卡ID',
  `order_id`   BIGINT                           COMMENT '订单ID',
  `used_uses`  INT      NOT NULL DEFAULT 1      COMMENT '使用次数',
  `used_at`    DATETIME NOT NULL                COMMENT '使用时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64)                      COMMENT '创建人',
  `updated_by` VARCHAR(64)                      COMMENT '更新人',
  `is_deleted` TINYINT  NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='疗程使用记录表';

-- ============================================================
-- 16. trade_refund - 退款记录表
-- ============================================================
DROP TABLE IF EXISTS `trade_refund`;
CREATE TABLE `trade_refund` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`     BIGINT        NOT NULL                COMMENT '租户ID',
  `store_id`      BIGINT                                COMMENT '门店ID',
  `refund_no`     VARCHAR(32)   NOT NULL                COMMENT '退款编号',
  `order_id`      BIGINT        NOT NULL                COMMENT '订单ID',
  `refund_amount` DECIMAL(10,2) NOT NULL                COMMENT '退款金额',
  `refund_reason` VARCHAR(256)                          COMMENT '退款原因',
  `refund_status` TINYINT       DEFAULT 0               COMMENT '退款状态:0待审核 1已通过 2已拒绝 3已退款',
  `refund_time`   DATETIME                              COMMENT '退款时间',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`    VARCHAR(64)                           COMMENT '创建人',
  `updated_by`    VARCHAR(64)                           COMMENT '更新人',
  `is_deleted`    TINYINT       NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款记录表';

-- ============================================================
-- 17. trade_coupon - 优惠券表
-- ============================================================
DROP TABLE IF EXISTS `trade_coupon`;
CREATE TABLE `trade_coupon` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`      BIGINT        NOT NULL                COMMENT '租户ID',
  `store_id`       BIGINT                                COMMENT '门店ID',
  `coupon_no`      VARCHAR(32)   NOT NULL                COMMENT '优惠券编号',
  `coupon_name`    VARCHAR(128)  NOT NULL                COMMENT '优惠券名称',
  `coupon_type`    TINYINT       NOT NULL                COMMENT '优惠券类型:1满减 2折扣 3体验',
  `discount_value` DECIMAL(10,2) NOT NULL                COMMENT '优惠值',
  `min_amount`     DECIMAL(10,2) DEFAULT 0               COMMENT '最低消费金额',
  `total_count`    INT           NOT NULL                COMMENT '总发行量',
  `remain_count`   INT           NOT NULL                COMMENT '剩余数量',
  `start_time`     DATETIME      NOT NULL                COMMENT '开始时间',
  `end_time`       DATETIME      NOT NULL                COMMENT '结束时间',
  `status`         TINYINT       DEFAULT 1               COMMENT '状态:1启用 2停用',
  `created_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`     VARCHAR(64)                           COMMENT '创建人',
  `updated_by`     VARCHAR(64)                           COMMENT '更新人',
  `is_deleted`     TINYINT       NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_coupon_no` (`coupon_no`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券表';

-- ============================================================
-- 18. trade_coupon_record - 优惠券领取/使用记录表
-- ============================================================
DROP TABLE IF EXISTS `trade_coupon_record`;
CREATE TABLE `trade_coupon_record` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`   BIGINT   NOT NULL                COMMENT '租户ID',
  `store_id`    BIGINT                           COMMENT '门店ID',
  `coupon_id`   BIGINT   NOT NULL                COMMENT '优惠券ID',
  `member_id`   BIGINT   NOT NULL                COMMENT '会员ID',
  `status`      TINYINT  DEFAULT 1               COMMENT '状态:1未使用 2已使用 3已过期',
  `used_time`   DATETIME                         COMMENT '使用时间',
  `order_id`    BIGINT                           COMMENT '订单ID',
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`  VARCHAR(64)                      COMMENT '创建人',
  `updated_by`  VARCHAR(64)                      COMMENT '更新人',
  `is_deleted`  TINYINT  NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券领取/使用记录表';

-- ============================================================
-- 19. trade_activity - 活动表
-- ============================================================
DROP TABLE IF EXISTS `trade_activity`;
CREATE TABLE `trade_activity` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`     BIGINT      NOT NULL                COMMENT '租户ID',
  `store_id`      BIGINT                              COMMENT '门店ID',
  `activity_name` VARCHAR(128) NOT NULL               COMMENT '活动名称',
  `activity_type` TINYINT                             COMMENT '活动类型:1限时折扣 2新人优惠 3会员日',
  `start_time`    DATETIME    NOT NULL                COMMENT '开始时间',
  `end_time`      DATETIME    NOT NULL                COMMENT '结束时间',
  `rules_json`    JSON                                COMMENT '活动规则(JSON)',
  `status`        TINYINT     DEFAULT 1               COMMENT '状态:1进行中 2已结束 3已暂停',
  `created_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`    VARCHAR(64)                         COMMENT '创建人',
  `updated_by`    VARCHAR(64)                         COMMENT '更新人',
  `is_deleted`    TINYINT     NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';

-- ============================================================
-- 20. user_member - 会员表
-- ============================================================
DROP TABLE IF EXISTS `user_member`;
CREATE TABLE `user_member` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`        BIGINT        NOT NULL                COMMENT '租户ID',
  `store_id`         BIGINT                                COMMENT '门店ID',
  `member_no`        VARCHAR(32)                           COMMENT '会员编号',
  `name`             VARCHAR(64)   NOT NULL                COMMENT '姓名',
  `phone`            VARCHAR(20)   NOT NULL                COMMENT '手机号',
  `gender`           TINYINT                               COMMENT '性别:0女 1男 2未知',
  `birthday`         DATE                                  COMMENT '生日',
  `avatar`           VARCHAR(512)                          COMMENT '头像URL',
  `constitution_type`TINYINT                               COMMENT '体质类型:1平和 2气虚 3阳虚 4阴虚 5痰湿 6湿热 7血瘀 8气郁 9特禀',
  `constitution_score`DECIMAL(5,2)                         COMMENT '体质评分',
  `constitution_time` DATETIME                             COMMENT '体质测评时间',
  `member_level_id`  BIGINT                                COMMENT '会员等级ID',
  `member_type`      TINYINT       DEFAULT 1               COMMENT '会员类型:1散客 2会员 3VIP',
  `points`           INT           DEFAULT 0               COMMENT '积分',
  `balance`          DECIMAL(10,2) DEFAULT 0               COMMENT '余额',
  `source_channel`   VARCHAR(32)                           COMMENT '来源渠道',
  `referrer_id`      BIGINT                                COMMENT '推荐人ID',
  `status`           TINYINT       DEFAULT 1               COMMENT '状态:1正常 2冻结 3注销',
  `last_visit_time`  DATETIME                              COMMENT '最近到店时间',
  `visit_count`      INT           DEFAULT 0               COMMENT '到店次数',
  `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`       VARCHAR(64)                           COMMENT '创建人',
  `updated_by`       VARCHAR(64)                           COMMENT '更新人',
  `is_deleted`       TINYINT       NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_no` (`member_no`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员表';

-- ============================================================
-- 21. user_member_account - 会员账户表
-- ============================================================
DROP TABLE IF EXISTS `user_member_account`;
CREATE TABLE `user_member_account` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`      BIGINT        NOT NULL                COMMENT '租户ID',
  `store_id`       BIGINT                                COMMENT '门店ID',
  `member_id`      BIGINT        NOT NULL                COMMENT '会员ID',
  `account_type`   TINYINT       NOT NULL                COMMENT '账户类型:1积分 2余额 3疗程',
  `balance`        DECIMAL(12,2) DEFAULT 0               COMMENT '余额',
  `frozen_amount`  DECIMAL(12,2) DEFAULT 0               COMMENT '冻结金额',
  `created_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`     VARCHAR(64)                           COMMENT '创建人',
  `updated_by`     VARCHAR(64)                           COMMENT '更新人',
  `is_deleted`     TINYINT       NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员账户表';

-- ============================================================
-- 22. user_member_level - 会员等级表
-- ============================================================
DROP TABLE IF EXISTS `user_member_level`;
CREATE TABLE `user_member_level` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`     BIGINT      NOT NULL                COMMENT '租户ID',
  `store_id`      BIGINT                              COMMENT '门店ID',
  `level_name`    VARCHAR(64) NOT NULL                COMMENT '等级名称',
  `level_code`    VARCHAR(32) NOT NULL                COMMENT '等级编码',
  `min_points`    INT         NOT NULL                COMMENT '最低积分',
  `max_points`    INT                                 COMMENT '最高积分',
  `discount_rate` DECIMAL(3,2)                        COMMENT '折扣率',
  `benefits_json` JSON                                COMMENT '权益(JSON)',
  `sort_order`    INT         DEFAULT 0               COMMENT '排序',
  `created_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`    VARCHAR(64)                         COMMENT '创建人',
  `updated_by`    VARCHAR(64)                         COMMENT '更新人',
  `is_deleted`    TINYINT     NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_level_code` (`level_code`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员等级表';

-- ============================================================
-- 23. user_employee - 员工表
-- ============================================================
DROP TABLE IF EXISTS `user_employee`;
CREATE TABLE `user_employee` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`     BIGINT      NOT NULL                COMMENT '租户ID',
  `store_id`      BIGINT                              COMMENT '门店ID',
  `employee_no`   VARCHAR(32)                         COMMENT '员工编号',
  `name`          VARCHAR(64) NOT NULL                COMMENT '姓名',
  `phone`         VARCHAR(20) NOT NULL                COMMENT '手机号',
  `gender`        TINYINT                             COMMENT '性别:0女 1男 2未知',
  `department_id` BIGINT                              COMMENT '部门ID',
  `position`      VARCHAR(64)                         COMMENT '职位',
  `role_id`       BIGINT                              COMMENT '角色ID',
  `status`        TINYINT     DEFAULT 1               COMMENT '状态:1在职 2离职',
  `hire_date`     DATE                                COMMENT '入职日期',
  `created_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`    VARCHAR(64)                         COMMENT '创建人',
  `updated_by`    VARCHAR(64)                         COMMENT '更新人',
  `is_deleted`    TINYINT     NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_employee_no` (`employee_no`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工表';

-- ============================================================
-- 24. user_department - 部门表
-- ============================================================
DROP TABLE IF EXISTS `user_department`;
CREATE TABLE `user_department` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`  BIGINT      NOT NULL                COMMENT '租户ID',
  `store_id`   BIGINT                              COMMENT '门店ID',
  `dept_name`  VARCHAR(64) NOT NULL                COMMENT '部门名称',
  `parent_id`  BIGINT      DEFAULT 0               COMMENT '父部门ID',
  `sort_order` INT         DEFAULT 0               COMMENT '排序',
  `status`     TINYINT     DEFAULT 1               COMMENT '状态:1启用 2停用',
  `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64)                         COMMENT '创建人',
  `updated_by` VARCHAR(64)                         COMMENT '更新人',
  `is_deleted` TINYINT     NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- ============================================================
-- 25. user_role - 角色表
-- ============================================================
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`   BIGINT      NOT NULL                COMMENT '租户ID',
  `store_id`    BIGINT                              COMMENT '门店ID',
  `role_name`   VARCHAR(64) NOT NULL                COMMENT '角色名称',
  `role_code`   VARCHAR(32) NOT NULL                COMMENT '角色编码',
  `description` VARCHAR(256)                        COMMENT '描述',
  `status`      TINYINT     DEFAULT 1               COMMENT '状态:1启用 2停用',
  `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`  VARCHAR(64)                         COMMENT '创建人',
  `updated_by`  VARCHAR(64)                         COMMENT '更新人',
  `is_deleted`  TINYINT     NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ============================================================
-- 26. user_permission - 权限表
-- ============================================================
DROP TABLE IF EXISTS `user_permission`;
CREATE TABLE `user_permission` (
  `id`              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`       BIGINT      NOT NULL                COMMENT '租户ID',
  `store_id`        BIGINT                              COMMENT '门店ID',
  `permission_name` VARCHAR(64) NOT NULL                COMMENT '权限名称',
  `permission_code` VARCHAR(64) NOT NULL                COMMENT '权限编码',
  `permission_type` TINYINT                             COMMENT '权限类型:1菜单 2按钮 3API',
  `parent_id`       BIGINT      DEFAULT 0               COMMENT '父权限ID',
  `sort_order`      INT         DEFAULT 0               COMMENT '排序',
  `status`          TINYINT     DEFAULT 1               COMMENT '状态:1启用 2停用',
  `created_at`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`      VARCHAR(64)                         COMMENT '创建人',
  `updated_by`      VARCHAR(64)                         COMMENT '更新人',
  `is_deleted`      TINYINT     NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- ============================================================
-- 27. user_role_permission - 角色权限关联表
-- ============================================================
DROP TABLE IF EXISTS `user_role_permission`;
CREATE TABLE `user_role_permission` (
  `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`     BIGINT   NOT NULL                COMMENT '租户ID',
  `store_id`      BIGINT                           COMMENT '门店ID',
  `role_id`       BIGINT   NOT NULL                COMMENT '角色ID',
  `permission_id` BIGINT   NOT NULL                COMMENT '权限ID',
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`    VARCHAR(64)                      COMMENT '创建人',
  `updated_by`    VARCHAR(64)                      COMMENT '更新人',
  `is_deleted`    TINYINT  NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- ============================================================
-- 28. user_health_profile - 健康档案表
-- ============================================================
DROP TABLE IF EXISTS `user_health_profile`;
CREATE TABLE `user_health_profile` (
  `id`               BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`        BIGINT   NOT NULL                COMMENT '租户ID',
  `store_id`         BIGINT                           COMMENT '门店ID',
  `member_id`        BIGINT   NOT NULL                COMMENT '会员ID',
  `constitution_type`TINYINT                          COMMENT '体质类型:1平和 2气虚 3阳虚 4阴虚 5痰湿 6湿热 7血瘀 8气郁 9特禀',
  `health_data`      JSON                             COMMENT '健康数据(JSON)',
  `last_check_time`  DATETIME                         COMMENT '最近检查时间',
  `is_encrypted`     TINYINT  DEFAULT 0               COMMENT '是否加密:0否 1是',
  `created_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`       VARCHAR(64)                      COMMENT '创建人',
  `updated_by`       VARCHAR(64)                      COMMENT '更新人',
  `is_deleted`       TINYINT  NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康档案表';

-- ============================================================
-- 29. billing_tenant - 租户表
-- ============================================================
DROP TABLE IF EXISTS `billing_tenant`;
CREATE TABLE `billing_tenant` (
  `id`                   BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`            BIGINT      NOT NULL                COMMENT '租户ID(自关联)',
  `store_id`             BIGINT                              COMMENT '门店ID',
  `tenant_no`            VARCHAR(32) NOT NULL                COMMENT '租户编号',
  `tenant_name`          VARCHAR(128) NOT NULL               COMMENT '租户名称',
  `plan_code`            VARCHAR(32) NOT NULL                COMMENT '套餐编码',
  `plan_expire_date`     DATE                                COMMENT '套餐过期日期',
  `white_label_enabled`  TINYINT     DEFAULT 0               COMMENT '是否启用白标:0否 1是',
  `white_label_config`   JSON                                COMMENT '白标配置(JSON)',
  `brand_name`           VARCHAR(128)                        COMMENT '品牌名称',
  `brand_logo`           VARCHAR(512)                        COMMENT '品牌Logo',
  `brand_theme`          VARCHAR(32)                         COMMENT '品牌主题色',
  `admin_user_id`        BIGINT                              COMMENT '管理员用户ID',
  `contact_name`         VARCHAR(64)                         COMMENT '联系人姓名',
  `contact_phone`        VARCHAR(20)                         COMMENT '联系电话',
  `contact_email`        VARCHAR(128)                        COMMENT '联系邮箱',
  `status`               TINYINT     DEFAULT 1               COMMENT '状态:1试用 2正式 3欠费 4停用',
  `trial_expire_date`    DATE                                COMMENT '试用过期日期',
  `created_at`           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`           VARCHAR(64)                         COMMENT '创建人',
  `updated_by`           VARCHAR(64)                         COMMENT '更新人',
  `is_deleted`           TINYINT     NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_no` (`tenant_no`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户表';

-- ============================================================
-- 30. billing_subscription - 订阅表
-- ============================================================
DROP TABLE IF EXISTS `billing_subscription`;
CREATE TABLE `billing_subscription` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`      BIGINT        NOT NULL                COMMENT '租户ID',
  `store_id`       BIGINT                                COMMENT '门店ID',
  `plan_code`      VARCHAR(32)   NOT NULL                COMMENT '套餐编码',
  `plan_name`      VARCHAR(64)   NOT NULL                COMMENT '套餐名称',
  `start_date`     DATE          NOT NULL                COMMENT '开始日期',
  `end_date`       DATE          NOT NULL                COMMENT '结束日期',
  `billing_cycle`  TINYINT       DEFAULT 1               COMMENT '计费周期:1月 2季 3年',
  `auto_renew`     TINYINT       DEFAULT 1               COMMENT '是否自动续费:0否 1是',
  `payment_method` VARCHAR(32)                           COMMENT '支付方式',
  `payment_status` TINYINT       DEFAULT 0               COMMENT '支付状态:0待支付 1已支付 2逾期',
  `paid_amount`    DECIMAL(10,2)                         COMMENT '已付金额',
  `status`         VARCHAR(16)   DEFAULT 'active'        COMMENT '状态:active/trial/expired/cancelled',
  `created_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`     VARCHAR(64)                           COMMENT '创建人',
  `updated_by`     VARCHAR(64)                           COMMENT '更新人',
  `is_deleted`     TINYINT       NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订阅表';

-- ============================================================
-- 31. billing_plan - 套餐表
-- ============================================================
DROP TABLE IF EXISTS `billing_plan`;
CREATE TABLE `billing_plan` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`     BIGINT        NOT NULL                COMMENT '租户ID',
  `store_id`      BIGINT                                COMMENT '门店ID',
  `plan_code`     VARCHAR(32)   NOT NULL                COMMENT '套餐编码',
  `plan_name`     VARCHAR(64)   NOT NULL                COMMENT '套餐名称',
  `monthly_price` DECIMAL(10,2) NOT NULL                COMMENT '月度价格',
  `yearly_price`  DECIMAL(10,2)                         COMMENT '年度价格',
  `max_stores`    INT                                   COMMENT '最大门店数',
  `features_json` JSON                                  COMMENT '功能特性(JSON)',
  `description`   TEXT                                  COMMENT '描述',
  `sort_order`    INT           DEFAULT 0               COMMENT '排序',
  `status`        TINYINT       DEFAULT 1               COMMENT '状态:1启用 2停用',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`    VARCHAR(64)                           COMMENT '创建人',
  `updated_by`    VARCHAR(64)                           COMMENT '更新人',
  `is_deleted`    TINYINT       NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plan_code` (`plan_code`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='套餐表';

-- ============================================================
-- 32. billing_usage - 用量计量表
-- ============================================================
DROP TABLE IF EXISTS `billing_usage`;
CREATE TABLE `billing_usage` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`    BIGINT        NOT NULL                COMMENT '租户ID',
  `store_id`     BIGINT                                COMMENT '门店ID',
  `usage_type`   VARCHAR(32)   NOT NULL                COMMENT '用量类型',
  `usage_date`   DATE          NOT NULL                COMMENT '用量日期',
  `usage_count`  INT           DEFAULT 0               COMMENT '用量计数',
  `unit_price`   DECIMAL(10,4)                         COMMENT '单价',
  `cost_cents`   INT           DEFAULT 0               COMMENT '费用(分)',
  `detail_json`  JSON                                  COMMENT '明细(JSON)',
  `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`   VARCHAR(64)                           COMMENT '创建人',
  `updated_by`   VARCHAR(64)                           COMMENT '更新人',
  `is_deleted`   TINYINT       NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_type_date` (`tenant_id`, `usage_type`, `usage_date`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用量计量表';

-- ============================================================
-- 33. ai_llm_call_log - LLM调用日志表
-- ============================================================
DROP TABLE IF EXISTS `ai_llm_call_log`;
CREATE TABLE `ai_llm_call_log` (
  `id`                BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`         BIGINT      NOT NULL                COMMENT '租户ID',
  `store_id`          BIGINT                              COMMENT '门店ID',
  `request_id`        VARCHAR(64) NOT NULL                COMMENT '请求ID',
  `task_type`         VARCHAR(32) NOT NULL                COMMENT '任务类型',
  `model_provider`    VARCHAR(32) NOT NULL                COMMENT '模型提供商',
  `model_name`        VARCHAR(64) NOT NULL                COMMENT '模型名称',
  `prompt_tokens`     INT         DEFAULT 0               COMMENT '提示Token数',
  `completion_tokens` INT         DEFAULT 0               COMMENT '完成Token数',
  `total_tokens`      INT         DEFAULT 0               COMMENT '总Token数',
  `latency_ms`        INT         DEFAULT 0               COMMENT '延迟(毫秒)',
  `first_token_ms`    INT                                 COMMENT '首Token延迟(毫秒)',
  `cost_cents`        INT         DEFAULT 0               COMMENT '费用(分)',
  `is_success`        TINYINT     DEFAULT 1               COMMENT '是否成功:0否 1是',
  `error_code`        VARCHAR(32)                         COMMENT '错误码',
  `error_message`     VARCHAR(512)                        COMMENT '错误信息',
  `fallback_triggered`TINYINT     DEFAULT 0               COMMENT '是否触发降级:0否 1是',
  `created_at`        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`        VARCHAR(64)                         COMMENT '创建人',
  `updated_by`        VARCHAR(64)                         COMMENT '更新人',
  `is_deleted`        TINYINT     NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  INDEX `idx_request_id` (`request_id`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM调用日志表';

-- ============================================================
-- 34. ai_daily_report - AI日报表
-- ============================================================
DROP TABLE IF EXISTS `ai_daily_report`;
CREATE TABLE `ai_daily_report` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`     BIGINT      NOT NULL                COMMENT '租户ID',
  `store_id`      BIGINT                              COMMENT '门店ID',
  `report_date`   DATE        NOT NULL                COMMENT '报告日期',
  `report_no`     VARCHAR(32) NOT NULL                COMMENT '报告编号',
  `summary`       TEXT                                COMMENT '摘要',
  `highlights`    JSON                                COMMENT '亮点(JSON)',
  `concerns`      JSON                                COMMENT '关注点(JSON)',
  `suggestions`   JSON                                COMMENT '建议(JSON)',
  `metrics_json`  JSON                                COMMENT '指标数据(JSON)',
  `ai_insight`    TEXT                                COMMENT 'AI洞察',
  `push_status`   TINYINT     DEFAULT 0               COMMENT '推送状态:0未推送 1已推送',
  `push_time`     DATETIME                            COMMENT '推送时间',
  `push_channels` JSON                                COMMENT '推送渠道(JSON)',
  `created_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`    VARCHAR(64)                         COMMENT '创建人',
  `updated_by`    VARCHAR(64)                         COMMENT '更新人',
  `is_deleted`    TINYINT     NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_report_no` (`report_no`),
  UNIQUE KEY `uk_store_date` (`store_id`, `report_date`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI日报表';

-- ============================================================
-- 35. integration_config - 平台对接配置表
-- ============================================================
DROP TABLE IF EXISTS `integration_config`;
CREATE TABLE `integration_config` (
  `id`                BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`         BIGINT      NOT NULL                COMMENT '租户ID',
  `store_id`          BIGINT                              COMMENT '门店ID',
  `platform`          VARCHAR(32) NOT NULL                COMMENT '平台标识',
  `platform_name`     VARCHAR(64)                         COMMENT '平台名称',
  `app_id`            VARCHAR(128)                        COMMENT '应用ID',
  `app_secret`        VARCHAR(512)                        COMMENT '应用密钥',
  `auth_access_token` VARCHAR(512)                        COMMENT '授权AccessToken',
  `auth_expired_at`   DATETIME                            COMMENT '授权过期时间',
  `refresh_token`     VARCHAR(512)                        COMMENT '刷新Token',
  `callback_url`      VARCHAR(512)                        COMMENT '回调地址',
  `callback_token`    VARCHAR(128)                        COMMENT '回调Token',
  `callback_aes_key`  VARCHAR(128)                        COMMENT '回调AES密钥',
  `extra_config`      JSON                                COMMENT '额外配置(JSON)',
  `enabled_modules`   JSON                                COMMENT '启用模块(JSON)',
  `status`            TINYINT     DEFAULT 0               COMMENT '状态:0未配置 1配置中 2正常 3异常',
  `last_sync_time`    DATETIME                            COMMENT '最近同步时间',
  `created_at`        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`        VARCHAR(64)                         COMMENT '创建人',
  `updated_by`        VARCHAR(64)                         COMMENT '更新人',
  `is_deleted`        TINYINT     NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_platform` (`tenant_id`, `platform`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台对接配置表';

-- ============================================================
-- 36. integration_lead - 线索表
-- ============================================================
DROP TABLE IF EXISTS `integration_lead`;
CREATE TABLE `integration_lead` (
  `id`                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`            BIGINT       NOT NULL                COMMENT '租户ID',
  `store_id`             BIGINT                               COMMENT '门店ID',
  `lead_no`              VARCHAR(64)  NOT NULL                COMMENT '线索编号',
  `platform`             VARCHAR(32)  NOT NULL                COMMENT '来源平台',
  `platform_lead_id`     VARCHAR(128)                         COMMENT '平台线索ID',
  `source_channel`       VARCHAR(32)                          COMMENT '来源渠道',
  `campaign_id`          VARCHAR(64)                          COMMENT '活动ID',
  `referrer_name`        VARCHAR(128)                         COMMENT '推荐人姓名',
  `customer_name`        VARCHAR(64)                          COMMENT '客户姓名',
  `customer_phone`       VARCHAR(20)                          COMMENT '客户电话',
  `customer_gender`      TINYINT                              COMMENT '客户性别:0女 1男 2未知',
  `customer_age`         INT                                  COMMENT '客户年龄',
  `customer_intent`      VARCHAR(32)                          COMMENT '客户意向',
  `consultation_content` TEXT                                 COMMENT '咨询内容',
  `ai_summary`           TEXT                                 COMMENT 'AI摘要',
  `lead_class`           TINYINT                              COMMENT '线索分类:1A类 2B类 3C类',
  `follow_status`        TINYINT                              COMMENT '跟进状态:1待跟进 2跟进中 3已转化 4无效',
  `assigned_to`          BIGINT                               COMMENT '分配给(员工ID)',
  `assigned_at`          DATETIME                             COMMENT '分配时间',
  `converted_order_id`   BIGINT                               COMMENT '转化订单ID',
  `converted_at`         DATETIME                             COMMENT '转化时间',
  `created_at`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`           VARCHAR(64)                          COMMENT '创建人',
  `updated_by`           VARCHAR(64)                          COMMENT '更新人',
  `is_deleted`           TINYINT       NOT NULL DEFAULT 0     COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_platform_lead` (`platform`, `platform_lead_id`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='线索表';

-- ============================================================
-- 37. integration_promotion_track - 推广追踪表
-- ============================================================
DROP TABLE IF EXISTS `integration_promotion_track`;
CREATE TABLE `integration_promotion_track` (
  `id`                  BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`           BIGINT      NOT NULL                COMMENT '租户ID',
  `store_id`            BIGINT                              COMMENT '门店ID',
  `promo_code`          VARCHAR(16) NOT NULL                COMMENT '推广码',
  `source_technician_id`BIGINT      NOT NULL                COMMENT '来源技师ID',
  `visitor_id`          VARCHAR(128)                        COMMENT '访客ID',
  `event_type`          VARCHAR(32) NOT NULL                COMMENT '事件类型:click/register/first_visit/first_order/repurchase',
  `event_time`          DATETIME    NOT NULL                COMMENT '事件时间',
  `device_info`         JSON                                COMMENT '设备信息(JSON)',
  `created_at`          DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`          DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`          VARCHAR(64)                         COMMENT '创建人',
  `updated_by`          VARCHAR(64)                         COMMENT '更新人',
  `is_deleted`          TINYINT     NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推广追踪表';

-- ============================================================
-- 38. data_appointment_funnel - 预约埋点表
-- ============================================================
DROP TABLE IF EXISTS `data_appointment_funnel`;
CREATE TABLE `data_appointment_funnel` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`   BIGINT      NOT NULL                COMMENT '租户ID',
  `store_id`    BIGINT                              COMMENT '门店ID',
  `member_id`   BIGINT                              COMMENT '会员ID',
  `session_id`  VARCHAR(64)                         COMMENT '会话ID',
  `step`        VARCHAR(32) NOT NULL                COMMENT '步骤:browse_store/select_tech/select_time/confirm/arrive/complete',
  `step_time`   DATETIME    NOT NULL                COMMENT '步骤时间',
  `duration_ms` INT                                 COMMENT '停留时长(毫秒)',
  `extra_json`  JSON                                COMMENT '扩展数据(JSON)',
  `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`  VARCHAR(64)                         COMMENT '创建人',
  `updated_by`  VARCHAR(64)                         COMMENT '更新人',
  `is_deleted`  TINYINT     NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约埋点表';

-- ============================================================
-- 39. sys_dict - 字典表
-- ============================================================
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`   BIGINT       NOT NULL                COMMENT '租户ID',
  `store_id`    BIGINT                               COMMENT '门店ID',
  `dict_code`   VARCHAR(64)  NOT NULL                COMMENT '字典编码',
  `dict_name`   VARCHAR(128) NOT NULL                COMMENT '字典名称',
  `description` VARCHAR(256)                         COMMENT '描述',
  `status`      TINYINT      DEFAULT 1               COMMENT '状态:1启用 2停用',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`  VARCHAR(64)                          COMMENT '创建人',
  `updated_by`  VARCHAR(64)                          COMMENT '更新人',
  `is_deleted`  TINYINT      NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_code` (`dict_code`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典表';

-- ============================================================
-- 40. sys_dict_item - 字典项表
-- ============================================================
DROP TABLE IF EXISTS `sys_dict_item`;
CREATE TABLE `sys_dict_item` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`  BIGINT       NOT NULL                COMMENT '租户ID',
  `store_id`   BIGINT                               COMMENT '门店ID',
  `dict_id`    BIGINT       NOT NULL                COMMENT '字典ID',
  `item_code`  VARCHAR(64)  NOT NULL                COMMENT '字典项编码',
  `item_name`  VARCHAR(128) NOT NULL                COMMENT '字典项名称',
  `item_value` VARCHAR(256)                         COMMENT '字典项值',
  `sort_order` INT          DEFAULT 0               COMMENT '排序',
  `status`     TINYINT      DEFAULT 1               COMMENT '状态:1启用 2停用',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64)                          COMMENT '创建人',
  `updated_by` VARCHAR(64)                          COMMENT '更新人',
  `is_deleted` TINYINT      NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典项表';

-- ============================================================
-- 41. sys_config - 系统配置表
-- ============================================================
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`    BIGINT       NOT NULL                COMMENT '租户ID',
  `store_id`     BIGINT                               COMMENT '门店ID',
  `config_key`   VARCHAR(128) NOT NULL                COMMENT '配置键',
  `config_value` TEXT                                 COMMENT '配置值',
  `config_name`  VARCHAR(128)                         COMMENT '配置名称',
  `description`  VARCHAR(256)                         COMMENT '描述',
  `config_type`  TINYINT      DEFAULT 1               COMMENT '配置类型:1字符串 2数字 3布尔 4JSON',
  `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`   VARCHAR(64)                          COMMENT '创建人',
  `updated_by`   VARCHAR(64)                          COMMENT '更新人',
  `is_deleted`   TINYINT      NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ============================================================
-- 42. sys_audit_log - 审计日志表
-- ============================================================
DROP TABLE IF EXISTS `sys_audit_log`;
CREATE TABLE `sys_audit_log` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`     BIGINT       NOT NULL                COMMENT '租户ID',
  `store_id`      BIGINT                               COMMENT '门店ID',
  `module`        VARCHAR(64)                          COMMENT '模块',
  `action`        VARCHAR(64)                          COMMENT '操作',
  `operator_id`   BIGINT                               COMMENT '操作人ID',
  `operator_name` VARCHAR(64)                          COMMENT '操作人姓名',
  `target_type`   VARCHAR(64)                          COMMENT '目标类型',
  `target_id`     BIGINT                               COMMENT '目标ID',
  `old_value`     TEXT                                 COMMENT '旧值',
  `new_value`     TEXT                                 COMMENT '新值',
  `ip`            VARCHAR(64)                          COMMENT 'IP地址',
  `user_agent`    VARCHAR(512)                         COMMENT '用户代理',
  `result`        TINYINT                              COMMENT '结果:1成功 2失败',
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`    VARCHAR(64)                          COMMENT '创建人',
  `updated_by`    VARCHAR(64)                          COMMENT '更新人',
  `is_deleted`    TINYINT       NOT NULL DEFAULT 0     COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

-- ============================================================
-- 43. sys_feature_flag - 功能开关表
-- ============================================================
DROP TABLE IF EXISTS `sys_feature_flag`;
CREATE TABLE `sys_feature_flag` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`    BIGINT       NOT NULL                COMMENT '租户ID',
  `store_id`     BIGINT                               COMMENT '门店ID',
  `flag_key`     VARCHAR(64)  NOT NULL                COMMENT '开关键',
  `flag_name`    VARCHAR(128) NOT NULL                COMMENT '开关名称',
  `description`  VARCHAR(512)                         COMMENT '描述',
  `default_value`TINYINT      DEFAULT 0               COMMENT '默认值:0关 1开',
  `type`         TINYINT      DEFAULT 1               COMMENT '类型:1全局 2租户 3门店 4用户 5百分比',
  `percentage`   INT                                  COMMENT '百分比(0-100)',
  `rules_json`   JSON                                 COMMENT '规则(JSON)',
  `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`   VARCHAR(64)                          COMMENT '创建人',
  `updated_by`   VARCHAR(64)                          COMMENT '更新人',
  `is_deleted`   TINYINT       NOT NULL DEFAULT 0     COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_flag_key` (`flag_key`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='功能开关表';

-- ============================================================
-- 44. sys_file - 文件表
-- ============================================================
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`  BIGINT       NOT NULL                COMMENT '租户ID',
  `store_id`   BIGINT                               COMMENT '门店ID',
  `file_name`  VARCHAR(256) NOT NULL                COMMENT '文件名',
  `file_path`  VARCHAR(512) NOT NULL                COMMENT '文件路径',
  `file_type`  VARCHAR(32)                          COMMENT '文件类型',
  `file_size`  BIGINT                               COMMENT '文件大小(字节)',
  `mime_type`  VARCHAR(128)                         COMMENT 'MIME类型',
  `biz_type`   VARCHAR(32)                          COMMENT '业务类型',
  `biz_id`     BIGINT                               COMMENT '业务ID',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64)                          COMMENT '创建人',
  `updated_by` VARCHAR(64)                          COMMENT '更新人',
  `is_deleted` TINYINT       NOT NULL DEFAULT 0     COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件表';

-- ============================================================
-- 45. sys_sms_log - 短信日志表
-- ============================================================
DROP TABLE IF EXISTS `sys_sms_log`;
CREATE TABLE `sys_sms_log` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id`     BIGINT      NOT NULL                COMMENT '租户ID',
  `store_id`      BIGINT                              COMMENT '门店ID',
  `phone`         VARCHAR(20) NOT NULL                COMMENT '手机号',
  `template_code` VARCHAR(64)                         COMMENT '模板编码',
  `content`       TEXT                                COMMENT '短信内容',
  `biz_id`        VARCHAR(64)                         COMMENT '业务ID',
  `send_status`   TINYINT     DEFAULT 0               COMMENT '发送状态:0待发送 1已发送 2发送失败',
  `send_time`     DATETIME                            COMMENT '发送时间',
  `callback_code` VARCHAR(32)                         COMMENT '回调状态码',
  `created_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`    VARCHAR(64)                         COMMENT '创建人',
  `updated_by`    VARCHAR(64)                         COMMENT '更新人',
  `is_deleted`    TINYINT     NOT NULL DEFAULT 0      COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='短信日志表';

SET FOREIGN_KEY_CHECKS=1;
