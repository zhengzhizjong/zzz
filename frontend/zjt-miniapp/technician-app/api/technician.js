const request = require('../utils/request');

module.exports = {
  // 工作台
  getWorkspace(techId) {
    return request.get(`/api/v1/store/technicians/${techId}/workspace`);
  },
  // 业绩
  getPerformance(techId) {
    return request.get(`/api/v1/store/technicians/${techId}/performance`);
  },
  // 签到
  checkIn(techId) {
    return request.post(`/api/v1/store/technicians/${techId}/check-in`);
  },
  // 签退
  checkOut(techId) {
    return request.post(`/api/v1/store/technicians/${techId}/check-out`);
  },
};
