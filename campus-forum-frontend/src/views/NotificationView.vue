<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px">
      <h2 class="page-title">🔔 通知中心</h2>
      <el-button @click="notifStore.markAllRead()" v-if="notifStore.unreadCount">全部已读</el-button>
    </div>
    <el-card v-for="n in notifStore.notifications" :key="n.id" class="notif-item" :class="{unread:!n.isRead}">
      <div class="notif-content">{{ n.content }}</div>
      <div class="notif-time">{{ n.createdAt?.slice(0,16) }}</div>
    </el-card>
    <el-empty v-if="!notifStore.notifications.length" description="暂无通知" />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useNotificationStore } from '@/stores/notification'
const notifStore = useNotificationStore()
onMounted(() => notifStore.fetchNotifications())
</script>

<style scoped>
.notif-item { margin-bottom: 8px; }
.notif-item.unread { border-left: 3px solid #409EFF; }
.notif-content { font-size: 14px; margin-bottom: 4px; }
.notif-time { font-size: 12px; color: #909399; }
</style>
