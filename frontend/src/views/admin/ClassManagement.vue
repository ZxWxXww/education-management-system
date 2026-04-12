<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createAdminClass,
  deleteAdminClass,
  fetchAdminClassDetail,
  fetchAdminClassPage,
  fetchAdminClassReferenceOptions,
  updateAdminClass
} from '../../api/admin/class'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const editingId = ref(null)
const formRef = ref()
const detailRow = ref(null)
const classRows = ref([])
const courseOptions = ref([])
const teacherOptions = ref([])
const queryForm = reactive({
  keyword: '',
  courseId: null,
  status: ''
})
const form = reactive({
  classCode: '',
  className: '',
  courseId: null,
  headTeacherId: null,
  startDate: '',
  endDate: '',
  statusValue: 1
})

const statusOptions = ['全部状态', '进行中', '招生中', '停用']

const cards = computed(() => {
  const total = classRows.value.length
  const active = classRows.value.filter((item) => item.status === '进行中').length
  const recruiting = classRows.value.filter((item) => item.status === '招生中').length
  const assignedTeacher = classRows.value.filter((item) => item.teacherName && item.teacherName !== '-').length
  return [
    { label: '班级总数', value: total, tip: '真实班级记录' },
    { label: '进行中班级', value: active, tip: '按状态统计' },
    { label: '招生中班级', value: recruiting, tip: '便于排班跟踪' },
    { label: '已分配教师', value: assignedTeacher, tip: '已关联班主任' }
  ]
})

const rules = {
  classCode: [{ required: true, message: '请输入班级编码', trigger: 'blur' }],
  className: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
  courseId: [{ required: true, message: '请选择所属课程', trigger: 'change' }]
}

async function loadClassModule() {
  loading.value = true
  try {
    const [page, refs] = await Promise.all([
      fetchAdminClassPage({
        pageNum: 1,
        pageSize: 200,
        keyword: queryForm.keyword,
        courseId: queryForm.courseId,
        status: queryForm.status
      }),
      fetchAdminClassReferenceOptions()
    ])
    classRows.value = page.list
    courseOptions.value = refs.courses
    teacherOptions.value = refs.teachers
  } catch (error) {
    ElMessage.error(error.message || '班级数据加载失败')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  editingId.value = null
  form.classCode = ''
  form.className = ''
  form.courseId = null
  form.headTeacherId = null
  form.startDate = ''
  form.endDate = ''
  form.statusValue = 1
  formRef.value?.clearValidate()
}

function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
}

async function openEditDialog(row) {
  try {
    const detail = await fetchAdminClassDetail(row.id)
    editingId.value = row.id
    form.classCode = detail.classCode
    form.className = detail.className
    form.courseId = detail.courseId
    form.headTeacherId = detail.headTeacherId
    form.startDate = detail.startDate
    form.endDate = detail.endDate
    form.statusValue = detail.statusValue
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '班级详情加载失败')
  }
}

async function openDetail(row) {
  try {
    detailRow.value = await fetchAdminClassDetail(row.id)
    detailVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '班级详情加载失败')
  }
}

function statusType(status) {
  if (status === '进行中') return 'success'
  if (status === '招生中') return 'warning'
  return 'info'
}

async function submitForm() {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      if (editingId.value) {
        await updateAdminClass(editingId.value, form)
        ElMessage.success('班级更新成功')
      } else {
        await createAdminClass(form)
        ElMessage.success('班级创建成功')
      }
      dialogVisible.value = false
      resetForm()
      await loadClassModule()
    } catch (error) {
      ElMessage.error(error.message || '班级保存失败')
    } finally {
      saving.value = false
    }
  })
}

async function removeClass(row) {
  try {
    await ElMessageBox.confirm(`确认删除班级“${row.className}”吗？`, '删除确认', { type: 'warning' })
    await deleteAdminClass(row.id)
    ElMessage.success('班级删除成功')
    await loadClassModule()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '班级删除失败')
    }
  }
}

onMounted(() => {
  loadClassModule()
})
</script>

