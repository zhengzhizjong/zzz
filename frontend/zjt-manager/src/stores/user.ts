import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<Record<string, any>>({})
  const storeId = ref(localStorage.getItem('storeId') || '')

  async function login(username: string, password: string) {
    const res: any = await request.post('/auth/login', { username, password, role: 'manager' })
    token.value = res.data.token
    userInfo.value = res.data.user
    storeId.value = res.data.user?.storeId || ''
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('storeId', storeId.value)
    return res
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    storeId.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('storeId')
  }

  async function getUserInfo() {
    const res: any = await request.get('/auth/me')
    userInfo.value = res.data
    if (res.data?.storeId) {
      storeId.value = res.data.storeId
      localStorage.setItem('storeId', res.data.storeId)
    }
    return res
  }

  return { token, userInfo, storeId, login, logout, getUserInfo }
})
