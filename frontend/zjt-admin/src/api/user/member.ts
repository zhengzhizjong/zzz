import request from '@/utils/request'
import type { R, PageResult } from '@/types/api'

export interface MemberInfo {
  id: number
  tenantId: number
  storeId: number
  memberNo: string
  nickname: string
  realName: string
  name: string
  phone: string
  gender: number
  birthday: string
  memberLevelId: number
  memberType: number
  totalConsumption: number
  visitCount: number
  balance: number
  points: number
  sourceChannel: string
  status: number
  createdAt: string
  updatedAt: string
}

export interface MemberUpdateRequest {
  name?: string
  realName?: string
  phone?: string
  gender?: number
  birthday?: string
  memberType?: number
  status?: number
}

export function getMemberList(params: {
  page: number
  pageSize: number
  keyword?: string
  memberType?: number
  memberLevelId?: number
  status?: number
}) {
  return request.get<R<PageResult<MemberInfo>>>('/api/v1/user/members', { params })
}

export function getMemberDetail(id: number) {
  return request.get<R<MemberInfo>>(`/api/v1/user/members/${id}`)
}

export function updateMember(id: number, data: MemberUpdateRequest) {
  return request.put<R<void>>(`/api/v1/user/members/${id}`, data)
}
