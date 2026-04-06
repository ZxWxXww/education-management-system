<script setup>
import { computed, ref } from 'vue'
import { DataAnalysis, Download, Histogram, RefreshRight, Search, TrendCharts, WarningFilled } from '@element-plus/icons-vue'

// 教师端成绩分析 Mock 数据（接入后端时替换为 src/api/teacher/grade-analysis.js）
const queryForm = ref({
  className: '全部班级',
  subject: '数学',
  examType: '月考',
  keyword: ''
})

const classOptions = ['全部班级', '高二（3）班', '高二（6）班', '竞赛1班']
const subjectOptions = ['数学', '物理', '英语']
const examTypeOptions = ['月考', '周测', '单元测']

const examOverviewList = ref([
  {
    id: 'GA-T-20260401',
    examName: '高二数学四月月考',
    className: '高二（3）班',
    subject: '数学',
    avgScore: 84.2,
    excellentRate: 34.1,
    passRate: 95.5,
    submitRate: 100,
    created_at: '2026-04-01 08:00:00',
    updated_at: '2026-04-01 18:10:00'
  },
  {
    id: 'GA-T-20260402',
    examName: '高二数学四月月考',
    className: '高二（6）班',
    subject: '数学',
    avgScore: 79.8,
    excellentRate: 22.7,
    passRate: 90.9,
    submitRate: 97.7,
    created_at: '2026-04-01 08:00:00',
    updated_at: '2026-04-01 18:16:00'
  },
  {
    id: 'GA-T-20260403',
    examName: '竞赛班阶段测评',
    className: '竞赛1班',
    subject: '数学',
    avgScore: 87.9,
    excellentRate: 46.4,
    passRate: 100,
    submitRate: 100,
    created_at: '2026-04-01 09:10:00',
    updated_at: '2026-04-01 19:02:00'
  }
])

const weakStudentList = ref([
  {
    studentName: '周一鸣',
    className: '高二（6）班',
    latestScore: 56,
    dropValue: -11,
    weakPoint: '立体几何空间向量',
    created_at: '2026-04-03 20:40:00',
    updated_at: '2026-04-03 20:40:00'
  },
  {
    studentName: '陈梦琪',
    className: '高二（3）班',
    latestScore: 61,
    dropValue: -8,
    weakPoint: '导数综合压轴题',
    created_at: '2026-04-03 20:42:00',
    updated_at: '2026-04-03 20:42:00'
  },
  {
    studentName: '郑宇航',
    className: '高二（6）班',
    latestScore: 59,
    dropValue: -9,
    weakPoint: '三角函数图像与性质',
    created_at: '2026-04-03 20:45:00',
    updated_at: '2026-04-03 20:45:00'
  }
])

const trendLabels = ['第1周', '第2周', '第3周', '第4周', '第5周', '第6周']
const classTrendData = {
  '高二（3）班': [73, 76, 79, 80, 83, 85],
  '高二（6）班': [69, 71, 73, 74, 77, 79],
  '竞赛1班': [78, 82, 84, 86, 88, 90]
}

const distributionData = [
  { label: '90-100', value: 28, tone: 'excellent' },
  { label: '80-89', value: 36, tone: 'good' },
  { label: '60-79', value: 29, tone: 'pass' },
  { label: '0-59', value: 7, tone: 'risk' }
]

const filteredOverview = computed(() => {
  const keyword = queryForm.value.keyword.trim()
  return examOverviewList.value.filter((item) => {
    const matchKeyword = !keyword || item.examName.includes(keyword) || item.className.includes(keyword)
    const matchClass = queryForm.value.className === '全部班级' || item.className === queryForm.value.className
    const matchSubject = item.subject === queryForm.value.subject
    const matchExamType = queryForm.value.examType === '月考' ? item.examName.includes('月考') : true
    return matchKeyword && matchClass && matchSubject && matchExamType
  })
})

