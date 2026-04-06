<script setup>
import { computed, ref } from 'vue'
import { Calendar, Plus, Search, Setting, UserFilled } from '@element-plus/icons-vue'

// 班级管理页 Mock 数据（切换真实后端时，请在 src/api/teacher/class.js 中封装接口并替换）
const queryForm = ref({
  keyword: '',
  grade: '全部年级',
  status: '全部状态'
})

const gradeOptions = ['全部年级', '高一', '高二', '高三']
const statusOptions = ['全部状态', '进行中', '待开课', '已结课']

const classStatistics = [
  { key: 'total', label: '班级总数', value: 12, unit: '个' },
  { key: 'running', label: '进行中', value: 8, unit: '个' },
  { key: 'students', label: '在读学员', value: 356, unit: '人' },
  { key: 'attendance', label: '本周平均出勤', value: 95.3, unit: '%' }
]

const classList = [
  {
    classId: 'CLS-2026-001',
    className: '高二数学提高班',
    courseName: '函数与导数',
    grade: '高二',
    schedule: '每周一/三 08:30-10:00',
    classroom: 'B-302',
    studentCount: 45,
    maxStudentCount: 48,
    attendanceRate: 96.4,
    status: '进行中',
    created_at: '2026-02-20 09:00:00',
    updated_at: '2026-04-06 08:45:00'
  },
  {
    classId: 'CLS-2026-006',
    className: '高二数学冲刺班',
    courseName: '数列与不等式',
    grade: '高二',
    schedule: '每周二/四 13:30-15:00',
    classroom: 'A-201',
    studentCount: 42,
    maxStudentCount: 45,
    attendanceRate: 94.7,
    status: '进行中',
    created_at: '2026-02-24 10:00:00',
    updated_at: '2026-04-06 08:30:00'
  },
  {
    classId: 'CLS-2026-010',
    className: '高一数学基础巩固班',
    courseName: '集合与函数概念',
    grade: '高一',
    schedule: '每周五 19:00-20:30',
    classroom: '线上直播',
    studentCount: 38,
    maxStudentCount: 50,
    attendanceRate: 91.6,
    status: '待开课',
    created_at: '2026-03-03 14:20:00',
    updated_at: '2026-04-05 21:15:00'
  },
  {
    classId: 'CLS-2025-021',
    className: '高三数学复盘班',
    courseName: '真题专题训练',
    grade: '高三',
    schedule: '每周日 09:00-11:00',
    classroom: 'C-105',
    studentCount: 40,
    maxStudentCount: 40,
    attendanceRate: 97.1,
    status: '已结课',
    created_at: '2025-09-01 09:00:00',
    updated_at: '2026-01-20 11:30:00'
  }
]

const pendingTodo = [
  { title: '高二数学提高班：第6章周测待发布', level: 'high' },
  { title: '高二数学冲刺班：2名学员连续两次缺勤', level: 'warn' },
  { title: '高一数学基础巩固班：课件需补充例题', level: 'normal' }
]

const filteredClassList = computed(() =>
  classList.filter((item) => {
    const keyword = queryForm.value.keyword.trim()
    const matchKeyword =
      !keyword ||
      item.className.includes(keyword) ||
      item.classId.includes(keyword) ||
      item.courseName.includes(keyword)
    const matchGrade = queryForm.value.grade === '全部年级' || queryForm.value.grade === item.grade
    const matchStatus = queryForm.value.status === '全部状态' || queryForm.value.status === item.status
    return matchKeyword && matchGrade && matchStatus
  })
)

function resetQuery() {
  queryForm.value.keyword = ''
  queryForm.value.grade = '全部年级'
  queryForm.value.status = '全部状态'
}

function statusType(status) {
  if (status === '进行中') return 'success'
  if (status === '待开课') return 'warning'
  return 'info'
}

function todoType(level) {
  if (level === 'high') return 'danger'
  if (level === 'warn') return 'warning'
  return 'info'
}
</script>

