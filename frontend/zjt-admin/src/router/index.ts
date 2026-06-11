import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/layout/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页', requiresAuth: true }
      },
      {
        path: 'store/list',
        name: 'StoreList',
        component: () => import('@/views/store/StoreList.vue'),
        meta: { title: '门店列表', requiresAuth: true }
      },
      {
        path: 'store/rooms',
        name: 'RoomList',
        component: () => import('@/views/store/RoomList.vue'),
        meta: { title: '房间管理', requiresAuth: true }
      },
      {
        path: 'store/schedules',
        name: 'ScheduleList',
        component: () => import('@/views/store/ScheduleList.vue'),
        meta: { title: '排班管理', requiresAuth: true }
      },
      {
        path: 'technician/list',
        name: 'TechnicianList',
        component: () => import('@/views/technician/TechnicianList.vue'),
        meta: { title: '技师列表', requiresAuth: true }
      },
      {
        path: 'timeslot/config',
        name: 'TimeSlotConfig',
        component: () => import('@/views/timeslot/TimeSlotConfig.vue'),
        meta: { title: '时段配置', requiresAuth: true }
      },
      {
        path: 'service-item/list',
        name: 'ServiceItemList',
        component: () => import('@/views/service-item/ServiceItemList.vue'),
        meta: { title: '服务项目列表', requiresAuth: true }
      },
      {
        path: 'dashboard/appointment-funnel',
        name: 'AppointmentFunnel',
        component: () => import('@/views/dashboard/AppointmentFunnel.vue'),
        meta: { title: '预约漏斗', requiresAuth: true }
      },
      {
        path: 'dashboard/promotion-funnel',
        name: 'PromotionFunnel',
        component: () => import('@/views/dashboard/PromotionFunnel.vue'),
        meta: { title: '推广漏斗', requiresAuth: true }
      },
      {
        path: 'ranking/store',
        name: 'StoreRanking',
        component: () => import('@/views/ranking/StoreRanking.vue'),
        meta: { title: '门店排行', requiresAuth: true }
      },
      {
        path: 'ranking/technician',
        name: 'TechnicianRanking',
        component: () => import('@/views/ranking/TechnicianRanking.vue'),
        meta: { title: '技师排行', requiresAuth: true }
      },
      {
        path: 'system/dict',
        name: 'DictList',
        component: () => import('@/views/system/DictList.vue'),
        meta: { title: '字典管理', requiresAuth: true }
      },
      {
        path: 'system/config',
        name: 'ConfigList',
        component: () => import('@/views/system/ConfigList.vue'),
        meta: { title: '配置管理', requiresAuth: true }
      },
      {
        path: 'system/feature-flag',
        name: 'FeatureFlagList',
        component: () => import('@/views/system/FeatureFlagList.vue'),
        meta: { title: '功能开关', requiresAuth: true }
      },
      {
        path: 'system/audit-logs',
        name: 'AuditLogList',
        component: () => import('@/views/system/AuditLogList.vue'),
        meta: { title: '审计日志', requiresAuth: true }
      },
      {
        path: 'system/sms-logs',
        name: 'SmsLogList',
        component: () => import('@/views/system/SmsLogList.vue'),
        meta: { title: '短信日志', requiresAuth: true }
      },
      {
        path: 'system/files',
        name: 'FileList',
        component: () => import('@/views/system/FileList.vue'),
        meta: { title: '文件管理', requiresAuth: true }
      },
      {
        path: 'appointment/list',
        name: 'AppointmentList',
        component: () => import('@/views/appointment/AppointmentList.vue'),
        meta: { title: '预约列表', requiresAuth: true }
      },
      {
        path: 'billing/tenants',
        name: 'TenantList',
        component: () => import('@/views/billing/TenantList.vue'),
        meta: { title: '租户管理', requiresAuth: true }
      },
      {
        path: 'billing/plans',
        name: 'PlanList',
        component: () => import('@/views/billing/PlanList.vue'),
        meta: { title: '套餐管理', requiresAuth: true }
      },
      {
        path: 'billing/subscriptions',
        name: 'SubscriptionList',
        component: () => import('@/views/billing/SubscriptionList.vue'),
        meta: { title: '订阅管理', requiresAuth: true }
      },
      {
        path: 'billing/usage',
        name: 'UsageList',
        component: () => import('@/views/billing/UsageList.vue'),
        meta: { title: '用量统计', requiresAuth: true }
      },
      {
        path: 'integration/leads',
        name: 'LeadList',
        component: () => import('@/views/integration/LeadList.vue'),
        meta: { title: '线索管理', requiresAuth: true }
      },
      {
        path: 'integration/configs',
        name: 'IntegrationConfigList',
        component: () => import('@/views/integration/ConfigList.vue'),
        meta: { title: '平台对接配置', requiresAuth: true }
      },
      {
        path: 'ai/gateway',
        name: 'GatewayList',
        component: () => import('@/views/ai/GatewayList.vue'),
        meta: { title: 'AI网关', requiresAuth: true }
      },
      {
        path: 'ai/daily-report',
        name: 'DailyReport',
        component: () => import('@/views/ai/DailyReport.vue'),
        meta: { title: 'AI日报', requiresAuth: true }
      },
      {
        path: 'user/employees',
        name: 'EmployeeList',
        component: () => import('@/views/user/EmployeeList.vue'),
        meta: { title: '员工管理', requiresAuth: true }
      },
      {
        path: 'user/roles',
        name: 'RoleList',
        component: () => import('@/views/user/RoleList.vue'),
        meta: { title: '角色权限', requiresAuth: true }
      },
      {
        path: 'user/members',
        name: 'MemberList',
        component: () => import('@/views/user/MemberList.vue'),
        meta: { title: '会员管理', requiresAuth: true }
      },
      {
        path: 'trade/orders',
        name: 'OrderList',
        component: () => import('@/views/trade/OrderList.vue'),
        meta: { title: '订单管理', requiresAuth: true }
      },
      {
        path: 'trade/refunds',
        name: 'RefundList',
        component: () => import('@/views/trade/RefundList.vue'),
        meta: { title: '退款管理', requiresAuth: true }
      },
      {
        path: 'trade/coupons',
        name: 'CouponList',
        component: () => import('@/views/trade/CouponList.vue'),
        meta: { title: '优惠券管理', requiresAuth: true }
      },
      {
        path: 'trade/treatment-cards',
        name: 'TreatmentCardList',
        component: () => import('@/views/trade/TreatmentCardList.vue'),
        meta: { title: '疗程卡管理', requiresAuth: true }
      },
      {
        path: 'trade/activities',
        name: 'ActivityList',
        component: () => import('@/views/trade/ActivityList.vue'),
        meta: { title: '活动管理', requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title || '忠济堂'} - 管理后台`
  const token = getToken()
  if (to.meta.requiresAuth !== false && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.path === '/login' && token) {
    next({ path: '/' })
  } else {
    next()
  }
})

export default router
