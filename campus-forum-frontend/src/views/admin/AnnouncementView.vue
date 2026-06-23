<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px">
      <h2 class="page-title">📢 公告管理</h2>
      <el-button type="primary" @click="showDialog=true">+ 发布公告</el-button>
    </div>
    <el-card>
      <el-table :data="list" v-loading="loading" border>
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="标题" prop="title" />
        <el-table-column label="状态" width="90">
          <template #default="{row}">
            <el-tag :type="row.status===1?'success':'info'" size="small">{{ row.status===1?'发布中':'已停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" prop="createdAt" width="160" />
        <el-table-column label="操作" width="180">
          <template #default="{row}">
            <el-button size="small" @click="toggleStatus(row)">{{ row.status===1?'停用':'启用' }}</el-button>
            <el-button size="small" type="danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDialog" title="发布公告" width="500px">
      <el-form :model="form" label-width="70px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="5" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="create">发布</el-button>
        <el-button @click="showDialog=false">取消</el-button>
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
const showDialog = ref(false)
const form = ref({ title: '', content: '' })

async function load() {
  loading.value = true
  const res = await http.get('/admin/announcements', { params: { page: 1, size: 20 } })
  list.value = res.data?.records || []
  loading.value = false
}

async function create() {
  await http.post('/admin/announcements', form.value)
  ElMessage.success('发布成功')
  showDialog.value = false
  form.value = { title: '', content: '' }
  load()
}

async function toggleStatus(row) {
  await http.put(`/admin/announcements/${row.id}/status`, null, { params: { status: row.status===1?0:1 } })
  row.status = row.status === 1 ? 0 : 1
}

async function del(row) {
  await ElMessageBox.confirm('确认删除？')
  await http.delete(`/admin/announcements/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>
