import { defineStore } from 'pinia'
import { ref } from 'vue'
import { post, get } from '@/utils/request'

const TOKEN_KEY = 'manager_token'
const STORE_ID_KEY = 'manager_store_id'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const userInfo = ref<any>(null)
  const storeId = ref(localStorage.getItem(STORE_ID_KEY) || '')
  const isLogin = ref(!!localStorage.getItem(TOKEN_KEY))

  async function login(phone: string, password: string) {
    const res: any = await post('/api/v1/user/employees/login', { phone, password })
    const data = res.data
    token.value = data.token
    isLogin.value = true
    localStorage.setItem(TOKEN_KEY, data.token)
    // 直接保存登录返回的用户信息
    userInfo.value = {
      id: data.memberId || data.id,
      name: data.nickname || data.name,
      phone: data.phone,
      storeId: data.storeId,
      avatarUrl: data.avatarUrl,
      position: data.position || '',
      employeeNo: data.employeeNo || '',
    }
    if (data.storeId) {
      storeId.value = String(data.storeId)
      localStorage.setItem(STORE_ID_KEY, String(data.storeId))
    }
    return data
  }

  async function fetchProfile() {
    try {
      const res: any = await get('/api/v1/user/employees/profile')
      const data = res.data
      if (data) {
        userInfo.value = {
          id: data.id,
          name: data.name,
          phone: data.phone,
          storeId: data.storeId,
          avatarUrl: data.avatarUrl,
          employeeNo: data.employeeNo,
          position: data.position,
          departmentId: data.departmentId,
          status: data.status
        }
        if (data.storeId) {
          storeId.value = String(data.storeId)
          localStorage.setItem(STORE_ID_KEY, String(data.storeId))
        }
      }
    } catch {
      // fetchProfile失败不影响页面，使用登录时保存的信息
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    storeId.value = ''
    isLogin.value = false
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(STORE_ID_KEY)
  }

  return { token, userInfo, storeId, isLogin, login, fetchProfile, logout }
})
