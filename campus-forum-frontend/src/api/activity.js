import http from './http'
export const getActivities = params => http.get('/activities', { params })
export const getActivity = id => http.get(`/activities/${id}`)
export const createActivity = data => http.post('/activities', data)
export const updateActivity = (id, data) => http.put(`/activities/${id}`, data)
export const deleteActivity = id => http.delete(`/activities/${id}`)
export const likeActivity = id => http.post(`/activities/${id}/like`)
export const getRecommend = () => http.get('/activities/recommend')
