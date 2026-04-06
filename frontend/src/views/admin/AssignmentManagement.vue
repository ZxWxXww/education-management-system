<script setup>
import { computed, ref } from 'vue'
import { Calendar, Plus, Refresh, Search, UploadFilled } from '@element-plus/icons-vue'

// 作业管理 mock 数据（后续接入真实接口时，可在 src/api/admin/assignment.js 中替换）
const searchKeyword = ref('')
const statusFilter = ref('全部状态')
const courseFilter = ref('全部课程')

const statusOptions = ['全部状态', '未开始', '进行中', '已截止']
const courseOptions = ['全部课程', '高一数学冲刺班', '高二英语提升班', 'Python 入门班', '前端工程化班']

const assignmentList = ref([
  {
    id: 'A-20260401',
    title: '函数综合训练（第一章）',
    course: '高一数学冲刺班',
    className: '高一(1)班',
    teacher: '王雪',
    deadline: '2026-04-12 23:59',
    status: '进行中',
    submitted: 42,
    total: 48,
    created_at: '2026-04-01 09:20:10',
    updated_at: '2026-04-06 10:36:25'
  },
  {
    id: 'A-20260328',
    title: '阅读理解专项（Unit 5）',
    course: '高二英语提升班',
    className: '高二(3)班',
    teacher: '刘敏',
    deadline: '2026-04-08 20:00',
    status: '进行中',
    submitted: 36,
    total: 40,
    created_at: '2026-03-28 14:02:31',
    updated_at: '2026-04-05 16:12:03'
  },
  {
    id: 'A-20260321',
    title: '循环与列表综合作业',
    course: 'Python 入门班',
    className: '初级(2)班',
    teacher: '周凯',
    deadline: '2026-04-06 18:00',
    status: '已截止',
    submitted: 27,
    total: 32,
    created_at: '2026-03-21 11:33:07',
    updated_at: '2026-04-06 18:15:19'
  },
  {
    id: 'A-20260409',
    title: 'Vue 组件通信练习',
    course: '前端工程化班',
    className: '前端(1)班',
    teacher: '陈然',
    deadline: '2026-04-18 22:00',
    status: '未开始',
    submitted: 0,
    total: 35,
    created_at: '2026-04-09 08:46:55',
    updated_at: '2026-04-09 08:46:55'
  }
])

const filteredAssignments = computed(() =>
  assignmentList.value.filter((item) => {
    const matchesKeyword =
      !searchKeyword.value ||
      item.title.includes(searchKeyword.value) ||
      item.id.includes(searchKeyword.value) ||
      item.teacher.includes(searchKeyword.value)
    const matchesStatus = statusFilter.value === '全部状态' || item.status === statusFilter.value
    const matchesCourse = courseFilter.value === '全部课程' || item.course === courseFilter.value
    return matchesKeyword && matchesStatus && matchesCourse
  })
)

const stats = computed(() => {
  const total = assignmentList.value.length
  const running = assignmentList.value.filter((item) => item.status === '进行中').length
  const closed = assignmentList.value.filter((item) => item.status === '已截止').length
  const avgRate =
    assignmentList.value.reduce((acc, item) => acc + Math.round((item.submitted / item.total) * 100), 0) / (total || 1)

  return [
    { label: '作业总数', value: `${total}`, trend: '+2' },
    { label: '进行中', value: `${running}`, trend: '实时' },
    { label: '已截止', value: `${closed}`, trend: '本周' },
    { label: '平均提交率', value: `${Math.round(avgRate)}%`, trend: '全课程' }
  ]
})

const deadlineBoard = computed(() =>
  [...assignmentList.value]
    .filter((item) => item.status !== '已截止')
    .sort((a, b) => new Date(a.deadline).getTime() - new Date(b.deadline).getTime())
    .slice(0, 3)
)

function submitRate(row) {
  return Math.round((row.submitted / row.total) * 100)
}

function statusTagType(status) {
  if (status === '进行中') return 'success'
  if (status === '已截止') return 'info'
  return 'warning'
}
</script>

