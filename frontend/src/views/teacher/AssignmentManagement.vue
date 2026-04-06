<script setup>
import { computed, ref } from 'vue'
import { Calendar, Clock, Document, Plus, RefreshRight, Search, Upload } from '@element-plus/icons-vue'

// 教师端作业管理 Mock 数据（接入后端时替换为 src/api/teacher/assignment.js）
const filterForm = ref({
  keyword: '',
  className: '全部班级',
  status: '全部状态'
})

const classOptions = ['全部班级', '高二（3）班', '高二（6）班', '竞赛1班', '高一（2）班']
const statusOptions = ['全部状态', '待发布', '进行中', '已截止']

const assignmentList = ref([
  {
    assignmentId: 'T-A-2026040601',
    title: '函数与导数综合训练（第 4 讲）',
    className: '高二（3）班',
    publishTime: '2026-04-06 10:20',
    deadline: '2026-04-09 23:00',
    status: '进行中',
    submitted: 41,
    total: 45,
    avgDuration: '34 分钟',
    created_at: '2026-04-06 10:20:00',
    updated_at: '2026-04-06 10:20:00'
  },
  {
    assignmentId: 'T-A-2026040402',
    title: '三角函数图像随堂练习',
    className: '高二（6）班',
    publishTime: '2026-04-04 18:30',
    deadline: '2026-04-07 22:00',
    status: '进行中',
    submitted: 38,
    total: 44,
    avgDuration: '29 分钟',
    created_at: '2026-04-04 18:30:00',
    updated_at: '2026-04-05 08:12:00'
  },
  {
    assignmentId: 'T-A-2026040303',
    title: '竞赛班不等式证明专题',
    className: '竞赛1班',
    publishTime: '2026-04-03 19:10',
    deadline: '2026-04-06 20:30',
    status: '已截止',
    submitted: 26,
    total: 28,
    avgDuration: '47 分钟',
    created_at: '2026-04-03 19:10:00',
    updated_at: '2026-04-06 20:31:00'
  },
  {
    assignmentId: 'T-A-2026040704',
    title: '数列单元基础检测（预告）',
    className: '高一（2）班',
    publishTime: '2026-04-07 08:00',
    deadline: '2026-04-11 21:00',
    status: '待发布',
    submitted: 0,
    total: 42,
    avgDuration: '--',
    created_at: '2026-04-07 08:00:00',
    updated_at: '2026-04-07 08:00:00'
  }
])

const filteredAssignments = computed(() =>
  assignmentList.value.filter((item) => {
    const keyword = filterForm.value.keyword.trim()
    const matchKeyword =
      !keyword || item.title.includes(keyword) || item.assignmentId.includes(keyword) || item.className.includes(keyword)
    const matchClass = filterForm.value.className === '全部班级' || item.className === filterForm.value.className
    const matchStatus = filterForm.value.status === '全部状态' || item.status === filterForm.value.status
    return matchKeyword && matchClass && matchStatus
  })
)

const dashboardCards = computed(() => {
  const total = assignmentList.value.length
  const running = assignmentList.value.filter((item) => item.status === '进行中').length
  const pending = assignmentList.value.filter((item) => item.status === '待发布').length
  const submitRate = Math.round(
    (assignmentList.value.reduce((sum, item) => sum + item.submitted, 0) /
      assignmentList.value.reduce((sum, item) => sum + item.total, 0)) *
      100
  )

  return [
    { label: '作业总数', value: `${total}`, desc: '当前学周' },
    { label: '进行中', value: `${running}`, desc: '需跟进批改' },
    { label: '待发布', value: `${pending}`, desc: '可一键发布' },
    { label: '整体提交率', value: `${Number.isFinite(submitRate) ? submitRate : 0}%`, desc: '全部班级' }
  ]
})

const upcomingList = computed(() =>
  [...assignmentList.value]
    .filter((item) => item.status !== '已截止')
    .sort((a, b) => new Date(a.deadline).getTime() - new Date(b.deadline).getTime())
    .slice(0, 3)
)

function getSubmitRate(row) {
  if (!row.total) return 0
  return Math.round((row.submitted / row.total) * 100)
}

function assignmentStatusType(status) {
  if (status === '进行中') return 'success'
  if (status === '待发布') return 'warning'
  return 'info'
}

function resetFilters() {
  filterForm.value.keyword = ''
  filterForm.value.className = '全部班级'
  filterForm.value.status = '全部状态'
}
</script>

