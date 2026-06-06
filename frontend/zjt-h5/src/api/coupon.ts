import { get, post } from '@/utils/request'

export function getMyCoupons(status?: string) {
  return get('/api/v1/trade/coupon-records/my', { params: { status } })
}

export function claimCoupon(couponId: number | string) {
  return post(`/api/v1/trade/coupons/${couponId}/claim`)
}
