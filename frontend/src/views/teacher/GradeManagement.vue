<script setup>
import { computed, ref } from 'vue'
import { DataAnalysis, DocumentChecked, Download, Plus, RefreshRight, Search, Tickets } from '@element-plus/icons-vue'

// 教师端成绩管理 Mock 数据（接入后端时替换为 src/api/teacher/grade.js）
const searchForm = ref({
  keyword: '',
  className: '全部班级',
  scoreRange: '全部分段'
})

const classOptions = ['全部班级', '高二（3）班', '高二（6）班', '竞赛1班', '高一（2）班']
const scoreRangeOptions = ['全部分段', '90-100', '80-89', '60-79', '0-59']

const gradeList = ref([
  {
    gradeId: 'T-G-20260406-001',
    className: '高二（3）班',
    studentName: '张若琳',
    studentNo: 'S202402031',
    assignmentName: '函数与导数综合训练（第 4 讲）',
    score: 93,
    rank: 3,
    comment: '计算步骤完整',
    created_at: '2026-04-06 20:12:00',
    updated_at: '2026-04-06 21:05:00'
  },
  {
    gradeId: 'T-G-20260406-002',
    className: '高二（6）班',
    studentName: '刘子恒',
    studentNo: 'S202402066',
    assignmentName: '三角函数图像随堂练习',
    score: 86,
    rank: 9,
    comment: '审题准确，细节可提升',
    created_at: '2026-04-06 19:40:00',
    updated_at: '2026-04-06 20:14:00'
  },
  {
    gradeId: 'T-G-20260406-003',
    className: '竞赛1班',
    studentName: '宋明哲',
    studentNo: 'S202402108',
    assignmentName: '竞赛班不等式证明专题',
    score: 78,
    rank: 15,
    comment: '证明逻辑有待加强',
    created_at: '2026-04-06 20:25:00',
    updated_at: '2026-04-06 20:58:00'
  },
  {
    gradeId: 'T-G-20260406-004',
    className: '高一（2）班',
    studentName: '林语晨',
    studentNo: 'S202402142',
    assignmentName: '数列基础检测',
    score: 57,
    rank: 34,
    comment: '错题集中在通项求解',
    created_at: '2026-04-06 17:15:00',
    updated_at: '2026-04-06 18:43:00'
  }
])

const filteredGrades = computed(() =>
  gradeList.value.filter((row) => {
    const keyword = searchForm.value.keyword.trim()
    const matchKeyword =
      !keyword ||
      row.studentName.includes(keyword) ||
      row.studentNo.includes(keyword) ||
      row.assignmentName.includes(keyword)
    const matchClass = searchForm.value.className === '全部班级' || row.className === searchForm.value.className

    if (searchForm.value.scoreRange === '全部分段') return matchKeyword && matchClass
    const [min, max] = searchForm.value.scoreRange.split('-').map(Number)
    const matchScore = row.score >= min && row.score <= max
    return matchKeyword && matchClass && matchScore
  })
)

const scoreCards = computed(() => {
  const records = gradeList.value
  const avg = Math.round(records.reduce((sum, item) => sum + item.score, 0) / (records.length || 1))
  const excellent = records.filter((item) => item.score >= 90).length
  const pass = records.filter((item) => item.score >= 60).length
  const passRate = Math.round((pass / (records.length || 1)) * 100)
  return [
    { label: '平均分', value: `${avg}`, desc: '全部班级' },
    { label: '优秀人数', value: `${excellent}`, desc: '90 分及以上' },
    { label: '及格率', value: `${passRate}%`, desc: '60 分及以上' },
    { label: '成绩记录', value: `${records.length}`, desc: '本次检索' }
  ]
})

