<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px">
      <h2 class="page-title">🎉 校园活动</h2>
      <el-button type="primary" @click="router.push('/activity/create')" v-if="authStore.isLoggedIn">
        + 发布活动
      </el-button>
    </div>

    <!-- 筛选栏 -->
    <el-card shadow="never" style="margin-bottom:20px">
      <el-row :gutter="16" align="middle">
        <el-col :span="8">
          <el-input v-model="keyword" placeholder="搜索活动..." clearable @change="load" prefix-icon="Search" />
        </el-col>
        <el-col :span="6">
          <el-select v-model="statusFilter" placeholder="活动状态" clearable @change="load" style="width:100%">
            <el-option label="报名中" :value="1" />
            <el-option label="进行中" :value="2" />
            <el-option label="已结束" :value="3" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button @click="load">搜索</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 活动卡片列表 -->
    <el-row :gutter="20" v-loading="loading">
      <el-col :span="8" v-for="a in activities" :key="a.id" style="margin-bottom:20px">
        <el-card class="card-hover" shadow="hover" @click="router.push(`/activity/${a.id}`)">
          <el-image :src="a.coverImage" style="width:100%;height:160px;border-radius:6px" fit="cover">
            <template #error><div class="img-placeholder">📷 暂无封面</div></template>
          </el-image>
          <div style="padding:12px 0 0">
            <div style="font-size:15px;font-weight:600;margin-bottom:8px;line-height:1.4">{{ a.title }}</div>
            <div style="display:flex;gap:8px;margin-bottom:8px;flex-wrap:wrap">
              <el-tag size="small">{{ a.location }}</el-tag>
              <el-tag size="small" type="success" v-if="a.status===1">报名中</el-tag>
              <el-tag size="small" type="warning" v-else-if="a.status===2">进行中</el-tag>
              <el-tag size="small" type="info" v-else>已结束</el-tag>
            </div>
            <div style="color:#909399;font-size:12px;display:flex;justify-content:space-between">
              <span>📅 {{ a.startTime?.slice(0,10) }}</span>
              <span>👥 {{ a.registeredCount || 0 }}/{{ a.maxParticipants || '不限' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && !activities.length" description="暂无活动" />

    <div style="text-align:center;margin-top:20px">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        layout="prev, pager, next"
        @current-change="load"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getActivities } from '@/api/activity'

const router = useRouter()
const authStore = useAuthStore()
const activities = ref([])
const loading = ref(false)
const keyword = ref('')
const statusFilter = ref(null)
const page = ref(1)
const size = ref(9)
const total = ref(0)

async function load() {
  loading.value = true
  try {
    const res = await getActivities({ page: page.value, size: size.value, keyword: keyword.value, status: statusFilter.value })
    activities.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.img-placeholder {
  height: 160px; display: flex; align-items: center; justify-content: center;
  background: #f5f7fa; color: #909399; font-size: 14px;
}
</style>
