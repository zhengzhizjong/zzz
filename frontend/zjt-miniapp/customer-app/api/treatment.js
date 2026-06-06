const request = require('../utils/request')

module.exports = {
  getMyCards() {
    return request.get('/api/v1/trade/treatment-cards/my')
  },
  getCardUsageHistory(cardId) {
    return request.get(`/api/v1/trade/treatment-cards/${cardId}/usage`)
  }
}
