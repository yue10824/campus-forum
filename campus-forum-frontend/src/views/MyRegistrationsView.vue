<template>
  <div>
    <h2 class="page-title" style="margin-bottom:20px">📋 我的报名记录</h2>
    <el-table :data="list" v-loading="loading" border>
      <el-table-column label="活动ID" prop="activityId" width="100" />
      <el-table-column label="报名时间" prop="createdAt" width="180" />
      <el-table-column label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.status===1?'success':row.status===2?'danger':'warning'">
            {{ row.status===1?'已通过':row.status===2?'已拒绝':'待审核' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="{row}">
          <el-button size="small" @click="router.push(`/activity/${row.activityId}`)">查看活动</el-button>
          <el-button size="small" type="danger" @click="cancelReg(row)" v-if="row.status===0">取消报名</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyRegistrations, cancel } from '@/api/registration'
import { ElMessage, ElMessageBox } from 'element-plus'
const router = useRouter()
const list = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  const res = await getMyRegistrations({ page: 1, size: 20 })
  list.value = res.data?.records || []
  loading.value = false
})

async function cancelReg(row) {
  await ElMessageBox.confirm('确认取消报名？')
  await cancel(row.activityId)
  list.value = list.value.filter(r => r.id !== row.id)
  ElMessage.success('已取消报名')
}
</script>
