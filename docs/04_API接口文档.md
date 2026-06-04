# 忠济堂中医养生连锁管理系统 · API接口文档（V3.3）

> **版本：V3.3 | 日期：2026年6月**
> **说明：RESTful API规范，版本前缀/api/v1，所有接口需JWT鉴权并自动注入tenant_id**

---

## 一、API规范

### 1.1 通用规范

| 项目 | 规范 |
|------|------|
| 版本前缀 | /api/v1 |
| 认证方式 | JWT Bearer Token |
| 请求格式 | Content-Type: application/json |
| 响应格式 | 统一JSON结构 |
| 字符编码 | UTF-8 |
| 时间格式 | ISO 8601 (yyyy-MM-ddTHH:mm:ss.SSSZ) |
| 多租户 | JWT中解析tenant_id，自动注入 |
| 限流 | 每租户每API独立限流（套餐决定配额） |

### 1.2 统一响应结构

```json
// 成功响应
{
  "code": 0,
  "message": "success",
  "data": { ... },
  "request_id": "uuid",
  "timestamp": "2026-06-04T10:00:00.000+08:00"
}

// 分页响应
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [ ... ],
    "pagination": {
      "page": 1,
      "page_size": 20,
      "total": 100,
      "total_pages": 5
    }
  },
  "request_id": "uuid",
  "timestamp": "2026-06-04T10:00:00.000+08:00"
}

// 错误响应
{
  "code": 40001,
  "message": "参数错误",
  "detail": "phone格式不正确",
  "request_id": "uuid",
  "timestamp": "2026-06-04T10:00:00.000+08:00"
}
```

### 1.3 错误码定义

| 错误码 | 含义 |
|--------|------|
| 0 | 成功 |
| 40001 | 参数错误 |
| 40101 | 未认证 |
| 40102 | Token过期 |
| 40301 | 无权限 |
| 40302 | 套餐功能未开通 |
| 40401 | 资源不存在 |
| 40901 | 资源冲突（如重复预约） |
| 42901 | 请求过于频繁 |
| 50001 | 系统内部错误 |

---

## 二、用户域API

### 2.1 会员管理 /user/member

#### 2.1.1 创建会员

```
POST /api/v1/user/members
```

**请求体：**
```json
{
  "name": "张三",
  "phone": "13800138000",
  "gender": 1,
  "birthday": "1990-06-15",
  "store_id": 101,
  "source_channel": "miniprogram",
  "constitution_type": 1,
  "referrer_id": 50001,
  "tags": ["新客", "高意向"]
}
```

**响应：**
```json
{
  "code": 0,
  "data": {
    "id": 50001,
    "member_no": "M202606040001",
    "name": "张三",
    "phone": "13800138000",
    "member_level": "普通会员",
    "points": 0,
    "balance": "0.00",
    "created_at": "2026-06-04T10:00:00+08:00"
  }
}
```

#### 2.1.2 查询会员列表

```
GET /api/v1/user/members
```

**查询参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| store_id | long | 否 | 门店ID |
| keyword | string | 否 | 搜索(姓名/手机/会员号) |
| member_level_id | long | 否 | 等级ID |
| constitution_type | int | 否 | 体质类型 |
| source_channel | string | 否 | 来源渠道 |
| status | int | 否 | 状态 |
| start_date | date | 否 | 创建起始日期 |
| end_date | date | 否 | 创建结束日期 |
| page | int | 否 | 页码，默认1 |
| page_size | int | 否 | 每页条数，默认20 |

#### 2.1.3 获取会员详情

```
GET /api/v1/user/members/{id}
```

**响应包含：**
- 基本信息（姓名/手机/体质/等级）
- 账户信息（积分/余额/疗程卡）
- 消费记录（最近10条订单）
- 预约记录（最近10条预约）
- AI健康画像（体质分析/推荐项目）

#### 2.1.4 更新会员体质

```
PUT /api/v1/user/members/{id}/constitution
```

**请求体：**
```json
{
  "constitution_type": 3,
  "constitution_score": 78.5,
  "ai_analysis": "舌象分析结果...",
  "recommendations": ["肩颈调理套餐", "艾灸理疗"]
}
```

