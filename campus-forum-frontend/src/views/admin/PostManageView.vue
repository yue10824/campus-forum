<template>
  <div>
    <h2 class="page-title" style="margin-bottom:20px">📝 帖子管理</h2>
    <el-card>
      <el-form inline>
        <el-form-item>
          <el-input v-model="keyword" placeholder="搜索帖子标题" clearable @keyup.enter="load" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="status" placeholder="状态筛选" clearable style="width:120px" @change="load">
            <el-option label="正常" :value="1" />
            <el-option label="已下线" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="load">搜索</el-button></el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="标题" prop="title" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{row}">
            <el-tag :type="row.status===1?'success':'warning'" size="small">
              {{ row.status===1?'正常':'已下线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="浏览/点赞" width="110">
          <template #default="{row}">{{ row.viewCount }}/{{ row.likeCount }}</template>
        </el-table-column>
        <el-table-column label="发布时间" prop="createdAt" width="160" />
        <el-table-column label="操作" width="300">
          <template #default="{row}">
            <el-button size="small" @click="aiReview(row)" :loading="row.reviewing">AI检测</el-button>
            <el-button size="small" type="success" @click="setStatus(row, 1)" v-if="row.status!==1">上线</el-button>
            <el-button size="small" type="warning" @click="setStatus(row, 0)" v-if="row.status===1">下线</el-button>
            <el-button size="small" type="info" @click="unbanByPost(row)" title="解除该帖发帖人的禁发">解禁</el-button>
            <el-button size="small" type="danger" @click="setStatus(row, -1)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top:16px;text-align:right">
        <el-pagination v-model:current-page="page" :total="total" layout="prev, pager, next" @current-change="load" />
      </div>
    </el-card>

    <!-- AI检测结果 -->
    <el-dialog v-model="showReview" title="🤖 AI内容检测结果" width="460px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="违规概率">
          <el-progress :percentage="reviewResult.score" :color="reviewResult.score>60?'#f56c6c':'#67c23a'" />
          <span style="margin-left:8px;font-size:13px">{{ reviewResult.score }}分</span>
        </el-descriptions-item>
        <el-descriptions-item label="违规类型">{{ reviewResult.tags?.join(' / ') || '无' }}</el-descriptions-item>
        <el-descriptions-item label="建议">{{ reviewResult.suggestion }}</el-descriptions-item>
      </el-descriptions>

      <!-- 违规分高时显示快速处理按钮 -->
      <div v-if="reviewResult.score > 60" style="margin-top:16px;padding:12px;background:#fff2f0;border-radius:8px;border:1px solid #ffccc7">
        <div style="color:#f56c6c;font-size:13px;margin-bottom:10px">
          ⚠️ AI检测违规概率较高（{{ reviewResult.score }}分），建议处理：
        </div>
        <el-button type="danger" size="small" :loading="banning" @click="aiTakedown">
          🚫 一键下线 + 禁止发帖30分钟
        </el-button>
        <div style="font-size:12px;color:#999;margin-top:6px">
          操作后将自动通知该用户，并禁止其发帖30分钟
        </div>
      </div>

      <template #footer>
        <el-button @click="showReview=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import http from '@/api/http'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const loading = ref(false)
const keyword = ref('')
const status = ref(null)
const page = ref(1)
const total = ref(0)
const showReview = ref(false)
const reviewResult = ref({})
const currentReviewRow = ref(null)
const banning = ref(false)

async function load() {
  loading.value = true
  const res = await http.get('/admin/posts', { params: { page: page.value, size: 10, keyword: keyword.value, status: status.value } })
  list.value = res.data?.records || []
  total.value = res.data?.total || 0
  loading.value = false
}

async function setStatus(row, s) {
  if (s === -1) {
    try {
      await ElMessageBox.confirm('确定永久删除该帖子吗？删除后不可恢复。', '删除确认', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消', confirmButtonClass: 'el-button--danger' })
    } catch { return }
  }
  await http.put(`/admin/posts/${row.id}/status`, null, { params: { status: s } })
  ElMessage.success(s === 1 ? '已上线' : s === 0 ? '已下线' : '已删除')
  if (s === -1) {
    // 删除后直接从列表移除该行，无需刷新
    list.value = list.value.filter(item => item.id !== row.id)
  } else {
    load()
  }
}

async function aiReview(row) {
  row.reviewing = true
  currentReviewRow.value = row
  try {
    const res = await http.post(`/admin/posts/${row.id}/ai-review`)
    const d = res.data || {}
    reviewResult.value = {
      score: d.violationScore ?? d.score ?? 0,
      tags: d.violationType ? [d.violationType] : (d.tags || []),
      suggestion: d.suggestion || '无',
      postId: row.id
    }
    showReview.value = true
  } finally {
    row.reviewing = false
  }
}

async function aiTakedown() {
  const row = currentReviewRow.value
  if (!row) return
  try {
    banning.value = true
    const reason = reviewResult.value.tags?.join('、') || ''
    await http.post(`/admin/posts/${row.id}/ai-takedown`, null, {
      params: { reason: reason }
    })
    ElMessage.success('已下线帖子，用户已被禁发30分钟，并收到站内通知')
    showReview.value = false
    load()
  } finally {
    banning.value = false
  }
}

async function unbanByPost(row) {
  await ElMessageBox.confirm(`确定解除该帖子发帖人的禁发限制吗？`, '解除禁发', { type: 'warning' })
  await http.post(`/admin/posts/${row.id}/unban-post`)
  ElMessage.success('已解除禁发，用户已收到通知')
}

onMounted(load)
</script>
