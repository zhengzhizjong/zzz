import { get, post, put, del } from '@/utils/request'

export function getScheduleByTechnician(technicianId: number, month: string) {
  return get(`/api/v1/store/schedules/technician/${technicianId}`, { params: { month } })
}

export function getScheduleByStore(storeId: number, date: string) {
  return get(`/api/v1/store/schedules/store/${storeId}`, { params: { date } })
}

export function createSchedule(data: any) {
  return post('/api/v1/store/schedules', data)
}

export function batchCreateSchedule(data: any) {
  return post('/api/v1/store/schedules/batch', data)
}

export function updateSchedule(id: number, data: any) {
  return put(`/api/v1/store/schedules/${id}`, data)
}

export function deleteSchedule(id: number) {
  return del(`/api/v1/store/schedules/${id}`)
}
