<template>
  <div>
    <el-card shadow="hover">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>综合评分排名</span>
          <div>
            <el-button type="primary" size="small" :loading="calcLoading" @click="handleCalculate">重新计算</el-button>
            <el-button size="small" @click="handleExport">导出Excel</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="query" size="small" style="margin-bottom:15px">
        <el-form-item label="学号"><el-input v-model="query.studentId" clearable /></el-form-item>
        <el-form-item label="班级"><el-input v-model="query.className" clearable /></el-form-item>
        <el-form-item label="最低分"><el-input-number v-model="query.scoreRangeMin" :min="0" :max="100" controls-position="right" /></el-form-item>
        <el-form-item label="最高分"><el-input-number v-model="query.scoreRangeMax" :min="0" :max="100" controls-position="right" /></el-form-item>
        <el-form-item><el-button type="primary" @click="loadData">查询</el-button><el-button @click="resetQuery">重置</el-button></el-form-item>
      </el-form>

      <el-table :data="list" border stripe v-loading="loading" size="small">
        <el-table-column label="排名" width="65">
          <template #default="{row}">
            <el-tag :type="row.ranking <= 3 ? 'danger' : ''" size="small">{{ row.ranking }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="studentId" label="学号" width="110" />
        <el-table-column prop="studentName" label="姓名" width="90" />
        <el-table-column label="考勤(20%)" width="150">
          <template #default="{row}">{{ row.attendanceRawScore?.toFixed(1) }} → {{ row.attendanceWeightedScore?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="周报(30%)" width="150">
          <template #default="{row}">{{ row.weeklyReportRawScore?.toFixed(1) }} → {{ row.weeklyReportWeightedScore?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="作业(50%)" width="150">
          <template #default="{row}">{{ row.homeworkRawScore?.toFixed(1) }} → {{ row.homeworkWeightedScore?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="综合总分" width="120">
          <template #default="{row}">
            <span style="font-weight:bold;color:#409eff;font-size:16px">{{ row.totalWeightedScore?.toFixed(2) }}</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" layout="total,sizes,prev,pager,next" style="margin-top:15px;justify-content:center" @change="loadData" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { comprehensiveApi } from '../api/index.js'
import { ElMessage } from 'element-plus'

const list = ref([]); const total = ref(0); const loading = ref(false); const calcLoading = ref(false)

const query = reactive({ page:1, size:10, studentId:'', className:'', scoreRangeMin:null, scoreRangeMax:null })

const loadData = async () => {
  loading.value = true
  try {
    const params = { page:query.page, size:query.size, studentId:query.studentId, className:query.className }
    if (query.scoreRangeMin !== null && query.scoreRangeMin !== '') params.scoreRangeMin = query.scoreRangeMin
    if (query.scoreRangeMax !== null && query.scoreRangeMax !== '') params.scoreRangeMax = query.scoreRangeMax
    const res = await comprehensiveApi.list(params)
    if (res.code === 200) { list.value = res.data.records; total.value = res.data.total }
  } finally { loading.value = false }
}

const resetQuery = () => { Object.assign(query, { page:1, studentId:'', className:'', scoreRangeMin:null, scoreRangeMax:null }) }

const handleCalculate = async () => {
  calcLoading.value = true
  try {
    const res = await comprehensiveApi.calculate()
    ElMessage.success(`计算完成，共 ${res.data.calculatedCount} 名学生`)
    loadData()
  } catch(e) { console.error(e) }
  finally { calcLoading.value = false }
}

const handleExport = async () => {
  try {
    const res = await comprehensiveApi.exportExcel({ className:query.className })
    const url = URL.createObjectURL(new Blob([res]))
    const a = document.createElement('a'); a.href = url; a.download = '综合评分排名.xlsx'; a.click()
    URL.revokeObjectURL(url)
  } catch(e) { console.error(e) }
}

onMounted(loadData)
</script>
