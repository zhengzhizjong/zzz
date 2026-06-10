import { get } from '@/utils/request'

export function getTechnicianList(params: any) {
  return get('/api/v1/store/technicians', { params })
}

export function getTechnicianDetail(id: number | string) {
  return get(`/api/v1/store/technicians/${id}`)
}
