import request from '@/utils/request'
import type { R, PageResult } from '@/types/api'

// ===== 字典管理 =====

export interface DictInfo {
  id: number
  dictCode: string
  dictName: string
  description: string
  status: number
  createdAt: string
  updatedAt: string
}

export interface DictItemInfo {
  id: number
  dictId: number
  itemCode: string
  itemName: string
  itemValue: string
  sortOrder: number
  status: number
}

export function getDictList(params: { page: number; pageSize: number; keyword?: string }) {
  return request.get<R<PageResult<DictInfo>>>('/api/v1/admin/dicts', { params })
}

export function createDict(data: { dictCode: string; dictName: string; description?: string; status?: number }) {
  return request.post<R<void>>('/api/v1/admin/dicts', data)
}

export function updateDict(id: number, data: { dictName?: string; description?: string; status?: number }) {
  return request.put<R<void>>(`/api/v1/admin/dicts/${id}`, data)
}

export function deleteDict(id: number) {
  return request.delete<R<void>>(`/api/v1/admin/dicts/${id}`)
}

export function getDictItems(dictId: number) {
  return request.get<R<DictItemInfo[]>>(`/api/v1/admin/dicts/${dictId}/items`)
}

export function getDictItemsByCode(dictCode: string) {
  return request.get<R<DictItemInfo[]>>(`/api/v1/admin/dicts/code/${dictCode}/items`)
}

export function createDictItem(dictId: number, data: { itemCode: string; itemName: string; itemValue?: string; sortOrder?: number; status?: number }) {
  return request.post<R<void>>(`/api/v1/admin/dicts/${dictId}/items`, data)
}

export function updateDictItem(id: number, data: { itemCode?: string; itemName?: string; itemValue?: string; sortOrder?: number; status?: number }) {
  return request.put<R<void>>(`/api/v1/admin/dicts/items/${id}`, data)
}

export function deleteDictItem(id: number) {
  return request.delete<R<void>>(`/api/v1/admin/dicts/items/${id}`)
}

// ===== 配置管理 =====

export interface ConfigInfo {
  id: number
  configKey: string
  configValue: string
  configName: string
  description: string
  configType: number
  updatedAt: string
}

export function getConfigList(params: { page: number; pageSize: number; keyword?: string }) {
  return request.get<R<PageResult<ConfigInfo>>>('/api/v1/admin/configs', { params })
}

export function updateConfig(id: number, data: { configValue: string }) {
  return request.put<R<void>>(`/api/v1/admin/configs/${id}`, data)
}

// ===== 功能开关 =====

export interface FeatureFlagInfo {
  id: number
  flagKey: string
  flagName: string
  description: string
  defaultValue: number
  type: number
  percentage: number
  rulesJson: string
  updatedAt: string
}

export function getFeatureFlagList(params: { page: number; pageSize: number; keyword?: string }) {
  return request.get<R<PageResult<FeatureFlagInfo>>>('/api/v1/admin/feature-flags', { params })
}

export function createFeatureFlag(data: { flagKey: string; flagName: string; description?: string; defaultValue?: number; type?: number; percentage?: number; rulesJson?: string }) {
  return request.post<R<void>>('/api/v1/admin/feature-flags', data)
}

export function updateFeatureFlag(id: number, data: { flagName?: string; description?: string; defaultValue?: number; type?: number; percentage?: number; rulesJson?: string }) {
  return request.put<R<void>>(`/api/v1/admin/feature-flags/${id}`, data)
}

export function toggleFeatureFlag(id: number, enabled: boolean) {
  return request.put<R<void>>(`/api/v1/admin/feature-flags/${id}/toggle`, { enabled })
}

export function deleteFeatureFlag(id: number) {
  return request.delete<R<void>>(`/api/v1/admin/feature-flags/${id}`)
}
