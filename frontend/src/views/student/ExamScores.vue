<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { DataAnalysis, DocumentChecked, Histogram, Notebook } from '@element-plus/icons-vue'
import PageShell from '../../components/PageShell.vue'
import { fetchCurrentStudentProfile } from '../../api/student/profile'
import { fetchStudentScorePage } from '../../api/student/score'
import { SCORE_PUBLISH_STATUS } from '../../constants/status'

const scoreModule = ref({
  profile: {
    studentNo: '-',
    studentName: '',
    gradeClass: '',
    updatedAt: '-'
  },
  summary: {
    publishedExamCount: 0,
    averageScore: 0,
    highestScore: 0,
    passRate: 0
  },
  records: []
})
const loading = ref(false)

const queryForm = ref({
  examName: '',
  courseName: ''
})

const detailVisible = ref(false)
const currentRecord = ref(null)

const examOptions = computed(() => [...new Set(scoreModule.value.records.map((item) => item.examName))])
const courseOptions = computed(() => [...new Set(scoreModule.value.records.map((item) => item.courseName))])

const summaryCards = computed(() => [
  { key: 'publishedExamCount', title: '已发布考试', value: `${scoreModule.value.summary.publishedExamCount} 次`, icon: Notebook, tone: 'blue' },
  { key: 'averageScore', title: '综合均分', value: `${scoreModule.value.summary.averageScore}`, icon: DataAnalysis, tone: 'green' },
  { key: 'highestScore', title: '最高单科', value: `${scoreModule.value.summary.highestScore}`, icon: Histogram, tone: 'purple' },
  { key: 'passRate', title: '及格率', value: `${scoreModule.value.summary.passRate}%`, icon: DocumentChecked, tone: 'orange' }
])

const filteredRecords = computed(() =>
  scoreModule.value.records.filter((item) => {
    const matchExam = queryForm.value.examName ? item.examName === queryForm.value.examName : true
    const matchCourse = queryForm.value.courseName ? item.courseName === queryForm.value.courseName : true
    return matchExam && matchCourse
  })
)

const scoreBands = computed(() => {
  const source = filteredRecords.value
  const total = source.length || 1
  return {
    excellent: Math.round((source.filter((item) => Number(item.score || 0) >= 90).length / total) * 100),
    good: Math.round((source.filter((item) => Number(item.score || 0) >= 80 && Number(item.score || 0) < 90).length / total) * 100),
    pass: Math.round((source.filter((item) => Number(item.score || 0) >= 60 && Number(item.score || 0) < 80).length / total) * 100),
    fail: Math.round((source.filter((item) => Number(item.score || 0) < 60).length / total) * 100)
  }
})

function getScoreTagType(score) {
  if (score >= 90) return 'success'
  if (score >= 80) return 'warning'
  return 'danger'
}

function getPublishStatusType(status) {
  return status === SCORE_PUBLISH_STATUS.PUBLISHED ? 'success' : 'info'
}

function openDetail(record) {
  currentRecord.value = record
  detailVisible.value = true
}

function resetFilters() {
  queryForm.value.examName = ''
  queryForm.value.courseName = ''
}

