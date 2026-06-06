import request from '@/utils/request'
import type { R, PageResult } from '@/types/api'

export interface TreatmentCardInfo {
  id: number
  name: string
  projectName: string
  totalCount: number
  remainingCount: number
  memberName: string
  price: number
  status: number
  createdAt: string
}

export function getTreatmentCardList(params: any) {
  return request.get<R<PageResult<TreatmentCardInfo>>>('/api/v1/trade/treatment-cards', { params })
}

export function createTreatmentCard(data: any) {
  return request.post<R<void>>('/api/v1/trade/treatment-cards', data)
}

export function getTreatmentCardUsageHistory(cardId: number) {
  return request.get<R<any>>(`/api/v1/trade/treatment-cards/${cardId}/usage`)
}
