import request from '@/utils/request'
import type { R } from '@/types/api'

export interface PromotionFunnelData {
  period: { startDate: string; endDate: string }
  funnel: Record<string, { count: number; rate: number }>
  conversion: Record<string, number>
  byChannel?: Array<{
    channel: string
    click: number
    register: number
    firstVisit: number
    firstOrder: number
    repurchase: number
  }>
}

export function getPromotionFunnel(params: { technicianId?: number; storeId?: number; startDate?: string; endDate?: string }) {
  return request.get<R<PromotionFunnelData>>('/api/v1/integration/promotion/funnel', { params })
}
