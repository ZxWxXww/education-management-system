<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

// 财务统计一级页 mock 数据（切换真实后端时，请替换为 src/api/admin/finance.js）
const financeCards = [
  { label: '本月应收', value: '¥ 682,400', tip: '环比 +8.6%' },
  { label: '本月实收', value: '¥ 641,850', tip: '回款率 94.1%' },
  { label: '待回款', value: '¥ 40,550', tip: '逾期 19 单' },
  { label: '退费总额', value: '¥ 12,600', tip: '环比 -2.4%' }
]

const weeklySeries = [146500, 158900, 169250, 167200]
const weeklyLabels = ['第1周', '第2周', '第3周', '第4周']

function goBillManagement() {
  router.push('/admin/finance/bills')
}

function linePath() {
  const max = Math.max(...weeklySeries)
  const min = Math.min(...weeklySeries)
  const span = max - min || 1
  return weeklySeries
    .map((value, index) => {
      const x = (index / (weeklySeries.length - 1)) * 560
      const y = 210 - ((value - min) / span) * 160 - 24
      return `${x.toFixed(2)},${y.toFixed(2)}`
    })
    .join(' ')
}
</script>

<template>
  <div class="finance-page">
    <section class="page-head">
      <div>
        <h1 class="title">财务统计</h1>
        <p class="subtitle">管理员端 / 财务统计</p>
      </div>
      <div class="actions">
        <el-button @click="goBillManagement">财单管理</el-button>
        <el-button type="primary">下载月报</el-button>
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
        <h2>本月回款走势（mock）</h2>
        <el-tag round effect="plain">2026-04</el-tag>
      </header>
      <div class="chart-box">
        <svg viewBox="0 0 560 240" preserveAspectRatio="none" aria-hidden="true">
          <polyline :points="linePath()" fill="none" stroke="#2563eb" stroke-width="4" stroke-linecap="round" />
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
