import request from '@/utils/request'
import type { R } from '@/types/api'

export function generateReport(data: any) {
  return request.post<R<any>>('/api/v1/ai/daily-report/generate', data)
}

export function getReportList(params: any) {
  return request.get<R<any>>('/api/v1/ai/daily-report', { params })
}

export function getReportDetail(id: number) {
  return request.get<R<any>>(`/api/v1/ai/daily-report/${id}`)
}
