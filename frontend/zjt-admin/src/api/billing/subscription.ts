import request from '@/utils/request'
import type { R } from '@/types/api'

export function getSubscription(tenantId: number) {
  return request.get<R<any>>(`/api/v1/admin/subscriptions/tenant/${tenantId}`)
}

export function subscribe(data: any) {
  return request.post<R<void>>('/api/v1/admin/subscriptions/subscribe', data)
}

export function renew(tenantId: number) {
  return request.post<R<void>>(`/api/v1/admin/subscriptions/renew/${tenantId}`)
}

export function cancel(tenantId: number) {
  return request.post<R<void>>(`/api/v1/admin/subscriptions/cancel/${tenantId}`)
}
