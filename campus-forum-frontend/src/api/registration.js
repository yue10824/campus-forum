import http from './http'
export const register = data => http.post('/registrations', data)
export const cancel = activityId => http.delete(`/registrations/${activityId}`)
export const getMyRegistrations = params => http.get('/registrations/my', { params })
export const getActivityRegistrations = id => http.get(`/registrations/activity/${id}`)
export const auditRegistration = (id, status) => http.put(`/registrations/${id}/status`, null, { params: { status } })
