import { defineStore } from 'pinia'
import { ref } from 'vue'
import { post } from '@/utils/request'
import { setToken, removeToken, getToken } from '@/utils/auth'
import router from '@/router'

interface UserInfo {
  id: number
  username: string
  name: string
  phone: string
  role: string
  avatar: string
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken() || '')
  const userInfo = ref<UserInfo | null>(null)
  const tenantId = ref<string>(localStorage.getItem('tenantId') || '')
  const permissions = ref<string[]>([])

  async function login(username: string, password: string) {
    const res: any = await post('/api/auth/login', { username, password })
    token.value = res.data.token
    tenantId.value = res.data.tenantId
    setToken(res.data.token)
    localStorage.setItem('tenantId', res.data.tenantId)
    userInfo.value = res.data.user
    permissions.value = res.data.permissions || []
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    tenantId.value = ''
    permissions.value = []
    removeToken()
    localStorage.removeItem('tenantId')
    router.push('/login')
  }

  return {
    token,
    userInfo,
    tenantId,
    permissions,
    login,
    logout
  }
})
