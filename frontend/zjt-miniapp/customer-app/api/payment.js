const request = require('../utils/request')

module.exports = {
  createPayment(orderId, channel) {
    return request.post('/api/v1/trade/payments/create', { orderId, channel })
  },
  getOrder(orderId) {
    return request.get(`/api/v1/trade/orders/${orderId}`)
  }
}
