<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px">
      <h2 class="page-title">📂 版块管理</h2>
      <el-button type="primary" @click="openAdd">+ 新增版块</el-button>
    </div>
    <el-card>
      <el-table :data="list" v-loading="loading" border>
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="名称" prop="name" width="120" />
        <el-table-column label="代码" prop="code" width="120" />
        <el-table-column label="描述" prop="description" show-overflow-tooltip />
        <el-table-column label="排序" prop="sort" width="80" />
        <el-table-column label="操作" width="160">
          <template #default="{row}">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDialog" :title="editId?'编辑版块':'新增版块'" width="400px">
      <el-form :model="form" label-width="70px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="代码" v-if="!editId"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="form.icon" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="save">保存</el-button>
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
const editId = ref(null)
const form = ref({ name: '', code: '', description: '', icon: '', sort: 100 })

async function load() {
  loading.value = true
  const res = await http.get('/admin/sections')
  list.value = res.data || []
  loading.value = false
}

function openAdd() {
  editId.value = null
  form.value = { name: '', code: '', description: '', icon: '', sort: 100 }
  showDialog.value = true
}

function openEdit(row) {
  editId.value = row.id
  form.value = { name: row.name, description: row.description, icon: row.icon, sort: row.sort }
  showDialog.value = true
}

async function save() {
  if (editId.value) {
    await http.put(`/admin/sections/${editId.value}`, form.value)
  } else {
    await http.post('/admin/sections', form.value)
  }
  ElMessage.success('保存成功')
  showDialog.value = false
  load()
}

async function del(row) {
  await ElMessageBox.confirm(`确认删除版块「${row.name}」？`)
  await http.delete(`/admin/sections/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>
