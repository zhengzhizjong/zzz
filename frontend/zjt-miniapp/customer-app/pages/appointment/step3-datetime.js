const appointmentApi = require('../../api/appointment');

Page({
  data: {
    storeId: '',
    storeName: '',
    techId: 0,
    techName: '',
    sessionId: '',
    dates: [],
    selectedDate: '',
    timeSlots: [],
    selectedTimeSlot: '',
    loading: true
  },

  onLoad(options) {
    this.setData({
      storeId: options.storeId || '',
      storeName: decodeURIComponent(options.storeName || ''),
      techId: parseInt(options.techId) || 0,
      techName: decodeURIComponent(options.techName || ''),
      sessionId: options.sessionId || ''
    });
    this.initDates();
    this.track('browse_time');
  },

  initDates() {
    const dates = [];
    const weekDays = ['日', '一', '二', '三', '四', '五', '六'];
    const now = new Date();
    for (let i = 0; i < 7; i++) {
      const d = new Date(now);
      d.setDate(now.getDate() + i);
      const month = (d.getMonth() + 1).toString().padStart(2, '0');
      const day = d.getDate().toString().padStart(2, '0');
      dates.push({
        dateStr: `${d.getFullYear()}-${month}-${day}`,
        display: `${month}/${day}`,
        weekDay: i === 0 ? '今天' : `周${weekDays[d.getDay()]}`,
        isPast: false
      });
    }
    const selectedDate = dates.length > 0 ? dates[0].dateStr : '';
    this.setData({ dates, selectedDate });
    if (selectedDate) {
      this.loadTimeSlots(selectedDate);
    }
  },

  onDateTap(e) {
    const date = e.currentTarget.dataset.date;
    if (this.data.selectedDate === date) return;
    this.setData({
      selectedDate: date,
      selectedTimeSlot: '',
      timeSlots: []
    });
    this.loadTimeSlots(date);
  },

  loadTimeSlots(date) {
    this.setData({ loading: true });
    appointmentApi.getAvailableSlots({
      storeId: this.data.storeId,
      technicianId: this.data.techId,
      date: date
    }).then(res => {
      this.setData({
        timeSlots: res.data || [],
        loading: false
      });
    }).catch(() => {
      this.setData({ loading: false });
    });
  },

  onSlotTap(e) {
    const slot = e.currentTarget.dataset.slot;
    const occupied = e.currentTarget.dataset.occupied;
    if (occupied) return;

    this.setData({ selectedTimeSlot: slot });
    this.track('select_time', { date: this.data.selectedDate, timeSlot: slot });

    // 锁定时段
    wx.showLoading({ title: '锁定时段中...' });
    appointmentApi.lockTemp({
      storeId: this.data.storeId,
      techId: this.data.techId,
      date: this.data.selectedDate,
      timeSlot: slot
    }).then(res => {
      wx.hideLoading();
      const lockId = res.data.lockId || '';
      const expireAt = res.data.expireAt || '';
      const { storeId, storeName, techId, techName, selectedDate, sessionId } = this.data;
      wx.navigateTo({
        url: `/pages/appointment/step4-confirm?storeId=${storeId}&storeName=${encodeURIComponent(storeName)}&techId=${techId}&techName=${encodeURIComponent(techName)}&date=${selectedDate}&timeSlot=${encodeURIComponent(slot)}&lockId=${lockId}&expireAt=${encodeURIComponent(expireAt)}&sessionId=${sessionId}`
      });
    }).catch(() => {
      wx.hideLoading();
      wx.showToast({ title: '时段锁定失败，请重试', icon: 'none' });
    });
  },

  track(event, extra) {
    appointmentApi.trackFunnel({
      sessionId: this.data.sessionId,
      step: 'step3',
      event,
      extra: extra || {}
    }).catch(() => {});
  }
});
