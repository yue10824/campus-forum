<template>
  <div>
    <h2 class="page-title" style="margin-bottom:20px">💬 私信</h2>
    <el-card class="chat-container">
      <div class="messages" ref="msgBox">
        <div v-for="m in messages" :key="m.id" :class="['msg', m.senderId === myId ? 'mine' : 'others']">
          <div class="bubble">{{ m.content }}</div>
          <div class="time">{{ m.createdAt?.slice(11,16) }}</div>
        </div>
      </div>
      <div class="input-area">
        <el-input v-model="input" placeholder="发消息..." @keyup.enter="send" />
        <el-button type="primary" @click="send">发送</el-button>
      </div>
    </el-card>
    <div style="color:#909399;font-size:12px;margin-top:8px;text-align:center">
      💡 提示：私信功能通过 WebSocket 实时传输
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useAuthStore } from '@/stores/auth'
const authStore = useAuthStore()
const myId = authStore.user?.id
const messages = ref([])
const input = ref('')
const msgBox = ref()
let ws = null

onMounted(() => {
  ws = new WebSocket(`ws://localhost:8080/ws/chat?token=${authStore.token}`)
  ws.onmessage = (e) => {
    try {
      const msg = JSON.parse(e.data)
      messages.value.push(msg)
      nextTick(() => msgBox.value?.scrollTo(0, msgBox.value.scrollHeight))
    } catch {}
  }
})

function send() {
  if (!input.value.trim() || !ws) return
  ws.send(JSON.stringify({ content: input.value }))
  messages.value.push({ senderId: myId, content: input.value, createdAt: new Date().toISOString() })
  input.value = ''
  nextTick(() => msgBox.value?.scrollTo(0, msgBox.value.scrollHeight))
}
</script>

<style scoped>
.chat-container { height: calc(100vh - 200px); display: flex; flex-direction: column; }
.messages { flex: 1; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 12px; }
.msg { display: flex; flex-direction: column; }
.msg.mine { align-items: flex-end; }
.msg.others { align-items: flex-start; }
.bubble { max-width: 60%; padding: 8px 12px; border-radius: 12px; font-size: 14px; line-height: 1.5; }
.mine .bubble { background: #409EFF; color: #fff; border-radius: 12px 2px 12px 12px; }
.others .bubble { background: #f0f0f0; color: #303133; border-radius: 2px 12px 12px 12px; }
.time { font-size: 11px; color: #909399; margin-top: 2px; }
.input-area { display: flex; gap: 8px; padding: 12px; border-top: 1px solid #f0f0f0; }
</style>
