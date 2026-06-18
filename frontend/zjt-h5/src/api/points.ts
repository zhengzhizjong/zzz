import { get, post } from '@/utils/request'

export function getPointsBalance() {
  return get('/api/v1/user/members/points')
}

export function getPointsMallItems(params?: Record<string, any>) {
  return get('/api/v1/content/points-goods', { params })
}

export function exchangePointsGoods(goodsId: number) {
  return post('/api/v1/content/points-exchange', { goodsId })
}

export function getPointsHistory(params?: Record<string, any>) {
  return get('/api/v1/user/members/points/history', { params })
}
