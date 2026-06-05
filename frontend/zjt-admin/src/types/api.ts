/** 统一响应类型 */
export interface R<T = any> {
  code: number
  message: string
  data: T
}

/** 分页响应类型 */
export interface PageResult<T = any> {
  list: T[]
  pagination: Pagination
}

/** 分页信息 */
export interface Pagination {
  page: number
  pageSize: number
  total: number
  totalPages: number
}

/** 分页请求参数 */
export interface PageParams {
  page: number
  pageSize: number
}
