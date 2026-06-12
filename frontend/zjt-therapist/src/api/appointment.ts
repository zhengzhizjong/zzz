import request from '../utils/request'

export function getTodayAppointments(params: { storeId?: number; status?: number; page?: number; pageSize?: number }) {
  return request.get('/api/v1/trade/appointments', {
    params: { page: 1, pageSize: 50, ...params }
  })
}

export function getAppointmentDetail(id: number) {
  return request.get(`/api/v1/trade/appointments/${id}`)
}

export function checkIn(technicianId: number) {
  return request.post(`/api/v1/store/technicians/${technicianId}/check-in`)
}

export function checkOut(technicianId: number) {
  return request.post(`/api/v1/store/technicians/${technicianId}/check-out`)
}