---

### 2.2 员工管理 /user/employee

| 接口 | 方法 | 说明 |
|------|------|------|
| POST /api/v1/user/employees | POST | 创建员工 |
| GET /api/v1/user/employees | GET | 员工列表 |
| GET /api/v1/user/employees/{id} | GET | 员工详情 |
| PUT /api/v1/user/employees/{id} | PUT | 更新员工 |
| PUT /api/v1/user/employees/{id}/status | PUT | 员工状态变更 |
| POST /api/v1/user/employees/batch-import | POST | 批量导入员工 |

---

### 2.3 健康档案 /user/health-profile

| 接口 | 方法 | 说明 |
|------|------|------|
| GET /api/v1/user/health-profiles/{member_id} | GET | 获取健康档案 |
| PUT /api/v1/user/health-profiles/{member_id} | PUT | 更新健康档案 |
| POST /api/v1/user/health-profiles/{member_id}/constitution | POST | 提交体质辨识 |

---

## 三、门店域API

### 3.1 门店管理 /store/info

| 接口 | 方法 | 说明 |
|------|------|------|
| POST /api/v1/store/stores | POST | 创建门店 |
| GET /api/v1/store/stores | GET | 门店列表 |
| GET /api/v1/store/stores/{id} | GET | 门店详情 |
| PUT /api/v1/store/stores/{id} | PUT | 更新门店 |
| PUT /api/v1/store/stores/{id}/status | PUT | 门店状态变更 |

### 3.2 技师管理 /store/technician

#### 3.2.1 查询技师列表

```
GET /api/v1/store/technicians
```

**查询参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| store_id | long | 否 | 门店ID |
| status | int | 否 | 状态 |
| is_online | int | 否 | 是否在岗 |
| skill_level | int | 否 | 技能等级 |
| service_item_id | long | 否 | 擅长项目 |
| date | date | 否 | 查询日期 |
| start_time | time | 否 | 可预约开始时间 |
| end_time | time | 否 | 可预约结束时间 |

**响应：**
```json
{
  "code": 0,
  "data": [
    {
      "id": 1001,
      "name": "李技师",
      "avatar": "https://...",
      "skill_level": 3,
      "skilled_items": ["肩颈推拿", "艾灸理疗"],
      "is_online": 1,
      "month_service_count": 45,
      "month_rating": 4.9,
      "available_slots": [
        { "time": "10:00", "room_id": 1 },
        { "time": "14:00", "room_id": 2 }
      ]
    }
  ]
}
```

#### 3.2.2 技师签到/签退

```
POST /api/v1/store/technicians/{id}/check-in
POST /api/v1/store/technicians/{id}/check-out
```

### 3.3 房间管理 /store/room

| 接口 | 方法 | 说明 |
|------|------|------|
| GET /api/v1/store/rooms | GET | 房间列表 |
| POST /api/v1/store/rooms | POST | 创建房间 |
| PUT /api/v1/store/rooms/{id} | PUT | 更新房间 |
| GET /api/v1/store/rooms/{id}/status | GET | 房间实时状态 |
| GET /api/v1/store/rooms/real-time-status | GET | 全店房间实时状态 |

### 3.4 排班管理 /store/schedule

| 接口 | 方法 | 说明 |
|------|------|------|
| GET /api/v1/store/schedules | GET | 排班列表 |
| POST /api/v1/store/schedules | POST | 创建排班 |
| PUT /api/v1/store/schedules/{id} | PUT | 更新排班 |
| POST /api/v1/store/schedules/batch | POST | 批量排班 |
| GET /api/v1/store/schedules/week-view | GET | 周视图 |

---

## 四、交易域API

### 4.1 预约管理 /trade/appointment

#### 4.1.1 创建预约

```
POST /api/v1/trade/appointments
```

**请求体：**
```json
{
  "member_id": 50001,
  "store_id": 101,
  "service_item_id": 2001,
  "technician_id": 1001,
  "appointment_date": "2026-06-05",
  "appointment_time": "14:00",
  "duration_minutes": 60,
  "source_channel": "miniprogram",
  "consultation_notes": "肩颈不适，想做推拿"
}
```

