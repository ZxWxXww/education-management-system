<script setup>
import { computed, ref } from 'vue'
import {
  DataAnalysis,
  DocumentChecked,
  Histogram,
  RefreshRight,
  Search,
  TrendCharts
} from '@element-plus/icons-vue'
import PageShell from '../../components/PageShell.vue'
import StatisticCard from '../../components/common/StatisticCard.vue'

// 学生端“学习统计-成绩分析”Mock 数据
// 切换真实后端时，请替换为 src/api/student/gradeAnalysis.js 接口返回
const gradeAnalysisMock = ref({
  profile: {
    studentNo: 'S20260318',
    studentName: '李明',
    gradeClass: '高二（3）班',
    semester: '2025-2026 学年第二学期',
    homeroomTeacher: '张老师',
    created_at: '2026-02-18 09:20:00',
    updated_at: '2026-04-06 11:30:00'
  },
  summary: {
    overallAverage: 88.7,
    classRanking: 8,
    bestSubject: '数学',
    scoreGrowthRate: 5.4,
    created_at: '2026-04-06 11:30:00',
    updated_at: '2026-04-06 11:30:00'
  },
  subjectStats: [
    { subject: '语文', averageScore: 86.2, classAverage: 82.5, created_at: '2026-04-06 11:30:00', updated_at: '2026-04-06 11:30:00' },
    { subject: '数学', averageScore: 93.4, classAverage: 84.1, created_at: '2026-04-06 11:30:00', updated_at: '2026-04-06 11:30:00' },
    { subject: '英语', averageScore: 88.9, classAverage: 83.6, created_at: '2026-04-06 11:30:00', updated_at: '2026-04-06 11:30:00' },
    { subject: '物理', averageScore: 84.6, classAverage: 80.3, created_at: '2026-04-06 11:30:00', updated_at: '2026-04-06 11:30:00' },
    { subject: '化学', averageScore: 90.1, classAverage: 82.8, created_at: '2026-04-06 11:30:00', updated_at: '2026-04-06 11:30:00' },
    { subject: '生物', averageScore: 89.4, classAverage: 83.1, created_at: '2026-04-06 11:30:00', updated_at: '2026-04-06 11:30:00' }
  ],
  examRecords: [
    {
      id: 1,
      examName: '三月月考',
      examType: '月考',
      examDate: '2026-03-26',
      totalScore: 525,
      classRank: 12,
      gradeRank: 58,
      growthValue: 0,
      created_at: '2026-03-26 18:30:00',
      updated_at: '2026-03-26 18:30:00'
    },
    {
      id: 2,
      examName: '期中模拟',
      examType: '模拟考',
      examDate: '2026-04-02',
      totalScore: 541,
      classRank: 9,
      gradeRank: 43,
      growthValue: 16,
      created_at: '2026-04-02 17:50:00',
      updated_at: '2026-04-02 17:50:00'
    },
    {
      id: 3,
      examName: '四月阶段测',
      examType: '阶段测',
      examDate: '2026-04-05',
      totalScore: 552,
      classRank: 8,
      gradeRank: 39,
      growthValue: 11,
      created_at: '2026-04-05 20:15:00',
      updated_at: '2026-04-05 20:15:00'
    }
  ]
})

const queryForm = ref({
  examType: '',
  keyword: ''
})

const lineChartWidth = 600
const lineChartHeight = 220
const chartPadding = 22

const summaryCards = computed(() => [
  {
    key: 'overallAverage',
    title: '综合均分',
    value: `${gradeAnalysisMock.value.summary.overallAverage}`,
    desc: '最近 3 次考试',
    icon: DataAnalysis,
    tone: 'blue'
  },
  {
    key: 'classRanking',
    title: '班级排名',
    value: `第 ${gradeAnalysisMock.value.summary.classRanking} 名`,
    desc: '当前学期',
    icon: DocumentChecked,
    tone: 'green'
  },
  {
    key: 'bestSubject',
    title: '优势学科',
    value: gradeAnalysisMock.value.summary.bestSubject,
    desc: '按最近一次考试',
    icon: Histogram,
    tone: 'purple'
  },
  {
    key: 'scoreGrowthRate',
    title: '分数增长率',
    value: `+${gradeAnalysisMock.value.summary.scoreGrowthRate}%`,
    desc: '较上阶段',
    icon: TrendCharts,
    tone: 'orange'
  }
])

