<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  fetchAdminDisplayCurrent,
  resetAdminDisplayCurrent,
  updateAdminDisplayCurrent
} from '../../api/admin/display'

function createDefaultForm() {
  return {
    siteTitle: '',
    siteSubtitle: '',
    darkModeDefault: true,
    dashboardLayout: 'card-grid',
    showAttendanceWidget: true,
    showFinanceWidget: true,
    loginNotice: '',
    brandColor: '#2563EB',
    locale: 'zh-CN',
    previewRecords: []
  }
}

const displayForm = ref(createDefaultForm())
const loading = ref(false)
const saving = ref(false)

const layoutOptions = [
  { label: '卡片网格', value: 'card-grid' },
  { label: '紧凑表格', value: 'compact-table' }
]

const localeOptions = [
  { label: '简体中文', value: 'zh-CN' },
  { label: 'English', value: 'en-US' }
]

const themePreviewCards = computed(() => displayForm.value.previewRecords || [])

async function loadDisplayConfig() {
  loading.value = true
  try {
    displayForm.value = await fetchAdminDisplayCurrent()
  } catch (error) {
    ElMessage.error(error.message || '展示设置加载失败')
  } finally {
    loading.value = false
  }
}

async function saveSettings() {
  saving.value = true
  try {
    await updateAdminDisplayCurrent({
      siteTitle: displayForm.value.siteTitle,
      siteSubtitle: displayForm.value.siteSubtitle,
      darkModeDefault: displayForm.value.darkModeDefault,
      dashboardLayout: displayForm.value.dashboardLayout,
      showAttendanceWidget: displayForm.value.showAttendanceWidget,
      showFinanceWidget: displayForm.value.showFinanceWidget,
      loginNotice: displayForm.value.loginNotice,
      brandColor: displayForm.value.brandColor,
      locale: displayForm.value.locale
    })
    ElMessage.success('展示设置已保存')
    await loadDisplayConfig()
  } catch (error) {
    ElMessage.error(error.message || '展示设置保存失败')
  } finally {
    saving.value = false
  }
}

async function resetSettings() {
  loading.value = true
  try {
    displayForm.value = await resetAdminDisplayCurrent()
    ElMessage.success('已恢复默认展示设置')
  } catch (error) {
    ElMessage.error(error.message || '恢复默认展示设置失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDisplayConfig()
})
</script>

<template>
  <div v-loading="loading" class="display-page">
    <section class="page-head">
      <div>
        <h1 class="title">展示设置</h1>
        <p class="subtitle">机构设置 / 展示设置</p>
      </div>
      <div class="actions">
        <el-button :disabled="saving" @click="resetSettings">恢复默认</el-button>
        <el-button type="primary" :loading="saving" @click="saveSettings">保存设置</el-button>
      </div>
    </section>

    <section class="grid">
      <article class="panel">
        <header class="panel-title">基础展示</header>
        <el-form :model="displayForm" label-width="110px">
          <el-form-item label="站点标题">
            <el-input v-model="displayForm.siteTitle" />
          </el-form-item>
          <el-form-item label="副标题">
            <el-input v-model="displayForm.siteSubtitle" />
          </el-form-item>
          <el-form-item label="默认主题">
            <el-switch v-model="displayForm.darkModeDefault" active-text="深色" inactive-text="浅色" />
          </el-form-item>
          <el-form-item label="布局模式">
            <el-radio-group v-model="displayForm.dashboardLayout">
              <el-radio v-for="item in layoutOptions" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="语言">
            <el-select v-model="displayForm.locale">
              <el-option v-for="item in localeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-form>
      </article>

      <article class="panel">
        <header class="panel-title">首页组件开关</header>
        <div class="toggle-item">
          <div>
            <p>考勤看板</p>
            <span>控制首页是否展示考勤趋势卡片</span>
          </div>
          <el-switch v-model="displayForm.showAttendanceWidget" />
        </div>
        <div class="toggle-item">
          <div>
            <p>财务看板</p>
            <span>控制首页是否展示财务统计卡片</span>
          </div>
          <el-switch v-model="displayForm.showFinanceWidget" />
        </div>
        <el-divider />
        <header class="panel-title">视觉偏好</header>
        <el-form :model="displayForm" label-width="110px">
          <el-form-item label="品牌主色">
            <el-color-picker v-model="displayForm.brandColor" />
          </el-form-item>
          <el-form-item label="登录公告">
            <el-input v-model="displayForm.loginNotice" type="textarea" :rows="4" />
          </el-form-item>
        </el-form>
      </article>
    </section>

    <section class="panel">
      <header class="panel-title">预览记录</header>
      <el-table :data="themePreviewCards" stripe>
        <el-table-column prop="title" label="模块" min-width="200" />
        <el-table-column prop="value" label="当前状态" min-width="160" />
        <el-table-column prop="createdAt" label="创建时间" min-width="180" />
        <el-table-column prop="updatedAt" label="更新时间" min-width="180" />
      </el-table>
    </section>
  </div>
</template>

<style scoped>
.display-page { display: flex; flex-direction: column; gap: 16px; }
.page-head { display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; }
.title { margin: 0; font-size: 28px; font-weight: 700; }
.subtitle { margin: 6px 0 0; color: #667085; }
.actions { display: flex; gap: 10px; }
.grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.panel { background: #fff; border: 1px solid #e6ebf2; border-radius: 12px; padding: 14px; }
.panel-title { font-weight: 700; margin-bottom: 12px; }
.toggle-item { display: flex; justify-content: space-between; align-items: center; gap: 10px; margin-bottom: 12px; }
.toggle-item p { margin: 0; font-weight: 600; }
.toggle-item span { color: #667085; font-size: 12px; }
@media (max-width: 1024px) { .grid { grid-template-columns: 1fr; } }
@media (max-width: 640px) { .actions { width: 100%; } .actions :deep(.el-button) { flex: 1; } }
:global(html.dark) .title { color: #e5eaf3; }
:global(html.dark) .subtitle, :global(html.dark) .toggle-item span { color: #a9b4c5; }
:global(html.dark) .panel { background: #1a2028; border-color: #2b3442; }
</style>

