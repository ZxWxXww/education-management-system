<script setup>
import { computed, reactive, ref } from 'vue'

const BILL_NOTICE_STORAGE_KEY = 'edu_bill_notifications'

// 财单管理 mock 数据（后续接入真实接口时，可替换为 src/api/admin/bill.js）
const filterForm = ref({
  keyword: '',
  billType: '全部类型',
  status: '全部状态',
  month: '2026-04'
})

const billTypeOptions = ['全部类型', '课时包缴费', '教材费', '考试费', '退费']
const statusOptions = ['全部状态', '待确认', '已完成', '已逾期', '已作废']

const financeCards = [
  { label: '本月应收总额', value: '¥ 682,400', trend: '+8.6%', tone: 'blue' },
  { label: '本月实收总额', value: '¥ 641,850', trend: '+6.9%', tone: 'green' },
  { label: '待核销金额', value: '¥ 37,600', trend: '-2.1%', tone: 'orange' },
  { label: '逾期财单数量', value: '19', trend: '+3.2%', tone: 'red' }
]

const trendLabels = ['第1周', '第2周', '第3周', '第4周']
const expectedSeries = [152000, 168000, 176000, 186400]
const receivedSeries = [146500, 158900, 169250, 167200]

const receivableBlocks = [
  { label: '课时包缴费', amount: '¥ 421,800', percent: 72, tone: 'blue' },
  { label: '教材费', amount: '¥ 96,300', percent: 53, tone: 'cyan' },
  { label: '考试费', amount: '¥ 58,900', percent: 61, tone: 'purple' },
  { label: '退费', amount: '¥ 34,600', percent: 28, tone: 'orange' }
]

const billRows = ref([
  {
    billNo: 'BL20260408001',
    studentName: '王子涵',
    className: '高一数学 A1 班',
    billType: '课时包缴费',
    amount: 12800,
    paidAmount: 12800,
    status: '已完成',
    created_at: '2026-04-08 10:22:15',
    updated_at: '2026-04-08 10:40:52'
  },
  {
    billNo: 'BL20260408002',
    studentName: '周雨彤',
    className: '高二英语 B3 班',
    billType: '教材费',
    amount: 1200,
    paidAmount: 0,
    status: '待确认',
    created_at: '2026-04-08 11:05:44',
    updated_at: '2026-04-08 11:06:13'
  },
  {
    billNo: 'BL20260408003',
    studentName: '陈睿泽',
    className: '初三物理冲刺班',
    billType: '考试费',
    amount: 680,
    paidAmount: 680,
    status: '已完成',
    created_at: '2026-04-08 13:20:27',
    updated_at: '2026-04-08 14:01:17'
  },
  {
    billNo: 'BL20260408004',
    studentName: '林可欣',
    className: '高三化学提升班',
    billType: '课时包缴费',
    amount: 15800,
    paidAmount: 5200,
    status: '已逾期',
    created_at: '2026-04-08 09:18:39',
    updated_at: '2026-04-09 08:12:08'
  },
  {
    billNo: 'BL20260408005',
    studentName: '刘浩然',
    className: '高一语文强化班',
    billType: '退费',
    amount: 2600,
    paidAmount: 0,
    status: '已作废',
    created_at: '2026-04-08 15:47:03',
    updated_at: '2026-04-08 16:18:30'
  }
])

const createDialogVisible = ref(false)
const createForm = reactive({
  studentName: '',
  className: '',
  billType: '课时包缴费',
  amount: 0,
  paidAmount: 0,
  status: '待确认'
})

const createStatusOptions = ['待确认', '已完成', '已逾期', '已作废']

const expectedPath = computed(() => buildPath(expectedSeries, 560, 210, 24))
const receivedPath = computed(() => buildPath(receivedSeries, 560, 210, 24))

const totalAmount = computed(() => billRows.value.reduce((sum, row) => sum + row.amount, 0))
const totalPaid = computed(() => billRows.value.reduce((sum, row) => sum + row.paidAmount, 0))
const totalPending = computed(() => totalAmount.value - totalPaid.value)

function buildPath(points, width, height, padding) {
  const max = Math.max(...points)
  const min = Math.min(...points)
  const span = max - min || 1
  return points
    .map((value, index) => {
      const x = (index / (points.length - 1)) * width
      const y = height - ((value - min) / span) * (height - padding * 2) - padding
      return `${x.toFixed(2)},${y.toFixed(2)}`
    })
    .join(' ')
}

function getStatusType(status) {
  if (status === '已完成') return 'success'
  if (status === '待确认') return 'warning'
  if (status === '已逾期') return 'danger'
  return 'info'
}

