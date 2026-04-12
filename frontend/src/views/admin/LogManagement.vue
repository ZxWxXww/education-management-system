<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  exportAdminLogs,
  fetchAdminLogArchiveStrategy,
  fetchAdminLogPage,
  updateAdminLogArchiveStrategy
} from '../../api/admin/log'

const loading = ref(false)
const exportLoading = ref(false)
const archiveDialogVisible = ref(false)
const archiveLoading = ref(false)
const archiveSaving = ref(false)
const queryForm = ref({
  keyword: '',
  module: '全部模块',
  level: '全部等级',
  dateRange: []
})
const logRows = ref([])
const archiveForm = ref({
  autoArchiveEnabled: true,
  retentionDays: 180,
  updatedAt: '-'
})

const moduleOptions = computed(() => [
  '全部模块',
  ...new Set(logRows.value.map((row) => row.module).filter(Boolean))
])
const levelOptions = ['全部等级', 'INFO', 'ERROR']

const statsCards = computed(() => {
  const total = logRows.value.length
  const infoCount = logRows.value.filter((row) => row.level === 'INFO').length
  const errorCount = logRows.value.filter((row) => row.level === 'ERROR').length
  const moduleCount = new Set(logRows.value.map((row) => row.module).filter(Boolean)).size
  return [
    { label: '日志总数', value: `${total}`, tip: '当前结果' },
    { label: '成功日志', value: `${infoCount}`, tip: 'INFO' },
    { label: '异常日志', value: `${errorCount}`, tip: 'ERROR' },
    { label: '模块覆盖', value: `${moduleCount}`, tip: '当前页' }
  ]
})

async function loadLogs() {
  loading.value = true
  try {
    const page = await fetchAdminLogPage({
      pageNum: 1,
      pageSize: 200,
      ...queryForm.value
    })
    logRows.value = page.list
  } catch (error) {
    ElMessage.error(error.message || '日志列表加载失败')
  } finally {
    loading.value = false
  }
}

async function loadArchiveStrategy() {
  archiveLoading.value = true
  try {
    archiveForm.value = await fetchAdminLogArchiveStrategy()
  } catch (error) {
    ElMessage.error(error.message || '归档策略加载失败')
  } finally {
    archiveLoading.value = false
  }
}

function handleQuery() {
  loadLogs()
}

function resetQuery() {
  queryForm.value = {
    keyword: '',
    module: '全部模块',
    level: '全部等级',
    dateRange: []
  }
  loadLogs()
}

async function handleExport() {
  exportLoading.value = true
  try {
    await exportAdminLogs(queryForm.value)
    ElMessage.success('日志导出成功')
  } catch (error) {
    ElMessage.error(error.message || '日志导出失败')
  } finally {
    exportLoading.value = false
  }
}

function handleArchive() {
  archiveDialogVisible.value = true
  if (archiveForm.value.updatedAt === '-') {
    loadArchiveStrategy()
  }
}

async function handleSaveArchive() {
  archiveSaving.value = true
  try {
    await updateAdminLogArchiveStrategy({
      autoArchiveEnabled: archiveForm.value.autoArchiveEnabled,
      retentionDays: archiveForm.value.retentionDays
    })
    ElMessage.success('归档策略已保存')
    await loadArchiveStrategy()
    archiveDialogVisible.value = false
  } catch (error) {
    ElMessage.error(error.message || '归档策略保存失败')
  } finally {
    archiveSaving.value = false
  }
}

function tagType(level) {
  if (level === 'ERROR') return 'danger'
  if (level === 'WARN') return 'warning'
  return 'success'
}

onMounted(() => {
  loadLogs()
  loadArchiveStrategy()
})
</script>

