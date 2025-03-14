import http from './http'
export const getSections = () => http.get('/sections')
export const getUser = id => http.get(`/users/${id}`)
export const updateProfile = data => http.put('/users/profile', data)
export const toggleFollow = id => http.post(`/users/${id}/follow`)
export const getFollowers = (id, params) => http.get(`/users/${id}/followers`, { params })
export const getFollowing = (id, params) => http.get(`/users/${id}/following`, { params })
export const isFollowing = id => http.get(`/users/${id}/is-following`)
