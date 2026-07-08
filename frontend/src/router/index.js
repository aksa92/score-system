import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },
  { path: '/student', name: 'Student', component: () => import('../views/Student.vue') },
  { path: '/attendance', name: 'Attendance', component: () => import('../views/Attendance.vue') },
  { path: '/weekly-report', name: 'WeeklyReport', component: () => import('../views/WeeklyReport.vue') },
  { path: '/homework', name: 'Homework', component: () => import('../views/Homework.vue') },
  { path: '/comprehensive', name: 'Comprehensive', component: () => import('../views/Comprehensive.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
