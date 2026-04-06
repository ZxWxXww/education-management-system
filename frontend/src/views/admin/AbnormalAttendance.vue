<script setup>
import { ref } from 'vue'

// 异常考勤页 mock 数据（后续接入真实接口时，可替换为 src/api/attendance 的接口返回）
const queryForm = ref({
  keyword: '',
  abnormalType: '',
  date: ''
})

const abnormalOptions = [
  { label: '全部类型', value: '' },
  { label: '迟到', value: 'late' },
  { label: '缺勤', value: 'absent' },
  { label: '早退', value: 'leave-early' },
  { label: '请假未审批', value: 'pending-leave' }
]

const summaryCards = [
  { label: '今日异常总数', value: 18, tone: 'danger' },
  { label: '迟到', value: 9, tone: 'warning' },
  { label: '缺勤', value: 5, tone: 'danger' },
  { label: '请假未审批', value: 4, tone: 'info' }
]

const records = [
  {
    studentName: '王小明',
    studentNo: 'S20261023',
    className: '高二英语 B3 班',
    teacher: '李老师',
    checkTime: '2026-04-06 08:17',
    abnormalType: '迟到',
    status: '待处理'
  },
  {
    studentName: '刘佳怡',
    studentNo: 'S20260714',
    className: '高一数学 A1 班',
    teacher: '陈老师',
    checkTime: '2026-04-06 08:05',
    abnormalType: '缺勤',
    status: '已通知'
  },
  {
    studentName: '赵梓轩',
    studentNo: 'S20261102',
    className: '高三化学提升班',
    teacher: '赵老师',
    checkTime: '2026-04-06 09:20',
    abnormalType: '早退',
    status: '已处理'
  },
  {
    studentName: '孙雨桐',
    studentNo: 'S20260518',
    className: '初三物理冲刺班',
    teacher: '王老师',
    checkTime: '2026-04-06 07:48',
    abnormalType: '请假未审批',
    status: '待审批'
  }
]

function resetQuery() {
  queryForm.value = {
    keyword: '',
    abnormalType: '',
    date: ''
  }
}

function getTagType(type) {
  if (type === '迟到') return 'warning'
  if (type === '缺勤') return 'danger'
  if (type === '早退') return 'info'
  return ''
}

function getStatusType(status) {
  if (status === '已处理' || status === '已通知') return 'success'
  if (status === '待审批' || status === '待处理') return 'warning'
  return 'info'
}
</script>

<template>
  <div class="abnormal-page">
    <section class="abnormal-head">
      <h1>异常考勤</h1>
      <p>管理员端 / 考勤管理 / 异常考勤</p>
    </section>

    <section class="summary-grid">
      <article v-for="card in summaryCards" :key="card.label" class="summary-card">
        <span>{{ card.label }}</span>
        <strong>{{ card.value }}</strong>
        <el-tag :type="card.tone" effect="light" size="small">{{ card.label }}</el-tag>
      </article>
    </section>

    <section class="query-panel">
      <el-form :model="queryForm" label-position="top" class="query-form">
        <el-form-item label="关键词检索">
          <el-input v-model="queryForm.keyword" placeholder="输入学生姓名 / 学号 / 班级" clearable />
        </el-form-item>
        <el-form-item label="异常类型">
          <el-select v-model="queryForm.abnormalType" placeholder="请选择类型">
            <el-option v-for="option in abnormalOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="queryForm.date" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>
        <el-form-item class="query-actions">
          <el-button type="primary">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" plain>批量导出</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="table-panel">
      <el-table :data="records" stripe>
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="studentNo" label="学号" width="130" />
        <el-table-column prop="className" label="班级" min-width="180" />
        <el-table-column prop="teacher" label="任课教师" width="110" />
        <el-table-column prop="checkTime" label="考勤时间" min-width="160" />
        <el-table-column label="异常类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTagType(row.abnormalType)" effect="light">{{ row.abnormalType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="dark">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default>
            <el-button link type="primary">查看详情</el-button>
            <el-button link>处理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<style scoped>
.abnormal-page {
  max-width: 1160px;
  margin: 0 auto;
  padding: 8px 4px 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.abnormal-head h1 {
  margin: 0;
  font-size: 28px;
  line-height: 38px;
  color: #111827;
}

.abnormal-head p {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 14px;
}

.summary-grid {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.summary-card {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 14px 16px;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.summary-card span {
  color: #6b7280;
  font-size: 13px;
}

.summary-card strong {
  color: #111827;
  font-size: 28px;
  line-height: 36px;
}

.query-panel,
.table-panel {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #ffffff;
  padding: 16px;
}

.query-form {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.query-actions {
  display: flex;
  align-items: flex-end;
}

.query-actions :deep(.el-form-item__content) {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

@media (max-width: 1024px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .query-form {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .summary-grid,
  .query-form {
    grid-template-columns: 1fr;
  }
}

:global(html.dark) .abnormal-head h1,
:global(html.dark) .summary-card strong {
  color: #e5e7eb;
}

:global(html.dark) .abnormal-head p,
:global(html.dark) .summary-card span {
  color: #9ca3af;
}

:global(html.dark) .summary-card,
:global(html.dark) .query-panel,
:global(html.dark) .table-panel {
  background: #1f2937;
  border-color: #374151;
}
</style>

