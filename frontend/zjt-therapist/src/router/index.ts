import { createRouter, createWebHistory } from 'vue-router'
import { isAuthenticated } from '../utils/auth'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/login/index.vue') },
  { path: '/', redirect: '/workspace' },
  { path: '/workspace', name: 'Workspace', component: () => import('../views/workspace/index.vue'), meta: { requiresAuth: true } },
  { path: '/performance', name: 'Performance', component: () => import('../views/performance/index.vue'), meta: { requiresAuth: true } },
  { path: '/promotion', name: 'Promotion', component: () => import('../views/promotion/index.vue'), meta: { requiresAuth: true } },
  { path: '/learning', name: 'Learning', component: () => import('../views/learning/index.vue'), meta: { requiresAuth: true } },
  { path: '/my', name: 'My', component: () => import('../views/my/index.vue'), meta: { requiresAuth: true } },
  { path: '/my/schedule', name: 'MySchedule', component: () => import('../views/my/schedule.vue'), meta: { requiresAuth: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  if (to.meta.requiresAuth && !isAuthenticated()) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router