function formatCurrency(value) {
  return `¥ ${value.toLocaleString('zh-CN')}`
}

function openCreateDialog() {
  createForm.studentName = ''
  createForm.className = ''
  createForm.billType = '课时包缴费'
  createForm.amount = 0
  createForm.paidAmount = 0
  createForm.status = '待确认'
  createDialogVisible.value = true
}

function nowDateTime() {
  return new Date().toISOString().slice(0, 19).replace('T', ' ')
}

function buildBillNo() {
  const datePart = new Date().toISOString().slice(0, 10).replace(/-/g, '')
  const maxSeq = billRows.value.reduce((max, row) => {
    if (!row.billNo || !row.billNo.startsWith(`BL${datePart}`)) return max
    const seq = Number(row.billNo.slice(-3))
    return Number.isNaN(seq) ? max : Math.max(max, seq)
  }, 0)
  return `BL${datePart}${String(maxSeq + 1).padStart(3, '0')}`
}

function saveNewBill() {
  if (!createForm.studentName || !createForm.className || Number(createForm.amount) <= 0) return
  const now = nowDateTime()
  const billNo = buildBillNo()
  const amount = Number(createForm.amount)
  let paidAmount = Number(createForm.paidAmount)
  if (createForm.status === '已完成') {
    paidAmount = amount
  }
  if (paidAmount > amount) {
    paidAmount = amount
  }
  if (paidAmount < 0) {
    paidAmount = 0
  }
  billRows.value.unshift({
    billNo,
    studentName: createForm.studentName,
    className: createForm.className,
    billType: createForm.billType,
    amount,
    paidAmount,
    status: createForm.status,
    created_at: now,
    updated_at: now
  })
  appendBillNotice({
    noticeId: `BN-${Date.now()}`,
    noticeType: 'bill',
    title: `账单通知：${createForm.billType}`,
    content: `管理员已为您创建账单 ${billNo}，请及时查看并完成支付。`,
    billNo,
    studentName: createForm.studentName,
    className: createForm.className,
    billType: createForm.billType,
    amount,
    paidAmount,
    billStatus: createForm.status,
    paymentStatus: createForm.status === '已完成' ? 'paid' : 'pending',
    created_at: now,
    updated_at: now
  })
  createDialogVisible.value = false
}

function getStoredNotices() {
  try {
    const raw = localStorage.getItem(BILL_NOTICE_STORAGE_KEY)
    const parsed = raw ? JSON.parse(raw) : []
    return Array.isArray(parsed) ? parsed : []
  } catch (error) {
    return []
  }
}

function appendBillNotice(notice) {
  const notices = getStoredNotices()
  notices.unshift(notice)
  localStorage.setItem(BILL_NOTICE_STORAGE_KEY, JSON.stringify(notices))
}
</script>