const scoreDistribution = computed(() => {
  const sections = [
    { label: '90-100', min: 90, max: 100, tone: 'excellent' },
    { label: '80-89', min: 80, max: 89, tone: 'good' },
    { label: '60-79', min: 60, max: 79, tone: 'pass' },
    { label: '0-59', min: 0, max: 59, tone: 'fail' }
  ]
  return sections.map((sec) => {
    const count = gradeList.value.filter((item) => item.score >= sec.min && item.score <= sec.max).length
    const percent = Math.round((count / (gradeList.value.length || 1)) * 100)
    return { ...sec, count, percent }
  })
})

const classAverageBoard = computed(() => {
  const classMap = {}
  gradeList.value.forEach((item) => {
    if (!classMap[item.className]) classMap[item.className] = []
    classMap[item.className].push(item.score)
  })

  return Object.keys(classMap)
    .map((name) => {
      const scores = classMap[name]
      const avg = Math.round(scores.reduce((sum, score) => sum + score, 0) / (scores.length || 1))
      return { className: name, averageScore: avg }
    })
    .sort((a, b) => b.averageScore - a.averageScore)
})

function scoreTagType(score) {
  if (score >= 90) return 'success'
  if (score >= 80) return ''
  if (score >= 60) return 'warning'
  return 'danger'
}

function resetSearch() {
  searchForm.value.keyword = ''
  searchForm.value.className = '全部班级'
  searchForm.value.scoreRange = '全部分段'
}
</script>

