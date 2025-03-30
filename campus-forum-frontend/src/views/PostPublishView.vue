<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px">
      <h2 class="page-title">📝 发布帖子</h2>
    </div>
    <el-card>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="给帖子起个好标题..." maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="版块" prop="sectionId">
          <el-select v-model="form.sectionId" placeholder="选择版块" style="width:200px">
            <el-option v-for="s in sections" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="12" placeholder="分享你的想法..." />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">发布</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createPost } from '@/api/post'
import { getSections } from '@/api/user'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const sections = ref([])
const form = ref({ title: '', sectionId: null, content: '' })
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  sectionId: [{ required: true, message: '请选择版块', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
}

onMounted(async () => {
  const res = await getSections()
  sections.value = res.data || []
})

async function handleSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await createPost(form.value)
    ElMessage.success('发布成功！')
    router.push('/discover')
  } finally {
    loading.value = false
  }
}
</script>
