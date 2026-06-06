import request from '@/utils/request'
import type { R, PageResult } from '@/types/api'

export interface RoomInfo {
  id: number
  storeId: number
  roomNo: string
  roomName: string
  roomType: number
  floor: number
  capacity: number
  status: number
  equipment: string
}

export function getRoomList(params: any) {
  return request.get<R<PageResult<RoomInfo>>>('/api/v1/store/rooms', { params })
}

export function createRoom(data: any) {
  return request.post<R<void>>('/api/v1/store/rooms', data)
}

export function updateRoom(id: number, data: any) {
  return request.put<R<void>>(`/api/v1/store/rooms/${id}`, data)
}

export function updateRoomStatus(id: number, status: number) {
  return request.put<R<void>>(`/api/v1/store/rooms/${id}/status`, { status })
}
