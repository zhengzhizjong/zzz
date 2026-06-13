import { defineStore } from 'pinia'
import { ref } from 'vue'
import { post, get } from '@/utils/request'
import { setToken, removeToken, getToken } from '@/utils/auth'
import router from '@/router'

interface UserInfo {
  id: number
  nickname: string
  name: string
  realName: string
  phone: string
  avatar: string
  gender: number
  birthday: string
  levelName: string
  memberType: string
  points: number
  balance: number
  constitutionType: string
  memberNo: string
  source: string
  status: number
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken() || '')
  const userInfo = ref<UserInfo | null>(null)
  const isLogin = ref<boolean>(!!getToken())

  async function loginBySms(phone: string, verifyCode: string) {
    const res: any = await post('/api/v1/user/members/login', { phone, verifyCode })
    const data = res.data
    token.value = data.token
    setToken(data.token)
    isLogin.value = true
    // 直接保存登录返回的用户信息
    userInfo.value = {
      id: data.memberId || data.id,
      nickname: data.nickname || '',
      name: data.nickname || '',
      realName: data.nickname || '',
      phone: data.phone || phone,
      avatar: data.avatarUrl || '',
      gender: 0,
      birthday: '',
      levelName: '',
      memberType: '',
      points: 0,
      balance: 0,
      constitutionType: '',
      memberNo: '',
      source: '',
      status: 1
    }
  }

  async function loginByWechat(code: string) {
    const res: any = await post('/api/v1/user/members/wechat-login', { code })
    const data = res.data
    token.value = data.token
    setToken(data.token)
    isLogin.value = true
    // 直接保存登录返回的用户信息
    userInfo.value = {
      id: data.memberId || data.id,
      nickname: data.nickname || '',
      name: data.nickname || '',
      realName: data.nickname || '',
      phone: data.phone || '',
      avatar: data.avatarUrl || '',
      gender: 0,
      birthday: '',
      levelName: '',
      memberType: '',
      points: 0,
      balance: 0,
      constitutionType: '',
      memberNo: '',
      source: '',
      status: 1
    }
  }

  async function fetchProfile() {
    try {
      const res: any = await get('/api/v1/user/members/profile')
      // 后端返回 {member: {...}, level: {...}}
      const data = res.data || {}
      userInfo.value = data.member || data
    } catch {
      userInfo.value = null
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    isLogin.value = false
    removeToken()
    router.push('/login')
  }

  return {
    token,
    userInfo,
    isLogin,
    loginBySms,
    loginByWechat,
    fetchProfile,
    logout
  }
})
