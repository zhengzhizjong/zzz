const userApi = require('../../api/user')
const { setToken, checkLogin } = require('../../utils/auth')

Page({
  data: {
    employeeNo: '',
    password: '',
    loading: false,
    showPassword: false
  },

  onLoad() {
    if (checkLogin()) {
      wx.switchTab({ url: '/pages/workspace/index' })
    }
  },

  onEmployeeNoInput(e) {
    this.setData({ employeeNo: e.detail.value })
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  togglePassword() {
    this.setData({ showPassword: !this.data.showPassword })
  },

  handleLogin() {
    const { employeeNo, password } = this.data
    if (!employeeNo) {
      wx.showToast({ title: '请输入工号', icon: 'none' })
      return
    }
    if (!password) {
      wx.showToast({ title: '请输入密码', icon: 'none' })
      return
    }

    this.setData({ loading: true })
    userApi.employeeLogin(employeeNo, password).then(res => {
      const token = res.data.token
      setToken(token)
      if (res.data.techId) {
        wx.setStorageSync('techId', res.data.techId)
      }
      wx.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => {
        wx.switchTab({ url: '/pages/workspace/index' })
      }, 500)
    }).catch(() => {}).finally(() => {
      this.setData({ loading: false })
    })
  }
})
