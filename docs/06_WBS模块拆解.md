# 忠济堂中医养生连锁管理系统 · WBS开发任务计划（V3.4）

> **版本：V3.4 | 日期：2026年6月**
> **说明：按阶段+批次拆分的最小开发单元，AI开发唯一调度依据**
> **规则：一次只做一个任务，验收通过再继续**

---

## 一、开发阶段总览

| 阶段 | 批次 | 模块 | 任务数 | 人天 | 前置依赖 |
|------|------|------|--------|------|----------|
| 阶段0 | - | 项目骨架+基础设施 | 12 | 20 | 无 |
| 阶段1 | 批次1 | 基础数据(门店/技师/时段/项目) | 10 | 29 | 阶段0 |
| 阶段2 | 批次2 | C端预约(4步流程+锁档) | 8 | 33 | 阶段1 |
| 阶段3 | 批次3 | 我的预约(改/取消闭环) | 6 | 18 | 阶段2 |
| 阶段4 | 批次4 | 技师端(导航+推广) | 8 | 26 | 阶段1 |
| 阶段5 | 批次5 | 后台管理+数据统计 | 8 | 29 | 阶段1-4 |
| 阶段6 | 批次6 | AI能力(问诊/话术/日报) | 7 | 35 | 阶段2 |
| 阶段7 | 批次7 | 平台对接(企微/抖音/美团) | 6 | 40 | 阶段5 |
| 阶段8 | 批次8 | SaaS多租户+计费 | 6 | 30 | 阶段5 |
| 阶段9 | - | 全量测试+灰度上线 | 5 | 20 | 阶段6-8 |
| **合计** | | | **76** | **280** | |

---

## 二、阶段0：项目骨架+基础设施

> 目标：搭建可运行的基础工程，骨架完工再开始业务模块

### S0-01 后端项目初始化

| 项目 | 内容 |
|------|------|
| 任务编号 | S0-01 |
| 任务名称 | 后端Spring Boot项目初始化 |
| 前置依赖 | 无 |
| 交付文件 | pom.xml(父POM), zjt-common/pom.xml, zjt-common-core/pom.xml |
| 代码清单 | 1. 父POM(Spring Boot 2.7+依赖管理)<br>2. zjt-common-core模块<br>3. com.zhongjitang.common.core.result.R<T> 统一响应<br>4. com.zhongjitang.common.core.result.PageResult 分页响应<br>5. com.zhongjitang.common.core.exception.BusinessException 业务异常<br>6. com.zhongjitang.common.core.exception.ErrorCode 错误码枚举<br>7. com.zhongjitang.common.core.context.TenantContext 租户上下文<br>8. com.zhongjitang.common.core.context.UserContext 用户上下文 |
| SQL | 无 |
| 测试 | R<T>构造测试、BusinessException抛出测试 |
| 验收标准 | mvn clean compile 编译通过 |

### S0-02 全局异常+统一响应+参数校验

| 项目 | 内容 |
|------|------|
| 任务编号 | S0-02 |
| 任务名称 | 全局异常处理器+统一响应+参数校验 |
| 前置依赖 | S0-01 |
| 交付文件 | zjt-common-core/src/main/java/com/zhongjitang/common/core/ |
| 代码清单 | 1. GlobalExceptionHandler(@RestControllerAdvice)<br>2. 参数校验异常处理(MethodArgumentNotValidException)<br>3. 业务异常处理(BusinessException)<br>4. 通用异常处理(Exception)<br>5. @Valid参数校验注解示例 |
| SQL | 无 |
| 测试 | GlobalExceptionHandler单元测试(4种异常场景) |
| 验收标准 | 访问不存在的接口返回统一JSON格式{code,message,data} |

### S0-03 JWT鉴权+多租户拦截器

| 项目 | 内容 |
|------|------|
| 任务编号 | S0-03 |
| 任务名称 | JWT工具类+认证拦截器+多租户拦截器 |
| 前置依赖 | S0-01 |
| 交付文件 | zjt-common-security/ |
| 代码清单 | 1. JwtUtil(Token生成/解析/刷新/验证)<br>2. AuthInterceptor(JWT校验+用户信息注入)<br>3. TenantInterceptor(tenant_id注入/清除)<br>4. SecurityConfig(拦截器注册+白名单路径)<br>5. LoginRequest/LoginResponse DTO |
| SQL | 无 |
| 测试 | JwtUtil生成/解析/过期测试, 拦截器放行/拦截测试 |
| 验收标准 | 无Token返回401, 有Token正常访问并注入tenant_id |

### S0-04 MyBatis Plus+多租户插件+数据库连接

| 项目 | 内容 |
|------|------|
| 任务编号 | S0-04 |
| 任务名称 | MyBatis Plus配置+多租户行级隔离+数据库连接 |
| 前置依赖 | S0-01 |
| 交付文件 | zjt-common-mybatis/ |
| 代码清单 | 1. MybatisPlusConfig(分页插件+多租户插件)<br>2. TenantLineHandler(tenant_id自动注入)<br>3. BaseDO(审计字段基类: id/tenant_id/store_id/created_at/updated_at/created_by/updated_by/is_deleted)<br>4. application-dev.yml(数据库连接配置) |
| SQL | CREATE DATABASE zjt_dev DEFAULT CHARSET utf8mb4; |
| 测试 | 多租户插件自动注入tenant_id测试 |
| 验收标准 | 插入数据自动带tenant_id, 查询自动过滤tenant_id |

### S0-05 Redis配置+分布式锁工具

