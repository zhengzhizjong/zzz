import request from '@/utils/request'
import type { R, PageResult } from '@/types/api'

export interface TenantInfo {
  id: number
  tenantName: string
  contactName: string
  contactPhone: string
  industry: string
  status: number
  statusName: string
  planName: string
  expireAt: string
  maxStores: number
  maxTechnicians: number
  currentStores: number
  currentTechnicians: number
  logoUrl: string
  primaryColor: string
  customDomain: string
  createdAt: string
}

export function getTenantList(params: { page: number; pageSize: number; keyword?: string; status?: number }) {
  return request.get<R<PageResult<TenantInfo>>>('/api/v1/admin/tenants', { params })
}

export function getTenantDetail(id: number) {
  return request.get<R<TenantInfo>>(`/api/v1/admin/tenants/${id}`)
}

export function createTenant(data: any) {
  return request.post<R<void>>('/api/v1/admin/tenants', data)
}

export function updateTenant(id: number, data: any) {
  return request.put<R<void>>(`/api/v1/admin/tenants/${id}`, data)
}

export function updateTenantStatus(id: number, status: number) {
  return request.put<R<void>>(`/api/v1/admin/tenants/${id}/status`, { status })
}

export function updateWhiteLabel(id: number, data: any) {
  return request.put<R<void>>(`/api/v1/admin/tenants/${id}/white-label`, data)
}