<template>
  <div class="bill-page">
    <section class="bill-head">
      <div>
        <h1 class="bill-title">财单管理</h1>
        <p class="bill-subtitle">管理员端 / 财务统计 / 财单管理</p>
      </div>
      <div class="bill-head-actions">
        <el-button size="large">导出财单</el-button>
        <el-button type="primary" size="large" @click="openCreateDialog">新建财单</el-button>
      </div>
    </section>

    <section class="kpi-grid">
      <article v-for="card in financeCards" :key="card.label" class="kpi-card">
        <p class="kpi-label">{{ card.label }}</p>
        <div class="kpi-main">
          <p class="kpi-value">{{ card.value }}</p>
          <span class="kpi-trend" :class="`kpi-trend--${card.tone}`">{{ card.trend }}</span>
        </div>
      </article>
    </section>

    <section class="filter-panel">
      <el-input v-model="filterForm.keyword" placeholder="请输入财单号/学员姓名" clearable />
      <el-select v-model="filterForm.billType">
        <el-option v-for="option in billTypeOptions" :key="option" :label="option" :value="option" />
      </el-select>
      <el-select v-model="filterForm.status">
        <el-option v-for="option in statusOptions" :key="option" :label="option" :value="option" />
      </el-select>
      <el-date-picker
        v-model="filterForm.month"
        value-format="YYYY-MM"
        format="YYYY-MM"
        type="month"
        placeholder="选择月份"
      />
      <el-button type="primary">查询</el-button>
      <el-button>重置</el-button>
    </section>

    <section class="panel-grid">
      <article class="panel panel--chart">
        <header class="panel-head">
          <h2>月度应收 / 实收趋势</h2>
          <el-tag effect="plain" round>2026 年 4 月</el-tag>
        </header>
        <div class="chart-box">
          <svg viewBox="0 0 560 240" preserveAspectRatio="none" aria-hidden="true">
            <defs>
              <linearGradient id="billExpectedFill" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#2563eb" stop-opacity="0.22" />
                <stop offset="100%" stop-color="#2563eb" stop-opacity="0" />
              </linearGradient>
            </defs>
            <polygon :points="`0,228 ${expectedPath} 560,228`" fill="url(#billExpectedFill)" />
            <polyline :points="expectedPath" fill="none" stroke="#2563eb" stroke-width="4" stroke-linecap="round" stroke-linejoin="round" />
            <polyline :points="receivedPath" fill="none" stroke="#16a34a" stroke-width="4" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
          <div class="chart-legend">
            <span><i class="dot dot--blue"></i>应收</span>
            <span><i class="dot dot--green"></i>实收</span>
          </div>
          <div class="chart-axis">
            <span v-for="label in trendLabels" :key="label">{{ label }}</span>
          </div>
        </div>
      </article>

      <article class="panel">
        <header class="panel-head panel-head--single">
          <h2>分类回款进度</h2>
        </header>
        <div class="receivable-list">
          <div v-for="item in receivableBlocks" :key="item.label" class="receivable-item">
            <div class="receivable-item__top">
              <span>{{ item.label }}</span>
              <span>{{ item.amount }}</span>
            </div>
            <div class="receivable-item__track">
              <div class="receivable-item__bar" :class="`receivable-item__bar--${item.tone}`" :style="{ width: `${item.percent}%` }"></div>
            </div>
            <div class="receivable-item__percent">回款进度 {{ item.percent }}%</div>
          </div>
        </div>
      </article>
    </section>

    <section class="panel">
      <header class="panel-head panel-head--single">
        <h2>财单明细</h2>
      </header>
      <el-table :data="billRows" stripe>
        <el-table-column prop="billNo" label="财单号" min-width="160" />
        <el-table-column prop="studentName" label="学员姓名" width="110" />
        <el-table-column prop="className" label="班级" min-width="160" />
        <el-table-column prop="billType" label="类型" width="120" />
        <el-table-column label="应收金额" width="120">
          <template #default="{ row }">
            {{ formatCurrency(row.amount) }}
          </template>
        </el-table-column>
        <el-table-column label="实收金额" width="120">
          <template #default="{ row }">
            {{ formatCurrency(row.paidAmount) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="96">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="light">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="created_at" min-width="170" />
        <el-table-column prop="updated_at" label="updated_at" min-width="170" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default>
            <el-button link type="primary">查看</el-button>
            <el-button link>核销</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <section class="summary-grid">
      <article class="summary-card">
        <p class="summary-label">财单总额</p>
        <p class="summary-value">{{ formatCurrency(totalAmount) }}</p>
      </article>
      <article class="summary-card">
        <p class="summary-label">已回款总额</p>
        <p class="summary-value summary-value--green">{{ formatCurrency(totalPaid) }}</p>
      </article>
      <article class="summary-card">
        <p class="summary-label">待回款总额</p>
        <p class="summary-value summary-value--orange">{{ formatCurrency(totalPending) }}</p>
      </article>
    </section>

    <el-dialog v-model="createDialogVisible" title="给用户添加账单" width="540px">
      <el-form label-width="92px" class="create-bill-form">
        <el-form-item label="学员姓名">
          <el-input v-model="createForm.studentName" placeholder="请输入学员姓名" />
        </el-form-item>
        <el-form-item label="班级">
          <el-input v-model="createForm.className" placeholder="请输入班级名称" />
        </el-form-item>
        <el-form-item label="账单类型">
          <el-select v-model="createForm.billType" style="width: 100%">
            <el-option
              v-for="option in billTypeOptions.filter((item) => item !== '全部类型')"
              :key="option"
              :label="option"
              :value="option"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="应收金额">
          <el-input-number v-model="createForm.amount" :min="0" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="实收金额">
          <el-input-number v-model="createForm.paidAmount" :min="0" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="createForm.status" style="width: 100%">
            <el-option v-for="option in createStatusOptions" :key="option" :label="option" :value="option" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :disabled="!createForm.studentName || !createForm.className || Number(createForm.amount) <= 0"
            @click="saveNewBill"
          >
            添加账单
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.bill-page {
  max-width: 1160px;
  margin: 0 auto;
  padding: 8px 4px 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.bill-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.bill-title {
  margin: 0;
  color: #111827;
  font-size: 28px;
  line-height: 38px;
  font-weight: 700;
}

.bill-subtitle {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 14px;
  line-height: 22px;
}

.bill-head-actions {
  display: flex;
  gap: 12px;
}

.kpi-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.kpi-card {
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  padding: 18px;
}

.kpi-label {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}

.kpi-main {
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.kpi-value {
  margin: 0;
  color: #111827;
  font-size: 26px;
  line-height: 34px;
  font-weight: 700;
}

.kpi-trend {
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 700;
  line-height: 16px;
}

.kpi-trend--blue {
  color: #1d4ed8;
  background: #dbeafe;
}

.kpi-trend--green {
  color: #15803d;
  background: #dcfce7;
}

.kpi-trend--orange {
  color: #c2410c;
  background: #ffedd5;
}

.kpi-trend--red {
  color: #b91c1c;
  background: #fee2e2;
}

.filter-panel {
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  padding: 14px;
  display: grid;
  gap: 10px;
  grid-template-columns: minmax(220px, 1.6fr) repeat(3, minmax(140px, 1fr)) auto auto;
}

.panel-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: 2fr 1fr;
}

.panel {
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  padding: 18px;
}

.panel--chart {
  padding-bottom: 14px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.panel-head--single {
  justify-content: flex-start;
}

.panel-head h2 {
  margin: 0;
  color: #111827;
  font-size: 18px;
  line-height: 28px;
}

.chart-box {
  position: relative;
  height: 240px;
  border-radius: 10px;
  background: linear-gradient(180deg, rgba(37, 99, 235, 0.12) 0%, rgba(37, 99, 235, 0) 100%);
  overflow: hidden;
}

.chart-box svg {
  width: 100%;
  height: 100%;
}

.chart-legend {
  position: absolute;
  left: 12px;
  top: 12px;
  display: flex;
  gap: 12px;
  color: #4b5563;
  font-size: 12px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
  margin-right: 6px;
}

.dot--blue {
  background: #2563eb;
}

.dot--green {
  background: #16a34a;
}

.chart-axis {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 8px;
  padding: 0 12px;
  color: #6b7280;
  font-size: 11px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
}

.receivable-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.receivable-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.receivable-item__top {
  display: flex;
  justify-content: space-between;
  color: #374151;
  font-size: 13px;
}

.receivable-item__track {
  height: 8px;
  border-radius: 999px;
  background: #eef2ff;
  overflow: hidden;
}

.receivable-item__bar {
  height: 100%;
  border-radius: 999px;
}

.receivable-item__bar--blue {
  background: linear-gradient(90deg, #1d4ed8 0%, #60a5fa 100%);
}

.receivable-item__bar--cyan {
  background: linear-gradient(90deg, #0891b2 0%, #22d3ee 100%);
}

.receivable-item__bar--purple {
  background: linear-gradient(90deg, #7c3aed 0%, #c084fc 100%);
}

.receivable-item__bar--orange {
  background: linear-gradient(90deg, #ea580c 0%, #fdba74 100%);
}

.receivable-item__percent {
  color: #9ca3af;
  font-size: 12px;
}

.summary-grid {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.summary-card {
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  padding: 16px;
}

.summary-label {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}

.summary-value {
  margin: 8px 0 0;
  color: #1f2937;
  font-size: 24px;
  font-weight: 700;
  line-height: 32px;
}

.summary-value--green {
  color: #15803d;
}

.summary-value--orange {
  color: #c2410c;
}

@media (max-width: 1100px) {
  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .filter-panel {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .panel-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .bill-head {
    flex-direction: column;
  }

  .bill-head-actions {
    width: 100%;
  }

  .bill-head-actions :deep(.el-button) {
    flex: 1;
  }

  .kpi-grid,
  .summary-grid,
  .filter-panel {
    grid-template-columns: 1fr;
  }
}

:global(html.dark) .bill-title,
:global(html.dark) .panel-head h2,
:global(html.dark) .kpi-value,
:global(html.dark) .summary-value {
  color: #e5e7eb;
}

:global(html.dark) .bill-subtitle,
:global(html.dark) .kpi-label,
:global(html.dark) .receivable-item__top,
:global(html.dark) .receivable-item__percent,
:global(html.dark) .summary-label,
:global(html.dark) .chart-axis,
:global(html.dark) .chart-legend {
  color: #9ca3af;
}

:global(html.dark) .panel,
:global(html.dark) .kpi-card,
:global(html.dark) .summary-card,
:global(html.dark) .filter-panel {
  background: #1f2937;
  border-color: #374151;
}

:global(html.dark) .chart-box {
  background: linear-gradient(180deg, rgba(59, 130, 246, 0.16) 0%, rgba(59, 130, 246, 0) 100%);
}

:global(html.dark) .receivable-item__track {
  background: #253044;
}

.create-bill-form {
  padding-right: 6px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>

