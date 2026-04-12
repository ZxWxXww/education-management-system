<script setup>
import { computed, onMounted, ref } from 'vue'
import { Calendar, Reading, UserFilled, Wallet } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { fetchAdminAttendanceOverview } from '../../api/admin/attendance'
import { fetchAdminBillPage } from '../../api/admin/bill'
import { fetchAdminCoursePage } from '../../api/admin/course'
import { fetchAdminClassPage } from '../../api/admin/class'
import { fetchAdminUserPage } from '../../api/admin/user'

const router = useRouter()
const loading = ref(false)
const userRows = ref([])
const courseRows = ref([])
const classRows = ref([])
const billRows = ref([])
const attendanceOverview = ref({
  totalAttendanceCount: 0,
  punctualRate: 0,
  leaveCount: 0,
  exceptionCount: 0,
  trend: [],
  focusAlerts: []
})

const statCards = computed(() => {
  const userTotal = userRows.value.length
  const studentTotal = userRows.value.filter((item) => String(item.role).includes('学生')).length
  const newWeekUsers = userRows.value.filter((item) => {
    if (item.createdAt === '-') return false
    return Date.now() - new Date(item.createdAt).getTime() <= 7 * 24 * 60 * 60 * 1000
  }).length
  const totalPaid = billRows.value.reduce((sum, item) => sum + Number(item.paidAmount || 0), 0)
  return [
    { title: '总用户数', value: `${userTotal}`, trend: `启用 ${userRows.value.filter((item) => item.status === '启用').length}`, icon: 'user' },
    { title: '在读学员', value: `${studentTotal}`, trend: `班级 ${classRows.value.length}`, icon: 'student' },
    { title: '近 7 天新增', value: `${newWeekUsers}`, trend: `课程 ${courseRows.value.length}`, icon: 'calendar' },
    { title: '累计实收', value: `¥${totalPaid.toFixed(2)}`, trend: `准点率 ${attendanceOverview.value.punctualRate}%`, icon: 'money' }
  ]
})

const weekLabels = computed(() => attendanceOverview.value.trend.map((item) => item.label))
const revenuePoints = computed(() => attendanceOverview.value.trend.map((item) => item.punctualRate))
const enrollTrend = computed(() => {
  const total = userRows.value.length || 1
  const enabledUsers = userRows.value.filter((item) => item.status === '启用').length
  const teachers = userRows.value.filter((item) => String(item.role).includes('教师')).length
  const students = userRows.value.filter((item) => String(item.role).includes('学生')).length
  const admins = userRows.value.filter((item) => String(item.role).includes('管理员')).length
  return [
    { label: '启用用户', value: `${enabledUsers} 人`, percent: Math.round((enabledUsers / total) * 100) },
    { label: '教师用户', value: `${teachers} 人`, percent: Math.round((teachers / total) * 100) },
    { label: '学生用户', value: `${students} 人`, percent: Math.round((students / total) * 100) },
    { label: '管理员', value: `${admins} 人`, percent: Math.round((admins / total) * 100) }
  ]
})

const activityList = computed(() => {
  const items = []
  if (courseRows.value[0]) {
    items.push({
      title: `课程《${courseRows.value[0].courseName}》最近更新`,
      desc: `课程管理 · ${courseRows.value[0].updatedAt}`,
      path: '/admin/course',
      buttonText: '查看课程'
    })
  }
  if (classRows.value[0]) {
    items.push({
      title: `班级《${classRows.value[0].className}》状态为 ${classRows.value[0].status}`,
      desc: `班级管理 · ${classRows.value[0].updatedAt}`,
      path: '/admin/course/classes',
      buttonText: '查看班级'
    })
  }
  if (billRows.value[0]) {
    items.push({
      title: `财单 ${billRows.value[0].billNo} 当前为 ${billRows.value[0].status}`,
      desc: `财单管理 · ${billRows.value[0].updatedAt}`,
      path: '/admin/finance/bills',
      buttonText: '查看财单'
    })
  }
  attendanceOverview.value.focusAlerts.forEach((item) => {
    items.push({
      title: item.text,
      desc: `考勤管理 · 风险等级 ${item.level}`,
      path: '/admin/attendance/abnormal',
      buttonText: '前往处理'
    })
  })
  return items.slice(0, 4)
})

const quickActions = [
  { text: '新增课程', tone: 'primary', path: '/admin/course' },
  { text: '添加用户', tone: 'soft-blue', path: '/admin/user' },
  { text: '财务统计', tone: 'soft-gray', path: '/admin/finance' },
  { text: '考勤管理', tone: 'soft-gray', path: '/admin/attendance' }
]

const revenuePath = computed(() => {
  const width = 592
  const height = 210
  if (!revenuePoints.value.length) return ''
  const max = Math.max(...revenuePoints.value)
  const min = Math.min(...revenuePoints.value)
  const span = max - min || 1

  return revenuePoints.value
    .map((v, index) => {
      const x = revenuePoints.value.length === 1 ? width / 2 : (index / (revenuePoints.value.length - 1)) * width
      const y = height - ((v - min) / span) * (height - 24) - 6
      return `${x.toFixed(2)},${y.toFixed(2)}`
    })
    .join(' ')
})

