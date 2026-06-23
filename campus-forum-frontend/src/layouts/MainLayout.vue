<template>
  <el-container class="main-layout">
    <!-- 顶部导航 -->
    <el-header height="60px" class="header">
      <div class="header-inner container">
        <div class="logo" @click="router.push('/')">🎓 校园活动平台</div>
        <el-menu mode="horizontal" :ellipsis="false" :router="true" class="nav-menu">
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/discover">发现</el-menu-item>
          <el-menu-item index="/activity">活动</el-menu-item>
          <el-menu-item index="/ai-assistant">AI助手</el-menu-item>
        </el-menu>
        <div class="header-right">
          <template v-if="authStore.isLoggedIn">
            <el-badge :value="notifStore.unreadCount || 0" :hidden="!notifStore.unreadCount">
              <el-button circle icon="Bell" @click="router.push('/notifications')" />
            </el-badge>
            <el-dropdown @command="handleCommand" style="margin-left:12px">
              <el-avatar :src="authStore.user?.avatar" :size="36" style="cursor:pointer">
                {{ authStore.user?.nickname?.[0] }}
              </el-avatar>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人主页</el-dropdown-item>
                  <el-dropdown-item command="collections">我的收藏</el-dropdown-item>
                  <el-dropdown-item command="registrations">我的报名</el-dropdown-item>
                  <el-dropdown-item command="chat">私信</el-dropdown-item>
                  <el-dropdown-item command="admin" v-if="authStore.user?.role===1">管理后台</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button @click="router.push('/login')">登录</el-button>
            <el-button type="primary" @click="router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>

    <!-- 主内容 -->
    <el-main class="main-content">
      <div class="container">
        <router-view />
      </div>
    </el-main>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'
import { onMounted } from 'vue'

const router = useRouter()
const authStore = useAuthStore()
const notifStore = useNotificationStore()

onMounted(() => {
  if (authStore.isLoggedIn) {
    notifStore.fetchUnreadCount()
    // 建立 WebSocket 连接
    const ws = new WebSocket(`ws://localhost:8080/ws/notification?token=${authStore.token}`)
    ws.onmessage = () => notifStore.increment()
  }
})

function handleCommand(cmd) {
  if (cmd === 'logout') {
    authStore.logout()
    router.push('/login')
  } else if (cmd === 'profile') {
    router.push(`/user/${authStore.user.id}`)
  } else if (cmd === 'admin') {
    router.push('/admin')
  } else {
    router.push(`/${cmd}`)
  }
}
</script>

<style scoped>
.main-layout { min-height: 100vh; }
.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  display: flex;
  align-items: center;
  height: 100%;
  gap: 24px;
}
.logo {
  font-size: 18px;
  font-weight: 700;
  color: #409EFF;
  cursor: pointer;
  white-space: nowrap;
}
.nav-menu {
  flex: 1;
  border-bottom: none !important;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}
.main-content {
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
  padding: 24px 0;
}
</style>
