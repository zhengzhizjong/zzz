import request from '@/utils/request'
import type { R, PageResult } from '@/types/api'

export interface LeadInfo {
  id: number
  sourcePlatform: string
  contactName: string
  contactPhone: string
  intentionLevel: string
  tags: string
  assignedTo: number
  status: number
  followUpNotes: string
  createdAt: string
}

export function getLeadList(params: any) {
  return request.get<R<PageResult<LeadInfo>>>('/api/v1/integration/leads', { params })
}

export function createLead(data: any) {
  return request.post<R<void>>('/api/v1/integration/leads', data)
}

export function updateLead(id: number, data: any) {
  return request.put<R<void>>(`/api/v1/integration/leads/${id}`, data)
}

export function assignLead(id: number, assignedTo: number) {
  return request.put<R<void>>(`/api/v1/integration/leads/${id}/assign`, { assignedTo })
}

export function convertLead(id: number, memberId: number) {
  return request.put<R<void>>(`/api/v1/integration/leads/${id}/convert`, { memberId })
}
