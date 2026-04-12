<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  fetchAdminAttendanceExceptionDetail,
  fetchAdminAttendanceExceptionPage,
  updateAdminAttendanceException
} from '../../api/admin/attendance'

const loading = ref(false)
const detailVisible = ref(false)
const handleVisible = ref(false)
const exceptionRecords = ref([])
const selectedRecord = ref(null)
const handleForm = reactive({ resolveRemark: '' })

const queryForm = ref({
  keyword: '',
  abnormalType: '',
  status: '',
  date: ''
})

const abnormalOptions = [
  { label: '全部类型', value: '' },
  { label: '迟到', value: '迟到' },
  { label: '缺勤', value: '缺勤' },
  { label: '早退', value: '早退' }
]

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '待处理', value: '待处理' },
  { label: '已处理', value: '已处理' }
]

const filteredRecords = computed(() => {
  const keyword = queryForm.value.keyword.trim()
  return exceptionRecords.value.filter((item) => {
    const matchKeyword =
      !keyword ||
      item.studentName.includes(keyword) ||
      item.studentNo.includes(keyword) ||
      item.className.includes(keyword) ||
      item.teacherName.includes(keyword)
    const matchType = !queryForm.value.abnormalType || item.abnormalType === queryForm.value.abnormalType
    const matchStatus = !queryForm.value.status || item.handleStatus === queryForm.value.status
    const matchDate = !queryForm.value.date || item.checkTime.startsWith(queryForm.value.date)
    return matchKeyword && matchType && matchStatus && matchDate
  })
})

const summaryCards = computed(() => [
  { label: '当前筛选异常数', value: filteredRecords.value.length, tone: 'danger' },
  { label: '迟到', value: filteredRecords.value.filter((item) => item.abnormalType === '迟到').length, tone: 'warning' },
  { label: '缺勤', value: filteredRecords.value.filter((item) => item.abnormalType === '缺勤').length, tone: 'danger' },
  { label: '待处理', value: filteredRecords.value.filter((item) => item.handleStatus === '待处理').length, tone: 'info' }
])

async function loadAttendanceExceptions() {
  loading.value = true
  try {
    const page = await fetchAdminAttendanceExceptionPage({ pageNum: 1, pageSize: 200 })
    exceptionRecords.value = page.list
  } catch (error) {
    ElMessage.error('管理员异常考勤加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  queryForm.value = {
    keyword: '',
    abnormalType: '',
    status: '',
    date: ''
  }
}

async function openDetail(row) {
  try {
    selectedRecord.value = await fetchAdminAttendanceExceptionDetail(row.id)
    detailVisible.value = true
  } catch (error) {
    ElMessage.error('异常详情加载失败，请稍后重试')
  }
}

async function openHandle(row) {
  try {
    selectedRecord.value = await fetchAdminAttendanceExceptionDetail(row.id)
    handleForm.resolveRemark = selectedRecord.value.resolveRemark || ''
    handleVisible.value = true
  } catch (error) {
    ElMessage.error('异常处理数据加载失败，请稍后重试')
  }
}

async function submitHandle() {
  if (!selectedRecord.value) return
  try {
    await updateAdminAttendanceException(selectedRecord.value.id, {
      attendanceRecordId: selectedRecord.value.attendanceRecordId,
      abnormalTypeValue: selectedRecord.value.abnormalTypeValue,
      severity: selectedRecord.value.severity || 'MEDIUM',
      isResolved: 1,
      resolveRemark: handleForm.resolveRemark
    })
    handleVisible.value = false
    ElMessage.success('异常考勤已更新为已处理')
    await loadAttendanceExceptions()
    selectedRecord.value = null
  } catch (error) {
    ElMessage.error(error.message || '异常处理失败')
  }
}

function getTagType(type) {
  if (type === '迟到') return 'warning'
  if (type === '缺勤') return 'danger'
  if (type === '早退') return 'info'
  return ''
}

function getStatusType(status) {
  return status === '已处理' ? 'success' : 'warning'
}

onMounted(() => {
  loadAttendanceExceptions()
})
</script>

<template>
  <div v-loading="loading" class="abnormal-page">
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
        <el-form-item label="处理状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option v-for="option in statusOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="queryForm.date" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>
        <el-form-item class="query-actions">
          <el-button type="primary" @click="loadAttendanceExceptions">刷新真实数据</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="table-panel">
      <el-table :data="filteredRecords" stripe>
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="studentNo" label="学号" width="130" />
        <el-table-column prop="className" label="班级" min-width="180" />
        <el-table-column prop="teacherName" label="任课教师" width="110" />
        <el-table-column prop="courseName" label="课程" width="110" />
        <el-table-column prop="checkTime" label="考勤时间" min-width="160" />
        <el-table-column label="异常类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTagType(row.abnormalType)" effect="light">{{ row.abnormalType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.handleStatus)" effect="dark">{{ row.handleStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看详情</el-button>
            <el-button link :disabled="row.handleStatus === '已处理'" @click="openHandle(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-drawer v-model="detailVisible" title="异常考勤详情" size="420px" append-to-body>
      <div v-if="selectedRecord" class="detail-grid">
        <p><strong>异常编号：</strong>{{ selectedRecord.displayId }}</p>
        <p><strong>学生姓名：</strong>{{ selectedRecord.studentName }}</p>
        <p><strong>学号：</strong>{{ selectedRecord.studentNo }}</p>
        <p><strong>班级：</strong>{{ selectedRecord.className }}</p>
        <p><strong>任课教师：</strong>{{ selectedRecord.teacherName }}</p>
        <p><strong>课程：</strong>{{ selectedRecord.courseName }}</p>
        <p><strong>考勤时间：</strong>{{ selectedRecord.checkTime }}</p>
        <p><strong>异常类型：</strong>{{ selectedRecord.abnormalType }}</p>
        <p><strong>处理状态：</strong>{{ selectedRecord.handleStatus }}</p>
        <p><strong>记录备注：</strong>{{ selectedRecord.remark || '-' }}</p>
        <p><strong>处理备注：</strong>{{ selectedRecord.resolveRemark || '-' }}</p>
        <p><strong>处理人：</strong>{{ selectedRecord.resolvedByName || '-' }}</p>
        <p><strong>处理时间：</strong>{{ selectedRecord.resolvedAt }}</p>
        <p><strong>创建时间：</strong>{{ selectedRecord.createdAt }}</p>
        <p><strong>更新时间：</strong>{{ selectedRecord.updatedAt }}</p>
      </div>
    </el-drawer>

    <el-dialog v-model="handleVisible" title="处理异常考勤" width="460px">
      <el-form label-position="top">
        <el-form-item label="处理备注">
          <el-input v-model="handleForm.resolveRemark" type="textarea" :rows="4" maxlength="200" show-word-limit placeholder="请输入处理说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确认处理</el-button>
      </template>
    </el-dialog>
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
  grid-template-columns: repeat(5, minmax(0, 1fr));
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
:global(html.dark) .summary-card span,
:global(html.dark) .detail-grid p {
  color: #9ca3af;
}

:global(html.dark) .summary-card,
:global(html.dark) .query-panel,
:global(html.dark) .table-panel {
  background: #1f2937;
  border-color: #374151;
}
</style>

