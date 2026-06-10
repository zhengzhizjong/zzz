import request from '@/utils/request'

export function getAppointmentList(params?: Record<string, any>) {
  return request.get('/appointments', { params })
}

export function modifyAppointment(id: string, data: Record<string, any>) {
  return request.put(`/appointments/${id}`, data)
}

export function cancelAppointment(id: string) {
  return request.put(`/appointments/${id}/cancel`)
}

export function createAppointment(data: Record<string, any>) {
  return request.post('/appointments', data)
}
