import request from '../utils/request'

export function askAiQuestion(data: { question: string; memberId?: number }) {
  return request.post('/api/v1/ai/consultation/ask', data)
}

export function getAiChatHistory(params: { memberId: number; page?: number; pageSize?: number }) {
  return request.get('/api/v1/ai/consultation/history', { params })
}
