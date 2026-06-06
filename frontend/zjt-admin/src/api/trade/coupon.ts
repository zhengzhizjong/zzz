import request from '@/utils/request'
import type { R, PageResult } from '@/types/api'

export interface CouponInfo {
  id: number
  name: string
  type: number
  value: number
  minAmount: number
  stock: number
  usedCount: number
  validStartTime: string
  validEndTime: string
  status: number
  createdAt: string
}

export function getCouponList(params: any) {
  return request.get<R<PageResult<CouponInfo>>>('/api/v1/trade/coupons', { params })
}

export function createCoupon(data: any) {
  return request.post<R<void>>('/api/v1/trade/coupons', data)
}

export function updateCoupon(id: number, data: any) {
  return request.put<R<void>>(`/api/v1/trade/coupons/${id}`, data)
}

export function issueCoupon(id: number, memberIds: number[]) {
  return request.post<R<void>>(`/api/v1/trade/coupons/${id}/issue`, { memberIds })
}
