import request from '@/utils/request'
import type { R, PageResult } from '@/types/api'

export interface SmsLogInfo {
  id: number
  phone: string
  templateName: string
  content: string
  status: number
  sentAt: string
}

export function getSmsLogList(params: any) {
  return request.get<R<PageResult<SmsLogInfo>>>('/api/v1/admin/sms-logs', { params })
}
