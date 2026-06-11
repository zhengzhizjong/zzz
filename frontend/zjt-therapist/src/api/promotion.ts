import request from '../utils/request'

export function getPromotion(technicianId: number) {
  return request.get('/api/v1/store/technician-promotion', {
    params: { technicianId }
  })
}

export function getPromotionStats(id: number) {
  return request.get(`/api/v1/store/technician-promotion/${id}/stats`)
}
