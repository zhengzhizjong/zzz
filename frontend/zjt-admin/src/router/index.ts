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
    name: 'Home',
    component: () => import('@/views/home/index.vue'),
    meta: { title: '首页', requiresAuth: true }
  },
  {
    path: '/store',
    name: 'Store',
    meta: { title: '门店管理', requiresAuth: true },
    children: [
      {
        path: 'list',
        name: 'StoreList',
        component: () => import('@/views/store/StoreList.vue'),
        meta: { title: '门店列表', requiresAuth: true }
      }
    ]
  },
  {
    path: '/technician',
    name: 'Technician',
    meta: { title: '技师管理', requiresAuth: true },
    children: [
      {
        path: 'list',
        name: 'TechnicianList',
        component: () => import('@/views/technician/TechnicianList.vue'),
        meta: { title: '技师列表', requiresAuth: true }
      }
    ]
  },
  {
    path: '/timeslot',
    name: 'TimeSlot',
    meta: { title: '时段配置', requiresAuth: true },
    children: [
      {
        path: 'config',
        name: 'TimeSlotConfig',
        component: () => import('@/views/timeslot/TimeSlotConfig.vue'),
        meta: { title: '时段配置', requiresAuth: true }
      }
    ]
  },
  {
    path: '/service-item',
    name: 'ServiceItem',
    meta: { title: '服务项目管理', requiresAuth: true },
    children: [
      {
        path: 'list',
        name: 'ServiceItemList',
        component: () => import('@/views/service-item/ServiceItemList.vue'),
        meta: { title: '服务项目列表', requiresAuth: true }
      }
    ]
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    meta: { title: '数据看板', requiresAuth: true },
    children: [
      {
        path: 'appointment-funnel',
        name: 'AppointmentFunnel',
        component: () => import('@/views/dashboard/AppointmentFunnel.vue'),
        meta: { title: '预约漏斗', requiresAuth: true }
      },
      {
        path: 'promotion-funnel',
        name: 'PromotionFunnel',
        component: () => import('@/views/dashboard/PromotionFunnel.vue'),
        meta: { title: '推广漏斗', requiresAuth: true }
      }
    ]
  },
  {
    path: '/ranking',
    name: 'Ranking',
    meta: { title: '排行榜', requiresAuth: true },
    children: [
      {
        path: 'store',
        name: 'StoreRanking',
        component: () => import('@/views/ranking/StoreRanking.vue'),
        meta: { title: '门店排行', requiresAuth: true }
      },
      {
        path: 'technician',
        name: 'TechnicianRanking',
        component: () => import('@/views/ranking/TechnicianRanking.vue'),
        meta: { title: '技师排行', requiresAuth: true }
      }
    ]
  },
  {
    path: '/system',
    name: 'System',
    meta: { title: '系统管理', requiresAuth: true },
    children: [
      {
        path: 'dict',
        name: 'DictList',
        component: () => import('@/views/system/DictList.vue'),
        meta: { title: '字典管理', requiresAuth: true }
      },
      {
        path: 'config',
        name: 'ConfigList',
        component: () => import('@/views/system/ConfigList.vue'),
        meta: { title: '配置管理', requiresAuth: true }
      },
      {
        path: 'feature-flag',
        name: 'FeatureFlagList',
        component: () => import('@/views/system/FeatureFlagList.vue'),
        meta: { title: '功能开关', requiresAuth: true }
      }
    ]
  },
  {
    path: '/appointment',
    name: 'Appointment',
    meta: { title: '预约管理', requiresAuth: true },
    children: [
      {
        path: 'list',
        name: 'AppointmentList',
        component: () => import('@/views/appointment/AppointmentList.vue'),
        meta: { title: '预约列表', requiresAuth: true }
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
