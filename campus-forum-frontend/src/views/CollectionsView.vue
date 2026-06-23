<template>
  <div>
    <h2 class="page-title" style="margin-bottom:20px">⭐ 我的收藏</h2>
    <el-tabs v-model="activeTab" @tab-click="load">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="活动" name="activity" />
      <el-tab-pane label="帖子" name="post" />
    </el-tabs>
    <el-card v-for="c in list" :key="c.id" class="card-hover" style="margin-bottom:8px" @click="goDetail(c)">
      <div style="font-size:14px;font-weight:500">
        {{ c.targetType === 'activity' ? '🎉 活动' : '📝 帖子' }} ID: {{ c.targetId }}
      </div>
      <div style="font-size:12px;color:#909399">收藏于 {{ c.createdAt?.slice(0,10) }}</div>
    </el-card>
    <el-empty v-if="!list.length" description="暂无收藏" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyCollections } from '@/api/collection'
const router = useRouter()
const list = ref([])
const activeTab = ref('')

async function load() {
  const res = await getMyCollections({ targetType: activeTab.value || undefined, page: 1, size: 20 })
  list.value = res.data?.records || []
}
function goDetail(c) {
  router.push(c.targetType === 'activity' ? `/activity/${c.targetId}` : `/post/${c.targetId}`)
}
onMounted(load)
</script>
