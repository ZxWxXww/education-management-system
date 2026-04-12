<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Key, UserFilled } from '@element-plus/icons-vue'
import PageShell from '../../components/PageShell.vue'
import {
  assignAdminUserRoles,
  createAuthorizedAdminUser,
  fetchAdminUserAuthorizationPage
} from '../../api/admin/roleAuth'

const loading = ref(false)
const saveLoading = ref(false)
const createLoading = ref(false)
const userAuthRows = ref([])
const roleCatalog = ref([])

const filterForm = reactive({
  keyword: '',
  roleId: null
})
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const editorVisible = ref(false)
const editForm = reactive({
  id: null,
  displayId: '',
  name: '',
  roleId: null,
  permissions: []
})
const creatorVisible = ref(false)
const createForm = reactive({
  username: '',
  realName: '',
  phone: '',
  email: '',
  password: '',
  roleId: null,
  permissions: [],
  status: 1
})

const roleOptions = computed(() => roleCatalog.value)
const permissionOptions = computed(() =>
  [...new Set(roleCatalog.value.flatMap((item) => item.permissionNames || []))].filter(Boolean)
)

const summaryCards = computed(() => {
  const total = userAuthRows.value.length
  const adminCount = userAuthRows.value.filter((item) => item.role === '管理员').length
  const teacherCount = userAuthRows.value.filter((item) => item.role === '教师').length
  const studentCount = userAuthRows.value.filter((item) => item.role === '学生').length
  return [
    { label: '当前页授权用户', value: `${total}`, tip: `总计 ${pagination.total}`, icon: UserFilled },
    { label: '当前页管理员角色', value: `${adminCount}`, tip: '基于当前页列表', icon: Key },
    { label: '当前页教师角色', value: `${teacherCount}`, tip: '基于当前页列表', icon: Key },
    { label: '当前页学生角色', value: `${studentCount}`, tip: '基于当前页列表', icon: Key }
  ]
})

const filteredRows = computed(() => userAuthRows.value)

function statusTagType(status) {
  return status === '启用' ? 'success' : 'info'
}

function getRoleById(roleId) {
  return roleCatalog.value.find((item) => item.id === roleId) || null
}

function syncPermissions(roleId, targetForm) {
  targetForm.permissions = getRoleById(roleId)?.permissionNames?.slice() || []
}