<template>
  <div class="class-management">
    <section class="management-hero">
      <div>
        <p class="management-hero__path">我的课程 / 班级管理</p>
        <h1 class="management-hero__title">班级管理</h1>
        <p class="management-hero__desc">集中管理授课班级、课表进度与学员学习状态</p>
      </div>
      <div class="management-hero__actions">
        <el-button type="primary" class="hero-btn">
          <el-icon><Plus /></el-icon>
          <span>新建班级</span>
        </el-button>
        <el-button class="hero-btn" plain>
          <el-icon><Setting /></el-icon>
          <span>排课设置</span>
        </el-button>
      </div>
    </section>

    <section class="statistics-grid">
      <article v-for="item in classStatistics" :key="item.key" class="statistics-card">
        <p class="statistics-card__label">{{ item.label }}</p>
        <p class="statistics-card__value">{{ item.value }}<small>{{ item.unit }}</small></p>
      </article>
    </section>

    <section class="panel">
      <header class="panel__head">
        <h2><el-icon><Search /></el-icon> 班级筛选</h2>
      </header>
      <el-form :inline="true" class="query-form">
        <el-form-item>
          <el-input
            v-model="queryForm.keyword"
            clearable
            placeholder="输入班级名称 / 课程 / 班级ID"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryForm.grade" style="width: 140px">
            <el-option v-for="item in gradeOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryForm.status" style="width: 140px">
            <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="content-grid">
      <article class="panel">
        <header class="panel__head panel__head--table">
          <h2><el-icon><Calendar /></el-icon> 班级列表</h2>
          <span>共 {{ filteredClassList.length }} 个班级</span>
        </header>
        <el-table :data="filteredClassList" stripe>
          <el-table-column prop="classId" label="班级ID" min-width="120" />
          <el-table-column prop="className" label="班级名称" min-width="180" />
          <el-table-column prop="courseName" label="课程" min-width="140" />
          <el-table-column prop="grade" label="年级" min-width="90" />
          <el-table-column prop="schedule" label="上课时间" min-width="170" />
          <el-table-column prop="classroom" label="教室" min-width="100" />
          <el-table-column label="人数" min-width="110">
            <template #default="{ row }">{{ row.studentCount }}/{{ row.maxStudentCount }}</template>
          </el-table-column>
          <el-table-column label="出勤率" min-width="90">
            <template #default="{ row }">{{ row.attendanceRate }}%</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" min-width="100">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)" effect="light">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="120" fixed="right">
            <template #default>
              <el-button link type="primary">查看详情</el-button>
              <el-button link type="primary">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </article>

      <article class="panel panel--aside">
        <header class="panel__head">
          <h2><el-icon><UserFilled /></el-icon> 班级待办</h2>
          <span>{{ pendingTodo.length }} 条</span>
        </header>
        <div class="todo-list">
          <div v-for="item in pendingTodo" :key="item.title" class="todo-item">
            <el-tag size="small" :type="todoType(item.level)" effect="plain">
              {{ item.level === 'high' ? '高优先' : item.level === 'warn' ? '提醒' : '常规' }}
            </el-tag>
            <p>{{ item.title }}</p>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<style scoped>
.class-management {
  max-width: 1160px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.management-hero {
  border-radius: 16px;
  background: linear-gradient(135deg, #1d4ed8 0%, #1e40af 100%);
  padding: 22px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.management-hero__path,
.management-hero__title,
.management-hero__desc {
  margin: 0;
}

.management-hero__path {
  color: rgba(255, 255, 255, 0.72);
  font-size: 12px;
}

.management-hero__title {
  margin-top: 6px;
  color: #ffffff;
  font-size: 30px;
  line-height: 38px;
  font-weight: 800;
}

.management-hero__desc {
  margin-top: 8px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.management-hero__actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.hero-btn {
  border-radius: 10px;
}

.statistics-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.statistics-card {
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  padding: 14px;
}

.statistics-card__label {
  margin: 0;
  color: #6b7280;
  font-size: 12px;
}

.statistics-card__value {
  margin: 8px 0 0;
  color: #111827;
  font-size: 28px;
  line-height: 34px;
  font-weight: 800;
}

.statistics-card__value small {
  margin-left: 4px;
  color: #6b7280;
  font-size: 12px;
  font-weight: 500;
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

.panel__head span {
  color: #6b7280;
  font-size: 12px;
}

.panel__head--table {
  margin-bottom: 10px;
}

.query-form {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 12px;
}

.panel--aside {
  align-self: start;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.todo-item {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.todo-item p {
  margin: 0;
  color: #374151;
  font-size: 13px;
  line-height: 20px;
}

@media (max-width: 1080px) {
  .statistics-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .management-hero {
    flex-direction: column;
    padding: 18px;
  }

  .statistics-grid {
    grid-template-columns: 1fr;
  }
}

:global(html.dark) .statistics-card,
:global(html.dark) .panel,
:global(html.dark) .todo-item {
  background: #1a2028;
  border-color: #2b3442;
}

:global(html.dark) .statistics-card__value,
:global(html.dark) .panel__head h2 {
  color: #e5eaf3;
}

:global(html.dark) .statistics-card__label,
:global(html.dark) .statistics-card__value small,
:global(html.dark) .panel__head span,
:global(html.dark) .todo-item p {
  color: #9ba7ba;
}
</style>

