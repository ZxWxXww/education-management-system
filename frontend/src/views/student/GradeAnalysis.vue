<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
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
import { fetchCurrentStudentProfile } from '../../api/student/profile'
import { fetchStudentScorePage } from '../../api/student/score'
import { SCORE_PUBLISH_STATUS } from '../../constants/status'

const gradeAnalysis = ref({
  profile: {
    studentNo: '-',
    studentName: '',
    gradeClass: '',
    updatedAt: '-'
  },
  summary: {
    overallAverage: 0,
    publishedExamCount: 0,
    bestSubject: '-',
    scoreGrowthRate: 0,
    latestRankInClass: '-'
  },
  subjectStats: [],
  examRecords: []
})
const loading = ref(false)

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
    value: `${gradeAnalysis.value.summary.overallAverage}`,
    desc: '基于真实成绩记录',
    icon: DataAnalysis,
    tone: 'blue'
  },
  {
    key: 'publishedExamCount',
    title: '已发布考试',
    value: `${gradeAnalysis.value.summary.publishedExamCount} 次`,
    desc: '仅统计已发布记录',
    icon: DocumentChecked,
    tone: 'green'
  },
  {
    key: 'bestSubject',
    title: '优势学科',
    value: gradeAnalysis.value.summary.bestSubject,
    desc: '按学科均分计算',
    icon: Histogram,
    tone: 'purple'
  },
  {
    key: 'scoreGrowthRate',
    title: '分数增长率',
    value: `${gradeAnalysis.value.summary.scoreGrowthRate > 0 ? '+' : ''}${gradeAnalysis.value.summary.scoreGrowthRate}%`,
    desc: `最近排名 ${gradeAnalysis.value.summary.latestRankInClass}`,
    icon: TrendCharts,
    tone: 'orange'
  }
])

const examTypeOptions = computed(() => [...new Set(gradeAnalysis.value.examRecords.map((item) => item.examType))])

const filteredRecords = computed(() => {
  const keyword = queryForm.value.keyword.trim()
  return gradeAnalysis.value.examRecords.filter((item) => {
    const matchType = queryForm.value.examType ? item.examType === queryForm.value.examType : true
    const matchKeyword = keyword ? item.examName.includes(keyword) : true
    return matchType && matchKeyword
  })
})

const scoreTrendPoints = computed(() => {
  const source = filteredRecords.value.length > 0 ? filteredRecords.value : gradeAnalysis.value.examRecords
  return buildLinePoints(source.map((item) => item.totalScore))
})

