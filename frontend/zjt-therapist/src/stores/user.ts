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
      // 直接保存登录返回的用户信息
      userInfo.value = {
        id: data.memberId || data.id,
        name: data.nickname || data.name,
        phone: data.phone,
        storeId: data.storeId,
        avatarUrl: data.avatarUrl,
        technicianId: data.technicianId || data.id
      }
    }
    return res
  }

  async function fetchProfile() {
    try {
      const res: any = await getProfile()
      const data = res.data || res
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
          status: data.status,
          technicianId: data.technicianId || data.id
        }
      }
    } catch {
      // fetchProfile失败不影响页面，使用登录时保存的信息
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
