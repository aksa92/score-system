<template>
  <div>
    <el-card shadow="hover">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>学生信息管理</span>
          <div>
            <el-button type="primary" size="small" @click="showDialog(null)">新增学生</el-button>
            <el-button size="small" @click="importVisible=true">批量导入</el-button>
          </div>
        </div>
      </template>

      <!-- Search -->
      <el-form :inline="true" :model="query" size="small" style="margin-bottom:15px">
        <el-form-item label="学号"><el-input v-model="query.studentId" placeholder="学号" clearable /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="query.studentName" placeholder="姓名" clearable /></el-form-item>
        <el-form-item label="班级"><el-input v-model="query.className" placeholder="班级" clearable /></el-form-item>
        <el-form-item><el-button type="primary" @click="loadData">查询</el-button><el-button @click="resetQuery">重置</el-button></el-form-item>
      </el-form>

      <el-table :data="list" border stripe v-loading="loading" size="small">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" width="120" />
        <el-table-column prop="className" label="班级" width="160" />
        <el-table-column prop="gender" label="性别" width="70" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="phone" label="手机" width="130" />
        <el-table-column prop="enrolmentYear" label="入学年份" width="100" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{row}">
            <el-button type="primary" link size="small" @click="showDialog(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[5,10,20,50]"
        layout="total,sizes,prev,pager,next"
        style="margin-top:15px;justify-content:center"
        @change="loadData"
      />
    </el-card>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑学生':'新增学生'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" size="small">
        <el-form-item label="学号" prop="studentId"><el-input v-model="form.studentId" :disabled="isEdit" /></el-form-item>
        <el-form-item label="姓名" prop="studentName"><el-input v-model="form.studentName" /></el-form-item>
        <el-form-item label="班级"><el-input v-model="form.className" /></el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select>
        </el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="入学年份"><el-input-number v-model="form.enrolmentYear" :min="2020" :max="2030" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- Import -->
    <el-dialog v-model="importVisible" title="批量导入学生" width="450px">
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
import { studentApi } from '../api/index.js'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const importVisible = ref(false)
const isEdit = ref(false)
const importResult = ref('')

const query = reactive({ page:1, size:10, studentId:'', studentName:'', className:'' })
const form = reactive({ studentId:'', studentName:'', className:'', gender:'', email:'', phone:'', enrolmentYear:2024 })
const rules = { studentId:[{required:true,message:'请输入学号'}], studentName:[{required:true,message:'请输入姓名'}] }

const loadData = async () => {
  loading.value = true
  try {
    const res = await studentApi.list(query)
    if (res.code === 200) { list.value = res.data.records; total.value = res.data.total }
  } finally { loading.value = false }
}

const resetQuery = () => { Object.assign(query, { studentId:'', studentName:'', className:'', page:1 }) }

const showDialog = (row) => {
  isEdit.value = !!row
  if (row) { Object.assign(form, row) }
  else { Object.assign(form, { studentId:'', studentName:'', className:'', gender:'', email:'', phone:'', enrolmentYear:2024 }) }
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    if (isEdit.value) { await studentApi.update(form.studentId, form) }
    else { await studentApi.create(form) }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch(e) { console.error(e) }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该学生？')
    await studentApi.delete(row.studentId)
    ElMessage.success('已删除')
    loadData()
  } catch(e) { /* cancelled */ }
}

const handleImport = async (uploadFile) => {
  try {
    const res = await studentApi.importFile(uploadFile.raw)
    if (res.code === 200) {
      importResult.value = `导入成功 ${res.data.successCount} 条，失败 ${res.data.failCount} 条`
      if (res.data.errors?.length) importResult.value += '\n' + res.data.errors.join('\n')
      loadData()
    }
  } catch(e) { importResult.value = '导入失败' }
}

onMounted(loadData)
</script>