**响应：**
```json
{
  "code": 0,
  "data": {
    "id": 80001,
    "appointment_no": "APT202606050001",
    "member_name": "张三",
    "service_item_name": "肩颈推拿(60分钟)",
    "technician_name": "李技师",
    "appointment_date": "2026-06-05",
    "appointment_time": "14:00",
    "status": 1,
    "created_at": "2026-06-04T10:00:00+08:00"
  }
}
```

#### 4.1.2 预约状态变更

```
PUT /api/v1/trade/appointments/{id}/status
```

**请求体：**
```json
{
  "status": 3,
  "start_time": "2026-06-05T14:00:00",
  "remark": "客户准时到店"
}
```

**状态流转：** 1待确认 → 2已确认 → 3服务中 → 4已完成 / 5已取消 / 6超时未到

#### 4.1.3 预约相关接口

| 接口 | 方法 | 说明 |
|------|------|------|
| GET /api/v1/trade/appointments | GET | 预约列表 |
| GET /api/v1/trade/appointments/{id} | GET | 预约详情 |
| PUT /api/v1/trade/appointments/{id} | PUT | 修改预约 |
| DELETE /api/v1/trade/appointments/{id} | DELETE | 取消预约 |
| GET /api/v1/trade/appointments/time-slots | GET | 查询可用时段 |
| POST /api/v1/trade/appointments/{id}/check-in | POST | 到店签到 |

### 4.2 订单管理 /trade/order

#### 4.2.1 创建订单

```
POST /api/v1/trade/orders
```

**请求体：**
```json
{
  "store_id": 101,
  "member_id": 50001,
  "appointment_id": 80001,
  "technician_id": 1001,
  "source_channel": "miniprogram",
  "items": [
    {
      "service_item_id": 2001,
      "technician_id": 1001,
      "quantity": 1,
      "unit_price": "168.00",
      "total_amount": "168.00"
    }
  ],
  "coupon_ids": [3001],
  "points_to_deduct": 100,
  "payment_method": 1,
  "remark": "客户要求加钟"
}
```

#### 4.2.2 订单相关接口

| 接口 | 方法 | 说明 |
|------|------|------|
| GET /api/v1/trade/orders | GET | 订单列表 |
| GET /api/v1/trade/orders/{id} | GET | 订单详情 |
| GET /api/v1/trade/orders/{id}/items | GET | 订单明细 |
| POST /api/v1/trade/orders/{id}/pay | POST | 支付订单 |
| POST /api/v1/trade/orders/{id}/refund | POST | 退款 |
| POST /api/v1/trade/orders/{id}/complete | POST | 确认完成 |
| GET /api/v1/trade/orders/daily-summary | GET | 日结汇总 |

### 4.3 收银相关 /trade/cashier

| 接口 | 方法 | 说明 |
|------|------|------|
| POST /api/v1/trade/cashier/quick-order | POST | 快速收银 |
| POST /api/v1/trade/cashier/pay | POST | 支付 |
| GET /api/v1/trade/cashier/pending-orders | GET | 待处理订单 |
| GET /api/v1/trade/cashier/daily-report | GET | 日结报表 |

### 4.4 优惠券管理 /trade/coupon

| 接口 | 方法 | 说明 |
|------|------|------|
| POST /api/v1/trade/coupons | POST | 创建优惠券 |
| GET /api/v1/trade/coupons | GET | 优惠券列表 |
| POST /api/v1/trade/coupons/issue | POST | 发放优惠券 |
| POST /api/v1/trade/coupons/{id}/verify | POST | 核销验证 |
| GET /api/v1/trade/coupons/member/{member_id} | GET | 会员优惠券列表 |

### 4.5 疗程卡管理 /trade/treatment-card

