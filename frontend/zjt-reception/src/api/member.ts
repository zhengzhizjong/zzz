import { get } from '@/utils/request'

export function getMemberList(params: any) {
  return get('/api/v1/user/members', { params })
}

export function getMemberDetail(id: number | string) {
  return get(`/api/v1/user/members/${id}`)
}

export function getMemberProfile() {
  return get('/api/v1/user/members/profile')
}
