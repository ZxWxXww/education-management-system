<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchAdminComparativeAnalysis } from '../../api/admin/report'

const queryForm = ref({
  campus: '全部校区',
  compareType: '班级对比',
  period: '当前全量数据'
})

const loading = ref(false)
const campusOptions = ['全部校区']
const compareTypeOptions = ['班级对比', '课程对比', '教师对比']
const periodOptions = ['当前全量数据']

const kpiCards = ref([])
const compareLabels = ref([])
const courseSeries = ref([])
const homeworkSeries = ref([])
const scoreTrendLabels = ref([])
const scoreSeriesA = ref([])
const scoreSeriesB = ref([])
const compareRows = ref([])
const insightCards = ref([])

const scorePathA = computed(() => buildLinePath(scoreSeriesA.value, 560, 210, 28))
const scorePathB = computed(() => buildLinePath(scoreSeriesB.value, 560, 210, 28))

function buildLinePath(points, width, height, padding) {
  if (!points.length) return ''
  const max = Math.max(...points)
  const min = Math.min(...points)
  const span = max - min || 1
  return points
    .map((value, index) => {
      const x = points.length === 1 ? width / 2 : (index / (points.length - 1)) * width
      const y = height - ((value - min) / span) * (height - padding * 2) - padding
      return `${x.toFixed(2)},${y.toFixed(2)}`
    })
    .join(' ')
}

function getScoreType(score) {
  if (score >= 90) return 'success'
  if (score >= 75) return 'warning'
  return 'info'
}

function getAbnormalType(rate) {
  if (rate >= 5.5) return 'danger'
  if (rate >= 4.5) return 'warning'
  return 'success'
}

