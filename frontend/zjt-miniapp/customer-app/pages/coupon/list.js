const couponApi = require('../../api/coupon')

Page({
  data: {
    tabs: [
      { key: 'available', name: '可用' },
      { key: 'used', name: '已使用' },
      { key: 'expired', name: '已过期' }
    ],
    currentTab: 'available',
    coupons: [],
    loading: false
  },

  onLoad() {
    this.loadCoupons()
  },

  onShow() {
    this.loadCoupons()
  },

  onTabTap(e) {
    const key = e.currentTarget.dataset.key
    if (key === this.data.currentTab) return
    this.setData({ currentTab: key })
    this.loadCoupons()
  },

  loadCoupons() {
    this.setData({ loading: true })
    couponApi.getMyCoupons(this.data.currentTab).then(res => {
      this.setData({
        coupons: res.data || [],
        loading: false
      })
    }).catch(() => {
      this.setData({ loading: false })
    })
  },

  onCouponTap(e) {
    const item = e.currentTarget.dataset.item
    wx.showModal({
      title: item.couponName || '优惠券详情',
      content: `优惠：${item.discountDisplay || ''}\n门槛：${item.thresholdDisplay || '无门槛'}\n有效期：${item.validityStart || ''} 至 ${item.validityEnd || ''}`,
      showCancel: false,
      confirmText: '知道了'
    })
  }
})
