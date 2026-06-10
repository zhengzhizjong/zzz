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
    token.value = res.data.token
    setToken(res.data.token)
    isLogin.value = true
    if (res.data.storeId) {
      setStoreId(String(res.data.storeId))
    }
    return res.data
  }

  async function fetchProfile() {
    try {
      const res: any = await get('/api/v1/user/employees/profile')
      userInfo.value = res.data
    } catch {
      userInfo.value = null
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
