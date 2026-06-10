import { get } from '@/utils/request'

export function getStoreList(params?: any) {
  return get('/api/v1/store/stores', { params })
}

export function getStoreDetail(id: number | string) {
  return get(`/api/v1/store/stores/${id}`)
}
