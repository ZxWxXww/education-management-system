<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Collection, Download, Files, FolderOpened } from '@element-plus/icons-vue'
import PageShell from '../../components/PageShell.vue'
import { fetchCurrentStudentProfile } from '../../api/student/profile'
import { fetchStudentResourceDetail, fetchStudentResourcePage } from '../../api/student/resource'

const resourceData = ref({
  profile: {
    studentNo: 'S20260318',
    studentName: '李明',
    gradeClass: '高二（3）班',
    semester: '2025-2026 学年第二学期',
    created_at: '2026-02-18 09:20:00',
    updated_at: '2026-04-09 16:20:00'
  },
  summary: {
    totalCount: 28,
    thisWeekCount: 6,
    courseCount: 5,
    downloadCount: 312,
    created_at: '2026-04-09 16:20:00',
    updated_at: '2026-04-09 16:20:00'
  },
  records: [
    {
      id: 'R-2401',
      title: '函数与导数复习讲义',
      courseName: '数学',
      teacherName: '王老师',
      category: '讲义',
      fileType: 'PDF',
      uploadTime: '2026-04-08 18:20:00',
      downloadCount: 56,
      description: '覆盖导数综合题型与典型错题解析。',
      created_at: '2026-04-08 18:20:00',
      updated_at: '2026-04-08 18:20:00'
    },
    {
      id: 'R-2402',
      title: '阅读理解高频题型拆解',
      courseName: '英语',
      teacherName: '陈老师',
      category: '题库',
      fileType: 'DOCX',
      uploadTime: '2026-04-07 20:10:00',
      downloadCount: 42,
      description: '包含题型策略与分层训练建议。',
      created_at: '2026-04-07 20:10:00',
      updated_at: '2026-04-07 20:10:00'
    },
    {
      id: 'R-2403',
      title: '电磁感应专题错题集',
      courseName: '物理',
      teacherName: '张老师',
      category: '错题集',
      fileType: 'PDF',
      uploadTime: '2026-04-06 19:40:00',
      downloadCount: 37,
      description: '按知识点归类，适合考前冲刺复盘。',
      created_at: '2026-04-06 19:40:00',
      updated_at: '2026-04-06 19:40:00'
    },
    {
      id: 'R-2404',
      title: '有机化学推断思维导图',
      courseName: '化学',
      teacherName: '刘老师',
      category: '思维导图',
      fileType: 'PPTX',
      uploadTime: '2026-04-05 17:30:00',
      downloadCount: 29,
      description: '帮助建立反应路径与官能团识别框架。',
      created_at: '2026-04-05 17:30:00',
      updated_at: '2026-04-05 17:30:00'
    }
  ]
})
const loading = ref(false)

const queryForm = ref({
  courseName: '',
  category: ''
})

const detailVisible = ref(false)
const currentResource = ref(null)

const courseOptions = computed(() => [...new Set(resourceData.value.records.map((item) => item.courseName))])
const categoryOptions = computed(() => [...new Set(resourceData.value.records.map((item) => item.category))])

const summaryCards = computed(() => [
  { key: 'totalCount', title: '资源总数', value: `${resourceData.value.summary.totalCount}`, icon: FolderOpened, tone: 'blue' },
  { key: 'thisWeekCount', title: '本周新增', value: `${resourceData.value.summary.thisWeekCount}`, icon: Collection, tone: 'green' },
  { key: 'courseCount', title: '覆盖课程', value: `${resourceData.value.summary.courseCount}`, icon: Files, tone: 'purple' },
  { key: 'downloadCount', title: '累计下载', value: `${resourceData.value.summary.downloadCount}`, icon: Download, tone: 'orange' }
])

const filteredRecords = computed(() =>
  resourceData.value.records.filter((item) => {
    const matchCourse = queryForm.value.courseName ? item.courseName === queryForm.value.courseName : true
    const matchCategory = queryForm.value.category ? item.category === queryForm.value.category : true
    return matchCourse && matchCategory
  })
)