const examTypeOptions = computed(() => [...new Set(gradeAnalysisMock.value.examRecords.map((item) => item.examType))])

const filteredRecords = computed(() => {
  const keyword = queryForm.value.keyword.trim()
  return gradeAnalysisMock.value.examRecords.filter((item) => {
    const matchType = queryForm.value.examType ? item.examType === queryForm.value.examType : true
    const matchKeyword = keyword ? item.examName.includes(keyword) : true
    return matchType && matchKeyword
  })
})

const scoreTrendPoints = computed(() => {
  const source = filteredRecords.value.length > 0 ? filteredRecords.value : gradeAnalysisMock.value.examRecords
  return buildLinePoints(source.map((item) => item.totalScore))
})

const chartLabels = computed(() => {
  const source = filteredRecords.value.length > 0 ? filteredRecords.value : gradeAnalysisMock.value.examRecords
  return source.map((item) => item.examName)
})

function buildLinePoints(values) {
  if (values.length === 1) {
    const centerX = lineChartWidth / 2
    const y = lineChartHeight / 2
    return `${centerX},${y}`
  }
  const max = Math.max(...values)
  const min = Math.min(...values)
  const span = max - min || 1
  return values
    .map((value, index) => {
      const x = (index / (values.length - 1)) * lineChartWidth
      const y = lineChartHeight - ((value - min) / span) * (lineChartHeight - chartPadding * 2) - chartPadding
      return `${x.toFixed(2)},${y.toFixed(2)}`
    })
    .join(' ')
}

function getGrowthTagType(value) {
  if (value > 0) return 'success'
  if (value === 0) return 'info'
  return 'danger'
}

function formatGrowthText(value) {
  if (value > 0) return `+${value}`
  return `${value}`
}

function resetFilters() {
  queryForm.value.examType = ''
  queryForm.value.keyword = ''
}
</script>

