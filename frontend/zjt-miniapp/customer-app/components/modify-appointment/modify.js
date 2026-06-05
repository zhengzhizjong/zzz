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
    dates: [],
    newDate: '',
    availableSlots: [],
    newTimeSlot: '',
    newTechId: 0,
    newTechName: '',
    modifyReason: '',
    submitting: false
  },

  observers: {
    'visible': function (val) {
      if (val && this.data.appointment) {
        this.initDates();
        this.setData({
          newDate: '',
          newTimeSlot: '',
          newTechId: 0,
          newTechName: '',
          modifyReason: '',
          availableSlots: [],
          submitting: false
        });
      }
    }
  },

  methods: {
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
          weekDay: i === 0 ? '今天' : `周${weekDays[d.getDay()]}`
        });
      }
      this.setData({ dates });
    },

    onDateChange(e) {
      const date = e.currentTarget.dataset.date;
      if (date === this.data.newDate) return;
      this.setData({
        newDate: date,
        newTimeSlot: '',
        availableSlots: []
      });
      this.loadAvailableSlots(date);
    },

    loadAvailableSlots(date) {
      const appointment = this.data.appointment;
      if (!appointment) return;
      appointmentApi.getAvailableSlots({
        storeId: appointment.storeId,
        techId: this.data.newTechId || appointment.techId || 0,
        date: date
      }).then(res => {
        this.setData({ availableSlots: res.data || [] });
      }).catch(() => {});
    },

    onSlotTap(e) {
      const slot = e.currentTarget.dataset.slot;
      const occupied = e.currentTarget.dataset.occupied;
      if (occupied) return;
      this.setData({ newTimeSlot: slot });
    },

    onTechChange(e) {
      const techId = e.currentTarget.dataset.id;
      const techName = e.currentTarget.dataset.name;
      this.setData({
        newTechId: techId,
        newTechName: techName,
        newTimeSlot: '',
        availableSlots: []
      });
      if (this.data.newDate) {
        this.loadAvailableSlots(this.data.newDate);
      }
    },

    onReasonInput(e) {
      this.setData({ modifyReason: e.detail.value });
    },

    onSubmit() {
      if (this.data.submitting) return;

      const { newDate, newTimeSlot, modifyReason, newTechId } = this.data;
      const appointment = this.data.appointment;

      if (!newDate) {
        wx.showToast({ title: '请选择新日期', icon: 'none' });
        return;
      }
      if (!newTimeSlot) {
        wx.showToast({ title: '请选择新时段', icon: 'none' });
        return;
      }
      if (!modifyReason.trim()) {
        wx.showToast({ title: '请填写修改原因', icon: 'none' });
        return;
      }

      wx.showModal({
        title: '确认修改',
        content: '修改后原时段将释放，确定修改？',
        success: (res) => {
          if (!res.confirm) return;

          this.setData({ submitting: true });
          appointmentApi.modifyAppointment(appointment.id, {
            date: newDate,
            timeSlot: newTimeSlot,
            techId: newTechId || undefined,
            reason: modifyReason.trim()
          }).then(() => {
            wx.showToast({ title: '修改成功', icon: 'success' });
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
