<script setup>
import { computed, ref } from 'vue'
import { UserFilled } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 用户管理页面 mock 数据（切换真实后端时，请替换为 src/api/admin/user.js）
const userRows = ref([
  {
    id: 'U-10001',
    name: '陈海峰',
    role: '管理员',
    phone: '13800001234',
    status: '启用',
    created_at: '2026-04-05 09:12:33',
    updated_at: '2026-04-07 15:20:10'
  },
  {
    id: 'U-10018',
    name: '林若曦',
    role: '教师',
    phone: '13900004567',
    status: '启用',
    created_at: '2026-03-28 11:06:22',
    updated_at: '2026-04-07 08:14:52'
  },
  {
    id: 'U-10326',
    name: '王子涵',
    role: '学生',
    phone: '13700007654',
    status: '停用',
    created_at: '2026-02-19 16:55:05',
    updated_at: '2026-04-06 19:45:26'
  }
])

const roleSummary = computed(() => {
  const total = userRows.value.length
  const adminCount = userRows.value.filter((item) => item.role === '管理员').length
  const teacherCount = userRows.value.filter((item) => item.role === '教师').length
  const studentCount = userRows.value.filter((item) => item.role === '学生').length
  return [
    { label: '用户总数', value: `${total}`, tip: '当前筛选结果' },
    { label: '管理员', value: `${adminCount}`, tip: '系统配置权限' },
    { label: '教师', value: `${teacherCount}`, tip: '授课与教学管理' },
    { label: '学生', value: `${studentCount}`, tip: '学习与作业提交' }
  ]
})

function goRolePermission() {
  router.push('/admin/user/roles')
}

function statusTagType(status) {
  return status === '启用' ? 'success' : 'info'
}
</script>

<template>
  <div class="user-page">
    <section class="page-head">
      <div>
        <h1 class="title">用户管理</h1>
        <p class="subtitle">管理员端 / 用户管理</p>
      </div>
      <div class="actions">
        <el-button @click="goRolePermission">角色权限管理</el-button>
        <el-button type="primary">新增用户</el-button>
      </div>
    </section>

    <section class="stats-grid">
      <article v-for="item in roleSummary" :key="item.label" class="stat-card">
        <p class="stat-label">{{ item.label }}</p>
        <p class="stat-value">{{ item.value }}</p>
        <p class="stat-tip">{{ item.tip }}</p>
      </article>
    </section>

    <section class="panel">
      <header class="panel-head">
        <h2>用户列表（mock）</h2>
        <el-input placeholder="搜索姓名/手机号" style="max-width: 260px" />
      </header>
      <el-table :data="userRows" stripe>
        <el-table-column prop="id" label="用户ID" width="130" />
        <el-table-column prop="name" label="姓名" min-width="120" />
        <el-table-column label="角色" width="110">
          <template #default="{ row }">
            <el-tag effect="light">{{ row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="light">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="created_at" min-width="170" />
        <el-table-column prop="updated_at" label="updated_at" min-width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default>
            <el-button link type="primary">
              <el-icon><UserFilled /></el-icon>查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<style scoped>
.user-page { display: flex; flex-direction: column; gap: 16px; }
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
.panel-head { display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-bottom: 12px; }
.panel-head h2 { margin: 0; font-size: 18px; }
@media (max-width: 1024px) { .stats-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 640px) {
  .stats-grid { grid-template-columns: 1fr; }
  .actions { width: 100%; }
  .actions :deep(.el-button) { flex: 1; }
  .panel-head { flex-direction: column; align-items: stretch; }
}
:global(html.dark) .title { color: #e5eaf3; }
:global(html.dark) .subtitle, :global(html.dark) .stat-label, :global(html.dark) .stat-tip { color: #a9b4c5; }
:global(html.dark) .stat-card, :global(html.dark) .panel { background: #1a2028; border-color: #2b3442; }
</style>
