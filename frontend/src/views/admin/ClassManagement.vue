<script setup>
import { computed, ref } from 'vue'
import { ArrowLeft, ArrowRight, Calendar, Filter, Plus, MoreFilled } from '@element-plus/icons-vue'

// 班级管理页面 mock 数据（后续切换真实接口时，可在 src/api/admin/class.js 中替换为 axios 请求）
const currentDateLabel = ref('05/20/2024')
const currentWeekLabel = ref('2024年5月第3周')
const currentWeekTab = ref('week')
const courseFilter = ref('所有状态')

const weekDays = [
  { key: 'monday', label: '周一', date: '20' },
  { key: 'tuesday', label: '周二', date: '21' },
  { key: 'wednesday', label: '周三', date: '22' },
  { key: 'thursday', label: '周四', date: '23' },
  { key: 'friday', label: '周五', date: '24' },
  { key: 'saturday', label: '周六', date: '25' },
  { key: 'sunday', label: '周日', date: '26' }
]

const weeklySchedules = [
  { day: 'monday', time: '09:00 - 10:30', name: '高级UI设计 A班', room: '教室 302', tone: 'blue' },
  { day: 'monday', time: '14:00 - 15:30', name: '前端工程化 C班', room: '教室 101', tone: 'purple' },
  { day: 'tuesday', time: '10:00 - 12:00', name: 'Python 入门 B班', room: '教室 405', tone: 'green' },
  { day: 'wednesday', time: '09:00 - 10:30', name: '高级UI设计 A班', room: '教室 302', tone: 'blue' },
  { day: 'thursday', time: '13:30 - 15:00', name: '英语口语强化 D班', room: '多媒体中心', tone: 'orange' },
  { day: 'friday', time: '09:00 - 10:30', name: '高级UI设计 A班', room: '教室 302', tone: 'blue' }
]

const classList = [
  { name: '高级UI设计 2024A', teacher: '张博学', capacity: 30, enrolled: 24, status: '进行中' },
  { name: 'Python 入门 B班', teacher: '李开发', capacity: 30, enrolled: 12, status: '招生中' },
  { name: '前端架构 C班', teacher: '王代码', capacity: 40, enrolled: 40, status: '已满员' }
]

const studentFeeds = [
  { name: '陈星宇', action: '刚刚签到成功', course: 'UI A班', tone: 'blue' },
  { name: '周子涵', action: '2分钟前 提交了作品', course: 'Python B班', tone: 'blue' },
  { name: '林木木', action: '15分钟前 请了病假', course: '前端 C班', tone: 'red' },
  { name: '赵雅琪', action: '1小时前 完成学案文档', course: 'UI A班', tone: 'blue' },
  { name: '孙志勇', action: '2小时前 缺考/迟到', course: '访客', tone: 'blue' }
]

const attendanceColumns = [66, 90, 72, 84, 80]

const groupedScheduleMap = computed(() =>
  weekDays.reduce((acc, day) => {
    acc[day.key] = weeklySchedules.filter((schedule) => schedule.day === day.key)
    return acc
  }, {})
)

function classProgress(item) {
  return Math.min(100, Math.round((item.enrolled / item.capacity) * 100))
}

function statusTone(status) {
  if (status === '进行中') return 'running'
  if (status === '招生中') return 'recruiting'
  return 'full'
}
</script>

