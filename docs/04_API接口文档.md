# 忠济堂中医养生连锁管理系统 · API接口文档（V3.4）

> **版本：V3.4 | 日期：2026年6月**
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
| 40902 | 时段已被占用 |
| 40903 | 时段临时锁定中 |
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

### 3.5 技师推广 /store/technician-promotion

#### 3.5.1 获取推广信息

```
GET /api/v1/store/technicians/{id}/promotion
```

**路径参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | 技师ID |

**响应：**
```json
{
  "code": 0,
  "data": {
    "technician_id": 1001,
    "technician_name": "李技师",
    "avatar": "https://...",
    "promotion_enabled": true,
    "promotion_code": "TC1001PROMO",
    "share_link": "https://app.zhongjitang.com/promo/TC1001PROMO",
    "qr_code_url": "https://cdn.zhongjitang.com/qr/TC1001PROMO.png",
    "commission_rate": "10.00",
    "total_promoted_customers": 56,
    "total_commission": "3280.00"
  }
}
```

#### 3.5.2 生成推广海报

```
POST /api/v1/store/technicians/{id}/promotion/generate-poster
```

**路径参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | 技师ID |

**请求体：**
```json
{
  "poster_template": "default",
  "custom_text": "专业肩颈推拿，欢迎预约体验",
  "include_qr_code": true
}
```

**响应：**
```json
{
  "code": 0,
  "data": {
    "poster_url": "https://cdn.zhongjitang.com/posters/TC1001_20260604.png",
    "thumbnail_url": "https://cdn.zhongjitang.com/posters/TC1001_20260604_thumb.png",
    "expires_at": "2026-07-04T23:59:59+08:00"
  }
}
```

#### 3.5.3 推广统计

```
GET /api/v1/store/technicians/{id}/promotion/stats
```

**路径参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | 技师ID |

**查询参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| start_date | date | 否 | 统计起始日期 |
| end_date | date | 否 | 统计结束日期 |

**响应：**
```json
{
  "code": 0,
  "data": {
    "technician_id": 1001,
    "period": { "start_date": "2026-05-01", "end_date": "2026-05-31" },
    "metrics": {
      "total_clicks": 320,
      "total_views": 580,
      "total_appointments": 45,
      "completed_appointments": 38,
      "conversion_rate": 6.56,
      "total_commission": "2280.00"
    },
    "daily_trend": [
      { "date": "2026-05-01", "clicks": 12, "views": 20, "appointments": 2 }
    ]
  }
}
```

#### 3.5.4 佣金明细

```
GET /api/v1/store/technicians/{id}/promotion/commissions
```

**路径参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | 技师ID |

**查询参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 否 | 佣金状态：pending/settled/cancelled |
| start_date | date | 否 | 起始日期 |
| end_date | date | 否 | 结束日期 |
| page | int | 否 | 页码，默认1 |
| page_size | int | 否 | 每页条数，默认20 |

**响应：**
```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "id": 7001,
        "order_id": 60001,
        "order_no": "ORD202606050001",
        "customer_name": "王**",
        "commission_amount": "58.00",
        "commission_rate": "10.00",
        "order_amount": "580.00",
        "status": "settled",
        "settled_at": "2026-06-05T18:00:00+08:00",
        "created_at": "2026-06-05T15:30:00+08:00"
      }
    ],
    "pagination": {
      "page": 1,
      "page_size": 20,
      "total": 38,
      "total_pages": 2
    },
    "summary": {
      "total_pending": "320.00",
      "total_settled": "2280.00",
      "total_cancelled": "80.00"
    }
  }
}
```

#### 3.5.5 推广排行榜

```
GET /api/v1/store/technicians/{id}/promotion/ranking
```

**路径参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | 技师ID |

**查询参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| store_id | long | 否 | 门店ID，不填则全租户 |
| period | string | 否 | 排行周期：week/month/quarter，默认month |

