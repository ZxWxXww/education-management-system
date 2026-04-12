<script setup>
import { computed, onMounted, ref } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { fetchAdminGradePage } from '../../api/admin/grade'

const keyword = ref('')
const courseFilter = ref('全部课程')
const termFilter = ref('全部时间')
const loading = ref(false)

const gradeList = ref([])

const courseOptions = computed(() => [
  '全部课程',
  ...new Set(gradeList.value.map((item) => item.course).filter(Boolean))
])

const termOptions = computed(() => [
  '全部时间',
  ...new Set(
    gradeList.value
      .map((item) => item.publishTime && item.publishTime !== '-' ? item.publishTime.slice(0, 7) : '')
      .filter(Boolean)
  )
])

const filteredGrades = computed(() =>
  gradeList.value.filter((row) => {
    const matchKeyword =
      !keyword.value ||
      row.studentName.includes(keyword.value) ||
      row.studentNo.includes(keyword.value) ||
      row.assignment.includes(keyword.value)
    const matchCourse = courseFilter.value === '全部课程' || row.course === courseFilter.value
    const matchTerm = termFilter.value === '全部时间' || row.publishTime.startsWith(termFilter.value)
    return matchKeyword && matchCourse && matchTerm
  })
)

const gradeStats = computed(() => {
  const all = filteredGrades.value
  const avg = Math.round(all.reduce((sum, row) => sum + row.score, 0) / (all.length || 1))
  const excellent = all.filter((row) => row.score >= 90).length
  const pass = all.filter((row) => row.score >= 60).length
  return [
    { label: '平均分', value: `${avg}`, tip: '当前筛选结果' },
    { label: '优秀人数(90+)', value: `${excellent}`, tip: '当前筛选结果' },
    { label: '及格率', value: `${Math.round((pass / (all.length || 1)) * 100)}%`, tip: '当前筛选结果' },
    { label: '成绩记录', value: `${all.length}`, tip: termFilter.value === '全部时间' ? '全部时间' : termFilter.value }
  ]
})

const scoreSegments = computed(() => {
  const segments = [
    { label: '90-100', min: 90, max: 100, tone: 'excellent' },
    { label: '80-89', min: 80, max: 89, tone: 'good' },
    { label: '60-79', min: 60, max: 79, tone: 'pass' },
    { label: '0-59', min: 0, max: 59, tone: 'fail' }
  ]

  return segments.map((seg) => {
    const count = filteredGrades.value.filter((row) => row.score >= seg.min && row.score <= seg.max).length
    const percent = Math.round((count / (filteredGrades.value.length || 1)) * 100)
    return { ...seg, count, percent }
  })
})

function scoreTagType(score) {
  if (score >= 90) return 'success'
  if (score >= 80) return ''
  if (score >= 60) return 'warning'
  return 'danger'
}

