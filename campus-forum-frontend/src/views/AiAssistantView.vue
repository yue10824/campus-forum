<template>
  <div>
    <h2 class="page-title" style="margin-bottom:20px">🤖 AI活动助手</h2>
    <el-card class="chat-wrap">
      <!-- 快捷问题 -->
      <div class="quick-btns">
        <el-tag
          v-for="q in quickQuestions" :key="q"
          style="cursor:pointer;margin-right:8px;margin-bottom:8px"
          @click="sendMsg(q)"
        >{{ q }}</el-tag>
      </div>
      <el-divider />

      <!-- 对话区 -->
      <div class="messages" ref="msgBox">
        <div v-for="(m, i) in messages" :key="i" :class="['msg', m.role === 'user' ? 'mine' : 'ai']">
          <div class="bubble" v-html="m.role === 'assistant' ? renderMd(m.content) : m.content" />
        </div>
        <div v-if="streaming" class="msg ai">
          <div class="bubble typing">{{ streamBuffer }}<span class="cursor">|</span></div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="input-area">
        <el-input
          v-model="input"
          placeholder="问我任何关于校园活动的问题..."
          @keyup.enter="sendMsg()"
          :disabled="streaming"
        />
        <el-button type="primary" @click="sendMsg()" :loading="streaming">发送</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { marked } from 'marked'

const authStore = useAuthStore()
const messages = ref([])
const input = ref('')
const msgBox = ref()
const streaming = ref(false)
const streamBuffer = ref('')
const sessionId = ref(`session_${Date.now()}`)

const quickQuestions = ['有什么近期活动？', '如何报名社团活动？', '帮我写活动简介', '校园活动有哪些类型？']

function renderMd(text) {
  return marked.parse(text || '')
}

async function sendMsg(text) {
  const content = text || input.value.trim()
  if (!content) return
  input.value = ''
  messages.value.push({ role: 'user', content })
  streaming.value = true
  streamBuffer.value = ''
  scrollBottom()

  try {
    // SSE流式输出 - 使用 fetch + ReadableStream（踩坑点：不能用普通axios）
    const token = authStore.token
    const params = new URLSearchParams({ message: content, sessionId: sessionId.value })
    const res = await fetch(`/api/ai/chat?${params}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    const reader = res.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let aiContent = ''
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      // 用 stream:true 防止多字节中文字符在分片边界被截断
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      // 最后一行可能不完整，留到下次拼接
      buffer = lines.pop()
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.slice(5).trim()
          if (data && data !== '[DONE]') {
            aiContent += data
            streamBuffer.value = aiContent
            scrollBottom()
          }
        }
      }
    }
    // 处理最后剩余的buffer
    if (buffer.startsWith('data:')) {
      const data = buffer.slice(5).trim()
      if (data && data !== '[DONE]') aiContent += data
    }
    messages.value.push({ role: 'assistant', content: aiContent })
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '抱歉，AI服务暂时不可用，请稍后再试。' })
  } finally {
    streaming.value = false
    streamBuffer.value = ''
    scrollBottom()
  }
}

function scrollBottom() {
  nextTick(() => msgBox.value?.scrollTo(0, msgBox.value.scrollHeight))
}
</script>

<style scoped>
.chat-wrap { display: flex; flex-direction: column; height: calc(100vh - 180px); }
.quick-btns { padding-bottom: 4px; }
.messages { flex: 1; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 12px; }
.msg { display: flex; }
.msg.mine { justify-content: flex-end; }
.msg.ai { justify-content: flex-start; }
.bubble {
  max-width: 70%; padding: 10px 14px; border-radius: 12px;
  font-size: 14px; line-height: 1.7;
}
.mine .bubble { background: #409EFF; color: #fff; border-radius: 12px 2px 12px 12px; }
.ai .bubble { background: #f0f0f0; color: #303133; border-radius: 2px 12px 12px 12px; }
.typing { animation: none; }
.cursor { animation: blink 1s infinite; }
@keyframes blink { 0%,100%{opacity:1} 50%{opacity:0} }
.input-area { display: flex; gap: 8px; padding: 12px 0 0; border-top: 1px solid #f0f0f0; }
</style>