async function loadDashboard() {
  loading.value = true
  try {
    const [users, courses, classes, bills, attendance] = await Promise.all([
      fetchAdminUserPage({ pageNum: 1, pageSize: 300 }),
      fetchAdminCoursePage({ pageNum: 1, pageSize: 300 }),
      fetchAdminClassPage({ pageNum: 1, pageSize: 300 }),
      fetchAdminBillPage({ pageNum: 1, pageSize: 300 }),
      fetchAdminAttendanceOverview()
    ])
    userRows.value = users.list
    courseRows.value = courses.list
    classRows.value = classes.list
    billRows.value = bills.list
    attendanceOverview.value = attendance
  } catch (error) {
    ElMessage.error(error.message || '仪表盘加载失败')
  } finally {
    loading.value = false
  }
}

function goAction(path) {
  router.push(path)
}

function openActivity(path) {
  if (!path) return
  router.push(path)
}

onMounted(() => {
  loadDashboard()
})
</script>

<template>
  <div v-loading="loading" class="dashboard">
    <section class="welcome">
      <h1 class="welcome__title">概览工作台</h1>
      <p class="welcome__subtitle">欢迎回来，这是您当前真实业务数据的概览面板。</p>
    </section>

    <section class="stats-grid">
      <article v-for="card in statCards" :key="card.title" class="stat-card">
        <div class="stat-card__head">
          <div class="stat-card__icon" :class="`stat-card__icon--${card.icon}`">
            <el-icon :size="16"><UserFilled v-if="card.icon === 'user'" /></el-icon>
            <el-icon :size="16"><Reading v-if="card.icon === 'student'" /></el-icon>
            <el-icon :size="16"><Calendar v-if="card.icon === 'calendar'" /></el-icon>
            <el-icon :size="16"><Wallet v-if="card.icon === 'money'" /></el-icon>
          </div>
          <span class="stat-card__trend">{{ card.trend }}</span>
        </div>
        <div class="stat-card__title">{{ card.title }}</div>
        <div class="stat-card__value">{{ card.value }}</div>
      </article>
    </section>

    <section class="charts-row">
      <article class="panel panel--revenue">
        <header class="panel__head">
          <h2 class="panel__title">到课率趋势图</h2>
          <span class="panel__tag">最近 7 天</span>
        </header>
        <div class="chart-area">
          <svg class="chart-svg" viewBox="0 0 592 256" preserveAspectRatio="none" aria-hidden="true">
            <defs>
              <linearGradient id="lineFill" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#005DAA" stop-opacity="0.35" />
                <stop offset="100%" stop-color="#005DAA" stop-opacity="0" />
              </linearGradient>
            </defs>
            <polygon v-if="revenuePath" :points="`0,242 ${revenuePath} 592,242`" fill="url(#lineFill)" />
            <polyline v-if="revenuePath" :points="revenuePath" fill="none" stroke="#005DAA" stroke-width="4" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
          <div class="chart-axis">
            <span v-for="day in weekLabels" :key="day">{{ day }}</span>
          </div>
        </div>
      </article>

      <article class="panel panel--enroll">
        <header class="panel__head panel__head--single">
          <h2 class="panel__title">用户结构占比</h2>
        </header>
        <div class="enroll-list">
          <div v-for="row in enrollTrend" :key="row.label" class="enroll-item">
            <div class="enroll-item__top">
              <span>{{ row.label }}</span>
              <span>{{ row.value }}</span>
            </div>
            <div class="enroll-item__track">
              <div class="enroll-item__bar" :style="{ width: `${row.percent}%` }"></div>
            </div>
          </div>
        </div>
      </article>
    </section>

    <section class="bottom-row">
      <article class="panel panel--activity">
        <header class="panel__head panel__head--single">
          <h2 class="panel__title">最近动态</h2>
        </header>
        <div class="activity-list">
          <div v-for="item in activityList" :key="item.title" class="activity-item">
            <span class="activity-item__dot"></span>
            <div class="activity-item__content">
              <p class="activity-item__title">{{ item.title }}</p>
              <p class="activity-item__desc">{{ item.desc }}</p>
            </div>
            <el-button link type="primary" class="activity-item__link" @click="openActivity(item.path)">
              {{ item.buttonText }}
            </el-button>
          </div>
        </div>
      </article>

      <article class="panel panel--quick">
        <header class="panel__head panel__head--single">
          <h2 class="panel__title">快捷操作</h2>
        </header>
        <div class="quick-list">
          <button
            v-for="action in quickActions"
            :key="action.text"
            type="button"
            class="quick-btn"
            :class="`quick-btn--${action.tone}`"
            @click="goAction(action.path)"
          >
            <span class="quick-btn__icon"></span>
            <span>{{ action.text }}</span>
          </button>
        </div>
      </article>
    </section>
  </div>
</template>

<style scoped>
.dashboard {
  max-width: 996px;
  margin: 0 auto;
  padding: 80px 16px 24px;
  display: flex;
  flex-direction: column;
  gap: 40px;
  background: #f7f9fc;
}

