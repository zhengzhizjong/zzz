import request from '@/utils/request'
import type { R, PageResult } from '@/types/api'
import type { TechnicianInfo, TechnicianCreateRequest, TechnicianUpdateRequest } from '@/types/technician'

export function getTechnicianList(params: { page: number; pageSize: number; keyword?: string; storeId?: number; status?: number; isOnline?: number }) {
  return request.get<R<PageResult<TechnicianInfo>>>('/api/v1/store/technicians', { params })
}

export function getTechnicianDetail(id: number) {
  return request.get<R<TechnicianInfo>>(`/api/v1/store/technicians/${id}`)
}

export function createTechnician(data: TechnicianCreateRequest) {
  return request.post<R<void>>('/api/v1/store/technicians', data)
}

export function updateTechnician(id: number, data: TechnicianUpdateRequest) {
  return request.put<R<void>>(`/api/v1/store/technicians/${id}`, data)
}

export function checkIn(id: number) {
  return request.put<R<void>>(`/api/v1/store/technicians/${id}/check-in`)
}

export function checkOut(id: number) {
  return request.put<R<void>>(`/api/v1/store/technicians/${id}/check-out`)
}
