const request = require('../utils/request');

module.exports = {
  // 获取技师列表(按门店筛选)
  getTechnicianList(params) {
    return request.get('/api/v1/store/technicians', params);
  },
  // 获取技师详情
  getTechnicianDetail(id) {
    return request.get(`/api/v1/store/technicians/${id}`);
  },
};
