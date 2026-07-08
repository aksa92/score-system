<template>
  <div>
    <el-card shadow="hover">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>周报管理</span>
          <el-button type="primary" size="small" @click="showDialog(null)">新增周报</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query" size="small" style="margin-bottom:15px">
        <el-form-item label="学号"><el-input v-model="query.studentId" clearable /></el-form-item>
        <el-form-item label="年份"><el-input-number v-model="query.year" :min="2020" :max="2030" controls-position="right" /></el-form-item>
        <el-form-item label="周数"><el-input-number v-model="query.weekNumber" :min="1" :max="53" controls-position="right" /></el-form-item>
        <el-form-item><el-button type="primary" @click="loadData">查询</el-button><el-button @click="resetQuery">重置</el-button></el-form-item>
      </el-form>

      <el-table :data="list" border stripe v-loading="loading" size="small">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="studentId" label="学号" width="110" />
        <el-table-column prop="studentName" label="姓名" width="90" />
        <el-table-column prop="year" label="年份" width="65" />
        <el-table-column prop="weekNumber" label="周数" width="65" />
        <el-table-column label="文件" width="120">
          <template #default="{row}">
            <el-button v-if="row.wordFilePath" type="primary" link size="small" @click="handleDownload(row)">下载</el-button>
            <span v-else style="color:#999">无文件</span>
          </template>
        </el-table-column>
        <el-table-column prop="rawScore" label="原始分" width="80" />
        <el-table-column prop="weightedScore" label="权重分(30%)" width="120">
          <template #default="{row}">{{ row.weightedScore?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="evaluator" label="评分人" width="90" />
        <el-table-column prop="evaluationTime" label="评分时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{row}">
            <el-button type="primary" link size="small" @click="showEvaluate(row)">评分</el-button>
            <el-button type="primary" link size="small" @click="showDialog(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" layout="total,sizes,prev,pager,next" style="margin-top:15px;justify-content:center" @change="loadData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑周报':'新增周报'" width="550px">
      <el-form :model="form" label-width="100px" size="small">
        <el-form-item label="学号"><el-input v-model="form.studentId" :disabled="isEdit" /></el-form-item>
        <el-form-item label="年份"><el-input-number v-model="form.year" :min="2020" :max="2030" /></el-form-item>
        <el-form-item label="周数"><el-input-number v-model="form.weekNumber" :min="1" :max="53" /></el-form-item>
        <el-form-item v-if="!isEdit" label="上传文件">
          <el-upload :auto-upload="false" :on-change="handleFileChange" :limit="1" accept=".doc,.docx,.pdf">
            <el-button size="small">选择文件</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remarks" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="evaluateVisible" title="周报评分" width="400px">
      <el-form :model="evaluateForm" label-width="100px" size="small">
        <el-form-item label="原始分数"><el-input-number v-model="evaluateForm.rawScore" :min="0" :max="100" :precision="2" /></el-form-item>
        <el-form-item label="评分人"><el-input v-model="evaluateForm.evaluator" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="evaluateVisible=false">取消</el-button>
        <el-button type="primary" @click="handleEvaluate">提交评分</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { weeklyReportApi } from '../api/index.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([]); const total = ref(0); const loading = ref(false)
const dialogVisible = ref(false); const evaluateVisible = ref(false); const isEdit = ref(false)
let editId = null; let evaluateId = null; let selectedFile = null

const query = reactive({ page:1, size:10, studentId:'', year:2026, weekNumber:null })
const form = reactive({ studentId:'', year:2026, weekNumber:1, remarks:'' })
const evaluateForm = reactive({ rawScore:80, evaluator:'王老师' })

const loadData = async () => {
  loading.value = true
  try {
    const params = { ...query }; if (!params.weekNumber) delete params.weekNumber
    const res = await weeklyReportApi.list(params)
    if (res.code === 200) { list.value = res.data.records; total.value = res.data.total }
  } finally { loading.value = false }
}
const resetQuery = () => { Object.assign(query, { page:1, studentId:'', year:2026, weekNumber:null }) }

const showDialog = (row) => {
  isEdit.value = !!row; editId = row?.id || null; selectedFile = null
  if (row) Object.assign(form, row)
  else Object.assign(form, { studentId:'', year:2026, weekNumber:1, remarks:'' })
  dialogVisible.value = true
}

const handleFileChange = (uploadFile) => { selectedFile = uploadFile.raw }

const handleSave = async () => {
  try {
    if (isEdit.value) {
      await weeklyReportApi.update(editId, form)
    } else {
      await weeklyReportApi.create(form, selectedFile)
    }
    ElMessage.success('保存成功'); dialogVisible.value = false; loadData()
  } catch(e) { console.error(e) }
}

const handleDelete = async (row) => {
  try { await ElMessageBox.confirm('确定删除？'); await weeklyReportApi.delete(row.id); ElMessage.success('已删除'); loadData() } catch(e) {}
}

const showEvaluate = (row) => {
  evaluateId = row.id; evaluateForm.rawScore = row.rawScore || 80; evaluateForm.evaluator = row.evaluator || '王老师'
  evaluateVisible.value = true
}

const handleEvaluate = async () => {
  try { await weeklyReportApi.evaluate(evaluateId, evaluateForm.rawScore, evaluateForm.evaluator); ElMessage.success('评分成功'); evaluateVisible.value = false; loadData() } catch(e) {}
}

const handleDownload = (row) => { window.open(weeklyReportApi.getDownloadUrl(row.id)) }

onMounted(loadData)
</script>
