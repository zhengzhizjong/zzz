import { get, put } from '@/utils/request'

export function getLeadList(params: any) {
  return get('/api/v1/integration/leads', { params })
}

export function updateLead(id: number | string, data: any) {
  return put(`/api/v1/integration/leads/${id}`, data)
}
