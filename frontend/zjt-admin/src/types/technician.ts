export interface TechnicianInfo {
  id: number
  storeId: number
  employeeId: number
  employeeName?: string
  storeName?: string
  technicianNo: string
  skillLevel: number
  skilledItems: string
  defaultSchedule: string
  monthServiceCount: number
  monthRevenue: number
  monthRating: number
  totalServiceCount: number
  status: number
  isOnline: number
}

export interface TechnicianCreateRequest {
  storeId: number
  employeeId: number
  skillLevel?: number
  skilledItems?: string
  defaultSchedule?: string
}

export type TechnicianUpdateRequest = Partial<TechnicianCreateRequest>

export const TECHNICIAN_STATUS_OPTIONS = [
  { value: 1, label: '在职', type: 'success' },
  { value: 2, label: '休息', type: 'warning' },
  { value: 3, label: '离职', type: 'danger' },
]

export const SKILL_LEVEL_OPTIONS = [
  { value: 1, label: '初级' },
  { value: 2, label: '中级' },
  { value: 3, label: '高级' },
  { value: 4, label: '专家' },
]
