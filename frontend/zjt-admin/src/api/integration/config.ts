import request from '@/utils/request'
import type { R, PageResult } from '@/types/api'

export interface ConfigInfo {
  id: number
  platform: string
  appKey: string
  appSecret: string
  callbackUrl: string
  status: number
  lastSyncTime: string
  createdAt: string
}

export function getConfigList(params: any) {
  return request.get<R<PageResult<ConfigInfo>>>('/api/v1/integration/configs', { params })
}

export function createConfig(data: any) {
  return request.post<R<void>>('/api/v1/integration/configs', data)
}

export function updateConfig(id: number, data: any) {
  return request.put<R<void>>(`/api/v1/integration/configs/${id}`, data)
}
