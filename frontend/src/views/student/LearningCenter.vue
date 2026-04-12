<script setup>
import { computed, onMounted, ref } from 'vue'
import { Calendar, CollectionTag, DataLine, Notebook, Warning } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import PageShell from '../../components/PageShell.vue'
import { fetchCurrentStudentProfile } from '../../api/student/profile'
import { fetchStudentCoursePage } from '../../api/student/course'
import { fetchStudentAssignmentSubmissionPage } from '../../api/student/assignment'
import { fetchStudentNotificationPage } from '../../api/student/notification'
import { fetchStudentAttendanceExceptionPage } from '../../api/student/attendance'
import { fetchStudentScorePage } from '../../api/student/score'

const profile = ref({
  studentNo: '-',
  name: '同学',
  grade: '-',
  updatedAt: '-'
})
const courseRows = ref([])
const assignmentRows = ref([])
const noticeRows = ref([])
const attendanceRows = ref([])
const scoreRows = ref([])
const loadingCourses = ref(false)

const summaryCards = computed(() => [
  {
    key: 'weekCourseCount',
    title: '课程总数',
    value: `${courseRows.value.length} 节`,
    icon: Notebook,
    tone: 'blue'
  },
  {
    key: 'pendingAssignmentCount',
    title: '待完成作业',
    value: `${assignmentRows.value.filter((item) => item.status === 'pending').length} 项`,
    icon: CollectionTag,
    tone: 'orange'
  },
  {
    key: 'attendanceAlertCount',
    title: '考勤异常',
    value: `${attendanceRows.value.length} 条`,
    icon: Calendar,
    tone: 'green'
  },
  {
    key: 'averageScore',
    title: '阶段均分',
    value: `${scoreRows.value.length ? (scoreRows.value.reduce((sum, item) => sum + Number(item.score || 0), 0) / scoreRows.value.length).toFixed(1) : '0.0'}`,
    icon: DataLine,
    tone: 'purple'
  }
])

function getAssignmentStatusType(status) {
  if (status === '即将完成') return 'success'
  if (status === '进行中') return 'warning'
  return 'info'
}

function getNoticeType(level) {
  return level === '重要' ? 'danger' : 'info'
}

async function loadCourseModule() {
  loadingCourses.value = true
  try {
    const [profileData, coursePage, assignmentPage, noticePage, attendancePage, scorePage] = await Promise.all([
      fetchCurrentStudentProfile(),
      fetchStudentCoursePage({ pageNum: 1, pageSize: 10 }),
      fetchStudentAssignmentSubmissionPage({ pageNum: 1, pageSize: 10 }),
      fetchStudentNotificationPage({ pageNum: 1, pageSize: 10 }),
      fetchStudentAttendanceExceptionPage({ pageNum: 1, pageSize: 10 }),
      fetchStudentScorePage({ pageNum: 1, pageSize: 20 })
    ])
    profile.value = {
      studentNo: profileData.studentNo || '-',
      name: profileData.name || '同学',
      grade: profileData.className || '-',
      updatedAt: profileData.updatedAt || '-'
    }
    courseRows.value = coursePage.list.map((item) => ({
      id: item.id,
      timeRange: item.nextLessonTime || item.schedule || '暂无后续排课',
      courseName: item.courseName || '-',
      teacher: item.teacherName || '-',
      className: item.className || '-',
      status: item.progress > 0 && item.progress < 100 ? '学习中' : item.progress >= 100 ? '已结课' : '待上课'
    }))
    assignmentRows.value = assignmentPage.list.map((item) => ({
      id: item.id,
      title: item.assignmentTitle,
      courseName: item.courseName,
      deadline: item.dueTime,
      progress: item.status === 'submitted' ? 100 : item.status === 'late' ? 90 : item.status === 'pending' ? 30 : 60,
      status: item.status === 'submitted' ? '已提交' : item.status === 'late' ? '逾期提交' : '进行中'
    }))
    noticeRows.value = noticePage.list.map((item) => ({
      id: item.id,
      level: item.noticeType || '普通',
      title: item.title,
      content: item.content,
      publishTime: item.publishTime
    }))
    attendanceRows.value = attendancePage.list.map((item) => ({
      id: item.rawId || item.id,
      date: item.date,
      courseName: item.courseName,
      type: item.abnormalType,
      comment: item.handleNote || item.reason || '-'
    }))
    scoreRows.value = scorePage.list
  } catch (error) {
    ElMessage.error(error.message || '学习中心数据加载失败')
  } finally {
    loadingCourses.value = false
  }
}

onMounted(() => {
  loadCourseModule()
})
</script>

