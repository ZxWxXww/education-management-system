<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { DataAnalysis, Histogram, RefreshRight, Search, WarningFilled } from '@element-plus/icons-vue'
import { fetchTeacherScorePage } from '../../api/teacher/score'

const queryForm = ref({
  className: '全部班级',
  subject: '',
  examType: '',
  keyword: ''
})

const loading = ref(false)
const rawScoreList = ref([])

function inferExamType(examName) {
  if (!examName) return '考试'
  if (examName.includes('月考')) return '月考'
  if (examName.includes('周测')) return '周测'
  if (examName.includes('单元')) return '单元测'
  if (examName.includes('阶段')) return '阶段测'
  if (examName.includes('模拟')) return '模拟考'
  return '考试'
}

const classOptions = computed(() => ['全部班级', ...new Set(rawScoreList.value.map((item) => item.className).filter(Boolean))])
const subjectOptions = computed(() => [...new Set(rawScoreList.value.map((item) => item.courseName).filter(Boolean))])
const examTypeOptions = computed(() => [...new Set(rawScoreList.value.map((item) => inferExamType(item.examName)).filter(Boolean))])

const filteredScoreList = computed(() => {
  const keyword = queryForm.value.keyword.trim()
  return rawScoreList.value.filter((item) => {
    const examType = inferExamType(item.examName)
    const matchKeyword =
      !keyword ||
      [item.examName, item.className, item.studentName, item.courseName]
        .filter(Boolean)
        .some((value) => String(value).includes(keyword))
    const matchClass = queryForm.value.className === '全部班级' || item.className === queryForm.value.className
    const matchSubject = !queryForm.value.subject || item.courseName === queryForm.value.subject
    const matchExamType = !queryForm.value.examType || examType === queryForm.value.examType
    return matchKeyword && matchClass && matchSubject && matchExamType
  })
})

const distributionData = computed(() => {
  const total = filteredScoreList.value.length || 1
  const sections = [
    { label: '90-100', matcher: (score) => score >= 90, tone: 'excellent' },
    { label: '80-89', matcher: (score) => score >= 80 && score < 90, tone: 'good' },
    { label: '60-79', matcher: (score) => score >= 60 && score < 80, tone: 'pass' },
    { label: '0-59', matcher: (score) => score < 60, tone: 'risk' }
  ]

  return sections.map((item) => ({
    ...item,
    count: filteredScoreList.value.filter((row) => item.matcher(Number(row.score || 0))).length,
    percent: Math.round((filteredScoreList.value.filter((row) => item.matcher(Number(row.score || 0))).length / total) * 100)
  }))
})

const examOverviewList = computed(() => {
  const overviewMap = filteredScoreList.value.reduce((acc, item) => {
    const key = `${item.examId || item.examName}-${item.className}`
    if (!acc[key]) {
      acc[key] = {
        id: key,
        examName: item.examName,
        className: item.className,
        subject: item.courseName || '未分类',
        examType: inferExamType(item.examName),
        totalScore: 0,
        recordCount: 0,
        excellentCount: 0,
        passCount: 0,
        updatedAt: item.updatedAt || '-'
      }
    }

    acc[key].totalScore += Number(item.score || 0)
    acc[key].recordCount += 1
    if (Number(item.score || 0) >= 90) acc[key].excellentCount += 1
    if (Number(item.score || 0) >= 60) acc[key].passCount += 1
    if ((item.updatedAt || '') > acc[key].updatedAt) {
      acc[key].updatedAt = item.updatedAt || '-'
    }
    return acc
  }, {})

  return Object.values(overviewMap)
    .map((item) => ({
      id: item.id,
      examName: item.examName,
      className: item.className,
      subject: item.subject,
      examType: item.examType,
      avgScore: Number((item.totalScore / (item.recordCount || 1)).toFixed(1)),
      excellentRate: Number(((item.excellentCount / (item.recordCount || 1)) * 100).toFixed(1)),
      passRate: Number(((item.passCount / (item.recordCount || 1)) * 100).toFixed(1)),
      recordCount: item.recordCount,
      updatedAt: item.updatedAt
    }))
    .sort((a, b) => String(b.updatedAt || '').localeCompare(String(a.updatedAt || '')))
})

