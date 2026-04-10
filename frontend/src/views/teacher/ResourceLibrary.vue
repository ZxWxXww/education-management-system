<script setup>
import { FolderOpened, UploadFilled } from '@element-plus/icons-vue'

// 教学资料库 Mock 数据（切换真实后端时，请替换为 src/api/teacher/resourceLibrary.js 的接口返回）
const resourceStats = [
  { label: '资料总数', value: '128' },
  { label: '本周新增', value: '16' },
  { label: '下载次数', value: '1,024' },
  { label: '共享班级', value: '12' }
]

const resourceList = [
  { id: 'RES-2026-001', title: '高二数学函数专题讲义', type: 'PDF', className: '高二（3）班', created_at: '2026-04-09 08:12:00', updated_at: '2026-04-09 08:12:00' },
  { id: 'RES-2026-002', title: '高二数学期中复习题库', type: 'Word', className: '高二（6）班', created_at: '2026-04-09 10:42:00', updated_at: '2026-04-09 10:42:00' },
  { id: 'RES-2026-003', title: '竞赛班数列进阶习题', type: 'PPT', className: '竞赛1班', created_at: '2026-04-08 19:15:00', updated_at: '2026-04-08 19:15:00' }
]
</script>

<template>
  <div class="resource-library-page">
    <section class="hero">
      <div>
        <p class="path">教学资源 / 教学资料库</p>
        <h1>教学资料库</h1>
        <p class="desc">统一管理教案、讲义与题库资源，支持班级级别共享分发</p>
      </div>
      <el-button type="primary" :icon="UploadFilled">上传资料</el-button>
    </section>

    <section class="stats-grid">
      <article v-for="item in resourceStats" :key="item.label" class="stat-card">
        <p>{{ item.label }}</p>
        <strong>{{ item.value }}</strong>
      </article>
    </section>

    <section class="panel">
      <header class="panel-head">
        <h2><el-icon><FolderOpened /></el-icon> 资料列表（Mock）</h2>
      </header>
      <el-table :data="resourceList" stripe>
        <el-table-column prop="id" label="资料编号" min-width="140" />
        <el-table-column prop="title" label="资料名称" min-width="220" />
        <el-table-column prop="type" label="类型" min-width="100" />
        <el-table-column prop="className" label="适用班级" min-width="120" />
        <el-table-column prop="created_at" label="created_at" min-width="170" />
        <el-table-column prop="updated_at" label="updated_at" min-width="170" />
      </el-table>
    </section>
  </div>
</template>

<style scoped>
.resource-library-page { max-width: 1180px; margin: 0 auto; display: flex; flex-direction: column; gap: 16px; }
.hero { border-radius: 16px; padding: 20px; display: flex; justify-content: space-between; align-items: flex-start; gap: 10px; background: linear-gradient(135deg, #1d4ed8 0%, #2563eb 100%); color: #fff; }
.path, .desc { margin: 0; opacity: 0.88; }
h1 { margin: 6px 0 8px; font-size: 30px; }
.stats-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; }
.stat-card { border: 1px solid #e5e7eb; border-radius: 12px; background: #fff; padding: 14px; }
.stat-card p { margin: 0; color: #64748b; font-size: 12px; }
.stat-card strong { display: block; margin-top: 6px; font-size: 28px; color: #0f172a; }
.panel { border: 1px solid #e5e7eb; border-radius: 12px; background: #fff; padding: 16px; }
.panel-head { margin-bottom: 10px; }
.panel-head h2 { margin: 0; display: inline-flex; align-items: center; gap: 8px; }
@media (max-width: 1024px) { .stats-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 700px) { .hero { flex-direction: column; } .stats-grid { grid-template-columns: 1fr; } }
:global(html.dark) .stat-card,:global(html.dark) .panel { background: #1a2028; border-color: #2b3442; }
:global(html.dark) .stat-card p { color: #9ba7ba; }
:global(html.dark) .stat-card strong,:global(html.dark) .panel-head h2 { color: #e5eaf3; }
</style>
