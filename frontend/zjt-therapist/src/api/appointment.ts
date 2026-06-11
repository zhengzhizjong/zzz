import request from '../utils/request'

export function getTodayAppointments(technicianId: number) {
  return request.get('/api/v1/trade/appointments', {
    params: { technicianId, date: 'today' }
  })
}

export function getAppointmentDetail(id: number) {
  return request.get(`/api/v1/trade/appointments/${id}`)
}

export function checkIn(technicianId: number) {
  return request.put(`/api/v1/store/technicians/${technicianId}/check-in`)
}

export function checkOut(technicianId: number) {
  return request.put(`/api/v1/store/technicians/${technicianId}/check-out`)
}
