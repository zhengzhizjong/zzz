const technicianApi = require('../../api/technician');
const appointmentApi = require('../../api/appointment');

Page({
  data: {
    technicianList: [],
    loading: true,
    storeId: '',
    storeName: '',
    selectedTechId: 0,
    selectedTechName: '不指定',
    sessionId: ''
  },

  onLoad(options) {
    this.setData({
      storeId: options.storeId || '',
      storeName: decodeURIComponent(options.storeName || ''),
      sessionId: options.sessionId || ''
    });
    this.loadTechnicianList();
    this.track('browse_tech');
  },

  loadTechnicianList() {
    this.setData({ loading: true });
    technicianApi.getTechnicianList({ storeId: this.data.storeId }).then(res => {
      this.setData({
        technicianList: res.data || [],
        loading: false
      });
    }).catch(() => {
      this.setData({ loading: false });
    });
  },

  onNoTechTap() {
    this.setData({
      selectedTechId: 0,
      selectedTechName: '不指定'
    });
  },

  onTechnicianTap(e) {
    const techId = e.currentTarget.dataset.id;
    const techName = e.currentTarget.dataset.name;
    const isFull = e.currentTarget.dataset.full;
    if (isFull) return;
    this.setData({
      selectedTechId: techId,
      selectedTechName: techName
    });
    this.track('select_tech', { techId });
  },

  onNextStep() {
    const { storeId, storeName, selectedTechId, selectedTechName, sessionId } = this.data;
    wx.navigateTo({
      url: `/pages/appointment/step3-datetime?storeId=${storeId}&storeName=${encodeURIComponent(storeName)}&techId=${selectedTechId}&techName=${encodeURIComponent(selectedTechName)}&sessionId=${sessionId}`
    });
  },

  track(event, extra) {
    appointmentApi.trackFunnel({
      sessionId: this.data.sessionId,
      step: 'step2',
      event,
      extra: extra || {}
    }).catch(() => {});
  }
});
