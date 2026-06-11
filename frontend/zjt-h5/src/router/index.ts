import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/home/index.vue'),
    meta: { title: '首页', showTabBar: true }
  },
  {
    path: '/store/list',
    name: 'StoreList',
    component: () => import('@/views/store/list.vue'),
    meta: { title: '门店列表' }
  },
  {
    path: '/store/detail/:id',
    name: 'StoreDetail',
    component: () => import('@/views/store/detail.vue'),
    meta: { title: '门店详情' }
  },
  {
    path: '/technician/list',
    name: 'TechnicianList',
    component: () => import('@/views/technician/list.vue'),
    meta: { title: '技师列表' }
  },
  {
    path: '/technician/detail/:id',
    name: 'TechnicianDetail',
    component: () => import('@/views/technician/detail.vue'),
    meta: { title: '技师详情' }
  },
  {
    path: '/appointment/step1',
    name: 'AppointmentStep1',
    component: () => import('@/views/appointment/Step1Store.vue'),
    meta: { title: '选择门店', showTabBar: true }
  },
  {
    path: '/appointment/step2',
    name: 'AppointmentStep2',
    component: () => import('@/views/appointment/Step2Technician.vue'),
    meta: { title: '选择技师' }
  },
  {
    path: '/appointment/step3',
    name: 'AppointmentStep3',
    component: () => import('@/views/appointment/Step3Datetime.vue'),
    meta: { title: '选择时间' }
  },
  {
    path: '/appointment/step4',
    name: 'AppointmentStep4',
    component: () => import('@/views/appointment/Step4Confirm.vue'),
    meta: { title: '确认预约' }
  },
  {
    path: '/my-appointment/list',
    name: 'MyAppointmentList',
    component: () => import('@/views/my-appointment/list.vue'),
    meta: { title: '我的预约' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/payment/:id',
    name: 'Payment',
    component: () => import('@/views/payment/index.vue'),
    meta: { title: '支付', requireAuth: true }
  },
  {
    path: '/my',
    name: 'My',
    component: () => import('@/views/my/index.vue'),
    meta: { title: '个人中心', showTabBar: true }
  },
  {
    path: '/profile/edit',
    name: 'ProfileEdit',
    component: () => import('@/views/profile/edit.vue'),
    meta: { title: '资料编辑', requireAuth: true }
  },
  {
    path: '/health/profile',
    name: 'HealthProfile',
    component: () => import('@/views/health/profile.vue'),
    meta: { title: '健康档案', requireAuth: true }
  },
  {
    path: '/coupon/list',
    name: 'CouponList',
    component: () => import('@/views/coupon/list.vue'),
    meta: { title: '优惠券', requireAuth: true }
  },
  {
    path: '/treatment/list',
    name: 'TreatmentList',
    component: () => import('@/views/treatment/list.vue'),
    meta: { title: '疗程卡', requireAuth: true }
  },
  {
    path: '/service/list',
    name: 'ServiceList',
    component: () => import('@/views/service/list.vue'),
    meta: { title: '服务项目' }
  },
  {
    path: '/order/list',
    name: 'OrderList',
    component: () => import('@/views/order/list.vue'),
    meta: { title: '我的订单', requireAuth: true }
  },
  {
    path: '/mall',
    name: 'Mall',
    component: () => import('@/views/mall/index.vue'),
    meta: { title: '商场', showTabBar: true }
  },
  {
    path: '/promotion',
    name: 'Promotion',
    component: () => import('@/views/promotion/index.vue'),
    meta: { title: '推广中心', showTabBar: true }
  },
  {
    path: '/ai/consultation',
    name: 'AiConsultation',
    component: () => import('@/views/ai/consultation.vue'),
    meta: { title: 'AI智能问诊' }
  },
  {
    path: '/notification/list',
    name: 'NotificationList',
    component: () => import('@/views/notification/list.vue'),
    meta: { title: '通知' }
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('@/views/search/index.vue'),
    meta: { title: '搜索' }
  },
  {
    path: '/service/customer-service',
    name: 'CustomerService',
    component: () => import('@/views/service/customer-service.vue'),
    meta: { title: '在线客服' }
  },
  {
    path: '/knowledge',
    name: 'Knowledge',
    component: () => import('@/views/knowledge/index.vue'),
    meta: { title: '养生知识' }
  },
  {
    path: '/points/mall',
    name: 'PointsMall',
    component: () => import('@/views/points/mall.vue'),
    meta: { title: '积分商城' }
  },
  {
    path: '/member/level',
    name: 'MemberLevel',
    component: () => import('@/views/member/level.vue'),
    meta: { title: '会员等级' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title || '忠济堂'} - 忠济堂`

  // 需要登录的页面
  if (to.meta.requireAuth && !getToken()) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router