.welcome {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.welcome__title {
  margin: 0;
  color: #191c1e;
  font-size: 30px;
  font-weight: 700;
  line-height: 40px;
}

.welcome__subtitle {
  margin: 0;
  color: #404753;
  font-size: 14px;
  line-height: 22px;
}

.stats-grid {
  display: grid;
  gap: 20px;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.stat-card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 93, 170, 0.06);
  padding: 24px;
}

.stat-card__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-card__icon {
  width: 32px;
  height: 32px;
  border-radius: 12px;
  background: #eff6ff;
  color: #1d4ed8;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.stat-card__trend {
  border-radius: 999px;
  padding: 4px 8px;
  background: #f0fdf4;
  color: #16a34a;
  font-size: 12px;
  font-weight: 700;
  line-height: 16px;
}

.stat-card__title {
  margin-top: 16px;
  color: #404753;
  font-size: 12px;
  line-height: 16px;
}

.stat-card__value {
  margin-top: 4px;
  color: #191c1e;
  font-family: Manrope, sans-serif;
  font-size: 24px;
  font-weight: 900;
  line-height: 32px;
}

.charts-row,
.bottom-row {
  display: grid;
  gap: 24px;
  grid-template-columns: 2fr 1fr;
}

.panel {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 93, 170, 0.06);
}

.panel--revenue {
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.panel--enroll {
  padding: 32px 32px 88px;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.panel--activity {
  padding: 32px 32px 136px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.panel--quick {
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel__head--single {
  justify-content: flex-start;
}

.panel__title {
  margin: 0;
  color: #191c1e;
  font-size: 18px;
  font-weight: 700;
  line-height: 28px;
}

.panel__tag {
  border-radius: 999px;
  padding: 4px 12px;
  background: #f2f4f7;
  color: #404753;
  font-size: 12px;
  line-height: 18px;
}

.chart-area {
  position: relative;
  height: 256px;
  background: linear-gradient(180deg, rgba(239, 246, 255, 0) 0%, rgba(239, 246, 255, 0.2) 100%);
}

.chart-svg {
  width: 100%;
  height: 100%;
}

.chart-axis {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  padding-bottom: 4px;
  color: #94a3b8;
  font-size: 10px;
  line-height: 15px;
  text-align: left;
}

.enroll-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.enroll-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.enroll-item__top {
  display: flex;
  justify-content: space-between;
  color: #404753;
  font-size: 12px;
  line-height: 20px;
}

.enroll-item__track {
  height: 8px;
  border-radius: 999px;
  background: #f1f5f9;
  overflow: hidden;
}

.enroll-item__bar {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #1d4ed8 0%, #60a5fa 100%);
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.activity-item__dot {
  width: 40px;
  height: 40px;
  border-radius: 999px;
  background: #eff6ff;
  flex: none;
}

.activity-item__content {
  flex: 1;
}

.activity-item__title,
.activity-item__desc {
  margin: 0;
}

.activity-item__title {
  color: #1e293b;
  font-size: 14px;
  line-height: 22px;
}

.activity-item__desc {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
  line-height: 20px;
}

.activity-item__link {
  font-size: 12px;
  font-weight: 700;
}

.quick-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.quick-btn {
  border: 0;
  border-radius: 12px;
  height: 56px;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  font-size: 16px;
  cursor: pointer;
}

.quick-btn__icon {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  background: currentColor;
  opacity: 0.2;
}

.quick-btn--primary {
  background: #005daa;
  color: #ffffff;
  box-shadow: 0 4px 6px -4px rgba(59, 130, 246, 0.2), 0 10px 15px -3px rgba(59, 130, 246, 0.2);
}

.quick-btn--soft-blue {
  background: #afceff;
  color: #385782;
}

.quick-btn--soft-gray {
  background: #f1f5f9;
  color: #334155;
}

@media (max-width: 1100px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .charts-row,
  .bottom-row {
    grid-template-columns: 1fr;
  }

  .panel--activity {
    padding-bottom: 32px;
  }
}

@media (max-width: 640px) {
  .dashboard {
    padding-top: 72px;
    gap: 24px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .panel--revenue,
  .panel--enroll,
  .panel--quick,
  .panel--activity {
    padding: 20px;
    gap: 20px;
  }
}

:global(html.dark) .dashboard {
  background: #0f1216;
}

:global(html.dark) .welcome__title,
:global(html.dark) .panel__title,
:global(html.dark) .stat-card__value {
  color: #e5eaf3;
}

:global(html.dark) .welcome__subtitle,
:global(html.dark) .stat-card__title,
:global(html.dark) .enroll-item__top,
:global(html.dark) .activity-item__title {
  color: #b9c2d0;
}

:global(html.dark) .panel,
:global(html.dark) .stat-card {
  background: #1a2028;
  box-shadow: none;
}

:global(html.dark) .chart-area {
  background: linear-gradient(180deg, rgba(29, 78, 216, 0.08) 0%, rgba(29, 78, 216, 0) 100%);
}
</style>

