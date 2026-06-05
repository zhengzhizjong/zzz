const storeApi = require('../../api/store');
const appointmentApi = require('../../api/appointment');

Page({
  data: {
    storeList: [],
    loading: true,
    keyword: '',
    selectedStoreId: '',
    sessionId: ''
  },

  onLoad() {
    const sessionId = 'sid_' + Date.now() + '_' + Math.random().toString(36).substr(2, 6);
    this.setData({ sessionId });
    this.loadStoreList();
    this.track('browse_store');
  },

  loadStoreList() {
    this.setData({ loading: true });
    storeApi.getStoreList({ keyword: this.data.keyword }).then(res => {
      this.setData({
        storeList: res.data || [],
        loading: false
      });
    }).catch(() => {
      this.setData({ loading: false });
    });
  },

  onSearch(e) {
    this.setData({ keyword: e.detail.value });
    this.loadStoreList();
  },

  onStoreTap(e) {
    const storeId = e.currentTarget.dataset.id;
    const storeName = e.currentTarget.dataset.name;
    this.setData({ selectedStoreId: storeId });
    this.track('select_store', { storeId });
  },

  onNextStep() {
    const { selectedStoreId, sessionId } = this.data;
    if (!selectedStoreId) return;
    const store = this.data.storeList.find(s => s.id === selectedStoreId);
    const storeName = store ? store.name : '';
    wx.navigateTo({
      url: `/pages/appointment/step2-technician?storeId=${selectedStoreId}&storeName=${encodeURIComponent(storeName)}&sessionId=${sessionId}`
    });
  },

  track(event, extra) {
    appointmentApi.trackFunnel({
      sessionId: this.data.sessionId,
      step: 'step1',
      event,
      extra: extra || {}
    }).catch(() => {});
  }
});
