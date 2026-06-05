-- ============================================================
-- 忠济堂中医养生连锁管理系统 - 种子数据+字典初始化
-- 版本: V1.0.1
-- 数据库: zjt_dev
-- 说明: 包含默认租户、管理员、字典、套餐、测试门店/技师/服务项目等种子数据
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;

USE `zjt_dev`;

-- ============================================================
-- 1. 默认租户 (billing_tenant)
-- ============================================================
INSERT INTO `billing_tenant` (
  `id`, `tenant_id`, `store_id`, `tenant_no`, `tenant_name`, `plan_code`,
  `plan_expire_date`, `white_label_enabled`, `white_label_config`,
  `brand_name`, `brand_logo`, `brand_theme`,
  `admin_user_id`, `contact_name`, `contact_phone`, `contact_email`,
  `status`, `trial_expire_date`,
  `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`
) VALUES (
  1, 1, NULL, 'ZJT001', '忠济堂(测试)', 'flagship',
  '2027-12-31', 0, NULL,
  '忠济堂', NULL, '#8B4513',
  1, '张管理', '13800000001', 'admin@zjt.com',
  2, '2025-12-31',
  NOW(), NOW(), 'system', 'system', 0
);

-- ============================================================
-- 2. 默认管理员 (user_employee)
-- ============================================================
INSERT INTO `user_employee` (
  `id`, `tenant_id`, `store_id`, `employee_no`, `name`, `phone`,
  `gender`, `department_id`, `position`, `role_id`,
  `status`, `hire_date`,
  `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`
) VALUES (
  1, 1, NULL, 'ADMIN001', '管理员', '13800000001',
  1, NULL, '系统管理员', NULL,
  1, '2025-01-01',
  NOW(), NOW(), 'system', 'system', 0
);

-- ============================================================
-- 3. 字典数据 (sys_dict + sys_dict_item)
-- ============================================================

