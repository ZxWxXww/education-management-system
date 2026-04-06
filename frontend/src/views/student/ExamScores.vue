<script setup>
import { computed, ref } from 'vue'
import { DataAnalysis, DocumentChecked, Histogram, Notebook } from '@element-plus/icons-vue'
import PageShell from '../../components/PageShell.vue'

// 学生端“我的作业-考试成绩”Mock 数据
// 切换真实后端时，请替换为 src/api/student/examScores.js 的接口返回
const examScoresMock = ref({
  profile: {
    studentNo: 'S20260318',
    studentName: '李明',
    gradeClass: '高二（3）班',
    semester: '2025-2026 学年第二学期',
    created_at: '2026-02-18 09:20:00',
    updated_at: '2026-04-06 09:10:00'
  },
  summary: {
    examCount: 8,
    averageScore: 88.6,
    highestScore: 97,
    passRate: 100,
    created_at: '2026-04-06 09:10:00',
    updated_at: '2026-04-06 09:10:00'
  },
  records: [
    {
      id: 1,
      examName: '三月月考',
      courseName: '数学',
      score: 92,
      fullScore: 100,
      rankInClass: 6,
      rankTrend: 'up',
      publishTime: '2026-03-26 18:20:00',
      teacherComment: '函数综合题思路清晰，压轴题计算可再优化。',
      created_at: '2026-03-26 18:20:00',
      updated_at: '2026-03-26 18:20:00'
    },
    {
      id: 2,
      examName: '三月月考',
      courseName: '英语',
      score: 89,
      fullScore: 100,
      rankInClass: 8,
      rankTrend: 'stable',
      publishTime: '2026-03-26 18:30:00',
      teacherComment: '阅读理解保持稳定，作文可以增加高级句式。',
      created_at: '2026-03-26 18:30:00',
      updated_at: '2026-03-26 18:30:00'
    },
    {
      id: 3,
      examName: '三月月考',
      courseName: '物理',
      score: 84,
      fullScore: 100,
      rankInClass: 13,
      rankTrend: 'down',
      publishTime: '2026-03-26 18:35:00',
      teacherComment: '电磁感应综合题丢分较多，建议加强错题复盘。',
      created_at: '2026-03-26 18:35:00',
      updated_at: '2026-03-26 18:35:00'
    },
    {
      id: 4,
      examName: '期中模拟',
      courseName: '化学',
      score: 91,
      fullScore: 100,
      rankInClass: 7,
      rankTrend: 'up',
      publishTime: '2026-04-02 17:10:00',
      teacherComment: '有机推断题完成度高，注意规范书写。',
      created_at: '2026-04-02 17:10:00',
      updated_at: '2026-04-02 17:10:00'
    },
    {
      id: 5,
      examName: '期中模拟',
      courseName: '语文',
      score: 87,
      fullScore: 100,
      rankInClass: 11,
      rankTrend: 'stable',
      publishTime: '2026-04-02 17:30:00',
      teacherComment: '现代文阅读表现较好，古诗文默写需加强。',
      created_at: '2026-04-02 17:30:00',
      updated_at: '2026-04-02 17:30:00'
    },
    {
      id: 6,
      examName: '期中模拟',
      courseName: '生物',
      score: 90,
      fullScore: 100,
      rankInClass: 9,
      rankTrend: 'up',
      publishTime: '2026-04-02 17:40:00',
      teacherComment: '知识点掌握全面，图表分析速度再提升。',
      created_at: '2026-04-02 17:40:00',
      updated_at: '2026-04-02 17:40:00'
    }
  ]
})

const queryForm = ref({
  examName: '',
  courseName: ''
})

const detailVisible = ref(false)
const currentRecord = ref(null)

const examOptions = computed(() => [...new Set(examScoresMock.value.records.map((item) => item.examName))])
const courseOptions = computed(() => [...new Set(examScoresMock.value.records.map((item) => item.courseName))])

