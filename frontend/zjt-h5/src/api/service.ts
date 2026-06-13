import { get } from '@/utils/request'

export function getServiceItemList(params?: Record<string, any>) {
  return get('/api/v1/store/services', { params })
}

export function getServiceItemDetail(id: number | string) {
  return get(`/api/v1/store/services/${id}`)
}
