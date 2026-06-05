const promotionApi = require('../../api/promotion')
const { checkLogin } = require('../../utils/auth')

Page({
  data: {
    promotionInfo: null,
    stats: null,
    commissions: [],
    ranking: [],
    activeTab: 'promotion',
    posterUrl: '',
    loading: true
  },

  onLoad() {
    if (!checkLogin()) return
    this.loadPromotion()
  },

  onShow() {
    if (checkLogin()) {
      this.loadPromotion()
    }
  },

  onPullDownRefresh() {
    this.loadPromotion().then(() => {
      wx.stopPullDownRefresh()
    })
  },

  getTechId() {
    return wx.getStorageSync('techId') || ''
  },

  loadPromotion() {
    const techId = this.getTechId()
    if (!techId) {
      this.setData({ loading: false })
      return Promise.resolve()
    }
    this.setData({ loading: true })
    return promotionApi.getPromotion(techId).then(res => {
      const data = res.data || {}
      this.setData({
        promotionInfo: data.promotionInfo || null,
        stats: data.stats || null,
        loading: false
      })
      this.loadTabData()
    }).catch(() => {
      this.setData({ loading: false })
    })
  },

  loadTabData() {
    const techId = this.getTechId()
    if (!techId) return
    const tab = this.data.activeTab
    if (tab === 'promotion') {
      promotionApi.getStats(techId).then(res => {
        this.setData({ stats: res.data || null })
      }).catch(() => {})
    } else if (tab === 'commission') {
      promotionApi.getCommissions(techId).then(res => {
        this.setData({ commissions: (res.data || {}).list || [] })
      }).catch(() => {})
    } else if (tab === 'ranking') {
      promotionApi.getRanking(techId).then(res => {
        this.setData({ ranking: (res.data || {}).list || [] })
      }).catch(() => {})
    }
  },

  onTabChange(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({ activeTab: tab })
    this.loadTabData()
  },

  onGeneratePoster() {
    const techId = this.getTechId()
    if (!techId) return
    wx.showLoading({ title: '生成海报中...' })
    promotionApi.generatePoster(techId).then(res => {
      wx.hideLoading()
      const data = res.data || {}
      this.setData({ posterUrl: data.posterUrl || '' })
      if (data.posterUrl) {
        wx.previewImage({
          urls: [data.posterUrl],
          current: data.posterUrl
        })
      }
    }).catch(() => {
      wx.hideLoading()
    })
  },

  onSavePoster() {
    const posterUrl = this.data.posterUrl
    if (!posterUrl) {
      wx.showToast({ title: '请先生成海报', icon: 'none' })
      return
    }
    wx.downloadFile({
      url: posterUrl,
      success(res) {
        if (res.statusCode === 200) {
          wx.saveImageToPhotosAlbum({
            filePath: res.tempFilePath,
            success() {
              wx.showToast({ title: '已保存到相册', icon: 'success' })
            },
            fail() {
              wx.showToast({ title: '保存失败', icon: 'none' })
            }
          })
        }
      },
      fail() {
        wx.showToast({ title: '下载失败', icon: 'none' })
      }
    })
  },

  onShare() {
    // 触发微信分享
  },

  onShareAppMessage() {
    const info = this.data.promotionInfo || {}
    return {
      title: '忠济堂·中医养生',
      path: `/pages/index/index?inviteCode=${info.promoCode || ''}`,
      imageUrl: this.data.posterUrl || ''
    }
  }
})
