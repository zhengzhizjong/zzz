import request from '@/utils/request'
import type { R, PageResult } from '@/types/api'

export interface EmployeeInfo {
  id: number
  employeeNo: string
  name: string
  phone: string
  department: string
  position: string
  status: number
  createdAt: string
}

export function getEmployeeList(params: {
  page: number
  pageSize: number
  keyword?: string
  department?: string
  status?: number
}) {
  return request.get<R<PageResult<EmployeeInfo>>>('/api/v1/user/employees', { params })
}

export function createEmployee(data: {
  employeeNo: string
  name: string
  phone: string
  department: string
  position: string
}) {
  return request.post<R<void>>('/api/v1/user/employees', data)
}

export function updateEmployee(id: number, data: {
  name?: string
  phone?: string
  department?: string
  position?: string
}) {
  return request.put<R<void>>(`/api/v1/user/employees/${id}`, data)
}

export function updateEmployeeStatus(id: number, status: number) {
  return request.put<R<void>>(`/api/v1/user/employees/${id}/status`, { status })
}
