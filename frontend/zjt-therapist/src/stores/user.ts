import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login } from '../api/user'
import { setToken, removeToken, getToken } from '../utils/auth'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<any>(null)
  const isLoggedIn = ref(!!getToken())

  async function doLogin(phone: string, password: string) {
    const res: any = await login({ phone, password })
    const token = res.data?.token || res.token
    if (token) {
      setToken(token)
      isLoggedIn.value = true
    }
    return res
  }

  function logout() {
    removeToken()
    userInfo.value = null
    isLoggedIn.value = false
  }

  function setUserInfo(info: any) {
    userInfo.value = info
  }

  return {
    userInfo,
    isLoggedIn,
    doLogin,
    logout,
    setUserInfo
  }
})