const summaryCards = computed(() => {
  const rows = filteredOverview.value
  const avg = (rows.reduce((sum, row) => sum + row.avgScore, 0) / (rows.length || 1)).toFixed(1)
  const excellent = (rows.reduce((sum, row) => sum + row.excellentRate, 0) / (rows.length || 1)).toFixed(1)
  const pass = (rows.reduce((sum, row) => sum + row.passRate, 0) / (rows.length || 1)).toFixed(1)
  const submit = (rows.reduce((sum, row) => sum + row.submitRate, 0) / (rows.length || 1)).toFixed(1)
  return [
    { label: '班级平均分', value: `${avg}`, desc: '当前筛选范围' },
    { label: '优秀率', value: `${excellent}%`, desc: '90 分及以上' },
    { label: '及格率', value: `${pass}%`, desc: '60 分及以上' },
    { label: '成绩提交率', value: `${submit}%`, desc: '考试记录完整度' }
  ]
})

const classComparison = computed(() => {
  const list = Object.keys(classTrendData).map((className) => {
    const trend = classTrendData[className]
    const latest = trend[trend.length - 1]
    const previous = trend[trend.length - 2]
    return {
      className,
      latest,
      trendUp: latest - previous
    }
  })
  return list.sort((a, b) => b.latest - a.latest)
})

const topTrendPath = computed(() => buildLinePath(classTrendData['竞赛1班'], 560, 210, 20))
const middleTrendPath = computed(() => buildLinePath(classTrendData['高二（3）班'], 560, 210, 20))
const lowTrendPath = computed(() => buildLinePath(classTrendData['高二（6）班'], 560, 210, 20))

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

function resetQuery() {
  queryForm.value.className = '全部班级'
  queryForm.value.subject = '数学'
  queryForm.value.examType = '月考'
  queryForm.value.keyword = ''
}

function getDropType(dropValue) {
  if (dropValue <= -10) return 'danger'
  if (dropValue <= -7) return 'warning'
  return 'info'
}
</script>

