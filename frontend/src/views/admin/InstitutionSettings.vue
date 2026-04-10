<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

// 机构设置一级页 mock 数据（切换真实后端时，请替换为 src/api/admin/settings.js）
const settingCards = [
  { label: '系统参数项', value: '42', tip: '已启用 40' },
  { label: '日志策略', value: '3', tip: '按模块归档' },
  { label: '展示模板', value: '6', tip: '当前模板 A-02' },
  { label: '待审批配置', value: '2', tip: '需管理员确认' }
]

const settingRows = [
  { module: '日志管理', owner: '运维组', state: '运行中', created_at: '2026-04-05 13:10:02', updated_at: '2026-04-08 09:11:09' },
  { module: '展示设置', owner: '产品组', state: '运行中', created_at: '2026-04-05 13:14:48', updated_at: '2026-04-08 08:57:33' }
]

function goLogs() {
  router.push('/admin/settings/logs')
}

function goDisplay() {
  router.push('/admin/settings/display')
}
</script>

<template>
  <div class="settings-page">
    <section class="page-head">
      <div>
        <h1 class="title">机构设置</h1>
        <p class="subtitle">管理员端 / 机构设置</p>
      </div>
      <div class="actions">
        <el-button @click="goLogs">日志管理</el-button>
        <el-button type="primary" @click="goDisplay">展示设置</el-button>
      </div>
    </section>

    <section class="stats-grid">
      <article v-for="item in settingCards" :key="item.label" class="stat-card">
        <p class="stat-label">{{ item.label }}</p>
        <p class="stat-value">{{ item.value }}</p>
        <p class="stat-tip">{{ item.tip }}</p>
      </article>
    </section>

    <section class="panel">
      <header class="panel-head">
        <h2>配置模块状态（mock）</h2>
      </header>
      <el-table :data="settingRows" stripe>
        <el-table-column prop="module" label="模块" min-width="180" />
        <el-table-column prop="owner" label="负责团队" min-width="140" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag type="success" effect="light">{{ row.state }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="created_at" min-width="170" />
        <el-table-column prop="updated_at" label="updated_at" min-width="170" />
      </el-table>
    </section>
  </div>
</template>

<style scoped>
.settings-page { display: flex; flex-direction: column; gap: 16px; }
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
.panel-head { margin-bottom: 12px; }
.panel-head h2 { margin: 0; font-size: 18px; }
@media (max-width: 1024px) { .stats-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 640px) { .stats-grid { grid-template-columns: 1fr; } .actions { width: 100%; } .actions :deep(.el-button) { flex: 1; } }
:global(html.dark) .title { color: #e5eaf3; }
:global(html.dark) .subtitle, :global(html.dark) .stat-label, :global(html.dark) .stat-tip { color: #a9b4c5; }
:global(html.dark) .stat-card, :global(html.dark) .panel { background: #1a2028; border-color: #2b3442; }
</style>
