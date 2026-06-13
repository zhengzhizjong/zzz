import { defineStore } from 'pinia'
import { ref } from 'vue'
import { post, get } from '@/utils/request'
import { getToken, setToken, removeToken, setStoreId } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const userInfo = ref<any>(null)
  const isLogin = ref(!!getToken())

  async function login(phone: string, password: string) {
    const res: any = await post('/api/v1/user/employees/login', { phone, password })
    const data = res.data
    token.value = data.token
    setToken(data.token)
    isLogin.value = true
    // 直接保存登录返回的用户信息
    userInfo.value = {
      id: data.memberId || data.id,
      name: data.nickname || data.name,
      phone: data.phone,
      storeId: data.storeId,
      avatarUrl: data.avatarUrl
    }
    if (data.storeId) {
      setStoreId(String(data.storeId))
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
          setStoreId(String(data.storeId))
        }
      }
    } catch {
      // fetchProfile失败不影响页面，使用登录时保存的信息
    }
  }

  function logout() {
    token.value = null
    userInfo.value = null
    isLogin.value = false
    removeToken()
  }

  return { token, userInfo, isLogin, login, fetchProfile, logout }
})
