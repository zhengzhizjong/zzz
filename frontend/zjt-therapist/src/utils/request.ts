import axios from 'axios'
import { getToken, removeToken } from './auth'
import { showToast } from 'vant'
import router from '../router'

const request = axios.create({
  baseURL: '',
  timeout: 15000
})

request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    const tenantId = localStorage.getItem('tenantId')
    if (tenantId) {
      config.headers['X-Tenant-Id'] = tenantId
    } else {
      config.headers['X-Tenant-Id'] = '1'
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
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

export function get<T = any>(url: string, config?: any): Promise<T> {
  return request.get(url, config) as unknown as Promise<T>
}

export function post<T = any>(url: string, data?: any, config?: any): Promise<T> {
  return request.post(url, data, config) as unknown as Promise<T>
}

export function put<T = any>(url: string, data?: any, config?: any): Promise<T> {
  return request.put(url, data, config) as unknown as Promise<T>
}

export function del<T = any>(url: string, config?: any): Promise<T> {
  return request.delete(url, config) as unknown as Promise<T>
}

export default request
