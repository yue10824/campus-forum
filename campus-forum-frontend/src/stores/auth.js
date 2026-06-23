import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  // 兼容旧格式缓存：如果 localStorage 里的 role 是字符串（如旧版本的 'ADMIN'），转换为数字
  function parseStoredUser() {
    try {
      const u = JSON.parse(localStorage.getItem('user') || 'null')
      if (!u) return null
      // 将旧格式 role 字符串转数字
      if (u.role === 'ADMIN') u.role = 1
      else if (u.role === 'USER') u.role = 0
      else u.role = Number(u.role) || 0
      return u
    } catch { return null }
  }

  const user = ref(parseStoredUser())
  const token = ref(localStorage.getItem('token') || '')

  const isLoggedIn = computed(() => !!token.value)

  async function login(username, password) {
    const res = await loginApi({ username, password })
    token.value = res.data.token
    user.value = {
      id: res.data.userId,
      nickname: res.data.nickname,
      avatar: res.data.avatar,
      role: res.data.role,
    }
    localStorage.setItem('token', token.value)
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  async function register(data) {
    await registerApi(data)
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  function updateUser(newUser) {
    user.value = { ...user.value, ...newUser }
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  return { user, token, isLoggedIn, login, register, logout, updateUser }
})