<template>
  <div class="log-page">
    <section class="page-head">
      <div>
        <h1 class="title">日志管理</h1>
        <p class="subtitle">机构设置 / 日志管理</p>
      </div>
      <div class="actions">
        <el-button :loading="exportLoading" @click="handleExport">导出日志</el-button>
        <el-button type="primary" @click="handleArchive">归档策略</el-button>
      </div>
    </section>

    <section class="stats-grid">
      <article v-for="card in statsCards" :key="card.label" class="stat-card">
        <p class="stat-label">{{ card.label }}</p>
        <p class="stat-value">{{ card.value }}</p>
        <p class="stat-tip">{{ card.tip }}</p>
      </article>
    </section>

    <section class="panel filter-panel">
      <el-input v-model="queryForm.keyword" placeholder="日志ID / 操作人 / 行为 / IP" clearable />
      <el-select v-model="queryForm.module">
        <el-option v-for="option in moduleOptions" :key="option" :label="option" :value="option" />
      </el-select>
      <el-select v-model="queryForm.level">
        <el-option v-for="option in levelOptions" :key="option" :label="option" :value="option" />
      </el-select>
      <el-date-picker v-model="queryForm.dateRange" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始日期" end-placeholder="结束日期" />
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </section>

    <section class="panel">
      <el-table v-loading="loading" :data="logRows" stripe>
        <el-table-column prop="id" label="日志ID" min-width="180" />
        <el-table-column prop="operator" label="操作人" width="130" />
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="action" label="操作行为" min-width="240" show-overflow-tooltip />
        <el-table-column prop="ip" label="来源IP" width="130" />
        <el-table-column label="等级" width="90">
          <template #default="{ row }">
            <el-tag :type="tagType(row.level)" effect="light">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="170" />
        <el-table-column prop="updatedAt" label="更新时间" min-width="170" />
      </el-table>
    </section>

    <el-dialog v-model="archiveDialogVisible" title="归档策略" width="420px">
      <div v-loading="archiveLoading" class="archive-body">
        <el-form :model="archiveForm" label-width="110px">
          <el-form-item label="自动归档">
            <el-switch v-model="archiveForm.autoArchiveEnabled" active-text="开启" inactive-text="关闭" />
          </el-form-item>
          <el-form-item label="保留天数">
            <el-input-number v-model="archiveForm.retentionDays" :min="1" :max="3650" controls-position="right" />
          </el-form-item>
        </el-form>
        <p class="archive-tip">最近更新时间：{{ archiveForm.updatedAt || '-' }}</p>
      </div>
      <template #footer>
        <el-button @click="archiveDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="archiveSaving" @click="handleSaveArchive">保存策略</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.log-page { display: flex; flex-direction: column; gap: 16px; }
.page-head { display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; }
.title { margin: 0; font-size: 28px; font-weight: 700; }
.subtitle { margin: 6px 0 0; color: #667085; }
.actions { display: flex; gap: 10px; }
.stats-grid { display: grid; gap: 12px; grid-template-columns: repeat(4, minmax(0, 1fr)); }
.stat-card { background: #fff; border: 1px solid #e6ebf2; border-radius: 12px; padding: 14px; }
.stat-label { margin: 0; color: #667085; font-size: 13px; }
.stat-value { margin: 6px 0 2px; font-size: 28px; font-weight: 700; }
.stat-tip { margin: 0; color: #98a2b3; font-size: 12px; }
.panel { background: #fff; border: 1px solid #e6ebf2; border-radius: 12px; padding: 14px; }
.filter-panel { display: grid; gap: 10px; grid-template-columns: 2fr 1fr 1fr 2fr auto auto; }
.archive-body { min-height: 120px; }
.archive-tip { margin: 4px 0 0; color: #98a2b3; font-size: 12px; }
@media (max-width: 1200px) { .stats-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } .filter-panel { grid-template-columns: 1fr 1fr; } }
@media (max-width: 640px) { .stats-grid { grid-template-columns: 1fr; } .actions { width: 100%; } .actions :deep(.el-button) { flex: 1; } .filter-panel { grid-template-columns: 1fr; } }
:global(html.dark) .title { color: #e5eaf3; }
:global(html.dark) .subtitle, :global(html.dark) .stat-label, :global(html.dark) .stat-tip, :global(html.dark) .archive-tip { color: #a9b4c5; }
:global(html.dark) .panel, :global(html.dark) .stat-card { background: #1a2028; border-color: #2b3442; }
</style>

