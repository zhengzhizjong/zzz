import axios, { type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { showToast } from 'vant'
import { getToken, removeToken } from './auth'
import router from '@/router'

const service = axios.create({
  baseURL: '',
  timeout: 15000
})

service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    const tenantId = localStorage.getItem('tenantId')
    if (tenantId) {
      config.headers['X-Tenant-Id'] = tenantId
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code !== 0) {
      if (res.code === 40101 || res.code === 40102 || res.code === 40103) {
        showToast('登录已过期，请重新登录')
        removeToken()
        router.push('/login')
        return Promise.reject(new Error('未授权'))
      }
      showToast(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        showToast('登录已过期，请重新登录')
        removeToken()
        router.push('/login')
      } else {
        showToast('网络错误，请稍后重试')
      }
    } else {
      showToast('网络连接失败，请检查网络')
    }
    return Promise.reject(error)
  }
)

export function get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return service.get(url, config) as unknown as Promise<T>
}

export function post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  return service.post(url, data, config) as unknown as Promise<T>
}

export function put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  return service.put(url, data, config) as unknown as Promise<T>
}

export function del<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return service.delete(url, config) as unknown as Promise<T>
}

export default service
