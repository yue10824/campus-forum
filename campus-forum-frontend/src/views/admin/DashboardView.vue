<template>
  <div>
    <h2 class="page-title" style="margin-bottom:20px">📊 数据概览</h2>

    <!-- 数字大盘 -->
    <el-row :gutter="20" style="margin-bottom:24px">
      <el-col :span="6" v-for="stat in stats" :key="stat.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon">{{ stat.icon }}</div>
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区第一行 -->
    <el-row :gutter="20" style="margin-bottom:20px">
      <el-col :span="16">
        <el-card shadow="never">
          <template #header><b>📈 近7天新增用户 / 帖子趋势</b></template>
          <BaseChart :option="lineOption" height="280px" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <template #header><b>🥧 板块内容分布</b></template>
          <BaseChart :option="pieOption" height="280px" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区第二行 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><b>🏅 用户等级分布</b></template>
          <BaseChart :option="levelBarOption" height="260px" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><b>📊 活动报名量 Top5</b></template>
          <BaseChart :option="barOption" height="260px" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import BaseChart from '@/components/charts/BaseChart.vue'
import http from '@/api/http'

const stats = ref([
  { label: '用户总数', value: '-', icon: '👥' },
  { label: '活动总数', value: '-', icon: '🎉' },
  { label: '帖子总数', value: '-', icon: '📝' },
  { label: '报名总数', value: '-', icon: '📋' },
])

const lineOption = ref({
  tooltip: { trigger: 'axis' },
  legend: { data: ['新注册用户', '新发帖子'] },
  xAxis: { type: 'category', data: [] },
  yAxis: { type: 'value', minInterval: 1 },
  series: [
    { name: '新注册用户', type: 'line', smooth: true, data: [], itemStyle: { color: '#409EFF' }, areaStyle: { opacity: 0.1 } },
    { name: '新发帖子', type: 'line', smooth: true, data: [], itemStyle: { color: '#67C23A' }, areaStyle: { opacity: 0.1 } },
  ]
})

const pieOption = ref({
  tooltip: { trigger: 'item', formatter: '{b}: {c}帖 ({d}%)' },
  legend: { bottom: 0, type: 'scroll' },
  series: [{
    type: 'pie', radius: ['40%', '68%'],
    data: [],
    label: { show: false }
  }]
})

const levelBarOption = ref({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: [] },
  yAxis: { type: 'value', minInterval: 1 },
  series: [{
    type: 'bar',
    data: [],
    itemStyle: {
      color: (params) => {
        const colors = ['#95de64','#52c41a','#1890ff','#722ed1','#eb2f96','#f5222d','#fa8c16','#fadb14','#a0d911','#13c2c2']
        return colors[params.dataIndex % colors.length]
      }
    },
    label: { show: true, position: 'top' }
  }]
})

const barOption = ref({
  tooltip: {},
  xAxis: { type: 'value' },
  yAxis: { type: 'category', data: [] },
  series: [{
    type: 'bar',
    data: [],
    itemStyle: { color: '#f093fb' },
    label: { show: true, position: 'right' }
  }]
})

onMounted(async () => {
  try {
    // 大盘数字
    const statsRes = await http.get('/admin/dashboard/stats')
    const d = statsRes.data
    stats.value = [
      { label: '用户总数', value: d.userCount ?? 0, icon: '👥' },
      { label: '活动总数', value: d.activityCount ?? 0, icon: '🎉' },
      { label: '帖子总数', value: d.postCount ?? 0, icon: '📝' },
      { label: '报名总数', value: d.registrationCount ?? 0, icon: '📋' },
    ]
  } catch (e) {}

  try {
    // 趋势折线图
    const trendRes = await http.get('/admin/dashboard/trend')
    const t = trendRes.data
    lineOption.value = {
      ...lineOption.value,
      xAxis: { type: 'category', data: t.labels },
      series: [
        { ...lineOption.value.series[0], data: t.userData },
        { ...lineOption.value.series[1], data: t.postData },
      ]
    }
  } catch (e) {}

  try {
    // 板块分布饼图
    const secRes = await http.get('/admin/dashboard/section-dist')
    pieOption.value.series[0].data = secRes.data
  } catch (e) {}

  try {
    // 用户等级分布柱状图
    const lvRes = await http.get('/admin/dashboard/level-dist')
    levelBarOption.value = {
      ...levelBarOption.value,
      xAxis: { type: 'category', data: lvRes.data.map(i => i.name) },
      series: [{ ...levelBarOption.value.series[0], data: lvRes.data.map(i => i.value) }]
    }
  } catch (e) {}
})
</script>

<style scoped>
.stat-card { text-align: center; padding: 10px; }
.stat-icon { font-size: 28px; margin-bottom: 8px; }
.stat-value { font-size: 32px; font-weight: 700; color: #409EFF; margin-bottom: 4px; }
.stat-label { font-size: 13px; color: #909399; }
</style>
