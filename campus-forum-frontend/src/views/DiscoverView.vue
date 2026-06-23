<template>
  <div>
    <h2 class="page-title" style="margin-bottom:20px">🔍 发现</h2>
    <el-tabs v-model="activeSection" @tab-click="loadPosts">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane v-for="s in sections" :key="s.id" :label="s.name" :name="String(s.id)" />
    </el-tabs>

    <div style="display:flex;justify-content:flex-end;margin-bottom:16px">
      <el-button type="primary" @click="router.push('/post/publish')">+ 发帖</el-button>
    </div>

    <div v-loading="loading">
      <el-card v-for="p in posts" :key="p.id" class="card-hover post-card" shadow="hover" @click="router.push(`/post/${p.id}`)">
        <div class="post-title">{{ p.title }}</div>
        <div class="post-content">{{ p.content?.slice(0, 120) }}...</div>
        <div class="post-footer">
          <span>👁 {{ p.viewCount }}</span>
          <span>❤ {{ p.likeCount }}</span>
          <span>💬 {{ p.commentCount }}</span>
          <span style="margin-left:auto;color:#c0c4cc;font-size:12px">{{ p.createdAt?.slice(0,10) }}</span>
        </div>
      </el-card>
      <el-empty v-if="!loading && !posts.length" description="暂无帖子" />
    </div>

    <div style="text-align:center;margin-top:20px">
      <el-pagination v-model:current-page="page" :total="total" layout="prev, pager, next" @current-change="loadPosts" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPosts } from '@/api/post'
import { getSections } from '@/api/user'

const router = useRouter()
const sections = ref([])
const posts = ref([])
const loading = ref(false)
const activeSection = ref('all')
const page = ref(1)
const total = ref(0)

async function loadPosts() {
  loading.value = true
  try {
    const sectionId = activeSection.value !== 'all' ? activeSection.value : undefined
    const res = await getPosts({ page: page.value, size: 10, sectionId })
    posts.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const res = await getSections()
  sections.value = res.data || []
  loadPosts()
})
</script>

<style scoped>
.post-card { margin-bottom: 12px; }
.post-title { font-size: 15px; font-weight: 600; margin-bottom: 6px; }
.post-content { color: #606266; font-size: 13px; margin-bottom: 10px; line-height: 1.6; }
.post-footer { display: flex; gap: 16px; color: #909399; font-size: 13px; }
</style>
