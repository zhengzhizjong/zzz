import request from '@/utils/request'
import type { R, PageResult } from '@/types/api'
import type { StoreInfo, StoreCreateRequest, StoreUpdateRequest } from '@/types/store'

export function getStoreList(params: { page: number; pageSize: number; keyword?: string; status?: number }) {
  return request.get<R<PageResult<StoreInfo>>>('/api/v1/store/stores', { params })
}

export function getStoreDetail(id: number) {
  return request.get<R<StoreInfo>>(`/api/v1/store/stores/${id}`)
}

export function createStore(data: StoreCreateRequest) {
  return request.post<R<void>>('/api/v1/store/stores', data)
}

export function updateStore(id: number, data: StoreUpdateRequest) {
  return request.put<R<void>>(`/api/v1/store/stores/${id}`, data)
}

export function updateStoreStatus(id: number, status: number) {
  return request.put<R<void>>(`/api/v1/store/stores/${id}/status`, { status })
}
