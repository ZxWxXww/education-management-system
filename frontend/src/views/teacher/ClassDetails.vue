<script setup>
import { computed, ref } from 'vue'
import { Calendar, DataAnalysis, DocumentChecked, Histogram, Reading, UserFilled } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 班级详情页 Mock 数据（接入真实后端时，替换为 src/api/teacher/class.js 的接口请求）
const classDetailMap = {
  'CLS-2026-001': {
    classId: 'CLS-2026-001',
    className: '高二数学提高班',
    courseName: '数学（函数与导数）',
    classroom: 'B-302',
    schedule: '每周一 / 三 08:30 - 10:00',
    termLabel: '2026 春季学期',
    classSlogan: '稳基础、强思维、提成绩',
    teacherName: '王老师',
    teacherTitle: '数学教研组',
    created_at: '2026-02-20 09:00:00',
    updated_at: '2026-04-06 08:40:00',
    stats: [
      { key: 'students', label: '在读学员', value: 45, unit: '人', trend: '+2', icon: 'users' },
      { key: 'attendance', label: '本周出勤率', value: 96.4, unit: '%', trend: '+1.2%', icon: 'calendar' },
      { key: 'homework', label: '作业提交率', value: 92.0, unit: '%', trend: '+3.4%', icon: 'homework' },
      { key: 'score', label: '阶段测评均分', value: 86.5, unit: '分', trend: '+2.1', icon: 'score' }
    ],
    chapterProgress: [
      { chapter: '函数图像综合', progress: 100 },
      { chapter: '导数基础应用', progress: 84 },
      { chapter: '导数综合题型', progress: 62 },
      { chapter: '阶段复盘训练', progress: 38 }
    ],
    sessions: [
      { date: '04-01', topic: '函数单调性专项', content: '单调区间判定与证明', homework: '课后练习 1-12 题', status: '已完成', created_at: '2026-04-01 10:00:00', updated_at: '2026-04-01 10:10:00' },
      { date: '04-03', topic: '导数与最值', content: '导数求最值综合题', homework: '课堂测验 A 卷', status: '已完成', created_at: '2026-04-03 10:00:00', updated_at: '2026-04-03 10:05:00' },
      { date: '04-08', topic: '切线问题提升', content: '切线方程与综合应用', homework: '课后练习 13-20 题', status: '进行中', created_at: '2026-04-08 08:30:00', updated_at: '2026-04-08 08:35:00' }
    ],
    students: [
      { studentId: 'STU-23001', studentName: '李同学', gender: '男', attendanceRate: 98, homeworkRate: 100, score: 91, status: '稳定', created_at: '2026-02-20 09:10:00', updated_at: '2026-04-06 08:20:00' },
      { studentId: 'STU-23002', studentName: '张同学', gender: '女', attendanceRate: 92, homeworkRate: 94, score: 86, status: '提升中', created_at: '2026-02-20 09:12:00', updated_at: '2026-04-06 08:21:00' },
      { studentId: 'STU-23003', studentName: '赵同学', gender: '男', attendanceRate: 88, homeworkRate: 82, score: 79, status: '需关注', created_at: '2026-02-20 09:13:00', updated_at: '2026-04-06 08:22:00' },
      { studentId: 'STU-23004', studentName: '王同学', gender: '女', attendanceRate: 95, homeworkRate: 96, score: 89, status: '稳定', created_at: '2026-02-20 09:15:00', updated_at: '2026-04-06 08:22:30' },
      { studentId: 'STU-23005', studentName: '陈同学', gender: '男', attendanceRate: 84, homeworkRate: 78, score: 74, status: '需关注', created_at: '2026-02-20 09:17:00', updated_at: '2026-04-06 08:23:00' }
    ]
  }
}

const fallbackId = 'CLS-2026-001'
const activeClassId = computed(() => String(route.params.id || fallbackId))
const classDetail = computed(() => classDetailMap[activeClassId.value] || classDetailMap[fallbackId])
const studentKeyword = ref('')

