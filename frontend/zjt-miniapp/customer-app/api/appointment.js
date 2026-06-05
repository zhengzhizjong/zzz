const request = require('../utils/request');

module.exports = {
  // 查询可用时段
  getAvailableSlots(params) {
    return request.get('/api/v1/trade/appointments/available-slots', params);
  },
  // 临时锁定时段
  lockTemp(data) {
    return request.post('/api/v1/trade/appointments/lock-temp', data);
  },
  // 创建预约
  createAppointment(data) {
    return request.post('/api/v1/trade/appointments', data);
  },
  // 埋点上报
  trackFunnel(data) {
    return request.post('/api/v1/data/appointment-funnel/track', data);
  },
  // 我的预约列表
  getMyAppointments(params) {
    return request.get('/api/v1/trade/appointments/my', params);
  },
  // 修改预约
  modifyAppointment(id, data) {
    return request.put(`/api/v1/trade/appointments/${id}/modify`, data);
  },
  // 取消预约
  cancelAppointment(id, data) {
    return request.put(`/api/v1/trade/appointments/${id}/cancel`, data);
  },
};