**响应：**
```json
{
  "code": 0,
  "data": {
    "period": "month",
    "my_rank": {
      "rank": 3,
      "technician_id": 1001,
      "technician_name": "李技师",
      "total_promoted": 38,
      "total_commission": "2280.00"
    },
    "ranking": [
      {
        "rank": 1,
        "technician_id": 1005,
        "technician_name": "张技师",
        "avatar": "https://...",
        "total_promoted": 52,
        "total_commission": "3120.00"
      },
      {
        "rank": 2,
        "technician_id": 1003,
        "technician_name": "王技师",
        "avatar": "https://...",
        "total_promoted": 45,
        "total_commission": "2700.00"
      }
    ]
  }
}
```

---

## 四、交易域API

### 4.1 预约管理 /trade/appointment

#### 4.1.1 查询可用时段

```
GET /api/v1/trade/appointments/available-slots
```

**查询参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| store_id | long | 是 | 门店ID |
| technician_id | long | 否 | 技师ID，0=不指定技师 |
| date | date | 是 | 查询日期 |

**响应：**
```json
{
  "code": 0,
  "data": {
    "store_id": 101,
    "date": "2026-06-05",
    "technician_id": 1001,
    "time_slots": [
      { "time_slot": "09:00", "available": true, "remaining_count": 3 },
      { "time_slot": "10:00", "available": true, "remaining_count": 1 },
      { "time_slot": "11:00", "available": false, "remaining_count": 0 },
      { "time_slot": "14:00", "available": true, "remaining_count": 2 },
      { "time_slot": "15:00", "available": false, "remaining_count": 0 },
      { "time_slot": "16:00", "available": true, "remaining_count": 2 }
    ]
  }
}
```

> **说明：** `available=false` 表示该时段已被占用（灰色不可选），`remaining_count` 表示该时段剩余可预约数量。

#### 4.1.2 临时锁定时段

```
POST /api/v1/trade/appointments/lock-temp
```

**请求体：**
```json
{
  "store_id": 101,
  "technician_id": 1001,
  "date": "2026-06-05",
  "time_slot": "14:00"
}
```

**参数说明：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| store_id | long | 是 | 门店ID |
| technician_id | long | 是 | 技师ID，0=不指定技师 |
| date | date | 是 | 预约日期 |
| time_slot | string | 是 | 时段，如"14:00" |

**响应：**
```json
{
  "code": 0,
  "data": {
    "lock_id": "LOCK202606041400001",
    "store_id": 101,
    "technician_id": 1001,
    "date": "2026-06-05",
    "time_slot": "14:00",
    "expire_at": "2026-06-04T10:05:00+08:00"
  }
}
```

> **说明：** 临时锁定有效期为5分钟，超时自动释放。在锁定期间，其他用户无法预约该时段。创建预约时需携带 `lock_id` 以确保时段一致性。

#### 4.1.3 创建预约

```
POST /api/v1/trade/appointments
```

**请求体：**
```json
{
  "store_id": 101,
  "date": "2026-06-05",
  "time_slot": "14:00",
  "technician_id": 1001,
  "lock_id": "LOCK202606041400001",
  "service_item_id": 2001,
  "member_id": 50001,
  "duration_minutes": 60,
  "source_channel": "miniprogram",
  "consultation_notes": "肩颈不适，想做推拿"
}
```

**参数说明：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| store_id | long | 是 | 门店ID |
| date | date | 是 | 预约日期 |
| time_slot | string | 是 | 预约时段，如"14:00" |
| technician_id | long | 否 | 技师ID，0或不传=不指定技师 |
| lock_id | string | 否 | 临时锁定ID（建议携带，确保时段一致） |
| service_item_id | long | 否 | 服务项目ID |
| member_id | long | 否 | 会员ID |
| duration_minutes | int | 否 | 服务时长（分钟） |
| source_channel | string | 否 | 来源渠道 |
| consultation_notes | string | 否 | 咨询备注 |

**响应：**
```json
{
  "code": 0,
  "data": {
    "id": 80001,
    "appointment_no": "APT202606050001",
    "member_name": "张三",
    "store_name": "忠济堂·朝阳店",
    "service_item_name": "肩颈推拿(60分钟)",
    "technician_name": "李技师",
    "appointment_date": "2026-06-05",
    "appointment_time": "14:00",
    "status": 1,
    "redirect": "my_appointments",
    "created_at": "2026-06-04T10:00:00+08:00"
  }
}
```

