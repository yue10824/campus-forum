import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useNotificationStore } from '@/stores/notification'

vi.mock('@/api/notification', () => ({
  getNotifications: vi.fn().mockResolvedValue({ data: { records: [
    { id: 1, content: '测试通知', isRead: 0, createdAt: '2024-01-01 10:00:00' }
  ]}
  }),
  getUnreadCount: vi.fn().mockResolvedValue({ data: 3 }),
  readAll: vi.fn().mockResolvedValue({}),
}))

describe('notification store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('初始状态 - 未读数为0', () => {
    const store = useNotificationStore()
    expect(store.unreadCount).toBe(0)
    expect(store.notifications).toHaveLength(0)
  })

  it('fetchUnreadCount - 应正确设置未读数', async () => {
    const store = useNotificationStore()
    await store.fetchUnreadCount()
    expect(store.unreadCount).toBe(3)
  })

  it('fetchNotifications - 应加载通知列表', async () => {
    const store = useNotificationStore()
    await store.fetchNotifications()
    expect(store.notifications).toHaveLength(1)
    expect(store.notifications[0].content).toBe('测试通知')
  })

  it('markAllRead - 应将未读数清零', async () => {
    const store = useNotificationStore()
    store.unreadCount = 5
    await store.markAllRead()
    expect(store.unreadCount).toBe(0)
  })

  it('increment - 应使未读数+1', () => {
    const store = useNotificationStore()
    store.unreadCount = 2
    store.increment()
    expect(store.unreadCount).toBe(3)
  })
})
