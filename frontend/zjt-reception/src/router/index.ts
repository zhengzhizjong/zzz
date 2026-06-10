import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/index.vue'),
      meta: { title: '登录', public: true }
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
          meta: { title: '今日看板' }
        },
        {
          path: 'appointment',
          name: 'Appointment',
          component: () => import('@/views/appointment/index.vue'),
          meta: { title: '预约管理' }
        },
        {
          path: 'cashier',
          name: 'Cashier',
          component: () => import('@/views/cashier/index.vue'),
          meta: { title: '收银台' }
        },
        {
          path: 'technician',
          name: 'Technician',
          component: () => import('@/views/technician/index.vue'),
          meta: { title: '技师状态' }
        },
        {
          path: 'member',
          name: 'Member',
          component: () => import('@/views/member/index.vue'),
          meta: { title: '会员查询' }
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/dashboard'
    }
  ]
})

router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title || '忠济堂'} - 前台管理`
  if (to.meta.public) {
    next()
    return
  }
  const token = getToken()
  if (!token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  next()
})

export default router
