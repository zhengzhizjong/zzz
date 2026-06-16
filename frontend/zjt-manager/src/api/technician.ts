import { get, post } from '@/utils/request'

export function getTechnicianList(params?: Record<string, any>) {
  return get('/api/v1/store/technicians', { params })
}

export function getTechnicianDetail(id: number | string) {
  return get(`/api/v1/store/technicians/${id}`)
}

export function checkIn(id: number | string) {
  return post(`/api/v1/store/technicians/${id}/check-in`)
}

export function checkOut(id: number | string) {
  return post(`/api/v1/store/technicians/${id}/check-out`)
}
