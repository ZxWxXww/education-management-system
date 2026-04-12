<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { Calendar, DataAnalysis, DocumentChecked, Histogram, Reading, UserFilled } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchTeacherClassDetail } from '../../api/teacher/class'

const route = useRoute()
const classDetail = ref({
  classId: '-',
  className: '',
  courseName: '',
  schedule: '',
  teacherName: '',
  teacherTitle: '',
  subject: '',
  createdAt: '-',
  updatedAt: '-',
  studentCount: 0,
  sessionCount: 0,
  attendanceRate: 0,
  averageScore: 0,
  recentSessions: [],
  students: []
})
const loading = ref(false)
const studentKeyword = ref('')

const filteredStudents = computed(() => {
  const keyword = studentKeyword.value.trim()
  if (!keyword) return classDetail.value.students || []
  return classDetail.value.students.filter(
    (item) => item.studentName.includes(keyword) || item.studentNo.includes(keyword)
  )
})

const statisticCards = computed(() => [
  { key: 'students', label: '在班学员', value: classDetail.value.studentCount, unit: '人', icon: 'users' },
  { key: 'attendance', label: '累计出勤率', value: classDetail.value.attendanceRate, unit: '%', icon: 'calendar' },
  { key: 'sessions', label: '累计课次', value: classDetail.value.sessionCount, unit: '节', icon: 'homework' },
  { key: 'score', label: '平均成绩', value: classDetail.value.averageScore, unit: '分', icon: 'score' }
])

const insightRows = computed(() => [
  { label: '班级编号', value: classDetail.value.classId || '-' },
  { label: '课程名称', value: classDetail.value.courseName || '-' },
  { label: '授课教师', value: classDetail.value.teacherName || '-' },
  { label: '教师职称', value: classDetail.value.teacherTitle || '-' },
  { label: '任教学科', value: classDetail.value.subject || '-' },
  { label: '开班周期', value: classDetail.value.schedule || '-' },
  { label: '创建时间', value: classDetail.value.createdAt || '-' },
  { label: '更新时间', value: classDetail.value.updatedAt || '-' }
])

function enrollmentStatusType(status) {
  if (status === '在班') return 'success'
  return 'info'
}

function sessionTagType(status) {
  if (status === 'DONE') return 'success'
  if (status === 'IN_PROGRESS') return 'primary'
  return 'info'
}

function cardIcon(icon) {
  if (icon === 'users') return UserFilled
  if (icon === 'calendar') return Calendar
  if (icon === 'homework') return DocumentChecked
  return DataAnalysis
}

async function loadClassDetail() {
  const numericClassId = Number(route.params.id)
  if (!Number.isFinite(numericClassId)) {
    ElMessage.error('班级编号无效')
    return
  }
  loading.value = true
  try {
    classDetail.value = {
      ...(await fetchTeacherClassDetail(numericClassId))
    }
  } catch (error) {
    ElMessage.error(error.message || '班级详情加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadClassDetail()
})

watch(
  () => route.params.id,
  () => {
    loadClassDetail()
  }
)
</script>

<template>
  <div v-loading="loading" class="class-details">
    <section class="details-hero">
      <div>
        <p class="details-hero__path">我的课程 / 班级详情</p>
        <h1 class="details-hero__title">{{ classDetail.className }}</h1>
        <p class="details-hero__meta">{{ classDetail.courseName }} · {{ classDetail.schedule || '未设置开班周期' }}</p>
        <p class="details-hero__desc">以下信息均来自当前班级的真实排课、出勤和成绩记录</p>
      </div>
      <div class="details-hero__badge">
        <span>{{ classDetail.classId }}</span>
        <strong>{{ classDetail.teacherName || '未分配教师' }}</strong>
        <small>{{ classDetail.teacherTitle || '未设置职称' }}</small>
      </div>
    </section>

    <section class="details-stat-grid">
      <article v-for="card in statisticCards" :key="card.key" class="stat-card">
        <div class="stat-card__head">
          <span>{{ card.label }}</span>
          <el-icon><component :is="cardIcon(card.icon)" /></el-icon>
        </div>
        <p class="stat-card__value">{{ card.value }}<small>{{ card.unit }}</small></p>
        <p class="stat-card__trend">来源于真实班级数据汇总</p>
      </article>
    </section>

    <section class="details-main-grid">
      <article class="panel">
        <header class="panel__head">
          <h2><el-icon><Histogram /></el-icon> 班级概览</h2>
          <span>{{ insightRows.length }} 项</span>
        </header>
        <div class="session-list">
          <div v-for="item in insightRows" :key="item.label" class="session-item">
            <div>
              <p class="session-item__topic">{{ item.label }}</p>
              <p class="session-item__text">{{ item.value }}</p>
            </div>
          </div>
        </div>
      </article>

      <article class="panel">
        <header class="panel__head">
          <h2><el-icon><Reading /></el-icon> 最近课次</h2>
          <span>{{ classDetail.recentSessions.length }} 节</span>
        </header>
        <div class="session-list">
          <div v-for="item in classDetail.recentSessions" :key="item.id" class="session-item">
            <div>
              <p class="session-item__date">{{ item.sessionDate }}</p>
              <p class="session-item__topic">{{ item.sessionTitle }}</p>
              <p class="session-item__text">{{ item.startTime }} - {{ item.endTime }}</p>
              <p class="session-item__text">{{ item.remark || '无额外备注' }}</p>
            </div>
            <el-tag :type="sessionTagType(item.status)" effect="plain">{{ item.status }}</el-tag>
          </div>
          <el-empty v-if="classDetail.recentSessions.length === 0" description="暂无课次记录" :image-size="72" />
        </div>
      </article>
    </section>

    <section class="panel">
      <header class="panel__head panel__head--with-action">
        <h2><el-icon><UserFilled /></el-icon> 学员名单</h2>
        <el-input v-model="studentKeyword" class="student-search" clearable placeholder="搜索姓名或学号" />
      </header>

      <el-table :data="filteredStudents" stripe>
        <el-table-column prop="studentNo" label="学号" min-width="120" />
        <el-table-column prop="studentName" label="姓名" min-width="100" />
        <el-table-column prop="grade" label="年级" min-width="90" />
        <el-table-column prop="attendanceRate" label="出勤率" min-width="110">
          <template #default="{ row }">{{ row.attendanceRate }}%</template>
        </el-table-column>
        <el-table-column prop="attendanceCount" label="考勤记录" min-width="110" />
        <el-table-column prop="averageScore" label="平均成绩" min-width="100" />
        <el-table-column prop="joinedAt" label="入班时间" min-width="160" />
        <el-table-column prop="enrollmentStatus" label="在班状态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="enrollmentStatusType(row.enrollmentStatus)" effect="light">{{ row.enrollmentStatus }}</el-tag>
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

