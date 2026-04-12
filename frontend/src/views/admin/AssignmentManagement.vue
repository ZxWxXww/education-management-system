<script setup>
import { computed, onMounted, ref } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { fetchAdminAssignmentPage } from '../../api/admin/assignment'

const searchKeyword = ref('')
const statusFilter = ref('全部状态')
const courseFilter = ref('全部课程')
const loading = ref(false)

const statusOptions = ['全部状态', '未开始', '进行中', '已截止']
const assignmentList = ref([])

const courseOptions = computed(() => [
  '全部课程',
  ...new Set(assignmentList.value.map((item) => item.course).filter(Boolean))
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
  const visibleAssignments = filteredAssignments.value
  const total = visibleAssignments.length
  const running = visibleAssignments.filter((item) => item.status === '进行中').length
  const closed = visibleAssignments.filter((item) => item.status === '已截止').length
  const avgRate =
    visibleAssignments.reduce((acc, item) => acc + submitRate(item), 0) / (total || 1)

  return [
    { label: '当前筛选作业', value: `${total}`, trend: '按筛选条件统计' },
    { label: '当前筛选进行中', value: `${running}`, trend: '按筛选条件统计' },
    { label: '当前筛选已截止', value: `${closed}`, trend: '按筛选条件统计' },
    { label: '平均提交率', value: `${Math.round(avgRate)}%`, trend: '按当前筛选作业计算' }
  ]
})

const deadlineBoard = computed(() =>
  [...filteredAssignments.value]
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

async function loadAssignments() {
  loading.value = true
  try {
    const page = await fetchAdminAssignmentPage({ pageNum: 1, pageSize: 200 })
    assignmentList.value = page.list
  } catch (error) {
    ElMessage.error(error.message || '管理员作业列表加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAssignments()
})
</script>

<template>
  <div v-loading="loading" class="assignment-management">
    <section class="page-head">
      <div>
        <h1 class="page-title">作业管理</h1>
        <p class="page-subtitle">教学资源 / 作业管理</p>
      </div>
      <div class="head-actions">
        <el-tag type="info" effect="plain">仅展示真实作业监控数据</el-tag>
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
          <el-button class="action-btn" type="default" @click="loadAssignments">
            <el-icon><Refresh /></el-icon>
            <span>刷新</span>
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
            <el-table-column prop="createdAt" label="创建时间" min-width="170" />
            <el-table-column prop="updatedAt" label="更新时间" min-width="170" />
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
            <h3>真实数据说明</h3>
            <p>当前页面已切换为管理员真实作业聚合视图，提交进度由班级学生数与提交记录实时计算。</p>
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

