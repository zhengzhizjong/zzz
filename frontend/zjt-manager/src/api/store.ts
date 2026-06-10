import request from '@/utils/request'

export function getStoreDetail(storeId: string) {
  return request.get(`/stores/${storeId}`)
}

export function getStoreList(params?: Record<string, any>) {
  return request.get('/stores', { params })
}
