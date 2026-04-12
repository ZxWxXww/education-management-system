<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { createAdminUser, fetchAdminUserPage } from '../../api/admin/user'

const router = useRouter()
const loading = ref(false)
const createLoading = ref(false)
const detailVisible = ref(false)
const creatorVisible = ref(false)
const detailUser = ref(null)
const filterForm = reactive({
  keyword: ''
})
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})
const createForm = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  status: 1
})

const userRows = ref([])

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

// 用户列表统一走 src/api 层，页面只消费适配后的展示字段。
async function loadUserPage() {
  loading.value = true
  try {
    const pageData = await fetchAdminUserPage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: filterForm.keyword.trim()
    })
    userRows.value = pageData.list
    pagination.total = pageData.total
  } catch (error) {
    ElMessage.error(error.message || '用户列表加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  loadUserPage()
}

function handleCurrentChange(pageNum) {
  pagination.pageNum = pageNum
  loadUserPage()
}

function handleSizeChange(pageSize) {
  pagination.pageSize = pageSize
  pagination.pageNum = 1
  loadUserPage()
}

function openDetail(row) {
  detailUser.value = row
  detailVisible.value = true
}

function resetCreateForm() {
  createForm.username = ''
  createForm.password = ''
  createForm.realName = ''
  createForm.phone = ''
  createForm.email = ''
  createForm.status = 1
}

function openCreator() {
  resetCreateForm()
  creatorVisible.value = true
}

async function submitCreate() {
  createLoading.value = true
  try {
    await createAdminUser({
      username: createForm.username.trim(),
      password: createForm.password.trim(),
      realName: createForm.realName.trim(),
      phone: createForm.phone.trim() || null,
      email: createForm.email.trim() || null,
      status: createForm.status
    })
    ElMessage.success('新增用户成功')
    creatorVisible.value = false
    handleSearch()
  } catch (error) {
    ElMessage.error(error.message || '新增用户失败')
  } finally {
    createLoading.value = false
  }
}

onMounted(() => {
  loadUserPage()
})
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
        <el-button type="primary" @click="openCreator">新增用户</el-button>
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
        <h2>用户列表</h2>
        <el-input
          v-model="filterForm.keyword"
          placeholder="搜索姓名/手机号/用户名"
          clearable
          style="max-width: 260px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
      </header>
      <el-table v-loading="loading" :data="userRows" stripe>
        <el-table-column prop="displayId" label="用户ID" width="110" />
        <el-table-column prop="name" label="姓名" min-width="120" />
        <el-table-column label="角色" width="110">
          <template #default="{ row }">
            <el-tag effect="light">{{ row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="light">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="170" />
        <el-table-column prop="updatedAt" label="更新时间" min-width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">
              <el-icon><UserFilled /></el-icon>查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :current-page="pagination.pageNum"
          :page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>
    </section>

    <el-dialog v-model="creatorVisible" title="新增用户" width="520px">
      <el-form label-width="92px">
        <el-form-item label="用户名">
          <el-input v-model="createForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="createForm.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="初始密码">
          <el-input v-model="createForm.password" placeholder="留空则使用默认密码" show-password />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="createForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="createForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="createForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="creatorVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="createLoading"
          :disabled="!createForm.username.trim() || !createForm.realName.trim()"
          @click="submitCreate"
        >
          保存
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="用户详情" width="520px">
      <el-descriptions v-if="detailUser" :column="1" border>
        <el-descriptions-item label="用户ID">{{ detailUser.displayId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detailUser.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ detailUser.name }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ detailUser.role }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailUser.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detailUser.email }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detailUser.status }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailUser.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailUser.updatedAt }}</el-descriptions-item>
        <el-descriptions-item label="最近登录">{{ detailUser.lastLoginAt }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
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
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 14px; }
@media (max-width: 1024px) { .stats-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 640px) {
  .stats-grid { grid-template-columns: 1fr; }
  .actions { width: 100%; }
  .actions :deep(.el-button) { flex: 1; }
  .panel-head { flex-direction: column; align-items: stretch; }
  .pagination-wrap { justify-content: center; overflow-x: auto; }
}
:global(html.dark) .title { color: #e5eaf3; }
:global(html.dark) .subtitle, :global(html.dark) .stat-label, :global(html.dark) .stat-tip { color: #a9b4c5; }
:global(html.dark) .stat-card, :global(html.dark) .panel { background: #1a2028; border-color: #2b3442; }
</style>