<template>
  <PageShell title="学生学习中心" subtitle="学习总览 / 课程安排 / 作业进度 / 考勤提醒">
    <div class="learning-center-page">
      <el-row :gutter="16">
        <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
          <el-card v-loading="loadingCourses" shadow="never" class="profile-card">
            <div class="profile-main">
              <div class="profile-title">欢迎回来，{{ profile.name }}</div>
              <div class="profile-subtitle">
                学号：{{ profile.studentNo }} ｜ 班级：{{ profile.grade }}
              </div>
            </div>
            <div class="profile-time">最近更新时间：{{ profile.updatedAt }}</div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="section-gap">
        <el-col v-for="card in summaryCards" :key="card.key" :xs="24" :sm="12" :md="12" :lg="6" :xl="6">
          <el-card shadow="hover" class="summary-card" :class="`tone-${card.tone}`">
            <div class="summary-head">
              <span class="summary-title">{{ card.title }}</span>
              <el-icon class="summary-icon"><component :is="card.icon" /></el-icon>
            </div>
            <div class="summary-value">{{ card.value }}</div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="section-gap">
        <el-col :xs="24" :sm="24" :md="24" :lg="14" :xl="14">
          <el-card v-loading="loadingCourses" shadow="never" class="panel-card">
            <template #header>
              <div class="panel-header">今日课程安排</div>
            </template>
            <el-timeline>
              <el-timeline-item
                v-for="course in courseRows"
                :key="course.id"
                :timestamp="course.timeRange"
                :type="course.status === '进行中' ? 'primary' : 'info'"
                placement="top"
              >
                <div class="timeline-main">
                  <div class="timeline-title">{{ course.courseName }} · {{ course.className }}</div>
                  <div class="timeline-meta">授课教师：{{ course.teacher }}</div>
                </div>
                <el-tag size="small" :type="course.status === '学习中' ? 'success' : 'info'">{{ course.status }}</el-tag>
              </el-timeline-item>
            </el-timeline>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="24" :md="24" :lg="10" :xl="10">
          <el-card shadow="never" class="panel-card">
            <template #header>
              <div class="panel-header">
                <span>考勤提醒</span>
                <el-icon><Warning /></el-icon>
              </div>
            </template>
            <el-empty v-if="attendanceRows.length === 0" description="暂无考勤异常" :image-size="84" />
            <div v-else class="attendance-list">
              <div v-for="item in attendanceRows" :key="item.id" class="attendance-item">
                <div class="attendance-date">{{ item.date }}</div>
                <div class="attendance-content">
                  <div>{{ item.courseName }} · {{ item.type }}</div>
                  <div class="attendance-comment">{{ item.comment }}</div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="section-gap">
        <el-col :xs="24" :sm="24" :md="24" :lg="14" :xl="14">
          <el-card shadow="never" class="panel-card">
            <template #header>
              <div class="panel-header">作业完成进度</div>
            </template>
            <div v-for="task in assignmentRows" :key="task.id" class="assignment-item">
              <div class="assignment-head">
                <div class="assignment-title">{{ task.title }}</div>
                <el-tag size="small" :type="getAssignmentStatusType(task.status)">{{ task.status }}</el-tag>
              </div>
              <div class="assignment-meta">课程：{{ task.courseName }} ｜ 截止：{{ task.deadline }}</div>
              <el-progress :percentage="task.progress" :stroke-width="10" :show-text="true" />
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="24" :md="24" :lg="10" :xl="10">
          <el-card shadow="never" class="panel-card">
            <template #header>
              <div class="panel-header">通知公告</div>
            </template>
            <div class="notice-list">
              <div v-for="notice in noticeRows" :key="notice.id" class="notice-item">
                <div class="notice-top">
                  <div class="notice-title">{{ notice.title }}</div>
                  <el-tag size="small" :type="getNoticeType(notice.level)">{{ notice.level }}</el-tag>
                </div>
                <div class="notice-content">{{ notice.content }}</div>
                <div class="notice-time">{{ notice.publishTime }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </PageShell>
</template>

<style scoped>
.learning-center-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-gap {
  margin-top: 0;
}

.profile-card {
  border-radius: 12px;
}

.profile-main {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.profile-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.profile-subtitle {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.profile-time {
  margin-top: 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.summary-card {
  border-radius: 12px;
  transition: transform 0.2s ease;
}

.summary-card:hover {
  transform: translateY(-2px);
}

.summary-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.summary-title {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.summary-icon {
  font-size: 20px;
}

.summary-value {
  margin-top: 12px;
  font-size: 28px;
  line-height: 1.2;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.tone-blue .summary-icon {
  color: #409eff;
}

.tone-orange .summary-icon {
  color: #e6a23c;
}

.tone-green .summary-icon {
  color: #67c23a;
}

.tone-purple .summary-icon {
  color: #8a65ff;
}

.panel-card {
  height: 100%;
  border-radius: 12px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 15px;
  font-weight: 700;
}

.timeline-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 6px;
}

.timeline-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.timeline-meta {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.attendance-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.attendance-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: var(--el-fill-color-light);
}

.attendance-date {
  min-width: 80px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.attendance-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 14px;
}

.attendance-comment {
  color: var(--el-text-color-secondary);
}

.assignment-item + .assignment-item {
  margin-top: 16px;
}

.assignment-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.assignment-title {
  font-size: 14px;
  font-weight: 600;
}

.assignment-meta {
  margin: 6px 0 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-item {
  padding: 12px;
  border-radius: 8px;
  border: 1px solid var(--el-border-color-light);
}

.notice-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.notice-title {
  font-size: 14px;
  font-weight: 600;
}

.notice-content {
  margin-top: 8px;
  line-height: 1.5;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.notice-time {
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

@media (max-width: 1200px) {
  .summary-value {
    font-size: 24px;
  }
}

@media (max-width: 768px) {
  .profile-title {
    font-size: 18px;
  }

  .profile-subtitle {
    line-height: 1.6;
  }
}
</style>

