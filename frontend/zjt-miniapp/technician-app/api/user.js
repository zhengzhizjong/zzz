const request = require('../utils/request')

module.exports = {
  employeeLogin(employeeNo, password) {
    return request.post('/api/v1/user/employees/login', { employeeNo, password })
  },
  getProfile() {
    return request.get('/api/v1/user/employees/profile')
  }
}