| 接口 | 方法 | 说明 |
|------|------|------|
| POST /api/v1/trade/treatment-cards | POST | 购买疗程卡 |
| GET /api/v1/trade/treatment-cards | GET | 疗程卡列表 |
| GET /api/v1/trade/treatment-cards/{id} | GET | 疗程卡详情 |
| POST /api/v1/trade/treatment-cards/{id}/use | POST | 使用扣次 |
| GET /api/v1/trade/treatment-cards/member/{member_id} | GET | 会员疗程卡列表 |

---

## 五、AI能力API

### 5.1 AI问诊 /ai/consultation

#### 5.1.1 创建问诊会话

```
POST /api/v1/ai/consultations
```

**请求体：**
```json
{
  "member_id": 50001,
  "chief_complaint": "最近肩颈酸痛，久坐办公",
  "symptoms": ["肩颈酸痛", "偶尔头晕"],
  "duration": "1周",
  "history": "有颈椎病史"
}
```

#### 5.1.2 AI问诊相关接口

| 接口 | 方法 | 说明 |
|------|------|------|
| POST /api/v1/ai/consultations | POST | 创建问诊会话 |
| POST /api/v1/ai/consultations/{id}/messages | POST | 发送消息 |
| GET /api/v1/ai/consultations/{id} | GET | 获取问诊会话 |
| GET /api/v1/ai/consultations/{id}/messages | GET | 获取问诊消息 |
| POST /api/v1/ai/consultations/{id}/conclusion | POST | 获取问诊结论 |

### 5.2 AI舌诊 /ai/tongue

```
POST /api/v1/ai/tongue/diagnosis
```

**请求体：**
```json
{
  "member_id": 50001,
  "image_base64": "data:image/jpeg;base64,...",
  "consultation_id": 90001
}
```

**响应：**
```json
{
  "code": 0,
  "data": {
    "result_id": "TR20260604001",
    "tongue_color": "淡红舌",
    "tongue_coating": "薄白苔",
    "tongue_shape": "齿痕舌",
    "constitution_type": 2,
    "constitution_name": "气虚质",
    "confidence": 0.87,
    "analysis": "舌象分析...",
    "recommendations": [
      { "item": "艾灸理疗", "reason": "补气养血" },
      { "item": "肩颈推拿", "reason": "疏通经络" }
    ]
  }
}
```

### 5.3 AI话术 /ai/script

```
POST /api/v1/ai/scripts/generate
```

**请求体：**
```json
{
  "script_type": "consultation",
  "scene": "first_visit_intro",
  "customer_profile": {
    "constitution_type": 3,
    "interested_items": ["肩颈推拿", "艾灸"],
    "age_group": "30-40"
  },
  "context": "客户首次到店，咨询肩颈项目"
}
```

### 5.4 AI日报 /ai/daily-report

| 接口 | 方法 | 说明 |
|------|------|------|
| POST /api/v1/ai/daily-reports/generate | POST | 生成AI日报 |
| GET /api/v1/ai/daily-reports | GET | 日报列表 |
| GET /api/v1/ai/daily-reports/{id} | GET | 日报详情 |
| GET /api/v1/ai/daily-reports/store/{store_id}/today | GET | 获取今日日报 |

### 5.5 AI统一网关 /ai/gateway

```
POST /api/v1/ai/gateway/chat
```

**请求体：**
```json
{
  "task_type": "chat_customer",
  "messages": [
    { "role": "system", "content": "你是忠济堂的AI健康顾问..." },
    { "role": "user", "content": "我最近睡眠不好怎么办" }
  ],
  "stream": false,
  "metadata": {
    "tenant_id": 1001,
    "store_id": 101,
    "member_id": 50001,
    "conversation_id": "conv_xxx"
  }
}
```

**响应：**
```json
{
  "code": 0,
  "data": {
    "request_id": "uuid",
    "model_used": "deepseek-chat",
    "latency_ms": 850,
    "usage": {
      "prompt_tokens": 156,
      "completion_tokens": 234,
      "total_tokens": 390
    },
    "choices": [
      { "role": "assistant", "content": "睡眠不好建议体验我们的安神助眠理疗项目..." }
    ],
    "fallback_triggered": false,
    "cost_cents": 12
  }
}
```

