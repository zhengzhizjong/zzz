const userApi = require('../../api/user')
const { setToken, checkLogin } = require('../../utils/auth')

Page({
  data: {
    phone: '',
    verifyCode: '',
    countdown: 0,
    loading: false,
    wxLoading: false
  },

  onLoad() {
    if (checkLogin()) {
      wx.switchTab({ url: '/pages/index/index' })
    }
  },

  onPhoneInput(e) {
    this.setData({ phone: e.detail.value })
  },

  onCodeInput(e) {
    this.setData({ verifyCode: e.detail.value })
  },

  sendCode() {
    const { phone, countdown } = this.data
    if (countdown > 0) return
    if (!phone || !/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }

    userApi.sendVerifyCode(phone).then(() => {
      wx.showToast({ title: '验证码已发送', icon: 'success' })
      this.startCountdown()
    }).catch(() => {})
  },

  startCountdown() {
    this.setData({ countdown: 60 })
    this._timer = setInterval(() => {
      if (this.data.countdown <= 1) {
        clearInterval(this._timer)
        this.setData({ countdown: 0 })
      } else {
        this.setData({ countdown: this.data.countdown - 1 })
      }
    }, 1000)
  },

  onUnload() {
    if (this._timer) clearInterval(this._timer)
  },

  handleLogin() {
    const { phone, verifyCode } = this.data
    if (!phone || !/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }
    if (!verifyCode) {
      wx.showToast({ title: '请输入验证码', icon: 'none' })
      return
    }

    this.setData({ loading: true })
    userApi.login(phone, verifyCode).then(res => {
      const token = res.data.token
      setToken(token)
      wx.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => {
        wx.switchTab({ url: '/pages/index/index' })
      }, 500)
    }).catch(() => {}).finally(() => {
      this.setData({ loading: false })
    })
  },

  handleWechatLogin() {
    this.setData({ wxLoading: true })
    wx.login({
      success: (res) => {
        if (res.code) {
          userApi.wechatLogin(res.code).then(loginRes => {
            const token = loginRes.data.token
            setToken(token)
            wx.showToast({ title: '登录成功', icon: 'success' })
            setTimeout(() => {
              wx.switchTab({ url: '/pages/index/index' })
            }, 500)
          }).catch(() => {}).finally(() => {
            this.setData({ wxLoading: false })
          })
        } else {
          wx.showToast({ title: '微信登录失败', icon: 'none' })
          this.setData({ wxLoading: false })
        }
      },
      fail: () => {
        wx.showToast({ title: '微信登录失败', icon: 'none' })
        this.setData({ wxLoading: false })
      }
    })
  }
})
