import axios from 'axios'
import { ElMessage } from 'element-plus'

const TOKEN_KEY = 'manager_token'

const service = axios.create({
  baseURL: '',
  timeout: 15000
})

service.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  config.headers['X-Tenant-Id'] = '1'
  return config
})

service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== undefined && res.code !== 0 && res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 40101 || res.code === 401) {
        localStorage.removeItem(TOKEN_KEY)
        window.location.href = '/manager/login'
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem(TOKEN_KEY)
      window.location.href = '/manager/login'
    }
    ElMessage.error(error.response?.data?.message || error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default service
export const get = (url: string, config?: any) => service.get(url, config)
export const post = (url: string, data?: any, config?: any) => service.post(url, data, config)
export const put = (url: string, data?: any, config?: any) => service.put(url, data, config)
export const del = (url: string, config?: any) => service.delete(url, config)
