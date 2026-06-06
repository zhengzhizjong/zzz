const request = require('../utils/request')

module.exports = {
  sendVerifyCode(phone) {
    return request.post('/api/v1/user/sms/send', { phone })
  },
  login(phone, verifyCode) {
    return request.post('/api/v1/user/members/login', { phone, verifyCode })
  },
  wechatLogin(code) {
    return request.post('/api/v1/user/members/wechat-login', { code })
  },
  getProfile() {
    return request.get('/api/v1/user/members/profile')
  },
  updateProfile(data) {
    return request.put('/api/v1/user/members/profile', data)
  },
  getHealthProfile() {
    return request.get('/api/v1/user/members/health-profile')
  },
  updateHealthProfile(data) {
    return request.put('/api/v1/user/members/health-profile', data)
  }
}
