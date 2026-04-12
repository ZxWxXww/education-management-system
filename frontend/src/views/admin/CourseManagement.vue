<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createAdminCourse,
  deleteAdminCourse,
  fetchAdminCourseDetail,
  fetchAdminCoursePage,
  updateAdminCourse
} from '../../api/admin/course'
import { fetchAdminClassPage } from '../../api/admin/class'

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const queryForm = reactive({ keyword: '', status: '' })
const courseRows = ref([])
const classRows = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const editingId = ref(null)
const currentDetail = ref(null)
const formRef = ref()
const form = reactive({
  courseCode: '',
  courseName: '',
  subject: '',
  description: '',
  statusValue: 1
})

const statusOptions = ['全部状态', '进行中', '排课中', '停用']

const enrichedCourses = computed(() => {
  const classByCourse = classRows.value.reduce((acc, item) => {
    if (!item.courseId) return acc
    acc[item.courseId] = acc[item.courseId] || []
    acc[item.courseId].push(item)
    return acc
  }, {})

  return courseRows.value.map((item) => {
    const classes = classByCourse[item.id] || []
    const teacherIds = [...new Set(classes.map((row) => row.headTeacherId).filter(Boolean))]
    return {
      ...item,
      classCount: classes.length,
      teacherCount: teacherIds.length
    }
  })
})

const courseCards = computed(() => {
  const total = enrichedCourses.value.length
  const activeClasses = classRows.value.filter((item) => item.status === '进行中').length
  const newCourses = enrichedCourses.value.filter((item) => {
    if (item.createdAt === '-') return false
    return Date.now() - new Date(item.createdAt).getTime() <= 7 * 24 * 60 * 60 * 1000
  }).length
  const enabledRate = total ? ((enrichedCourses.value.filter((item) => item.status !== '停用').length / total) * 100).toFixed(1) : '0.0'
  return [
    { label: '课程总数', value: String(total), tip: '真实课程记录' },
    { label: '进行中班级', value: String(activeClasses), tip: '班级管理联动统计' },
    { label: '近 7 天新增课程', value: String(newCourses), tip: '按创建时间计算' },
    { label: '启用课程占比', value: `${enabledRate}%`, tip: '排除停用课程' }
  ]
})

const rules = {
  courseCode: [{ required: true, message: '请输入课程编码', trigger: 'blur' }],
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }]
}

async function loadCourseModule() {
  loading.value = true
  try {
    const [coursePage, classPage] = await Promise.all([
      fetchAdminCoursePage({
        pageNum: 1,
        pageSize: 200,
        keyword: queryForm.keyword,
        status: queryForm.status
      }),
      fetchAdminClassPage({ pageNum: 1, pageSize: 300 })
    ])
    courseRows.value = coursePage.list
    classRows.value = classPage.list
  } catch (error) {
    ElMessage.error(error.message || '课程数据加载失败')
  } finally {
    loading.value = false
  }
}

function goClassManagement() {
  router.push('/admin/course/classes')
}

function resetForm() {
  editingId.value = null
  form.courseCode = ''
  form.courseName = ''
  form.subject = ''
  form.description = ''
  form.statusValue = 1
  formRef.value?.clearValidate()
}

function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
}

async function openEditDialog(row) {
  try {
    const detail = await fetchAdminCourseDetail(row.id)
    editingId.value = row.id
    form.courseCode = detail.courseCode
    form.courseName = detail.courseName
    form.subject = detail.subject === '-' ? '' : detail.subject
    form.description = detail.description
    form.statusValue = detail.statusValue
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '课程详情加载失败')
  }
}

async function openDetail(row) {
  try {
    currentDetail.value = await fetchAdminCourseDetail(row.id)
    detailVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '课程详情加载失败')
  }
}

function statusType(status) {
  if (status === '进行中') return 'success'
  if (status === '排课中') return 'warning'
  return 'info'
}

async function submitForm() {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      if (editingId.value) {
        await updateAdminCourse(editingId.value, form)
        ElMessage.success('课程更新成功')
      } else {
        await createAdminCourse(form)
        ElMessage.success('课程创建成功')
      }
      dialogVisible.value = false
      resetForm()
      await loadCourseModule()
    } catch (error) {
      ElMessage.error(error.message || '课程保存失败')
    } finally {
      saving.value = false
    }
  })
}

