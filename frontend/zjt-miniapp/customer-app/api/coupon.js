const request = require('../utils/request')

module.exports = {
  getMyCoupons(status) {
    return request.get('/api/v1/trade/coupon-records/my', { status })
  },
  claimCoupon(couponId) {
    return request.post(`/api/v1/trade/coupons/${couponId}/claim`)
  }
}
