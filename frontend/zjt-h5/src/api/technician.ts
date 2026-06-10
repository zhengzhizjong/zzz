import { get } from '@/utils/request'

export function getTechnicianList(params?: Record<string, any>) {
  return get('/api/v1/store/technicians', { params })
}

export function getTechnicianDetail(id: number | string) {
  return get(`/api/v1/store/technicians/${id}`)
}

export function getTechnicianWorkspace(id: number | string) {
  return get(`/api/v1/store/technicians/${id}/workspace`)
}

export function getTechnicianPerformance(id: number | string) {
  return get(`/api/v1/store/technicians/${id}/performance`)
}
