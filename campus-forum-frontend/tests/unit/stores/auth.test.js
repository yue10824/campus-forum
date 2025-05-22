import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'

// Mock API
vi.mock('@/api/auth', () => ({
  login: vi.fn(),
  register: vi.fn(),
}))

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  it('初始状态 - 未登录', () => {
    const store = useAuthStore()
    expect(store.isLoggedIn).toBe(false)
    expect(store.user).toBeNull()
    expect(store.token).toBe('')
  })

  it('logout - 应清空登录状态', () => {
    const store = useAuthStore()
    store.token = 'test-token'
    store.user = { id: 1, nickname: '测试' }

    store.logout()

    expect(store.isLoggedIn).toBe(false)
    expect(store.token).toBe('')
    expect(store.user).toBeNull()
    expect(localStorage.getItem('token')).toBeNull()
  })

  it('updateUser - 应合并更新用户信息', () => {
    const store = useAuthStore()
    store.user = { id: 1, nickname: '旧昵称', avatar: null }

    store.updateUser({ nickname: '新昵称', avatar: 'http://example.com/avatar.png' })

    expect(store.user.nickname).toBe('新昵称')
    expect(store.user.avatar).toBe('http://example.com/avatar.png')
    expect(store.user.id).toBe(1) // 其他字段保留
  })

  it('isLoggedIn - token存在时为true', () => {
    const store = useAuthStore()
    store.token = 'valid-token'
    expect(store.isLoggedIn).toBe(true)
  })
})