async function loadGrades() {
  loading.value = true
  try {
    const page = await fetchAdminGradePage({ pageNum: 1, pageSize: 200 })
    gradeList.value = page.list
  } catch (error) {
    ElMessage.error(error.message || '管理员成绩列表加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadGrades()
})
</script>

<template>
  <div v-loading="loading" class="grade-management">
    <section class="page-head">
      <div>
        <h1 class="page-title">成绩管理</h1>
        <p class="page-subtitle">教学资源 / 成绩管理</p>
      </div>
    </section>

    <section class="stats-grid">
      <article v-for="card in gradeStats" :key="card.label" class="stat-card">
        <p class="stat-card__label">{{ card.label }}</p>
        <p class="stat-card__value">{{ card.value }}</p>
        <p class="stat-card__tip">{{ card.tip }}</p>
      </article>
    </section>

    <section class="panel">
      <header class="panel-toolbar">
        <div class="toolbar-left">
          <el-input v-model="keyword" placeholder="搜索学员 / 学号 / 作业" clearable class="toolbar-input">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select v-model="courseFilter" class="toolbar-select">
            <el-option v-for="course in courseOptions" :key="course" :label="course" :value="course" />
          </el-select>
          <el-select v-model="termFilter" class="toolbar-select">
            <el-option v-for="term in termOptions" :key="term" :label="term" :value="term" />
          </el-select>
        </div>
        <div class="toolbar-right">
          <el-button class="action-btn" type="default" @click="loadGrades">
            <el-icon><Refresh /></el-icon>
            <span>刷新</span>
          </el-button>
        </div>
      </header>

      <div class="content-grid">
        <article class="table-wrap">
          <el-table :data="filteredGrades" stripe>
            <el-table-column prop="id" label="记录编号" min-width="125" />
            <el-table-column prop="studentName" label="学员" min-width="92" />
            <el-table-column prop="studentNo" label="学号" min-width="118" />
            <el-table-column prop="course" label="课程" min-width="145" />
            <el-table-column prop="className" label="班级" min-width="100" />
            <el-table-column prop="assignment" label="作业/考试" min-width="200" show-overflow-tooltip />
            <el-table-column label="成绩" width="90">
              <template #default="{ row }">
                <el-tag :type="scoreTagType(row.score)" effect="light">{{ row.score }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="rank" label="班级排名" width="95" />
            <el-table-column prop="teacher" label="任课教师" width="95" />
            <el-table-column prop="publishTime" label="发布时间" min-width="170" />
            <el-table-column prop="createdAt" label="创建时间" min-width="170" />
            <el-table-column prop="updatedAt" label="更新时间" min-width="170" />
          </el-table>
        </article>

        <aside class="side-panel">
          <article class="mini-card">
            <h3>分数段分布</h3>
            <div class="segment-list">
              <div v-for="seg in scoreSegments" :key="seg.label" class="segment-item">
                <div class="segment-item__head">
                  <span>{{ seg.label }}</span>
                  <span>{{ seg.count }}人</span>
                </div>
                <div class="segment-item__track">
                  <div class="segment-item__bar" :class="`segment-item__bar--${seg.tone}`" :style="{ width: `${seg.percent}%` }"></div>
                </div>
              </div>
            </div>
          </article>
          <article class="mini-card">
            <h3>真实数据说明</h3>
            <p>当前页面已切换为管理员考试成绩聚合视图，分数段分布和统计卡片均基于真实成绩记录计算。</p>
          </article>
        </aside>
      </div>
    </section>
  </div>
</template>

<style scoped>
.grade-management {
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

.action-btn {
  border-radius: 10px;
  height: 38px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.stat-card {
  border-radius: 12px;
  border: 1px solid #eef2f7;
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
  font-size: 28px;
  font-weight: 800;
}

.stat-card__tip {
  margin: 6px 0 0;
  color: #4b5563;
  font-size: 12px;
}

.panel {
  border-radius: 16px;
  border: 1px solid #eef2f7;
  background: #ffffff;
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
  width: 170px;
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

.side-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mini-card {
  border-radius: 12px;
  border: 1px solid #eef2f7;
  background: #fcfdff;
  padding: 14px;
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

.segment-list {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.segment-item__head {
  display: flex;
  justify-content: space-between;
  color: #475569;
  font-size: 12px;
}

.segment-item__track {
  margin-top: 5px;
  height: 8px;
  border-radius: 999px;
  background: #e9eef5;
  overflow: hidden;
}

.segment-item__bar {
  height: 100%;
  border-radius: 999px;
}

.segment-item__bar--excellent {
  background: linear-gradient(90deg, #16a34a 0%, #4ade80 100%);
}

.segment-item__bar--good {
  background: linear-gradient(90deg, #2563eb 0%, #60a5fa 100%);
}

.segment-item__bar--pass {
  background: linear-gradient(90deg, #d97706 0%, #fbbf24 100%);
}

.segment-item__bar--fail {
  background: linear-gradient(90deg, #dc2626 0%, #f87171 100%);
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
:global(html.dark) .stat-card__tip,
:global(html.dark) .segment-item__head,
:global(html.dark) .mini-card p {
  color: #9ba7b8;
}

:global(html.dark) .panel,
:global(html.dark) .stat-card,
:global(html.dark) .table-wrap,
:global(html.dark) .mini-card {
  background: #111827;
  border-color: #1f2937;
}

:global(html.dark) .segment-item__track {
  background: #1f2937;
}
</style>

