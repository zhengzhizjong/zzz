import request from '@/utils/request'

export function getDailyReport(params?: Record<string, any>) {
  return request.get('/ai/daily-report', { params })
}
