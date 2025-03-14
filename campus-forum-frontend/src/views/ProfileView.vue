<template>
  <div v-loading="loading">
    <template v-if="user">
      <!-- 个人信息头部 -->
      <el-card style="margin-bottom:20px">
        <div class="profile-header">
          <el-avatar :src="user.avatar" :size="80">{{ user.nickname?.[0] }}</el-avatar>
          <div class="profile-info">
            <div class="nickname-row">
              <h2>{{ user.nickname }}</h2>
              <el-tag :type="levelInfo.tagType" size="small" style="margin-left:10px">
                {{ levelInfo.icon }} Lv.{{ user.level || 1 }} {{ levelInfo.title }}
              </el-tag>
            </div>
            <!-- 经验值进度条 -->
            <div class="exp-bar-wrap">
              <span class="exp-label">经验值 {{ user.exp || 0 }} / {{ levelInfo.nextExp }}</span>
              <el-progress
                :percentage="expPercent"
                :color="levelInfo.color"
                :stroke-width="8"
                style="flex:1;margin-left:10px"
              />
            </div>
            <div class="profile-stats">
              <span>帖子 <b>{{ user.postCount || 0 }}</b></span>
              <span>关注 <b>{{ followingCount }}</b></span>
              <span>粉丝 <b>{{ followersCount }}</b></span>
            </div>
            <div style="color:#606266;margin-top:8px">{{ user.bio || '这个人很懒，什么都没写' }}</div>
          </div>
          <div class="profile-actions" v-if="authStore.user?.id != route.params.id">
            <el-button :type="isFollowing?'default':'primary'" @click="handleFollow">
              {{ isFollowing ? '已关注' : '+ 关注' }}
            </el-button>
          </div>
          <div v-else>
            <el-button @click="editVisible=true">编辑资料</el-button>
          </div>
        </div>
      </el-card>

      <!-- 等级说明卡片 -->
      <el-card style="margin-bottom:20px" shadow="never">
        <template #header><b>🏅 等级体系</b></template>
        <div class="level-list">
          <div v-for="lv in allLevels" :key="lv.level"
            :class="['level-item', (user.level||1) === lv.level ? 'level-active' : '']">
            <span class="level-badge" :style="{background: lv.color}">Lv.{{ lv.level }}</span>
            <span class="level-title">{{ lv.title }}</span>
            <span class="level-exp">{{ lv.minExp }}exp</span>
          </div>
        </div>
      </el-card>

      <!-- 帖子列表 -->
      <el-tabs>
        <el-tab-pane label="他的帖子">
          <el-card v-for="p in posts" :key="p.id" class="post-card card-hover" @click="router.push(`/post/${p.id}`)">
            <div class="post-title">{{ p.title }}</div>
            <div style="color:#909399;font-size:12px">{{ p.createdAt?.slice(0,10) }}</div>
          </el-card>
          <el-empty v-if="!posts.length" description="暂无帖子" />
        </el-tab-pane>
      </el-tabs>
    </template>

    <!-- 编辑资料对话框 -->
    <el-dialog v-model="editVisible" title="编辑资料" width="400px">
      <el-form :model="editForm" label-width="60px">
        <el-form-item label="昵称"><el-input v-model="editForm.nickname" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="editForm.bio" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="saveProfile">保存</el-button>
        <el-button @click="editVisible=false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getUser, toggleFollow, isFollowing as checkFollow, updateProfile } from '@/api/user'
import { getPosts } from '@/api/post'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const user = ref(null)
const posts = ref([])
const loading = ref(false)
const isFollowing = ref(false)
const followingCount = ref(0)
const followersCount = ref(0)
const editVisible = ref(false)
const editForm = ref({ nickname: '', bio: '' })

