<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchAdminAttendanceOverview } from '../../api/admin/attendance'

const router = useRouter()
const loading = ref(false)
const overview = ref({
  totalAttendanceCount: 0,
  punctualRate: 0,
  leaveCount: 0,
  exceptionCount: 0,
  trend: [],
  classOverview: [],
  focusAlerts: []
})

const kpiCards = computed(() => [
  { label: '本周总考勤人次', value: overview.value.totalAttendanceCount.toLocaleString(), trend: `${overview.value.trend.length} 天`, tone: 'blue' },
  { label: '准时到课率', value: `${overview.value.punctualRate}%`, trend: '近 7 日', tone: 'green' },
  { label: '请假申请数', value: `${overview.value.leaveCount}`, trend: '近 7 日', tone: 'orange' },
  { label: '异常考勤数', value: `${overview.value.exceptionCount}`, trend: '近 7 日', tone: 'red' }
])

const trendLabels = computed(() => overview.value.trend.map((item) => item.label))
const classRows = computed(() => overview.value.classOverview)
const focusAlerts = computed(() => overview.value.focusAlerts)

const trendPath = computed(() => {
  const points = overview.value.trend.map((item) => item.punctualRate)
  if (!points.length) return ''
  const width = 560
  const height = 180
  const max = Math.max(...points)
  const min = Math.min(...points)
  const span = max - min || 1

  return points
    .map((value, index) => {
      const x = points.length === 1 ? width / 2 : (index / (points.length - 1)) * width
      const y = height - ((value - min) / span) * (height - 30) - 12
      return `${x.toFixed(2)},${y.toFixed(2)}`
    })
    .join(' ')
})

