<script setup>
import { computed } from 'vue'
import { Bell, Calendar, CircleCheck, DataAnalysis, Reading, UserFilled, WarningFilled } from '@element-plus/icons-vue'

// 教师工作台 Mock 数据（切换真实后端时，请替换为 src/api/teacher/workspace.js 的接口返回）
const teacherProfile = {
  name: '王老师',
  title: '数学教研组 · 高二年级',
  todayLabel: '2026-04-06 星期一',
  created_at: '2026-04-06 08:00:00',
  updated_at: '2026-04-06 08:00:00'
}

const statisticCards = [
  { key: 'classes', label: '在授班级', value: '6', unit: '个', trend: '+1', trendType: 'up', icon: 'reading' },
  { key: 'students', label: '在读学生', value: '218', unit: '人', trend: '+12', trendType: 'up', icon: 'users' },
  { key: 'homework', label: '待批作业', value: '38', unit: '份', trend: '-4', trendType: 'down', icon: 'check' },
  { key: 'abnormal', label: '异常考勤', value: '7', unit: '条', trend: '+2', trendType: 'warn', icon: 'warning' }
]

const todayCourseList = [
  {
    id: 1,
    courseName: '高二数学提高班',
    className: '高二（3）班',
    timeRange: '08:30 - 10:00',
    room: 'B-302',
    status: '进行中',
    created_at: '2026-04-06 08:30:00',
    updated_at: '2026-04-06 08:35:00'
  },
  {
    id: 2,
    courseName: '高二数学冲刺班',
    className: '高二（6）班',
    timeRange: '13:30 - 15:00',
    room: 'A-201',
    status: '待开始',
    created_at: '2026-04-06 13:30:00',
    updated_at: '2026-04-06 13:30:00'
  },
  {
    id: 3,
    courseName: '高二竞赛数学',
    className: '竞赛1班',
    timeRange: '19:00 - 20:30',
    room: '线上直播',
    status: '待开始',
    created_at: '2026-04-06 19:00:00',
    updated_at: '2026-04-06 19:00:00'
  }
]

const assignmentProgress = [
  { className: '高二（3）班', submitted: 42, total: 45, created_at: '2026-04-05 21:00:00', updated_at: '2026-04-06 08:10:00' },
  { className: '高二（6）班', submitted: 39, total: 44, created_at: '2026-04-05 21:00:00', updated_at: '2026-04-06 08:12:00' },
  { className: '竞赛1班', submitted: 22, total: 28, created_at: '2026-04-05 22:00:00', updated_at: '2026-04-06 08:15:00' }
]

const abnormalAttendanceList = [
  { studentName: '李同学', className: '高二（3）班', type: '迟到', time: '08:42', created_at: '2026-04-06 08:42:00', updated_at: '2026-04-06 08:43:00' },
  { studentName: '张同学', className: '高二（6）班', type: '缺勤', time: '13:32', created_at: '2026-04-06 13:32:00', updated_at: '2026-04-06 13:35:00' },
  { studentName: '赵同学', className: '竞赛1班', type: '请假', time: '18:55', created_at: '2026-04-06 18:55:00', updated_at: '2026-04-06 18:58:00' }
]

const teachingTrend = [72, 75, 80, 84, 82, 88, 91]
const trendLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const trendPath = computed(() => {
  const width = 560
  const height = 210
  const min = Math.min(...teachingTrend)
  const max = Math.max(...teachingTrend)
  const range = max - min || 1
  return teachingTrend
    .map((point, index) => {
      const x = (index / (teachingTrend.length - 1)) * width
      const y = height - ((point - min) / range) * (height - 36) - 12
      return `${x.toFixed(2)},${y.toFixed(2)}`
    })
    .join(' ')
})

const assignmentProgressWithRate = computed(() =>
  assignmentProgress.map((item) => ({
    ...item,
    rate: Number(((item.submitted / item.total) * 100).toFixed(0))
  }))
)
</script>

