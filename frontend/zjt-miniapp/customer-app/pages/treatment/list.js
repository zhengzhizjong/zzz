const treatmentApi = require('../../api/treatment')

Page({
  data: {
    cards: [],
    loading: false,
    showUsagePopup: false,
    currentCard: null,
    usageHistory: []
  },

  onLoad() {
    this.loadCards()
  },

  onShow() {
    this.loadCards()
  },

  loadCards() {
    this.setData({ loading: true })
    treatmentApi.getMyCards().then(res => {
      const cards = (res.data || []).map(card => {
        const total = card.totalCount || 0
        const remaining = card.remainingCount || 0
        const used = total - remaining
        const progress = total > 0 ? Math.round((used / total) * 100) : 0
        return { ...card, used, progress }
      })
      this.setData({ cards, loading: false })
    }).catch(() => {
      this.setData({ loading: false })
    })
  },

  onCardTap(e) {
    const card = e.currentTarget.dataset.item
    this.setData({ currentCard: card, showUsagePopup: true })
    this.loadUsageHistory(card.id)
  },

  loadUsageHistory(cardId) {
    treatmentApi.getCardUsageHistory(cardId).then(res => {
      this.setData({ usageHistory: res.data || [] })
    }).catch(() => {
      this.setData({ usageHistory: [] })
    })
  },

  onClosePopup() {
    this.setData({ showUsagePopup: false, currentCard: null, usageHistory: [] })
  }
})
