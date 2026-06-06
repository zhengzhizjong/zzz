import { get } from '@/utils/request'

export function getMyCards() {
  return get('/api/v1/trade/treatment-cards/my')
}

export function getCardUsageHistory(cardId: number | string) {
  return get(`/api/v1/trade/treatment-cards/${cardId}/usage`)
}