<template>
  <div class="grade-analysis">
    <section class="hero">
      <div>
        <p class="hero__path">教学数据 / 成绩分析</p>
        <h1 class="hero__title">成绩分析</h1>
        <p class="hero__desc">按班级追踪成绩趋势与分层分布，快速识别风险学生并制定辅导策略</p>
      </div>
      <div class="hero__action">
        <el-button class="hero-btn" plain>
          <el-icon><Download /></el-icon>
          <span>导出报表</span>
        </el-button>
      </div>
    </section>

    <section class="filter-panel">
      <el-input v-model="queryForm.keyword" placeholder="搜索考试名称 / 班级" clearable>
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="queryForm.className">
        <el-option v-for="item in classOptions" :key="item" :label="item" :value="item" />
      </el-select>
      <el-select v-model="queryForm.subject">
        <el-option v-for="item in subjectOptions" :key="item" :label="item" :value="item" />
      </el-select>
      <el-select v-model="queryForm.examType">
        <el-option v-for="item in examTypeOptions" :key="item" :label="item" :value="item" />
      </el-select>
      <el-button class="filter-btn" type="primary">
        <el-icon><DataAnalysis /></el-icon>
        <span>分析</span>
      </el-button>
      <el-button class="filter-btn" @click="resetQuery">
        <el-icon><RefreshRight /></el-icon>
        <span>重置</span>
      </el-button>
    </section>

    <section class="summary-grid">
      <article v-for="card in summaryCards" :key="card.label" class="summary-card">
        <p class="summary-card__label">{{ card.label }}</p>
        <p class="summary-card__value">{{ card.value }}</p>
        <p class="summary-card__desc">{{ card.desc }}</p>
      </article>
    </section>

    <section class="main-grid">
      <article class="panel panel--chart">
        <header class="panel__head">
          <h2><el-icon><TrendCharts /></el-icon> 近 6 周班级均分趋势</h2>
          <div class="legend">
            <span><i class="dot dot--top"></i>竞赛1班</span>
            <span><i class="dot dot--mid"></i>高二（3）班</span>
            <span><i class="dot dot--low"></i>高二（6）班</span>
          </div>
        </header>
        <div class="line-chart">
          <svg viewBox="0 0 560 220" preserveAspectRatio="none" aria-hidden="true">
            <defs>
              <linearGradient id="trendAreaTop" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#10b981" stop-opacity="0.25" />
                <stop offset="100%" stop-color="#10b981" stop-opacity="0" />
              </linearGradient>
            </defs>
            <polygon :points="`0,210 ${topTrendPath} 560,210`" fill="url(#trendAreaTop)" />
            <polyline :points="topTrendPath" fill="none" stroke="#10b981" stroke-width="4" stroke-linecap="round" />
            <polyline :points="middleTrendPath" fill="none" stroke="#2563eb" stroke-width="4" stroke-linecap="round" />
            <polyline :points="lowTrendPath" fill="none" stroke="#f59e0b" stroke-width="4" stroke-linecap="round" />
          </svg>
          <div class="line-chart__axis">
            <span v-for="label in trendLabels" :key="label">{{ label }}</span>
          </div>
        </div>
      </article>

      <aside class="side-panel">
        <article class="panel panel--dist">
          <header class="panel__head panel__head--single">
            <h2><el-icon><Histogram /></el-icon> 分数段分布</h2>
          </header>
          <div class="dist-list">
            <div v-for="item in distributionData" :key="item.label" class="dist-item">
              <div class="dist-item__head">
                <span>{{ item.label }}</span>
                <span>{{ item.value }}%</span>
              </div>
              <div class="dist-item__track">
                <div class="dist-item__bar" :class="`dist-item__bar--${item.tone}`" :style="{ width: `${item.value}%` }"></div>
              </div>
            </div>
          </div>
        </article>

        <article class="panel panel--rank">
          <header class="panel__head panel__head--single">
            <h2><el-icon><DataAnalysis /></el-icon> 班级最新均分排名</h2>
          </header>
          <div class="rank-list">
            <div v-for="item in classComparison" :key="item.className" class="rank-item">
              <span>{{ item.className }}</span>
              <div>
                <strong>{{ item.latest }}</strong>
                <small>较上周 {{ item.trendUp >= 0 ? '+' : '' }}{{ item.trendUp }}</small>
              </div>
            </div>
          </div>
        </article>
      </aside>
    </section>

    <section class="panel">
      <header class="panel__head">
        <h2><el-icon><WarningFilled /></el-icon> 重点关注学生</h2>
      </header>
      <el-table :data="weakStudentList" stripe>
        <el-table-column prop="studentName" label="学生姓名" width="100" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="latestScore" label="最新成绩" width="100" />
        <el-table-column label="分数波动" width="110">
          <template #default="{ row }">
            <el-tag :type="getDropType(row.dropValue)" effect="light">{{ row.dropValue }} 分</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="weakPoint" label="薄弱知识点" min-width="220" show-overflow-tooltip />
        <el-table-column prop="created_at" label="created_at" min-width="170" />
        <el-table-column prop="updated_at" label="updated_at" min-width="170" />
      </el-table>
    </section>

    <section class="panel">
      <header class="panel__head panel__head--single">
        <h2><el-icon><DataAnalysis /></el-icon> 班级成绩概览</h2>
      </header>
      <el-table :data="filteredOverview" stripe>
        <el-table-column prop="id" label="分析编号" width="140" />
        <el-table-column prop="examName" label="考试名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="subject" label="学科" width="90" />
        <el-table-column label="平均分" width="100">
          <template #default="{ row }">{{ row.avgScore }}</template>
        </el-table-column>
        <el-table-column label="优秀率" width="100">
          <template #default="{ row }">{{ row.excellentRate }}%</template>
        </el-table-column>
        <el-table-column label="及格率" width="100">
          <template #default="{ row }">{{ row.passRate }}%</template>
        </el-table-column>
        <el-table-column label="提交率" width="100">
          <template #default="{ row }">{{ row.submitRate }}%</template>
        </el-table-column>
        <el-table-column prop="created_at" label="created_at" min-width="170" />
        <el-table-column prop="updated_at" label="updated_at" min-width="170" />
      </el-table>
    </section>
  </div>
</template>

