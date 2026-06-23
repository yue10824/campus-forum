import http from './http'
export const login = data => http.post('/auth/login', data)
export const register = data => http.post('/auth/register', data)
export const sendCode = email => http.post(`/auth/send-code?email=${encodeURIComponent(email)}`)
