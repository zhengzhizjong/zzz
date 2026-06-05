const appointmentApi = require('../../api/appointment');

Page({
  data: {
    tabs: [
      { key: 'pending', name: '待服务' },
      { key: 'completed', name: '已完成' },
      { key: 'cancelled', name: '已取消' }
    ],
    currentTab: 'pending',
    appointments: [],
    loading: false,
    page: 1,
    hasMore: true,
    showModifyPopup: false,
    showCancelPopup: false,
    currentAppointment: null
  },

  onShow() {
    this.loadAppointments(true);
  },

  onTabTap(e) {
    const key = e.currentTarget.dataset.key;
    if (key === this.data.currentTab) return;
    this.setData({ currentTab: key });
    this.loadAppointments(true);
  },

  onPullDownRefresh() {
    this.loadAppointments(true).then(() => {
      wx.stopPullDownRefresh();
    }).catch(() => {
      wx.stopPullDownRefresh();
    });
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadAppointments(false);
    }
  },

  loadAppointments(reset) {
    if (this.data.loading) return Promise.resolve();

    const page = reset ? 1 : this.data.page;
    this.setData({ loading: true });

    return appointmentApi.getMyAppointments({
      status: this.data.currentTab,
      page: page,
      pageSize: 10
    }).then(res => {
      const list = res.data && res.data.list ? res.data.list : [];
      const total = res.data && res.data.total ? res.data.total : 0;
      this.setData({
        appointments: reset ? list : this.data.appointments.concat(list),
        page: page + 1,
        hasMore: (reset ? list.length : this.data.appointments.concat(list).length) < total,
        loading: false
      });
    }).catch(() => {
      this.setData({ loading: false });
    });
  },

  onModifyTap(e) {
    const item = e.currentTarget.dataset.item;
    this.setData({
      currentAppointment: item,
      showModifyPopup: true
    });
  },

  onCancelTap(e) {
    const item = e.currentTarget.dataset.item;
    this.setData({
      currentAppointment: item,
      showCancelPopup: true
    });
  },

  onModifyClose() {
    this.setData({ showModifyPopup: false, currentAppointment: null });
  },

  onCancelClose() {
    this.setData({ showCancelPopup: false, currentAppointment: null });
  },

  onRefresh() {
    this.setData({
      showModifyPopup: false,
      showCancelPopup: false,
      currentAppointment: null
    });
    this.loadAppointments(true);
  }
});
