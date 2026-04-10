<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Clock, Download, EditPen, Filter, Upload, View } from '@element-plus/icons-vue'

// 教师端班级考勤批量处理 Mock 数据（切换真实后端时，请替换为 src/api/teacher/attendanceBatch.js 的接口返回）
const classOptions = ['高二（3）班', '高二（6）班', '竞赛1班', '高一（2）班']
const statusOptions = ['正常', '迟到', '早退', '缺勤', '请假']

const filterForm = reactive({ className: '', dateRange: [], status: '', keyword: '' })
const batchForm = reactive({ status: '', remark: '' })
const viewMode = ref('table')
const processing = ref(false)
const processPercent = ref(0)
const selectedIds = ref([])

const attendanceRecords = ref([
  { id: 'BAT-20260409-001', studentName: '张小伟', studentNo: '202308101', className: '高二（3）班', date: '2026-04-09', status: '正常', remark: '按时到课', created_at: '2026-04-09 08:00:00', updated_at: '2026-04-09 08:00:00' },
  { id: 'BAT-20260409-002', studentName: '李雯', studentNo: '202308102', className: '高二（3）班', date: '2026-04-09', status: '迟到', remark: '迟到 8 分钟', created_at: '2026-04-09 08:12:04', updated_at: '2026-04-09 08:12:04' },
  { id: 'BAT-20260409-003', studentName: '王强', studentNo: '202308103', className: '高二（6）班', date: '2026-04-09', status: '缺勤', remark: '已电话通知家长', created_at: '2026-04-09 08:30:00', updated_at: '2026-04-09 08:30:00' },
  { id: 'BAT-20260409-004', studentName: '陈思雨', studentNo: '202308104', className: '竞赛1班', date: '2026-04-09', status: '请假', remark: '病假申请已通过', created_at: '2026-04-09 07:52:10', updated_at: '2026-04-09 07:52:10' },
  { id: 'BAT-20260408-005', studentName: '孙佳', studentNo: '202308105', className: '高一（2）班', date: '2026-04-08', status: '早退', remark: '家长提前接回', created_at: '2026-04-08 16:20:00', updated_at: '2026-04-08 16:20:00' },
  { id: 'BAT-20260408-006', studentName: '周航', studentNo: '202308106', className: '高二（6）班', date: '2026-04-08', status: '正常', remark: '课堂表现良好', created_at: '2026-04-08 08:00:00', updated_at: '2026-04-08 08:00:00' }
])

const operationLogs = ref([
  { id: 'LOG-001', action: '批量导入', detail: '导入 24 条考勤记录（CSV）', result: '成功', created_at: '2026-04-08 19:12:22', updated_at: '2026-04-08 19:12:22' },
  { id: 'LOG-002', action: '批量状态修改', detail: '将 5 条记录状态更新为请假', result: '成功', created_at: '2026-04-08 20:06:14', updated_at: '2026-04-08 20:06:14' }
])

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
  if (status === '正常') return 'success'
  if (status === '迟到') return 'warning'
  if (status === '早退') return 'info'
  if (status === '缺勤') return 'danger'
  return 'primary'
}

function nowText() {
  const now = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return now.getFullYear() + '-' + pad(now.getMonth() + 1) + '-' + pad(now.getDate()) + ' ' + pad(now.getHours()) + ':' + pad(now.getMinutes()) + ':' + pad(now.getSeconds())
}

function pushLog(action, detail, result = '成功') {
  const time = nowText()
  operationLogs.value.unshift({ id: 'LOG-' + Date.now(), action, detail, result, created_at: time, updated_at: time })
}

function runProcess(task, finish) {
  processing.value = true
  processPercent.value = 0
  const timer = setInterval(() => {
    processPercent.value = Math.min(100, processPercent.value + Math.floor(Math.random() * 20 + 10))
    if (processPercent.value >= 100) {
      clearInterval(timer)
      processing.value = false
      finish()
      ElMessage.success(task + '完成')
    }
  }, 240)
}

function onSelectionChange(rows) { selectedIds.value = rows.map((item) => item.id) }
function resetFilter() { filterForm.className = ''; filterForm.dateRange = []; filterForm.status = ''; filterForm.keyword = '' }

function onCardSelect(id, checked) {
  const set = new Set(selectedIds.value)
  if (checked) set.add(id)
  else set.delete(id)
  selectedIds.value = Array.from(set)
}

function beforeImport(file) {
  if (!/\.(csv|xlsx|xls)$/i.test(file.name)) {
    ElMessage.error('仅支持 Excel/CSV 文件')
    return false
  }
  runProcess('批量导入', () => {
    pushLog('批量导入', '导入文件 ' + file.name + '，校验 ' + filteredRecords.value.length + ' 条记录')
  })
  return false
}

function applyBatchStatus() {
  if (!batchForm.status) return ElMessage.warning('请先选择目标考勤状态')
  if (!selectedIds.value.length) return ElMessage.warning('请先选择要操作的记录')
  runProcess('批量状态修改', () => {
    const now = nowText()
    attendanceRecords.value = attendanceRecords.value.map((item) => selectedIds.value.includes(item.id) ? { ...item, status: batchForm.status, updated_at: now } : item)
    pushLog('批量状态修改', '将 ' + selectedIds.value.length + ' 条记录更新为' + batchForm.status + '')
  })
}

