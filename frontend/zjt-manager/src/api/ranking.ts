import { get } from '@/utils/request'

export function getStoreRanking(params?: Record<string, any>) {
  return get('/api/v1/data/ranking/store', { params })
}

export function getTechnicianRanking(params?: Record<string, any>) {
  return get('/api/v1/data/ranking/technician', { params })
}