async function loadAnalysis() {
  loading.value = true
  try {
    const data = await fetchAdminComparativeAnalysis(queryForm.value.compareType)
    kpiCards.value = data.kpiCards
    compareLabels.value = data.compareLabels
    courseSeries.value = data.courseSeries
    homeworkSeries.value = data.homeworkSeries
    scoreTrendLabels.value = data.scoreTrendLabels
    scoreSeriesA.value = data.scoreSeriesA
    scoreSeriesB.value = data.scoreSeriesB
    compareRows.value = data.compareRows
    insightCards.value = data.insightCards
  } catch (error) {
    ElMessage.error(error.message || '对比分析加载失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  queryForm.value = {
    campus: '全部校区',
    compareType: '班级对比',
    period: '当前全量数据'
  }
  loadAnalysis()
}

onMounted(() => {
  loadAnalysis()
})
</script>

<template>
  <div v-loading="loading" class="analysis-page">
    <section class="analysis-head">
      <div>
        <h1 class="analysis-title">对比分析</h1>
        <p class="analysis-subtitle">管理员端 / 数据报表 / 对比分析</p>
      </div>
      <div class="analysis-actions">
        <el-tag type="info" effect="plain">实时聚合视图</el-tag>
      </div>
    </section>

    <section class="filter-panel">
      <el-select v-model="queryForm.campus" disabled>
        <el-option v-for="option in campusOptions" :key="option" :label="option" :value="option" />
      </el-select>
      <el-select v-model="queryForm.compareType">
        <el-option v-for="option in compareTypeOptions" :key="option" :label="option" :value="option" />
      </el-select>
      <el-select v-model="queryForm.period" disabled>
        <el-option v-for="option in periodOptions" :key="option" :label="option" :value="option" />
      </el-select>
      <el-input value="当前按已打通真实数据全量聚合" readonly />
      <el-button type="primary" @click="loadAnalysis">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </section>

    <section class="kpi-grid">
      <article v-for="card in kpiCards" :key="card.label" class="kpi-card">
        <p class="kpi-label">{{ card.label }}</p>
        <div class="kpi-main">
          <p class="kpi-value">{{ card.value }}</p>
          <span class="kpi-delta" :class="`kpi-delta--${card.tone}`">{{ card.delta }}</span>
        </div>
      </article>
    </section>

    <section class="panel-grid">
      <article class="panel panel--bars">
        <header class="panel-head">
          <h2>对象表现对比</h2>
          <el-tag round effect="plain">到课率 / 作业提交率</el-tag>
        </header>
        <div class="bar-list">
          <div v-for="(label, idx) in compareLabels" :key="label" class="bar-item">
            <div class="bar-item__top">
              <span>{{ label }}</span>
              <span>{{ courseSeries[idx] }}% / {{ homeworkSeries[idx] }}%</span>
            </div>
            <div class="bar-item__track">
              <div class="bar-item__bar bar-item__bar--blue" :style="{ width: `${courseSeries[idx]}%` }"></div>
              <div class="bar-item__bar bar-item__bar--green" :style="{ width: `${homeworkSeries[idx]}%` }"></div>
            </div>
          </div>
        </div>
      </article>

      <article class="panel panel--insight">
        <header class="panel-head panel-head--single">
          <h2>分析洞察</h2>
        </header>
        <div class="insight-list">
          <div v-for="item in insightCards" :key="item.title" class="insight-item">
            <p class="insight-title">{{ item.title }}</p>
            <p class="insight-content">{{ item.content }}</p>
          </div>
        </div>
      </article>
    </section>

    <section class="panel">
      <header class="panel-head">
        <h2>平均成绩对比曲线</h2>
        <div class="legend-group">
          <span><i class="dot dot--blue"></i>对象均分</span>
          <span><i class="dot dot--purple"></i>整体均分</span>
        </div>
      </header>
      <div class="line-chart">
        <svg viewBox="0 0 560 230" preserveAspectRatio="none" aria-hidden="true">
          <defs>
            <linearGradient id="scoreFillA" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stop-color="#2563eb" stop-opacity="0.24" />
              <stop offset="100%" stop-color="#2563eb" stop-opacity="0" />
            </linearGradient>
          </defs>
          <polygon v-if="scorePathA" :points="`0,214 ${scorePathA} 560,214`" fill="url(#scoreFillA)" />
          <polyline v-if="scorePathA" :points="scorePathA" fill="none" stroke="#2563eb" stroke-width="4" stroke-linejoin="round" stroke-linecap="round" />
          <polyline v-if="scorePathB" :points="scorePathB" fill="none" stroke="#7c3aed" stroke-width="4" stroke-linejoin="round" stroke-linecap="round" />
        </svg>
        <div class="line-chart-axis">
          <span v-for="label in scoreTrendLabels" :key="label">{{ label }}</span>
        </div>
      </div>
    </section>

    <section class="panel">
      <header class="panel-head panel-head--single">
        <h2>对比明细排行</h2>
      </header>
      <el-table :data="compareRows" stripe>
        <el-table-column prop="rank" label="排名" width="72" />
        <el-table-column prop="target" label="对象" min-width="180" />
        <el-table-column label="到课率" width="112">
          <template #default="{ row }">
            {{ row.attendanceRate }}%
          </template>
        </el-table-column>
        <el-table-column label="作业提交率" width="128">
          <template #default="{ row }">
            {{ row.homeworkRate }}%
          </template>
        </el-table-column>
        <el-table-column label="平均成绩" width="112">
          <template #default="{ row }">
            <el-tag :type="getScoreType(row.averageScore)" effect="light">{{ row.averageScore }}分</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="异常考勤占比" width="132">
          <template #default="{ row }">
            <el-tag :type="getAbnormalType(row.abnormalRate)" effect="light">{{ row.abnormalRate }}%</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="综合得分" width="112">
          <template #default="{ row }">
            {{ row.compositeScore }}
          </template>
        </el-table-column>
        <el-table-column prop="teacherName" label="教师" min-width="120" />
      </el-table>
    </section>
  </div>
</template>

<style scoped>
.analysis-page {
  max-width: 1160px;
  margin: 0 auto;
  padding: 8px 4px 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.analysis-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.analysis-title {
  margin: 0;
  color: #111827;
  font-size: 28px;
  line-height: 38px;
  font-weight: 700;
}

.analysis-subtitle {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 14px;
  line-height: 22px;
}

.analysis-actions {
  display: flex;
  gap: 12px;
}

.filter-panel {
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  padding: 14px;
  display: grid;
  gap: 10px;
  grid-template-columns: repeat(3, minmax(130px, 1fr)) minmax(300px, 1.8fr) auto auto;
}

.kpi-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.kpi-card {
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  padding: 18px;
}

.kpi-label {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}

.kpi-main {
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.kpi-value {
  margin: 0;
  color: #111827;
  font-size: 28px;
  line-height: 36px;
  font-weight: 700;
}

.kpi-delta {
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 700;
}

.kpi-delta--blue {
  color: #1d4ed8;
  background: #dbeafe;
}

.kpi-delta--green {
  color: #15803d;
  background: #dcfce7;
}

.kpi-delta--purple {
  color: #6d28d9;
  background: #ede9fe;
}

.kpi-delta--orange {
  color: #c2410c;
  background: #ffedd5;
}

.panel-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: 2fr 1fr;
}

.panel {
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  padding: 18px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.panel-head--single {
  justify-content: flex-start;
}

.panel-head h2 {
  margin: 0;
  color: #111827;
  font-size: 18px;
  line-height: 28px;
}

.bar-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.bar-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.bar-item__top {
  display: flex;
  justify-content: space-between;
  color: #374151;
  font-size: 13px;
}

.bar-item__track {
  height: 18px;
  border-radius: 10px;
  background: #eef2ff;
  padding: 2px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.bar-item__bar {
  height: 6px;
  border-radius: 999px;
}

.bar-item__bar--blue {
  background: linear-gradient(90deg, #1d4ed8 0%, #60a5fa 100%);
}

.bar-item__bar--green {
  background: linear-gradient(90deg, #059669 0%, #34d399 100%);
}

.insight-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.insight-item {
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  background: #f8fafc;
  padding: 12px;
}

.insight-title {
  margin: 0;
  color: #111827;
  font-size: 14px;
  font-weight: 700;
}

.insight-content {
  margin: 6px 0 0;
  color: #4b5563;
  font-size: 13px;
  line-height: 20px;
}

.legend-group {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #4b5563;
  font-size: 12px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
  margin-right: 6px;
}

.dot--blue {
  background: #2563eb;
}

.dot--purple {
  background: #7c3aed;
}

.line-chart {
  position: relative;
  height: 230px;
  border-radius: 10px;
  overflow: hidden;
  background: linear-gradient(180deg, rgba(37, 99, 235, 0.1) 0%, rgba(37, 99, 235, 0) 100%);
}

.line-chart svg {
  width: 100%;
  height: 100%;
}

.line-chart-axis {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 8px;
  padding: 0 12px;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  color: #6b7280;
  font-size: 11px;
}

@media (max-width: 1100px) {
  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .filter-panel {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .panel-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .analysis-head {
    flex-direction: column;
    align-items: stretch;
  }

  .analysis-actions {
    width: 100%;
  }

  .analysis-actions :deep(.el-tag) {
    width: fit-content;
  }

  .kpi-grid,
  .filter-panel {
    grid-template-columns: 1fr;
  }

  .legend-group {
    display: none;
  }
}

:global(html.dark) .analysis-title,
:global(html.dark) .kpi-value,
:global(html.dark) .panel-head h2,
:global(html.dark) .insight-title {
  color: #e5e7eb;
}

:global(html.dark) .analysis-subtitle,
:global(html.dark) .kpi-label,
:global(html.dark) .bar-item__top,
:global(html.dark) .line-chart-axis,
:global(html.dark) .legend-group,
:global(html.dark) .insight-content {
  color: #9ca3af;
}

:global(html.dark) .filter-panel,
:global(html.dark) .kpi-card,
:global(html.dark) .panel {
  background: #1f2937;
  border-color: #374151;
}

:global(html.dark) .insight-item {
  border-color: #374151;
  background: #111827;
}

:global(html.dark) .bar-item__track {
  background: #253044;
}

:global(html.dark) .line-chart {
  background: linear-gradient(180deg, rgba(59, 130, 246, 0.16) 0%, rgba(59, 130, 246, 0) 100%);
}
</style>

