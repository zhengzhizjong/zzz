const technicianApi = require('../../api/technician')
const { checkLogin } = require('../../utils/auth')

Page({
  data: {
    technicianInfo: null,
    isOnline: false,
    currentService: null,
    pendingAppointments: [],
    loading: true
  },

  onLoad() {
    if (!checkLogin()) return
    this.loadWorkspace()
  },

  onShow() {
    if (checkLogin()) {
      this.loadWorkspace()
    }
  },

  onPullDownRefresh() {
    this.loadWorkspace().then(() => {
      wx.stopPullDownRefresh()
    })
  },

  loadWorkspace() {
    const techId = wx.getStorageSync('techId') || ''
    if (!techId) {
      this.setData({ loading: false })
      return Promise.resolve()
    }
    return technicianApi.getWorkspace(techId).then(res => {
      const data = res.data || {}
      this.setData({
        technicianInfo: data.technicianInfo || null,
        isOnline: data.isOnline || false,
        currentService: data.currentService || null,
        pendingAppointments: (data.pendingAppointments || []).slice(0, 5),
        loading: false
      })
    }).catch(() => {
      this.setData({ loading: false })
    })
  },

  onCheckIn() {
    const techId = wx.getStorageSync('techId') || ''
    if (!techId) return
    wx.showLoading({ title: '签到中...' })
    technicianApi.checkIn(techId).then(res => {
      wx.hideLoading()
      wx.showToast({ title: '签到成功', icon: 'success' })
      this.setData({ isOnline: true })
    }).catch(() => {
      wx.hideLoading()
    })
  },

  onCheckOut() {
    const techId = wx.getStorageSync('techId') || ''
    if (!techId) return
    wx.showModal({
      title: '确认签退',
      content: '签退后将无法接收新订单，确认签退？',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '签退中...' })
          technicianApi.checkOut(techId).then(() => {
            wx.hideLoading()
            wx.showToast({ title: '签退成功', icon: 'success' })
            this.setData({ isOnline: false })
          }).catch(() => {
            wx.hideLoading()
          })
        }
      }
    })
  },

  onAppointmentTap(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/appointment-detail/index?id=${id}` })
  }
})