const filteredStudents = computed(() => {
  const keyword = studentKeyword.value.trim()
  if (!keyword) return classDetail.value.students
  return classDetail.value.students.filter(
    (item) => item.studentName.includes(keyword) || item.studentId.includes(keyword)
  )
})

function statusType(status) {
  if (status === '稳定') return 'success'
  if (status === '提升中') return 'warning'
  return 'danger'
}

function sessionTagType(status) {
  if (status === '已完成') return 'success'
  if (status === '进行中') return 'primary'
  return 'info'
}

function cardIcon(icon) {
  if (icon === 'users') return UserFilled
  if (icon === 'calendar') return Calendar
  if (icon === 'homework') return DocumentChecked
  return DataAnalysis
}
</script>

<template>
  <div class="class-details">
    <section class="details-hero">
      <div>
        <p class="details-hero__path">我的课程 / 班级详情</p>
        <h1 class="details-hero__title">{{ classDetail.className }}</h1>
        <p class="details-hero__meta">{{ classDetail.courseName }} · {{ classDetail.schedule }} · {{ classDetail.classroom }}</p>
        <p class="details-hero__desc">{{ classDetail.classSlogan }}</p>
      </div>
      <div class="details-hero__badge">
        <span>{{ classDetail.termLabel }}</span>
        <strong>{{ classDetail.teacherName }}</strong>
        <small>{{ classDetail.teacherTitle }}</small>
      </div>
    </section>

    <section class="details-stat-grid">
      <article v-for="card in classDetail.stats" :key="card.key" class="stat-card">
        <div class="stat-card__head">
          <span>{{ card.label }}</span>
          <el-icon><component :is="cardIcon(card.icon)" /></el-icon>
        </div>
        <p class="stat-card__value">{{ card.value }}<small>{{ card.unit }}</small></p>
        <p class="stat-card__trend">较上周 {{ card.trend }}</p>
      </article>
    </section>

    <section class="details-main-grid">
      <article class="panel">
        <header class="panel__head">
          <h2><el-icon><Histogram /></el-icon> 章节进度</h2>
          <span>{{ classDetail.chapterProgress.length }} 个模块</span>
        </header>
        <div class="progress-list">
          <div v-for="item in classDetail.chapterProgress" :key="item.chapter" class="progress-item">
            <div class="progress-item__top">
              <span>{{ item.chapter }}</span>
              <span>{{ item.progress }}%</span>
            </div>
            <el-progress :percentage="item.progress" :stroke-width="8" :show-text="false" />
          </div>
        </div>
      </article>

      <article class="panel">
        <header class="panel__head">
          <h2><el-icon><Reading /></el-icon> 最近课次</h2>
          <span>{{ classDetail.sessions.length }} 节</span>
        </header>
        <div class="session-list">
          <div v-for="item in classDetail.sessions" :key="`${item.date}-${item.topic}`" class="session-item">
            <div>
              <p class="session-item__date">{{ item.date }}</p>
              <p class="session-item__topic">{{ item.topic }}</p>
              <p class="session-item__text">{{ item.content }}</p>
              <p class="session-item__text">作业：{{ item.homework }}</p>
            </div>
            <el-tag :type="sessionTagType(item.status)" effect="plain">{{ item.status }}</el-tag>
          </div>
        </div>
      </article>
    </section>

    <section class="panel">
      <header class="panel__head panel__head--with-action">
        <h2><el-icon><UserFilled /></el-icon> 学员名单</h2>
        <el-input v-model="studentKeyword" class="student-search" clearable placeholder="搜索姓名或学号" />
      </header>

      <el-table :data="filteredStudents" stripe>
        <el-table-column prop="studentId" label="学号" min-width="120" />
        <el-table-column prop="studentName" label="姓名" min-width="100" />
        <el-table-column prop="gender" label="性别" min-width="80" />
        <el-table-column prop="attendanceRate" label="出勤率" min-width="110">
          <template #default="{ row }">{{ row.attendanceRate }}%</template>
        </el-table-column>
        <el-table-column prop="homeworkRate" label="作业完成率" min-width="130">
          <template #default="{ row }">{{ row.homeworkRate }}%</template>
        </el-table-column>
        <el-table-column prop="score" label="阶段成绩" min-width="100" />
        <el-table-column prop="status" label="学习状态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="light">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<style scoped>
