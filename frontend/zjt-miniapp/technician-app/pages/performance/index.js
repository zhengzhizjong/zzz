const technicianApi = require('../../api/technician')
const { checkLogin } = require('../../utils/auth')

Page({
  data: {
    monthRevenue: 0,
    monthTarget: 0,
    targetCompletion: 0,
    monthServiceCount: 0,
    avgPrice: 0,
    monthRating: 0,
    rank: 0,
    loading: true
  },

  onLoad() {
    if (!checkLogin()) return
    this.loadPerformance()
  },

  onShow() {
    if (checkLogin()) {
      this.loadPerformance()
    }
  },

  onPullDownRefresh() {
    this.loadPerformance().then(() => {
      wx.stopPullDownRefresh()
    })
  },

  loadPerformance() {
    const techId = wx.getStorageSync('techId') || ''
    if (!techId) {
      this.setData({ loading: false })
      return Promise.resolve()
    }
    return technicianApi.getPerformance(techId).then(res => {
      const data = res.data || {}
      const monthRevenue = data.monthRevenue || 0
      const monthTarget = data.monthTarget || 0
      const targetCompletion = monthTarget > 0 ? Math.min(Math.round(monthRevenue / monthTarget * 100), 100) : 0
      this.setData({
        monthRevenue,
        monthTarget,
        targetCompletion,
        monthServiceCount: data.monthServiceCount || 0,
        avgPrice: data.avgPrice || 0,
        monthRating: data.monthRating || 0,
        rank: data.rank || 0,
        loading: false
      })
    }).catch(() => {
      this.setData({ loading: false })
    })
  }
})
