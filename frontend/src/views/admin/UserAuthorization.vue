<script setup>
import { computed, reactive, ref } from 'vue'
import { Key, UserFilled } from '@element-plus/icons-vue'
import PageShell from '../../components/PageShell.vue'

// 管理员端“用户管理-用户角色授权”Mock 数据
// 切换真实后端时，请替换为 src/api/admin/userAuthorization.js 的接口返回
const userAuthRows = ref([
  {
    id: 'U-10001',
    name: '陈海峰',
    role: '管理员',
    permissions: ['用户查看', '用户编辑', '角色分配', '系统配置'],
    status: '启用',
    created_at: '2026-04-05 09:12:33',
    updated_at: '2026-04-09 10:18:06'
  },
  {
    id: 'U-10018',
    name: '林若曦',
    role: '教师',
    permissions: ['课程查看', '作业管理', '成绩录入'],
    status: '启用',
    created_at: '2026-03-28 11:06:22',
    updated_at: '2026-04-09 09:02:12'
  },
  {
    id: 'U-10326',
    name: '王子涵',
    role: '学生',
    permissions: ['课程查看', '作业提交', '成绩查看'],
    status: '启用',
    created_at: '2026-02-19 16:55:05',
    updated_at: '2026-04-08 16:45:26'
  }
])

const roleOptions = ['管理员', '教师', '学生']
const permissionOptions = [
  '用户查看',
  '用户编辑',
  '角色分配',
  '系统配置',
  '课程查看',
  '作业管理',
  '成绩录入',
  '作业提交',
  '成绩查看'
]

const filterForm = reactive({
  keyword: '',
  role: ''
})

const editorVisible = ref(false)
const editForm = reactive({
  id: '',
  name: '',
  role: '',
  permissions: []
})
const creatorVisible = ref(false)
const createForm = reactive({
  name: '',
  role: '',
  permissions: [],
  status: '启用'
})

const summaryCards = computed(() => {
  const total = userAuthRows.value.length
  const adminCount = userAuthRows.value.filter((item) => item.role === '管理员').length
  const teacherCount = userAuthRows.value.filter((item) => item.role === '教师').length
  const studentCount = userAuthRows.value.filter((item) => item.role === '学生').length
  return [
    { label: '授权用户数', value: `${total}`, icon: UserFilled },
    { label: '管理员角色', value: `${adminCount}`, icon: Key },
    { label: '教师角色', value: `${teacherCount}`, icon: Key },
    { label: '学生角色', value: `${studentCount}`, icon: Key }
  ]
})

const filteredRows = computed(() =>
  userAuthRows.value.filter((item) => {
    const hitKeyword = filterForm.keyword
      ? item.name.includes(filterForm.keyword) || item.id.includes(filterForm.keyword)
      : true
    const hitRole = filterForm.role ? item.role === filterForm.role : true
    return hitKeyword && hitRole
  })
)

function statusTagType(status) {
  return status === '启用' ? 'success' : 'info'
}

function openEditor(row) {
  editForm.id = row.id
  editForm.name = row.name
  editForm.role = row.role
  editForm.permissions = [...row.permissions]
  editorVisible.value = true
}

function saveAuthorization() {
  const target = userAuthRows.value.find((item) => item.id === editForm.id)
  if (!target) return
  target.role = editForm.role
  target.permissions = [...editForm.permissions]
  target.updated_at = new Date().toISOString().slice(0, 19).replace('T', ' ')
  editorVisible.value = false
}

function openCreator() {
  createForm.name = ''
  createForm.role = ''
  createForm.permissions = []
  createForm.status = '启用'
  creatorVisible.value = true
}

function generateUserId() {
  const maxNumber = userAuthRows.value.reduce((max, item) => {
    const value = Number(item.id.replace('U-', ''))
    return Number.isNaN(value) ? max : Math.max(max, value)
  }, 10000)
  return `U-${maxNumber + 1}`
}

function getNowString() {
  return new Date().toISOString().slice(0, 19).replace('T', ' ')
}

function saveNewUser() {
  if (!createForm.name || !createForm.role || createForm.permissions.length === 0) return
  const now = getNowString()
  userAuthRows.value.unshift({
    id: generateUserId(),
    name: createForm.name,
    role: createForm.role,
    permissions: [...createForm.permissions],
    status: createForm.status,
    created_at: now,
    updated_at: now
  })
  creatorVisible.value = false
}

function resetFilter() {
  filterForm.keyword = ''
  filterForm.role = ''
}
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
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" class="panel-card">
        <div class="filter-wrap">
          <el-input
            v-model="filterForm.keyword"
            placeholder="搜索用户ID/姓名"
            clearable
            style="max-width: 260px"
          />
          <el-select v-model="filterForm.role" placeholder="全部角色" clearable style="width: 160px">
            <el-option v-for="role in roleOptions" :key="role" :label="role" :value="role" />
          </el-select>
          <el-button type="primary" @click="openCreator">添加用户</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </div>

        <el-table :data="filteredRows" stripe>
          <el-table-column prop="id" label="用户ID" width="120" />
          <el-table-column prop="name" label="姓名" min-width="110" />
          <el-table-column label="角色" width="110">
            <template #default="{ row }">
              <el-tag>{{ row.role }}</el-tag>
            </template>
          </el-table-column>
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
          <el-table-column prop="created_at" label="created_at" min-width="170" />
          <el-table-column prop="updated_at" label="updated_at" min-width="170" />
          <el-table-column label="操作" width="110" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEditor(row)">修改授权</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-drawer v-model="editorVisible" title="角色与权限调整" size="460px" append-to-body>
        <el-form label-width="90px" class="editor-form">
          <el-form-item label="用户ID">
            <el-input v-model="editForm.id" disabled />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="editForm.name" disabled />
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="editForm.role" style="width: 100%">
              <el-option v-for="role in roleOptions" :key="role" :label="role" :value="role" />
            </el-select>
          </el-form-item>
          <el-form-item label="权限">
            <el-checkbox-group v-model="editForm.permissions" class="perm-group">
              <el-checkbox v-for="perm in permissionOptions" :key="perm" :label="perm">
                {{ perm }}
              </el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="drawer-footer">
            <el-button @click="editorVisible = false">取消</el-button>
            <el-button type="primary" @click="saveAuthorization">保存</el-button>
          </div>
        </template>
      </el-drawer>

      <el-drawer v-model="creatorVisible" title="添加授权用户" size="460px" append-to-body>
        <el-form label-width="90px" class="editor-form">
          <el-form-item label="姓名">
            <el-input v-model="createForm.name" placeholder="请输入姓名" />
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="createForm.role" style="width: 100%" placeholder="请选择角色">
              <el-option v-for="role in roleOptions" :key="role" :label="role" :value="role" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="createForm.status" style="width: 100%">
              <el-option label="启用" value="启用" />
              <el-option label="停用" value="停用" />
            </el-select>
          </el-form-item>
          <el-form-item label="权限">
            <el-checkbox-group v-model="createForm.permissions" class="perm-group">
              <el-checkbox v-for="perm in permissionOptions" :key="perm" :label="perm">
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
              :disabled="!createForm.name || !createForm.role || createForm.permissions.length === 0"
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

@media (max-width: 768px) {
  .perm-group {
    grid-template-columns: 1fr;
  }
}
</style>
