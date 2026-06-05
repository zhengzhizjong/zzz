const appointmentApi = require('../../api/appointment');

Page({
  data: {
    storeId: '',
    storeName: '',
    techId: 0,
    techName: '',
    date: '',
    timeSlot: '',
    lockId: '',
    expireAt: '',
    sessionId: '',
    countdown: 300,
    countdownDisplay: '05:00',
    countdownWarning: false,
    serviceItems: [],
    selectedServiceItemId: '',
    submitting: false
  },

  _timer: null,

  onLoad(options) {
    this.setData({
      storeId: options.storeId || '',
      storeName: decodeURIComponent(options.storeName || ''),
      techId: parseInt(options.techId) || 0,
      techName: decodeURIComponent(options.techName || ''),
      date: options.date || '',
      timeSlot: decodeURIComponent(options.timeSlot || ''),
      lockId: options.lockId || '',
      expireAt: decodeURIComponent(options.expireAt || ''),
      sessionId: options.sessionId || ''
    });
    this.loadServiceItems();
    this.startCountdown();
    this.track('browse_confirm');
  },

  loadServiceItems() {
    // 加载服务项目列表（可从门店或通用接口获取）
    appointmentApi.getAvailableSlots({
      storeId: this.data.storeId,
      date: this.data.date
    }).then(res => {
      const items = res.data && res.data.serviceItems ? res.data.serviceItems : [];
      this.setData({ serviceItems: items });
    }).catch(() => {});
  },

  startCountdown() {
    // 如果有expireAt，计算剩余秒数
    if (this.data.expireAt) {
      const expireTime = new Date(this.data.expireAt).getTime();
      const now = Date.now();
      const remain = Math.max(0, Math.floor((expireTime - now) / 1000));
      this.setData({ countdown: remain });
    }

    this._timer = setInterval(() => {
      let countdown = this.data.countdown - 1;
      if (countdown <= 0) {
        countdown = 0;
        clearInterval(this._timer);
        this._timer = null;
        wx.showModal({
          title: '提示',
          content: '锁定已过期，请重新选择时段',
          showCancel: false,
          success: () => {
            wx.navigateBack({ delta: 2 });
          }
        });
      }
      const minutes = Math.floor(countdown / 60).toString().padStart(2, '0');
      const seconds = (countdown % 60).toString().padStart(2, '0');
      this.setData({
        countdown,
        countdownDisplay: `${minutes}:${seconds}`,
        countdownWarning: countdown < 60
      });
    }, 1000);
  },

  onServiceItemTap(e) {
    const itemId = e.currentTarget.dataset.id;
    this.setData({
      selectedServiceItemId: this.data.selectedServiceItemId === itemId ? '' : itemId
    });
  },

  onSubmit() {
    const { storeId, techId, date, timeSlot, lockId, submitting } = this.data;
    if (submitting) return;

    // 校验必填项
    if (!storeId) {
      wx.showToast({ title: '请选择门店', icon: 'none' });
      return;
    }
    if (!techId && techId !== 0) {
      wx.showToast({ title: '请选择技师', icon: 'none' });
      return;
    }
    if (!date) {
      wx.showToast({ title: '请选择日期', icon: 'none' });
      return;
    }
    if (!timeSlot) {
      wx.showToast({ title: '请选择时段', icon: 'none' });
      return;
    }
    if (!lockId) {
      wx.showToast({ title: '时段锁定已失效，请重新选择', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });

    appointmentApi.createAppointment({
      storeId,
      techId,
      date,
      timeSlot,
      lockId,
      serviceItemId: this.data.selectedServiceItemId || undefined
    }).then(() => {
      wx.showToast({ title: '预约成功', icon: 'success' });
      this.track('confirm', { storeId, techId, date, timeSlot });
      setTimeout(() => {
        wx.switchTab({ url: '/pages/my-appointment/list' });
      }, 1500);
    }).catch(() => {
      this.setData({ submitting: false });
    });
  },

  track(event, extra) {
    appointmentApi.trackFunnel({
      sessionId: this.data.sessionId,
      step: 'step4',
      event,
      extra: extra || {}
    }).catch(() => {});
  },

  onUnload() {
    if (this._timer) {
      clearInterval(this._timer);
      this._timer = null;
    }
  }
});
