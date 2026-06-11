import request from '../utils/request'

export function login(data: { phone: string; password: string }) {
  return request.post('/api/v1/user/employees/login', data)
}

export function getProfile() {
  return request.get('/api/v1/user/employees/profile')
}
