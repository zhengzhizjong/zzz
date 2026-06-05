import request from '@/utils/request'
import type { R, PageResult } from '@/types/api'
import type { ServiceItem, ServiceItemCreateRequest, ServiceItemUpdateRequest } from '@/types/service-item'

export function getServiceItemList(params: { page: number; pageSize: number; keyword?: string; status?: number; categoryId?: number }) {
  return request.get<R<PageResult<ServiceItem>>>('/api/v1/content/service-items', { params })
}

export function getServiceItemDetail(id: number) {
  return request.get<R<ServiceItem>>(`/api/v1/content/service-items/${id}`)
}

export function createServiceItem(data: ServiceItemCreateRequest) {
  return request.post<R<void>>('/api/v1/content/service-items', data)
}

export function updateServiceItem(id: number, data: ServiceItemUpdateRequest) {
  return request.put<R<void>>(`/api/v1/content/service-items/${id}`, data)
}

export function updateServiceItemStatus(id: number, status: number) {
  return request.put<R<void>>(`/api/v1/content/service-items/${id}/status`, { status })
}

export function listAllServiceItems() {
  return request.get<R<ServiceItem[]>>('/api/v1/content/service-items/all')
}