### 5.6 AI推荐 /ai/recommendation

```
GET /api/v1/ai/recommendations/items
GET /api/v1/ai/recommendations/technicians
```

**查询参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| member_id | long | 是 | 会员ID |
| store_id | long | 是 | 门店ID |
| type | string | 否 | recommended/popular/seasonal |
| page | int | 否 | 页码 |
| page_size | int | 否 | 每页条数 |

---

## 六、平台对接API

### 6.1 线索管理 /integration/leads

| 接口 | 方法 | 说明 |
|------|------|------|
| GET /api/v1/integration/leads | GET | 线索列表(全渠道聚合) |
| GET /api/v1/integration/leads/{id} | GET | 线索详情 |
| PUT /api/v1/integration/leads/{id} | PUT | 更新线索 |
| PUT /api/v1/integration/leads/{id}/classify | PUT | 线索分类 |
| PUT /api/v1/integration/leads/{id}/assign | PUT | 线索分配 |
| PUT /api/v1/integration/leads/{id}/convert | PUT | 线索转预约 |

### 6.2 平台配置 /integration/platforms

| 接口 | 方法 | 说明 |
|------|------|------|
| GET /api/v1/integration/platforms | GET | 平台列表 |
| POST /api/v1/integration/platforms | POST | 添加平台配置 |
| PUT /api/v1/integration/platforms/{id} | PUT | 更新平台配置 |
| POST /api/v1/integration/platforms/{id}/auth | POST | 完成平台授权 |
| GET /api/v1/integration/platforms/{id}/sync-status | GET | 同步状态 |

### 6.3 团购核销 /integration/orders

| 接口 | 方法 | 说明 |
|------|------|------|
| POST /api/v1/integration/orders/verify | POST | 核销验证 |
| POST /api/v1/integration/orders/confirm | POST | 确认核销 |
| GET /api/v1/integration/orders | GET | 核销订单列表 |

---

## 七、SaaS计费API

### 7.1 租户管理 /billing/tenant

| 接口 | 方法 | 说明 |
|------|------|------|
| POST /api/v1/billing/tenants | POST | 创建租户(注册) |
| GET /api/v1/billing/tenants/{id} | GET | 租户详情 |
| PUT /api/v1/billing/tenants/{id} | PUT | 更新租户 |
| PUT /api/v1/billing/tenants/{id}/whitelabel | PUT | 配置白标 |

### 7.2 订阅管理 /billing/subscription

| 接口 | 方法 | 说明 |
|------|------|------|
| GET /api/v1/billing/plans | GET | 套餐列表 |
| POST /api/v1/billing/subscriptions | POST | 订阅套餐 |
| GET /api/v1/billing/subscriptions/current | GET | 当前订阅 |
| POST /api/v1/billing/subscriptions/{id}/renew | POST | 续费 |
| PUT /api/v1/billing/subscriptions/{id}/cancel | PUT | 取消订阅 |

### 7.3 用量查询 /billing/usage

| 接口 | 方法 | 说明 |
|------|------|------|
| GET /api/v1/billing/usage | GET | 用量列表 |
| GET /api/v1/billing/usage/summary | GET | 用量汇总 |
| GET /api/v1/billing/usage/daily | GET | 日用量明细 |
| GET /api/v1/billing/bills | GET | 账单列表 |

---

## 八、BI数据API

### 8.1 经营看板 /bi/dashboard

#### 8.1.1 门店经营看板

```
GET /api/v1/bi/stores/{store_id}/dashboard
```

**响应：**
```json
{
  "code": 0,
  "data": {
    "date": "2026-06-04",
    "metrics": {
      "revenue": { "value": 12580.00, "yoy": 15.3, "mom": 8.2 },
      "customer_count": { "value": 38, "yoy": 10.5, "mom": 5.1 },
      "avg_per_customer": { "value": 331.05, "yoy": 4.3, "mom": 3.0 },
      "appointment_count": { "value": 42, "yoy": 12.1 },
      "completion_rate": { "value": 95.2 },
      "return_visit_rate": { "value": 68.4 },
      "tech_rating_avg": { "value": 4.85 },
      "target_completion": { "value": 72.5 }
    },
    "realtime": {
      "online_technicians": 6,
      "busy_rooms": 3,
      "waiting_customers": 2
    },
    "trends": {
      "revenue_7days": [...],
      "peak_hours": [...]
    }
  }
}
```

