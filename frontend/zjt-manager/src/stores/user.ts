import { defineStore } from 'pinia'
import { ref } from 'vue'
import { post, get } from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('manager_token') || '')
  const userInfo = ref<any>(null)
  const storeId = ref(localStorage.getItem('manager_store_id') || '')
  const isLogin = ref(!!localStorage.getItem('manager_token'))

  async function login(phone: string, password: string) {
    const res: any = await post('/api/v1/user/employees/login', { phone, password })
    token.value = res.data.token
    isLogin.value = true
    localStorage.setItem('manager_token', res.data.token)
    if (res.data.storeId) {
      storeId.value = String(res.data.storeId)
      localStorage.setItem('manager_store_id', String(res.data.storeId))
    }
    return res.data
  }

  async function fetchProfile() {
    try {
      const res: any = await get('/api/v1/user/employees/profile')
      userInfo.value = res.data
      if (res.data?.storeId) {
        storeId.value = String(res.data.storeId)
        localStorage.setItem('manager_store_id', String(res.data.storeId))
      }
    } catch {
      userInfo.value = null
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    storeId.value = ''
    isLogin.value = false
    localStorage.removeItem('manager_token')
    localStorage.removeItem('manager_store_id')
  }

  return { token, userInfo, storeId, isLogin, login, fetchProfile, logout }
})
