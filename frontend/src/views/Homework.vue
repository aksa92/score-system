<template>
  <div>
    <el-card shadow="hover">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>作业管理</span>
          <el-button type="primary" size="small" @click="showDialog(null)">新增作业</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query" size="small" style="margin-bottom:15px">
        <el-form-item label="学号"><el-input v-model="query.studentId" clearable /></el-form-item>
        <el-form-item label="作业编号"><el-input v-model="query.homeworkId" clearable /></el-form-item>
        <el-form-item label="作业名称"><el-input v-model="query.homeworkName" clearable /></el-form-item>
        <el-form-item><el-button type="primary" @click="loadData">查询</el-button><el-button @click="resetQuery">重置</el-button></el-form-item>
      </el-form>

      <el-table :data="list" border stripe v-loading="loading" size="small">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="homeworkId" label="作业编号" width="130" />
        <el-table-column prop="homeworkName" label="作业名称" min-width="150" />
        <el-table-column prop="studentId" label="学号" width="110" />
        <el-table-column prop="studentName" label="姓名" width="80" />
        <el-table-column prop="rawScore" label="原始分" width="80" />
        <el-table-column prop="weightedScore" label="权重分(50%)" width="120">
          <template #default="{row}">{{ row.weightedScore?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="evaluator" label="评分人" width="90" />
        <el-table-column prop="evaluationTime" label="评分时间" width="160" />
        <el-table-column label="文件" width="80">
          <template #default="{row}">
            <el-button v-if="row.filePath" type="primary" link size="small" @click="handleDownload(row)">下载</el-button>
            <span v-else style="color:#999">无</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{row}">
            <el-button type="primary" link size="small" @click="showDialog(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" layout="total,sizes,prev,pager,next" style="margin-top:15px;justify-content:center" @change="loadData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑作业':'新增作业'" width="550px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" size="small">
        <el-form-item label="作业编号" prop="homeworkId"><el-input v-model="form.homeworkId" :disabled="isEdit" @blur="checkHomeworkId"><el-button v-if="!isEdit" slot="suffix" @click="checkHomeworkId">校验</el-button></el-input></el-form-item>
        <el-alert v-if="idCheckMsg" :title="idCheckMsg" :type="idCheckMsg.includes('可用')?'success':'warning'" show-icon style="margin-bottom:10px" />
        <el-form-item label="作业名称" prop="homeworkName"><el-input v-model="form.homeworkName" /></el-form-item>
        <el-form-item label="学号" prop="studentId"><el-input v-model="form.studentId" :disabled="isEdit" /></el-form-item>
        <el-form-item label="原始分数"><el-input-number v-model="form.rawScore" :min="0" :max="100" :precision="2" /></el-form-item>
        <el-form-item label="评分人"><el-input v-model="form.evaluator" /></el-form-item>
        <el-form-item label="提交内容"><el-input v-model="form.submissionContent" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { homeworkApi } from '../api/index.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([]); const total = ref(0); const loading = ref(false)
const dialogVisible = ref(false); const isEdit = ref(false); const idCheckMsg = ref('')
let editHomeworkId = null

const query = reactive({ page:1, size:10, studentId:'', homeworkId:'', homeworkName:'' })
const form = reactive({ homeworkId:'', homeworkName:'', studentId:'', rawScore:80, evaluator:'李老师', submissionContent:'' })
const rules = { homeworkId:[{required:true,message:'请输入作业编号'}], homeworkName:[{required:true,message:'请输入作业名称'}], studentId:[{required:true,message:'请输入学号'}] }

const loadData = async () => {
  loading.value = true
  try {
    const res = await homeworkApi.list(query)
    if (res.code === 200) { list.value = res.data.records; total.value = res.data.total }
  } finally { loading.value = false }
}
const resetQuery = () => { Object.assign(query, { page:1, studentId:'', homeworkId:'', homeworkName:'' }) }

const showDialog = (row) => {
  isEdit.value = !!row; editHomeworkId = row?.homeworkId || null; idCheckMsg.value = ''
  if (row) Object.assign(form, row)
  else Object.assign(form, { homeworkId:'', homeworkName:'', studentId:'', rawScore:80, evaluator:'李老师', submissionContent:'' })
  dialogVisible.value = true
}

const checkHomeworkId = async () => {
  if (!form.homeworkId) return
  try {
    const res = await homeworkApi.checkId(form.homeworkId)
    idCheckMsg.value = res.data.available ? '编号可用' : '编号已存在，请更换'
  } catch(e) { idCheckMsg.value = '校验失败' }
}

const handleSave = async () => {
  try {
    if (isEdit.value) await homeworkApi.update(editHomeworkId, form)
    else await homeworkApi.create(form)
    ElMessage.success('保存成功'); dialogVisible.value = false; loadData()
  } catch(e) { console.error(e) }
}

const handleDelete = async (row) => {
  try { await ElMessageBox.confirm('确定删除？'); await homeworkApi.delete(row.homeworkId); ElMessage.success('已删除'); loadData() } catch(e) {}
}

const handleDownload = (row) => { window.open(homeworkApi.getDownloadUrl(row.homeworkId)) }

onMounted(loadData)
</script>
