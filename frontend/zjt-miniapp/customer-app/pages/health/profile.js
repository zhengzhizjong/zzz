const userApi = require('../../api/user')

const CONSTITUTION_MAP = {
  'pinghe': { name: '平和质', color: '#07C160' },
  'qixu': { name: '气虚质', color: '#E6A23C' },
  'yangxu': { name: '阳虚质', color: '#F56C6C' },
  'yinxu': { name: '阴虚质', color: '#909399' },
  'tanshi': { name: '痰湿质', color: '#409EFF' },
  'shire': { name: '湿热质', color: '#FA5151' },
  'xueyu': { name: '血瘀质', color: '#9B59B6' },
  'qiyu': { name: '气郁质', color: '#1ABC9C' },
  'tebing': { name: '特禀质', color: '#E67E22' }
}

Page({
  data: {
    constitutionType: '',
    constitutionName: '',
    constitutionColor: '#909399',
    allergies: '',
    contraindications: '',
    medicalHistory: '',
    medication: '',
    notes: '',
    submitting: false
  },

  onLoad() {
    this.loadHealthProfile()
  },

  loadHealthProfile() {
    wx.showLoading({ title: '加载中' })
    userApi.getHealthProfile().then(res => {
      wx.hideLoading()
      const data = res.data || {}
      const constitutionInfo = CONSTITUTION_MAP[data.constitutionType] || {}
      this.setData({
        constitutionType: data.constitutionType || '',
        constitutionName: constitutionInfo.name || data.constitutionName || '未辨识',
        constitutionColor: constitutionInfo.color || '#909399',
        allergies: data.allergies || '',
        contraindications: data.contraindications || '',
        medicalHistory: data.medicalHistory || '',
        medication: data.medication || '',
        notes: data.notes || ''
      })
    }).catch(() => {
      wx.hideLoading()
    })
  },

  onAllergiesInput(e) {
    this.setData({ allergies: e.detail.value })
  },

  onContraindicationsInput(e) {
    this.setData({ contraindications: e.detail.value })
  },

  onMedicalHistoryInput(e) {
    this.setData({ medicalHistory: e.detail.value })
  },

  onMedicationInput(e) {
    this.setData({ medication: e.detail.value })
  },

  onNotesInput(e) {
    this.setData({ notes: e.detail.value })
  },

  onSubmit() {
    const { allergies, contraindications, medicalHistory, medication, notes, submitting } = this.data
    if (submitting) return

    this.setData({ submitting: true })
    userApi.updateHealthProfile({
      allergies,
      contraindications,
      medicalHistory,
      medication,
      notes
    }).then(() => {
      wx.showToast({ title: '保存成功', icon: 'success' })
      this.setData({ submitting: false })
    }).catch(() => {
      this.setData({ submitting: false })
    })
  },

  onGoAiDiagnosis() {
    wx.navigateTo({ url: '/pages/health/ai-diagnosis' })
  }
})
