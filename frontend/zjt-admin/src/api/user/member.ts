import request from '@/utils/request'
import type { R, PageResult } from '@/types/api'

export interface MemberInfo {
  id: number
  memberNo: string
  name: string
  phone: string
  memberLevelId: number
  memberType: number
  points: number
  balance: number
  visitCount: number
  totalConsumption: number
  createdAt: string
}

export function getMemberList(params: {
  page: number
  pageSize: number
  keyword?: string
  memberLevelId?: number
}) {
  return request.get<R<PageResult<MemberInfo>>>('/api/v1/user/members', { params })
}

export function getMemberDetail(id: number) {
  return request.get<R<MemberInfo>>(`/api/v1/user/members/${id}`)
}

export function updateMember(id: number, data: { name?: string; memberLevelId?: number }) {
  return request.put<R<void>>(`/api/v1/user/members/${id}`, data)
}
