import request from '@/utils/request'
import type { R } from '@/types/api'

export interface PlanInfo {
  id: number
  planName: string
  planCode: string
  price: number
  durationDays: number
  maxStores: number
  maxTechnicians: number
  featuresJson: string
  aiQuota: number
  status: number
}

export function getPlanList() {
  return request.get<R<PlanInfo[]>>('/api/v1/admin/plans')
}

export function createPlan(data: any) {
  return request.post<R<void>>('/api/v1/admin/plans', data)
}

export function updatePlan(id: number, data: any) {
  return request.put<R<void>>(`/api/v1/admin/plans/${id}`, data)
}