> **说明：** 创建成功后前端应跳转至"我的预约"页面（`redirect: "my_appointments"`）。`technician_id` 为0时表示不指定技师，由门店分配。

#### 4.1.4 修改预约

```
PUT /api/v1/trade/appointments/{id}/modify
```

**路径参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | 预约ID |

**请求体：**
```json
{
  "date": "2026-06-06",
  "time_slot": "15:00",
  "technician_id": 1002,
  "modify_reason": "临时有事需要改期"
}
```

**参数说明：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| date | date | 否 | 新预约日期 |
| time_slot | string | 否 | 新预约时段 |
| technician_id | long | 否 | 新技师ID，0=不指定技师 |
| modify_reason | string | 是 | 修改原因（必填） |

**响应：**
```json
{
  "code": 0,
  "data": {
    "id": 80001,
    "appointment_no": "APT202606050001",
    "member_name": "张三",
    "store_name": "忠济堂·朝阳店",
    "service_item_name": "肩颈推拿(60分钟)",
    "technician_name": "王技师",
    "appointment_date": "2026-06-06",
    "appointment_time": "15:00",
    "status": 1,
    "modify_reason": "临时有事需要改期",
    "modified_at": "2026-06-04T11:00:00+08:00"
  }
}
```

> **说明：** 修改预约时，原时段自动释放，新时段自动锁定。仅允许修改待确认(1)和已确认(2)状态的预约。

#### 4.1.5 取消预约

```
PUT /api/v1/trade/appointments/{id}/cancel
```

**路径参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | 预约ID |

**请求体：**
```json
{
  "cancel_reason": "身体不适，需要取消"
}
```

**参数说明：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| cancel_reason | string | 是 | 取消原因（必填） |

**响应：**
```json
{
  "code": 0,
  "data": {
    "id": 80001,
    "appointment_no": "APT202606050001",
    "status": 5,
    "cancel_reason": "身体不适，需要取消",
    "slot_released": true,
    "cancelled_at": "2026-06-04T12:00:00+08:00"
  }
}
```

> **说明：** 取消预约后，该预约占用的时段自动释放（`slot_released: true`），其他用户可重新预约该时段。仅允许取消待确认(1)和已确认(2)状态的预约。

#### 4.1.6 我的预约列表

```
GET /api/v1/trade/appointments/my
```

**查询参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status_group | string | 否 | 分组筛选：pending/completed/cancelled/all，默认all |
| page | int | 否 | 页码，默认1 |
| page_size | int | 否 | 每页条数，默认20 |

**响应：**
```json
{
  "code": 0,
  "data": {
    "pending": {
      "count": 2,
      "list": [
        {
          "id": 80001,
          "appointment_no": "APT202606050001",
          "store_name": "忠济堂·朝阳店",
          "service_item_name": "肩颈推拿(60分钟)",
          "technician_name": "李技师",
          "appointment_date": "2026-06-05",
          "appointment_time": "14:00",
          "status": 1,
          "status_name": "待确认",
          "created_at": "2026-06-04T10:00:00+08:00"
        }
      ]
    },
    "completed": {
      "count": 5,
      "list": [
        {
          "id": 79001,
          "appointment_no": "APT202606030001",
          "store_name": "忠济堂·朝阳店",
          "service_item_name": "艾灸理疗(45分钟)",
          "technician_name": "王技师",
          "appointment_date": "2026-06-03",
          "appointment_time": "10:00",
          "status": 4,
          "status_name": "已完成",
          "completed_at": "2026-06-03T10:45:00+08:00"
        }
      ]
    },
    "cancelled": {
      "count": 1,
      "list": [
        {
          "id": 78501,
          "appointment_no": "APT202606010002",
          "store_name": "忠济堂·朝阳店",
          "service_item_name": "足底推拿(30分钟)",
          "technician_name": "赵技师",
          "appointment_date": "2026-06-01",
          "appointment_time": "16:00",
          "status": 5,
          "status_name": "已取消",
          "cancel_reason": "临时有事",
          "cancelled_at": "2026-06-01T14:00:00+08:00"
        }
      ]
    }
  }
}
```

