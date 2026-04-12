<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Clock, Download, EditPen, Filter, Upload, View } from '@element-plus/icons-vue'
import {
  batchUpdateTeacherAttendance,
  createTeacherAttendanceBatchTask,
  fetchTeacherAttendanceBatchTaskPage,
  fetchTeacherAttendancePage
} from '../../api/teacher/attendanceBatch'
import { ATTENDANCE_STATUS, ATTENDANCE_STATUS_OPTIONS, getStatusMeta } from '../../constants/status'

const statusOptions = ATTENDANCE_STATUS_OPTIONS.map((item) => item.label)
const filterForm = reactive({ className: '', dateRange: [], status: '', keyword: '' })
const batchForm = reactive({ status: '', remark: '' })
const viewMode = ref('table')
const processing = ref(false)
const processPercent = ref(0)
const selectedIds = ref([])
const loading = ref(false)
const attendanceRecords = ref([])
const operationLogs = ref([])

const classOptions = computed(() => [...new Set(attendanceRecords.value.map((item) => item.className).filter(Boolean))])

const filteredRecords = computed(() => {
  const keyword = filterForm.keyword.trim()
  const [startDate, endDate] = filterForm.dateRange || []
  return attendanceRecords.value.filter((item) => {
    const matchClass = !filterForm.className || item.className === filterForm.className
    const matchStatus = !filterForm.status || item.status === filterForm.status
    const matchKeyword = !keyword || item.studentName.includes(keyword) || item.studentNo.includes(keyword)
    const matchDate = !startDate || !endDate || (item.date >= startDate && item.date <= endDate)
    return matchClass && matchStatus && matchKeyword && matchDate
  })
})

const selectedCount = computed(() => selectedIds.value.length)

function getStatusType(status) {
  return getStatusMeta(ATTENDANCE_STATUS_OPTIONS, mapStatusToCode(status)).tagType
}

function getLogResultType(result) {
  return result === '失败' ? 'danger' : 'success'
}

function mapStatusToCode(status) {
  if (status === '迟到') return ATTENDANCE_STATUS.LATE
  if (status === '缺勤') return ATTENDANCE_STATUS.ABSENT
  if (status === '请假') return ATTENDANCE_STATUS.LEAVE
  return ATTENDANCE_STATUS.PRESENT
}

function onSelectionChange(rows) { selectedIds.value = rows.map((item) => item.id) }
function resetFilter() { filterForm.className = ''; filterForm.dateRange = []; filterForm.status = ''; filterForm.keyword = ''; loadAttendanceModule() }

function onCardSelect(id, checked) {
  const set = new Set(selectedIds.value)
  if (checked) set.add(id)
  else set.delete(id)
  selectedIds.value = Array.from(set)
}

async function beforeImport(file) {
  if (!/\.(csv|xlsx|xls)$/i.test(file.name)) {
    ElMessage.error('仅支持 Excel/CSV 文件')
    return false
  }
  await createBatchTask(`登记导入任务：${file.name}`)
  ElMessage.success('已登记导入任务，可在批任务日志中追踪')
  return false
}

async function applyBatchStatus() {
  if (!batchForm.status) return ElMessage.warning('请先选择目标考勤状态')
  if (!selectedIds.value.length) return ElMessage.warning('请先选择要操作的记录')
  processing.value = true
  processPercent.value = 35
  try {
    await batchUpdateTeacherAttendance({
      recordIds: selectedIds.value,
      status: mapStatusToCode(batchForm.status),
      remark: batchForm.remark || undefined
    })
    processPercent.value = 75
    await createBatchTask(`批量状态修改：将 ${selectedIds.value.length} 条记录更新为${batchForm.status}`, selectedIds.value)
    await loadAttendanceModule()
    processPercent.value = 100
    ElMessage.success('批量状态修改完成')
  } finally {
    processing.value = false
  }
}

async function applyBatchRemark() {
  const remark = batchForm.remark.trim()
  if (!remark) return ElMessage.warning('请输入批量备注内容')
  if (!selectedIds.value.length) return ElMessage.warning('请先选择要操作的记录')
  const currentStatus = attendanceRecords.value.find((item) => selectedIds.value.includes(item.id))?.status || '正常'
  processing.value = true
  processPercent.value = 35
  try {
    await batchUpdateTeacherAttendance({
      recordIds: selectedIds.value,
      status: mapStatusToCode(currentStatus),
      remark
    })
    processPercent.value = 75
    await createBatchTask(`批量备注：为 ${selectedIds.value.length} 条记录追加备注`, selectedIds.value)
    await loadAttendanceModule()
    batchForm.remark = ''
    processPercent.value = 100
    ElMessage.success('批量备注完成')
  } finally {
    processing.value = false
  }
}

