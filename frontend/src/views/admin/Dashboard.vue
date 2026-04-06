<script setup>
import { computed } from 'vue'
import { Calendar, Reading, UserFilled, Wallet } from '@element-plus/icons-vue'

// 仪表盘 mock 数据（后续可切换为 src/api/admin 下的真实接口）
const statCards = [
  { title: '总用户数', value: '1,280', trend: '+12.5%', icon: 'user' },
  { title: '在读学员', value: '956', trend: '+8.2%', icon: 'student' },
  { title: '今日报名', value: '42', trend: '+24.1%', icon: 'calendar' },
  { title: '总收入', value: '¥284,500', trend: '+15.3%', icon: 'money' }
]

const weekLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const revenuePoints = [34, 92, 68, 156, 115, 178, 130]
const enrollTrend = [
  { label: '课程咨询', value: '98 人', percent: 82 },
  { label: '试听预约', value: '76 人', percent: 65 },
  { label: '报名转化', value: '42 人', percent: 44 },
  { label: '退费申请', value: '6 人', percent: 12 }
]

const activityList = [
  { title: '课程《高一数学冲刺班》已创建', desc: '教务处 · 2 分钟前', action: '查看详情' },
  { title: '学员请假申请待处理 3 条', desc: '班主任系统 · 10 分钟前', action: '处理' },
  { title: '系统完成凌晨数据备份', desc: '运维服务 · 31 分钟前', action: '日志' }
]

const quickActions = [
  { text: '新增课程', tone: 'primary' },
  { text: '添加用户', tone: 'soft-blue' },
  { text: '导出报表', tone: 'soft-gray' },
  { text: '发送公告', tone: 'soft-gray' }
]

const revenuePath = computed(() => {
  const width = 592
  const height = 210
  const max = Math.max(...revenuePoints)
  const min = Math.min(...revenuePoints)
  const span = max - min || 1

  return revenuePoints
    .map((v, index) => {
      const x = (index / (revenuePoints.length - 1)) * width
      const y = height - ((v - min) / span) * (height - 24) - 6
      return `${x.toFixed(2)},${y.toFixed(2)}`
    })
    .join(' ')
})
</script>

<template>
  <div class="dashboard">
    <section class="welcome">
      <h1 class="welcome__title">概览工作台</h1>
      <p class="welcome__subtitle">欢迎回来，这是您今日的数据分析与教学简报。</p>
    </section>

    <section class="stats-grid">
      <article v-for="card in statCards" :key="card.title" class="stat-card">
        <div class="stat-card__head">
          <div class="stat-card__icon" :class="`stat-card__icon--${card.icon}`">
            <el-icon :size="16"><UserFilled v-if="card.icon === 'user'" /></el-icon>
            <el-icon :size="16"><Reading v-if="card.icon === 'student'" /></el-icon>
            <el-icon :size="16"><Calendar v-if="card.icon === 'calendar'" /></el-icon>
            <el-icon :size="16"><Wallet v-if="card.icon === 'money'" /></el-icon>
          </div>
          <span class="stat-card__trend">{{ card.trend }}</span>
        </div>
        <div class="stat-card__title">{{ card.title }}</div>
        <div class="stat-card__value">{{ card.value }}</div>
      </article>
    </section>

    <section class="charts-row">
      <article class="panel panel--revenue">
        <header class="panel__head">
          <h2 class="panel__title">收入趋势图</h2>
          <span class="panel__tag">最近 7 天</span>
        </header>
        <div class="chart-area">
          <svg class="chart-svg" viewBox="0 0 592 256" preserveAspectRatio="none" aria-hidden="true">
            <defs>
              <linearGradient id="lineFill" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#005DAA" stop-opacity="0.35" />
                <stop offset="100%" stop-color="#005DAA" stop-opacity="0" />
              </linearGradient>
            </defs>
            <polygon :points="`0,242 ${revenuePath} 592,242`" fill="url(#lineFill)" />
            <polyline :points="revenuePath" fill="none" stroke="#005DAA" stroke-width="4" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
          <div class="chart-axis">
            <span v-for="day in weekLabels" :key="day">{{ day }}</span>
          </div>
        </div>
      </article>

      <article class="panel panel--enroll">
        <header class="panel__head panel__head--single">
          <h2 class="panel__title">报名人数趋势</h2>
        </header>
        <div class="enroll-list">
          <div v-for="row in enrollTrend" :key="row.label" class="enroll-item">
            <div class="enroll-item__top">
              <span>{{ row.label }}</span>
              <span>{{ row.value }}</span>
            </div>
            <div class="enroll-item__track">
              <div class="enroll-item__bar" :style="{ width: `${row.percent}%` }"></div>
            </div>
          </div>
        </div>
      </article>
    </section>

    <section class="bottom-row">
      <article class="panel panel--activity">
        <header class="panel__head panel__head--single">
          <h2 class="panel__title">最近动态</h2>
        </header>
        <div class="activity-list">
          <div v-for="item in activityList" :key="item.title" class="activity-item">
            <span class="activity-item__dot"></span>
            <div class="activity-item__content">
              <p class="activity-item__title">{{ item.title }}</p>
              <p class="activity-item__desc">{{ item.desc }}</p>
            </div>
            <el-button link type="primary" class="activity-item__action">{{ item.action }}</el-button>
          </div>
        </div>
      </article>

      <article class="panel panel--quick">
        <header class="panel__head panel__head--single">
          <h2 class="panel__title">快捷操作</h2>
        </header>
        <div class="quick-list">
          <button
            v-for="action in quickActions"
            :key="action.text"
            type="button"
            class="quick-btn"
            :class="`quick-btn--${action.tone}`"
          >
            <span class="quick-btn__icon"></span>
            <span>{{ action.text }}</span>
          </button>
        </div>
      </article>
    </section>
  </div>
