import { get, post } from '@/utils/request'

export function getHealthProfile(memberId: number | string) {
  return get(`/api/v1/user/health-profiles/${memberId}`)
}

export function createHealthProfile(data: Record<string, any>) {
  return post('/api/v1/user/health-profiles', data)
}