> **说明：** 该接口为客户视角，仅返回当前登录会员的预约记录。按待服务(pending)、已完成(completed)、已取消(cancelled)三组返回，每组包含数量和列表。

#### 4.1.7 预约状态变更

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

#### 4.1.8 预约相关接口

| 接口 | 方法 | 说明 |
|------|------|------|
| GET /api/v1/trade/appointments | GET | 预约列表（管理端） |
| GET /api/v1/trade/appointments/{id} | GET | 预约详情 |
| GET /api/v1/trade/appointments/available-slots | GET | 查询可用时段 |
| POST /api/v1/trade/appointments/lock-temp | POST | 临时锁定时段 |
| POST /api/v1/trade/appointments | POST | 创建预约 |
| PUT /api/v1/trade/appointments/{id}/modify | PUT | 修改预约 |
| PUT /api/v1/trade/appointments/{id}/cancel | PUT | 取消预约 |
| GET /api/v1/trade/appointments/my | GET | 我的预约列表（客户视角） |
| PUT /api/v1/trade/appointments/{id}/status | PUT | 预约状态变更 |
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

### 6.4 推广追踪 /integration/promotion

#### 6.4.1 推广事件追踪

```
POST /api/v1/integration/promotion/track
```

**请求体：**
```json
{
  "event_type": "click",
  "promotion_code": "TC1001PROMO",
  "technician_id": 1001,
  "source_channel": "wechat_share",
  "customer_id": 50002,
  "customer_ip": "192.168.1.1",
  "user_agent": "Mozilla/5.0...",
  "extra": {
    "share_scene": "friend",
    "page_url": "https://app.zhongjitang.com/promo/TC1001PROMO"
  }
}
```

**参数说明：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| event_type | string | 是 | 事件类型：click/view/appointment/complete |
| promotion_code | string | 是 | 推广码 |
| technician_id | long | 是 | 技师ID |
| source_channel | string | 否 | 来源渠道：wechat_share/poster/qr_code/miniprogram |
| customer_id | long | 否 | 客户ID（已登录时传入） |
| customer_ip | string | 否 | 客户IP |
| user_agent | string | 否 | 客户UA |
| extra | object | 否 | 扩展信息 |

**响应：**
```json
{
  "code": 0,
  "data": {
    "track_id": "TRK20260604100001",
    "event_type": "click",
    "tracked_at": "2026-06-04T10:00:00+08:00"
  }
}
```

#### 6.4.2 推广漏斗数据

```
GET /api/v1/integration/promotion/funnel
```

**查询参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| technician_id | long | 否 | 技师ID，不填则全租户汇总 |
| store_id | long | 否 | 门店ID |
| start_date | date | 否 | 起始日期 |
| end_date | date | 否 | 结束日期 |

**响应：**
```json
{
  "code": 0,
  "data": {
    "period": { "start_date": "2026-05-01", "end_date": "2026-05-31" },
    "funnel": {
      "view": { "count": 1200, "rate": 100.0 },
      "click": { "count": 580, "rate": 48.3 },
      "appointment": { "count": 120, "rate": 10.0 },
      "complete": { "count": 95, "rate": 7.9 }
    },
    "conversion": {
      "view_to_click": 48.3,
      "click_to_appointment": 20.7,
      "appointment_to_complete": 79.2,
      "overall_conversion": 7.9
    },
    "by_channel": [
      {
        "channel": "wechat_share",
        "view": 500,
        "click": 280,
        "appointment": 65,
        "complete": 52
      },
      {
        "channel": "poster",
        "view": 400,
        "click": 180,
        "appointment": 35,
        "complete": 28
      },
      {
        "channel": "qr_code",
        "view": 300,
        "click": 120,
        "appointment": 20,
        "complete": 15
      }
    ]
  }
}
```

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

### 8.2 预约漏斗 /data/appointment-funnel

#### 8.2.1 预约埋点上报

```
POST /api/v1/data/appointment-funnel/track
```

