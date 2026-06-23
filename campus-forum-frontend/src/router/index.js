import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  // 认证页
  { path: '/login', component: () => import('@/views/LoginView.vue'), meta: { guest: true } },
  { path: '/register', component: () => import('@/views/RegisterView.vue'), meta: { guest: true } },

  // 主布局
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      { path: '', name: 'home', component: () => import('@/views/HomeView.vue') },
      { path: 'discover', name: 'discover', component: () => import('@/views/DiscoverView.vue') },
      { path: 'section/:id', name: 'section', component: () => import('@/views/SectionView.vue') },

      // 帖子
      { path: 'post/publish', name: 'post-publish', component: () => import('@/views/PostPublishView.vue'), meta: { auth: true } },
      { path: 'post/:id', name: 'post-detail', component: () => import('@/views/PostDetailView.vue') },

      // 活动
      { path: 'activity', name: 'activity-list', component: () => import('@/views/ActivityListView.vue') },
      { path: 'activity/create', name: 'activity-create', component: () => import('@/views/ActivityCreateView.vue'), meta: { auth: true } },
      { path: 'activity/:id', name: 'activity-detail', component: () => import('@/views/ActivityDetailView.vue') },

      // 用户
      { path: 'user/:id', name: 'profile', component: () => import('@/views/ProfileView.vue') },
      { path: 'user/registrations', name: 'my-registrations', component: () => import('@/views/MyRegistrationsView.vue'), meta: { auth: true } },
      { path: 'collections', name: 'collections', component: () => import('@/views/CollectionsView.vue'), meta: { auth: true } },

      // 消息
      { path: 'notifications', name: 'notifications', component: () => import('@/views/NotificationView.vue'), meta: { auth: true } },
      { path: 'chat', name: 'chat', component: () => import('@/views/ChatView.vue'), meta: { auth: true } },

      // AI
      { path: 'ai-assistant', name: 'ai-assistant', component: () => import('@/views/AiAssistantView.vue'), meta: { auth: true } },
    ]
  },

  // 管理端布局
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { auth: true, admin: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'admin-dashboard', component: () => import('@/views/admin/DashboardView.vue') },
      { path: 'users', name: 'admin-users', component: () => import('@/views/admin/UserManageView.vue') },
      { path: 'posts', name: 'admin-posts', component: () => import('@/views/admin/PostManageView.vue') },
      { path: 'activities', name: 'admin-activities', component: () => import('@/views/admin/ActivityManageView.vue') },
      { path: 'sections', name: 'admin-sections', component: () => import('@/views/admin/SectionManageView.vue') },
      { path: 'announcements', name: 'admin-announcements', component: () => import('@/views/admin/AnnouncementView.vue') },
    ]
  },

  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if (to.meta.auth && !authStore.isLoggedIn) {
    return next('/login')
  }
  if (to.meta.guest && authStore.isLoggedIn) {
    return next('/')
  }
  if (to.meta.admin && authStore.user?.role !== 1) {
    return next('/')
  }
  next()
})

export default router
