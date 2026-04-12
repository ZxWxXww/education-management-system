<script setup>
import { computed, onMounted, ref } from 'vue'
import { Document, EditPen, Reading } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchAdminTeachingResourceOverview } from '../../api/admin/resource'

const router = useRouter()
const loading = ref(false)
const resourceOverview = ref({
  totalAssignments: 0,
  publishedThisWeek: 0,
  gradeRecords: 0,
  avgScore: 0
})
const recentActivities = ref([])

const cards = computed(() => [
  { label: '作业总数', value: resourceOverview.value.totalAssignments, tip: '累计发布' },
  { label: '本周新发布', value: resourceOverview.value.publishedThisWeek, tip: '最近7天' },
  { label: '成绩记录', value: resourceOverview.value.gradeRecords, tip: '已入库' },
  { label: '平均分', value: `${resourceOverview.value.avgScore}`, tip: '当前学期' }
])

function goAssignment() {
  router.push('/admin/resources/assignments')
}

function goGrade() {
  router.push('/admin/resources/grades')
}

async function loadOverview() {
  loading.value = true
  try {
    const data = await fetchAdminTeachingResourceOverview()
    resourceOverview.value = data
    recentActivities.value = data.recentActivities
  } catch (error) {
    ElMessage.error(error.message || '教学资源总览加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadOverview()
})
</script>

<template>
  <div v-loading="loading" class="teaching-resources">
    <section class="head">
      <h1 class="title">教学资源</h1>
      <p class="subtitle">统一管理作业与成绩数据，支持快速发布与追踪</p>
    </section>

    <section class="stats">
      <article v-for="item in cards" :key="item.label" class="stat-card">
        <p class="stat-label">{{ item.label }}</p>
        <p class="stat-value">{{ item.value }}</p>
        <p class="stat-tip">{{ item.tip }}</p>
      </article>
    </section>

    <section class="main-grid">
      <article class="panel nav-panel">
        <header class="panel-head">
          <h2>资源模块导航</h2>
        </header>
        <div class="nav-actions">
          <button type="button" class="nav-card" @click="goAssignment">
            <el-icon :size="20"><EditPen /></el-icon>
            <div>
              <p class="nav-card__title">作业管理</p>
              <p class="nav-card__desc">发布、筛选、追踪提交进度</p>
            </div>
          </button>
          <button type="button" class="nav-card" @click="goGrade">
            <el-icon :size="20"><Reading /></el-icon>
            <div>
              <p class="nav-card__title">成绩管理</p>
              <p class="nav-card__desc">录入成绩、分析分布、导出报告</p>
            </div>
          </button>
        </div>
      </article>

      <article class="panel">
        <header class="panel-head">
          <h2>最近动态</h2>
        </header>
        <div class="activity-list">
          <div v-for="row in recentActivities" :key="row.title" class="activity-item">
            <el-icon><Document /></el-icon>
            <div>
              <p class="activity-title">{{ row.title }}</p>
              <p class="activity-meta">{{ row.type }} · {{ row.time }}</p>
            </div>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<style scoped>
.teaching-resources {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
}
.subtitle {
  margin: 8px 0 0;
  color: #667085;
}
.stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}
.stat-card {
  background: #fff;
  border: 1px solid #eef2f7;
  border-radius: 12px;
  padding: 16px;
}
.stat-label {
  margin: 0;
  color: #667085;
  font-size: 13px;
}
.stat-value {
  margin: 6px 0 2px;
  font-size: 28px;
  font-weight: 700;
}
.stat-tip {
  margin: 0;
  color: #98a2b3;
  font-size: 12px;
}
.main-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 12px;
}
.panel {
  background: #fff;
  border: 1px solid #eef2f7;
  border-radius: 12px;
  padding: 16px;
}
.panel-head h2 {
  margin: 0 0 12px;
  font-size: 16px;
}
.nav-actions {
  display: grid;
  gap: 10px;
}
.nav-card {
  border: 1px solid #e4e7ec;
  border-radius: 10px;
  background: #f9fafb;
  padding: 14px;
  display: grid;
  grid-template-columns: 20px 1fr;
  gap: 10px;
  text-align: left;
  cursor: pointer;
}
.nav-card__title {
  margin: 0;
  font-weight: 600;
}
.nav-card__desc {
  margin: 4px 0 0;
  font-size: 12px;
  color: #667085;
}
.activity-list {
  display: grid;
  gap: 12px;
}
.activity-item {
  display: grid;
  grid-template-columns: 16px 1fr;
  gap: 10px;
  align-items: start;
}
.activity-title {
  margin: 0;
  font-size: 14px;
}
.activity-meta {
  margin: 4px 0 0;
  color: #98a2b3;
  font-size: 12px;
}
@media (max-width: 900px) {
  .stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .main-grid {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 600px) {
  .stats {
    grid-template-columns: 1fr;
  }
}
:global(html.dark) .stat-card,
:global(html.dark) .panel {
  background: #1a2028;
  border-color: #2b3442;
}
:global(html.dark) .title {
  color: #e5eaf3;
}
:global(html.dark) .subtitle,
:global(html.dark) .stat-label,
:global(html.dark) .stat-tip,
:global(html.dark) .activity-meta,
:global(html.dark) .nav-card__desc {
  color: #a9b4c5;
}
:global(html.dark) .nav-card {
  background: #111827;
  border-color: #263040;
  color: #e5eaf3;
}
</style>
