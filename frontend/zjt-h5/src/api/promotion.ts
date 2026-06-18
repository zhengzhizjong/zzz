import { get } from '@/utils/request'

export function getPromotionStats() {
  return get('/api/v1/data/promotion/stats')
}

export function getPromotionList(params?: Record<string, any>) {
  return get('/api/v1/data/promotion/list', { params })
}