async function exportReport() {
  const success = await createBatchTask(`登记导出任务：记录数 ${filteredRecords.value.length}`, filteredRecords.value.map((item) => item.id))
  if (success) {
    ElMessage.success('已登记导出任务，可在批任务日志中追踪')
  }
}

function deriveBatchContext(recordIds = []) {
  const sourceRecords = recordIds.length
    ? attendanceRecords.value.filter((item) => recordIds.includes(item.id))
    : filteredRecords.value
  if (!sourceRecords.length) return null
  const classIds = [...new Set(sourceRecords.map((item) => item.classId).filter(Boolean))]
  const dates = [...new Set(sourceRecords.map((item) => item.date).filter(Boolean))]
  if (classIds.length !== 1 || dates.length !== 1) return null
  return { classId: classIds[0], attendanceDate: dates[0] }
}

async function createBatchTask(remark, recordIds = []) {
  const context = deriveBatchContext(recordIds)
  if (!context) {
    ElMessage.warning('当前选择跨班级或跨日期，暂无法记录单个批任务日志')
    return false
  }
  await createTeacherAttendanceBatchTask({
    classId: context.classId,
    attendanceDate: context.attendanceDate,
    taskStatus: 'DONE',
    remark
  })
  await loadBatchLogs()
  return true
}

async function loadBatchLogs() {
  try {
    const page = await fetchTeacherAttendanceBatchTaskPage({ pageNum: 1, pageSize: 50 })
    operationLogs.value = page.list
  } catch (error) {
    operationLogs.value = []
  }
}


