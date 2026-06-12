import request from '../utils/request'

export function getPromotion(technicianId: number) {
  return request.get(`/api/v1/store/technicians/${technicianId}/promotion`)
}

export function generatePoster(technicianId: number) {
  return request.post(`/api/v1/store/technicians/${technicianId}/promotion/generate-poster`)
}

export function getPromotionStats(technicianId: number, startDate: string, endDate: string) {
  return request.get(`/api/v1/store/technicians/${technicianId}/promotion/stats`, {
    params: { startDate, endDate }
  })
}

export function getCommissions(technicianId: number, params?: { status?: string; page?: number; pageSize?: number }) {
  return request.get(`/api/v1/store/technicians/${technicianId}/promotion/commissions`, {
    params: { page: 1, pageSize: 20, ...params }
  })
}

export function getRanking(technicianId: number, params?: { storeId?: number; period?: string }) {
  return request.get(`/api/v1/store/technicians/${technicianId}/promotion/ranking`, {
    params: { period: 'month', ...params }
  })
}