function buildDownloadUrl(fileUrl) {
  if (!fileUrl) return ''
  if (/^https?:\/\//i.test(fileUrl)) return fileUrl
  return new URL(fileUrl, import.meta.env.VITE_API_BASE_URL || window.location.origin).toString()
}

function downloadResource(record) {
  const downloadUrl = buildDownloadUrl(record.fileUrl)
  if (!downloadUrl) {
    ElMessage.warning('当前资源未配置下载地址')
    return
  }
  window.open(downloadUrl, '_blank', 'noopener')
}

function resetFilters() {
  queryForm.value.courseName = ''
  queryForm.value.category = ''
}

async function loadResourceModule() {
  loading.value = true
  try {
    const [profile, page] = await Promise.all([
      fetchCurrentStudentProfile(),
      fetchStudentResourcePage({ pageNum: 1, pageSize: 100 })
    ])
    resourceData.value.profile = {
      ...resourceData.value.profile,
      studentNo: profile.studentNo || resourceData.value.profile.studentNo,
      studentName: profile.name || resourceData.value.profile.studentName,
      gradeClass: profile.className || resourceData.value.profile.gradeClass,
      semester: profile.grade ? `${profile.grade} 当前学籍信息` : resourceData.value.profile.semester,
      updated_at: profile.updatedAt || resourceData.value.profile.updated_at
    }
    resourceData.value.records = page.list
    resourceData.value.summary = {
      ...resourceData.value.summary,
      totalCount: page.list.length,
      thisWeekCount: page.list.filter((item) => item.uploadTime !== '-').length,
      courseCount: new Set(page.list.map((item) => item.courseName).filter(Boolean)).size,
      downloadCount: page.list.reduce((sum, item) => sum + Number(item.downloadCount || 0), 0)
    }
  } catch (error) {
    ElMessage.error('学习资料加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function openDetail(record) {
  detailVisible.value = true
  currentResource.value = record
  try {
    currentResource.value = await fetchStudentResourceDetail(record.id)
  } catch (error) {
    ElMessage.warning('资源详情加载失败，已展示列表中的基础信息')
  }
}

onMounted(() => {
  loadResourceModule()
})
</script>

<template>
  <PageShell title="学习资料库" subtitle="我的作业 / 学习资料库">
    <div class="resource-page">
      <el-card v-loading="loading" shadow="never" class="profile-card">
        <div class="profile-title">
          {{ resourceData.profile.studentName }}，以下是你所在班级教师上传的学习资源
        </div>
        <div class="profile-meta">
          学号：{{ resourceData.profile.studentNo }} ｜ 班级：{{ resourceData.profile.gradeClass }} ｜ 学期：{{ resourceData.profile.semester }}
        </div>
      </el-card>

      <el-row :gutter="16">
        <el-col v-for="card in summaryCards" :key="card.key" :xs="24" :sm="12" :md="12" :lg="6">
          <el-card shadow="hover" class="summary-card" :class="`tone-${card.tone}`">
            <div class="summary-head">
              <span class="summary-title">{{ card.title }}</span>
              <el-icon class="summary-icon"><component :is="card.icon" /></el-icon>
            </div>
            <div class="summary-value">{{ card.value }}</div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" class="filter-card">
        <el-form :inline="true" :model="queryForm" class="query-form">
          <el-form-item label="课程">
            <el-select v-model="queryForm.courseName" placeholder="全部课程" clearable style="width: 170px">
              <el-option v-for="name in courseOptions" :key="name" :label="name" :value="name" />
            </el-select>
          </el-form-item>
          <el-form-item label="资源类型">
            <el-select v-model="queryForm.category" placeholder="全部类型" clearable style="width: 170px">
              <el-option v-for="name in categoryOptions" :key="name" :label="name" :value="name" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button @click="resetFilters">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card v-loading="loading" shadow="never" class="table-card">
        <template #header>
          <div class="card-header">资源列表</div>
        </template>
        <el-table :data="filteredRecords" stripe>
          <el-table-column prop="resourceId" label="资源ID" min-width="110" />
          <el-table-column prop="title" label="资源名称" min-width="190" />
          <el-table-column prop="courseName" label="课程" min-width="90" />
          <el-table-column prop="teacherName" label="上传教师" min-width="100" />
          <el-table-column prop="category" label="类型" min-width="100" />
          <el-table-column prop="fileType" label="格式" min-width="80" />
          <el-table-column prop="uploadTime" label="上传时间" min-width="155" />
          <el-table-column prop="downloadCount" label="下载次数" min-width="90" />
          <el-table-column label="操作" width="138" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
              <el-button link @click="downloadResource(row)">
                <el-icon><Download /></el-icon>下载
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-drawer v-model="detailVisible" title="资源详情" size="460px" append-to-body>
          <div v-if="currentResource" class="detail-panel">
          <div class="detail-row"><span class="detail-label">资源名称</span><span>{{ currentResource.title }}</span></div>
          <div class="detail-row"><span class="detail-label">课程</span><span>{{ currentResource.courseName }}</span></div>
          <div class="detail-row"><span class="detail-label">上传教师</span><span>{{ currentResource.teacherName }}</span></div>
          <div class="detail-row"><span class="detail-label">资源类型</span><span>{{ currentResource.category }}</span></div>
          <div class="detail-row"><span class="detail-label">文件格式</span><span>{{ currentResource.fileType }}</span></div>
          <div class="detail-row"><span class="detail-label">文件大小</span><span>{{ currentResource.fileSizeKb ? `${currentResource.fileSizeKb} KB` : '-' }}</span></div>
          <div class="detail-row"><span class="detail-label">上传时间</span><span>{{ currentResource.uploadTime }}</span></div>
          <div class="detail-row"><span class="detail-label">下载次数</span><span>{{ currentResource.downloadCount }}</span></div>
          <div class="comment-block">
            <div class="detail-label">资源说明</div>
            <div class="comment-content">{{ currentResource.description }}</div>
          </div>
          <div class="timestamps">
            <div>created_at：{{ currentResource.createdAt }}</div>
            <div>updated_at：{{ currentResource.updatedAt }}</div>
          </div>
        </div>
        <template #footer>
          <div class="drawer-footer">
            <el-button @click="detailVisible = false">关闭</el-button>
            <el-button type="primary" @click="downloadResource(currentResource)">下载资源</el-button>
          </div>
        </template>
      </el-drawer>
    </div>
  </PageShell>
</template>

<style scoped>
.resource-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.profile-card,
.summary-card,
.filter-card,
.table-card {
  border-radius: 12px;
}

.profile-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.profile-meta {
  margin-top: 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.summary-card {
  transition: transform 0.2s ease;
}

.summary-card:hover {
  transform: translateY(-2px);
}

.summary-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.summary-title {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.summary-icon {
  font-size: 20px;
}

.summary-value {
  margin-top: 10px;
  font-size: 28px;
  line-height: 1.2;
  font-weight: 700;
}

.tone-blue .summary-icon {
  color: #409eff;
}

.tone-green .summary-icon {
  color: #67c23a;
}

.tone-purple .summary-icon {
  color: #8a65ff;
}

.tone-orange .summary-icon {
  color: #e6a23c;
}

.query-form {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 12px;
}

.card-header {
  font-size: 15px;
  font-weight: 700;
}

.detail-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 14px;
}

.detail-label {
  color: var(--el-text-color-secondary);
}

.comment-block {
  margin-top: 4px;
  padding: 12px;
  border-radius: 8px;
  background: var(--el-fill-color-light);
}

.comment-content {
  margin-top: 6px;
  line-height: 1.6;
}

.timestamps {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.6;
  color: var(--el-text-color-placeholder);
}

@media (max-width: 768px) {
  .profile-title {
    font-size: 18px;
  }

  .summary-value {
    font-size: 24px;
  }
}
</style>
