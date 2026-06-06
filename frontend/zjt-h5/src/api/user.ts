import { get, post, put } from '@/utils/request'

export function sendVerifyCode(phone: string) {
  return post('/api/v1/user/sms/send', { phone })
}

export function loginBySms(phone: string, verifyCode: string) {
  return post('/api/v1/user/members/login', { phone, verifyCode })
}

export function wechatLogin(code: string) {
  return post('/api/v1/user/members/wechat-login', { code })
}

export function getProfile() {
  return get('/api/v1/user/members/profile')
}

export function updateProfile(data: Record<string, any>) {
  return put('/api/v1/user/members/profile', data)
}

export function getHealthProfile() {
  return get('/api/v1/user/members/health-profile')
}

export function updateHealthProfile(data: Record<string, any>) {
  return put('/api/v1/user/members/health-profile', data)
}