const chartLabels = computed(() => {
  const source = filteredRecords.value.length > 0 ? filteredRecords.value : gradeAnalysis.value.examRecords
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

function inferExamType(examName) {
  if (!examName) return '考试'
  if (examName.includes('月考')) return '月考'
  if (examName.includes('模拟')) return '模拟考'
  if (examName.includes('阶段')) return '阶段测'
  return '考试'
}

async function loadGradeAnalysis() {
  loading.value = true
  try {
    const [profile, page] = await Promise.all([
      fetchCurrentStudentProfile(),
      fetchStudentScorePage({ pageNum: 1, pageSize: 200 })
    ])
    const records = page.list
    const groupedExam = Object.values(records.reduce((acc, item) => {
      const key = item.examName || item.examId || item.id
      if (!acc[key]) {
        acc[key] = {
          id: item.examId || item.id,
          examName: item.examName,
          examType: inferExamType(item.examName),
          examDate: (item.examTime || item.publishTime || '').slice(0, 10),
          totalScore: 0,
          classRank: item.rankInClass || '-',
          publishStatus: item.examStatusLabel,
          growthValue: 0,
          createdAt: item.createdAt,
          updatedAt: item.updatedAt
        }
      }
      acc[key].totalScore += Number(item.score || 0)
      return acc
    }, {}))
      .sort((a, b) => String(a.examDate).localeCompare(String(b.examDate)))
      .map((item, index, arr) => ({
        ...item,
        growthValue: index === 0 ? 0 : Number((item.totalScore - arr[index - 1].totalScore).toFixed(1))
      }))

    const subjectStats = records.reduce((acc, item) => {
      const key = item.courseName || '未分类'
      if (!acc[key]) {
        acc[key] = { subject: key, averageScore: 0, count: 0 }
      }
      acc[key].averageScore += Number(item.score || 0)
      acc[key].count += 1
      return acc
    }, {})
    const subjectList = Object.values(subjectStats).map((item) => ({
      subject: item.subject,
      averageScore: Number((item.averageScore / (item.count || 1)).toFixed(1))
    }))
    const overallAverage = records.length
      ? Number((records.reduce((sum, item) => sum + Number(item.score || 0), 0) / records.length).toFixed(1))
      : 0
    const bestSubject = [...subjectList].sort((a, b) => b.averageScore - a.averageScore)[0]?.subject || '-'
    gradeAnalysis.value = {
      profile: {
        studentNo: profile.studentNo || '-',
        studentName: profile.name || '',
        gradeClass: profile.className || '',
        updatedAt: profile.updatedAt || '-'
      },
      summary: {
        overallAverage,
        publishedExamCount: new Set(records.filter((item) => item.examStatusCode === SCORE_PUBLISH_STATUS.PUBLISHED).map((item) => item.examName)).size,
        bestSubject,
        scoreGrowthRate: groupedExam.length > 1 && groupedExam[0].totalScore
          ? Number((((groupedExam[groupedExam.length - 1].totalScore - groupedExam[0].totalScore) / groupedExam[0].totalScore) * 100).toFixed(1))
          : 0,
        latestRankInClass: records[0]?.rankInClass ? `第 ${records[0].rankInClass} 名` : '-'
      },
      subjectStats: subjectList,
      examRecords: groupedExam
    }
  } catch (error) {
    ElMessage.error('成绩分析加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadGradeAnalysis()
})
</script>

<template>
  <PageShell title="成绩分析" subtitle="学习统计 / 成绩分析">
    <div class="grade-analysis-page">
      <el-card v-loading="loading" shadow="never" class="profile-card">
        <div class="profile-title">
          {{ gradeAnalysis.profile.studentName }}，这是你的成绩分析结果
        </div>
        <div class="profile-meta">
          学号：{{ gradeAnalysis.profile.studentNo }} ｜ 班级：{{ gradeAnalysis.profile.gradeClass }} ｜ 最近更新时间：{{ gradeAnalysis.profile.updatedAt }}
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
          <el-card v-loading="loading" shadow="never" class="panel-card">
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
          <el-card v-loading="loading" shadow="never" class="panel-card">
            <template #header>
              <div class="panel-header">学科对比</div>
            </template>
            <div class="subject-list">
              <div v-for="item in gradeAnalysis.subjectStats" :key="item.subject" class="subject-item">
                <div class="subject-head">
                  <span>{{ item.subject }}</span>
                  <span>{{ item.averageScore }} 分</span>
                </div>
                <el-progress :percentage="item.averageScore" :stroke-width="10" :show-text="false" />
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card v-loading="loading" shadow="never" class="panel-card">
        <template #header>
          <div class="panel-header">考试记录</div>
        </template>
        <el-table :data="filteredRecords" stripe>
          <el-table-column prop="examName" label="考试名称" min-width="160" />
          <el-table-column prop="examType" label="类型" width="100" />
          <el-table-column prop="examDate" label="考试日期" width="120" />
          <el-table-column prop="totalScore" label="总分" width="100" />
          <el-table-column prop="classRank" label="班级排名" width="100" />
          <el-table-column prop="publishStatus" label="发布状态" width="100" />
          <el-table-column label="分数波动" width="110">
            <template #default="{ row }">
              <el-tag :type="getGrowthTagType(row.growthValue)" effect="plain">
                {{ formatGrowthText(row.growthValue) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" min-width="170" />
          <el-table-column prop="updatedAt" label="更新时间" min-width="170" />
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

