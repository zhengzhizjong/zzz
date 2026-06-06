const userApi = require('../../api/user')

Page({
  data: {
    nickname: '',
    realName: '',
    gender: 0,
    birthday: '',
    phone: '',
    avatarUrl: '',
    genderOptions: ['未设置', '男', '女'],
    submitting: false
  },

  onLoad() {
    this.loadProfile()
  },

  loadProfile() {
    wx.showLoading({ title: '加载中' })
    userApi.getProfile().then(res => {
      wx.hideLoading()
      const data = res.data || {}
      this.setData({
        nickname: data.nickname || '',
        realName: data.realName || '',
        gender: data.gender || 0,
        birthday: data.birthday || '',
        phone: data.phone || '',
        avatarUrl: data.avatar || ''
      })
    }).catch(() => {
      wx.hideLoading()
    })
  },

  onChooseAvatar() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        this.setData({ avatarUrl: res.tempFilePaths[0] })
      }
    })
  },

  onNicknameInput(e) {
    this.setData({ nickname: e.detail.value })
  },

  onRealNameInput(e) {
    this.setData({ realName: e.detail.value })
  },

  onGenderChange(e) {
    this.setData({ gender: Number(e.detail.value) })
  },

  onBirthdayChange(e) {
    this.setData({ birthday: e.detail.value })
  },

  onSubmit() {
    const { nickname, realName, gender, birthday, avatarUrl, submitting } = this.data
    if (submitting) return

    if (!nickname.trim()) {
      wx.showToast({ title: '请输入昵称', icon: 'none' })
      return
    }

    this.setData({ submitting: true })
    userApi.updateProfile({
      nickname: nickname.trim(),
      realName: realName.trim(),
      gender,
      birthday,
      avatar: avatarUrl
    }).then(() => {
      wx.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    }).catch(() => {
      this.setData({ submitting: false })
    })
  }
})
