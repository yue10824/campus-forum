import http from './http'
export const getComments = params => http.get('/comments', { params })
export const createComment = data => http.post('/comments', data)
export const deleteComment = id => http.delete(`/comments/${id}`)
export const likeComment = id => http.post(`/comments/${id}/like`)
