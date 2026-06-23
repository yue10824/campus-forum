<template>
  <div v-loading="loading">
    <template v-if="post">
      <el-card>
        <h1 style="font-size:22px;margin-bottom:12px">{{ post.title }}</h1>
        <div style="display:flex;gap:16px;color:#909399;font-size:13px;margin-bottom:16px">
          <span>👁 {{ post.viewCount }}</span>
          <span>{{ post.createdAt?.slice(0,10) }}</span>
        </div>
        <el-divider />
        <div style="line-height:1.9;color:#303133;white-space:pre-wrap">{{ post.content }}</div>
        <el-divider />
        <el-space>
          <el-button @click="handleLike" :type="liked?'danger':'default'">
            ❤ {{ post.likeCount }}
          </el-button>
          <el-button @click="handleCollect" :type="collected?'warning':'default'">
            ⭐ {{ collected ? '已收藏' : '收藏' }}
          </el-button>
        </el-space>
      </el-card>

      <!-- 评论区 -->
      <el-card style="margin-top:16px">
        <template #header><b>💬 评论 ({{ comments.length }})</b></template>
        <div v-if="authStore.isLoggedIn" style="margin-bottom:16px">
          <el-input v-model="commentContent" type="textarea" :rows="3" placeholder="写下你的评论..." />
          <el-button type="primary" style="margin-top:8px" @click="submitComment">发表评论</el-button>
        </div>
        <div v-for="c in comments" :key="c.id" class="comment-item">
          <el-avatar :src="c.user?.avatar" :size="36">{{ c.user?.nickname?.[0] }}</el-avatar>
          <div class="comment-body">
            <div class="comment-author">{{ c.user?.nickname }}</div>
            <div class="comment-content">{{ c.content }}</div>
            <div class="comment-meta">{{ c.createdAt?.slice(0,16) }}</div>
          </div>
        </div>
        <el-empty v-if="!comments.length" description="暂无评论" :image-size="60" />
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getPost, likePost } from '@/api/post'
import { getComments, createComment } from '@/api/comment'
import { toggleCollection, checkCollection } from '@/api/collection'
import { ElMessage } from 'element-plus'

const route = useRoute()
const authStore = useAuthStore()
const post = ref(null)
const comments = ref([])
const loading = ref(false)
const liked = ref(false)
const collected = ref(false)
const commentContent = ref('')

onMounted(async () => {
  loading.value = true
  try {
    const [postRes, comRes] = await Promise.all([
      getPost(route.params.id),
      getComments({ targetId: route.params.id, targetType: 'post' })
    ])
    post.value = postRes.data
    comments.value = comRes.data || []
    if (authStore.isLoggedIn) {
      const colRes = await checkCollection({ targetId: route.params.id, targetType: 'post' })
      collected.value = colRes.data
    }
  } finally {
    loading.value = false
  }
})

async function handleLike() {
  if (!authStore.isLoggedIn) return ElMessage.warning('请先登录')
  await likePost(route.params.id)
  liked.value = !liked.value
  post.value.likeCount += liked.value ? 1 : -1
}

async function handleCollect() {
  if (!authStore.isLoggedIn) return ElMessage.warning('请先登录')
  const res = await toggleCollection({ targetId: route.params.id, targetType: 'post' })
  collected.value = res.data
}

async function submitComment() {
  if (!commentContent.value.trim()) return
  await createComment({ targetId: route.params.id, targetType: 'post', content: commentContent.value })
  commentContent.value = ''
  const res = await getComments({ targetId: route.params.id, targetType: 'post' })
  comments.value = res.data || []
}
</script>

<style scoped>
.comment-item { display: flex; gap: 12px; margin-bottom: 16px; }
.comment-body { flex: 1; }
.comment-author { font-weight: 600; font-size: 13px; margin-bottom: 4px; }
.comment-content { font-size: 14px; line-height: 1.6; }
.comment-meta { font-size: 12px; color: #909399; margin-top: 4px; }
</style>
