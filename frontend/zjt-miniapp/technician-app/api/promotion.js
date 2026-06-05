const request = require('../utils/request');

module.exports = {
  // 获取推广信息
  getPromotion(techId) {
    return request.get(`/api/v1/store/technicians/${techId}/promotion`);
  },
  // 生成海报
  generatePoster(techId) {
    return request.post(`/api/v1/store/technicians/${techId}/promotion/generate-poster`);
  },
  // 推广统计
  getStats(techId, params) {
    return request.get(`/api/v1/store/technicians/${techId}/promotion/stats`, params);
  },
  // 佣金明细
  getCommissions(techId, params) {
    return request.get(`/api/v1/store/technicians/${techId}/promotion/commissions`, params);
  },
  // 排行榜
  getRanking(techId, params) {
    return request.get(`/api/v1/store/technicians/${techId}/promotion/ranking`, params);
  },
};