**请求体：**
```json
{
  "event_type": "view_time_slots",
  "store_id": 101,
  "member_id": 50001,
  "session_id": "sess_abc123",
  "technician_id": 1001,
  "date": "2026-06-05",
  "time_slot": "14:00",
  "extra": {
    "source_page": "technician_detail",
    "stay_duration_ms": 3500
  }
}
```

**参数说明：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| event_type | string | 是 | 埋点事件类型，见下方枚举 |
| store_id | long | 是 | 门店ID |
| member_id | long | 否 | 会员ID（未登录时可不传） |
| session_id | string | 是 | 会话ID，用于串联同一用户流程 |
| technician_id | long | 否 | 技师ID |
| date | date | 否 | 预约日期 |
| time_slot | string | 否 | 时段 |
| extra | object | 否 | 扩展信息 |

**event_type 枚举：**
| 事件 | 说明 |
|------|------|
| enter_appointment_page | 进入预约页面 |
| view_time_slots | 浏览时段列表 |
| select_time_slot | 选择时段 |
| lock_time_slot | 锁定时段 |
| submit_appointment | 提交预约 |
| appointment_success | 预约成功 |
| appointment_failed | 预约失败 |
| modify_appointment | 修改预约 |
| cancel_appointment | 取消预约 |

**响应：**
```json
{
  "code": 0,
  "data": {
    "track_id": "FTRK20260604100001",
    "event_type": "view_time_slots",
    "tracked_at": "2026-06-04T10:00:00+08:00"
  }
}
```

#### 8.2.2 预约漏斗统计

```
GET /api/v1/data/appointment-funnel/statistics
```

**查询参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| store_id | long | 否 | 门店ID，不填则全租户汇总 |
| start_date | date | 否 | 起始日期 |
| end_date | date | 否 | 结束日期 |
| source_channel | string | 否 | 来源渠道筛选 |
| group_by | string | 否 | 分组维度：store/channel/technician/date |

**响应：**
```json
{
  "code": 0,
  "data": {
    "period": { "start_date": "2026-05-01", "end_date": "2026-05-31" },
    "funnel": {
      "enter_appointment_page": { "count": 5000, "rate": 100.0 },
      "view_time_slots": { "count": 3800, "rate": 76.0 },
      "select_time_slot": { "count": 2200, "rate": 44.0 },
      "lock_time_slot": { "count": 1800, "rate": 36.0 },
      "submit_appointment": { "count": 1500, "rate": 30.0 },
      "appointment_success": { "count": 1350, "rate": 27.0 },
      "appointment_failed": { "count": 150, "rate": 3.0 }
    },
    "conversion": {
      "page_to_view": 76.0,
      "view_to_select": 57.9,
      "select_to_lock": 81.8,
      "lock_to_submit": 83.3,
      "submit_to_success": 90.0,
      "overall_conversion": 27.0
    },
    "failure_reasons": [
      { "reason": "time_slot_occupied", "count": 80, "percentage": 53.3 },
      { "reason": "lock_expired", "count": 40, "percentage": 26.7 },
      { "reason": "network_error", "count": 30, "percentage": 20.0 }
    ],
    "groups": [
      {
        "dimension": "store",
        "dimension_value": "忠济堂·朝阳店",
        "enter_appointment_page": 1500,
        "view_time_slots": 1200,
        "select_time_slot": 700,
        "lock_time_slot": 580,
        "submit_appointment": 480,
        "appointment_success": 432,
        "overall_conversion": 28.8
      }
    ]
  }
}
```

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
| appointment.modified | 预约修改 | /webhook/appointment/modified |
| appointment.slot_locked | 时段锁定 | /webhook/appointment/slot-locked |
| appointment.slot_released | 时段释放 | /webhook/appointment/slot-released |
| order.paid | 订单支付成功 | /webhook/order/paid |
| order.refunded | 订单退款 | /webhook/order/refunded |
| member.registered | 新会员注册 | /webhook/member/registered |
| member.level_changed | 会员等级变更 | /webhook/member/level-changed |
| integration.lead_created | 新线索接入 | /webhook/integration/lead-created |
| promotion.event_tracked | 推广事件追踪 | /webhook/promotion/event-tracked |

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

> **文档版本：V3.4**
> **编制日期：2026年6月**
> **审核状态：草稿**
