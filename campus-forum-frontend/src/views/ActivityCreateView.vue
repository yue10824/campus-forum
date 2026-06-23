<template>
  <div>
    <h2 class="page-title" style="margin-bottom:20px">🎉 发布活动</h2>
    <el-card>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="form.title" placeholder="活动标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="活动地点" prop="location">
          <el-input v-model="form.location" placeholder="活动地点" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width:100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" style="width:100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="人数上限">
          <el-input-number v-model="form.maxParticipants" :min="1" :max="9999" placeholder="不填则不限" />
        </el-form-item>
        <el-form-item label="活动描述" prop="description">
          <div style="width:100%">
            <el-input v-model="form.description" type="textarea" :rows="8" placeholder="详细描述活动内容..." />
            <el-button
              type="primary"
              plain
              size="small"
              style="margin-top:8px"
              :loading="aiLoading"
              @click="generateDesc"
            >🤖 AI生成简介</el-button>
            <span style="color:#909399;font-size:12px;margin-left:8px">（输入标题后点击，AI自动生成描述）</span>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">发布活动</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createActivity } from '@/api/activity'
import http from '@/api/http'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const aiLoading = ref(false)
const form = ref({
  title: '', location: '', startTime: '', endTime: '',
  maxParticipants: null, description: ''
})
const rules = {
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  description: [{ required: true, message: '请输入活动描述', trigger: 'blur' }],
}

async function generateDesc() {
  if (!form.value.title) return ElMessage.warning('请先填写活动标题')
  aiLoading.value = true
  try {
    const res = await http.post(`/ai/generate-desc?keyword=${encodeURIComponent(form.value.title)}`)
    form.value.description = res.data
    ElMessage.success('AI生成成功！')
  } finally {
    aiLoading.value = false
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await createActivity(form.value)
    ElMessage.success('活动发布成功！')
    router.push('/activity')
  } finally {
    loading.value = false
  }
}
</script>
