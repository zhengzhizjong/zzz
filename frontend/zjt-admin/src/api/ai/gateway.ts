import request from '@/utils/request'
import type { R } from '@/types/api'

export function getProviders() {
  return request.get<R<any>>('/api/v1/ai/gateway/providers')
}

export function getUsage(params: any) {
  return request.get<R<any>>('/api/v1/ai/gateway/usage', { params })
}
