﻿<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { FolderOpened } from '@element-plus/icons-vue'
import { fetchTeacherResourcePage } from '../../api/teacher/resource'

const loading = ref(false)
const resourceList = ref([])

function isWithinRecentSevenDays(value) {
  if (!value || value === '-') return false
  const timestamp = new Date(String(value).replace(' ', 'T')).getTime()
  if (Number.isNaN(timestamp)) return false
  return Date.now() - timestamp <= 7 * 24 * 60 * 60 * 1000
}

const resourceStats = computed(() => {
  const total = resourceList.value.length
  const recent = resourceList.value.filter((item) => isWithinRecentSevenDays(item.createdAt)).length
  const downloads = resourceList.value.reduce((sum, item) => sum + Number(item.downloadCount || 0), 0)
  const classes = new Set(resourceList.value.map((item) => item.className).filter(Boolean)).size
  return [
    { label: '资料总数', value: String(total) },
    { label: '最近7天新增', value: String(recent) },
    { label: '下载次数', value: String(downloads) },
    { label: '共享班级', value: String(classes) }
  ]
})

async function loadResources() {
  loading.value = true
  try {
    const page = await fetchTeacherResourcePage({ pageNum: 1, pageSize: 100 })
    resourceList.value = page.list
  } catch (error) {
    ElMessage.error('教学资源加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadResources()
})
</script>

<template>
  <div class="resource-library-page">
    <section class="hero">
      <div>
        <p class="path">教学资源 / 教学资料库</p>
        <h1>教学资料库</h1>
        <p class="desc">统一管理教案、讲义与题库资源，支持班级级别共享分发</p>
      </div>
    </section>

    <section class="stats-grid">
      <article v-for="item in resourceStats" :key="item.label" class="stat-card">
        <p>{{ item.label }}</p>
        <strong>{{ item.value }}</strong>
      </article>
    </section>

    <section class="panel">
      <header class="panel-head">
        <h2><el-icon><FolderOpened /></el-icon> 资料列表</h2>
      </header>
      <el-table v-loading="loading" :data="resourceList" stripe>
        <el-table-column prop="resourceId" label="资料编号" min-width="140" />
        <el-table-column prop="title" label="资料名称" min-width="220" />
        <el-table-column prop="type" label="类型" min-width="100" />
        <el-table-column prop="className" label="适用班级" min-width="120" />
        <el-table-column prop="courseName" label="课程" min-width="140" />
        <el-table-column prop="createdAt" label="创建时间" min-width="170" />
        <el-table-column prop="updatedAt" label="更新时间" min-width="170" />
      </el-table>
    </section>
  </div>
</template>

<style scoped>
.resource-library-page { max-width: 1180px; margin: 0 auto; display: flex; flex-direction: column; gap: 16px; }
.hero { border-radius: 16px; padding: 20px; display: flex; justify-content: space-between; align-items: flex-start; gap: 10px; background: linear-gradient(135deg, #1d4ed8 0%, #2563eb 100%); color: #fff; }
.path, .desc { margin: 0; opacity: 0.88; }
h1 { margin: 6px 0 8px; font-size: 30px; }
.stats-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; }
.stat-card { border: 1px solid #e5e7eb; border-radius: 12px; background: #fff; padding: 14px; }
.stat-card p { margin: 0; color: #64748b; font-size: 12px; }
.stat-card strong { display: block; margin-top: 6px; font-size: 28px; color: #0f172a; }
.panel { border: 1px solid #e5e7eb; border-radius: 12px; background: #fff; padding: 16px; }
.panel-head { margin-bottom: 10px; }
.panel-head h2 { margin: 0; display: inline-flex; align-items: center; gap: 8px; }
@media (max-width: 1024px) { .stats-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 700px) { .hero { flex-direction: column; } .stats-grid { grid-template-columns: 1fr; } }
:global(html.dark) .stat-card,:global(html.dark) .panel { background: #1a2028; border-color: #2b3442; }
:global(html.dark) .stat-card p { color: #9ba7ba; }
:global(html.dark) .stat-card strong,:global(html.dark) .panel-head h2 { color: #e5eaf3; }
</style>
