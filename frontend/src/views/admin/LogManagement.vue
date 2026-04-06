<script setup>
import { computed, ref } from 'vue'

// 日志管理 mock 数据（后续接入真实接口时，可替换为 src/api/admin/log.js）
const queryForm = ref({
  keyword: '',
  module: '全部模块',
  level: '全部等级',
  dateRange: ['2026-04-01', '2026-04-10']
})

const moduleOptions = ['全部模块', '用户中心', '课程管理', '教学资源', '考勤管理', '财务统计', '机构设置']
const levelOptions = ['全部等级', 'INFO', 'WARN', 'ERROR']

const logRows = ref([
  {
    id: 'LOG-20260410-0001',
    operator: 'admin',
    module: '机构设置',
    action: '更新站点标题与 Logo',
    ip: '10.10.3.15',
    level: 'INFO',
    created_at: '2026-04-10 09:18:24',
    updated_at: '2026-04-10 09:18:24'
  },
  {
    id: 'LOG-20260410-0002',
    operator: 'finance_admin',
    module: '财务统计',
    action: '导出财单明细（4月）',
    ip: '10.10.8.21',
    level: 'WARN',
    created_at: '2026-04-10 10:06:41',
    updated_at: '2026-04-10 10:06:41'
  },
  {
    id: 'LOG-20260410-0003',
    operator: 'teacher_zhang',
    module: '教学资源',
    action: '批量上传作业附件',
    ip: '10.10.2.71',
    level: 'ERROR',
    created_at: '2026-04-10 10:42:13',
    updated_at: '2026-04-10 10:42:13'
  },
  {
    id: 'LOG-20260410-0004',
    operator: 'ops_admin',
    module: '机构设置',
    action: '切换首页展示模板',
    ip: '10.10.1.9',
    level: 'INFO',
    created_at: '2026-04-10 11:17:59',
    updated_at: '2026-04-10 11:17:59'
  }
])

const statsCards = computed(() => {
  const total = logRows.value.length
  const err = logRows.value.filter((row) => row.level === 'ERROR').length
  const warn = logRows.value.filter((row) => row.level === 'WARN').length
  return [
    { label: '日志总数', value: `${total}`, tip: '最近 10 天' },
    { label: '告警日志', value: `${warn}`, tip: 'WARN' },
    { label: '异常日志', value: `${err}`, tip: 'ERROR' },
    { label: '模块覆盖', value: '7', tip: '核心模块' }
  ]
})

const filteredRows = computed(() =>
  logRows.value.filter((row) => {
    const keyword = queryForm.value.keyword.trim()
    const hitKeyword =
      !keyword || row.id.includes(keyword) || row.operator.includes(keyword) || row.action.includes(keyword) || row.ip.includes(keyword)
    const hitModule = queryForm.value.module === '全部模块' || row.module === queryForm.value.module
    const hitLevel = queryForm.value.level === '全部等级' || row.level === queryForm.value.level
    return hitKeyword && hitModule && hitLevel
  })
)

function resetQuery() {
  queryForm.value = {
    keyword: '',
    module: '全部模块',
    level: '全部等级',
    dateRange: ['2026-04-01', '2026-04-10']
  }
}

function tagType(level) {
  if (level === 'ERROR') return 'danger'
  if (level === 'WARN') return 'warning'
  return 'success'
}
</script>

<template>
  <div class="log-page">
    <section class="page-head">
      <div>
        <h1 class="title">日志管理</h1>
        <p class="subtitle">机构设置 / 日志管理</p>
      </div>
      <div class="actions">
        <el-button>导出日志</el-button>
        <el-button type="primary">归档策略</el-button>
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
      <el-button type="primary">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </section>

    <section class="panel">
      <el-table :data="filteredRows" stripe>
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
        <el-table-column prop="created_at" label="created_at" min-width="170" />
        <el-table-column prop="updated_at" label="updated_at" min-width="170" />
      </el-table>
    </section>
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
@media (max-width: 1200px) { .stats-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } .filter-panel { grid-template-columns: 1fr 1fr; } }
@media (max-width: 640px) { .stats-grid { grid-template-columns: 1fr; } .actions { width: 100%; } .actions :deep(.el-button) { flex: 1; } .filter-panel { grid-template-columns: 1fr; } }
:global(html.dark) .title { color: #e5eaf3; }
:global(html.dark) .subtitle, :global(html.dark) .stat-label, :global(html.dark) .stat-tip { color: #a9b4c5; }
:global(html.dark) .panel, :global(html.dark) .stat-card { background: #1a2028; border-color: #2b3442; }
</style>

