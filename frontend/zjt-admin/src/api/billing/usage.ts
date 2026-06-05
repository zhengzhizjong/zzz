import request from '@/utils/request'
import type { R } from '@/types/api'

export function getUsage(tenantId: number, params: any) {
  return request.get<R<any>>(`/api/v1/admin/usage/${tenantId}`, { params })
}

export function getUsageSummary(tenantId: number, month: string) {
  return request.get<R<any>>(`/api/v1/admin/usage/${tenantId}/summary`, { params: { month } })
}
