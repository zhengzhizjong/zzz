import { get, put, post } from '@/utils/request'

export function getAppointmentList(params?: Record<string, any>) {
  return get('/api/v1/trade/appointments', { params })
}

export function getAppointmentDetail(id: number | string) {
  return get(`/api/v1/trade/appointments/${id}`)
}

export function modifyAppointment(id: number | string, data: Record<string, any>) {
  return put(`/api/v1/trade/appointments/${id}`, data)
}

export function cancelAppointment(id: number | string, data?: Record<string, any>) {
  return put(`/api/v1/trade/appointments/${id}/cancel`, data)
}

export function createAppointment(data: Record<string, any>) {
  return post('/api/v1/trade/appointments', data)
}
