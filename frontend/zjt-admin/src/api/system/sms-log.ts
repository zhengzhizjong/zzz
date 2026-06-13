import request from '@/utils/request'
import type { R, PageResult } from '@/types/api'

export interface SmsLogInfo {
  id: number
  phone: string
  templateCode: string
  content: string
  sendStatus: number
  sendTime: string
  bizId: string
  createdAt: string
}

export function getSmsLogList(params: any) {
  return request.get<R<PageResult<SmsLogInfo>>>('/api/v1/admin/sms-logs', { params })
}
