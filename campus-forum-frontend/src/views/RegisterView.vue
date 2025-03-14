<template>
  <div class="login-page">
    <el-card class="login-card" shadow="always">
      <h2 class="title">注册账号</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名（4-20位字母数字）" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="昵称" prefix-icon="UserFilled" size="large" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="邮箱地址" prefix-icon="Message" size="large" />
        </el-form-item>
        <el-form-item prop="emailCode">
          <div style="display:flex;gap:8px;width:100%">
            <el-input v-model="form.emailCode" placeholder="6位验证码" prefix-icon="Key" size="large" maxlength="6" style="flex:1" />
            <el-button
              type="primary"
              size="large"
              :disabled="countdown > 0 || !isValidEmail"
              :loading="sendingCode"
              @click="handleSendCode"
              style="min-width:110px"
            >
              {{ countdown > 0 ? `${countdown}s 后重发` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" placeholder="密码（至少6位）" type="password" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleRegister" style="width:100%">注册</el-button>
        </el-form-item>
        <div style="text-align:center">
          已有账号？<el-link type="primary" @click="router.push('/login')">去登录</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { sendCode as sendCodeApi } from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
let countdownTimer = null

const form = ref({ username: '', nickname: '', password: '', email: '', emailCode: '' })

const isValidEmail = computed(() => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email))

const rules = {
  username: [{ required: true, min: 4, max: 20, message: '用户名4-20位', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  emailCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
  ],
  password: [{ required: true, min: 6, message: '密码至少6位', trigger: 'blur' }],
}

async function handleSendCode() {
  if (!isValidEmail.value) {
    ElMessage.warning('请先输入正确的邮箱地址')
    return
  }
  sendingCode.value = true
  try {
    await sendCodeApi(form.value.email)
    ElMessage.success('验证码已发送，请查收邮箱')
    // 开始倒计时
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(countdownTimer)
        countdownTimer = null
      }
    }, 1000)
  } catch (e) {
    // 错误提示由 http 拦截器处理
  } finally {
    sendingCode.value = false
  }
}

async function handleRegister() {
  await formRef.value.validate()
  loading.value = true
  try {
    await authStore.register(form.value)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    // 错误提示由 http 拦截器处理
  } finally {
    loading.value = false
  }
}

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})
</script>

<style scoped>
.login-page {
  display: flex; justify-content: center; align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card { width: 440px; padding: 20px; border-radius: 12px; }
.title { text-align: center; margin-bottom: 24px; font-size: 22px; }
</style>
