const technicianApi = require('../../api/technician');

const SKILL_LEVEL_MAP = {
  1: '初级',
  2: '中级',
  3: '高级',
  4: '专家'
};

Page({
  data: {
    technicians: [],
    loading: false,
    storeId: '',
    keyword: ''
  },

  onLoad(options) {
    this.setData({ storeId: options.storeId || '' });
    this.loadTechnicianList();
  },

  onPullDownRefresh() {
    this.setData({ keyword: '' });
    this.loadTechnicianList().then(() => {
      wx.stopPullDownRefresh();
    });
  },

  loadTechnicianList() {
    if (this.data.loading) return Promise.resolve();

    this.setData({ loading: true });

    const params = { storeId: this.data.storeId };
    if (this.data.keyword) {
      params.keyword = this.data.keyword;
    }

    return technicianApi.getTechnicianList(params).then(res => {
      const list = (res.data.list || res.data || []).map(item => ({
        ...item,
        skillLevelText: SKILL_LEVEL_MAP[item.skillLevel] || '未知',
        isFullyBooked: item.isFullyBooked || false,
        isOnline: item.isOnline !== false
      }));
      this.setData({
        technicians: list,
        loading: false
      });
    }).catch(() => {
      this.setData({ loading: false });
    });
  },

  onSearchInput(e) {
    this.setData({ keyword: e.detail.value });
  },

  onSearch() {
    this.loadTechnicianList();
  },

  onTechnicianTap(e) {
    const techId = e.currentTarget.dataset.id;
    const isFullyBooked = e.currentTarget.dataset.fullybooked;
    if (isFullyBooked) {
      wx.showToast({ title: '该技师已约满', icon: 'none' });
      return;
    }
    wx.navigateTo({
      url: `/pages/appointment/step3-datetime?storeId=${this.data.storeId}&techId=${techId}`
    });
  }
});