async function loadOverview() {
  loading.value = true
  try {
    overview.value = await fetchAdminAttendanceOverview()
  } catch (error) {
    ElMessage.error('管理员考勤总览加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function viewAbnormal() {
  router.push('/admin/attendance/abnormal')
}

function getRateType(rate) {
  if (rate >= 97) return 'success'
  if (rate >= 95) return 'warning'
  return 'danger'
}

onMounted(() => {
  loadOverview()
})
</script>

<template>
  <div v-loading="loading" class="attendance-page">
    <section class="attendance-head">
      <div>
        <h1 class="attendance-title">考勤管理</h1>
        <p class="attendance-subtitle">管理员端 / 考勤管理</p>
      </div>
      <div class="attendance-actions">
        <el-tag type="info" effect="plain">当前仅支持全校区真实总览</el-tag>
      </div>
    </section>

    <section class="kpi-grid">
      <article v-for="card in kpiCards" :key="card.label" class="kpi-card">
        <div class="kpi-label">{{ card.label }}</div>
        <div class="kpi-main">
          <span class="kpi-value">{{ card.value }}</span>
          <span class="kpi-trend" :class="`kpi-trend--${card.tone}`">{{ card.trend }}</span>
        </div>
      </article>
    </section>

    <section class="panel-grid">
      <article class="panel panel--chart">
        <header class="panel-head">
          <h2>7 日到课率趋势</h2>
          <el-tag round effect="plain">最近 7 天</el-tag>
        </header>
        <div class="chart-box">
          <svg viewBox="0 0 560 220" preserveAspectRatio="none" aria-hidden="true">
            <defs>
              <linearGradient id="attendanceLineFill" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#1d4ed8" stop-opacity="0.24" />
                <stop offset="100%" stop-color="#1d4ed8" stop-opacity="0" />
              </linearGradient>
            </defs>
            <polygon v-if="trendPath" :points="`0,205 ${trendPath} 560,205`" fill="url(#attendanceLineFill)" />
            <polyline v-if="trendPath" :points="trendPath" fill="none" stroke="#1d4ed8" stroke-width="4" stroke-linejoin="round" stroke-linecap="round" />
          </svg>
          <div class="chart-axis">
            <span v-for="label in trendLabels" :key="label">{{ label }}</span>
          </div>
        </div>
      </article>

      <article class="panel panel--alert">
        <header class="panel-head panel-head--single">
          <h2>关注提醒</h2>
        </header>
        <div class="alert-list">
          <div v-for="item in focusAlerts" :key="item.text" class="alert-item">
            <el-tag :type="item.level === '高' ? 'danger' : item.level === '中' ? 'warning' : 'info'" effect="dark" size="small">
              {{ item.level }}
            </el-tag>
            <p>{{ item.text }}</p>
          </div>
        </div>
      </article>
    </section>

    <section class="panel">
      <header class="panel-head panel-head--single">
        <h2>班级考勤概览</h2>
      </header>
      <el-table :data="classRows" stripe>
        <el-table-column prop="className" label="班级名称" min-width="180" />
        <el-table-column prop="teacherName" label="任课教师" width="120" />
        <el-table-column prop="shouldAttend" label="应到" width="100" />
        <el-table-column prop="actualAttend" label="实到" width="100" />
        <el-table-column label="到课率" width="120">
          <template #default="{ row }">
            <el-tag :type="getRateType(row.normalRate)" effect="light">{{ row.normalRate }}%</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="abnormalCount" label="异常人数" width="110" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewAbnormal(row)">查看明细</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<style scoped>
.attendance-page {
  max-width: 1160px;
  margin: 0 auto;
  padding: 8px 4px 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.attendance-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.attendance-title {
  margin: 0;
  font-size: 28px;
  line-height: 38px;
  color: #111827;
}

.attendance-subtitle {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 14px;
}

.attendance-actions {
  display: flex;
  gap: 12px;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.kpi-card {
  border-radius: 14px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  padding: 18px 18px 16px;
}

.kpi-label {
  color: #6b7280;
  font-size: 13px;
}

.kpi-main {
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.kpi-value {
  color: #111827;
  font-size: 28px;
  font-weight: 700;
}

.kpi-trend {
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 700;
}

.kpi-trend--blue {
  background: #dbeafe;
  color: #1d4ed8;
}

.kpi-trend--green {
  background: #dcfce7;
  color: #15803d;
}

.kpi-trend--orange {
  background: #ffedd5;
  color: #c2410c;
}

.kpi-trend--red {
  background: #fee2e2;
  color: #b91c1c;
}

.panel-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
}

.panel {
  border-radius: 14px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  padding: 18px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.panel-head--single {
  justify-content: flex-start;
}

.panel-head h2 {
  margin: 0;
  font-size: 18px;
  color: #111827;
}

.chart-box {
  position: relative;
  height: 220px;
  background: linear-gradient(180deg, rgba(29, 78, 216, 0.1) 0%, rgba(29, 78, 216, 0) 100%);
  border-radius: 10px;
  overflow: hidden;
}

.chart-box svg {
  width: 100%;
  height: 100%;
}

.chart-axis {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 8px;
  padding: 0 12px;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  color: #6b7280;
  font-size: 11px;
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.alert-item {
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.alert-item p {
  margin: 0;
  color: #374151;
  font-size: 14px;
  line-height: 22px;
}

@media (max-width: 1024px) {
  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .panel-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .attendance-head {
    flex-direction: column;
    align-items: stretch;
  }

  .attendance-actions {
    width: 100%;
  }

  .attendance-actions :deep(.el-tag) {
    align-self: flex-start;
  }

  .kpi-grid {
    grid-template-columns: 1fr;
  }
}

:global(html.dark) .attendance-title,
:global(html.dark) .panel-head h2,
:global(html.dark) .kpi-value {
  color: #e5e7eb;
}

:global(html.dark) .attendance-subtitle,
:global(html.dark) .kpi-label,
:global(html.dark) .chart-axis,
:global(html.dark) .alert-item p {
  color: #9ca3af;
}

:global(html.dark) .panel,
:global(html.dark) .kpi-card,
:global(html.dark) .alert-item {
  background: #1f2937;
  border-color: #374151;
}
</style>