<style scoped>
.grade-analysis {
  max-width: 1160px;
  margin: 0 auto;
  padding: 8px 4px 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero {
  border-radius: 14px;
  padding: 22px;
  background: linear-gradient(135deg, #1e40af 0%, #0f766e 100%);
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
  margin: 8px 0 0;
  color: #ffffff;
  font-size: 30px;
  line-height: 38px;
  font-weight: 800;
}

.hero__desc {
  margin: 8px 0 0;
  color: rgba(255, 255, 255, 0.94);
  font-size: 14px;
}

.hero-btn {
  height: 38px;
  border-radius: 10px;
}

.filter-panel {
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  padding: 14px;
  display: grid;
  gap: 10px;
  grid-template-columns: 1.6fr repeat(3, minmax(120px, 1fr)) auto auto;
}

.filter-btn {
  border-radius: 10px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.summary-card {
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  padding: 16px;
}

.summary-card__label {
  margin: 0;
  color: #6b7280;
  font-size: 12px;
}

.summary-card__value {
  margin: 8px 0 0;
  color: #111827;
  font-size: 30px;
  line-height: 1.2;
  font-weight: 800;
}

.summary-card__desc {
  margin: 6px 0 0;
  color: #4b5563;
  font-size: 12px;
}

.main-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 14px;
}

.panel {
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  padding: 16px;
}

.panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 14px;
}

.panel__head--single {
  justify-content: flex-start;
}

.panel__head h2 {
  margin: 0;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #111827;
  font-size: 16px;
  font-weight: 700;
}

.line-chart {
  position: relative;
  height: 220px;
  border-radius: 10px;
  overflow: hidden;
  background: linear-gradient(180deg, rgba(37, 99, 235, 0.12) 0%, rgba(37, 99, 235, 0) 100%);
}

.line-chart svg {
  width: 100%;
  height: 100%;
}

.line-chart__axis {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 6px;
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  padding: 0 12px;
  color: #6b7280;
  font-size: 11px;
}

.legend {
  display: flex;
  gap: 10px;
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

.dot--top {
  background: #10b981;
}

.dot--mid {
  background: #2563eb;
}

.dot--low {
  background: #f59e0b;
}

.side-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.dist-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dist-item__head {
  display: flex;
  justify-content: space-between;
  color: #475569;
  font-size: 12px;
}

.dist-item__track {
  margin-top: 5px;
  height: 8px;
  border-radius: 999px;
  background: #e5e7eb;
  overflow: hidden;
}

.dist-item__bar {
  height: 100%;
  border-radius: 999px;
}

.dist-item__bar--excellent {
  background: linear-gradient(90deg, #16a34a 0%, #4ade80 100%);
}

.dist-item__bar--good {
  background: linear-gradient(90deg, #2563eb 0%, #60a5fa 100%);
}

.dist-item__bar--pass {
  background: linear-gradient(90deg, #d97706 0%, #fbbf24 100%);
}

.dist-item__bar--risk {
  background: linear-gradient(90deg, #dc2626 0%, #f87171 100%);
}

.rank-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.rank-item {
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  background: #f8fafc;
  padding: 10px 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #334155;
  font-size: 13px;
}

.rank-item strong {
  color: #0f172a;
  margin-right: 8px;
}

.rank-item small {
  color: #64748b;
}

@media (max-width: 1120px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .filter-panel {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .main-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 700px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .filter-panel {
    grid-template-columns: 1fr;
  }

  .legend {
    display: none;
  }
}

:global(html.dark) .filter-panel,
:global(html.dark) .summary-card,
:global(html.dark) .panel,
:global(html.dark) .rank-item {
  background: #1a2028;
  border-color: #2b3442;
}

:global(html.dark) .summary-card__value,
:global(html.dark) .panel__head h2,
:global(html.dark) .rank-item strong {
  color: #e5eaf3;
}

:global(html.dark) .summary-card__label,
:global(html.dark) .summary-card__desc,
:global(html.dark) .legend,
:global(html.dark) .line-chart__axis,
:global(html.dark) .rank-item,
:global(html.dark) .rank-item small,
:global(html.dark) .dist-item__head {
  color: #9ba7ba;
}

:global(html.dark) .line-chart {
  background: linear-gradient(180deg, rgba(59, 130, 246, 0.2) 0%, rgba(59, 130, 246, 0) 100%);
}

:global(html.dark) .dist-item__track {
  background: #313b4b;
}

:global(html.dark) .rank-item {
  background: #131922;
}
</style>

