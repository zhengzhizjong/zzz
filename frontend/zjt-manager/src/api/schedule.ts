import { get, post, put, del } from '@/utils/request'

export function getScheduleList(params: any) {
  return get('/api/v1/store/schedules', { params })
}

export function createSchedule(data: any) {
  return post('/api/v1/store/schedules', data)
}

export function batchCreateSchedule(data: any) {
  return post('/api/v1/store/schedules/batch', data)
}

export function updateSchedule(id: number, data: any) {
  return put(`/api/v1/store/schedules/${id}`, data)
}

export function deleteSchedule(id: number) {
  return del(`/api/v1/store/schedules/${id}`)
}
