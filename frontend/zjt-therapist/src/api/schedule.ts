import request from '../utils/request'

export function getSchedule(technicianId: number, month: string) {
  return request.get(`/api/v1/store/schedules/technician/${technicianId}`, {
    params: { month }
  })
}