<template>
  <div v-loading="loading" class="class-management">
    <section class="cm-header">
      <div>
        <h1 class="cm-title">班级课程管理</h1>
        <p class="cm-subtitle">实时增控全校班级状态与排课进度</p>
      </div>
      <div class="cm-toolbar">
        <el-select v-model="queryForm.courseId" clearable placeholder="全部课程" style="width: 220px">
          <el-option v-for="item in courseOptions" :key="item.id" :label="item.label" :value="item.id" />
        </el-select>
        <el-select v-model="queryForm.status" clearable placeholder="全部状态" style="width: 140px">
          <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item === '全部状态' ? '' : item" />
        </el-select>
        <el-button type="primary" @click="loadClassModule">查询</el-button>
        <el-button type="primary" plain @click="openCreateDialog">新建班级</el-button>
      </div>
    </section>

    <section class="stats-grid">
      <article v-for="item in cards" :key="item.label" class="stat-card">
        <p class="stat-label">{{ item.label }}</p>
        <p class="stat-value">{{ item.value }}</p>
        <p class="stat-tip">{{ item.tip }}</p>
      </article>
    </section>

    <section class="panel-card">
      <header class="panel-head">
        <h2>班级列表</h2>
        <div class="toolbar">
          <el-input v-model="queryForm.keyword" clearable placeholder="搜索班级名称/编码" style="max-width: 260px" />
          <el-button @click="loadClassModule">刷新</el-button>
        </div>
      </header>
      <el-table :data="classRows" stripe>
        <el-table-column prop="classCode" label="班级编码" min-width="140" />
        <el-table-column prop="className" label="班级名称" min-width="160" />
        <el-table-column prop="courseName" label="所属课程" min-width="160" />
        <el-table-column prop="teacherName" label="班主任" width="120" />
        <el-table-column prop="startDate" label="开班日期" width="120" />
        <el-table-column prop="endDate" label="结课日期" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="light">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" min-width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button link @click="openEditDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="removeClass(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑班级' : '新建班级'" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="班级编码" prop="classCode">
              <el-input v-model="form.classCode" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="班级名称" prop="className">
              <el-input v-model="form.className" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="所属课程" prop="courseId">
              <el-select v-model="form.courseId" placeholder="请选择课程">
                <el-option v-for="item in courseOptions" :key="item.id" :label="item.label" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="班主任">
              <el-select v-model="form.headTeacherId" clearable placeholder="可不设置">
                <el-option v-for="item in teacherOptions" :key="item.id" :label="item.label" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="开班日期">
              <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择开班日期" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结课日期">
              <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="选择结课日期" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态">
          <el-radio-group v-model="form.statusValue">
            <el-radio :value="1">进行中</el-radio>
            <el-radio :value="2">招生中</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="班级详情" size="420px">
      <div v-if="detailRow" class="detail-grid">
        <p><strong>班级编码：</strong>{{ detailRow.classCode }}</p>
        <p><strong>班级名称：</strong>{{ detailRow.className }}</p>
        <p><strong>所属课程：</strong>{{ detailRow.courseName }}</p>
        <p><strong>班主任：</strong>{{ detailRow.teacherName }}</p>
        <p><strong>开班日期：</strong>{{ detailRow.startDate || '-' }}</p>
        <p><strong>结课日期：</strong>{{ detailRow.endDate || '-' }}</p>
        <p><strong>状态：</strong>{{ detailRow.status }}</p>
        <p><strong>创建时间：</strong>{{ detailRow.createdAt }}</p>
        <p><strong>更新时间：</strong>{{ detailRow.updatedAt }}</p>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.class-management {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 1160px;
  margin: 0 auto;
  color: #0f172a;
}
.cm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.cm-title {
  margin: 0;
  font-size: 36px;
  line-height: 1.2;
  font-weight: 800;
}
.cm-subtitle {
  margin: 6px 0 0;
  color: #4b5563;
  font-size: 16px;
}
.cm-toolbar,
.toolbar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}
.stat-card,
.panel-card {
  border-radius: 16px;
  background: #ffffff;
  border: 1px solid #eef2f7;
}
.stat-card {
  padding: 16px;
}
.stat-label {
  margin: 0;
  color: #667085;
  font-size: 13px;
}
.stat-value {
  margin: 6px 0 2px;
  font-size: 28px;
  font-weight: 800;
}
.stat-tip {
  margin: 0;
  color: #98a2b3;
  font-size: 12px;
}
.panel-card {
  padding: 18px;
}
.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.panel-head h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
}
.detail-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.detail-grid p {
  margin: 0;
  color: #374151;
}
@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
@media (max-width: 640px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  .cm-title {
    font-size: 28px;
  }
  .panel-head {
    flex-direction: column;
    align-items: stretch;
  }
  .cm-toolbar,
  .toolbar {
    flex-direction: column;
  }
}
:global(html.dark) .class-management {
  color: #e2e8f0;
}
:global(html.dark) .stat-card,
:global(html.dark) .panel-card {
  background: #111827;
  border-color: #1f2937;
}
:global(html.dark) .cm-subtitle,
:global(html.dark) .stat-label,
:global(html.dark) .stat-tip,
:global(html.dark) .detail-grid p {
  color: #94a3b8;
}
:global(html.dark) .panel-head h2,
:global(html.dark) .cm-title,
:global(html.dark) .stat-value {
  color: #e2e8f0;
}
</style>

