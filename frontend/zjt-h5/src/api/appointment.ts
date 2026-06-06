import { get, post, put } from '@/utils/request'

export function getAvailableSlots(params: Record<string, any>) {
  return get('/api/v1/trade/appointments/available-slots', { params })
}

export function lockTemp(data: Record<string, any>) {
  return post('/api/v1/trade/appointments/lock-temp', data)
}

export function createAppointment(data: Record<string, any>) {
  return post('/api/v1/trade/appointments', data)
}

export function trackFunnel(data: Record<string, any>) {
  return post('/api/v1/data/appointment-funnel/track', data)
}

export function getMyAppointments(params?: Record<string, any>) {
  return get('/api/v1/trade/appointments/my', { params })
}

export function modifyAppointment(id: number | string, data: Record<string, any>) {
  return put(`/api/v1/trade/appointments/${id}/modify`, data)
}

export function cancelAppointment(id: number | string, data?: Record<string, any>) {
  return put(`/api/v1/trade/appointments/${id}/cancel`, data)
}