</template>

<style scoped>
.dashboard {
  max-width: 996px;
  margin: 0 auto;
  padding: 80px 16px 24px;
  display: flex;
  flex-direction: column;
  gap: 40px;
  background: #f7f9fc;
}

.welcome {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.welcome__title {
  margin: 0;
  color: #191c1e;
  font-size: 30px;
  font-weight: 700;
  line-height: 40px;
}

.welcome__subtitle {
  margin: 0;
  color: #404753;
  font-size: 14px;
  line-height: 22px;
}

.stats-grid {
  display: grid;
  gap: 20px;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.stat-card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 93, 170, 0.06);
  padding: 24px;
}

.stat-card__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-card__icon {
  width: 32px;
  height: 32px;
  border-radius: 12px;
  background: #eff6ff;
  color: #1d4ed8;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.stat-card__trend {
  border-radius: 999px;
  padding: 4px 8px;
  background: #f0fdf4;
  color: #16a34a;
  font-size: 12px;
  font-weight: 700;
  line-height: 16px;
}

.stat-card__title {
  margin-top: 16px;
  color: #404753;
  font-size: 12px;
  line-height: 16px;
}

.stat-card__value {
  margin-top: 4px;
  color: #191c1e;
  font-family: Manrope, sans-serif;
  font-size: 24px;
  font-weight: 900;
  line-height: 32px;
}

.charts-row,
.bottom-row {
  display: grid;
  gap: 24px;
  grid-template-columns: 2fr 1fr;
}

.panel {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 93, 170, 0.06);
}

.panel--revenue {
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.panel--enroll {
  padding: 32px 32px 88px;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.panel--activity {
  padding: 32px 32px 136px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.panel--quick {
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel__head--single {
  justify-content: flex-start;
}

.panel__title {
  margin: 0;
  color: #191c1e;
  font-size: 18px;
  font-weight: 700;
  line-height: 28px;
}

.panel__tag {
  border-radius: 999px;
  padding: 4px 12px;
  background: #f2f4f7;
  color: #404753;
  font-size: 12px;
  line-height: 18px;
}

.chart-area {
  position: relative;
  height: 256px;
  background: linear-gradient(180deg, rgba(239, 246, 255, 0) 0%, rgba(239, 246, 255, 0.2) 100%);
}

.chart-svg {
  width: 100%;
  height: 100%;
}

.chart-axis {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  padding-bottom: 4px;
  color: #94a3b8;
  font-size: 10px;
  line-height: 15px;
  text-align: left;
}

.enroll-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.enroll-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.enroll-item__top {
  display: flex;
  justify-content: space-between;
  color: #404753;
  font-size: 12px;
  line-height: 20px;
}

.enroll-item__track {
  height: 8px;
  border-radius: 999px;
  background: #f1f5f9;
  overflow: hidden;
}

.enroll-item__bar {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #1d4ed8 0%, #60a5fa 100%);
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.activity-item__dot {
  width: 40px;
  height: 40px;
  border-radius: 999px;
  background: #eff6ff;
  flex: none;
}

.activity-item__content {
  flex: 1;
}

.activity-item__title,
.activity-item__desc {
  margin: 0;
}

.activity-item__title {
  color: #1e293b;
  font-size: 14px;
  line-height: 22px;
}

.activity-item__desc {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
  line-height: 20px;
}

.activity-item__action {
  font-size: 12px;
  font-weight: 700;
}

.quick-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.quick-btn {
  border: 0;
  border-radius: 12px;
  height: 56px;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  font-size: 16px;
  cursor: pointer;
}

.quick-btn__icon {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  background: currentColor;
  opacity: 0.2;
}

.quick-btn--primary {
  background: #005daa;
  color: #ffffff;
  box-shadow: 0 4px 6px -4px rgba(59, 130, 246, 0.2), 0 10px 15px -3px rgba(59, 130, 246, 0.2);
}

.quick-btn--soft-blue {
  background: #afceff;
  color: #385782;
}

.quick-btn--soft-gray {
  background: #f1f5f9;
  color: #334155;
}

@media (max-width: 1100px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .charts-row,
  .bottom-row {
    grid-template-columns: 1fr;
  }

  .panel--activity {
    padding-bottom: 32px;
  }
}

@media (max-width: 640px) {
  .dashboard {
    padding-top: 72px;
    gap: 24px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .panel--revenue,
  .panel--enroll,
  .panel--quick,
  .panel--activity {
    padding: 20px;
    gap: 20px;
  }
}

:global(html.dark) .dashboard {
  background: #0f1216;
}

:global(html.dark) .welcome__title,
:global(html.dark) .panel__title,
:global(html.dark) .stat-card__value {
  color: #e5eaf3;
}

:global(html.dark) .welcome__subtitle,
:global(html.dark) .stat-card__title,
:global(html.dark) .enroll-item__top,
:global(html.dark) .activity-item__title {
  color: #b9c2d0;
}

:global(html.dark) .panel,
:global(html.dark) .stat-card {
  background: #1a2028;
  box-shadow: none;
}

:global(html.dark) .chart-area {
  background: linear-gradient(180deg, rgba(29, 78, 216, 0.08) 0%, rgba(29, 78, 216, 0) 100%);
}
</style>

