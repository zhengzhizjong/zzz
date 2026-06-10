import { get, post } from '@/utils/request'

export function createPayment(orderId: number | string, channel: string) {
  return post('/api/v1/trade/payments/create', null, { params: { orderId, channel } })
}

export function getPaymentByOrder(orderId: number | string) {
  return get(`/api/v1/trade/payments/order/${orderId}`)
}
