<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell, Clock, DataAnalysis, Search, Warning } from '@element-plus/icons-vue'
import { fetchTeacherAttendanceExceptionPage } from '../../api/teacher/attendance'

const filterForm = ref({
  keyword: '',
  abnormalType: '',
  handleStatus: '',
  dateRange: []
})

const abnormalTypeOptions = [
  { label: '全部类型', value: '' },
  { label: '迟到', value: '迟到' },
  { label: '缺勤', value: '缺勤' },
  { label: '早退', value: '早退' }
]

const handleStatusOptions = [
  { label: '全部状态', value: '' },
  { label: '待处理', value: '待处理' },
  { label: '已处理', value: '已处理' }
]

const attendanceRecords = ref([])
const loading = ref(false)

function getRecordDate(item) {
  return String(item.checkInTime || '').slice(0, 10)
}

const selectedRecord = ref(null)
const drawerVisible = ref(false)

const filteredRecords = computed(() => {
  const keyword = filterForm.value.keyword.trim()
  const [startDate, endDate] = filterForm.value.dateRange || []

  return attendanceRecords.value.filter((item) => {
    const matchKeyword =
      !keyword ||
      item.studentName.includes(keyword) ||
      item.studentNo.includes(keyword) ||
      item.className.includes(keyword)
    const matchType = !filterForm.value.abnormalType || item.abnormalType === filterForm.value.abnormalType
    const matchStatus = !filterForm.value.handleStatus || item.handleStatus === filterForm.value.handleStatus
    const dateValue = getRecordDate(item)
    const matchDate = !startDate || !endDate || (dateValue >= startDate && dateValue <= endDate)
    return matchKeyword && matchType && matchStatus && matchDate
  })
})

const statisticCards = computed(() => {
  const total = filteredRecords.value.length
  const lateCount = filteredRecords.value.filter((item) => item.abnormalType === '迟到').length
  const absentCount = filteredRecords.value.filter((item) => item.abnormalType === '缺勤').length
  const unhandledCount = filteredRecords.value.filter((item) => item.handleStatus === '待处理').length
  return [
    { label: '当前筛选异常数', value: total, icon: Bell, tone: 'primary' },
    { label: '迟到人数', value: lateCount, icon: Clock, tone: 'warning' },
    { label: '缺勤人数', value: absentCount, icon: Warning, tone: 'danger' },
    { label: '待处理', value: unhandledCount, icon: DataAnalysis, tone: 'info' }
  ]
})

const trendRecords = computed(() => {
  const bucketMap = new Map()

  filteredRecords.value.forEach((item) => {
    const dateValue = getRecordDate(item)
    if (!dateValue) return
    bucketMap.set(dateValue, (bucketMap.get(dateValue) || 0) + 1)
  })

  return [...bucketMap.entries()]
    .sort(([left], [right]) => left.localeCompare(right))
    .slice(-7)
    .map(([fullDate, count]) => ({
      fullDate,
      day: fullDate.slice(5),
      count
    }))
})

const trendMax = computed(() => Math.max(1, ...trendRecords.value.map((item) => item.count)))

function resetFilter() {
  filterForm.value = {
    keyword: '',
    abnormalType: '',
    handleStatus: '',
    dateRange: []
  }
}

