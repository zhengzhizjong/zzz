export interface ServiceItem {
  id: number
  itemNo: string
  itemName: string
  categoryId: number
  categoryName: string
  price: number
  costPrice: number
  durationMinutes: number
  commissionType: number
  commissionValue: number
  isPackage: number
  isAiRecommended: number
  tags: string
  description: string
  status: number
  sortOrder: number
}

export interface ServiceItemCreateRequest {
  itemName: string
  categoryId?: number
  price: number
  costPrice?: number
  durationMinutes: number
  commissionType?: number
  commissionValue?: number
  isPackage?: number
  isAiRecommended?: number
  tags?: string
  description?: string
  sortOrder?: number
}

export type ServiceItemUpdateRequest = Partial<ServiceItemCreateRequest>

export const SERVICE_ITEM_STATUS_OPTIONS = [
  { value: 1, label: '上架', type: 'success' },
  { value: 2, label: '下架', type: 'info' },
]

export const COMMISSION_TYPE_OPTIONS = [
  { value: 1, label: '固定金额' },
  { value: 2, label: '比例' },
]
