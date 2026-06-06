const request = require('../utils/request')

module.exports = {
  getMySchedule(month) {
    return request.get('/api/v1/store/schedules/my', { month })
  }
}
