import request from '@/utils/request'
import type { R } from '@/types/api'

export function getScheduleByTechnician(technicianId: number, month: string) {
  return request.get<R<any>>(`/api/v1/store/schedules/technician/${technicianId}`, { params: { month } })
}

export function getScheduleByStore(storeId: number, date: string) {
  return request.get<R<any>>(`/api/v1/store/schedules/store/${storeId}`, { params: { date } })
}

export function createSchedule(data: any) {
  return request.post<R<void>>('/api/v1/store/schedules', data)
}

export function batchCreateSchedule(data: any) {
  return request.post<R<void>>('/api/v1/store/schedules/batch', data)
}

export function updateSchedule(id: number, data: any) {
  return request.put<R<void>>(`/api/v1/store/schedules/${id}`, data)
}

export function deleteSchedule(id: number) {
  return request.delete<R<void>>(`/api/v1/store/schedules/${id}`)
}
