<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { Calendar, CollectionTag, DataLine } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageShell from '../../components/PageShell.vue'
import { fetchStudentClassDetail } from '../../api/student/course'

const route = useRoute()

const classInformation = ref({
  classId: route.params.id || '-',
  course: {
    courseName: '',
    teacherName: '',
    cycle: '',
    totalLessons: 0,
    completedLessons: 0,
    nextLessonTime: '',
    createdAt: '-',
    updatedAt: '-'
  },
  statistics: {
    studentCount: 0,
    averageAttendanceRate: 0,
    averageScore: 0
  },
  scheduleList: [],
  classmates: []
})

const loading = ref(false)

const summaryCards = computed(() => [
  {
    key: 'studentCount',
    title: '班级人数',
    value: `${classInformation.value.statistics.studentCount} 人`,
    icon: CollectionTag,
    tone: 'blue'
  },
  {
    key: 'averageAttendanceRate',
    title: '平均出勤率',
    value: `${classInformation.value.statistics.averageAttendanceRate}%`,
    icon: Calendar,
    tone: 'green'
  },
  {
    key: 'averageScore',
    title: '平均成绩',
    value: `${classInformation.value.statistics.averageScore} 分`,
    icon: DataLine,
    tone: 'purple'
  }
])

const lessonProgress = computed(() => {
  const { completedLessons, totalLessons } = classInformation.value.course
  if (!totalLessons) return 0
  return Math.round((completedLessons / totalLessons) * 100)
})

function getScheduleTagType(status) {
  return status === 'DONE' ? 'success' : 'warning'
}

async function loadClassInformation() {
  const numericClassId = Number(route.params.id)
  if (!Number.isFinite(numericClassId)) {
    ElMessage.error('班级编号无效')
    return
  }
  loading.value = true
  try {
    const detail = await fetchStudentClassDetail(numericClassId)
    classInformation.value = {
      classId: detail.classId || '-',
      course: {
        courseName: detail.courseName || '',
        teacherName: detail.teacherName || '',
        cycle: detail.cycle || '',
        totalLessons: Number(detail.totalLessons || 0),
        completedLessons: Number(detail.completedLessons || 0),
        nextLessonTime: detail.nextLessonTime || '',
        createdAt: detail.createdAt || '-',
        updatedAt: detail.updatedAt || '-'
      },
      statistics: {
        studentCount: Number(detail.studentCount || 0),
        averageAttendanceRate: Number(detail.averageAttendanceRate || 0),
        averageScore: Number(detail.averageScore || 0)
      },
      scheduleList: detail.scheduleList || [],
      classmates: detail.classmates || []
    }
  } catch (error) {
    ElMessage.error(error.message || '班级信息加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadClassInformation()
})

watch(
  () => route.params.id,
  () => {
    loadClassInformation()
  }
)
</script>

<template>
  <PageShell :title="`班级信息（ID: ${classInformation.classId}）`" subtitle="课程列表 / 班级信息">
    <div v-loading="loading" class="class-information-page">
      <el-card shadow="never" class="overview-card">
        <div class="overview-header">
          <div class="title-block">
            <h3>{{ classInformation.course.courseName }}</h3>
            <p>授课教师：{{ classInformation.course.teacherName }} ｜ 开课周期：{{ classInformation.course.cycle }}</p>
          </div>
          <el-tag type="primary" effect="light" round>{{ classInformation.course.nextLessonTime || '暂无后续排课' }}</el-tag>
        </div>
        <el-row :gutter="12" class="overview-body">
          <el-col :xs="24" :sm="24" :md="14" :lg="14" :xl="14">
            <el-descriptions :column="2" border size="default">
              <el-descriptions-item label="课程编号">{{ classInformation.classId }}</el-descriptions-item>
              <el-descriptions-item label="任课教师">{{ classInformation.course.teacherName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="开课时间">{{ classInformation.course.createdAt }}</el-descriptions-item>
              <el-descriptions-item label="最近更新">{{ classInformation.course.updatedAt }}</el-descriptions-item>
            </el-descriptions>
          </el-col>
          <el-col :xs="24" :sm="24" :md="10" :lg="10" :xl="10">
            <div class="progress-panel">
              <div class="progress-title">课时进度</div>
              <div class="progress-value">{{ classInformation.course.completedLessons }} / {{ classInformation.course.totalLessons }}</div>
              <el-progress :percentage="lessonProgress" :stroke-width="12" />
            </div>
          </el-col>
        </el-row>
      </el-card>

      <el-row :gutter="12" class="summary-row">
        <el-col v-for="item in summaryCards" :key="item.key" :xs="24" :sm="8" :md="8" :lg="8" :xl="8">
          <el-card shadow="hover" class="summary-card" :class="`tone-${item.tone}`">
            <div class="summary-head">
              <span>{{ item.title }}</span>
              <el-icon><component :is="item.icon" /></el-icon>
            </div>
            <div class="summary-value">{{ item.value }}</div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" class="table-card">
        <template #header>
          <div class="section-header">课程安排</div>
        </template>
        <el-table :data="classInformation.scheduleList" stripe>
          <el-table-column prop="sessionDate" label="日期" min-width="110" />
          <el-table-column label="时间" min-width="130">
            <template #default="{ row }">{{ row.startTime }} - {{ row.endTime }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="getScheduleTagType(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="updatedAt" label="更新时间" min-width="160" />
        </el-table>
      </el-card>

      <el-card shadow="never" class="table-card">
        <template #header>
          <div class="section-header">班级同学概览</div>
        </template>
        <el-table :data="classInformation.classmates" stripe>
          <el-table-column prop="studentNo" label="学号" min-width="120" />
          <el-table-column prop="studentName" label="姓名" min-width="100" />
          <el-table-column label="出勤率" min-width="120">
            <template #default="{ row }">
              <el-progress :percentage="row.attendanceRate" :stroke-width="8" />
            </template>
          </el-table-column>
          <el-table-column prop="latestScore" label="最近测验分" min-width="110" />
          <el-table-column prop="updatedAt" label="更新时间" min-width="160" />
        </el-table>
      </el-card>
    </div>
  </PageShell>
</template>

<style scoped>
.class-information-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.overview-card,
.table-card,
.summary-card {
  border-radius: 12px;
}

.overview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.title-block h3 {
  margin: 0;
  font-size: 20px;
  line-height: 1.3;
}

.title-block p {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.overview-body {
  margin-top: 0;
}

.inline-icon {
  margin-right: 4px;
  vertical-align: -2px;
}

.progress-panel {
  height: 100%;
  min-height: 132px;
  padding: 14px;
  border-radius: 10px;
  border: 1px solid var(--el-border-color-light);
  background: var(--el-fill-color-light);
}

.progress-title {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.progress-value {
  margin: 8px 0 14px;
  font-size: 28px;
  line-height: 1;
  font-weight: 700;
}

.summary-row {
  margin-top: 0;
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
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.summary-head .el-icon {
  font-size: 18px;
}

.summary-value {
  margin-top: 10px;
  font-size: 26px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.tone-blue .el-icon {
  color: #409eff;
}

.tone-green .el-icon {
  color: #67c23a;
}

.tone-purple .el-icon {
  color: #8a65ff;
}

.section-header {
  font-size: 15px;
  font-weight: 700;
}

:global(html.dark) .progress-panel {
  background: #1a2029;
  border-color: #303848;
}

@media (max-width: 992px) {
  .overview-header {
    align-items: flex-start;
    flex-direction: column;
  }
}

@media (max-width: 768px) {
  .progress-value {
    font-size: 24px;
  }
}
</style>