#### 8.1.2 BI看板相关接口

| 接口 | 方法 | 说明 |
|------|------|------|
| GET /api/v1/bi/stores/{store_id}/dashboard | GET | 门店经营看板 |
| GET /api/v1/bi/stores/{store_id}/daily-report | GET | 店长日报数据 |
| GET /api/v1/bi/stores/{store_id}/technicians/ranking | GET | 技师排行榜 |
| GET /api/v1/bi/region/{region_id}/dashboard | GET | 片区看板 |
| GET /api/v1/bi/headquarters/dashboard | GET | 总部全国看板 |
| GET /api/v1/bi/stores/ranking | GET | 门店排行榜 |
| GET /api/v1/bi/trends/{metric} | GET | 指标趋势分析 |

---

## 九、管理后台API

### 9.1 系统配置 /admin/config

| 接口 | 方法 | 说明 |
|------|------|------|
| GET /api/v1/admin/configs | GET | 配置列表 |
| PUT /api/v1/admin/configs/{key} | PUT | 更新配置 |
| GET /api/v1/admin/configs/feature-flags | GET | 功能开关列表 |
| PUT /api/v1/admin/configs/feature-flags/{key} | PUT | 更新功能开关 |

### 9.2 服务项目 /admin/service-items

| 接口 | 方法 | 说明 |
|------|------|------|
| POST /api/v1/admin/service-items | POST | 创建项目 |
| GET /api/v1/admin/service-items | GET | 项目列表 |
| PUT /api/v1/admin/service-items/{id} | PUT | 更新项目 |
| PUT /api/v1/admin/service-items/{id}/status | PUT | 项目上下架 |
| POST /api/v1/admin/service-items/batch-status | POST | 批量上下架 |

### 9.3 会员等级 /admin/member-levels

| 接口 | 方法 | 说明 |
|------|------|------|
| GET /api/v1/admin/member-levels | GET | 等级列表 |
| POST /api/v1/admin/member-levels | POST | 创建等级 |
| PUT /api/v1/admin/member-levels/{id} | PUT | 更新等级 |

### 9.4 审计日志 /admin/audit-logs

```
GET /api/v1/admin/audit-logs
```

**查询参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| module | string | 否 | 模块 |
| action | string | 否 | 操作类型 |
| operator_id | long | 否 | 操作人 |
| start_date | date | 否 | 开始日期 |
| end_date | date | 否 | 结束日期 |

---

## 十、Webhook事件

### 10.1 事件列表

| 事件 | 说明 | 回调URL |
|------|------|----------|
| appointment.created | 预约创建 | /webhook/appointment/created |
| appointment.completed | 预约完成 | /webhook/appointment/completed |
| appointment.cancelled | 预约取消 | /webhook/appointment/cancelled |
| order.paid | 订单支付成功 | /webhook/order/paid |
| order.refunded | 订单退款 | /webhook/order/refunded |
| member.registered | 新会员注册 | /webhook/member/registered |
| member.level_changed | 会员等级变更 | /webhook/member/level-changed |
| integration.lead_created | 新线索接入 | /webhook/integration/lead-created |

### 10.2 事件推送格式

```json
{
  "event": "appointment.completed",
  "event_id": "evt_xxx",
  "timestamp": "2026-06-04T14:30:00+08:00",
  "tenant_id": 1001,
  "data": {
    "appointment_id": 80001,
    "store_id": 101,
    "member_id": 50001,
    "technician_id": 1001,
    "service_item_id": 2001,
    "completed_at": "2026-06-04T15:30:00+08:00"
  },
  "signature": "HMAC-SHA256(..., secret)"
}
```

---

> **文档版本：V3.3**
> **编制日期：2026年6月**
> **审核状态：草稿**
