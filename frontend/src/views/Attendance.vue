<template>
  <div>
    <el-card shadow="hover">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>考勤管理</span>
          <div>
            <el-button type="primary" size="small" @click="showDialog(null)">新增考勤</el-button>
            <el-button size="small" @click="importVisible=true">批量导入</el-button>
            <el-button size="small" @click="handleExport">导出Excel</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="query" size="small" style="margin-bottom:15px">
        <el-form-item label="学号"><el-input v-model="query.studentId" clearable /></el-form-item>
        <el-form-item label="年份"><el-input-number v-model="query.year" :min="2020" :max="2030" controls-position="right" /></el-form-item>
        <el-form-item label="月份"><el-input-number v-model="query.month" :min="1" :max="12" controls-position="right" /></el-form-item>
        <el-form-item><el-button type="primary" @click="loadData">查询</el-button><el-button @click="resetQuery">重置</el-button></el-form-item>
      </el-form>

      <el-table :data="list" border stripe v-loading="loading" size="small">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="studentId" label="学号" width="110" />
        <el-table-column prop="studentName" label="姓名" width="100" />
        <el-table-column prop="year" label="年份" width="70" />
        <el-table-column prop="month" label="月份" width="70" />
        <el-table-column prop="attendanceDays" label="出勤" width="70" />
        <el-table-column prop="absenceDays" label="缺勤" width="70" />
        <el-table-column prop="rawScore" label="原始分" width="90" />
        <el-table-column prop="weightedScore" label="权重分(20%)" width="120">
          <template #default="{row}">{{ row.weightedScore?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="remarks" label="备注" min-width="150" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{row}">
            <el-button type="primary" link size="small" @click="showDialog(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" layout="total,sizes,prev,pager,next" style="margin-top:15px;justify-content:center" @change="loadData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑考勤':'新增考勤'" width="500px">
      <el-form :model="form" label-width="110px" size="small">
        <el-form-item label="学号" prop="studentId"><el-input v-model="form.studentId" :disabled="isEdit" /></el-form-item>
        <el-form-item label="年份"><el-input-number v-model="form.year" :min="2020" :max="2030" /></el-form-item>
        <el-form-item label="月份"><el-input-number v-model="form.month" :min="1" :max="12" /></el-form-item>
        <el-form-item label="出勤天数"><el-input-number v-model="form.attendanceDays" :min="0" :max="31" /></el-form-item>
        <el-form-item label="缺勤天数"><el-input-number v-model="form.absenceDays" :min="0" :max="31" /></el-form-item>
        <el-form-item label="原始分数"><el-input-number v-model="form.rawScore" :min="0" :max="100" :precision="2" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remarks" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importVisible" title="批量导入考勤" width="450px">
      <el-upload drag accept=".xlsx,.xls" :auto-upload="false" :on-change="handleImport" :limit="1">
        <el-icon style="font-size:48px;color:#c0c4cc"><UploadFilled /></el-icon>
        <div style="margin-top:8px">点击或拖拽Excel文件上传</div>
      </el-upload>
      <el-alert v-if="importResult" :title="importResult" type="info" show-icon style="margin-top:10px" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { attendanceApi } from '../api/index.js'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const importVisible = ref(false)
const isEdit = ref(false)
const importResult = ref('')

const query = reactive({ page:1, size:10, studentId:'', year:2026, month:null })
const form = reactive({ studentId:'', year:2026, month:1, attendanceDays:22, absenceDays:0, rawScore:100, remarks:'' })
let editId = null

const loadData = async () => {
  loading.value = true
  try {
    const params = { ...query }
    if (!params.month) delete params.month
    const res = await attendanceApi.list(params)
    if (res.code === 200) { list.value = res.data.records; total.value = res.data.total }
  } finally { loading.value = false }
}

const resetQuery = () => { Object.assign(query, { page:1, studentId:'', year:2026, month:null }) }

const showDialog = (row) => {
  isEdit.value = !!row; editId = row?.id || null
  if (row) Object.assign(form, row)
  else Object.assign(form, { studentId:'', year:2026, month:1, attendanceDays:22, absenceDays:0, rawScore:100, remarks:'' })
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    if (isEdit.value) await attendanceApi.update(editId, form)
    else await attendanceApi.create(form)
    ElMessage.success('保存成功'); dialogVisible.value = false; loadData()
  } catch(e) { console.error(e) }
}

const handleDelete = async (row) => {
  try { await ElMessageBox.confirm('确定删除？'); await attendanceApi.delete(row.id); ElMessage.success('已删除'); loadData() } catch(e) {}
}

const handleImport = async (uploadFile) => {
  try {
    const res = await attendanceApi.importFile(uploadFile.raw)
    if (res.code === 200) { importResult.value = `成功 ${res.data.successCount} 条，失败 ${res.data.failCount} 条`; loadData() }
  } catch(e) { importResult.value = '导入失败' }
}

const handleExport = async () => {
  try {
    const res = await attendanceApi.exportExcel({ studentId:query.studentId, year:query.year, month:query.month })
    const url = URL.createObjectURL(new Blob([res]))
    const a = document.createElement('a'); a.href = url; a.download = '考勤数据.xlsx'; a.click()
    URL.revokeObjectURL(url)
  } catch(e) { console.error(e) }
}

onMounted(loadData)
</script>
