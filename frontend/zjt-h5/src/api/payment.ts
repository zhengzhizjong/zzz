import { get, post } from '@/utils/request'

export function createPayment(orderId: number | string, channel: string) {
  return post('/api/v1/trade/payments/create', { orderId, channel })
}

export function getOrder(orderId: number | string) {
  return get(`/api/v1/trade/orders/${orderId}`)
}
