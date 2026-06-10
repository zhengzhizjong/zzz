import { get } from '@/utils/request'

export function getMyCards(memberId: number | string) {
  return get('/api/v1/trade/treatment-cards/my', { params: { memberId } })
}

export function getCardUsageHistory(cardId: number | string) {
  return get(`/api/v1/trade/treatment-cards/${cardId}/usage-history`)
}