<template>
  <div class="teacher-grade">
    <section class="hero">
      <div>
        <p class="hero__path">教学资源 / 成绩管理</p>
        <h1 class="hero__title">成绩管理</h1>
        <p class="hero__desc">录入与追踪班级成绩，快速查看分布与班级均分排名</p>
      </div>
      <div class="hero__actions">
        <el-button class="hero-btn" plain>
          <el-icon><DataAnalysis /></el-icon>
          <span>成绩分析</span>
        </el-button>
        <el-button class="hero-btn hero-btn--primary" type="primary">
          <el-icon><Plus /></el-icon>
          <span>录入成绩</span>
        </el-button>
      </div>
    </section>

    <section class="stats-grid">
      <article v-for="card in scoreCards" :key="card.label" class="stat-card">
        <p class="stat-card__label">{{ card.label }}</p>
        <p class="stat-card__value">{{ card.value }}</p>
        <p class="stat-card__desc">{{ card.desc }}</p>
      </article>
    </section>

    <section class="panel">
      <header class="panel__toolbar">
        <div class="toolbar__left">
          <el-input v-model="searchForm.keyword" placeholder="搜索学员 / 学号 / 作业" clearable class="toolbar__input">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select v-model="searchForm.className" class="toolbar__select">
            <el-option v-for="item in classOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="searchForm.scoreRange" class="toolbar__select">
            <el-option v-for="item in scoreRangeOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </div>
        <div class="toolbar__right">
          <el-button class="panel-btn" @click="resetSearch">
            <el-icon><RefreshRight /></el-icon>
            <span>重置</span>
          </el-button>
          <el-button class="panel-btn">
            <el-icon><Download /></el-icon>
            <span>导出</span>
          </el-button>
        </div>
      </header>

      <div class="content-grid">
        <article class="table-wrap">
          <el-table :data="filteredGrades" stripe>
            <el-table-column prop="gradeId" label="记录编号" min-width="150" />
            <el-table-column prop="className" label="班级" min-width="110" />
            <el-table-column prop="studentName" label="学员姓名" min-width="95" />
            <el-table-column prop="studentNo" label="学号" min-width="120" />
            <el-table-column prop="assignmentName" label="作业名称" min-width="220" show-overflow-tooltip />
            <el-table-column label="分数" width="85">
              <template #default="{ row }">
                <el-tag :type="scoreTagType(row.score)" effect="light">{{ row.score }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="rank" label="班级排名" width="95" />
            <el-table-column prop="comment" label="教师评语" min-width="160" show-overflow-tooltip />
            <el-table-column prop="created_at" label="created_at" min-width="170" />
            <el-table-column prop="updated_at" label="updated_at" min-width="170" />
          </el-table>
        </article>

        <aside class="side-panel">
          <article class="mini-card">
            <h3><el-icon><DocumentChecked /></el-icon> 分数段分布</h3>
            <div class="segment-list">
              <div v-for="item in scoreDistribution" :key="item.label" class="segment-item">
                <div class="segment-item__head">
                  <span>{{ item.label }}</span>
                  <span>{{ item.count }} 人</span>
                </div>
                <div class="segment-item__track">
                  <div class="segment-item__bar" :class="`segment-item__bar--${item.tone}`" :style="{ width: `${item.percent}%` }"></div>
                </div>
              </div>
            </div>
          </article>

          <article class="mini-card">
            <h3><el-icon><Tickets /></el-icon> 班级均分榜</h3>
            <div class="rank-list">
              <div v-for="item in classAverageBoard" :key="item.className" class="rank-item">
                <span>{{ item.className }}</span>
                <strong>{{ item.averageScore }}</strong>
              </div>
            </div>
          </article>
        </aside>
      </div>
    </section>
  </div>
</template>

<style scoped>
.teacher-grade {
  max-width: 1160px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero {
  border-radius: 14px;
  padding: 22px;
  background: linear-gradient(135deg, #0f766e 0%, #1d4ed8 100%);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.hero__path {
  margin: 0;
  color: rgba(255, 255, 255, 0.85);
  font-size: 13px;
}

.hero__title {
  margin: 6px 0 0;
  color: #ffffff;
  font-size: 30px;
  line-height: 38px;
  font-weight: 800;
}

.hero__desc {
  margin: 8px 0 0;
  color: rgba(255, 255, 255, 0.92);
  font-size: 14px;
}

.hero__actions {
  display: flex;
  gap: 10px;
}

.hero-btn {
  height: 38px;
  border-radius: 10px;
}

.hero-btn--primary {
  border: none;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.stat-card {
  border: 1px solid #e5eaf3;
  border-radius: 12px;
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
  font-size: 30px;
  line-height: 1.2;
  font-weight: 800;
}

.stat-card__desc {
  margin: 6px 0 0;
  color: #475569;
  font-size: 12px;
}

.panel {
  border: 1px solid #e5eaf3;
  border-radius: 16px;
  background: #ffffff;
  padding: 16px;
}

.panel__toolbar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar__left,
.toolbar__right {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.toolbar__input {
  width: 280px;
}

.toolbar__select {
  width: 160px;
}

.panel-btn {
  height: 36px;
  border-radius: 10px;
}

.content-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 14px;
}

.table-wrap {
  border: 1px solid #e5eaf3;
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
  border: 1px solid #e5eaf3;
  background: #f8fafc;
  padding: 14px;
}

.mini-card h3 {
  margin: 0;
  color: #0f172a;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 6px;
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
  background: #e2e8f0;
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

.rank-list {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 9px;
}

.rank-item {
  border-radius: 10px;
  border: 1px solid #dbe4f0;
  background: #ffffff;
  padding: 9px 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #334155;
  font-size: 13px;
}

.rank-item strong {
  color: #0f172a;
  font-size: 16px;
}

@media (max-width: 1120px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 680px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .toolbar__input,
  .toolbar__select {
    width: 100%;
  }
}

:global(html.dark) .stat-card,
:global(html.dark) .panel,
:global(html.dark) .table-wrap,
:global(html.dark) .mini-card,
:global(html.dark) .rank-item {
  background: #131922;
  border-color: #273244;
}

:global(html.dark) .stat-card__value,
:global(html.dark) .mini-card h3,
:global(html.dark) .rank-item strong {
  color: #e5eaf3;
}

:global(html.dark) .stat-card__label,
:global(html.dark) .stat-card__desc,
:global(html.dark) .segment-item__head,
:global(html.dark) .rank-item {
  color: #9ba7ba;
}

:global(html.dark) .segment-item__track {
  background: #202b3a;
}

:global(html.dark) .rank-item {
  background: #0f141c;
}
</style>

