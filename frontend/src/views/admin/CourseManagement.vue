<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 课程管理页面 mock 数据（切换真实后端时，请替换为 src/api/admin/course.js）
const courseCards = [
  { label: '课程总数', value: '86', tip: '在售 + 历史课程' },
  { label: '进行中班级', value: '42', tip: '今日有排课' },
  { label: '本周新开班', value: '6', tip: '近7天' },
  { label: '平均满班率', value: '81.6%', tip: '动态统计' }
]

const courseRows = ref([
  {
    courseCode: 'MATH-G1-001',
    courseName: '高一数学拔高',
    subject: '数学',
    classCount: 8,
    teacherCount: 6,
    status: '进行中',
    created_at: '2026-03-12 08:34:40',
    updated_at: '2026-04-07 14:19:27'
  },
  {
    courseCode: 'ENG-G2-003',
    courseName: '高二英语强化',
    subject: '英语',
    classCount: 6,
    teacherCount: 4,
    status: '进行中',
    created_at: '2026-02-20 10:22:11',
    updated_at: '2026-04-06 18:06:43'
  },
  {
    courseCode: 'PHY-J3-007',
    courseName: '初三物理冲刺',
    subject: '物理',
    classCount: 5,
    teacherCount: 3,
    status: '排课中',
    created_at: '2026-03-28 09:08:56',
    updated_at: '2026-04-07 09:40:12'
  }
])

function goClassManagement() {
  router.push('/admin/course/classes')
}

function statusType(status) {
  return status === '进行中' ? 'success' : 'warning'
}
</script>

<template>
  <div class="course-page">
    <section class="page-head">
      <div>
        <h1 class="title">课程管理</h1>
        <p class="subtitle">管理员端 / 课程管理</p>
      </div>
      <div class="actions">
        <el-button @click="goClassManagement">班级管理</el-button>
        <el-button type="primary">新建课程</el-button>
      </div>
    </section>

    <section class="stats-grid">
      <article v-for="item in courseCards" :key="item.label" class="stat-card">
        <p class="stat-label">{{ item.label }}</p>
        <p class="stat-value">{{ item.value }}</p>
        <p class="stat-tip">{{ item.tip }}</p>
      </article>
    </section>

    <section class="panel">
      <header class="panel-head">
        <h2>课程总览（mock）</h2>
        <el-input placeholder="搜索课程名/编号" style="max-width: 260px" />
      </header>
      <el-table :data="courseRows" stripe>
        <el-table-column prop="courseCode" label="课程编号" min-width="150" />
        <el-table-column prop="courseName" label="课程名称" min-width="150" />
        <el-table-column prop="subject" label="学科" width="100" />
        <el-table-column prop="classCount" label="班级数" width="90" />
        <el-table-column prop="teacherCount" label="授课教师" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="light">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="created_at" min-width="170" />
        <el-table-column prop="updated_at" label="updated_at" min-width="170" />
      </el-table>
    </section>
  </div>
</template>

<style scoped>
.course-page { display: flex; flex-direction: column; gap: 16px; }
.page-head { display: flex; align-items: center; justify-content: space-between; gap: 12px; flex-wrap: wrap; }
.title { margin: 0; font-size: 28px; font-weight: 700; }
.subtitle { margin: 6px 0 0; color: #667085; }
.actions { display: flex; gap: 10px; }
.stats-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; }
.stat-card { background: #fff; border: 1px solid #e6ebf2; border-radius: 12px; padding: 14px; }
.stat-label { margin: 0; color: #667085; font-size: 13px; }
.stat-value { margin: 6px 0 2px; font-size: 28px; font-weight: 700; }
.stat-tip { margin: 0; color: #98a2b3; font-size: 12px; }
.panel { background: #fff; border: 1px solid #e6ebf2; border-radius: 12px; padding: 14px; }
.panel-head { display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-bottom: 12px; }
.panel-head h2 { margin: 0; font-size: 18px; }
@media (max-width: 1024px) { .stats-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 640px) {
  .stats-grid { grid-template-columns: 1fr; }
  .actions { width: 100%; }
  .actions :deep(.el-button) { flex: 1; }
  .panel-head { flex-direction: column; align-items: stretch; }
}
:global(html.dark) .title { color: #e5eaf3; }
:global(html.dark) .subtitle, :global(html.dark) .stat-label, :global(html.dark) .stat-tip { color: #a9b4c5; }
:global(html.dark) .stat-card, :global(html.dark) .panel { background: #1a2028; border-color: #2b3442; }
</style>
