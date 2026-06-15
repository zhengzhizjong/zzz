import request from '@/utils/request'
import type { R } from '@/types/api'

export interface ScheduleItem {
  id?: number
  technicianId: number
  scheduleDate: string
  startTime?: string
  endTime?: string
  scheduleType: number // 1上班 2休息 3请假
  shiftType?: string // morning早班 afternoon中班 evening晚班
  notes?: string
}

export function getScheduleByTechnician(technicianId: number, month: string) {
  return request.get<R<any>>(`/api/v1/store/schedules/technician/${technicianId}`, { params: { month } })
}

export function getScheduleByStore(storeId: number, date: string) {
  return request.get<R<any>>(`/api/v1/store/schedules/store/${storeId}`, { params: { date } })
}

export function createSchedule(data: ScheduleItem) {
  return request.post<R<void>>('/api/v1/store/schedules', data)
}

export function batchCreateSchedule(data: any) {
  return request.post<R<void>>('/api/v1/store/schedules/batch', data)
}

export function updateSchedule(id: number, data: Partial<ScheduleItem>) {
  return request.put<R<void>>(`/api/v1/store/schedules/${id}`, data)
}

export function deleteSchedule(id: number) {
  return request.delete<R<void>>(`/api/v1/store/schedules/${id}`)
}
