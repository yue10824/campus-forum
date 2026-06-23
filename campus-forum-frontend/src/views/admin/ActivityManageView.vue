<template>
  <div>
    <h2 class="page-title" style="margin-bottom:20px">🎉 活动管理</h2>
    <el-card>
      <el-table :data="list" v-loading="loading" border>
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="标题" prop="title" show-overflow-tooltip />
        <el-table-column label="地点" prop="location" width="120" />
        <el-table-column label="报名进度" width="180">
          <template #default="{row}">
            <el-progress
              :percentage="row.maxParticipants ? Math.min(100, Math.round((row.registeredCount||0)/row.maxParticipants*100)) : 0"
              :status="(row.registeredCount||0)>=(row.maxParticipants||Infinity)?'success':''"
            />
            <div style="font-size:11px;color:#909399">{{ row.registeredCount||0 }}/{{ row.maxParticipants||'不限' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{row}">
            <el-tag :type="row.status===1?'success':row.status===2?'warning':'info'" size="small">
              {{ row.status===1?'报名中':row.status===2?'进行中':'已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{row}">
            <el-button size="small" type="success" @click="setStatus(row, 1)" v-if="row.status!==1">上线</el-button>
            <el-button size="small" type="info" @click="setStatus(row, 0)" v-if="row.status===1">下线</el-button>
            <el-button size="small" type="danger" @click="setStatus(row, 0)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:16px;text-align:right">
        <el-pagination v-model:current-page="page" :total="total" layout="prev, pager, next" @current-change="load" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import http from '@/api/http'
const list = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)

async function load() {
  loading.value = true
  const res = await http.get('/admin/activities', { params: { page: page.value, size: 10 } })
  list.value = res.data?.records || []
  total.value = res.data?.total || 0
  loading.value = false
}

async function setStatus(row, s) {
  await http.put(`/admin/activities/${row.id}/status`, null, { params: { status: s } })
  row.status = s
}
onMounted(load)
</script>
