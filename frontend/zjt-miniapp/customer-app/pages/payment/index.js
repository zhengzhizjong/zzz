const paymentApi = require('../../api/payment')

Page({
  data: {
    orderId: '',
    orderInfo: null,
    loading: true,
    paying: false
  },

  onLoad(options) {
    if (options.orderId) {
      this.setData({ orderId: options.orderId })
      this.loadOrder(options.orderId)
    }
  },

  loadOrder(orderId) {
    this.setData({ loading: true })
    paymentApi.getOrder(orderId).then(res => {
      this.setData({
        orderInfo: res.data || null,
        loading: false
      })
    }).catch(() => {
      this.setData({ loading: false })
    })
  },

  handlePay() {
    const { orderId, paying } = this.data
    if (paying) return

    this.setData({ paying: true })
    paymentApi.createPayment(orderId, 'wechat').then(res => {
      const payParams = res.data
      wx.requestPayment({
        timeStamp: payParams.timeStamp,
        nonceStr: payParams.nonceStr,
        package: payParams.package,
        signType: payParams.signType || 'MD5',
        paySign: payParams.paySign,
        success: () => {
          wx.showToast({ title: '支付成功', icon: 'success' })
          setTimeout(() => {
            wx.redirectTo({ url: `/pages/my-appointment/list` })
          }, 1000)
        },
        fail: (err) => {
          if (err.errMsg === 'requestPayment:fail cancel') {
            wx.showToast({ title: '已取消支付', icon: 'none' })
          } else {
            wx.showModal({
              title: '支付失败',
              content: '支付未成功，请重试',
              confirmText: '重试',
              cancelText: '取消',
              success: (modalRes) => {
                if (modalRes.confirm) {
                  this.handlePay()
                }
              }
            })
          }
        }
      })
    }).catch(() => {}).finally(() => {
      this.setData({ paying: false })
    })
  }
})
