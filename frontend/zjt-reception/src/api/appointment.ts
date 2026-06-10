import { get, put, post } from '@/utils/request'

export function getTodayAppointments(storeId: number | string) {
  return get('/api/v1/trade/appointments', { params: { storeId, statusGroup: 'pending', page: 1, pageSize: 100 } })
}

export function getMyAppointments(params: any) {
  return get('/api/v1/trade/appointments/my', { params })
}

export function getAppointmentList(params: any) {
  return get('/api/v1/trade/appointments', { params })
}

export function checkInAppointment(id: number | string) {
  return put(`/api/v1/trade/appointments/${id}/modify`, { status: 'in_service' })
}

export function completeAppointment(id: number | string) {
  return put(`/api/v1/trade/appointments/${id}/modify`, { status: 'completed' })
}

export function cancelAppointment(id: number | string, data?: any) {
  return put(`/api/v1/trade/appointments/${id}/cancel`, data)
}
