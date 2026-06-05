import request from '@/utils/request'
import type { R } from '@/types/api'

export interface FunnelStatistics {
  period: { startDate: string; endDate: string }
  funnel: Record<string, { count: number; rate: number }>
  conversion: Record<string, number>
  groups?: Array<{
    dimension: string
    dimensionValue: string
    [key: string]: any
  }>
}

export function getFunnelStatistics(params: { storeId?: number; startDate?: string; endDate?: string; groupBy?: string }) {
  return request.get<R<FunnelStatistics>>('/api/v1/data/appointment-funnel/statistics', { params })
}
