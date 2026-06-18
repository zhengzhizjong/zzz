import { get } from '@/utils/request'

export function getCourseList(params?: Record<string, any>) {
  return get('/api/v1/content/courses', { params })
}

export function getCourseDetail(id: number | string) {
  return get(`/api/v1/content/courses/${id}`)
}
