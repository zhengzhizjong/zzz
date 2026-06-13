import request from '@/utils/request'
import type { R, PageResult } from '@/types/api'

export interface AuditLogInfo {
  id: number
  operatorName: string
  module: string
  action: string
  targetType: string
  targetId: string
  ip: string
  status: number
  oldValue: string
  newValue: string
  userAgent: string
  createdAt: string
}

export function getAuditLogList(params: any) {
  return request.get<R<PageResult<AuditLogInfo>>>('/api/v1/admin/audit-logs', { params })
}
