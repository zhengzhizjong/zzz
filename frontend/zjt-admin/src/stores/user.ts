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
  position: string
  storeId: number
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken() || '')
  const userInfo = ref<UserInfo | null>(null)
  const tenantId = ref<string>(localStorage.getItem('tenantId') || '')
  const permissions = ref<string[]>([])

  async function login(username: string, password: string) {
    const res: any = await post('/api/v1/user/employees/login', { phone: username, password })
    token.value = res.data.token
    tenantId.value = String(res.data.tenantId || 1)
    setToken(res.data.token)
    localStorage.setItem('tenantId', String(res.data.tenantId || 1))
    userInfo.value = {
      id: res.data.memberId,
      username: res.data.nickname,
      name: res.data.nickname,
      phone: res.data.phone,
      role: 'employee',
      avatar: res.data.avatarUrl || '',
      position: res.data.position || '',
      storeId: res.data.storeId || 0,
    }
    permissions.value = res.data.permissions || ['*']
    return res.data
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
