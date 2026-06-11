import request from '@/utils/request'
import type { R, PageResult } from '@/types/api'

export interface FileInfo {
  id: number
  name: string
  type: string
  size: number
  uploader: string
  uploadedAt: string
}

export function getFileList(params: any) {
  return request.get<R<PageResult<FileInfo>>>('/api/v1/admin/files', { params })
}

export function uploadFile(data: FormData) {
  return request.post<R<void>>('/api/v1/admin/files/upload', data, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function deleteFile(id: number) {
  return request.delete<R<void>>(`/api/v1/admin/files/${id}`)
}