<template>
  <PageShell title="成绩分析" subtitle="学习统计 / 成绩分析">
    <div class="grade-analysis-page">
      <el-card shadow="never" class="profile-card">
        <div class="profile-title">
          {{ gradeAnalysisMock.profile.studentName }}，这是你本学期的成绩分析结果
        </div>
        <div class="profile-meta">
          学号：{{ gradeAnalysisMock.profile.studentNo }} ｜ 班级：{{ gradeAnalysisMock.profile.gradeClass }} ｜ 学期：{{ gradeAnalysisMock.profile.semester }}
        </div>
        <div class="profile-meta">
          班主任：{{ gradeAnalysisMock.profile.homeroomTeacher }} ｜ 最近更新时间：{{ gradeAnalysisMock.profile.updated_at }}
        </div>
      </el-card>

      <el-row :gutter="16">
        <el-col v-for="card in summaryCards" :key="card.key" :xs="24" :sm="12" :md="12" :lg="6" :xl="6">
          <StatisticCard
            :title="card.title"
            :value="card.value"
            :desc="card.desc"
            :icon="card.icon"
            :tone="card.tone"
          />
        </el-col>
      </el-row>

      <el-card shadow="never" class="filter-card">
        <el-form :inline="true" :model="queryForm" class="query-form">
          <el-form-item label="考试类型">
            <el-select v-model="queryForm.examType" clearable placeholder="全部类型" style="width: 160px">
              <el-option v-for="item in examTypeOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="考试名称">
            <el-input v-model="queryForm.keyword" placeholder="请输入关键词" clearable style="width: 220px">
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button @click="resetFilters">
              <el-icon><RefreshRight /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-row :gutter="16">
        <el-col :xs="24" :sm="24" :md="24" :lg="14" :xl="14">
          <el-card shadow="never" class="panel-card">
            <template #header>
              <div class="panel-header">总分趋势</div>
            </template>
            <div class="line-chart">
              <svg viewBox="0 0 600 220" preserveAspectRatio="none" aria-hidden="true">
                <defs>
                  <linearGradient id="scoreTrendArea" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stop-color="#409eff" stop-opacity="0.28" />
                    <stop offset="100%" stop-color="#409eff" stop-opacity="0" />
                  </linearGradient>
                </defs>
                <polygon :points="`0,220 ${scoreTrendPoints} 600,220`" fill="url(#scoreTrendArea)" />
                <polyline
                  :points="scoreTrendPoints"
                  fill="none"
                  stroke="#409eff"
                  stroke-width="4"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              <div class="line-axis">
                <span v-for="label in chartLabels" :key="label">{{ label }}</span>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="24" :md="24" :lg="10" :xl="10">
          <el-card shadow="never" class="panel-card">
            <template #header>
              <div class="panel-header">学科对比</div>
            </template>
            <div class="subject-list">
              <div v-for="item in gradeAnalysisMock.subjectStats" :key="item.subject" class="subject-item">
                <div class="subject-head">
                  <span>{{ item.subject }}</span>
                  <span>{{ item.averageScore }} / 班均 {{ item.classAverage }}</span>
                </div>
                <el-progress :percentage="item.averageScore" :stroke-width="10" :show-text="false" />
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" class="panel-card">
        <template #header>
          <div class="panel-header">考试记录</div>
        </template>
        <el-table :data="filteredRecords" stripe>
          <el-table-column prop="examName" label="考试名称" min-width="160" />
          <el-table-column prop="examType" label="类型" width="100" />
          <el-table-column prop="examDate" label="考试日期" width="120" />
          <el-table-column prop="totalScore" label="总分" width="100" />
          <el-table-column prop="classRank" label="班级排名" width="100" />
          <el-table-column prop="gradeRank" label="年级排名" width="100" />
          <el-table-column label="分数波动" width="110">
            <template #default="{ row }">
              <el-tag :type="getGrowthTagType(row.growthValue)" effect="plain">
                {{ formatGrowthText(row.growthValue) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="created_at" label="created_at" min-width="170" />
          <el-table-column prop="updated_at" label="updated_at" min-width="170" />
        </el-table>
      </el-card>
    </div>
  </PageShell>
</template>

<style scoped>
.grade-analysis-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.profile-card {
  border-radius: 12px;
}

.profile-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.profile-meta {
  margin-top: 10px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.filter-card,
.panel-card {
  border-radius: 12px;
}

.query-form {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 12px;
}

.panel-header {
  font-size: 15px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.line-chart {
  position: relative;
  height: 220px;
  border-radius: 10px;
  overflow: hidden;
  background: linear-gradient(180deg, rgba(64, 158, 255, 0.13) 0%, rgba(64, 158, 255, 0.02) 100%);
}

.line-chart svg {
  width: 100%;
  height: 100%;
}

.line-axis {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 6px;
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: 1fr;
  text-align: center;
  padding: 0 10px;
  font-size: 12px;
  color: #6b7280;
}

.subject-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.subject-item {
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid var(--el-border-color-lighter);
}

.subject-head {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

@media (max-width: 768px) {
  .profile-title {
    font-size: 18px;
  }
}

:global(html.dark) .line-chart {
  background: linear-gradient(180deg, rgba(64, 158, 255, 0.2) 0%, rgba(64, 158, 255, 0.04) 100%);
}

:global(html.dark) .line-axis {
  color: #9ba7ba;
}

:global(html.dark) .subject-item {
  border-color: #2b3442;
  background: #1a2028;
}

:global(html.dark) .subject-head {
  color: #a7b3c4;
}
</style>