<template>
  <div class="class-management">
    <section class="cm-header">
      <div>
        <h1 class="cm-title">班级课程管理</h1>
        <p class="cm-subtitle">实时增控全校班级状态与排课进度</p>
      </div>
      <div class="cm-toolbar">
        <el-button class="tool-btn" type="default">
          <el-icon><Calendar /></el-icon>
          <span>{{ currentDateLabel }}</span>
        </el-button>
        <el-dropdown trigger="click">
          <el-button class="tool-btn" type="default">
            <el-icon><Filter /></el-icon>
            <span>{{ courseFilter }}</span>
          </el-button>
        </el-dropdown>
        <el-button class="tool-btn tool-btn--primary" type="primary">
          <el-icon><Plus /></el-icon>
          <span>新建班级</span>
        </el-button>
      </div>
    </section>

    <section class="cm-main-grid">
      <article class="board-card">
        <header class="board-head">
          <div class="board-week">
            <el-button text circle><el-icon><ArrowLeft /></el-icon></el-button>
            <strong>{{ currentWeekLabel }}</strong>
            <el-button text circle><el-icon><ArrowRight /></el-icon></el-button>
          </div>
          <div class="board-tabs">
            <button
              type="button"
              class="tab-btn"
              :class="{ 'tab-btn--active': currentWeekTab === 'week' }"
              @click="currentWeekTab = 'week'"
            >
              周视图
            </button>
            <button
              type="button"
              class="tab-btn"
              :class="{ 'tab-btn--active': currentWeekTab === 'month' }"
              @click="currentWeekTab = 'month'"
            >
              月视图
            </button>
          </div>
        </header>
        <div class="calendar-grid">
          <div v-for="day in weekDays" :key="day.key" class="calendar-column">
            <div class="calendar-top">
              <div class="calendar-day">{{ day.label }}</div>
              <div class="calendar-date">{{ day.date }}</div>
            </div>
            <div class="schedule-stack">
              <article
                v-for="(schedule, idx) in groupedScheduleMap[day.key]"
                :key="`${day.key}-${idx}`"
                class="schedule-card"
                :class="`schedule-card--${schedule.tone}`"
              >
                <p class="schedule-time">{{ schedule.time }}</p>
                <p class="schedule-name">{{ schedule.name }}</p>
                <p class="schedule-room">{{ schedule.room }}</p>
              </article>
            </div>
          </div>
        </div>
      </article>

      <div class="right-column">
        <article class="panel-card">
          <header class="panel-head">
            <h2>班级列表</h2>
            <el-link type="primary" :underline="false">查看全部</el-link>
          </header>
          <div class="class-list">
            <article v-for="item in classList" :key="item.name" class="class-item">
              <div class="class-item__head">
                <h3>{{ item.name }}</h3>
                <span class="status-chip" :class="`status-chip--${statusTone(item.status)}`">{{ item.status }}</span>
              </div>
              <p class="class-item__teacher">讲师：{{ item.teacher }}</p>
              <div class="class-item__progress">
                <div class="class-item__bar" :style="{ width: `${classProgress(item)}%` }"></div>
              </div>
              <div class="class-item__foot">
                <span>{{ item.enrolled }}/{{ item.capacity }}人</span>
                <el-button text circle><el-icon><ArrowRight /></el-icon></el-button>
              </div>
            </article>
          </div>
        </article>

        <article class="panel-card">
          <header class="panel-head">
            <h2>学员动态</h2>
            <el-button text circle><el-icon><MoreFilled /></el-icon></el-button>
          </header>
          <div class="feed-list">
            <article v-for="feed in studentFeeds" :key="`${feed.name}-${feed.action}`" class="feed-item">
              <span class="avatar-dot">{{ feed.name.slice(0, 1) }}</span>
              <div class="feed-item__content">
                <p class="feed-item__title">{{ feed.name }}</p>
                <p class="feed-item__desc">{{ feed.action }}</p>
              </div>
              <span class="feed-item__tag" :class="`feed-item__tag--${feed.tone}`">{{ feed.course }}</span>
            </article>
          </div>
        </article>
      </div>
    </section>

    <section class="attendance-card">
      <div class="attendance-left">
        <span class="attendance-icon">✓</span>
        <div>
          <p class="attendance-label">本周出勤率</p>
          <p class="attendance-value">94.2%</p>
        </div>
      </div>
      <div class="attendance-right">
        <span v-for="(height, index) in attendanceColumns" :key="`attend-${index}`" class="attendance-bar" :style="{ height: `${height}%` }"></span>
      </div>
    </section>
  </div>
</template>

<style scoped>
.class-management {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 1040px;
  margin: 0 auto;
  color: #0f172a;
}

.cm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.cm-title {
  margin: 0;
  font-size: 44px;
  line-height: 1.2;
  font-weight: 800;
}

.cm-subtitle {
  margin: 6px 0 0;
  color: #4b5563;
  font-size: 18px;
}

.cm-toolbar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.tool-btn {
  border-radius: 12px;
  height: 44px;
  padding: 0 16px;
  font-weight: 600;
}

.tool-btn--primary {
  border: none;
  background: #0068d6;
}

.cm-main-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 16px;
}

.board-card,
.panel-card,
.attendance-card {
  border-radius: 16px;
  background: #ffffff;
  border: 1px solid #eef2f7;
}

.board-card {
  padding: 20px;
}

.board-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.board-week {
  display: flex;
  align-items: center;
  gap: 10px;
}

.board-week strong {
  font-size: 20px;
  font-weight: 800;
}

.board-tabs {
  padding: 4px;
  border-radius: 10px;
  background: #f3f5f9;
  display: inline-flex;
  gap: 4px;
}

.tab-btn {
  border: none;
  background: transparent;
  border-radius: 8px;
  height: 30px;
  padding: 0 14px;
  color: #6b7280;
  font-weight: 600;
  cursor: pointer;
}

.tab-btn--active {
  background: #ffffff;
  color: #0068d6;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.08);
}

.calendar-grid {
  margin-top: 16px;
  border-radius: 12px;
  border: 1px solid #eef2f7;
  overflow: hidden;
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  min-height: 450px;
}

.calendar-column {
  padding: 14px 10px;
  border-right: 1px solid #f1f5f9;
}

.calendar-column:last-child {
  border-right: none;
}

