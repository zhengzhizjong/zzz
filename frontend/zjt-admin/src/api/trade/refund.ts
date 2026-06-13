import request from '@/utils/request'
import type { R, PageResult } from '@/types/api'

export interface RefundInfo {
  id: number
  refundNo: string
  orderId: number
  orderNo: string
  customerName: string
  refundAmount: number
  refundReason: string
  refundStatus: number
  createdAt: string
}

export function getRefundList(params: {
  page: number
  pageSize: number
  refundStatus?: number
  keyword?: string
}) {
  return request.get<R<PageResult<RefundInfo>>>('/api/v1/trade/refunds', { params })
}

export function approveRefund(id: number) {
  return request.put<R<void>>(`/api/v1/trade/refunds/${id}/approve`)
}

export function rejectRefund(id: number, data: { rejectReason: string }) {
  return request.put<R<void>>(`/api/v1/trade/refunds/${id}/reject`, data)
}
