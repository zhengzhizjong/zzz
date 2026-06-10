import request from '@/utils/request'

export function getTechnicianList(params?: Record<string, any>) {
  return request.get('/technicians', { params })
}

export function getTechnicianDetail(id: string) {
  return request.get(`/technicians/${id}`)
}

export function checkIn(id: string) {
  return request.post(`/technicians/${id}/check-in`)
}

export function checkOut(id: string) {
  return request.post(`/technicians/${id}/check-out`)
}
