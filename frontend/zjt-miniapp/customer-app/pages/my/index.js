const { checkLogin, getToken } = require('../../utils/auth')
const userApi = require('../../api/user')

Page({
  data: {
    isLogin: false,
    userInfo: null
  },

  onShow() {
    const isLogin = checkLogin()
    this.setData({ isLogin })
    if (isLogin) {
      this.loadProfile()
    }
  },

  loadProfile() {
    userApi.getProfile().then(res => {
      this.setData({ userInfo: res.data || null })
    }).catch(() => {})
  },

  goLogin() {
    wx.navigateTo({ url: '/pages/login/index' })
  },

  goAppointment() {
    if (!checkLogin()) {
      wx.navigateTo({ url: '/pages/login/index' })
      return
    }
    wx.navigateTo({ url: '/pages/my-appointment/list' })
  },

  handleLogout() {
    wx.showModal({
      title: '提示',
      content: '确认退出登录？',
      success: (res) => {
        if (res.confirm) {
          const { removeToken } = require('../../utils/auth')
          removeToken()
          this.setData({ isLogin: false, userInfo: null })
          wx.showToast({ title: '已退出登录', icon: 'success' })
        }
      }
    })
  }
})