<template>
  <div class="assignment-management">
    <section class="page-head">
      <div>
        <h1 class="page-title">作业管理</h1>
        <p class="page-subtitle">教学资源 / 作业管理</p>
      </div>
      <div class="head-actions">
        <el-button class="action-btn" type="default">
          <el-icon><Calendar /></el-icon>
          <span>本月作业</span>
        </el-button>
        <el-button class="action-btn action-btn--primary" type="primary">
          <el-icon><Plus /></el-icon>
          <span>发布作业</span>
        </el-button>
      </div>
    </section>

    <section class="stats-grid">
      <article v-for="card in stats" :key="card.label" class="stat-card">
        <p class="stat-card__label">{{ card.label }}</p>
        <p class="stat-card__value">{{ card.value }}</p>
        <p class="stat-card__trend">{{ card.trend }}</p>
      </article>
    </section>

    <section class="panel">
      <header class="panel-toolbar">
        <div class="toolbar-left">
          <el-input v-model="searchKeyword" placeholder="搜索作业标题 / 编号 / 教师" clearable class="toolbar-input">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select v-model="courseFilter" class="toolbar-select">
            <el-option v-for="option in courseOptions" :key="option" :label="option" :value="option" />
          </el-select>
          <el-select v-model="statusFilter" class="toolbar-select">
            <el-option v-for="option in statusOptions" :key="option" :label="option" :value="option" />
          </el-select>
        </div>
        <div class="toolbar-right">
          <el-button class="action-btn" type="default">
            <el-icon><Refresh /></el-icon>
            <span>刷新</span>
          </el-button>
          <el-button class="action-btn" type="default">
            <el-icon><UploadFilled /></el-icon>
            <span>导出</span>
          </el-button>
        </div>
      </header>

      <div class="content-grid">
        <article class="table-wrap">
          <el-table :data="filteredAssignments" stripe>
            <el-table-column prop="id" label="作业编号" min-width="120" />
            <el-table-column prop="title" label="作业标题" min-width="220" show-overflow-tooltip />
            <el-table-column prop="course" label="课程" min-width="150" show-overflow-tooltip />
            <el-table-column prop="className" label="班级" min-width="120" />
            <el-table-column prop="teacher" label="教师" min-width="90" />
            <el-table-column label="提交进度" min-width="170">
              <template #default="{ row }">
                <div class="rate-cell">
                  <el-progress :percentage="submitRate(row)" :stroke-width="8" />
                  <span>{{ row.submitted }}/{{ row.total }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)" effect="light">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="deadline" label="截止时间" min-width="160" />
            <el-table-column prop="created_at" label="created_at" min-width="170" />
            <el-table-column prop="updated_at" label="updated_at" min-width="170" />
          </el-table>
        </article>

        <aside class="side-panel">
          <article class="mini-card">
            <h3>临近截止</h3>
            <div class="deadline-list">
              <div v-for="item in deadlineBoard" :key="item.id" class="deadline-item">
                <p class="deadline-item__title">{{ item.title }}</p>
                <p class="deadline-item__meta">{{ item.className }} · {{ item.deadline }}</p>
              </div>
            </div>
          </article>
          <article class="mini-card">
            <h3>接口切换说明</h3>
            <p>当前为 mock 数据。接入真实接口时，将 assignmentList 替换为 src/api/admin/assignment.js 请求结果。</p>
          </article>
        </aside>
      </div>
    </section>
  </div>
</template>

<style scoped>
.assignment-management {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.page-title {
  margin: 0;
  font-size: 30px;
  line-height: 38px;
  font-weight: 700;
  color: #191c1e;
}

.page-subtitle {
  margin: 4px 0 0;
  color: #404753;
  font-size: 14px;
}

.head-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  border-radius: 10px;
  height: 38px;
}

.action-btn--primary {
  border: none;
  background: #005daa;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.stat-card {
  background: #ffffff;
  border: 1px solid #eef2f7;
  border-radius: 12px;
  padding: 16px;
}

.stat-card__label {
  margin: 0;
  color: #64748b;
  font-size: 12px;
}

.stat-card__value {
  margin: 6px 0 0;
  color: #0f172a;
  font-size: 28px;
  font-weight: 800;
  line-height: 1.2;
}

.stat-card__trend {
  margin: 6px 0 0;
  color: #0f9f6e;
  font-size: 12px;
  font-weight: 600;
}

.panel {
  background: #ffffff;
  border: 1px solid #eef2f7;
  border-radius: 16px;
  padding: 16px;
}

.panel-toolbar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.toolbar-input {
  width: 280px;
}

.toolbar-select {
  width: 150px;
}

.content-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 14px;
}

.table-wrap {
  border: 1px solid #eef2f7;
  border-radius: 12px;
  overflow: hidden;
}

.rate-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rate-cell span {
  color: #475569;
  font-size: 12px;
  white-space: nowrap;
}

.side-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mini-card {
  border: 1px solid #eef2f7;
  border-radius: 12px;
  padding: 14px;
  background: #fcfdff;
}

.mini-card h3 {
  margin: 0;
  font-size: 15px;
  color: #0f172a;
}

.mini-card p {
  margin: 10px 0 0;
  color: #64748b;
  font-size: 13px;
  line-height: 20px;
}

.deadline-list {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.deadline-item {
  border-radius: 10px;
  border: 1px solid #edf1f6;
  padding: 10px;
}

.deadline-item__title {
  margin: 0;
  color: #1e293b;
  font-size: 13px;
  font-weight: 700;
}

.deadline-item__meta {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 12px;
}

@media (max-width: 1180px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .toolbar-input,
  .toolbar-select {
    width: 100%;
  }
}

:global(html.dark) .page-title,
:global(html.dark) .stat-card__value,
:global(html.dark) .mini-card h3 {
  color: #e5eaf3;
}

:global(html.dark) .page-subtitle,
:global(html.dark) .stat-card__label,
:global(html.dark) .rate-cell span,
:global(html.dark) .deadline-item__meta,
:global(html.dark) .mini-card p {
  color: #9ba7b8;
}

:global(html.dark) .panel,
:global(html.dark) .stat-card,
:global(html.dark) .table-wrap,
:global(html.dark) .mini-card,
:global(html.dark) .deadline-item {
  background: #111827;
  border-color: #1f2937;
}

:global(html.dark) .deadline-item__title {
  color: #dce3ee;
}
</style>

