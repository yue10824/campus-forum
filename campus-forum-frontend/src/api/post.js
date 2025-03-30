import http from './http'
export const getPosts = params => http.get('/posts', { params })
export const getPost = id => http.get(`/posts/${id}`)
export const createPost = data => http.post('/posts', data)
export const deletePost = id => http.delete(`/posts/${id}`)
export const likePost = id => http.post(`/posts/${id}/like`)
