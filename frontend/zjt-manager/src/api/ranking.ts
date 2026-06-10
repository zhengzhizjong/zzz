import request from '@/utils/request'

export function getStoreRanking(params?: Record<string, any>) {
  return request.get('/data/ranking', { params })
}

export function getTechnicianRanking(params?: Record<string, any>) {
  return request.get('/data/ranking/technicians', { params })
}
