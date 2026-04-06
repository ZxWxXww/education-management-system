<script setup>
import { computed, ref } from 'vue'

// 对比分析页 mock 数据（后续接入真实接口时，可替换为 src/api/admin/report.js）
const queryForm = ref({
  campus: '全部校区',
  compareType: '班级对比',
  period: '近30天',
  dateRange: ['2026-03-10', '2026-04-08']
})

const campusOptions = ['全部校区', '东城校区', '南山校区', '北辰校区']
const compareTypeOptions = ['班级对比', '学科对比', '教师对比']
const periodOptions = ['近7天', '近30天', '近90天']

const kpiCards = [
  { label: '课程完课率均值', value: '92.6%', delta: '+3.1%', tone: 'blue' },
  { label: '作业按时提交率', value: '89.4%', delta: '+1.8%', tone: 'green' },
  { label: '平均提分幅度', value: '+11.2 分', delta: '+2.4%', tone: 'purple' },
  { label: '异常考勤占比', value: '4.7%', delta: '-0.9%', tone: 'orange' }
]

const compareLabels = ['高一数学A1', '高二英语B3', '初三物理冲刺', '高三化学提升', '高一语文强化']
const courseSeries = [93, 89, 95, 90, 87]
const homeworkSeries = [91, 84, 93, 88, 86]

const scoreTrendLabels = ['第1周', '第2周', '第3周', '第4周', '第5周', '第6周']
const scoreSeriesA = [72, 74, 75, 77, 79, 82]
const scoreSeriesB = [68, 69, 70, 72, 73, 75]

const compareRows = [
  {
    rank: 1,
    target: '初三物理冲刺班',
    completionRate: 95.2,
    homeworkRate: 93.4,
    scoreGrowth: 13.6,
    abnormalRate: 3.2,
    created_at: '2026-04-08 09:18:35',
    updated_at: '2026-04-08 11:46:52'
  },
  {
    rank: 2,
    target: '高一数学A1班',
    completionRate: 93.1,
    homeworkRate: 91.2,
    scoreGrowth: 12.4,
    abnormalRate: 4.1,
    created_at: '2026-04-08 09:20:08',
    updated_at: '2026-04-08 11:46:52'
  },
  {
    rank: 3,
    target: '高三化学提升班',
    completionRate: 90.3,
    homeworkRate: 88.8,
    scoreGrowth: 10.7,
    abnormalRate: 4.8,
    created_at: '2026-04-08 09:21:26',
    updated_at: '2026-04-08 11:46:52'
  },
  {
    rank: 4,
    target: '高二英语B3班',
    completionRate: 88.9,
    homeworkRate: 84.5,
    scoreGrowth: 8.9,
    abnormalRate: 5.6,
    created_at: '2026-04-08 09:22:57',
    updated_at: '2026-04-08 11:46:52'
  },
  {
    rank: 5,
    target: '高一语文强化班',
    completionRate: 87.4,
    homeworkRate: 86.1,
    scoreGrowth: 7.8,
    abnormalRate: 6.0,
    created_at: '2026-04-08 09:23:51',
    updated_at: '2026-04-08 11:46:52'
  }
]

const insightCards = [
  {
    title: '高贡献班级',
    content: '初三物理冲刺班在“完课率 + 作业提交率 + 提分”三项均位于第一梯队。'
  },
  {
    title: '需重点跟进',
    content: '高二英语B3班作业按时提交率偏低，建议优化作业提醒与课后答疑机制。'
  },
  {
    title: '风险提醒',
    content: '高一语文强化班异常考勤占比连续两周高于 5%，需排查到课时段与授课节奏。'
  }
]

const scorePathA = computed(() => buildLinePath(scoreSeriesA, 560, 210, 28))
const scorePathB = computed(() => buildLinePath(scoreSeriesB, 560, 210, 28))

function buildLinePath(points, width, height, padding) {
  const max = Math.max(...points)
  const min = Math.min(...points)
  const span = max - min || 1
  return points
    .map((value, index) => {
      const x = (index / (points.length - 1)) * width
      const y = height - ((value - min) / span) * (height - padding * 2) - padding
      return `${x.toFixed(2)},${y.toFixed(2)}`
    })
    .join(' ')
}

function getScoreType(scoreGrowth) {
  if (scoreGrowth >= 12) return 'success'
  if (scoreGrowth >= 9) return 'warning'
  return 'info'
}

function getAbnormalType(rate) {
  if (rate >= 5.5) return 'danger'
  if (rate >= 4.5) return 'warning'
  return 'success'
}
</script>

<template>
  <div class="analysis-page">
    <section class="analysis-head">
      <div>
        <h1 class="analysis-title">对比分析</h1>
        <p class="analysis-subtitle">管理员端 / 数据报表 / 对比分析</p>
      </div>
      <div class="analysis-actions">
        <el-button size="large">导出分析</el-button>
        <el-button type="primary" size="large">生成简报</el-button>
      </div>
    </section>

    <section class="filter-panel">
      <el-select v-model="queryForm.campus">
        <el-option v-for="option in campusOptions" :key="option" :label="option" :value="option" />
      </el-select>
      <el-select v-model="queryForm.compareType">
        <el-option v-for="option in compareTypeOptions" :key="option" :label="option" :value="option" />
      </el-select>
      <el-select v-model="queryForm.period">
        <el-option v-for="option in periodOptions" :key="option" :label="option" :value="option" />
      </el-select>
      <el-date-picker
        v-model="queryForm.dateRange"
        type="daterange"
        value-format="YYYY-MM-DD"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
      />
      <el-button type="primary">查询</el-button>
      <el-button>重置</el-button>
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
          <h2>班级表现对比</h2>
          <el-tag round effect="plain">完课率 / 作业率</el-tag>
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
        <h2>6周提分趋势对比</h2>
        <div class="legend-group">
          <span><i class="dot dot--blue"></i>高绩效班级均分</span>
          <span><i class="dot dot--purple"></i>全体班级均分</span>
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
          <polygon :points="`0,214 ${scorePathA} 560,214`" fill="url(#scoreFillA)" />
          <polyline :points="scorePathA" fill="none" stroke="#2563eb" stroke-width="4" stroke-linejoin="round" stroke-linecap="round" />
          <polyline :points="scorePathB" fill="none" stroke="#7c3aed" stroke-width="4" stroke-linejoin="round" stroke-linecap="round" />
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
        <el-table-column prop="target" label="班级" min-width="180" />
        <el-table-column label="完课率" width="112">
          <template #default="{ row }">
            {{ row.completionRate }}%
          </template>
        </el-table-column>
        <el-table-column label="作业提交率" width="128">
          <template #default="{ row }">
            {{ row.homeworkRate }}%
          </template>
        </el-table-column>
        <el-table-column label="提分幅度" width="112">
          <template #default="{ row }">
            <el-tag :type="getScoreType(row.scoreGrowth)" effect="light">+{{ row.scoreGrowth }}分</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="异常考勤占比" width="132">
          <template #default="{ row }">
            <el-tag :type="getAbnormalType(row.abnormalRate)" effect="light">{{ row.abnormalRate }}%</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="created_at" min-width="170" />
        <el-table-column prop="updated_at" label="updated_at" min-width="170" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default>
            <el-button link type="primary">查看详情</el-button>
          </template>
        </el-table-column>
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
  grid-template-columns: repeat(6, 1fr);
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

  .analysis-actions :deep(.el-button) {
    flex: 1;
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

