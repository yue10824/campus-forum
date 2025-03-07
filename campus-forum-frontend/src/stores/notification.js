import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getNotifications, getUnreadCount, readAll } from '@/api/notification'

export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)
  const notifications = ref([])

  async function fetchUnreadCount() {
    const res = await getUnreadCount()
    unreadCount.value = res.data
  }

  async function fetchNotifications(page = 1) {
    const res = await getNotifications({ page })
    notifications.value = res.data.records || []
  }

  async function markAllRead() {
    await readAll()
    unreadCount.value = 0
    notifications.value.forEach(n => n.isRead = 1)
  }

  function increment() {
    unreadCount.value++
  }

  return { unreadCount, notifications, fetchUnreadCount, fetchNotifications, markAllRead, increment }
})