const summaryCards = computed(() => {
  const records = filteredScoreList.value
  const total = records.length || 1
  const avg = (records.reduce((sum, item) => sum + Number(item.score || 0), 0) / total).toFixed(1)
  const excellent = ((records.filter((item) => Number(item.score || 0) >= 90).length / total) * 100).toFixed(1)
  const pass = ((records.filter((item) => Number(item.score || 0) >= 60).length / total) * 100).toFixed(1)

  return [
    { label: '平均成绩', value: `${avg}`, desc: '当前筛选成绩记录' },
    { label: '优秀率', value: `${excellent}%`, desc: '90 分及以上' },
    { label: '及格率', value: `${pass}%`, desc: '60 分及以上' },
    { label: '成绩记录数', value: `${records.length}`, desc: '全部来自真实成绩接口' }
  ]
})

const classAverageRows = computed(() => {
  const grouped = filteredScoreList.value.reduce((acc, item) => {
    if (!item.className) return acc
    if (!acc[item.className]) {
      acc[item.className] = {
        className: item.className,
        totalScore: 0,
        recordCount: 0
      }
    }
    acc[item.className].totalScore += Number(item.score || 0)
    acc[item.className].recordCount += 1
    return acc
  }, {})

  return Object.values(grouped)
    .map((item) => ({
      className: item.className,
      averageScore: Number((item.totalScore / (item.recordCount || 1)).toFixed(1)),
      recordCount: item.recordCount
    }))
    .sort((a, b) => b.averageScore - a.averageScore)
})

const classAverageBars = computed(() =>
  classAverageRows.value.slice(0, 6).map((item, index) => ({
    ...item,
    percent: Math.max(0, Math.min(100, item.averageScore)),
    tone: index === 0 ? 'top' : index === 1 ? 'mid' : 'base'
  }))
)

const lowScoreStudents = computed(() =>
  [...filteredScoreList.value]
    .sort((a, b) => Number(a.score || 0) - Number(b.score || 0))
    .slice(0, 10)
    .map((item) => ({
      id: item.id,
      studentName: item.studentName,
      className: item.className,
      courseName: item.courseName || '-',
      examName: item.examName || '-',
      score: item.score,
      rankInClass: item.rankInClass || '-',
      updatedAt: item.updatedAt || '-'
    }))
)

function resetQuery() {
  queryForm.value.className = '全部班级'
  queryForm.value.subject = ''
  queryForm.value.examType = ''
  queryForm.value.keyword = ''
}

