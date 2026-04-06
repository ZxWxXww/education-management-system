<script setup>
import { computed, ref } from 'vue'
import { Calendar, CollectionTag, DataLine, Location } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import PageShell from '../../components/PageShell.vue'

const route = useRoute()

// 班级信息 Mock 数据（切换真实后端时，请替换为 src/api/student/classInformation.js 的接口返回）
const classInformationMock = ref({
  classId: route.params.id || 'CLS-20260406-01',
  course: {
    courseName: '高二数学强化提升班',
    teacherName: '王晓晨',
    classroom: '教学楼 B 栋 302',
    cycle: '2026 春季学期（2-6 月）',
    totalLessons: 48,
    completedLessons: 31,
    nextLessonTime: '2026-04-08 19:00 - 20:40',
    contactOfficeHour: '周一至周五 16:00 - 17:30',
    created_at: '2026-02-10 10:00:00',
    updated_at: '2026-04-06 09:20:00'
  },
  statistics: {
    studentCount: 36,
    averageAttendanceRate: 97.2,
    averageHomeworkCompletionRate: 92.8,
    averageScore: 88.4,
    created_at: '2026-04-06 09:20:00',
    updated_at: '2026-04-06 09:20:00'
  },
  scheduleList: [
    {
      id: 1,
      lessonNo: '第 32 课时',
      date: '2026-04-08',
      timeRange: '19:00 - 20:40',
      chapter: '函数与导数综合训练',
      homework: '函数压轴题专项（A 卷）',
      status: '未开始',
      created_at: '2026-04-06 09:20:00',
      updated_at: '2026-04-06 09:20:00'
    },
    {
      id: 2,
      lessonNo: '第 31 课时',
      date: '2026-04-05',
      timeRange: '19:00 - 20:40',
      chapter: '导数几何意义与最值问题',
      homework: '导数应用分层作业（B 卷）',
      status: '已完成',
      created_at: '2026-04-05 21:00:00',
      updated_at: '2026-04-05 21:00:00'
    },
    {
      id: 3,
      lessonNo: '第 30 课时',
      date: '2026-04-03',
      timeRange: '19:00 - 20:40',
      chapter: '导数基础回顾与例题讲解',
      homework: '导数计算专项训练',
      status: '已完成',
      created_at: '2026-04-03 21:00:00',
      updated_at: '2026-04-03 21:00:00'
    }
  ],
  classmates: [
    { id: 1, studentNo: 'S20260318', studentName: '李明', attendanceRate: 100, homeworkRate: 95, latestScore: 91, created_at: '2026-02-10 10:00:00', updated_at: '2026-04-06 09:20:00' },
    { id: 2, studentNo: 'S20260320', studentName: '周雨彤', attendanceRate: 97, homeworkRate: 92, latestScore: 89, created_at: '2026-02-10 10:00:00', updated_at: '2026-04-06 09:20:00' },
    { id: 3, studentNo: 'S20260327', studentName: '张博文', attendanceRate: 94, homeworkRate: 88, latestScore: 84, created_at: '2026-02-10 10:00:00', updated_at: '2026-04-06 09:20:00' },
    { id: 4, studentNo: 'S20260333', studentName: '陈思远', attendanceRate: 96, homeworkRate: 91, latestScore: 87, created_at: '2026-02-10 10:00:00', updated_at: '2026-04-06 09:20:00' }
  ]
})

const summaryCards = computed(() => [
  {
    key: 'studentCount',
    title: '班级人数',
    value: `${classInformationMock.value.statistics.studentCount} 人`,
    icon: CollectionTag,
    tone: 'blue'
  },
  {
    key: 'averageAttendanceRate',
    title: '平均出勤率',
    value: `${classInformationMock.value.statistics.averageAttendanceRate}%`,
    icon: Calendar,
    tone: 'green'
  },
  {
    key: 'averageHomeworkCompletionRate',
    title: '作业完成率',
    value: `${classInformationMock.value.statistics.averageHomeworkCompletionRate}%`,
    icon: DataLine,
    tone: 'purple'
  }
])

const lessonProgress = computed(() => {
  const { completedLessons, totalLessons } = classInformationMock.value.course
  return Math.round((completedLessons / totalLessons) * 100)
})

function getScheduleTagType(status) {
  return status === '已完成' ? 'success' : 'warning'
}
</script>

<template>
  <PageShell :title="`班级信息（ID: ${classInformationMock.classId}）`" subtitle="课程列表 / 班级信息">
    <div class="class-information-page">
      <el-card shadow="never" class="overview-card">
        <div class="overview-header">
          <div class="title-block">
            <h3>{{ classInformationMock.course.courseName }}</h3>
            <p>授课教师：{{ classInformationMock.course.teacherName }} ｜ 开课周期：{{ classInformationMock.course.cycle }}</p>
          </div>
          <el-tag type="primary" effect="light" round>{{ classInformationMock.course.nextLessonTime }}</el-tag>
        </div>
        <el-row :gutter="12" class="overview-body">
          <el-col :xs="24" :sm="24" :md="14" :lg="14" :xl="14">
            <el-descriptions :column="2" border size="default">
              <el-descriptions-item label="上课地点">
                <el-icon class="inline-icon"><Location /></el-icon>
                {{ classInformationMock.course.classroom }}
              </el-descriptions-item>
              <el-descriptions-item label="教师答疑">{{ classInformationMock.course.contactOfficeHour }}</el-descriptions-item>
              <el-descriptions-item label="开课时间">{{ classInformationMock.course.created_at }}</el-descriptions-item>
              <el-descriptions-item label="最近更新">{{ classInformationMock.course.updated_at }}</el-descriptions-item>
            </el-descriptions>
          </el-col>
          <el-col :xs="24" :sm="24" :md="10" :lg="10" :xl="10">
            <div class="progress-panel">
              <div class="progress-title">课时进度</div>
              <div class="progress-value">{{ classInformationMock.course.completedLessons }} / {{ classInformationMock.course.totalLessons }}</div>
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
        <el-table :data="classInformationMock.scheduleList" stripe>
          <el-table-column prop="lessonNo" label="课时" min-width="100" />
          <el-table-column prop="date" label="日期" min-width="110" />
          <el-table-column prop="timeRange" label="时间" min-width="130" />
          <el-table-column prop="chapter" label="教学内容" min-width="220" />
          <el-table-column prop="homework" label="课后作业" min-width="200" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="getScheduleTagType(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="updated_at" label="更新时间" min-width="160" />
        </el-table>
      </el-card>

      <el-card shadow="never" class="table-card">
        <template #header>
          <div class="section-header">班级同学概览</div>
        </template>
        <el-table :data="classInformationMock.classmates" stripe>
          <el-table-column prop="studentNo" label="学号" min-width="120" />
          <el-table-column prop="studentName" label="姓名" min-width="100" />
          <el-table-column label="出勤率" min-width="120">
            <template #default="{ row }">
              <el-progress :percentage="row.attendanceRate" :stroke-width="8" />
            </template>
          </el-table-column>
          <el-table-column label="作业完成率" min-width="140">
            <template #default="{ row }">
              <el-progress :percentage="row.homeworkRate" :stroke-width="8" status="success" />
            </template>
          </el-table-column>
          <el-table-column prop="latestScore" label="最近测验分" min-width="110" />
          <el-table-column prop="updated_at" label="更新时间" min-width="160" />
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

