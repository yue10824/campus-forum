<template>
  <div>
    <!-- Banner -->
    <el-card class="banner-card" shadow="never">
      <h1>🎓 校园活动发布平台</h1>
      <p>发现精彩活动，分享校园生活</p>
      <el-space>
        <el-button type="primary" size="large" @click="router.push('/activity')">浏览活动</el-button>
        <el-button size="large" @click="router.push('/discover')">逛逛论坛</el-button>
      </el-space>
    </el-card>

    <el-row :gutter="24" style="margin-top:24px">
      <!-- 推荐活动 -->
      <el-col :span="16">
        <div class="section-header">
          <span class="page-title">🔥 推荐活动</span>
          <el-link type="primary" @click="router.push('/activity')">查看全部 →</el-link>
        </div>
        <el-row :gutter="16">
          <el-col :span="12" v-for="a in activities" :key="a.id" style="margin-bottom:16px">
            <el-card class="card-hover" shadow="hover" @click="router.push(`/activity/${a.id}`)">
              <el-image :src="a.coverImage" style="width:100%;height:140px;border-radius:6px" fit="cover">
                <template #error><div class="img-placeholder">📷</div></template>
              </el-image>
              <div style="padding:10px 0 0">
                <div class="act-title">{{ a.title }}</div>
                <div class="act-meta">
                  <el-tag size="small" type="info">{{ a.location }}</el-tag>
                  <span style="color:#909399;font-size:12px">{{ a.startTime?.slice(0,10) }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 最新帖子 -->
        <div class="section-header" style="margin-top:8px">
          <span class="page-title">📝 最新帖子</span>
        </div>
        <el-card v-for="p in posts" :key="p.id" class="post-item card-hover" shadow="hover" @click="router.push(`/post/${p.id}`)">
          <div class="post-title">{{ p.title }}</div>
          <div class="post-meta">
            <span>👁 {{ p.viewCount }}</span>
            <span>❤ {{ p.likeCount }}</span>
            <span>💬 {{ p.commentCount }}</span>
          </div>
        </el-card>
      </el-col>

      <!-- 侧边栏 -->
      <el-col :span="8">
        <!-- 公告 -->
        <el-card class="sidebar-card">
          <template #header><b>📢 公告</b></template>
          <div v-for="a in announcements" :key="a.id" class="announce-item">📌 {{ a.title }}</div>
          <el-empty v-if="!announcements.length" description="暂无公告" :image-size="40" />
        </el-card>

        <!-- 版块导航 -->
        <el-card class="sidebar-card" style="margin-top:16px">
          <template #header><b>📂 版块</b></template>
          <el-tag
            v-for="s in sections" :key="s.id"
            style="margin:4px;cursor:pointer"
            @click="router.push(`/section/${s.id}`)"
          >{{ s.name }}</el-tag>
        </el-card>

        <!-- 快捷操作 -->
        <el-card v-if="authStore.isLoggedIn" class="sidebar-card" style="margin-top:16px">
          <template #header><b>⚡ 快捷操作</b></template>
          <el-button type="primary" plain block @click="router.push('/activity/create')">发布活动</el-button>
          <el-button plain block @click="router.push('/post/publish')" style="margin-top:8px">发布帖子</el-button>
          <el-button plain block @click="router.push('/ai-assistant')" style="margin-top:8px">🤖 AI助手</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getRecommend, getActivities } from '@/api/activity'
import { getPosts } from '@/api/post'
import { getSections } from '@/api/user'
import http from '@/api/http'

const router = useRouter()
const authStore = useAuthStore()
const activities = ref([])
const posts = ref([])
const sections = ref([])
const announcements = ref([])

onMounted(async () => {
  try {
    const [actRes, postRes, secRes] = await Promise.all([
      authStore.isLoggedIn ? getRecommend() : getActivities({ page: 1, size: 6 }),
      getPosts({ page: 1, size: 8 }),
      getSections(),
    ])
    activities.value = authStore.isLoggedIn ? actRes.data : (actRes.data?.records || [])
    posts.value = postRes.data?.records || []
    sections.value = secRes.data || []
    // 公告
    const annRes = await http.get('/announcements', { params: { size: 5 } }).catch(() => null)
    announcements.value = annRes?.data || []
  } catch (e) {}
})
</script>

<style scoped>
.banner-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 20px;
  border-radius: 12px;
  border: none;
}
.banner-card h1 { font-size: 26px; margin-bottom: 8px; }
.banner-card p { margin-bottom: 16px; opacity: 0.9; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.act-title { font-weight: 600; font-size: 14px; line-height: 1.4; margin-bottom: 6px; }
.act-meta { display: flex; justify-content: space-between; align-items: center; }
.img-placeholder { height: 140px; display: flex; align-items: center; justify-content: center; background: #f5f7fa; font-size: 24px; }
.post-item { margin-bottom: 8px; }
.post-title { font-size: 14px; font-weight: 500; margin-bottom: 6px; }
.post-meta { display: flex; gap: 12px; color: #909399; font-size: 12px; }
.sidebar-card { border-radius: 8px; }
.announce-item { padding: 6px 0; border-bottom: 1px solid #f0f0f0; font-size: 13px; }
</style>
