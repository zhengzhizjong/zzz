import request from '@/utils/request'
import type { R, PageResult } from '@/types/api'

export interface RoleInfo {
  id: number
  name: string
  code: string
  description: string
  permissions: number[]
  createdAt: string
}

export interface PermissionNode {
  id: number
  label: string
  children?: PermissionNode[]
}

export function getRoleList(params: { page: number; pageSize: number; keyword?: string }) {
  return request.get<R<PageResult<RoleInfo>>>('/api/v1/user/roles', { params })
}

export function createRole(data: { name: string; code: string; description?: string }) {
  return request.post<R<void>>('/api/v1/user/roles', data)
}

export function updateRole(id: number, data: { name?: string; code?: string; description?: string }) {
  return request.put<R<void>>(`/api/v1/user/roles/${id}`, data)
}

export function deleteRole(id: number) {
  return request.delete<R<void>>(`/api/v1/user/roles/${id}`)
}

export function assignPermissions(roleId: number, permissionIds: number[]) {
  return request.put<R<void>>(`/api/v1/user/roles/${roleId}/permissions`, { permissionIds })
}

export function getPermissionTree() {
  return request.get<R<PermissionNode[]>>('/api/v1/user/permissions/tree')
}
