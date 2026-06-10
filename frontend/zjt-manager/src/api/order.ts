import { get, post, put } from '@/utils/request'

export function getOrderList(params?: Record<string, any>) {
  return get('/api/v1/trade/orders', { params })
}

export function getOrderDetail(id: number | string) {
  return get(`/api/v1/trade/orders/${id}`)
}

export function createOrderFromAppointment(appointmentId: number | string) {
  return post(`/api/v1/trade/orders/from-appointment/${appointmentId}`)
}

export function completeOrder(id: number | string) {
  return put(`/api/v1/trade/orders/${id}/complete`)
}

export function cancelOrder(id: number | string) {
  return put(`/api/v1/trade/orders/${id}/cancel`)
}

export function refundOrder(id: number | string, data?: Record<string, any>) {
  return put(`/api/v1/trade/orders/${id}/cancel`, data)
}
