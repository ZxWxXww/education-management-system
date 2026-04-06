<script setup>
import { computed, ref } from 'vue'
import { Calendar, CollectionTag, DataLine, Notebook, Warning } from '@element-plus/icons-vue'
import PageShell from '../../components/PageShell.vue'

// 学生学习中心 Mock 数据（切换真实后端时，请替换为 src/api/student/learningCenter.js 的接口返回）
const learningCenterMock = ref({
  profile: {
    studentNo: 'S20260318',
    name: '李明',
    grade: '高二（3）班',
    advisor: '张老师',
    created_at: '2026-02-18 09:20:00',
    updated_at: '2026-04-06 08:30:00'
  },
  summary: {
    weekCourseCount: 18,
    pendingAssignmentCount: 4,
    attendanceRate: 98.6,
    averageScore: 89.5,
    created_at: '2026-04-06 08:30:00',
    updated_at: '2026-04-06 08:30:00'
  },
  todayCourses: [
    { id: 1, timeRange: '08:00 - 09:40', courseName: '数学', teacher: '王老师', classroom: 'B-302', status: '进行中', created_at: '2026-04-06 08:00:00', updated_at: '2026-04-06 08:30:00' },
    { id: 2, timeRange: '10:10 - 11:50', courseName: '英语', teacher: '刘老师', classroom: 'A-203', status: '待开始', created_at: '2026-04-06 10:10:00', updated_at: '2026-04-06 08:30:00' },
    { id: 3, timeRange: '14:00 - 15:40', courseName: '物理', teacher: '陈老师', classroom: '实验楼-101', status: '待开始', created_at: '2026-04-06 14:00:00', updated_at: '2026-04-06 08:30:00' }
  ],
  assignments: [
    { id: 1, title: '数学函数专题练习', courseName: '数学', deadline: '2026-04-07 22:00', progress: 60, status: '进行中', created_at: '2026-04-05 18:20:00', updated_at: '2026-04-06 07:55:00' },
    { id: 2, title: '英语阅读理解（Unit 8）', courseName: '英语', deadline: '2026-04-08 21:00', progress: 35, status: '进行中', created_at: '2026-04-05 19:00:00', updated_at: '2026-04-06 08:15:00' },
    { id: 3, title: '物理电学实验报告', courseName: '物理', deadline: '2026-04-09 20:00', progress: 85, status: '即将完成', created_at: '2026-04-04 21:30:00', updated_at: '2026-04-06 08:10:00' }
  ],
  notices: [
    { id: 1, level: '重要', title: '月考安排通知', content: '本周五进行月考，请提前 15 分钟到考场。', publishTime: '2026-04-05 16:30', created_at: '2026-04-05 16:30:00', updated_at: '2026-04-05 16:30:00' },
    { id: 2, level: '普通', title: '英语角活动', content: '周三 17:30 在图书馆二层举办英语角交流。', publishTime: '2026-04-04 12:00', created_at: '2026-04-04 12:00:00', updated_at: '2026-04-04 12:00:00' }
  ],
  attendanceAlerts: [
    { id: 1, date: '2026-03-29', courseName: '化学', type: '迟到', comment: '入班时间 08:12', created_at: '2026-03-29 08:20:00', updated_at: '2026-03-29 08:20:00' }
  ]
})

const summaryCards = computed(() => [
  {
    key: 'weekCourseCount',
    title: '本周课程数',
    value: `${learningCenterMock.value.summary.weekCourseCount} 节`,
    icon: Notebook,
    tone: 'blue'
  },
  {
    key: 'pendingAssignmentCount',
    title: '待完成作业',
    value: `${learningCenterMock.value.summary.pendingAssignmentCount} 项`,
    icon: CollectionTag,
    tone: 'orange'
  },
  {
    key: 'attendanceRate',
    title: '本月出勤率',
    value: `${learningCenterMock.value.summary.attendanceRate}%`,
    icon: Calendar,
    tone: 'green'
  },
  {
    key: 'averageScore',
    title: '阶段均分',
    value: `${learningCenterMock.value.summary.averageScore}`,
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
</script>

<template>
  <PageShell title="学生学习中心" subtitle="学习总览 / 课程安排 / 作业进度 / 考勤提醒">
    <div class="learning-center-page">
      <el-row :gutter="16">
        <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
          <el-card shadow="never" class="profile-card">
            <div class="profile-main">
              <div class="profile-title">欢迎回来，{{ learningCenterMock.profile.name }}</div>
              <div class="profile-subtitle">
                学号：{{ learningCenterMock.profile.studentNo }} ｜ 班级：{{ learningCenterMock.profile.grade }} ｜ 班主任：{{ learningCenterMock.profile.advisor }}
              </div>
            </div>
            <div class="profile-time">最近更新时间：{{ learningCenterMock.profile.updated_at }}</div>
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
          <el-card shadow="never" class="panel-card">
            <template #header>
              <div class="panel-header">今日课程安排</div>
            </template>
            <el-timeline>
              <el-timeline-item
                v-for="course in learningCenterMock.todayCourses"
                :key="course.id"
                :timestamp="course.timeRange"
                :type="course.status === '进行中' ? 'primary' : 'info'"
                placement="top"
              >
                <div class="timeline-main">
                  <div class="timeline-title">{{ course.courseName }} · {{ course.classroom }}</div>
                  <div class="timeline-meta">授课教师：{{ course.teacher }}</div>
                </div>
                <el-tag size="small" :type="course.status === '进行中' ? 'success' : 'info'">{{ course.status }}</el-tag>
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
            <el-empty v-if="learningCenterMock.attendanceAlerts.length === 0" description="暂无考勤异常" :image-size="84" />
            <div v-else class="attendance-list">
              <div v-for="item in learningCenterMock.attendanceAlerts" :key="item.id" class="attendance-item">
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
            <div v-for="task in learningCenterMock.assignments" :key="task.id" class="assignment-item">
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
              <div v-for="notice in learningCenterMock.notices" :key="notice.id" class="notice-item">
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

