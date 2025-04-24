<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px">
      <h2 class="page-title">👥 用户管理</h2>
      <el-button type="primary" @click="showAdd=true">+ 新增用户</el-button>
    </div>

    <el-card>
      <el-form inline>
        <el-form-item>
          <el-input v-model="keyword" placeholder="搜索用户名/昵称" clearable @keyup.enter="load" prefix-icon="Search" />
        </el-form-item>
        <el-form-item><el-button type="primary" @click="load">搜索</el-button></el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="头像" width="60">
          <template #default="{row}"><el-avatar :src="row.avatar" :size="32">{{ row.nickname?.[0] }}</el-avatar></template>
        </el-table-column>
        <el-table-column label="用户名" prop="username" />
        <el-table-column label="昵称" prop="nickname" />
        <el-table-column label="角色" width="90">
          <template #default="{row}">
            <el-tag :type="row.role===1?'danger':'info'" size="small">{{ row.role===1?'管理员':'用户' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="账号状态" width="90">
          <template #default="{row}">
            <el-switch :model-value="row.status===1" @change="toggleStatus(row)" />
          </template>
        </el-table-column>
        <el-table-column label="发帖禁制" width="180">
          <template #default="{row}">
            <span v-if="isBanned(row)" style="color:#f56c6c;font-size:12px">
              🚫 禁发至 {{ formatBanTime(row.postBanUntil) }}
            </span>
            <span v-else style="color:#67c23a;font-size:12px">✅ 正常</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{row}">
            <el-button
              v-if="isBanned(row)"
              size="small" type="warning"
              @click="unbanUser(row)"
            >解除禁发</el-button>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" prop="createdAt" width="160" />
      </el-table>

      <div style="margin-top:16px;text-align:right">
        <el-pagination v-model:current-page="page" :total="total" layout="prev, pager, next" @current-change="load" />
      </div>
    </el-card>

    <!-- 新增用户 -->
    <el-dialog v-model="showAdd" title="新增用户" width="400px">
      <el-form :model="addForm" label-width="70px">
        <el-form-item label="用户名"><el-input v-model="addForm.username" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="addForm.nickname" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="addForm.password" type="password" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="createUser">确定</el-button>
        <el-button @click="showAdd=false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import http from '@/api/http'
import { ElMessage } from 'element-plus'

const list = ref([])
const loading = ref(false)
const keyword = ref('')
const page = ref(1)
const total = ref(0)
const showAdd = ref(false)
const addForm = ref({ username: '', nickname: '', password: '' })

function isBanned(row) {
  if (!row.postBanUntil) return false
  return new Date(row.postBanUntil) > new Date()
}

function formatBanTime(t) {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}`
}

async function load() {
  loading.value = true
  const res = await http.get('/admin/users', { params: { page: page.value, size: 10, keyword: keyword.value } })
  list.value = res.data?.records || []
  total.value = res.data?.total || 0
  loading.value = false
}

async function toggleStatus(row) {
  await http.put(`/admin/users/${row.id}/status`, null, { params: { status: row.status === 1 ? 0 : 1 } })
  row.status = row.status === 1 ? 0 : 1
}

async function unbanUser(row) {
  await http.post(`/admin/users/${row.id}/unban-post`)
  ElMessage.success('已解除禁发，用户已收到通知')
  load()
}

async function createUser() {
  await http.post('/admin/users', addForm.value)
  ElMessage.success('创建成功')
  showAdd.value = false
  load()
}

onMounted(load)
</script>
