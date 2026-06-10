import { get } from '@/utils/request'

export function getMemberList(params?: Record<string, any>) {
  return get('/api/v1/user/members', { params })
}

export function getMemberDetail(id: number | string) {
  return get(`/api/v1/user/members/${id}`)
}
