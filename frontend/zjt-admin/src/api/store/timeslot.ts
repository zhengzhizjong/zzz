import request from '@/utils/request'
import type { R } from '@/types/api'

export interface TimeSlotConfig {
  id: number
  storeId: number
  businessStartTime: string
  businessEndTime: string
  slotDurationMinutes: number
  restStartTime: string
  restEndTime: string
}

export interface TimeSlotConfigRequest {
  storeId: number
  businessStartTime: string
  businessEndTime: string
  slotDurationMinutes: number
  restStartTime?: string
  restEndTime?: string
}

export interface AvailableSlot {
  startTime: string
  endTime: string
  isAvailable: boolean
}

export function getTimeSlotConfig(storeId: number) {
  return request.get<R<TimeSlotConfig>>(`/api/v1/store/time-slot-config/${storeId}`)
}

export function createTimeSlotConfig(data: TimeSlotConfigRequest) {
  return request.post<R<TimeSlotConfig>>('/api/v1/store/time-slot-config', data)
}

export function updateTimeSlotConfig(id: number, data: TimeSlotConfigRequest) {
  return request.put<R<TimeSlotConfig>>(`/api/v1/store/time-slot-config/${id}`, data)
}

export function getAvailableSlots(params: { storeId: number; technicianId?: number; date: string }) {
  return request.get<R<AvailableSlot[]>>('/api/v1/store/time-slots/available', { params })
}