async function removeCourse(row) {
  try {
    await ElMessageBox.confirm(`确认删除课程“${row.courseName}”吗？`, '删除确认', { type: 'warning' })
    await deleteAdminCourse(row.id)
    ElMessage.success('课程删除成功')
    await loadCourseModule()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '课程删除失败')
    }
  }
}

onMounted(() => {
  loadCourseModule()
})
</script>

<template>
  <div v-loading="loading" class="course-page">
    <section class="page-head">
      <div>
        <h1 class="title">课程管理</h1>
        <p class="subtitle">管理员端 / 课程管理</p>
      </div>
      <div class="actions">
        <el-button @click="goClassManagement">班级管理</el-button>
        <el-button type="primary" @click="openCreateDialog">新建课程</el-button>
      </div>
    </section>

    <section class="stats-grid">
      <article v-for="item in courseCards" :key="item.label" class="stat-card">
        <p class="stat-label">{{ item.label }}</p>
        <p class="stat-value">{{ item.value }}</p>
        <p class="stat-tip">{{ item.tip }}</p>
      </article>
    </section>

    <section class="panel">
      <header class="panel-head">
        <h2>课程总览</h2>
        <div class="toolbar">
          <el-input v-model="queryForm.keyword" clearable placeholder="搜索课程名/编号/学科" style="max-width: 260px" />
          <el-select v-model="queryForm.status" clearable placeholder="全部状态" style="width: 140px">
            <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item === '全部状态' ? '' : item" />
          </el-select>
          <el-button type="primary" @click="loadCourseModule">查询</el-button>
        </div>
      </header>
      <el-table :data="enrichedCourses" stripe>
        <el-table-column prop="courseCode" label="课程编号" min-width="150" />
        <el-table-column prop="courseName" label="课程名称" min-width="150" />
        <el-table-column prop="subject" label="学科" width="100" />
        <el-table-column prop="classCount" label="班级数" width="90" />
        <el-table-column prop="teacherCount" label="授课教师" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="light">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="170" />
        <el-table-column prop="updatedAt" label="更新时间" min-width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button link @click="openEditDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="removeCourse(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑课程' : '新建课程'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="课程编码" prop="courseCode">
          <el-input v-model="form.courseCode" clearable />
        </el-form-item>
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="form.courseName" clearable />
        </el-form-item>
        <el-form-item label="学科">
          <el-input v-model="form.subject" clearable />
        </el-form-item>
        <el-form-item label="课程描述">
          <el-input v-model="form.description" type="textarea" :rows="4" maxlength="300" show-word-limit />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.statusValue">
            <el-radio :value="1">进行中</el-radio>
            <el-radio :value="2">排课中</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="课程详情" size="420px">
      <div v-if="currentDetail" class="detail-grid">
        <p><strong>课程编号：</strong>{{ currentDetail.courseCode }}</p>
        <p><strong>课程名称：</strong>{{ currentDetail.courseName }}</p>
        <p><strong>学科：</strong>{{ currentDetail.subject }}</p>
        <p><strong>状态：</strong>{{ currentDetail.status }}</p>
        <p><strong>描述：</strong>{{ currentDetail.description || '-' }}</p>
        <p><strong>创建时间：</strong>{{ currentDetail.createdAt }}</p>
        <p><strong>更新时间：</strong>{{ currentDetail.updatedAt }}</p>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.course-page { display: flex; flex-direction: column; gap: 16px; }
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
.toolbar { display: flex; gap: 10px; flex-wrap: wrap; }
.detail-grid { display: flex; flex-direction: column; gap: 10px; }
.detail-grid p { margin: 0; color: #374151; line-height: 20px; }
@media (max-width: 1024px) { .stats-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 640px) {
  .stats-grid { grid-template-columns: 1fr; }
  .actions { width: 100%; }
  .actions :deep(.el-button) { flex: 1; }
  .panel-head { flex-direction: column; align-items: stretch; }
  .toolbar { flex-direction: column; }
}
:global(html.dark) .title { color: #e5eaf3; }
:global(html.dark) .subtitle, :global(html.dark) .stat-label, :global(html.dark) .stat-tip, :global(html.dark) .detail-grid p { color: #a9b4c5; }
:global(html.dark) .stat-card, :global(html.dark) .panel { background: #1a2028; border-color: #2b3442; }
</style>
