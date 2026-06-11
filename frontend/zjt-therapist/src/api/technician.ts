import request from '../utils/request'

export function getWorkspace(technicianId: number) {
  return request.get(`/api/v1/store/technicians/${technicianId}/workspace`)
}

export function getPerformance(technicianId: number) {
  return request.get(`/api/v1/store/technicians/${technicianId}/performance`)
}