const summaryCards = computed(() => [
  { key: 'examCount', title: '已发布考试', value: `${examScoresMock.value.summary.examCount} 次`, icon: Notebook, tone: 'blue' },
  { key: 'averageScore', title: '综合均分', value: `${examScoresMock.value.summary.averageScore}`, icon: DataAnalysis, tone: 'green' },
  { key: 'highestScore', title: '最高单科', value: `${examScoresMock.value.summary.highestScore}`, icon: Histogram, tone: 'purple' },
  { key: 'passRate', title: '及格率', value: `${examScoresMock.value.summary.passRate}%`, icon: DocumentChecked, tone: 'orange' }
])

const filteredRecords = computed(() =>
  examScoresMock.value.records.filter((item) => {
    const matchExam = queryForm.value.examName ? item.examName === queryForm.value.examName : true
    const matchCourse = queryForm.value.courseName ? item.courseName === queryForm.value.courseName : true
    return matchExam && matchCourse
  })
)

const trendSummary = computed(() => {
  const upCount = filteredRecords.value.filter((item) => item.rankTrend === 'up').length
  const stableCount = filteredRecords.value.filter((item) => item.rankTrend === 'stable').length
  const downCount = filteredRecords.value.filter((item) => item.rankTrend === 'down').length
  const total = filteredRecords.value.length || 1
  return {
    up: Math.round((upCount / total) * 100),
    stable: Math.round((stableCount / total) * 100),
    down: Math.round((downCount / total) * 100)
  }
})

function getScoreTagType(score) {
  if (score >= 90) return 'success'
  if (score >= 80) return 'warning'
  return 'danger'
}

function getTrendTagType(trend) {
  if (trend === 'up') return 'success'
  if (trend === 'stable') return 'info'
  return 'danger'
}

function getTrendText(trend) {
  if (trend === 'up') return '上升'
  if (trend === 'stable') return '持平'
  return '下降'
}

function openDetail(record) {
  currentRecord.value = record
  detailVisible.value = true
}

function resetFilters() {
  queryForm.value.examName = ''
  queryForm.value.courseName = ''
}
</script>

<template>
  <PageShell title="考试成绩" subtitle="我的作业 / 考试成绩">
    <div class="exam-score-page">
      <el-card shadow="never" class="profile-card">
        <div class="profile-head">
          <div class="profile-title">
            {{ examScoresMock.profile.studentName }}，以下为你的考试成绩记录
          </div>
          <div class="profile-meta">
            学号：{{ examScoresMock.profile.studentNo }} ｜ 班级：{{ examScoresMock.profile.gradeClass }} ｜ 学期：{{ examScoresMock.profile.semester }}
          </div>
        </div>
        <div class="profile-time">
          最近更新时间：{{ examScoresMock.profile.updated_at }}
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
          <el-card shadow="never" class="table-card">
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
              <el-table-column label="排名趋势" min-width="90">
                <template #default="{ row }">
                  <el-tag effect="plain" :type="getTrendTagType(row.rankTrend)">
                    {{ getTrendText(row.rankTrend) }}
                  </el-tag>
                </template>
              </el-table-column>
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
              <div class="card-header">趋势统计</div>
            </template>
            <div class="trend-item">
              <div class="trend-label">排名上升</div>
              <el-progress :percentage="trendSummary.up" status="success" :stroke-width="12" />
            </div>
            <div class="trend-item">
              <div class="trend-label">排名持平</div>
              <el-progress :percentage="trendSummary.stable" :stroke-width="12" />
            </div>
            <div class="trend-item">
              <div class="trend-label">排名下降</div>
              <el-progress :percentage="trendSummary.down" status="exception" :stroke-width="12" />
            </div>
            <el-divider />
            <div class="tips-title">学习建议</div>
            <ul class="tips-list">
              <li>保持数学与化学优势学科的稳定输出。</li>
              <li>重点复盘物理错题，按知识点建立错题卡。</li>
              <li>英语写作每周至少完成 1 篇限时训练。</li>
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
            <span>第 {{ currentRecord.rankInClass }} 名（{{ getTrendText(currentRecord.rankTrend) }}）</span>
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
            <div>created_at：{{ currentRecord.created_at }}</div>
            <div>updated_at：{{ currentRecord.updated_at }}</div>
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

