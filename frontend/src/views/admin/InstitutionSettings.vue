<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { fetchAdminSettingsOverview } from '../../api/admin/settings'

const router = useRouter()
const loading = ref(false)
const overview = ref({
  systemSettingCount: 0,
  logStrategyCount: 0,
  displayTemplateCount: 0,
  pendingApprovalCount: 0,
  moduleStatusList: []
})

const settingCards = computed(() => [
  { label: '系统参数项', value: `${overview.value.systemSettingCount}`, tip: '真实配置项' },
  { label: '日志策略', value: `${overview.value.logStrategyCount}`, tip: '按模块统计' },
  { label: '展示模板', value: `${overview.value.displayTemplateCount}`, tip: '当前生效记录' },
  { label: '待审批配置', value: `${overview.value.pendingApprovalCount}`, tip: '真实待处理数' }
])

const settingRows = computed(() => overview.value.moduleStatusList || [])

async function loadOverview() {
  loading.value = true
  try {
    overview.value = await fetchAdminSettingsOverview()
  } catch (error) {
    ElMessage.error(error.message || '机构设置总览加载失败')
  } finally {
    loading.value = false
  }
}

function goLogs() {
  router.push('/admin/settings/logs')
}

function goDisplay() {
  router.push('/admin/settings/display')
}

onMounted(() => {
  loadOverview()
})
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
        <h2>配置模块状态</h2>
      </header>
      <el-table v-loading="loading" :data="settingRows" stripe>
        <el-table-column prop="module" label="模块" min-width="180" />
        <el-table-column prop="owner" label="负责团队" min-width="140" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.state === '运行中' ? 'success' : 'warning'" effect="light">{{ row.state }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="170" />
        <el-table-column prop="updatedAt" label="更新时间" min-width="170" />
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