-- 3.1 预约状态
INSERT INTO `sys_dict` (`id`, `tenant_id`, `store_id`, `dict_code`, `dict_name`, `description`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES (1, 1, NULL, 'appointment_status', '预约状态', '预约状态字典', 1, NOW(), NOW(), 'system', 'system', 0);

INSERT INTO `sys_dict_item` (`tenant_id`, `store_id`, `dict_id`, `item_code`, `item_name`, `item_value`, `sort_order`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
  (1, NULL, 1, '1', '待确认', '1', 1, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 1, '2', '已确认', '2', 2, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 1, '3', '服务中', '3', 3, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 1, '4', '已完成', '4', 4, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 1, '5', '已取消', '5', 5, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 1, '6', '超时未到', '6', 6, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 1, '7', '已修改', '7', 7, 1, NOW(), NOW(), 'system', 'system', 0);

-- 3.2 订单状态
INSERT INTO `sys_dict` (`id`, `tenant_id`, `store_id`, `dict_code`, `dict_name`, `description`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES (2, 1, NULL, 'order_status', '订单状态', '订单支付状态字典', 1, NOW(), NOW(), 'system', 'system', 0);

INSERT INTO `sys_dict_item` (`tenant_id`, `store_id`, `dict_id`, `item_code`, `item_name`, `item_value`, `sort_order`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
  (1, NULL, 2, '0', '待支付', '0', 1, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 2, '1', '已支付', '1', 2, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 2, '2', '部分退款', '2', 3, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 2, '3', '已退款', '3', 4, 1, NOW(), NOW(), 'system', 'system', 0);

-- 3.3 支付方式
INSERT INTO `sys_dict` (`id`, `tenant_id`, `store_id`, `dict_code`, `dict_name`, `description`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES (3, 1, NULL, 'payment_method', '支付方式', '支付方式字典', 1, NOW(), NOW(), 'system', 'system', 0);

INSERT INTO `sys_dict_item` (`tenant_id`, `store_id`, `dict_id`, `item_code`, `item_name`, `item_value`, `sort_order`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
  (1, NULL, 3, '1', '微信', '1', 1, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 3, '2', '支付宝', '2', 2, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 3, '3', '现金', '3', 3, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 3, '4', '储值', '4', 4, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 3, '5', '积分', '5', 5, 1, NOW(), NOW(), 'system', 'system', 0);

-- 3.4 体质类型
INSERT INTO `sys_dict` (`id`, `tenant_id`, `store_id`, `dict_code`, `dict_name`, `description`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES (4, 1, NULL, 'constitution_type', '体质类型', '中医九种体质分类', 1, NOW(), NOW(), 'system', 'system', 0);

INSERT INTO `sys_dict_item` (`tenant_id`, `store_id`, `dict_id`, `item_code`, `item_name`, `item_value`, `sort_order`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
  (1, NULL, 4, '1', '平和', '1', 1, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 4, '2', '气虚', '2', 2, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 4, '3', '阳虚', '3', 3, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 4, '4', '阴虚', '4', 4, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 4, '5', '痰湿', '5', 5, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 4, '6', '湿热', '6', 6, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 4, '7', '血瘀', '7', 7, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 4, '8', '气郁', '8', 8, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 4, '9', '特禀', '9', 9, 1, NOW(), NOW(), 'system', 'system', 0);

-- 3.5 会员类型
INSERT INTO `sys_dict` (`id`, `tenant_id`, `store_id`, `dict_code`, `dict_name`, `description`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES (5, 1, NULL, 'member_type', '会员类型', '会员类型字典', 1, NOW(), NOW(), 'system', 'system', 0);

INSERT INTO `sys_dict_item` (`tenant_id`, `store_id`, `dict_id`, `item_code`, `item_name`, `item_value`, `sort_order`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
  (1, NULL, 5, '1', '散客', '1', 1, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 5, '2', '会员', '2', 2, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 5, '3', 'VIP', '3', 3, 1, NOW(), NOW(), 'system', 'system', 0);

-- 3.6 门店状态
INSERT INTO `sys_dict` (`id`, `tenant_id`, `store_id`, `dict_code`, `dict_name`, `description`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES (6, 1, NULL, 'store_status', '门店状态', '门店营业状态字典', 1, NOW(), NOW(), 'system', 'system', 0);

INSERT INTO `sys_dict_item` (`tenant_id`, `store_id`, `dict_id`, `item_code`, `item_name`, `item_value`, `sort_order`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
  (1, NULL, 6, '1', '营业中', '1', 1, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 6, '2', '休息中', '2', 2, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 6, '3', '装修中', '3', 3, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 6, '4', '已关闭', '4', 4, 1, NOW(), NOW(), 'system', 'system', 0);

-- 3.7 技师状态
INSERT INTO `sys_dict` (`id`, `tenant_id`, `store_id`, `dict_code`, `dict_name`, `description`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES (7, 1, NULL, 'technician_status', '技师状态', '技师在职状态字典', 1, NOW(), NOW(), 'system', 'system', 0);

INSERT INTO `sys_dict_item` (`tenant_id`, `store_id`, `dict_id`, `item_code`, `item_name`, `item_value`, `sort_order`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
  (1, NULL, 7, '1', '在职', '1', 1, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 7, '2', '休息', '2', 2, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 7, '3', '离职', '3', 3, 1, NOW(), NOW(), 'system', 'system', 0);

-- 3.8 技能等级
INSERT INTO `sys_dict` (`id`, `tenant_id`, `store_id`, `dict_code`, `dict_name`, `description`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES (8, 1, NULL, 'skill_level', '技能等级', '技师技能等级字典', 1, NOW(), NOW(), 'system', 'system', 0);

INSERT INTO `sys_dict_item` (`tenant_id`, `store_id`, `dict_id`, `item_code`, `item_name`, `item_value`, `sort_order`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
  (1, NULL, 8, '1', '初级', '1', 1, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 8, '2', '中级', '2', 2, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 8, '3', '高级', '3', 3, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 8, '4', '专家', '4', 4, 1, NOW(), NOW(), 'system', 'system', 0);

-- 3.9 线索分类
INSERT INTO `sys_dict` (`id`, `tenant_id`, `store_id`, `dict_code`, `dict_name`, `description`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES (9, 1, NULL, 'lead_class', '线索分类', '线索质量分类字典', 1, NOW(), NOW(), 'system', 'system', 0);

INSERT INTO `sys_dict_item` (`tenant_id`, `store_id`, `dict_id`, `item_code`, `item_name`, `item_value`, `sort_order`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
  (1, NULL, 9, '1', 'A类', '1', 1, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 9, '2', 'B类', '2', 2, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 9, '3', 'C类', '3', 3, 1, NOW(), NOW(), 'system', 'system', 0);

-- 3.10 跟进状态
INSERT INTO `sys_dict` (`id`, `tenant_id`, `store_id`, `dict_code`, `dict_name`, `description`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES (10, 1, NULL, 'follow_status', '跟进状态', '线索跟进状态字典', 1, NOW(), NOW(), 'system', 'system', 0);

INSERT INTO `sys_dict_item` (`tenant_id`, `store_id`, `dict_id`, `item_code`, `item_name`, `item_value`, `sort_order`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
  (1, NULL, 10, '1', '待跟进', '1', 1, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 10, '2', '跟进中', '2', 2, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 10, '3', '已转化', '3', 3, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 10, '4', '无效', '4', 4, 1, NOW(), NOW(), 'system', 'system', 0);

-- 3.11 来源渠道
INSERT INTO `sys_dict` (`id`, `tenant_id`, `store_id`, `dict_code`, `dict_name`, `description`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES (11, 1, NULL, 'source_channel', '来源渠道', '客户来源渠道字典', 1, NOW(), NOW(), 'system', 'system', 0);

INSERT INTO `sys_dict_item` (`tenant_id`, `store_id`, `dict_id`, `item_code`, `item_name`, `item_value`, `sort_order`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
  (1, NULL, 11, 'miniprogram', '小程序', 'miniprogram', 1, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 11, 'wechat_work', '企业微信', 'wechat_work', 2, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 11, 'douyin', '抖音', 'douyin', 3, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 11, 'meituan', '美团', 'meituan', 4, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 11, 'xiaohongshu', '小红书', 'xiaohongshu', 5, 1, NOW(), NOW(), 'system', 'system', 0),
  (1, NULL, 11, 'phone', '电话', 'phone', 6, 1, NOW(), NOW(), 'system', 'system', 0);

-- ============================================================
-- 4. 套餐数据 (billing_plan)
-- ============================================================
INSERT INTO `billing_plan` (
  `id`, `tenant_id`, `store_id`, `plan_code`, `plan_name`, `monthly_price`,
  `yearly_price`, `max_stores`, `features_json`, `description`,
  `sort_order`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`
) VALUES
(1, 1, NULL, 'basic', '基础版', 298.00,
  2980.00, 1,
  '{"max_technicians":5,"max_rooms":5,"ai_consultation":false,"daily_report":false,"multi_platform":false}',
  '适合单店初创，包含基础预约、会员管理功能',
  1, 1, NOW(), NOW(), 'system', 'system', 0),
(2, 1, NULL, 'standard', '标准版', 698.00,
  6980.00, 3,
  '{"max_technicians":20,"max_rooms":15,"ai_consultation":true,"daily_report":true,"multi_platform":false}',
  '适合小型连锁，增加AI体质测评与日报功能',
  2, 1, NOW(), NOW(), 'system', 'system', 0),
(3, 1, NULL, 'professional', '专业版', 1298.00,
  12980.00, 10,
  '{"max_technicians":50,"max_rooms":50,"ai_consultation":true,"daily_report":true,"multi_platform":true,"crm_advanced":true}',
  '适合中型连锁，支持多平台对接与高级CRM',
  3, 1, NOW(), NOW(), 'system', 'system', 0),
(4, 1, NULL, 'flagship', '旗舰版', 2998.00,
  29980.00, 999,
  '{"max_technicians":999,"max_rooms":999,"ai_consultation":true,"daily_report":true,"multi_platform":true,"crm_advanced":true,"white_label":true,"api_access":true}',
  '适合大型连锁，不限门店数，支持白标与API开放',
  4, 1, NOW(), NOW(), 'system', 'system', 0);

-- ============================================================
-- 5. 测试门店 (store_info) - 2个门店
-- ============================================================
INSERT INTO `store_info` (
  `id`, `tenant_id`, `store_id`, `store_no`, `store_name`, `store_type`,
  `brand_id`, `province_code`, `city_code`, `district_code`,
  `address`, `latitude`, `longitude`,
  `contact_name`, `contact_phone`,
  `business_start_time`, `business_end_time`,
  `room_count`, `technician_count`, `region_id`, `status`,
  `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`
) VALUES
(1, 1, 1, 'STORE001', '忠济堂·朝阳旗舰店', 1,
 1, '110000', '110105', '110105001',
 '北京市朝阳区建国路88号', 39.90882000, 116.46032000,
 '李店长', '010-65001234',
 '09:00:00', '21:00:00',
 8, 5, 1, 1,
 NOW(), NOW(), 'system', 'system', 0),
(2, 1, 2, 'STORE002', '忠济堂·海淀学院路店', 1,
 1, '110000', '110108', '110108001',
 '北京市海淀区学院路15号', 39.98382000, 116.34846000,
 '王店长', '010-82001234',
 '09:30:00', '21:30:00',
 6, 3, 1, 1,
 NOW(), NOW(), 'system', 'system', 0);

-- ============================================================
-- 6. 测试技师 (user_employee + store_technician) - 5个技师
-- ============================================================

-- 6.1 技师对应的员工记录
INSERT INTO `user_employee` (
  `id`, `tenant_id`, `store_id`, `employee_no`, `name`, `phone`,
  `gender`, `department_id`, `position`, `role_id`,
  `status`, `hire_date`,
  `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`
) VALUES
(2, 1, 1, 'TECH001', '张明华', '13800000011', 1, NULL, '技师', NULL, 1, '2024-03-15', NOW(), NOW(), 'system', 'system', 0),
(3, 1, 1, 'TECH002', '李婉清', '13800000012', 0, NULL, '技师', NULL, 1, '2024-05-20', NOW(), NOW(), 'system', 'system', 0),
(4, 1, 1, 'TECH003', '王德厚', '13800000013', 1, NULL, '技师', NULL, 1, '2024-06-10', NOW(), NOW(), 'system', 'system', 0),
(5, 1, 2, 'TECH004', '赵雅芝', '13800000014', 0, NULL, '技师', NULL, 1, '2024-08-01', NOW(), NOW(), 'system', 'system', 0),
(6, 1, 2, 'TECH005', '陈志远', '13800000015', 1, NULL, '技师', NULL, 1, '2024-09-12', NOW(), NOW(), 'system', 'system', 0);

-- 6.2 技师记录
INSERT INTO `store_technician` (
  `id`, `tenant_id`, `store_id`, `employee_id`, `technician_no`,
  `skill_level`, `skilled_items`, `default_schedule`,
  `month_service_count`, `month_revenue`, `month_rating`, `total_service_count`,
  `status`, `is_online`,
  `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`
) VALUES
(1, 1, 1, 2, 'T20240001',
 4, '["中医推拿","艾灸","拔罐"]', '早班',
 45, 13500.00, 4.90, 520,
 1, 1,
 NOW(), NOW(), 'system', 'system', 0),
(2, 1, 1, 3, 'T20240002',
 3, '["足疗","精油SPA","刮痧"]', '中班',
 38, 10200.00, 4.85, 380,
 1, 1,
 NOW(), NOW(), 'system', 'system', 0),
(3, 1, 1, 4, 'T20240003',
 2, '["中医推拿","拔罐"]', '晚班',
 30, 7500.00, 4.70, 210,
 1, 0,
 NOW(), NOW(), 'system', 'system', 0),
(4, 1, 2, 5, 'T20240004',
 3, '["艾灸","精油SPA","足疗"]', '早班',
 35, 9800.00, 4.80, 320,
 1, 1,
 NOW(), NOW(), 'system', 'system', 0),
(5, 1, 2, 6, 'T20240005',
 2, '["中医推拿","刮痧"]', '中班',
 28, 6800.00, 4.65, 180,
 1, 1,
 NOW(), NOW(), 'system', 'system', 0);

-- ============================================================
-- 7. 测试服务项目 (content_service_item) - 6个项目
-- ============================================================
INSERT INTO `content_service_item` (
  `id`, `tenant_id`, `store_id`, `item_no`, `item_name`,
  `category_id`, `category_name`, `price`, `cost_price`,
  `duration_minutes`, `commission_type`, `commission_value`,
  `is_package`, `is_ai_recommended`, `tags`, `description`,
  `status`, `sort_order`,
  `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`
) VALUES
(1, 1, NULL, 'SVC001', '中医推拿',
 1, '推拿类', 298.00, 80.00,
 60, 1, 50.00,
 0, 1, '经典,养生', '传统中医推拿手法，疏通经络，调理气血',
 1, 1,
 NOW(), NOW(), 'system', 'system', 0),
(2, 1, NULL, 'SVC002', '艾灸理疗',
 2, '艾灸类', 268.00, 60.00,
 60, 1, 40.00,
 0, 1, '温阳,祛湿', '采用优质艾条，温经散寒，扶阳固本',
 1, 2,
 NOW(), NOW(), 'system', 'system', 0),
(3, 1, NULL, 'SVC003', '足部反射疗法',
 3, '足疗类', 198.00, 45.00,
 60, 2, 0.15,
 0, 0, '足疗,放松', '基于足部反射区理论，调理脏腑功能',
 1, 3,
 NOW(), NOW(), 'system', 'system', 0),
(4, 1, NULL, 'SVC004', '精油SPA',
 4, 'SPA类', 398.00, 120.00,
 90, 1, 60.00,
 0, 1, '精油,舒缓', '精选植物精油，深层放松身心',
 1, 4,
 NOW(), NOW(), 'system', 'system', 0),
(5, 1, NULL, 'SVC005', '拔罐祛湿',
 2, '艾灸类', 168.00, 35.00,
 30, 1, 25.00,
 0, 0, '祛湿,排毒', '传统拔罐疗法，祛风除湿，活血化瘀',
 1, 5,
 NOW(), NOW(), 'system', 'system', 0),
(6, 1, NULL, 'SVC006', '体质调理套餐',
 5, '套餐类', 888.00, 200.00,
 120, 1, 100.00,
 1, 1, '套餐,体质', '含推拿+艾灸+拔罐，根据体质定制调理方案',
 1, 6,
 NOW(), NOW(), 'system', 'system', 0);

-- ============================================================
-- 8. 测试时段配置 (store_time_slot_config) - 2个门店
-- ============================================================
INSERT INTO `store_time_slot_config` (
  `id`, `tenant_id`, `store_id`, `business_start_time`, `business_end_time`,
  `slot_duration_minutes`, `rest_start_time`, `rest_end_time`, `status`,
  `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`
) VALUES
(1, 1, 1, '09:00:00', '21:00:00',
 60, '12:00:00', '13:00:00', 1,
 NOW(), NOW(), 'system', 'system', 0),
(2, 1, 2, '09:30:00', '21:30:00',
 60, '12:00:00', '13:30:00', 1,
 NOW(), NOW(), 'system', 'system', 0);

SET FOREIGN_KEY_CHECKS=1;
