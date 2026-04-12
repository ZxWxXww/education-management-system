<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { fetchAdminBillPage } from '../../api/admin/bill'
import { BILL_STATUS } from '../../constants/status'

const router = useRouter()
const loading = ref(false)
const billRows = ref([])

const currentMonthLabel = computed(() => {
  const anchor = monthlyBills.value
    .map((item) => item.dueDate || item.createdAt?.slice(0, 10))
    .filter(Boolean)
    .sort()
    .pop()
  const anchorDate = anchor ? new Date(anchor) : new Date()
  return `${anchorDate.getFullYear()}-${String(anchorDate.getMonth() + 1).padStart(2, '0')}`
})

const monthlyBills = computed(() => {
  const anchor = billRows.value
    .map((item) => item.dueDate || item.createdAt?.slice(0, 10))
    .filter(Boolean)
    .sort()
    .pop()
  const anchorDate = anchor ? new Date(anchor) : new Date()
  const month = anchorDate.getMonth()
  const year = anchorDate.getFullYear()
  return billRows.value.filter((item) => {
    const rawDate = item.dueDate || item.createdAt?.slice(0, 10)
    if (!rawDate) return false
    const date = new Date(rawDate)
    return date.getFullYear() === year && date.getMonth() === month
  })
})

const financeCards = computed(() => {
  const amount = monthlyBills.value.reduce((sum, item) => sum + Number(item.amount || 0), 0)
  const paid = monthlyBills.value.reduce((sum, item) => sum + Number(item.paidAmount || 0), 0)
  const pending = monthlyBills.value
    .filter((item) => item.statusValue !== BILL_STATUS.COMPLETED)
    .reduce((sum, item) => sum + Math.max(0, Number(item.amount || 0) - Number(item.paidAmount || 0)), 0)
  const refund = monthlyBills.value
    .filter((item) => item.billTypeValue === 'REFUND')
    .reduce((sum, item) => sum + Number(item.amount || 0), 0)
  const completedRate = amount ? ((paid / amount) * 100).toFixed(1) : '0.0'
  const overdueCount = monthlyBills.value.filter((item) => item.statusValue === BILL_STATUS.OVERDUE).length
  return [
    { label: '本月应收', value: `¥ ${amount.toFixed(2)}`, tip: `真实账单 ${monthlyBills.value.length} 笔` },
    { label: '本月实收', value: `¥ ${paid.toFixed(2)}`, tip: `回款率 ${completedRate}%` },
    { label: '待回款', value: `¥ ${pending.toFixed(2)}`, tip: `逾期 ${overdueCount} 单` },
    { label: '退费总额', value: `¥ ${refund.toFixed(2)}`, tip: '按退费账单统计' }
  ]
})

const weeklyLabels = ['第1周', '第2周', '第3周', '第4周']

const weeklySeries = computed(() => {
  const result = [0, 0, 0, 0]
  monthlyBills.value.forEach((item) => {
    const rawDate = item.dueDate || item.createdAt?.slice(0, 10)
    if (!rawDate) return
    const day = new Date(rawDate).getDate()
    const index = Math.min(3, Math.floor((day - 1) / 7))
    result[index] += Number(item.paidAmount || 0)
  })
  return result
})

const trendPath = computed(() => {
  const points = weeklySeries.value
  if (!points.length) return ''
  const max = Math.max(...points)
  const min = Math.min(...points)
  const span = max - min || 1
  return points
    .map((value, index) => {
      const x = points.length === 1 ? 280 : (index / (points.length - 1)) * 560
      const y = 210 - ((value - min) / span) * 160 - 24
      return `${x.toFixed(2)},${y.toFixed(2)}`
    })
    .join(' ')
})

async function loadFinance() {
  loading.value = true
  try {
    const page = await fetchAdminBillPage({ pageNum: 1, pageSize: 300 })
    billRows.value = page.list
  } catch (error) {
    ElMessage.error(error.message || '财务统计加载失败')
  } finally {
    loading.value = false
  }
}

function goBillManagement() {
  router.push('/admin/finance/bills')
}

function goOrderManagement() {
  router.push('/admin/finance/orders')
}

onMounted(() => {
  loadFinance()
})
</script>

<template>
  <div v-loading="loading" class="finance-page">
    <section class="page-head">
      <div>
        <h1 class="title">财务统计</h1>
        <p class="subtitle">管理员端 / 财务统计</p>
      </div>
      <div class="actions">
        <el-button type="primary" plain @click="goOrderManagement">订单管理</el-button>
        <el-button @click="goBillManagement">财单管理</el-button>
      </div>
    </section>

    <section class="stats-grid">
      <article v-for="item in financeCards" :key="item.label" class="stat-card">
        <p class="stat-label">{{ item.label }}</p>
        <p class="stat-value">{{ item.value }}</p>
        <p class="stat-tip">{{ item.tip }}</p>
      </article>
    </section>

    <section class="panel">
      <header class="panel-head">
        <h2>本月回款走势</h2>
        <el-tag round effect="plain">{{ currentMonthLabel }}</el-tag>
      </header>
      <div class="chart-box">
        <svg viewBox="0 0 560 240" preserveAspectRatio="none" aria-hidden="true">
          <polyline v-if="trendPath" :points="trendPath" fill="none" stroke="#2563eb" stroke-width="4" stroke-linecap="round" />
        </svg>
        <div class="chart-axis">
          <span v-for="item in weeklyLabels" :key="item">{{ item }}</span>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.finance-page { display: flex; flex-direction: column; gap: 16px; }
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
.panel-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.panel-head h2 { margin: 0; font-size: 18px; }
.chart-box { position: relative; border-radius: 10px; background: linear-gradient(180deg, rgba(37, 99, 235, 0.12), rgba(37, 99, 235, 0)); height: 240px; overflow: hidden; }
.chart-box svg { width: 100%; height: 100%; }
.chart-axis { position: absolute; left: 0; right: 0; bottom: 8px; padding: 0 12px; display: grid; grid-template-columns: repeat(4, 1fr); color: #6b7280; font-size: 11px; }
@media (max-width: 1024px) { .stats-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 640px) { .stats-grid { grid-template-columns: 1fr; } .actions { width: 100%; } .actions :deep(.el-button) { flex: 1; } }
:global(html.dark) .title { color: #e5eaf3; }
:global(html.dark) .subtitle, :global(html.dark) .stat-label, :global(html.dark) .stat-tip, :global(html.dark) .chart-axis { color: #a9b4c5; }
:global(html.dark) .stat-card, :global(html.dark) .panel { background: #1a2028; border-color: #2b3442; }
</style>