// 等级配置表
const allLevels = [
  { level: 1,  title: '新手萌芽', minExp: 0,    nextExp: 50,   color: '#95de64', tagType: 'info' },
  { level: 2,  title: '初露锋芒', minExp: 50,   nextExp: 150,  color: '#52c41a', tagType: 'success' },
  { level: 3,  title: '小有名气', minExp: 150,  nextExp: 300,  color: '#1890ff', tagType: '' },
  { level: 4,  title: '声名鹊起', minExp: 300,  nextExp: 500,  color: '#096dd9', tagType: '' },
  { level: 5,  title: '叱咤风云', minExp: 500,  nextExp: 800,  color: '#722ed1', tagType: 'warning' },
  { level: 6,  title: '誉满校园', minExp: 800,  nextExp: 1200, color: '#eb2f96', tagType: 'warning' },
  { level: 7,  title: '学霸达人', minExp: 1200, nextExp: 1800, color: '#f5222d', tagType: 'danger' },
  { level: 8,  title: '校园传说', minExp: 1800, nextExp: 2500, color: '#fa8c16', tagType: 'danger' },
  { level: 9,  title: '院内宗师', minExp: 2500, nextExp: 3500, color: '#fadb14', tagType: 'danger' },
  { level: 10, title: '无冕之王', minExp: 3500, nextExp: 9999, color: '#13c2c2', tagType: 'danger' },
]

const levelInfo = computed(() => {
  const lv = Math.min(Math.max(user.value?.level || 1, 1), 10)
  return allLevels[lv - 1]
})

const expPercent = computed(() => {
  if (!user.value) return 0
  const lv = allLevels[(user.value.level || 1) - 1]
  const prev = lv.minExp
  const next = lv.nextExp
  const cur = user.value.exp || 0
  if (next >= 9999) return 100
  return Math.min(Math.round(((cur - prev) / (next - prev)) * 100), 100)
})

onMounted(async () => {
  loading.value = true
  try {
    const [userRes, postRes] = await Promise.all([
      getUser(route.params.id),
      getPosts({ page: 1, size: 10 })
    ])
    user.value = userRes.data
    posts.value = postRes.data?.records || []
    editForm.value.nickname = user.value.nickname
    editForm.value.bio = user.value.bio
    if (authStore.isLoggedIn && authStore.user?.id != route.params.id) {
      const followRes = await checkFollow(route.params.id)
      isFollowing.value = followRes.data
    }
  } finally {
    loading.value = false
  }
})

async function handleFollow() {
  const res = await toggleFollow(route.params.id)
  isFollowing.value = res.data
  ElMessage.success(isFollowing.value ? '已关注' : '已取消关注')
}

async function saveProfile() {
  const res = await updateProfile(editForm.value)
  authStore.updateUser(res.data)
  user.value = res.data
  editVisible.value = false
  ElMessage.success('保存成功')
}
</script>

<style scoped>
.profile-header { display: flex; gap: 20px; align-items: flex-start; }
.profile-info { flex: 1; }
.profile-info h2 { font-size: 20px; margin: 0; }
.nickname-row { display: flex; align-items: center; margin-bottom: 10px; }
.exp-bar-wrap { display: flex; align-items: center; margin-bottom: 10px; }
.exp-label { font-size: 12px; color: #909399; white-space: nowrap; }
.profile-stats { display: flex; gap: 20px; color: #606266; margin-top: 6px; }
.post-card { margin-bottom: 8px; padding: 12px; cursor: pointer; }
.post-title { font-size: 14px; font-weight: 500; margin-bottom: 4px; }

/* 等级列表 */
.level-list { display: flex; flex-wrap: wrap; gap: 10px; }
.level-item {
  display: flex; align-items: center; gap: 6px;
  padding: 6px 12px; border-radius: 20px;
  border: 1px solid #eee; background: #fafafa;
  font-size: 13px; cursor: default;
}
.level-active {
  border-color: #409EFF; background: #ecf5ff;
  box-shadow: 0 0 6px rgba(64,158,255,0.3);
}
.level-badge {
  color: #fff; padding: 2px 7px; border-radius: 10px; font-size: 12px; font-weight: 600;
}
.level-title { font-weight: 500; }
.level-exp { color: #909399; font-size: 12px; }
</style>
