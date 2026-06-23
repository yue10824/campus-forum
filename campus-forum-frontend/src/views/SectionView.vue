<template>
  <div>
    <h2 class="page-title" style="margin-bottom:20px">{{ section?.name }}</h2>
    <p style="color:#606266;margin-bottom:16px">{{ section?.description }}</p>
    <div v-loading="loading">
      <el-card v-for="p in posts" :key="p.id" class="post-card card-hover" @click="router.push(`/post/${p.id}`)">
        <div class="post-title">{{ p.title }}</div>
        <div class="post-meta">
          <span>👁 {{ p.viewCount }}</span>
          <span>❤ {{ p.likeCount }}</span>
          <span>{{ p.createdAt?.slice(0,10) }}</span>
        </div>
      </el-card>
      <el-empty v-if="!posts.length && !loading" description="暂无帖子" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPosts } from '@/api/post'
import { getSections } from '@/api/user'

const route = useRoute()
const router = useRouter()
const section = ref(null)
const posts = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  const [secRes, postRes] = await Promise.all([
    getSections(),
    getPosts({ page: 1, size: 20, sectionId: route.params.id })
  ])
  section.value = (secRes.data || []).find(s => s.id == route.params.id)
  posts.value = postRes.data?.records || []
  loading.value = false
})
</script>

<style scoped>
.post-card { margin-bottom: 8px; }
.post-title { font-size: 14px; font-weight: 500; margin-bottom: 6px; }
.post-meta { display: flex; gap: 12px; color: #909399; font-size: 12px; }
</style>
