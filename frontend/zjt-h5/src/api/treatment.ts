import { get, post } from '@/utils/request'

export function getMyCards(memberId: number | string) {
  return get('/api/v1/trade/treatment-cards/my', { params: { memberId } })
}

export function getCardUsageHistory(cardId: number | string) {
  return get(`/api/v1/trade/treatment-cards/${cardId}/usage-history`)
}

export function getTreatmentCards(params?: Record<string, any>) {
  return get('/api/v1/trade/treatment-cards', { params })
}

export function buyTreatmentCard(cardId: number) {
  return post('/api/v1/trade/treatment-cards/buy', { cardId })
}