async function loadAuthorizationPage() {
  loading.value = true
  try {
    const pageData = await fetchAdminUserAuthorizationPage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: filterForm.keyword.trim(),
      roleId: filterForm.roleId || undefined
    })
    userAuthRows.value = pageData.list
    roleCatalog.value = pageData.roles
    pagination.total = pageData.total
  } catch (error) {
    ElMessage.error(error.message || '授权列表加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  loadAuthorizationPage()
}

function handleCurrentChange(pageNum) {
  pagination.pageNum = pageNum
  loadAuthorizationPage()
}

function handleSizeChange(pageSize) {
  pagination.pageSize = pageSize
  pagination.pageNum = 1
  loadAuthorizationPage()
}

function openEditor(row) {
  editForm.id = row.id
  editForm.displayId = row.displayId
  editForm.name = row.name
  editForm.roleId = row.roleId
  editForm.permissions = [...row.permissions]
  editorVisible.value = true
}

async function saveAuthorization() {
  if (!editForm.roleId) return

  saveLoading.value = true
  try {
    await assignAdminUserRoles(editForm.id, [editForm.roleId])
    ElMessage.success('角色授权已更新')
    editorVisible.value = false
    await loadAuthorizationPage()
  } catch (error) {
    ElMessage.error(error.message || '角色授权更新失败')
  } finally {
    saveLoading.value = false
  }
}

function openCreator() {
  createForm.username = ''
  createForm.realName = ''
  createForm.phone = ''
  createForm.email = ''
  createForm.password = ''
  createForm.roleId = null
  createForm.permissions = []
  createForm.status = 1
  creatorVisible.value = true
}

async function saveNewUser() {
  if (!createForm.username.trim() || !createForm.realName.trim() || !createForm.roleId) return

  createLoading.value = true
  try {
    await createAuthorizedAdminUser({
      username: createForm.username.trim(),
      realName: createForm.realName.trim(),
      phone: createForm.phone.trim(),
      email: createForm.email.trim(),
      password: createForm.password.trim(),
      status: createForm.status,
      roleId: createForm.roleId
    })
    ElMessage.success('授权用户已创建')
    creatorVisible.value = false
    await handleSearch()
  } catch (error) {
    ElMessage.error(error.message || '授权用户创建失败')
  } finally {
    createLoading.value = false
  }
}

function resetFilter() {
  filterForm.keyword = ''
  filterForm.roleId = null
  handleSearch()
}

onMounted(() => {
  loadAuthorizationPage()
})
</script>

<template>
  <PageShell title="用户角色授权" subtitle="用户管理 / 用户角色授权">
    <div class="auth-page">
      <el-row :gutter="16">
        <el-col v-for="card in summaryCards" :key="card.label" :xs="24" :sm="12" :md="12" :lg="6">
          <el-card shadow="hover" class="summary-card">
            <div class="summary-head">
              <span class="summary-label">{{ card.label }}</span>
              <el-icon class="summary-icon"><component :is="card.icon" /></el-icon>
            </div>
            <div class="summary-value">{{ card.value }}</div>
            <div class="summary-tip">{{ card.tip }}</div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" class="panel-card">
        <div class="filter-wrap">
          <el-input
            v-model="filterForm.keyword"
            placeholder="搜索用户名/姓名/手机号"
            clearable
            style="max-width: 260px"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
          <el-select v-model="filterForm.roleId" placeholder="全部角色" clearable style="width: 160px">
            <el-option
              v-for="role in roleOptions"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button type="primary" @click="openCreator">添加用户</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </div>

        <el-table v-loading="loading" :data="filteredRows" stripe>
          <el-table-column prop="displayId" label="用户ID" width="120" />
          <el-table-column prop="name" label="姓名" min-width="110" />
          <el-table-column label="角色" width="110">
            <template #default="{ row }">
              <el-tag>{{ row.role }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="username" label="用户名" min-width="140" />
          <el-table-column label="权限清单" min-width="260">
            <template #default="{ row }">
              <div class="permissions-cell">
                <el-tag v-for="perm in row.permissions" :key="perm" type="info" effect="plain" size="small">
                  {{ perm }}
                </el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" effect="light">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" min-width="170" />
          <el-table-column prop="updatedAt" label="更新时间" min-width="170" />
          <el-table-column label="操作" width="110" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEditor(row)">修改授权</el-button>
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
      </el-card>

      <el-drawer v-model="editorVisible" title="角色与权限调整" size="460px" append-to-body>
        <el-form label-width="90px" class="editor-form">
          <el-form-item label="用户ID">
            <el-input v-model="editForm.displayId" disabled />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="editForm.name" disabled />
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="editForm.roleId" style="width: 100%" @change="(value) => syncPermissions(value, editForm)">
              <el-option
                v-for="role in roleOptions"
                :key="role.id"
                :label="role.roleName"
                :value="role.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="权限">
            <el-checkbox-group v-model="editForm.permissions" class="perm-group">
              <el-checkbox v-for="perm in permissionOptions" :key="perm" :label="perm" disabled>
                {{ perm }}
              </el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="drawer-footer">
            <el-button @click="editorVisible = false">取消</el-button>
            <el-button type="primary" :loading="saveLoading" @click="saveAuthorization">保存</el-button>
          </div>
        </template>
      </el-drawer>

      <el-drawer v-model="creatorVisible" title="添加授权用户" size="460px" append-to-body>
        <el-form label-width="90px" class="editor-form">
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
          <el-form-item label="角色">
            <el-select v-model="createForm.roleId" style="width: 100%" placeholder="请选择角色" @change="(value) => syncPermissions(value, createForm)">
              <el-option
                v-for="role in roleOptions"
                :key="role.id"
                :label="role.roleName"
                :value="role.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="createForm.status">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="权限">
            <el-checkbox-group v-model="createForm.permissions" class="perm-group">
              <el-checkbox v-for="perm in permissionOptions" :key="perm" :label="perm" disabled>
                {{ perm }}
              </el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="drawer-footer">
            <el-button @click="creatorVisible = false">取消</el-button>
            <el-button
              type="primary"
              :loading="createLoading"
              :disabled="!createForm.username.trim() || !createForm.realName.trim() || !createForm.roleId"
              @click="saveNewUser"
            >
              添加
            </el-button>
          </div>
        </template>
      </el-drawer>
    </div>
  </PageShell>
</template>

<style scoped>
.auth-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.summary-card,
.panel-card {
  border-radius: 12px;
}

.summary-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.summary-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.summary-icon {
  color: #2563eb;
  font-size: 18px;
}

.summary-value {
  margin-top: 10px;
  font-size: 28px;
  font-weight: 700;
}

.summary-tip {
  margin-top: 6px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.filter-wrap {
  margin-bottom: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.permissions-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.editor-form {
  padding-right: 4px;
}

.perm-group {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}

@media (max-width: 768px) {
  .perm-group {
    grid-template-columns: 1fr;
  }

  .pagination-wrap {
    justify-content: center;
    overflow-x: auto;
  }
}
</style>
