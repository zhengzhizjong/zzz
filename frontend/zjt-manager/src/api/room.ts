import { get, post, put } from '@/utils/request'

export function getRoomList(params: any) {
  return get('/api/v1/store/rooms', { params })
}

export function createRoom(data: any) {
  return post('/api/v1/store/rooms', data)
}

export function updateRoom(id: number, data: any) {
  return put(`/api/v1/store/rooms/${id}`, data)
}

export function updateRoomStatus(id: number, status: number) {
  return put(`/api/v1/store/rooms/${id}/status`, { status })
}