.calendar-top {
  margin-bottom: 14px;
}

.calendar-day {
  color: #6b7280;
  font-size: 12px;
}

.calendar-date {
  margin-top: 2px;
  font-size: 38px;
  line-height: 1.05;
  font-weight: 800;
  color: #111827;
}

.schedule-stack {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.schedule-card {
  border-radius: 10px;
  padding: 10px;
  border-left: 4px solid #3b82f6;
  background: #eff6ff;
}

.schedule-card--green {
  border-left-color: #10b981;
  background: #ecfdf5;
}

.schedule-card--orange {
  border-left-color: #f97316;
  background: #fff7ed;
}

.schedule-card--purple {
  border-left-color: #6366f1;
  background: #eef2ff;
}

.schedule-time,
.schedule-name,
.schedule-room {
  margin: 0;
}

.schedule-time {
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
}

.schedule-name {
  margin-top: 2px;
  font-size: 15px;
  font-weight: 700;
  color: #1f2937;
}

.schedule-room {
  margin-top: 3px;
  color: #6b7280;
  font-size: 12px;
}

.right-column {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel-card {
  padding: 18px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-head h2 {
  margin: 0;
  font-size: 30px;
  font-weight: 800;
}

.class-list {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.class-item {
  border-radius: 12px;
  border: 1px solid #eef2f7;
  padding: 12px;
}

.class-item__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.class-item__head h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.status-chip {
  border-radius: 999px;
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 700;
}

.status-chip--running {
  color: #0f9f6e;
  background: #ecfdf3;
}

.status-chip--recruiting {
  color: #2563eb;
  background: #eff6ff;
}

.status-chip--full {
  color: #6b7280;
  background: #f3f4f6;
}

.class-item__teacher {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 13px;
}

.class-item__progress {
  margin-top: 10px;
  height: 6px;
  border-radius: 999px;
  background: #eef2f7;
  overflow: hidden;
}

.class-item__bar {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #0068d6 0%, #3b82f6 100%);
}

.class-item__foot {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #334155;
  font-size: 13px;
  font-weight: 600;
}

.feed-list {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.feed-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar-dot {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0f172a 0%, #334155 100%);
  color: #ffffff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.feed-item__content {
  flex: 1;
}

.feed-item__title,
.feed-item__desc {
  margin: 0;
}

.feed-item__title {
  font-size: 14px;
  font-weight: 700;
  color: #111827;
}

.feed-item__desc {
  margin-top: 1px;
  color: #64748b;
  font-size: 12px;
}

.feed-item__tag {
  font-size: 12px;
  font-weight: 700;
}

.feed-item__tag--red {
  color: #ef4444;
}

.feed-item__tag--blue {
  color: #2563eb;
}

.attendance-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
}

.attendance-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.attendance-icon {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: #dbeafe;
  color: #1d4ed8;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
}

.attendance-label {
  margin: 0;
  font-size: 14px;
  color: #475569;
}

.attendance-value {
  margin: 2px 0 0;
  font-size: 36px;
  line-height: 1;
  color: #0068d6;
  font-weight: 800;
}

.attendance-right {
  display: flex;
  gap: 8px;
  align-items: flex-end;
}

.attendance-bar {
  width: 30px;
  border-radius: 8px 8px 0 0;
  background: linear-gradient(180deg, #93c5fd 0%, #0068d6 100%);
}

@media (max-width: 1280px) {
  .cm-main-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .cm-title {
    font-size: 32px;
  }

  .cm-subtitle {
    font-size: 14px;
  }

  .calendar-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .class-management {
    gap: 12px;
  }

  .calendar-grid {
    grid-template-columns: 1fr;
  }

  .panel-head h2 {
    font-size: 24px;
  }

  .class-item__head h3 {
    font-size: 16px;
  }

  .attendance-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
  }
}

:global(html.dark) .class-management {
  color: #e2e8f0;
}

:global(html.dark) .board-card,
:global(html.dark) .panel-card,
:global(html.dark) .attendance-card {
  background: #111827;
  border-color: #1f2937;
}

:global(html.dark) .calendar-grid,
:global(html.dark) .calendar-column,
:global(html.dark) .class-item {
  border-color: #1f2937;
}

:global(html.dark) .cm-subtitle,
:global(html.dark) .calendar-day,
:global(html.dark) .class-item__teacher,
:global(html.dark) .feed-item__desc,
:global(html.dark) .attendance-label {
  color: #94a3b8;
}

:global(html.dark) .calendar-date,
:global(html.dark) .schedule-name,
:global(html.dark) .panel-head h2,
:global(html.dark) .class-item__head h3,
:global(html.dark) .feed-item__title {
  color: #e2e8f0;
}

:global(html.dark) .board-tabs {
  background: #0b1220;
}

:global(html.dark) .tab-btn--active {
  background: #1e293b;
}
</style>

