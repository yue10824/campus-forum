<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="aside">
      <div class="admin-logo">🛠 管理后台</div>
      <el-menu
        :router="true"
        :default-active="route.path"
        background-color="#1e2a3a"
        text-color="#c0c4cc"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/admin/dashboard"><el-icon><DataLine /></el-icon>数据概览</el-menu-item>
        <el-menu-item index="/admin/users"><el-icon><User /></el-icon>用户管理</el-menu-item>
        <el-menu-item index="/admin/posts"><el-icon><Document /></el-icon>帖子管理</el-menu-item>
        <el-menu-item index="/admin/activities"><el-icon><Calendar /></el-icon>活动管理</el-menu-item>
        <el-menu-item index="/admin/sections"><el-icon><Grid /></el-icon>版块管理</el-menu-item>
        <el-menu-item index="/admin/announcements"><el-icon><Bell /></el-icon>公告管理</el-menu-item>
        <el-divider />
        <el-menu-item index="/" @click="router.push('/')"><el-icon><House /></el-icon>返回前台</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <span style="font-size:14px;color:#606266">欢迎，{{ authStore.user?.nickname }}</span>
        <el-button type="danger" plain size="small" @click="logout">退出</el-button>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
function logout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout { min-height: 100vh; }
.aside { background: #1e2a3a; overflow-y: auto; }
.admin-logo {
  color: #fff; font-size: 16px; font-weight: 700;
  padding: 20px; border-bottom: 1px solid #2d3f55;
}
.admin-header {
  background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  display: flex; justify-content: flex-end; align-items: center; gap: 12px;
}
.admin-main { background: #f5f7fa; padding: 24px; }
</style>
