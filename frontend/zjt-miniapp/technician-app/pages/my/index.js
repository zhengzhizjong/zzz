const { checkLogin } = require('../../utils/auth')

Page({
  data: {
    isLogin: false,
    userInfo: null
  },

  onShow() {
    this.setData({ isLogin: checkLogin() })
  }
})
