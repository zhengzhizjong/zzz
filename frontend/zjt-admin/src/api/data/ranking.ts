import request from '@/utils/request'
import type { R, PageResult } from '@/types/api'

export interface StoreRankingItem {
  storeId: number
  storeName: string
  storeNo: string
  revenue: number
  customerCount: number
  rating: number
  revenueChange: number
  customerChange: number
  ratingChange: number
}

export interface TechnicianRankingItem {
  technicianId: number
  technicianName: string
  storeName: string
  technicianNo: string
  revenue: number
  serviceCount: number
  rating: number
  promotionCount: number
  revenueChange: number
  serviceChange: number
  ratingChange: number
  promotionChange: number
}

export function getStoreRanking(params: { period: string; dimension: string; page?: number; pageSize?: number }) {
  return request.get<R<PageResult<StoreRankingItem>>>('/api/v1/data/ranking/store', { params })
}

export function getTechnicianRanking(params: { period: string; dimension: string; page?: number; pageSize?: number }) {
  return request.get<R<PageResult<TechnicianRankingItem>>>('/api/v1/data/ranking/technician', { params })
}
