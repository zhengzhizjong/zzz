import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, getProfile } from '../api/user'
import { setToken, removeToken, getToken } from '../utils/auth'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<any>(null)
  const isLoggedIn = ref(!!getToken())

  async function doLogin(phone: string, password: string) {
    const res: any = await loginApi({ phone, password })
    const data = res.data || res
    const token = data.token
    if (token) {
      setToken(token)
      isLoggedIn.value = true
      // 保存基本信息
      userInfo.value = {
        id: data.memberId,
        name: data.nickname,
        phone: data.phone,
        avatarUrl: data.avatarUrl
      }
    }
    return res
  }

  async function fetchProfile() {
    try {
      const res: any = await getProfile()
      const data = res.data || res
      if (data) {
        userInfo.value = data
      }
    } catch {
      // ignore
    }
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
    fetchProfile,
    logout,
    setUserInfo
  }
})