<template>
  <div class="workspace">
    <section class="workspace__hero">
      <div>
        <h1 class="workspace__title">教师工作台</h1>
        <p class="workspace__subtitle">{{ teacherProfile.name }}，{{ teacherProfile.title }}</p>
      </div>
      <div class="workspace__date">{{ teacherProfile.todayLabel }}</div>
    </section>

    <section class="workspace__stats">
      <article v-for="card in statisticCards" :key="card.key" class="stat-card">
        <div class="stat-card__head">
          <span class="stat-card__label">{{ card.label }}</span>
          <span class="stat-card__icon" :class="`stat-card__icon--${card.icon}`">
            <el-icon :size="16"><Reading v-if="card.icon === 'reading'" /></el-icon>
            <el-icon :size="16"><UserFilled v-if="card.icon === 'users'" /></el-icon>
            <el-icon :size="16"><CircleCheck v-if="card.icon === 'check'" /></el-icon>
            <el-icon :size="16"><WarningFilled v-if="card.icon === 'warning'" /></el-icon>
          </span>
        </div>
        <p class="stat-card__value">{{ card.value }}<small>{{ card.unit }}</small></p>
        <p class="stat-card__trend" :class="`stat-card__trend--${card.trendType}`">较昨日 {{ card.trend }}</p>
      </article>
    </section>

    <section class="workspace__main">
      <article class="panel panel--schedule">
        <header class="panel__head">
          <h2 class="panel__title"><el-icon><Calendar /></el-icon> 今日课程安排</h2>
          <span class="panel__tag">{{ todayCourseList.length }} 节</span>
        </header>
        <div class="course-list">
          <div v-for="course in todayCourseList" :key="course.id" class="course-item">
            <div class="course-item__time">{{ course.timeRange }}</div>
            <div class="course-item__content">
              <p class="course-item__title">{{ course.courseName }}</p>
              <p class="course-item__meta">{{ course.className }} · {{ course.room }}</p>
            </div>
            <span class="course-item__status" :class="{ 'course-item__status--active': course.status === '进行中' }">{{ course.status }}</span>
          </div>
        </div>
      </article>

      <article class="panel panel--trend">
        <header class="panel__head">
          <h2 class="panel__title"><el-icon><DataAnalysis /></el-icon> 教学完成度趋势</h2>
          <span class="panel__tag">近 7 天</span>
        </header>
        <div class="trend-chart">
          <svg viewBox="0 0 560 240" preserveAspectRatio="none" aria-hidden="true">
            <defs>
              <linearGradient id="workspaceLineArea" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#1d4ed8" stop-opacity="0.35" />
                <stop offset="100%" stop-color="#1d4ed8" stop-opacity="0" />
              </linearGradient>
            </defs>
            <polygon :points="`0,228 ${trendPath} 560,228`" fill="url(#workspaceLineArea)" />
            <polyline :points="trendPath" fill="none" stroke="#1d4ed8" stroke-width="4" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
          <div class="trend-chart__axis">
            <span v-for="day in trendLabels" :key="day">{{ day }}</span>
          </div>
        </div>
      </article>
    </section>

    <section class="workspace__bottom">
      <article class="panel panel--assignment">
        <header class="panel__head">
          <h2 class="panel__title"><el-icon><CircleCheck /></el-icon> 作业提交进度</h2>
          <span class="panel__tag">{{ assignmentProgress.length }} 个班级</span>
        </header>
        <div class="progress-list">
          <div v-for="item in assignmentProgressWithRate" :key="item.className" class="progress-item">
            <div class="progress-item__top">
              <span>{{ item.className }}</span>
              <span>{{ item.submitted }}/{{ item.total }}（{{ item.rate }}%）</span>
            </div>
            <div class="progress-item__track">
              <div class="progress-item__bar" :style="{ width: `${item.rate}%` }"></div>
            </div>
          </div>
        </div>
      </article>

      <article class="panel panel--attendance">
        <header class="panel__head">
          <h2 class="panel__title"><el-icon><Bell /></el-icon> 异常考勤提醒</h2>
          <span class="panel__tag panel__tag--warn">{{ abnormalAttendanceList.length }} 条</span>
        </header>
        <div class="warning-list">
          <div v-for="item in abnormalAttendanceList" :key="`${item.studentName}-${item.time}`" class="warning-item">
            <div class="warning-item__left">
              <p class="warning-item__name">{{ item.studentName }}</p>
              <p class="warning-item__meta">{{ item.className }} · {{ item.type }}</p>
            </div>
            <span class="warning-item__time">{{ item.time }}</span>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<style scoped>
.workspace {
  max-width: 1160px;
  margin: 0 auto;
  padding: 8px 4px 18px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.workspace__hero {
  border-radius: 14px;
  background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 100%);
  padding: 24px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.workspace__title {
  margin: 0;
  color: #ffffff;
  font-size: 28px;
  line-height: 36px;
  font-weight: 800;
}

.workspace__subtitle {
  margin: 8px 0 0;
  color: rgba(255, 255, 255, 0.86);
  font-size: 14px;
  line-height: 22px;
}

.workspace__date {
  border-radius: 999px;
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.18);
  color: #ffffff;
  font-size: 12px;
  line-height: 18px;
}

.workspace__stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.stat-card {
  border-radius: 12px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  padding: 16px;
}

.stat-card__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-card__label {
  color: #6b7280;
  font-size: 12px;
  line-height: 18px;
}

.stat-card__icon {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  display: inline-flex;
  justify-content: center;
  align-items: center;
}

.stat-card__icon--reading {
  background: #dbeafe;
  color: #1d4ed8;
}

.stat-card__icon--users {
  background: #e0f2fe;
  color: #0284c7;
}

