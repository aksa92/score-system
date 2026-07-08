<template>
  <el-container style="height:100vh">
    <el-aside width="220px" style="background:#304156">
      <div style="height:60px;display:flex;align-items:center;justify-content:center;color:#fff;font-size:18px;font-weight:bold;border-bottom:1px solid rgba(255,255,255,0.1)">
        学生评分系统
      </div>
      <el-menu
        :default-active="route.path"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        style="border-right:none"
      >
        <el-menu-item index="/dashboard"><el-icon><HomeFilled /></el-icon><span>仪表盘</span></el-menu-item>
        <el-menu-item index="/student"><el-icon><UserFilled /></el-icon><span>学生管理</span></el-menu-item>
        <el-menu-item index="/attendance"><el-icon><Calendar /></el-icon><span>考勤管理</span></el-menu-item>
        <el-menu-item index="/weekly-report"><el-icon><Document /></el-icon><span>周报管理</span></el-menu-item>
        <el-menu-item index="/homework"><el-icon><EditPen /></el-icon><span>作业管理</span></el-menu-item>
        <el-menu-item index="/comprehensive"><el-icon><TrendCharts /></el-icon><span>综合评分</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background:#fff;border-bottom:1px solid #e6e6e6;display:flex;align-items:center;justify-content:space-between;padding:0 20px">
        <span style="font-size:16px;font-weight:500">学生综合评分管理系统</span>
        <span style="font-size:14px;color:#666">管理员</span>
      </el-header>
      <el-main style="background:#f0f2f5;overflow-y:auto">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { HomeFilled, UserFilled, Calendar, Document, EditPen, TrendCharts } from '@element-plus/icons-vue'
const route = useRoute()
</script>

<style>
body { margin: 0; }
#app { font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', sans-serif; }
.el-menu-item { font-size: 14px; }
</style>
