import request from '@/utils/request'
import type { R, PageResult } from '@/types/api'

export interface ActivityInfo {
  id: number
  activityName: string
  activityType: number
  rulesJson: string
  startTime: string
  endTime: string
  status: number
  createdAt: string
}

export function getActivityList(params: any) {
  return request.get<R<PageResult<ActivityInfo>>>('/api/v1/trade/activities', { params })
}

export function createActivity(data: any) {
  return request.post<R<void>>('/api/v1/trade/activities', data)
}

export function updateActivity(id: number, data: any) {
  return request.put<R<void>>(`/api/v1/trade/activities/${id}`, data)
}

export function updateActivityStatus(id: number, status: number) {
  return request.put<R<void>>(`/api/v1/trade/activities/${id}/status`, { status })
}
