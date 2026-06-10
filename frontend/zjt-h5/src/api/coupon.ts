import { get, post } from '@/utils/request'

export function getMyCoupons(params?: Record<string, any>) {
  return get('/api/v1/trade/coupon-records/my', { params })
}

export function claimCoupon(couponId: number | string, memberId: number | string) {
  return post(`/api/v1/trade/coupons/${couponId}/claim`, null, { params: { memberId } })
}

export function getCouponList(params?: Record<string, any>) {
  return get('/api/v1/trade/coupons', { params })
}
