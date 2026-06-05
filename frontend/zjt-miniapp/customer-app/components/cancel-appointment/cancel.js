const appointmentApi = require('../../api/appointment');

Component({
  properties: {
    visible: {
      type: Boolean,
      value: false
    },
    appointment: {
      type: Object,
      value: null
    }
  },

  data: {
    reasonOptions: ['计划变更', '时间冲突', '身体不适', '其他'],
    selectedReason: '',
    otherReason: '',
    submitting: false
  },

  observers: {
    'visible': function (val) {
      if (val) {
        this.setData({
          selectedReason: '',
          otherReason: '',
          submitting: false
        });
      }
    }
  },

  methods: {
    onReasonTap(e) {
      const reason = e.currentTarget.dataset.reason;
      this.setData({
        selectedReason: reason,
        otherReason: reason === '其他' ? this.data.otherReason : ''
      });
    },

    onOtherReasonInput(e) {
      this.setData({ otherReason: e.detail.value });
    },

    onSubmit() {
      if (this.data.submitting) return;

      const { selectedReason, otherReason } = this.data;
      const appointment = this.data.appointment;

      let cancelReason = selectedReason;
      if (selectedReason === '其他') {
        cancelReason = otherReason.trim();
      }

      if (!cancelReason) {
        wx.showToast({ title: '请选择或填写取消原因', icon: 'none' });
        return;
      }

      wx.showModal({
        title: '确认取消',
        content: '取消后时段将释放，确定取消？',
        success: (res) => {
          if (!res.confirm) return;

          this.setData({ submitting: true });
          appointmentApi.cancelAppointment(appointment.id, {
            reason: cancelReason
          }).then(() => {
            wx.showToast({ title: '已取消', icon: 'success' });
            this.triggerEvent('refresh');
          }).catch(() => {
            this.setData({ submitting: false });
          });
        }
      });
    },

    onClose() {
      this.triggerEvent('close');
    },

    onMaskTap() {
      this.onClose();
    },

    preventBubble() {
      // 阻止冒泡，点击弹窗内容区不关闭
    }
  }
});