| 项目 | 内容 |
|------|------|
| 任务编号 | S0-05 |
| 任务名称 | Redis配置+分布式锁工具类 |
| 前置依赖 | S0-01 |
| 交付文件 | zjt-common-redis/ |
| 代码清单 | 1. RedisConfig(Lettuce连接池)<br>2. RedisUtil(get/set/delete/expire)<br>3. DistributedLockUtil(SETNX锁+超时+重试)<br>4. CacheUtil(缓存穿透防护-布隆过滤器/缓存击穿防护-互斥锁) |
| SQL | 无 |
| 测试 | DistributedLockUtil加锁/释放/超时测试 |
| 验收标准 | 分布式锁在并发场景下互斥, 5分钟超时自动释放 |

### S0-06 Swagger/OpenAPI配置

| 项目 | 内容 |
|------|------|
| 任务编号 | S0-06 |
| 任务名称 | Swagger/OpenAPI接口文档配置 |
| 前置依赖 | S0-02 |
| 交付文件 | zjt-common-core/ |
| 代码清单 | 1. SwaggerConfig(OpenAPI 3.0配置)<br>2. JWT安全方案配置<br>3. 通用响应模型注册 |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | 访问/swagger-ui.html 可看API文档 |

### S0-07 跨域+日志+工具类

| 项目 | 内容 |
|------|------|
| 任务编号 | S0-07 |
| 任务名称 | 跨域配置+AOP日志+工具类 |
| 前置依赖 | S0-01 |
| 交付文件 | zjt-common-log/, zjt-common-core/utils/ |
| 代码清单 | 1. CorsConfig(跨域配置)<br>2. LogAspect(AOP操作日志)<br>3. RequestIdFilter(请求ID生成)<br>4. DesensitizeUtil(手机号/身份证脱敏) |
| SQL | 无 |
| 测试 | 脱敏工具测试 |
| 验收标准 | 跨域请求正常, 操作日志自动记录 |

### S0-08 前端Vue3工程初始化

| 项目 | 内容 |
|------|------|
| 任务编号 | S0-08 |
| 任务名称 | Vue3+Element Plus管理后台工程初始化 |
| 前置依赖 | 无 |
| 交付文件 | frontend/zjt-admin/ |
| 代码清单 | 1. Vite+Vue3+TypeScript项目初始化<br>2. Element Plus全局注册<br>3. Axios请求封装(拦截器/Token/错误处理/刷新)<br>4. 路由配置+路由守卫(登录拦截)<br>5. Pinia用户状态管理<br>6. .env.development/.env.test/.env.production |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | npm run dev 启动成功, API请求自动带Token |

### S0-09 客户小程序框架初始化