async function loadScores() {
  loading.value = true
  try {
    const [profile, page] = await Promise.all([
      fetchCurrentStudentProfile(),
      fetchStudentScorePage({ pageNum: 1, pageSize: 200 })
    ])
    const records = page.list
    const averageScore = records.length
      ? Number((records.reduce((sum, item) => sum + Number(item.score || 0), 0) / records.length).toFixed(1))
      : 0
    const highestScore = records.length ? Math.max(...records.map((item) => Number(item.score || 0))) : 0
    const passRate = records.length
      ? Math.round((records.filter((item) => Number(item.score || 0) >= 60).length / records.length) * 100)
      : 0
    scoreModule.value = {
      profile: {
        studentNo: profile.studentNo || '-',
        studentName: profile.name || '',
        gradeClass: profile.className || '',
        updatedAt: profile.updatedAt || '-'
      },
      summary: {
        publishedExamCount: new Set(records.filter((item) => item.examStatusCode === SCORE_PUBLISH_STATUS.PUBLISHED).map((item) => item.examName)).size,
        averageScore,
        highestScore,
        passRate
      },
      records
    }
  } catch (error) {
    ElMessage.error('考试成绩加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadScores()
})
</script>

<template>
  <PageShell title="考试成绩" subtitle="我的作业 / 考试成绩">
    <div class="exam-score-page">
      <el-card v-loading="loading" shadow="never" class="profile-card">
        <div class="profile-head">
          <div class="profile-title">
            {{ scoreModule.profile.studentName }}，以下为你的考试成绩记录
          </div>
          <div class="profile-meta">
            学号：{{ scoreModule.profile.studentNo }} ｜ 班级：{{ scoreModule.profile.gradeClass }}
          </div>
        </div>
        <div class="profile-time">
          最近更新时间：{{ scoreModule.profile.updatedAt }}
        </div>
      </el-card>

      <el-row :gutter="16">
        <el-col v-for="card in summaryCards" :key="card.key" :xs="24" :sm="12" :md="12" :lg="6" :xl="6">
          <el-card shadow="hover" class="summary-card" :class="`tone-${card.tone}`">
            <div class="summary-head">
              <span class="summary-title">{{ card.title }}</span>
              <el-icon class="summary-icon"><component :is="card.icon" /></el-icon>
            </div>
            <div class="summary-value">{{ card.value }}</div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" class="filter-card">
        <el-form :inline="true" :model="queryForm" class="query-form">
          <el-form-item label="考试批次">
            <el-select v-model="queryForm.examName" placeholder="全部考试" clearable style="width: 190px">
              <el-option v-for="name in examOptions" :key="name" :label="name" :value="name" />
            </el-select>
          </el-form-item>
          <el-form-item label="课程">
            <el-select v-model="queryForm.courseName" placeholder="全部课程" clearable style="width: 160px">
              <el-option v-for="name in courseOptions" :key="name" :label="name" :value="name" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button @click="resetFilters">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-row :gutter="16">
        <el-col :xs="24" :sm="24" :md="24" :lg="16" :xl="16">
          <el-card v-loading="loading" shadow="never" class="table-card">
            <template #header>
              <div class="card-header">成绩明细</div>
            </template>
            <el-table :data="filteredRecords" stripe>
              <el-table-column type="index" label="#" width="58" />
              <el-table-column prop="examName" label="考试批次" min-width="120" />
              <el-table-column prop="courseName" label="课程" min-width="90" />
              <el-table-column label="分数" min-width="110">
                <template #default="{ row }">
                  <el-tag :type="getScoreTagType(row.score)">
                    {{ row.score }} / {{ row.fullScore }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="rankInClass" label="班级排名" min-width="90" />
              <el-table-column label="发布状态" min-width="90">
                <template #default="{ row }">
                  <el-tag effect="plain" :type="getPublishStatusType(row.examStatusCode)">
                    {{ row.examStatusLabel }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="examTime" label="考试时间" min-width="150" />
              <el-table-column prop="publishTime" label="发布时间" min-width="150" />
              <el-table-column label="操作" width="90" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="openDetail(row)">查看</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="24" :md="24" :lg="8" :xl="8">
          <el-card shadow="never" class="trend-card">
            <template #header>
              <div class="card-header">分数分布</div>
            </template>
            <div class="trend-item">
              <div class="trend-label">90 分及以上</div>
              <el-progress :percentage="scoreBands.excellent" status="success" :stroke-width="12" />
            </div>
            <div class="trend-item">
              <div class="trend-label">80-89 分</div>
              <el-progress :percentage="scoreBands.good" :stroke-width="12" />
            </div>
            <div class="trend-item">
              <div class="trend-label">60-79 分</div>
              <el-progress :percentage="scoreBands.pass" status="warning" :stroke-width="12" />
            </div>
            <div class="trend-item">
              <div class="trend-label">60 分以下</div>
              <el-progress :percentage="scoreBands.fail" status="exception" :stroke-width="12" />
            </div>
            <el-divider />
            <div class="tips-title">学习建议</div>
            <ul class="tips-list">
              <li>优先复盘未达 80 分科目，对照考试时间建立错题清单。</li>
              <li>关注未发布成绩状态，避免把草稿成绩当作最终结果。</li>
              <li>结合教师评语追踪最近一次考试的改进点。</li>
            </ul>
          </el-card>
        </el-col>
      </el-row>

      <el-drawer v-model="detailVisible" title="成绩详情" size="480px" append-to-body>
        <div v-if="currentRecord" class="detail-panel">
          <div class="detail-row">
            <span class="detail-label">考试批次</span>
            <span>{{ currentRecord.examName }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">课程</span>
            <span>{{ currentRecord.courseName }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">分数</span>
            <span>{{ currentRecord.score }} / {{ currentRecord.fullScore }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">班级排名</span>
            <span>{{ currentRecord.rankInClass ? `第 ${currentRecord.rankInClass} 名` : '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">发布状态</span>
            <el-tag :type="getPublishStatusType(currentRecord.examStatusCode)" effect="plain">
              {{ currentRecord.examStatusLabel }}
            </el-tag>
          </div>
          <div class="detail-row">
            <span class="detail-label">考试时间</span>
            <span>{{ currentRecord.examTime }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">发布时间</span>
            <span>{{ currentRecord.publishTime }}</span>
          </div>
          <div class="comment-block">
            <div class="detail-label">教师评语</div>
            <div class="comment-content">{{ currentRecord.teacherComment }}</div>
          </div>
          <div class="timestamps">
            <div>created_at：{{ currentRecord.createdAt }}</div>
            <div>updated_at：{{ currentRecord.updatedAt }}</div>
          </div>
        </div>
      </el-drawer>
    </div>
  </PageShell>
</template>

<style scoped>
.exam-score-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.profile-card,
.summary-card,
.filter-card,
.table-card,
.trend-card {
  border-radius: 12px;
}

.profile-head {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.profile-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.profile-meta,
.profile-time {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.profile-time {
  margin-top: 8px;
}

.summary-card {
  transition: transform 0.2s ease;
}

.summary-card:hover {
  transform: translateY(-2px);
}

.summary-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.summary-title {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.summary-icon {
  font-size: 20px;
}

.summary-value {
  margin-top: 12px;
  font-size: 28px;
  line-height: 1.2;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.tone-blue .summary-icon {
  color: #409eff;
}

.tone-green .summary-icon {
  color: #67c23a;
}

.tone-purple .summary-icon {
  color: #8a65ff;
}

.tone-orange .summary-icon {
  color: #e6a23c;
}

.query-form {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 12px;
}

.card-header {
  font-size: 15px;
  font-weight: 700;
}

.trend-item + .trend-item {
  margin-top: 14px;
}

.trend-label {
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.tips-title {
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 700;
}

.tips-list {
  margin: 0;
  padding-left: 18px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}

.detail-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 14px;
}

.detail-label {
  color: var(--el-text-color-secondary);
}

.comment-block {
  margin-top: 4px;
  padding: 12px;
  border-radius: 8px;
  background: var(--el-fill-color-light);
}

.comment-content {
  margin-top: 6px;
  line-height: 1.6;
}

.timestamps {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.6;
  color: var(--el-text-color-placeholder);
}

@media (max-width: 768px) {
  .profile-title {
    font-size: 18px;
  }

  .summary-value {
    font-size: 24px;
  }
}
</style>