async function loadAttendanceModule() {
  loading.value = true
  try {
    const page = await fetchTeacherAttendancePage({ pageNum: 1, pageSize: 200 })
    attendanceRecords.value = page.list
    selectedIds.value = []
    await loadBatchLogs()
  } catch (error) {
    ElMessage.error('考勤批量数据加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAttendanceModule()
})
</script>

<template>
  <div class="batch-page">
    <section class="hero">
      <div>
        <p class="path">考勤记录 / 班级考勤批量处理</p>
        <h1>班级考勤批量处理</h1>
        <p class="desc">支持批量状态修改、备注追加，以及导入/导出任务登记与追踪</p>
      </div>
      <el-tag type="primary" effect="dark" round>批量操作模式</el-tag>
    </section>

    <section class="panel">
      <header class="panel-head">
        <h2><el-icon><Filter /></el-icon> 批量筛选条件</h2>
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button label="table" value="table">表格视图</el-radio-button>
          <el-radio-button label="card" value="card">卡片视图</el-radio-button>
        </el-radio-group>
      </header>
      <el-form class="filter-grid" label-position="top">
        <el-form-item label="班级"><el-select v-model="filterForm.className" clearable placeholder="全部班级"><el-option v-for="item in classOptions" :key="item" :label="item" :value="item" /></el-select></el-form-item>
        <el-form-item label="日期范围"><el-date-picker v-model="filterForm.dateRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" /></el-form-item>
        <el-form-item label="考勤状态"><el-select v-model="filterForm.status" clearable placeholder="全部状态"><el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" /></el-select></el-form-item>
        <el-form-item label="关键词"><el-input v-model="filterForm.keyword" clearable placeholder="学生姓名/学号" /></el-form-item>
        <el-form-item class="actions"><el-button @click="resetFilter">重置</el-button><el-button type="primary" @click="loadAttendanceModule">刷新真实数据</el-button></el-form-item>
      </el-form>
    </section>

    <section class="panel">
      <header class="panel-head">
        <h2><el-icon><EditPen /></el-icon> 批量操作区</h2>
        <span class="meta">已选 {{ selectedCount }} 条</span>
      </header>
      <div class="batch-actions">
        <el-upload :show-file-list="false" :before-upload="beforeImport" accept=".csv,.xlsx,.xls"><el-button type="primary" plain :icon="Upload">登记导入任务</el-button></el-upload>
        <el-select v-model="batchForm.status" placeholder="目标状态" style="max-width: 180px"><el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" /></el-select>
        <el-button type="warning" @click="applyBatchStatus">批量修改状态</el-button>
        <el-input v-model="batchForm.remark" placeholder="输入批量备注信息" style="max-width: 260px" />
        <el-button type="success" @click="applyBatchRemark">批量添加备注</el-button>
        <el-button type="info" :icon="Download" @click="exportReport">登记导出任务</el-button>
      </div>
      <div class="progress-box">
        <div class="progress-title"><el-icon><Clock /></el-icon><span>操作进度</span></div>
        <el-progress :percentage="processPercent" :status="processing ? '' : 'success'" :indeterminate="processing" :stroke-width="12" />
      </div>
    </section>

    <section v-loading="loading" class="panel">
      <header class="panel-head"><h2><el-icon><View /></el-icon> 批量处理数据（{{ filteredRecords.length }}）</h2></header>
      <el-table v-if="viewMode === 'table'" :data="filteredRecords" stripe @selection-change="onSelectionChange">
        <el-table-column type="selection" width="48" />
        <el-table-column prop="studentName" label="学生姓名" min-width="100" />
        <el-table-column prop="studentNo" label="学号" min-width="120" />
        <el-table-column prop="className" label="班级" min-width="120" />
        <el-table-column prop="courseName" label="课程" min-width="120" />
        <el-table-column prop="sessionTime" label="课节时间" min-width="120" />
        <el-table-column prop="date" label="日期" min-width="110" />
        <el-table-column label="状态" min-width="100"><template #default="{ row }"><el-tag :type="getStatusType(row.status)" effect="light">{{ row.status }}</el-tag></template></el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column prop="updatedAt" label="更新时间" min-width="160" />
      </el-table>

      <div v-else class="card-grid">
        <article v-for="item in filteredRecords" :key="item.id" class="record-card">
          <div class="record-head"><el-checkbox :model-value="selectedIds.includes(item.id)" @change="(v) => onCardSelect(item.id, v)" /><el-tag :type="getStatusType(item.status)" effect="light">{{ item.status }}</el-tag></div>
          <h3>{{ item.studentName }}（{{ item.studentNo }}）</h3>
          <p><strong>班级：</strong>{{ item.className }}</p>
          <p><strong>课程：</strong>{{ item.courseName || '-' }}</p>
          <p><strong>课节：</strong>{{ item.sessionTime }}</p>
          <p><strong>日期：</strong>{{ item.date }}</p>
          <p><strong>备注：</strong>{{ item.remark }}</p>
          <p class="time"><strong>更新时间：</strong>{{ item.updatedAt }}</p>
        </article>
      </div>
    </section>

    <section class="panel">
      <header class="panel-head"><h2>批量操作日志</h2></header>
      <el-table :data="operationLogs" size="small" stripe>
        <el-table-column prop="action" label="操作类型" min-width="110" />
        <el-table-column prop="operatorName" label="操作人" min-width="100" />
        <el-table-column prop="detail" label="操作详情" min-width="280" />
        <el-table-column label="结果" min-width="90"><template #default="{ row }"><el-tag :type="getLogResultType(row.result)" effect="light">{{ row.result }}</el-tag></template></el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="160" />
        <el-table-column prop="updatedAt" label="更新时间" min-width="160" />
      </el-table>
    </section>
  </div>
</template>

<style scoped>
.batch-page { max-width: 1180px; margin: 0 auto; display: flex; flex-direction: column; gap: 16px; }
.hero { border-radius: 16px; padding: 22px; display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; background: linear-gradient(135deg, #0f5ef7 0%, #155dfc 60%, #1d4ed8 100%); color: #fff; }
.hero h1,.hero p { margin: 0; }
.path { font-size: 12px; opacity: 0.8; }
.hero h1 { margin-top: 6px; font-size: 30px; font-weight: 800; }
.desc { margin-top: 8px; font-size: 14px; opacity: 0.9; }
.panel { border: 1px solid #e5e7eb; border-radius: 12px; background: #fff; padding: 16px; }
.panel-head { display: flex; justify-content: space-between; align-items: center; gap: 12px; margin-bottom: 12px; }
.panel-head h2 { margin: 0; display: inline-flex; align-items: center; gap: 8px; font-size: 16px; }
.meta { font-size: 12px; color: #64748b; }
.filter-grid { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 12px; }
.actions :deep(.el-form-item__content) { display: flex; align-items: flex-end; gap: 8px; flex-wrap: wrap; }
.batch-actions { display: flex; flex-wrap: wrap; gap: 10px; }
.progress-box { margin-top: 14px; }
.progress-title { display: inline-flex; align-items: center; gap: 6px; margin-bottom: 8px; color: #475569; font-size: 12px; }
.card-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; }
.record-card { border: 1px solid #e5e7eb; border-radius: 10px; padding: 12px; background: #fcfdff; }
.record-head { display: flex; justify-content: space-between; margin-bottom: 8px; }
.record-card h3 { margin: 0 0 8px; font-size: 16px; }
.record-card p { margin: 0 0 6px; color: #334155; font-size: 13px; }
.record-card .time { margin-top: 6px; color: #64748b; }
@media (max-width: 1100px) { .filter-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } .card-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 700px) { .hero { flex-direction: column; padding: 16px; } .filter-grid,.card-grid { grid-template-columns: 1fr; } }
:global(html.dark) .panel,:global(html.dark) .record-card { background: #1a2028; border-color: #2b3442; }
:global(html.dark) .panel-head h2,:global(html.dark) .record-card h3 { color: #e5eaf3; }
:global(html.dark) .meta,:global(html.dark) .progress-title,:global(html.dark) .record-card p,:global(html.dark) .record-card .time { color: #9ba7ba; }
</style>