.stat-card__icon--check {
  background: #dcfce7;
  color: #16a34a;
}

.stat-card__icon--warning {
  background: #fee2e2;
  color: #dc2626;
}

.stat-card__value {
  margin: 14px 0 4px;
  color: #111827;
  font-size: 30px;
  line-height: 38px;
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
  font-size: 12px;
  line-height: 18px;
}

.stat-card__trend--up {
  color: #16a34a;
}

.stat-card__trend--down {
  color: #2563eb;
}

.stat-card__trend--warn {
  color: #dc2626;
}

.workspace__main,
.workspace__bottom {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(0, 1fr);
  gap: 14px;
}

.panel {
  border-radius: 12px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  padding: 18px;
}

.panel__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.panel__title {
  margin: 0;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #111827;
  font-size: 16px;
  font-weight: 700;
  line-height: 24px;
}

.panel__tag {
  border-radius: 999px;
  background: #eff6ff;
  color: #1d4ed8;
  padding: 4px 10px;
  font-size: 12px;
  line-height: 18px;
}

.panel__tag--warn {
  background: #fef2f2;
  color: #dc2626;
}

.course-list,
.progress-list,
.warning-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.course-item {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 12px;
  display: grid;
  grid-template-columns: 100px minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
}

.course-item__time {
  color: #1d4ed8;
  font-size: 12px;
  line-height: 18px;
  font-weight: 600;
}

.course-item__title,
.course-item__meta,
.warning-item__name,
.warning-item__meta {
  margin: 0;
}

.course-item__title {
  color: #111827;
  font-size: 14px;
  line-height: 20px;
  font-weight: 600;
}

.course-item__meta {
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
  line-height: 18px;
}

.course-item__status {
  border-radius: 999px;
  padding: 4px 10px;
  background: #f3f4f6;
  color: #4b5563;
  font-size: 12px;
}

.course-item__status--active {
  background: #dcfce7;
  color: #15803d;
}

.trend-chart {
  height: 226px;
  background: linear-gradient(180deg, rgba(29, 78, 216, 0.08) 0%, rgba(29, 78, 216, 0.01) 100%);
  border-radius: 10px;
  position: relative;
  overflow: hidden;
}

.trend-chart svg {
  width: 100%;
  height: calc(100% - 28px);
}

.trend-chart__axis {
  position: absolute;
  bottom: 6px;
  left: 10px;
  right: 10px;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  color: #6b7280;
  font-size: 11px;
}

.progress-item__top {
  display: flex;
  justify-content: space-between;
  color: #374151;
  font-size: 12px;
  line-height: 18px;
  margin-bottom: 6px;
}

.progress-item__track {
  height: 8px;
  border-radius: 999px;
  background: #e5e7eb;
  overflow: hidden;
}

.progress-item__bar {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #1d4ed8 0%, #60a5fa 100%);
}

.warning-item {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.warning-item__name {
  color: #111827;
  font-size: 14px;
  line-height: 20px;
  font-weight: 600;
}

.warning-item__meta {
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
  line-height: 18px;
}

.warning-item__time {
  border-radius: 999px;
  background: #fef2f2;
  color: #dc2626;
  padding: 4px 10px;
  font-size: 12px;
}

@media (max-width: 1080px) {
  .workspace__stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .workspace__main,
  .workspace__bottom {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .workspace__hero {
    padding: 18px;
    flex-direction: column;
  }

  .workspace__title {
    font-size: 24px;
    line-height: 32px;
  }

  .workspace__stats {
    grid-template-columns: 1fr;
  }

  .course-item {
    grid-template-columns: 1fr;
    gap: 8px;
    align-items: flex-start;
  }
}

:global(html.dark) .stat-card,
:global(html.dark) .panel,
:global(html.dark) .course-item,
:global(html.dark) .warning-item {
  background: #1a2028;
  border-color: #2b3442;
}

:global(html.dark) .panel__title,
:global(html.dark) .course-item__title,
:global(html.dark) .warning-item__name,
:global(html.dark) .stat-card__value {
  color: #e5eaf3;
}

:global(html.dark) .course-item__meta,
:global(html.dark) .warning-item__meta,
:global(html.dark) .progress-item__top,
:global(html.dark) .trend-chart__axis,
:global(html.dark) .stat-card__label,
:global(html.dark) .stat-card__value small {
  color: #9ba7ba;
}

:global(html.dark) .course-item__status {
  background: #313b4b;
  color: #b9c2d0;
}

:global(html.dark) .course-item__status--active {
  background: rgba(22, 163, 74, 0.24);
  color: #7be5a8;
}

:global(html.dark) .progress-item__track {
  background: #313b4b;
}

:global(html.dark) .warning-item__time {
  background: rgba(220, 38, 38, 0.22);
  color: #fca5a5;
}
</style>