async function loadTeacherScoreAnalysis() {
  loading.value = true
  try {
    const page = await fetchTeacherScorePage({ pageNum: 1, pageSize: 200 })
    rawScoreList.value = page.list
  } catch (error) {
    ElMessage.error(error.message || '成绩分析加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadTeacherScoreAnalysis()
})
</script>

<template>
  <div class="grade-analysis">
    <section class="hero">
      <div>
        <p class="hero__path">教学数据 / 成绩分析</p>
        <h1 class="hero__title">成绩分析</h1>
        <p class="hero__desc">只基于真实成绩记录汇总班级均分、分数段分布和低分学生名单</p>
      </div>
    </section>

    <section class="filter-panel">
      <el-input v-model="queryForm.keyword" placeholder="搜索考试 / 班级 / 学生 / 科目" clearable>
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="queryForm.className">
        <el-option v-for="item in classOptions" :key="item" :label="item" :value="item" />
      </el-select>
      <el-select v-model="queryForm.subject" clearable placeholder="全部科目">
        <el-option v-for="item in subjectOptions" :key="item" :label="item" :value="item" />
      </el-select>
      <el-select v-model="queryForm.examType" clearable placeholder="全部考试类型">
        <el-option v-for="item in examTypeOptions" :key="item" :label="item" :value="item" />
      </el-select>
      <el-button class="filter-btn" type="primary" @click="loadTeacherScoreAnalysis">
        <el-icon><DataAnalysis /></el-icon>
        <span>刷新</span>
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
      <article v-loading="loading" class="panel">
        <header class="panel__head">
          <h2><el-icon><DataAnalysis /></el-icon> 班级平均分对比</h2>
          <span>{{ classAverageBars.length }} 个班级</span>
        </header>
        <div v-if="classAverageBars.length" class="comparison-list">
          <div v-for="item in classAverageBars" :key="item.className" class="comparison-item">
            <div class="comparison-item__head">
              <span>{{ item.className }}</span>
              <strong>{{ item.averageScore }}</strong>
            </div>
            <div class="comparison-item__track">
              <div class="comparison-item__bar" :class="`comparison-item__bar--${item.tone}`" :style="{ width: `${item.percent}%` }"></div>
            </div>
            <p class="comparison-item__desc">共 {{ item.recordCount }} 条成绩记录</p>
          </div>
        </div>
        <el-empty v-else description="当前筛选下暂无班级成绩数据" :image-size="72" />
      </article>

      <article v-loading="loading" class="panel">
        <header class="panel__head panel__head--single">
          <h2><el-icon><Histogram /></el-icon> 分数段分布</h2>
        </header>
        <div v-if="distributionData.length" class="dist-list">
          <div v-for="item in distributionData" :key="item.label" class="dist-item">
            <div class="dist-item__head">
              <span>{{ item.label }}</span>
              <span>{{ item.count }} 条 / {{ item.percent }}%</span>
            </div>
            <div class="dist-item__track">
              <div class="dist-item__bar" :class="`dist-item__bar--${item.tone}`" :style="{ width: `${item.percent}%` }"></div>
            </div>
          </div>
        </div>
      </article>
    </section>

    <section v-loading="loading" class="panel">
      <header class="panel__head">
        <h2><el-icon><WarningFilled /></el-icon> 低分学生名单</h2>
        <span>{{ lowScoreStudents.length }} 条</span>
      </header>
      <el-table :data="lowScoreStudents" stripe>
        <el-table-column prop="studentName" label="学生姓名" width="110" />
        <el-table-column prop="className" label="班级" width="140" />
        <el-table-column prop="courseName" label="科目" width="120" />
        <el-table-column prop="examName" label="考试名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="score" label="成绩" width="100" />
        <el-table-column prop="rankInClass" label="班级排名" width="100" />
        <el-table-column prop="updatedAt" label="最近更新时间" min-width="170" />
      </el-table>
    </section>

    <section v-loading="loading" class="panel">
      <header class="panel__head panel__head--single">
        <h2><el-icon><DataAnalysis /></el-icon> 班级成绩概览</h2>
      </header>
      <el-table :data="examOverviewList" stripe>
        <el-table-column prop="id" label="分析编号" min-width="180" />
        <el-table-column prop="examName" label="考试名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="className" label="班级" width="140" />
        <el-table-column prop="subject" label="科目" width="100" />
        <el-table-column prop="examType" label="考试类型" width="110" />
        <el-table-column label="平均分" width="100">
          <template #default="{ row }">{{ row.avgScore }}</template>
        </el-table-column>
        <el-table-column label="优秀率" width="100">
          <template #default="{ row }">{{ row.excellentRate }}%</template>
        </el-table-column>
        <el-table-column label="及格率" width="100">
          <template #default="{ row }">{{ row.passRate }}%</template>
        </el-table-column>
        <el-table-column prop="recordCount" label="记录数" width="90" />
        <el-table-column prop="updatedAt" label="最近更新时间" min-width="170" />
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

.filter-panel {
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  padding: 14px;
  display: grid;
  gap: 10px;
  grid-template-columns: 1.8fr repeat(3, minmax(120px, 1fr)) auto auto;
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
  grid-template-columns: minmax(0, 1.2fr) minmax(0, 0.8fr);
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

.panel__head span {
  color: #6b7280;
  font-size: 12px;
}

.comparison-list,
.dist-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comparison-item__head,
.dist-item__head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  color: #374151;
  font-size: 13px;
}

.comparison-item__head strong {
  color: #111827;
}

.comparison-item__track,
.dist-item__track {
  margin-top: 6px;
  height: 8px;
  border-radius: 999px;
  background: #e5e7eb;
  overflow: hidden;
}

.comparison-item__bar,
.dist-item__bar {
  height: 100%;
  border-radius: 999px;
}

.comparison-item__bar--top {
  background: linear-gradient(90deg, #16a34a 0%, #4ade80 100%);
}

.comparison-item__bar--mid {
  background: linear-gradient(90deg, #2563eb 0%, #60a5fa 100%);
}

.comparison-item__bar--base {
  background: linear-gradient(90deg, #0f766e 0%, #2dd4bf 100%);
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

.comparison-item__desc {
  margin: 6px 0 0;
  color: #6b7280;
  font-size: 12px;
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
  .summary-grid,
  .filter-panel {
    grid-template-columns: 1fr;
  }
}

:global(html.dark) .filter-panel,
:global(html.dark) .summary-card,
:global(html.dark) .panel {
  background: #1a2028;
  border-color: #2b3442;
}

:global(html.dark) .summary-card__value,
:global(html.dark) .panel__head h2,
:global(html.dark) .comparison-item__head strong {
  color: #e5eaf3;
}

:global(html.dark) .summary-card__label,
:global(html.dark) .summary-card__desc,
:global(html.dark) .panel__head span,
:global(html.dark) .comparison-item__head,
:global(html.dark) .comparison-item__desc,
:global(html.dark) .dist-item__head {
  color: #9ba7ba;
}

:global(html.dark) .comparison-item__track,
:global(html.dark) .dist-item__track {
  background: #313b4b;
}
</style>
