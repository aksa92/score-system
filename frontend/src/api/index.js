import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) return res
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message))
  },
  error => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request

// ========== Student APIs ==========
export const studentApi = {
  list(params) { return request.get('/student/list', { params }) },
  get(id) { return request.get(`/student/${id}`) },
  create(data) { return request.post('/student', data) },
  update(id, data) { return request.put(`/student/${id}`, data) },
  delete(id) { return request.delete(`/student/${id}`) },
  importFile(file) {
    const form = new FormData(); form.append('file', file)
    return request.post('/student/import', form)
  }
}

// ========== Attendance APIs ==========
export const attendanceApi = {
  list(params) { return request.get('/attendance/list', { params }) },
  get(id) { return request.get(`/attendance/${id}`) },
  create(data) { return request.post('/attendance', data) },
  update(id, data) { return request.put(`/attendance/${id}`, data) },
  delete(id) { return request.delete(`/attendance/${id}`) },
  importFile(file) {
    const form = new FormData(); form.append('file', file)
    return request.post('/attendance/import', form)
  },
  exportExcel(params) {
    return request.get('/attendance/export', { params, responseType: 'blob' })
  }
}

// ========== Weekly Report APIs ==========
export const weeklyReportApi = {
  list(params) { return request.get('/weekly-report/list', { params }) },
  get(id) { return request.get(`/weekly-report/${id}`) },
  create(data, file) {
    const form = new FormData()
    form.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }))
    if (file) form.append('file', file)
    return request.post('/weekly-report', form)
  },
  delete(id) { return request.delete(`/weekly-report/${id}`) },
  evaluate(id, rawScore, evaluator) {
    return request.put(`/weekly-report/${id}/score`, { rawScore, evaluator })
  },
  uploadFile(id, file) {
    const form = new FormData(); form.append('file', file)
    return request.post(`/weekly-report/${id}/upload`, form)
  },
  getDownloadUrl(id) { return `/api/weekly-report/${id}/download` }
}

// ========== Homework APIs ==========
export const homeworkApi = {
  list(params) { return request.get('/homework/list', { params }) },
  get(id) { return request.get(`/homework/${id}`) },
  create(data) { return request.post('/homework', data) },
  update(id, data) { return request.put(`/homework/${id}`, data) },
  delete(id) { return request.delete(`/homework/${id}`) },
  checkId(id) { return request.get('/homework/check-id', { params: { homeworkId: id } }) },
  getDownloadUrl(id) { return `/api/homework/${id}/download` }
}

// ========== Comprehensive Score APIs ==========
export const comprehensiveApi = {
  list(params) { return request.get('/comprehensive/list', { params }) },
  get(studentId) { return request.get(`/comprehensive/${studentId}`) },
  calculate() { return request.post('/comprehensive/calculate') },
  calculateStudent(studentId) { return request.post(`/comprehensive/calculate/${studentId}`) },
  statistics() { return request.get('/comprehensive/statistics') },
  exportExcel(params) {
    return request.get('/comprehensive/export', { params, responseType: 'blob' })
  }
}

// ========== System APIs ==========
export const systemApi = {
  login(data) { return request.post('/system/login', data) },
  logout() { return request.post('/system/logout') }
}
