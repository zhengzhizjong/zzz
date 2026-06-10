import { get } from '@/utils/request'

export function getStoreDetail(id: number | string) {
  return get(`/api/v1/store/stores/${id}`)
}

export function getStoreList(params?: Record<string, any>) {
  return get('/api/v1/store/stores', { params })
}
