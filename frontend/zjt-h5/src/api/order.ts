import { get, post } from '@/utils/request'

export function getOrderList(params?: Record<string, any>) {
  return get('/api/v1/trade/orders', { params })
}

export function getOrderDetail(id: number | string) {
  return get(`/api/v1/trade/orders/${id}`)
}

export function createOrderFromAppointment(appointmentId: number | string) {
  return post(`/api/v1/trade/orders/from-appointment/${appointmentId}`)
}
