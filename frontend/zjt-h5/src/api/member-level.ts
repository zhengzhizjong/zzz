import { get } from '@/utils/request'

export function getMemberLevels() {
  return get('/api/v1/user/member-levels')
}

export function getCurrentLevel() {
  return get('/api/v1/user/members/level')
}
