<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchAdminReportOverview } from '../../api/admin/report'

const router = useRouter()
const loading = ref(false)
const reportCards = ref([])
const hotReports = ref([])

function goComparative() {
  router.push('/admin/reports/comparative')
}

async function loadReports() {
  loading.value = true
  try {
    const data = await fetchAdminReportOverview()
    reportCards.value = data.reportCards
    hotReports.value = data.hotReports
  } catch (error) {
    ElMessage.error(error.message || '数据报表加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadReports()
})
</script>

<template>
  <div v-loading="loading" class="reports-page">
    <section class="page-head">
      <div>
        <h1 class="title">数据报表</h1>
        <p class="subtitle">管理员端 / 数据报表</p>
      </div>
      <div class="actions">
        <el-button @click="goComparative">对比分析</el-button>
      </div>
    </section>

    <section class="stats-grid">
      <article v-for="item in reportCards" :key="item.label" class="stat-card">
        <p class="stat-label">{{ item.label }}</p>
        <p class="stat-value">{{ item.value }}</p>
        <p class="stat-tip">{{ item.tip }}</p>
      </article>
    </section>

    <section class="panel">
      <header class="panel-head">
        <h2>真实报表总览</h2>
      </header>
      <el-table :data="hotReports" stripe>
        <el-table-column prop="name" label="报表名称" min-width="220" />
        <el-table-column prop="owner" label="负责部门" min-width="140" />
        <el-table-column prop="createdAt" label="创建时间" min-width="170" />
        <el-table-column prop="updatedAt" label="更新时间" min-width="170" />
      </el-table>
    </section>
  </div>
</template>

<style scoped>
.reports-page { display: flex; flex-direction: column; gap: 16px; }
.page-head { display: flex; align-items: center; justify-content: space-between; gap: 12px; flex-wrap: wrap; }
.title { margin: 0; font-size: 28px; font-weight: 700; }
.subtitle { margin: 6px 0 0; color: #667085; }
.actions { display: flex; gap: 10px; }
.stats-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; }
.stat-card { background: #fff; border: 1px solid #e6ebf2; border-radius: 12px; padding: 14px; }
.stat-label { margin: 0; color: #667085; font-size: 13px; }
.stat-value { margin: 6px 0 2px; font-size: 28px; font-weight: 700; }
.stat-tip { margin: 0; color: #98a2b3; font-size: 12px; }
.panel { background: #fff; border: 1px solid #e6ebf2; border-radius: 12px; padding: 14px; }
.panel-head { margin-bottom: 12px; }
.panel-head h2 { margin: 0; font-size: 18px; }
@media (max-width: 1024px) { .stats-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 640px) { .stats-grid { grid-template-columns: 1fr; } .actions { width: 100%; } .actions :deep(.el-button) { flex: 1; } }
:global(html.dark) .title { color: #e5eaf3; }
:global(html.dark) .subtitle, :global(html.dark) .stat-label, :global(html.dark) .stat-tip { color: #a9b4c5; }
:global(html.dark) .stat-card, :global(html.dark) .panel { background: #1a2028; border-color: #2b3442; }
</style>
