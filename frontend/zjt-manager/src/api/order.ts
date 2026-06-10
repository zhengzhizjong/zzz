import request from '@/utils/request'

export function getOrderList(params?: Record<string, any>) {
  return request.get('/orders', { params })
}

export function getOrderDetail(id: string) {
  return request.get(`/orders/${id}`)
}

export function refundOrder(id: string, data?: Record<string, any>) {
  return request.post(`/orders/${id}/refund`, data)
}