function applyBatchRemark() {
  const remark = batchForm.remark.trim()
  if (!remark) return ElMessage.warning('请输入批量备注内容')
  if (!selectedIds.value.length) return ElMessage.warning('请先选择要操作的记录')
  runProcess('批量备注', () => {
    const now = nowText()
    attendanceRecords.value = attendanceRecords.value.map((item) => selectedIds.value.includes(item.id) ? { ...item, remark, updated_at: now } : item)
    pushLog('批量备注', '为 ' + selectedIds.value.length + ' 条记录追加备注')
    batchForm.remark = ''
  })
}

function exportReport() {
  runProcess('批量导出', () => {
    const dateText = filterForm.dateRange.length ? filterForm.dateRange.join('~') : '全部日期'
    pushLog('批量导出', '导出时间范围：' + dateText + '，记录数：' + filteredRecords.value.length)
  })
}
</script>

<template>
  <div class="batch-page">
    <section class="hero">
      <div>
        <p class="path">考勤记录 / 班级考勤批量处理</p>
        <h1>班级考勤批量处理</h1>
        <p class="desc">支持导入、状态批改、备注追加与报表导出，统一处理多班级考勤数据</p>
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
        <el-form-item class="actions"><el-button @click="resetFilter">重置</el-button><el-button type="primary">筛选</el-button></el-form-item>
      </el-form>
    </section>

    <section class="panel">
      <header class="panel-head">
        <h2><el-icon><EditPen /></el-icon> 批量操作区</h2>
        <span class="meta">已选 {{ selectedCount }} 条</span>
      </header>
      <div class="batch-actions">
        <el-upload :show-file-list="false" :before-upload="beforeImport" accept=".csv,.xlsx,.xls"><el-button type="primary" plain :icon="Upload">批量导入（Excel/CSV）</el-button></el-upload>
        <el-select v-model="batchForm.status" placeholder="目标状态" style="max-width: 180px"><el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" /></el-select>
        <el-button type="warning" @click="applyBatchStatus">批量修改状态</el-button>
        <el-input v-model="batchForm.remark" placeholder="输入批量备注信息" style="max-width: 260px" />
        <el-button type="success" @click="applyBatchRemark">批量添加备注</el-button>
        <el-button type="info" :icon="Download" @click="exportReport">导出统计报表</el-button>
      </div>
      <div class="progress-box">
        <div class="progress-title"><el-icon><Clock /></el-icon><span>操作进度</span></div>
        <el-progress :percentage="processPercent" :status="processing ? '' : 'success'" :indeterminate="processing" :stroke-width="12" />
      </div>
    </section>

    <section class="panel">
      <header class="panel-head"><h2><el-icon><View /></el-icon> 批量处理数据（{{ filteredRecords.length }}）</h2></header>
      <el-table v-if="viewMode === 'table'" :data="filteredRecords" stripe @selection-change="onSelectionChange">
        <el-table-column type="selection" width="48" />
        <el-table-column prop="studentName" label="学生姓名" min-width="100" />
        <el-table-column prop="studentNo" label="学号" min-width="120" />
        <el-table-column prop="className" label="班级" min-width="120" />
        <el-table-column prop="date" label="日期" min-width="110" />
        <el-table-column label="状态" min-width="100"><template #default="{ row }"><el-tag :type="getStatusType(row.status)" effect="light">{{ row.status }}</el-tag></template></el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column prop="updated_at" label="updated_at" min-width="160" />
      </el-table>

      <div v-else class="card-grid">
        <article v-for="item in filteredRecords" :key="item.id" class="record-card">
          <div class="record-head"><el-checkbox :model-value="selectedIds.includes(item.id)" @change="(v) => onCardSelect(item.id, v)" /><el-tag :type="getStatusType(item.status)" effect="light">{{ item.status }}</el-tag></div>
          <h3>{{ item.studentName }}（{{ item.studentNo }}）</h3>
          <p><strong>班级：</strong>{{ item.className }}</p>
          <p><strong>日期：</strong>{{ item.date }}</p>
          <p><strong>备注：</strong>{{ item.remark }}</p>
          <p class="time"><strong>updated_at：</strong>{{ item.updated_at }}</p>
        </article>
      </div>
    </section>

    <section class="panel">
      <header class="panel-head"><h2>批量操作日志</h2></header>
      <el-table :data="operationLogs" size="small" stripe>
        <el-table-column prop="action" label="操作类型" min-width="110" />
        <el-table-column prop="detail" label="操作详情" min-width="280" />
        <el-table-column label="结果" min-width="90"><template #default="{ row }"><el-tag :type="row.result === '成功' ? 'success' : 'danger'" effect="light">{{ row.result }}</el-tag></template></el-table-column>
        <el-table-column prop="created_at" label="created_at" min-width="160" />
        <el-table-column prop="updated_at" label="updated_at" min-width="160" />
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