<template>
  <div class="teacher-assignment">
    <section class="hero">
      <div>
        <p class="hero__path">教学资源 / 作业管理</p>
        <h1 class="hero__title">作业管理</h1>
        <p class="hero__desc">统一管理班级作业发布、提交进度与批改安排</p>
      </div>
      <div class="hero__actions">
        <el-button class="hero-btn" plain>
          <el-icon><Calendar /></el-icon>
          <span>作业日历</span>
        </el-button>
        <el-button class="hero-btn hero-btn--primary" type="primary">
          <el-icon><Plus /></el-icon>
          <span>发布作业</span>
        </el-button>
      </div>
    </section>

    <section class="stats-grid">
      <article v-for="card in dashboardCards" :key="card.label" class="stat-card">
        <p class="stat-card__label">{{ card.label }}</p>
        <p class="stat-card__value">{{ card.value }}</p>
        <p class="stat-card__desc">{{ card.desc }}</p>
      </article>
    </section>

    <section class="panel">
      <header class="panel__toolbar">
        <div class="toolbar__left">
          <el-input v-model="filterForm.keyword" placeholder="搜索作业标题 / 编号 / 班级" clearable class="toolbar__input">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select v-model="filterForm.className" class="toolbar__select">
            <el-option v-for="item in classOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="filterForm.status" class="toolbar__select">
            <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </div>
        <div class="toolbar__right">
          <el-button class="panel-btn" @click="resetFilters">
            <el-icon><RefreshRight /></el-icon>
            <span>重置</span>
          </el-button>
          <el-button class="panel-btn">
            <el-icon><Upload /></el-icon>
            <span>导出</span>
          </el-button>
        </div>
      </header>

      <div class="content-grid">
        <article class="table-wrap">
          <el-table :data="filteredAssignments" stripe>
            <el-table-column prop="assignmentId" label="作业编号" min-width="150" />
            <el-table-column prop="title" label="作业标题" min-width="220" show-overflow-tooltip />
            <el-table-column prop="className" label="班级" min-width="110" />
            <el-table-column prop="publishTime" label="发布时间" min-width="140" />
            <el-table-column prop="deadline" label="截止时间" min-width="140" />
            <el-table-column label="提交率" min-width="170">
              <template #default="{ row }">
                <div class="rate-cell">
                  <el-progress :percentage="getSubmitRate(row)" :stroke-width="8" />
                  <span>{{ row.submitted }}/{{ row.total }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="avgDuration" label="平均耗时" min-width="90" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="assignmentStatusType(row.status)" effect="light">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="created_at" label="created_at" min-width="170" />
            <el-table-column prop="updated_at" label="updated_at" min-width="170" />
          </el-table>
        </article>

        <aside class="side-panel">
          <article class="mini-card">
            <h3><el-icon><Clock /></el-icon> 临近截止</h3>
            <div class="deadline-list">
              <div v-for="item in upcomingList" :key="item.assignmentId" class="deadline-item">
                <p class="deadline-item__title">{{ item.title }}</p>
                <p class="deadline-item__meta">{{ item.className }} · {{ item.deadline }}</p>
              </div>
            </div>
          </article>
          <article class="mini-card">
            <h3><el-icon><Document /></el-icon> Mock / API 切换</h3>
            <p>当前页面数据来自本地 mock。接入后端时，将 assignmentList 替换为 src/api/teacher/assignment.js 的接口结果。</p>
          </article>
        </aside>
      </div>
    </section>
  </div>
</template>

<style scoped>
.teacher-assignment {
  max-width: 1160px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero {
  border-radius: 14px;
  padding: 22px;
  background: linear-gradient(135deg, #155e75 0%, #2563eb 100%);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.hero__path {
  margin: 0;
  color: rgba(255, 255, 255, 0.86);
  font-size: 13px;
}

.hero__title {
  margin: 6px 0 0;
  color: #ffffff;
  font-size: 30px;
  line-height: 38px;
  font-weight: 800;
}

.hero__desc {
  margin: 8px 0 0;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.hero__actions {
  display: flex;
  gap: 10px;
}

.hero-btn {
  height: 38px;
  border-radius: 10px;
}

.hero-btn--primary {
  border: none;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.stat-card {
  border: 1px solid #e5eaf3;
  border-radius: 12px;
  background: #ffffff;
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
  font-size: 30px;
  line-height: 1.2;
  font-weight: 800;
}

.stat-card__desc {
  margin: 6px 0 0;
  color: #475569;
  font-size: 12px;
}

.panel {
  border: 1px solid #e5eaf3;
  border-radius: 16px;
  background: #ffffff;
  padding: 16px;
}

.panel__toolbar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar__left,
.toolbar__right {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.toolbar__input {
  width: 280px;
}

.toolbar__select {
  width: 160px;
}

.panel-btn {
  height: 36px;
  border-radius: 10px;
}

.content-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 14px;
}

.table-wrap {
  border: 1px solid #e5eaf3;
  border-radius: 12px;
  overflow: hidden;
}

.rate-cell {
  display: flex;
  align-items: center;
  gap: 8px;
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
  border-radius: 12px;
  border: 1px solid #e5eaf3;
  background: #f8fafc;
  padding: 14px;
}

.mini-card h3 {
  margin: 0;
  color: #0f172a;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.mini-card p {
  margin: 10px 0 0;
  color: #475569;
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
  border: 1px solid #dbe4f0;
  background: #ffffff;
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

@media (max-width: 1120px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 680px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .toolbar__input,
  .toolbar__select {
    width: 100%;
  }
}

:global(html.dark) .stat-card,
:global(html.dark) .panel,
:global(html.dark) .table-wrap,
:global(html.dark) .mini-card,
:global(html.dark) .deadline-item {
  background: #131922;
  border-color: #273244;
}

:global(html.dark) .stat-card__value,
:global(html.dark) .mini-card h3,
:global(html.dark) .deadline-item__title {
  color: #e5eaf3;
}

:global(html.dark) .stat-card__label,
:global(html.dark) .stat-card__desc,
:global(html.dark) .rate-cell span,
:global(html.dark) .mini-card p,
:global(html.dark) .deadline-item__meta {
  color: #9ba7ba;
}

:global(html.dark) .deadline-item {
  background: #0f141c;
}
</style>

