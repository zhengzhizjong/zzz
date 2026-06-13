import request from '@/utils/request'
import type { R, PageResult } from '@/types/api'

export interface OrderInfo {
  id: number
  orderNo: string
  memberName: string
  memberPhone: string
  storeName: string
  totalAmount: number
  discountAmount: number
  paidAmount: number
  paymentMethod: number
  paymentStatus: number
  paymentTime: string
  createdAt: string
}

export function getOrderList(params: {
  page: number
  pageSize: number
  storeId?: number
  status?: number
  startDate?: string
  endDate?: string
  keyword?: string
}) {
  return request.get<R<PageResult<OrderInfo>>>('/api/v1/trade/orders', { params })
}

export function getOrderDetail(id: number) {
  return request.get<R<OrderInfo>>(`/api/v1/trade/orders/${id}`)
}

export function completeOrder(id: number) {
  return request.put<R<void>>(`/api/v1/trade/orders/${id}/complete`)
}

export function cancelOrder(id: number) {
  return request.put<R<void>>(`/api/v1/trade/orders/${id}/cancel`)
}
