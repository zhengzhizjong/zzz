export interface StoreInfo {
  id: number
  storeNo: string
  storeName: string
  storeType: number
  provinceCode: string
  cityCode: string
  districtCode: string
  address: string
  latitude: number
  longitude: number
  contactName: string
  contactPhone: string
  businessStartTime: string
  businessEndTime: string
  roomCount: number
  technicianCount: number
  regionId: number
  status: number
  createdAt: string
}

export interface StoreCreateRequest {
  storeName: string
  storeType?: number
  provinceCode?: string
  cityCode?: string
  districtCode?: string
  address?: string
  latitude?: number
  longitude?: number
  contactName?: string
  contactPhone?: string
  businessStartTime?: string
  businessEndTime?: string
}

export type StoreUpdateRequest = Partial<StoreCreateRequest>

export interface StoreStatusOption {
  value: number
  label: string
  type: string
}

export const STORE_STATUS_OPTIONS: StoreStatusOption[] = [
  { value: 1, label: '营业中', type: 'success' },
  { value: 2, label: '休息中', type: 'warning' },
  { value: 3, label: '装修中', type: 'info' },
  { value: 4, label: '已关闭', type: 'danger' },
]

export const STORE_TYPE_OPTIONS = [
  { value: 1, label: '直营' },
  { value: 2, label: '加盟' },
]
