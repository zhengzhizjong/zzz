import request from '../utils/request'

export function getNotificationList(params: { page?: number; pageSize?: number }) {
  return request.get('/api/v1/user/notifications', { params })
}

export function markNotificationRead(id: number) {
  return request.put(`/api/v1/user/notifications/${id}/read`)
}
