<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="6"><el-card shadow="hover"><div class="stat-card"><div class="stat-num">{{ stats.totalStudents }}</div><div class="stat-label">学生总数</div></div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><div class="stat-card"><div class="stat-num">{{ stats.averageScore }}</div><div class="stat-label">平均综合分</div></div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><div class="stat-card"><div class="stat-num" style="color:#67c23a">{{ stats.maxScore }}</div><div class="stat-label">最高分</div></div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><div class="stat-card"><div class="stat-num" style="color:#f56c6c">{{ stats.minScore }}</div><div class="stat-label">最低分</div></div></el-card></el-col>
    </el-row>

    <el-card shadow="hover" style="margin-top:20px">
      <template #header><span>分数分布</span></template>
      <el-row :gutter="20">
        <el-col :span="4" v-for="item in distribution" :key="item.label">
          <div class="dist-item">
            <div class="dist-bar" :style="{height:item.pct+'%',background:item.color}"></div>
            <div class="dist-label">{{ item.label }}</div>
            <div class="dist-val">{{ item.count }}人</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="hover" style="margin-top:20px">
      <template #header><span>权重说明</span></template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="考勤评分" label-class-name="weight-label">占比 20%</el-descriptions-item>
        <el-descriptions-item label="周报评分" label-class-name="weight-label">占比 30%</el-descriptions-item>
        <el-descriptions-item label="作业评分" label-class-name="weight-label">占比 50%</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { comprehensiveApi } from '../api/index.js'

const stats = ref({ totalStudents: 0, averageScore: 0, maxScore: 0, minScore: 0 })

const distribution = ref([
  { label: '90-100', pct: 0, count: 0, color: '#67c23a' },
  { label: '80-89', pct: 0, count: 0, color: '#409eff' },
  { label: '70-79', pct: 0, count: 0, color: '#e6a23c' },
  { label: '60-69', pct: 0, count: 0, color: '#f56c6c' },
  { label: '<60', pct: 0, count: 0, color: '#909399' }
])

const loadStats = async () => {
  try {
    const res = await comprehensiveApi.statistics()
    if (res.code === 200) {
      stats.value = res.data
      const items = [
        { label: '90-100', count: res.data.score90to100 || 0, color: '#67c23a' },
        { label: '80-89', count: res.data.score80to89 || 0, color: '#409eff' },
        { label: '70-79', count: res.data.score70to79 || 0, color: '#e6a23c' },
        { label: '60-69', count: res.data.score60to69 || 0, color: '#f56c6c' },
        { label: '<60', count: res.data.scoreBelow60 || 0, color: '#909399' }
      ]
      const max = Math.max(...items.map(i => i.count), 1)
      items.forEach(i => { i.pct = (i.count / max) * 100 })
      distribution.value = items
    }
  } catch(e) { console.error(e) }
}

onMounted(loadStats)
</script>

<style scoped>
.stat-card { text-align:center;padding:10px 0 }
.stat-num { font-size:36px;font-weight:bold;color:#303133 }
.stat-label { font-size:14px;color:#909399;margin-top:8px }
.dist-item { text-align:center }
.dist-bar { width:40px;margin:0 auto 8px;border-radius:4px 4px 0 0;transition:height 0.3s }
.dist-label { font-size:13px;color:#909399;margin-bottom:4px }
.dist-val { font-size:14px;color:#303133;font-weight:bold }
.weight-label { font-weight:bold;color:#409eff }
</style>
