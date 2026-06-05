const request = require('../utils/request');

module.exports = {
  // 获取门店列表
  getStoreList(params) {
    return request.get('/api/v1/store/stores', params);
  },
  // 获取门店详情
  getStoreDetail(id) {
    return request.get(`/api/v1/store/stores/${id}`);
  },
};
