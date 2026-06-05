import request from '@/utils/request'
import type { R, PageResult } from '@/types/api'

// ===== 字典管理 =====

export interface DictInfo {
  id: number
  dictType: string
  dictName: string
  remark: string
  createdAt: string
  updatedAt: string
}

export interface DictItemInfo {
  id: number
  dictType: string
  itemValue: string
  itemLabel: string
  sort: number
  remark: string
}

export function getDictList(params: { page: number; pageSize: number; keyword?: string }) {
  return request.get<R<PageResult<DictInfo>>>('/api/v1/system/dicts', { params })
}

export function createDict(data: { dictType: string; dictName: string; remark?: string }) {
  return request.post<R<void>>('/api/v1/system/dicts', data)
}

export function updateDict(id: number, data: { dictName?: string; remark?: string }) {
  return request.put<R<void>>(`/api/v1/system/dicts/${id}`, data)
}

export function deleteDict(id: number) {
  return request.delete<R<void>>(`/api/v1/system/dicts/${id}`)
}

export function getDictItems(dictType: string) {
  return request.get<R<DictItemInfo[]>>(`/api/v1/system/dicts/${dictType}/items`)
}

export function createDictItem(data: { dictType: string; itemValue: string; itemLabel: string; sort?: number; remark?: string }) {
  return request.post<R<void>>('/api/v1/system/dict-items', data)
}

export function updateDictItem(id: number, data: { itemValue?: string; itemLabel?: string; sort?: number; remark?: string }) {
  return request.put<R<void>>(`/api/v1/system/dict-items/${id}`, data)
}

export function deleteDictItem(id: number) {
  return request.delete<R<void>>(`/api/v1/system/dict-items/${id}`)
}

// ===== 配置管理 =====

export interface ConfigInfo {
  id: number
  configKey: string
  configValue: string
  configName: string
  remark: string
  updatedAt: string
}

export function getConfigList(params: { page: number; pageSize: number; keyword?: string }) {
  return request.get<R<PageResult<ConfigInfo>>>('/api/v1/system/configs', { params })
}

export function updateConfig(id: number, data: { configValue: string }) {
  return request.put<R<void>>(`/api/v1/system/configs/${id}`, data)
}

// ===== 功能开关 =====

export interface FeatureFlagInfo {
  id: number
  flagKey: string
  flagName: string
  enabled: boolean
  grayPercent: number
  description: string
  updatedAt: string
}

export function getFeatureFlagList(params: { page: number; pageSize: number; keyword?: string }) {
  return request.get<R<PageResult<FeatureFlagInfo>>>('/api/v1/system/feature-flags', { params })
}

export function createFeatureFlag(data: { flagKey: string; flagName: string; enabled?: boolean; grayPercent?: number; description?: string }) {
  return request.post<R<void>>('/api/v1/system/feature-flags', data)
}

export function updateFeatureFlag(id: number, data: { flagName?: string; enabled?: boolean; grayPercent?: number; description?: string }) {
  return request.put<R<void>>(`/api/v1/system/feature-flags/${id}`, data)
}

export function toggleFeatureFlag(id: number, enabled: boolean) {
  return request.put<R<void>>(`/api/v1/system/feature-flags/${id}/toggle`, { enabled })
}

export function deleteFeatureFlag(id: number) {
  return request.delete<R<void>>(`/api/v1/system/feature-flags/${id}`)
}
