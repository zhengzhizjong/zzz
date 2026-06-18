import { get } from '@/utils/request'

export function getArticleList(params?: Record<string, any>) {
  return get('/api/v1/content/articles', { params })
}

export function getArticleDetail(id: number | string) {
  return get(`/api/v1/content/articles/${id}`)
}
