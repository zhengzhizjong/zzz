import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/index.vue'),
      meta: { title: '登录' }
    },
    {
      path: '/',
      component: () => import('@/views/layout/index.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/index.vue'),
          meta: { title: '门店概览' }
        },
        {
          path: 'appointment',
          name: 'Appointment',
          component: () => import('@/views/appointment/index.vue'),
          meta: { title: '预约管理' }
        },
        {
          path: 'technician',
          name: 'Technician',
          component: () => import('@/views/technician/index.vue'),
          meta: { title: '技师管理' }
        },
        {
          path: 'schedule',
          name: 'Schedule',
          component: () => import('@/views/schedule/index.vue'),
          meta: { title: '排班管理' }
        },
        {
          path: 'room',
          name: 'Room',
          component: () => import('@/views/room/index.vue'),
          meta: { title: '房间管理' }
        },
        {
          path: 'order',
          name: 'Order',
          component: () => import('@/views/order/index.vue'),
          meta: { title: '订单管理' }
        },
        {
          path: 'member',
          name: 'Member',
          component: () => import('@/views/member/index.vue'),
          meta: { title: '会员管理' }
        },
        {
          path: 'report',
          name: 'Report',
          component: () => import('@/views/report/index.vue'),
          meta: { title: '数据报表' }
        },
        {
          path: 'settings',
          name: 'Settings',
          component: () => import('@/views/settings/index.vue'),
          meta: { title: '门店设置' }
        }
      ]
    }
  ]
})

router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title || '忠济堂'} - 店长管理`
  const userStore = useUserStore()
  if (to.path !== '/login' && !userStore.token) {
    next('/login')
  } else if (to.path === '/login' && userStore.token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
