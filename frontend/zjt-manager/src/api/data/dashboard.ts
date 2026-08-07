import request from '@/utils/request'

export function getDashboardSummary(storeId?: number) {
  return request.get('/api/v1/data/dashboard/summary', { params: { storeId } })
}
