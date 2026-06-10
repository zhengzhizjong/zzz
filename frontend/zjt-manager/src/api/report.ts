import { get } from '@/utils/request'

export function getDailyReport(params?: Record<string, any>) {
  return get('/api/v1/ai/daily-report/generate', { params })
}
