import http from './http'
export const getNotifications = params => http.get('/notifications', { params })
export const getUnreadCount = () => http.get('/notifications/unread-count')
export const readAll = () => http.put('/notifications/read-all')
export const readOne = id => http.put(`/notifications/${id}/read`)
