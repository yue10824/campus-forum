<template>
  <div v-loading="loading">
    <template v-if="activity">
      <el-row :gutter="24">
        <el-col :span="16">
          <el-card>
            <el-image :src="activity.coverImage" style="width:100%;max-height:300px;border-radius:8px" fit="cover">
              <template #error><div style="height:200px;background:#f5f7fa;display:flex;align-items:center;justify-content:center;font-size:24px">📷</div></template>
            </el-image>
            <div style="padding:20px 0 0">
              <h1 style="font-size:22px;margin-bottom:12px">{{ activity.title }}</h1>
              <el-descriptions :column="2" border>
                <el-descriptions-item label="📍 地点">{{ activity.location }}</el-descriptions-item>
                <el-descriptions-item label="👥 人数限制">{{ activity.maxParticipants || '不限' }}</el-descriptions-item>
                <el-descriptions-item label="📅 开始时间">{{ activity.startTime }}</el-descriptions-item>
                <el-descriptions-item label="📅 结束时间">{{ activity.endTime }}</el-descriptions-item>
              </el-descriptions>
              <div style="margin-top:16px;line-height:1.8;color:#606266" v-html="activity.description" />

              <!-- 操作按钮 -->
              <el-divider />
              <el-space>
                <el-button
                  type="primary"
                  @click="handleRegister"
                  :disabled="activity.status !== 1 || registered"
                  v-if="authStore.isLoggedIn"
                >{{ registered ? '已报名' : '立即报名' }}</el-button>
                <el-button @click="handleLike" :type="liked ? 'danger' : 'default'">
                  ❤ {{ activity.likeCount || 0 }}
                </el-button>
                <el-button @click="handleCollect" :type="collected ? 'warning' : 'default'">
                  ⭐ {{ collected ? '已收藏' : '收藏' }}
                </el-button>
              </el-space>
            </div>
          </el-card>

          <!-- 评论区 -->
          <el-card style="margin-top:16px">
            <template #header><b>💬 评论 ({{ comments.length }})</b></template>
            <div v-if="authStore.isLoggedIn" style="margin-bottom:16px">
              <el-input v-model="commentContent" placeholder="写下你的评论..." type="textarea" :rows="3" />
              <el-button type="primary" style="margin-top:8px" @click="submitComment">发表评论</el-button>
            </div>
            <div v-for="c in comments" :key="c.id" class="comment-item">
              <el-avatar :src="c.user?.avatar" :size="36">{{ c.user?.nickname?.[0] }}</el-avatar>
              <div class="comment-body">
                <div class="comment-author">{{ c.user?.nickname }}</div>
                <div class="comment-content">{{ c.content }}</div>
                <div class="comment-meta">{{ c.createdAt?.slice(0,16) }}</div>
                <!-- 子评论 -->
                <div v-for="r in c.replies" :key="r.id" class="reply-item">
                  <b>{{ r.user?.nickname }}</b>：{{ r.content }}
                </div>
              </div>
            </div>
            <el-empty v-if="!comments.length" description="暂无评论" :image-size="60" />
          </el-card>
        </el-col>

        <!-- 侧栏 -->
        <el-col :span="8">
          <el-card>
            <template #header><b>📋 报名信息</b></template>
            <el-statistic title="已报名" :value="activity.registeredCount || 0" />
            <el-progress
              :percentage="activity.maxParticipants ? Math.round((activity.registeredCount||0)/activity.maxParticipants*100) : 0"
              style="margin-top:12px"
            />
            <el-divider />
            <el-tag :type="activity.status===1?'success':activity.status===2?'warning':'info'" size="large">
              {{ activity.status===1 ? '报名中' : activity.status===2 ? '进行中' : '已结束' }}
            </el-tag>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getActivity, likeActivity } from '@/api/activity'
import { getComments, createComment } from '@/api/comment'
import { register as doRegister } from '@/api/registration'
import { toggleCollection, checkCollection } from '@/api/collection'
import { ElMessage } from 'element-plus'

const route = useRoute()
const authStore = useAuthStore()
const activity = ref(null)
const comments = ref([])
const loading = ref(false)
const liked = ref(false)
const collected = ref(false)
const registered = ref(false)
const commentContent = ref('')

onMounted(async () => {
  loading.value = true
  try {
    const [actRes, comRes] = await Promise.all([
      getActivity(route.params.id),
      getComments({ targetId: route.params.id, targetType: 'activity' })
    ])
    activity.value = actRes.data
    comments.value = comRes.data || []
    if (authStore.isLoggedIn) {
      const colRes = await checkCollection({ targetId: route.params.id, targetType: 'activity' })
      collected.value = colRes.data
    }
  } finally {
    loading.value = false
  }
})

async function handleLike() {
  if (!authStore.isLoggedIn) return ElMessage.warning('请先登录')
  await likeActivity(route.params.id)
  liked.value = !liked.value
  activity.value.likeCount += liked.value ? 1 : -1
}

async function handleCollect() {
  if (!authStore.isLoggedIn) return ElMessage.warning('请先登录')
  const res = await toggleCollection({ targetId: route.params.id, targetType: 'activity' })
  collected.value = res.data
  ElMessage.success(collected.value ? '已收藏' : '已取消收藏')
}

async function handleRegister() {
  try {
    await doRegister({ activityId: route.params.id })
    registered.value = true
    ElMessage.success('报名成功！')
  } catch (e) {}
}

async function submitComment() {
  if (!commentContent.value.trim()) return
  await createComment({ targetId: route.params.id, targetType: 'activity', content: commentContent.value })
  commentContent.value = ''
  const res = await getComments({ targetId: route.params.id, targetType: 'activity' })
  comments.value = res.data || []
}
</script>

<style scoped>
.comment-item { display: flex; gap: 12px; margin-bottom: 16px; }
.comment-body { flex: 1; }
.comment-author { font-weight: 600; font-size: 13px; margin-bottom: 4px; }
.comment-content { font-size: 14px; color: #303133; line-height: 1.6; }
.comment-meta { font-size: 12px; color: #909399; margin-top: 4px; }
.reply-item { background: #f9f9f9; padding: 6px 10px; border-radius: 4px; margin-top: 6px; font-size: 13px; }
</style>
