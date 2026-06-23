import http from './http'
export const toggleCollection = data => http.post('/collections', data)
export const getMyCollections = params => http.get('/collections/my', { params })
export const checkCollection = params => http.get('/collections/check', { params })
export const cancelCollection = (targetId, targetType) => http.delete(`/collections/${targetId}`, { params: { targetType } })
