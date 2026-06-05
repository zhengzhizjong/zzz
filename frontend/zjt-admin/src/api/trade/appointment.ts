import request from '@/utils/request'
import type { R, PageResult } from '@/types/api'

export interface AppointmentInfo {
  id: number
  appointmentNo: string
  memberName: string
  memberPhone: string
  storeName: string
  serviceItemName: string
  technicianName: string
  appointmentDate: string
  appointmentTime: string
  timeSlot: string
  status: number
  statusName: string
  sourceChannel: string
  createdAt: string
}

export function getAppointmentList(params: {
  page: number
  pageSize: number
  storeId?: number
  technicianId?: number
  startDate?: string
  endDate?: string
  status?: number
  keyword?: string
}) {
  return request.get<R<PageResult<AppointmentInfo>>>('/api/v1/trade/appointments', { params })
}

export function getAppointmentDetail(id: number) {
  return request.get<R<AppointmentInfo>>(`/api/v1/trade/appointments/${id}`)
}