| 项目 | 内容 |
|------|------|
| 任务编号 | S0-09 |
| 任务名称 | 微信小程序(客户端)框架初始化 |
| 前置依赖 | 无 |
| 交付文件 | frontend/zjt-miniapp/customer-app/ |
| 代码清单 | 1. 小程序项目创建(app.js/app.json/app.wxss)<br>2. request.js封装(自动Token/错误处理)<br>3. auth.js(微信登录/静默登录/Token管理)<br>4. env.js(dev/test/prod环境配置)<br>5. 全局样式变量(主题色#07C160)<br>6. 底部TabBar配置(首页/商城/预约/推广/我的) |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | 微信开发者工具打开正常, 请求封装可用 |

### S0-10 技师小程序框架初始化

| 项目 | 内容 |
|------|------|
| 任务编号 | S0-10 |
| 任务名称 | 微信小程序(技师端)框架初始化 |
| 前置依赖 | 无 |
| 交付文件 | frontend/zjt-miniapp/technician-app/ |
| 代码清单 | 1. 小程序项目创建<br>2. request.js/auth.js/env.js(同客户端共享)<br>3. 底部TabBar(工作台/业绩/推广/学习/我的)<br>4. 全局样式变量 |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | 微信开发者工具打开正常, 5个Tab正常切换 |

### S0-11 数据库全量建表

| 项目 | 内容 |
|------|------|
| 任务编号 | S0-11 |
| 任务名称 | 数据库全量表结构创建 |
| 前置依赖 | S0-04 |
| 交付文件 | sql/V1.0.0__init_all_tables.sql |
| 代码清单 | 1. store_info 门店表<br>2. store_room 房间表<br>3. store_technician 技师表<br>4. store_technician_skill 技师技能表<br>5. store_schedule 排班表<br>6. store_technician_promotion 技师推广表<br>7. content_service_item 服务项目表<br>8. trade_appointment 预约表(含version/time_slot/modify_reason/source_type)<br>9. trade_appointment_lock 预约锁档表<br>10. trade_order 订单表<br>11. trade_order_item 订单明细表<br>12. trade_payment 支付记录表<br>13. trade_treatment_card 疗程卡表<br>14. trade_coupon 优惠券表<br>15. user_member 会员表<br>16. user_member_account 会员账户表<br>17. user_employee 员工表<br>18. user_role 角色表<br>19. user_permission 权限表<br>20. billing_tenant 租户表<br>21. billing_subscription 订阅表<br>22. billing_plan 套餐表<br>23. billing_usage 用量计量表<br>24. ai_llm_call_log LLM调用日志表<br>25. ai_daily_report AI日报表<br>26. integration_config 平台对接配置表<br>27. integration_lead 线索表<br>28. integration_promotion_track 推广追踪表<br>29. data_appointment_funnel 预约埋点表<br>30. sys_dict/sys_dict_item 字典表<br>31. sys_config 系统配置表<br>32. sys_audit_log 审计日志表<br>33. sys_feature_flag 功能开关表<br>34. sys_file 文件表<br>35. store_time_slot_config 时段配置表(新增) |
| SQL | 完整CREATE TABLE语句+索引+注释 |
| 测试 | 所有表可正常CREATE, 索引生效 |
| 验收标准 | mysql source V1.0.0__init_all_tables.sql 执行无错误 |

### S0-12 种子数据+字典初始化

| 项目 | 内容 |
|------|------|
| 任务编号 | S0-12 |
| 任务名称 | 种子数据+字典数据初始化 |
| 前置依赖 | S0-11 |
| 交付文件 | sql/V1.0.1__init_seed_data.sql |
| 代码清单 | 1. 默认租户(billing_tenant: 忠济堂测试租户)<br>2. 默认管理员(user_employee: admin)<br>3. 字典数据(sys_dict: 预约状态/订单状态/体质类型/支付方式等)<br>4. 套餐数据(billing_plan: 4档套餐)<br>5. 测试门店(store_info: 2个测试门店)<br>6. 测试技师(store_technician: 5个测试技师) |
| SQL | INSERT语句 |
| 测试 | 查询验证数据完整 |
| 验收标准 | 所有种子数据可正常插入和查询 |

---

## 三、阶段1：基础数据模块（批次1）

> 目标：门店/技师/时段/项目CRUD，后台可配置，小程序可展示

### B1-01 门店管理后端API

| 项目 | 内容 |
|------|------|
| 任务编号 | B1-01 |
| 任务名称 | 门店CRUD后端API |
| 前置依赖 | S0-11 |
| 交付文件 | zjt-store/ |
| 代码清单 | 1. StoreInfoDO 实体类<br>2. StoreInfoMapper Mapper接口<br>3. IStoreInfoService/StoreInfoServiceImpl Service层<br>4. StoreInfoController Controller层<br>5. StoreCreateRequest/StoreUpdateRequest/StoreResponse DTO<br>6. API: POST /api/v1/store/stores, GET /api/v1/store/stores, GET /api/v1/store/stores/{id}, PUT /api/v1/store/stores/{id}, PUT /api/v1/store/stores/{id}/status |
| SQL | 已在S0-11建表 |
| 测试 | StoreInfoServiceImpl单元测试(CRUD+状态变更), StoreInfoController接口测试 |
| 验收标准 | 门店增删改查API全部可用, 多租户隔离生效 |

### B1-02 门店管理前端页面

| 项目 | 内容 |
|------|------|
| 任务编号 | B1-02 |
| 任务名称 | 门店管理Vue3页面 |
| 前置依赖 | B1-01, S0-08 |
| 交付文件 | frontend/zjt-admin/src/pages/store/ |
| 代码清单 | 1. api/store/info.ts API定义<br>2. types/store.ts 类型定义<br>3. pages/store/StoreList.vue 门店列表页<br>4. pages/store/StoreForm.vue 门店表单(新增/编辑)<br>5. composables/useStore.ts |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | 门店列表展示, 新增/编辑/状态变更正常 |

### B1-03 门店小程序展示

| 项目 | 内容 |
|------|------|
| 任务编号 | B1-03 |
| 任务名称 | 客户小程序门店列表展示 |
| 前置依赖 | B1-01, S0-09 |
| 交付文件 | frontend/zjt-miniapp/customer-app/pages/store/ |
| 代码清单 | 1. pages/store/list.js 门店列表页<br>2. pages/store/list.wxml<br>3. pages/store/list.wxss<br>4. api/store.js API调用 |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | 小程序展示门店列表(名称/距离/评分/营业状态) |

### B1-04 技师管理后端API

| 项目 | 内容 |
|------|------|
| 任务编号 | B1-04 |
| 任务名称 | 技师CRUD后端API |
| 前置依赖 | S0-11 |
| 交付文件 | zjt-store/ |
| 代码清单 | 1. StoreTechnicianDO 实体类<br>2. StoreTechnicianMapper<br>3. IStoreTechnicianService/StoreTechnicianServiceImpl<br>4. StoreTechnicianController<br>5. TechnicianCreateRequest/TechnicianUpdateRequest/TechnicianResponse DTO<br>6. API: POST/GET/PUT/DELETE /api/v1/store/technicians, PUT /api/v1/store/technicians/{id}/status, POST /api/v1/store/technicians/{id}/check-in, POST /api/v1/store/technicians/{id}/check-out |
| SQL | 已在S0-11建表 |
| 测试 | 技师CRUD单元测试+接口测试 |
| 验收标准 | 技师增删改查+签到签退API可用 |

### B1-05 技师管理前端页面

| 项目 | 内容 |
|------|------|
| 任务编号 | B1-05 |
| 任务名称 | 技师管理Vue3页面 |
| 前置依赖 | B1-04, S0-08 |
| 交付文件 | frontend/zjt-admin/src/pages/technician/ |
| 代码清单 | 1. api/store/technician.ts<br>2. types/technician.ts<br>3. pages/technician/TechnicianList.vue<br>4. pages/technician/TechnicianForm.vue |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | 技师列表展示, 新增/编辑/状态变更正常 |

### B1-06 技师小程序展示

| 项目 | 内容 |
|------|------|
| 任务编号 | B1-06 |
| 任务名称 | 客户小程序技师列表展示 |
| 前置依赖 | B1-04, S0-09 |
| 交付文件 | frontend/zjt-miniapp/customer-app/pages/technician/ |
| 代码清单 | 1. pages/technician/list.js<br>2. pages/technician/list.wxml/wxss<br>3. api/technician.js |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | 小程序展示技师列表(头像/姓名/等级/评分/擅长), 约满技师灰色 |

### B1-07 时段配置后端API

| 项目 | 内容 |
|------|------|
| 任务编号 | B1-07 |
| 任务名称 | 时段配置后端API(营业时间+时段间隔+休息时段) |
| 前置依赖 | S0-11 |
| 交付文件 | zjt-store/ |
| 代码清单 | 1. StoreTimeSlotConfigDO 实体类<br>2. StoreTimeSlotConfigMapper<br>3. ITimeSlotService/TimeSlotServiceImpl<br>4. TimeSlotController<br>5. API: POST /api/v1/store/time-slot-config, GET /api/v1/store/time-slot-config/{store_id}, PUT /api/v1/store/time-slot-config/{id}, GET /api/v1/store/time-slots/available?store_id=&date= |
| SQL | 新增表store_time_slot_config: id, tenant_id, store_id, business_start_time, business_end_time, slot_duration_minutes, rest_start_time, rest_end_time, 审计字段 |
| 测试 | 时段生成逻辑测试(营业时段+排除休息时段+排除已约时段) |
| 验收标准 | 配置营业时间后可生成时段列表, 可用时段查询正确排除已约时段 |

### B1-08 时段配置前端页面

| 项目 | 内容 |
|------|------|
| 任务编号 | B1-08 |
| 任务名称 | 时段配置Vue3页面 |
| 前置依赖 | B1-07, S0-08 |
| 交付文件 | frontend/zjt-admin/src/pages/timeslot/ |
| 代码清单 | 1. api/store/timeslot.ts<br>2. pages/timeslot/TimeSlotConfig.vue(营业时间+时段间隔+休息时段配置) |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | 可配置门店营业时间和时段间隔 |

### B1-09 服务项目管理后端API

| 项目 | 内容 |
|------|------|
| 任务编号 | B1-09 |
| 任务名称 | 服务项目CRUD后端API |
| 前置依赖 | S0-11 |
| 交付文件 | zjt-content/ |
| 代码清单 | 1. ContentServiceItemDO<br>2. ContentServiceItemMapper<br>3. IServiceItemService/ServiceItemServiceImpl<br>4. ServiceItemController<br>5. API: POST/GET/PUT/DELETE /api/v1/admin/service-items, PUT /api/v1/admin/service-items/{id}/status |
| SQL | 已在S0-11建表 |
| 测试 | 服务项目CRUD单元测试 |
| 验收标准 | 服务项目增删改查+上下架API可用 |

### B1-10 服务项目管理前端页面

| 项目 | 内容 |
|------|------|
| 任务编号 | B1-10 |
| 任务名称 | 服务项目管理Vue3页面 |
| 前置依赖 | B1-09, S0-08 |
| 交付文件 | frontend/zjt-admin/src/pages/service-item/ |
| 代码清单 | 1. api/content/service-item.ts<br>2. pages/service-item/ServiceItemList.vue<br>3. pages/service-item/ServiceItemForm.vue |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | 服务项目列表展示, 新增/编辑/上下架正常 |

**阶段1验收标准**：
- [ ] 后台可增删改查门店/技师/项目/时段
- [ ] 小程序可展示门店列表和技师列表
- [ ] 时段配置生效，可查询可用时段
- [ ] 所有API有单元测试，覆盖率≥70%

---

## 四、阶段2：C端预约模块（批次2）

> 目标：4步预约流程完整可用，锁档防重复，已占时段灰色不可选

### B2-01 可用时段查询API

| 项目 | 内容 |
|------|------|
| 任务编号 | B2-01 |
| 任务名称 | 可用时段查询API(Redis缓存+占用状态) |
| 前置依赖 | B1-07 |
| 交付文件 | zjt-trade/ |
| 代码清单 | 1. AppointmentSlotService 时段查询服务<br>2. API: GET /api/v1/trade/appointments/available-slots?store_id=&technician_id=0&date=<br>3. Redis缓存Key: slot:{tenant_id}:{store_id}:{tech_id}:{date}<br>4. 返回: [{time_slot, available, remaining_count}]<br>5. technician_id=0时查门店所有技师可用时段合并 |
| SQL | 无(查询已有表) |
| 测试 | 时段查询测试(含缓存命中/未命中/已占用场景) |
| 验收标准 | 已占用时段available=false, 可选时段available=true, Redis缓存生效 |

### B2-02 预约锁档API

| 项目 | 内容 |
|------|------|
| 任务编号 | B2-02 |
| 任务名称 | 预约临时锁档API(Redis分布式锁5分钟) |
| 前置依赖 | S0-05, B2-01 |
| 交付文件 | zjt-trade/ |
| 代码清单 | 1. AppointmentLockService 锁档服务<br>2. API: POST /api/v1/trade/appointments/lock-temp<br>3. Redis锁Key: lock:{tenant_id}:appointment:{store_id}:{tech_id}:{date}:{time_slot}<br>4. 锁定5分钟, 返回lock_id+expire_at<br>5. 防重复: 同一客户同一门店同一天最多3个有效预约 |
| SQL | trade_appointment_lock表(已在S0-11建) |
| 测试 | 锁档测试(加锁/释放/超时/并发冲突) |
| 验收标准 | 5分钟超时自动释放, 并发锁定互斥 |

### B2-03 预约创建API

| 项目 | 内容 |
|------|------|
| 任务编号 | B2-03 |
| 任务名称 | 预约创建API(防重复+乐观锁+事件发布) |
| 前置依赖 | B2-02 |
| 交付文件 | zjt-trade/ |
| 代码清单 | 1. TradeAppointmentDO 实体类(含version/time_slot/modify_reason/source_type)<br>2. TradeAppointmentMapper<br>3. IAppointmentService/AppointmentServiceImpl<br>4. AppointmentController<br>5. AppointmentCreateRequest/AppointmentResponse DTO<br>6. API: POST /api/v1/trade/appointments<br>7. 参数: store_id(必填), technician_id(0=不指定,必填), date(必填), time_slot(必填), service_item_id(必填), lock_id(必填)<br>8. 防重复: DB唯一索引uk_lock + 乐观锁version<br>9. 创建成功: 释放锁档+更新Redis时段缓存+发布AppointmentCreatedEvent |
| SQL | 已在S0-11建表 |
| 测试 | 预约创建测试(正常/重复/锁档过期/并发), 100并发零超卖测试 |
| 验收标准 | 预约创建成功, 重复预约拦截, 100并发零超卖 |

### B2-04 预约页面-选门店

| 项目 | 内容 |
|------|------|
| 任务编号 | B2-04 |
| 任务名称 | 小程序预约Step1-选门店 |
| 前置依赖 | B1-03, B2-01 |
| 交付文件 | frontend/zjt-miniapp/customer-app/pages/appointment/ |
| 代码清单 | 1. pages/appointment/step1-store.js/wxml/wxss<br>2. 门店卡片(名称/距离/评分/🟢营业中/🔴已打烊)<br>3. 点击选中高亮, 自动进入Step2 |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | 门店列表展示, 可选中, 营业状态标签正确 |

### B2-05 预约页面-选技师+选日期时间

| 项目 | 内容 |
|------|------|
| 任务编号 | B2-05 |
| 任务名称 | 小程序预约Step2-选技师(含不指定) + Step3-选日期时间 |
| 前置依赖 | B1-06, B2-01 |
| 交付文件 | frontend/zjt-miniapp/customer-app/pages/appointment/ |
| 代码清单 | 1. pages/appointment/step2-technician.js/wxml/wxss<br>2. "不指定技师"选项卡(默认选中✅)<br>3. 技师卡片(头像/姓名/等级/评分/擅长)<br>4. 约满技师灰色+"约满"<br>5. pages/appointment/step3-datetime.js/wxml/wxss<br>6. 日历组件(历史日期灰色禁用)<br>7. 时段网格(可选绿色/已占用灰色🔒+"已约")<br>8. 选中时段后提示"请在5分钟内完成预约" |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | 不指定技师默认选中, 已占用时段灰色不可选, 历史日期禁用 |

### B2-06 预约页面-确认预约

| 项目 | 内容 |
|------|------|
| 任务编号 | B2-06 |
| 任务名称 | 小程序预约Step4-确认预约+跳转我的预约 |
| 前置依赖 | B2-03, B2-05 |
| 交付文件 | frontend/zjt-miniapp/customer-app/pages/appointment/ |
| 代码清单 | 1. pages/appointment/step4-confirm.js/wxml/wxss<br>2. 预约摘要(门店*/技师/日期*/时段*/项目)<br>3. 必填标注红色*号<br>4. 5分钟倒计时显示<br>5. "确认预约"绿色大按钮<br>6. 缺少必填项时按钮灰色<br>7. 确认后: 调用创建API→跳转我的预约→Toast"预约成功" |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | 确认后跳转我的预约, 必填缺失按钮置灰, 5分钟倒计时 |

### B2-07 预约漏斗埋点

| 项目 | 内容 |
|------|------|
| 任务编号 | B2-07 |
| 任务名称 | 预约漏斗埋点(6步) |
| 前置依赖 | B2-04 |
| 交付文件 | zjt-data/ |
| 代码清单 | 1. AppointmentFunnelService 埋点服务<br>2. API: POST /api/v1/data/appointment-funnel/track<br>3. 6步埋点: browse_store/select_tech/select_time/confirm/arrive/complete<br>4. 小程序端埋点上报(每个步骤切换时上报) |
| SQL | data_appointment_funnel表(已在S0-11建) |
| 测试 | 埋点上报测试 |
| 验收标准 | 每步操作自动上报埋点数据 |

### B2-08 预约漏斗统计API

| 项目 | 内容 |
|------|------|
| 任务编号 | B2-08 |
| 任务名称 | 预约漏斗统计API |
| 前置依赖 | B2-07 |
| 交付文件 | zjt-data/ |
| 代码清单 | 1. API: GET /api/v1/data/appointment-funnel/statistics?store_id=&start_date=&end_date=<br>2. 返回: 每步数量+转化率+流失率 |
| SQL | 无(查询已有表) |
| 测试 | 漏斗统计测试 |
| 验收标准 | 漏斗数据正确, 转化率计算准确 |

**阶段2验收标准**：
- [ ] 4步预约流程完整可用
- [ ] 已占用时段灰色不可选
- [ ] 不指定技师可选
- [ ] 5分钟锁档超时自动释放
- [ ] 100并发零超卖
- [ ] 预约漏斗埋点正常上报

---

## 五、阶段3：我的预约（改/取消闭环）（批次3）

### B3-01 我的预约列表API

| 项目 | 内容 |
|------|------|
| 任务编号 | B3-01 |
| 任务名称 | 我的预约列表API(客户视角,分组) |
| 前置依赖 | B2-03 |
| 交付文件 | zjt-trade/ |
| 代码清单 | 1. API: GET /api/v1/trade/appointments/my<br>2. 返回: {pending:[], completed:[], cancelled:[]}<br>3. pending含修改/取消按钮显示条件(modify_count<2且距预约>2h) |
| SQL | 无 |
| 测试 | 我的预约列表测试(分组+按钮显隐) |
| 验收标准 | 预约按状态分组, 修改/取消按钮显隐条件正确 |

### B3-02 我的预约小程序页面

| 项目 | 内容 |
|------|------|
| 任务编号 | B3-02 |
| 任务名称 | 小程序我的预约页面(Tab分组+修改/取消按钮) |
| 前置依赖 | B3-01 |
| 交付文件 | frontend/zjt-miniapp/customer-app/pages/my-appointment/ |
| 代码清单 | 1. pages/my-appointment/list.js/wxml/wxss<br>2. Tab: 待服务/已完成/已取消<br>3. 预约卡片(门店/技师/日期/时段/状态标签)<br>4. 修改按钮: 仅"待服务"且modify_count<2且距预约>2h显示<br>5. 取消按钮: 仅"待服务"且距预约>2h显示 |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | Tab分组正确, 修改/取消按钮显隐条件正确 |

### B3-03 修改预约API

| 项目 | 内容 |
|------|------|
| 任务编号 | B3-03 |
| 任务名称 | 修改预约API(释放旧时段+锁定新时段+version校验) |
| 前置依赖 | B2-03 |
| 交付文件 | zjt-trade/ |
| 代码清单 | 1. API: PUT /api/v1/trade/appointments/{id}/modify<br>2. 参数: date/time_slot/technician_id/modify_reason(必填)<br>3. 流程: 校验可修改→锁定新时段→释放旧时段→更新预约(version+1)→更新Redis缓存→发布AppointmentModifiedEvent |
| SQL | 无 |
| 测试 | 修改预约测试(正常/超2次/已过期/并发修改) |
| 验收标准 | 修改后原时段释放, 新时段锁定, version+1 |

### B3-04 修改预约小程序弹窗

| 项目 | 内容 |
|------|------|
| 任务编号 | B3-04 |
| 任务名称 | 小程序修改预约弹窗 |
| 前置依赖 | B3-03 |
| 交付文件 | frontend/zjt-miniapp/customer-app/components/ |
| 代码清单 | 1. components/modify-appointment/modify.js/wxml/wxss<br>2. 选新技师+新日期+新时段<br>3. 原时段释放提示<br>4. 二次确认弹窗 |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | 修改弹窗正常, 原时段释放, 新时段锁定 |

### B3-05 取消预约API+弹窗

| 项目 | 内容 |
|------|------|
| 任务编号 | B3-05 |
| 任务名称 | 取消预约API+小程序弹窗 |
| 前置依赖 | B2-03 |
| 交付文件 | zjt-trade/ + frontend/zjt-miniapp/customer-app/components/ |
| 代码清单 | 后端:<br>1. API: PUT /api/v1/trade/appointments/{id}/cancel<br>2. 参数: cancel_reason(必填)<br>3. 流程: 校验可取消→填原因→释放时段→更新状态→发布AppointmentCancelledEvent<br><br>小程序:<br>1. components/cancel-appointment/cancel.js/wxml/wxss<br>2. 取消原因单选(计划变更/时间冲突/身体不适/其他)+其他输入框<br>3. 二次确认弹窗 |
| SQL | 无 |
| 测试 | 取消预约测试(正常/2小时内/已过期) |
| 验收标准 | 取消后时段释放, 原因必填, 2小时内不可取消 |

### B3-06 过期预约自动处理

| 项目 | 内容 |
|------|------|
| 任务编号 | B3-06 |
| 任务名称 | 过期预约定时任务(每日凌晨自动过期) |
| 前置依赖 | B2-03 |
| 交付文件 | zjt-job/ |
| 代码清单 | 1. AppointmentExpireJob 定时任务(@Scheduled cron="0 5 0 * * ?")<br>2. 每日00:05扫描: 预约时间已过且状态为"待服务"→标记"已过期"<br>3. 释放对应时段Redis缓存 |
| SQL | 无 |
| 测试 | 定时任务测试(模拟过期数据) |
| 验收标准 | 过期预约自动标记, 时段自动释放 |

**阶段3验收标准**：
- [ ] 我的预约Tab分组显示
- [ ] 修改按钮仅"待服务"且修改<2次且距预约>2h显示
- [ ] 取消按钮仅距预约>2h显示
- [ ] 修改后原时段释放
- [ ] 取消后时段释放
- [ ] 过期预约每日自动标记

---

## 六、阶段4：技师端导航+推广模块（批次4）

### B4-01 技师端底部导航重构

| 项目 | 内容 |
|------|------|
| 任务编号 | B4-01 |
| 任务名称 | 技师小程序底部5Tab导航(工作台/业绩/推广/学习/我的) |
| 前置依赖 | S0-10 |
| 交付文件 | frontend/zjt-miniapp/technician-app/ |
| 代码清单 | 1. app.json tabBar配置(5个tab)<br>2. pages/promotion/index.js/wxml/wxss 推广页面占位 |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | 5个Tab正常切换 |

### B4-02 推广后端API

| 项目 | 内容 |
|------|------|
| 任务编号 | B4-02 |
| 任务名称 | 技师推广后端API(推广码/海报/统计/佣金/排行) |
| 前置依赖 | S0-11 |
| 交付文件 | zjt-store/ |
| 代码清单 | 1. TechnicianPromotionService<br>2. API: GET /api/v1/store/technicians/{id}/promotion<br>3. API: POST /api/v1/store/technicians/{id}/promotion/generate-poster<br>4. API: GET /api/v1/store/technicians/{id}/promotion/stats<br>5. API: GET /api/v1/store/technicians/{id}/promotion/commissions<br>6. API: GET /api/v1/store/technicians/{id}/promotion/ranking<br>7. 推广码生成: ZJT+门店后2位+技师后2位+2位校验 |
| SQL | store_technician_promotion表(已在S0-11建) |
| 测试 | 推广码生成/统计/佣金计算测试 |
| 验收标准 | 推广码生成正确, 统计数据准确, 佣金计算正确 |

### B4-03 推广海报生成

| 项目 | 内容 |
|------|------|
| 任务编号 | B4-03 |
| 任务名称 | 小程序推广海报(Canvas绘制+分享) |
| 前置依赖 | B4-02 |
| 交付文件 | frontend/zjt-miniapp/technician-app/pages/promotion/ |
| 代码清单 | 1. pages/promotion/poster.js/wxml/wxss<br>2. Canvas绘制海报(品牌Logo+邀请码+二维码+技师信息)<br>3. 保存到相册/分享到微信/分享到朋友圈 |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | 海报可生成, 可保存/分享 |

### B4-04 推广数据+佣金页面

| 项目 | 内容 |
|------|------|
| 任务编号 | B4-04 |
| 任务名称 | 小程序推广数据卡片+佣金明细+排行榜 |
| 前置依赖 | B4-02 |
| 交付文件 | frontend/zjt-miniapp/technician-app/pages/promotion/ |
| 代码清单 | 1. pages/promotion/stats.js/wxml/wxss 推广数据(点击/注册/到店/转化率)<br>2. pages/promotion/commission.js/wxml/wxss 佣金(已结算🟢/待结算🟠)<br>3. pages/promotion/ranking.js/wxml/wxss 排行榜(排名+变化箭头) |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | 推广数据展示正确, 佣金状态区分, 排行榜有变化箭头 |

### B4-05 推广追踪归因API

| 项目 | 内容 |
|------|------|
| 任务编号 | B4-05 |
| 任务名称 | 推广追踪归因API(30天窗口+唯一归因) |
| 前置依赖 | B4-02 |
| 交付文件 | zjt-integration/ |
| 代码清单 | 1. PromotionTrackService<br>2. API: POST /api/v1/integration/promotion/track<br>3. 事件类型: click/register/first_visit/first_order/repurchase<br>4. 归因规则: 30天窗口, 首次点击归因<br>5. API: GET /api/v1/integration/promotion/funnel |
| SQL | integration_promotion_track表(已在S0-11建) |
| 测试 | 追踪归因测试(30天窗口/唯一归因/漏斗统计) |
| 验收标准 | 推广链接点击可追踪, 30天内注册归因到技师 |

### B4-06 佣金计算定时任务

| 项目 | 内容 |
|------|------|
| 任务编号 | B4-06 |
| 任务名称 | 佣金计算定时任务(T+7结算) |
| 前置依赖 | B4-05 |
| 交付文件 | zjt-job/ |
| 代码清单 | 1. CommissionSettleJob 定时任务(每日凌晨)<br>2. 扫描7天前已完成订单→计算佣金→更新状态<br>3. 佣金规则: 首单10%, 复购5%, 单笔上限200元 |
| SQL | 无 |
| 测试 | 佣金计算测试(首单/复购/上限) |
| 验收标准 | 佣金计算正确, T+7自动结算 |

### B4-07 技师工作台+业绩页面

| 项目 | 内容 |
|------|------|
| 任务编号 | B4-07 |
| 任务名称 | 技师小程序工作台+业绩页面 |
| 前置依赖 | B1-04, S0-10 |
| 交付文件 | frontend/zjt-miniapp/technician-app/pages/ |
| 代码清单 | 1. pages/workspace/index.js/wxml/wxss 工作台<br>2. pages/performance/index.js/wxml/wxss 业绩<br>3. 工作台: 技师信息/在岗状态/进行中服务/待服务列表<br>4. 业绩: 本月营收/目标进度/服务单数/客单价/评分/排名 |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | 工作台展示待服务列表, 业绩数据正确 |

### B4-08 技师端后端API(工作台+业绩)

| 项目 | 内容 |
|------|------|
| 任务编号 | B4-08 |
| 任务名称 | 技师端工作台+业绩后端API |
| 前置依赖 | B1-04 |
| 交付文件 | zjt-store/ |
| 代码清单 | 1. API: GET /api/v1/store/technicians/{id}/workspace<br>2. API: GET /api/v1/store/technicians/{id}/performance<br>3. API: POST /api/v1/store/technicians/{id}/check-in<br>4. API: POST /api/v1/store/technicians/{id}/check-out |
| SQL | 无 |
| 测试 | API测试 |
| 验收标准 | 工作台/业绩API数据正确 |

**阶段4验收标准**：
- [ ] 底部5个Tab正常切换
- [ ] 海报可生成并分享
- [ ] 推广链接点击可追踪归因
- [ ] 佣金计算正确(T+7)
- [ ] 工作台和业绩数据正确

---

## 七、阶段5：后台管理+数据统计（批次5）

### B5-01 后台门店管理页面(完善)

| 项目 | 内容 |
|------|------|
| 任务编号 | B5-01 |
| 任务名称 | 后台门店管理页面完善(含状态管理+搜索) |
| 前置依赖 | B1-02 |
| 交付文件 | frontend/zjt-admin/src/pages/store/ |
| 代码清单 | 1. 门店列表(搜索/筛选/分页)<br>2. 状态标签(营业中🟢/休息中🟡/已关闭🔴)<br>3. 状态变更确认弹窗 |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | 门店列表搜索/筛选/状态变更正常 |

### B5-02 后台技师管理页面(完善)

| 项目 | 内容 |
|------|------|
| 任务编号 | B5-02 |
| 任务名称 | 后台技师管理页面完善(含排班+技能) |
| 前置依赖 | B1-05 |
| 交付文件 | frontend/zjt-admin/src/pages/technician/ |
| 代码清单 | 1. 技师列表(搜索/筛选/分页)<br>2. 排班管理(周视图)<br>3. 技能标签管理 |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | 技师管理+排班+技能正常 |

### B5-03 后台时段配置页面(完善)

| 项目 | 内容 |
|------|------|
| 任务编号 | B5-03 |
| 任务名称 | 后台时段配置页面完善 |
| 前置依赖 | B1-08 |
| 交付文件 | frontend/zjt-admin/src/pages/timeslot/ |
| 代码清单 | 1. 时段配置表单(营业时间+时段间隔+休息时段)<br>2. 时段预览(日历视图) |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | 时段配置保存后预览正确 |

### B5-04 预约漏斗看板

| 项目 | 内容 |
|------|------|
| 任务编号 | B5-04 |
| 任务名称 | 后台预约漏斗看板(6步漏斗+转化率) |
| 前置依赖 | B2-08 |
| 交付文件 | frontend/zjt-admin/src/pages/dashboard/ |
| 代码清单 | 1. api/data/appointment-funnel.ts<br>2. pages/dashboard/AppointmentFunnel.vue<br>3. 漏斗图(浏览→选技师→选时间→确认→到店→完成)<br>4. 每步数量+转化率+流失率 |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | 漏斗图数据正确, 转化率计算准确 |

### B5-05 推广漏斗看板

| 项目 | 内容 |
|------|------|
| 任务编号 | B5-05 |
| 任务名称 | 后台推广漏斗看板(6步漏斗+ROI) |
| 前置依赖 | B4-05 |
| 交付文件 | frontend/zjt-admin/src/pages/dashboard/ |
| 代码清单 | 1. api/data/promotion-funnel.ts<br>2. pages/dashboard/PromotionFunnel.vue<br>3. 漏斗图(分享→点击→注册→到店→首单→复购)<br>4. ROI计算(佣金支出/新增营收) |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | 推广漏斗数据正确, ROI计算准确 |

### B5-06 门店/技师排行

| 项目 | 内容 |
|------|------|
| 任务编号 | B5-06 |
| 任务名称 | 后台门店/技师排行榜(多维度) |
| 前置依赖 | B1-01, B1-04 |
| 交付文件 | frontend/zjt-admin/src/pages/ranking/ |
| 代码清单 | 1. api/data/ranking.ts<br>2. pages/ranking/StoreRanking.vue(营收/客流/评分)<br>3. pages/ranking/TechnicianRanking.vue(业绩/服务数/评分/推广) |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | 排行榜数据正确, 多维度切换 |

### B5-07 系统配置管理

| 项目 | 内容 |
|------|------|
| 任务编号 | B5-07 |
| 任务名称 | 后台系统配置管理(字典/功能开关) |
| 前置依赖 | S0-11 |
| 交付文件 | zjt-common/ + frontend/zjt-admin/src/pages/system/ |
| 代码清单 | 后端:<br>1. SysDictController 字典CRUD<br>2. SysConfigController 配置CRUD<br>3. SysFeatureFlagController 功能开关CRUD<br><br>前端:<br>1. pages/system/DictList.vue<br>2. pages/system/ConfigList.vue<br>3. pages/system/FeatureFlagList.vue |
| SQL | 已在S0-11建表 |
| 测试 | 字典/配置CRUD测试 |
| 验收标准 | 字典/配置/功能开关可增删改查 |

### B5-08 预约管理页面(后台查看)

| 项目 | 内容 |
|------|------|
| 任务编号 | B5-08 |
| 任务名称 | 后台预约管理页面(查看/筛选/导出) |
| 前置依赖 | B2-03 |
| 交付文件 | frontend/zjt-admin/src/pages/appointment/ |
| 代码清单 | 1. api/trade/appointment.ts<br>2. pages/appointment/AppointmentList.vue<br>3. 筛选(门店/技师/日期/状态)<br>4. 预约详情弹窗<br>5. 导出CSV |
| SQL | 无 |
| 测试 | 无 |
| 验收标准 | 预约列表可筛选查看, 可导出 |

**阶段5验收标准**：
- [ ] 后台所有配置页面可用
- [ ] 漏斗数据实时展示
- [ ] 排行榜数据正确
- [ ] 系统配置可管理

---

## 八、阶段6-9概要（后续批次详细任务在验收前补充）

### 阶段6：AI能力（批次6）- 35人天

| 编号 | 任务 | 人天 |
|------|------|------|
| B6-01 | AI网关服务(LLM路由+限流+熔断+计费) | 8 |
| B6-02 | 模型接入(DeepSeek/Qwen) | 5 |
| B6-03 | AI问诊服务(对话+体质辨识) | 8 |
| B6-04 | AI话术服务(咨询/升单/回访) | 5 |
| B6-05 | AI日报服务(数据聚合+日报生成) | 5 |
| B6-06 | AI客服服务(24h应答) | 2 |
| B6-07 | 知识库RAG | 2 |

### 阶段7：平台对接（批次7）- 40人天

| 编号 | 任务 | 人天 |
|------|------|------|
| B7-01 | 对接框架(统一适配器) | 8 |
| B7-02 | 企业微信适配器 | 10 |
| B7-03 | 抖音适配器 | 10 |
| B7-04 | 美团适配器 | 8 |
| B7-05 | 线索管理(清洗/分类/分配) | 4 |

### 阶段8：SaaS多租户+计费（批次8）- 30人天

| 编号 | 任务 | 人天 |
|------|------|------|
| B8-01 | 租户管理API+页面 | 6 |
| B8-02 | 套餐订阅API+页面 | 5 |
| B8-03 | 用量计量API+页面 | 5 |
| B8-04 | 账单管理API+页面 | 5 |
| B8-05 | 白标配置 | 4 |
| B8-06 | 租户数据隔离验证 | 5 |

### 阶段9：全量测试+灰度上线 - 20人天

| 编号 | 任务 | 人天 |
|------|------|------|
| B9-01 | 全量单元测试补齐 | 5 |
| B9-02 | E2E自动化测试 | 5 |
| B9-03 | 性能压测(100并发) | 3 |
| B9-04 | 安全测试 | 3 |
| B9-05 | 灰度发布+监控 | 4 |

---

## 九、AI开发调度规则

### 9.1 调度流程

```
1. 按阶段顺序，一次只下发一个任务(如B2-01)
2. AI读取对应文档(需求+架构+数据库+API+编码规范)
3. AI按交付格式生成代码(需求说明→文件清单→完整代码→SQL→测试→自测报告)
4. 人工验收(运行+测试+检查)
5. 验收通过 → 下发下一个任务
6. 验收不通过 → AI返修，直到通过
```

### 9.2 验收检查表

| 检查项 | 标准 |
|--------|------|
| 代码可运行 | 无编译错误，启动成功 |
| 功能完整 | 任务描述的所有需求点覆盖 |
| 单元测试 | 覆盖率≥70% |
| 代码规范 | 符合08_编码规范 |
| SQL正确 | 可执行，有回滚SQL |
| 自测报告 | 所有项✅ |

### 9.3 禁止事项

| 禁止 | 说明 |
|------|------|
| 禁止跨任务开发 | 一次只做一个任务 |
| 禁止私自新增需求 | 只按文档开发 |
| 禁止猜测 | 文档没写的不做，有疑问先确认 |
| 禁止跳过测试 | 每个任务必须有单元测试 |

> **文档版本：V3.4**
> **编制日期：2026年6月**
> **审核状态：定稿**