.class-details {
  max-width: 1160px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.details-hero {
  border-radius: 16px;
  padding: 24px;
  background: linear-gradient(135deg, #1d4ed8 0%, #1e3a8a 100%);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.details-hero__path,
.details-hero__title,
.details-hero__meta,
.details-hero__desc {
  margin: 0;
}

.details-hero__path {
  font-size: 12px;
  line-height: 18px;
  color: rgba(255, 255, 255, 0.74);
}

.details-hero__title {
  margin-top: 6px;
  color: #ffffff;
  font-size: 28px;
  line-height: 36px;
  font-weight: 800;
}

.details-hero__meta {
  margin-top: 8px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.details-hero__desc {
  margin-top: 4px;
  color: rgba(255, 255, 255, 0.78);
  font-size: 13px;
}

.details-hero__badge {
  border-radius: 12px;
  padding: 12px 14px;
  min-width: 150px;
  background: rgba(255, 255, 255, 0.16);
  color: #ffffff;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.details-hero__badge span {
  font-size: 12px;
  opacity: 0.86;
}

.details-hero__badge strong {
  font-size: 18px;
  line-height: 24px;
}

.details-hero__badge small {
  font-size: 12px;
  opacity: 0.86;
}

.details-stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.stat-card {
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  padding: 14px;
}

.stat-card__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #6b7280;
  font-size: 12px;
}

.stat-card__value {
  margin: 12px 0 4px;
  color: #111827;
  font-size: 26px;
  line-height: 32px;
  font-weight: 800;
}

.stat-card__value small {
  margin-left: 4px;
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
}

.stat-card__trend {
  margin: 0;
  color: #16a34a;
  font-size: 12px;
}

.details-main-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 12px;
}

.panel {
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  padding: 16px;
}

.panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}

.panel__head h2 {
  margin: 0;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  color: #111827;
}

.panel__head span {
  color: #6b7280;
  font-size: 12px;
}

.panel__head--with-action {
  flex-wrap: wrap;
}

.student-search {
  width: 240px;
}

.progress-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.progress-item__top {
  margin-bottom: 6px;
  display: flex;
  justify-content: space-between;
  color: #374151;
  font-size: 13px;
}

.session-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.session-item {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 12px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}

.session-item__date,
.session-item__topic,
.session-item__text {
  margin: 0;
}

.session-item__date {
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 600;
}

.session-item__topic {
  margin-top: 2px;
  color: #111827;
  font-size: 14px;
  font-weight: 700;
}

.session-item__text {
  margin-top: 3px;
  color: #6b7280;
  font-size: 12px;
}

@media (max-width: 1080px) {
  .details-stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .details-main-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .details-hero {
    flex-direction: column;
    padding: 18px;
  }

  .details-stat-grid {
    grid-template-columns: 1fr;
  }

  .student-search {
    width: 100%;
  }
}

:global(html.dark) .stat-card,
:global(html.dark) .panel,
:global(html.dark) .session-item {
  background: #1a2028;
  border-color: #2b3442;
}

:global(html.dark) .panel__head h2,
:global(html.dark) .stat-card__value,
:global(html.dark) .session-item__topic {
  color: #e5eaf3;
}

:global(html.dark) .stat-card__head,
:global(html.dark) .panel__head span,
:global(html.dark) .stat-card__value small,
:global(html.dark) .progress-item__top,
:global(html.dark) .session-item__text {
  color: #9ba7ba;
}
</style>

