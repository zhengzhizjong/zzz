import request from '@/utils/request'

export function getMemberList(params?: Record<string, any>) {
  return request.get('/members', { params })
}

export function getMemberDetail(id: string) {
  return request.get(`/members/${id}`)
}