async function loadAttendanceExceptions() {
  loading.value = true
  try {
    const page = await fetchTeacherAttendanceExceptionPage({ pageNum: 1, pageSize: 200 })
    attendanceRecords.value = page.list
  } catch (error) {
    ElMessage.error('异常考勤加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function openDetail(row) {
  selectedRecord.value = row
  drawerVisible.value = true
}

function getAbnormalTagType(type) {
  if (type === '迟到') return 'warning'
  if (type === '缺勤') return 'danger'
  if (type === '早退') return 'info'
  return ''
}

function getStatusTagType(status) {
  if (status === '已处理') return 'success'
  if (status === '已通知家长') return 'primary'
  return 'warning'
}

onMounted(() => {
  loadAttendanceExceptions()
})
</script>

<template>
  <div class="teacher-abnormal">
    <section class="teacher-abnormal__hero">
      <div>
        <p class="teacher-abnormal__path">考勤记录 / 异常考勤</p>
        <h1 class="teacher-abnormal__title">异常考勤</h1>
        <p class="teacher-abnormal__desc">实时跟踪课堂异常，统一处理迟到、缺勤与审批异常记录</p>
      </div>
      <div class="teacher-abnormal__hero-badge">更新于 {{ attendanceRecords[0]?.updatedAt || '-' }}</div>
    </section>

    <section class="teacher-abnormal__stats">
      <article v-for="card in statisticCards" :key="card.label" class="stat-card">
        <div class="stat-card__label">{{ card.label }}</div>
        <div class="stat-card__value">{{ card.value }}</div>
        <div class="stat-card__icon" :class="`stat-card__icon--${card.tone}`">
          <el-icon :size="16"><component :is="card.icon" /></el-icon>
        </div>
      </article>
    </section>

    <section v-loading="loading" class="panel">
      <header class="panel__head">
        <h2><el-icon><Search /></el-icon> 筛选条件</h2>
      </header>
      <el-form class="filter-form" label-position="top">
        <el-form-item label="关键词">
          <el-input v-model="filterForm.keyword" placeholder="学生姓名 / 学号 / 班级" clearable />
        </el-form-item>
        <el-form-item label="异常类型">
          <el-select v-model="filterForm.abnormalType" placeholder="请选择" clearable>
            <el-option v-for="item in abnormalTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="filterForm.handleStatus" placeholder="请选择" clearable>
            <el-option v-for="item in handleStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item class="filter-form__actions">
          <el-button type="primary" @click="loadAttendanceExceptions">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="teacher-abnormal__content">
      <article class="panel">
        <header class="panel__head">
          <h2><el-icon><Warning /></el-icon> 异常明细</h2>
          <span class="panel__meta">共 {{ filteredRecords.length }} 条</span>
        </header>
        <el-table :data="filteredRecords" stripe>
          <el-table-column prop="id" label="记录编号" min-width="160" />
          <el-table-column prop="studentName" label="学生姓名" min-width="100" />
          <el-table-column prop="studentNo" label="学号" min-width="120" />
          <el-table-column prop="className" label="班级" min-width="130" />
          <el-table-column prop="courseName" label="课程" min-width="150" />
          <el-table-column prop="checkInTime" label="考勤时间" min-width="160" />
          <el-table-column label="异常类型" min-width="110">
            <template #default="{ row }">
              <el-tag :type="getAbnormalTagType(row.abnormalType)" effect="light">{{ row.abnormalType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="处理状态" min-width="120">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.handleStatus)" effect="dark">{{ row.handleStatus }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="openDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </article>

      <article class="panel trend-panel">
        <header class="panel__head">
          <h2><el-icon><DataAnalysis /></el-icon> 最近 7 个异常日期</h2>
        </header>
        <div class="trend-list">
          <div v-for="item in trendRecords" :key="item.fullDate" class="trend-item">
            <div class="trend-item__top">
              <span>{{ item.day }}</span>
              <span>{{ item.count }} 人次</span>
            </div>
            <div class="trend-item__track">
              <div class="trend-item__bar" :style="{ width: `${(item.count / trendMax) * 100}%` }"></div>
            </div>
          </div>
        </div>
      </article>
    </section>

    <el-drawer v-model="drawerVisible" title="异常记录详情" size="420px">
      <div v-if="selectedRecord" class="detail-grid">
        <p><strong>记录编号：</strong>{{ selectedRecord.id }}</p>
        <p><strong>学生姓名：</strong>{{ selectedRecord.studentName }}</p>
        <p><strong>学号：</strong>{{ selectedRecord.studentNo }}</p>
        <p><strong>班级：</strong>{{ selectedRecord.className }}</p>
        <p><strong>课程：</strong>{{ selectedRecord.courseName }}</p>
        <p><strong>考勤时间：</strong>{{ selectedRecord.checkInTime }}</p>
        <p><strong>异常类型：</strong>{{ selectedRecord.abnormalType }}</p>
        <p><strong>处理状态：</strong>{{ selectedRecord.handleStatus }}</p>
        <p><strong>备注：</strong>{{ selectedRecord.remark }}</p>
        <p><strong>创建时间：</strong>{{ selectedRecord.createdAt }}</p>
        <p><strong>更新时间：</strong>{{ selectedRecord.updatedAt }}</p>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.teacher-abnormal {
  max-width: 1160px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.teacher-abnormal__hero {
  border-radius: 16px;
  background: linear-gradient(135deg, #1d4ed8 0%, #1e40af 100%);
  padding: 22px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.teacher-abnormal__path,
.teacher-abnormal__title,
.teacher-abnormal__desc {
  margin: 0;
}

.teacher-abnormal__path {
  color: rgba(255, 255, 255, 0.72);
  font-size: 12px;
  line-height: 18px;
}

.teacher-abnormal__title {
  margin-top: 6px;
  color: #ffffff;
  font-size: 30px;
  line-height: 38px;
  font-weight: 800;
}

.teacher-abnormal__desc {
  margin-top: 8px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.teacher-abnormal__hero-badge {
  border-radius: 999px;
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.16);
  color: #ffffff;
  font-size: 12px;
  line-height: 18px;
  white-space: nowrap;
}

.teacher-abnormal__stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.stat-card {
  position: relative;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  padding: 14px;
}

.stat-card__label {
  color: #6b7280;
  font-size: 12px;
  line-height: 18px;
}

.stat-card__value {
  margin-top: 8px;
  color: #111827;
  font-size: 30px;
  line-height: 38px;
  font-weight: 800;
}

.stat-card__icon {
  position: absolute;
  right: 14px;
  top: 14px;
  width: 30px;
  height: 30px;
  border-radius: 8px;
  display: inline-flex;
  justify-content: center;
  align-items: center;
}

.stat-card__icon--primary {
  background: #dbeafe;
  color: #1d4ed8;
}

.stat-card__icon--warning {
  background: #fef3c7;
  color: #d97706;
}

.stat-card__icon--danger {
  background: #fee2e2;
  color: #dc2626;
}

.stat-card__icon--info {
  background: #e0f2fe;
  color: #0284c7;
}

.panel {
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  padding: 16px;
}

.panel__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.panel__head h2 {
  margin: 0;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #111827;
  font-size: 16px;
}

.panel__meta {
  color: #6b7280;
  font-size: 12px;
}

.filter-form {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.filter-form__actions :deep(.el-form-item__content) {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.teacher-abnormal__content {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 12px;
}

.trend-panel {
  align-self: start;
}

.trend-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.trend-item__top {
  display: flex;
  justify-content: space-between;
  color: #374151;
  font-size: 12px;
  margin-bottom: 6px;
}

.trend-item__track {
  height: 8px;
  border-radius: 999px;
  background: #e5e7eb;
  overflow: hidden;
}

.trend-item__bar {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #f97316 0%, #fb923c 100%);
}

.detail-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-grid p {
  margin: 0;
  color: #374151;
  line-height: 20px;
}

@media (max-width: 1080px) {
  .teacher-abnormal__stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .filter-form {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .teacher-abnormal__content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .teacher-abnormal__hero {
    flex-direction: column;
    padding: 18px;
  }

  .teacher-abnormal__stats,
  .filter-form {
    grid-template-columns: 1fr;
  }
}

:global(html.dark) .stat-card,
:global(html.dark) .panel {
  background: #1a2028;
  border-color: #2b3442;
}

:global(html.dark) .stat-card__value,
:global(html.dark) .panel__head h2 {
  color: #e5eaf3;
}

:global(html.dark) .stat-card__label,
:global(html.dark) .panel__meta,
:global(html.dark) .trend-item__top,
:global(html.dark) .detail-grid p {
  color: #9ba7ba;
}

:global(html.dark) .trend-item__track {
  background: #313b4b;
}
</style>

