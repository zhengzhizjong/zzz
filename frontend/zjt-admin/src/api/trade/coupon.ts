import request from '@/utils/request'
import type { R, PageResult } from '@/types/api'

export interface CouponInfo {
  id: number
  couponNo: string
  couponName: string
  couponType: number
  discountValue: number
  minAmount: number
  totalCount: number
  remainCount: number
  startTime: string
  endTime: string
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
