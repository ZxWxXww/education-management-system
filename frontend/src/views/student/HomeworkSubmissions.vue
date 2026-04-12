<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled, DataAnalysis, DocumentChecked, WarningFilled } from '@element-plus/icons-vue'
import PageShell from '../../components/PageShell.vue'
import { fetchCurrentStudentProfile } from '../../api/student/profile'
import { fetchStudentAssignmentSubmissionPage } from '../../api/student/assignment'

const homeworkData = ref({
  profile: {
    studentNo: 'S20260318',
    studentName: '李明',
    gradeClass: '高二（3）班',
    semester: '2025-2026 学年第二学期',
    created_at: '2026-02-18 09:20:00',
    updated_at: '2026-04-09 15:40:00'
  },
  summary: {
    totalCount: 18,
    submittedCount: 16,
    onTimeCount: 14,
    pendingCount: 2,
    created_at: '2026-04-09 15:40:00',
    updated_at: '2026-04-09 15:40:00'
  },
  records: [
    {
      id: 1,
      assignmentTitle: '函数综合提升训练',
      courseName: '数学',
      teacherName: '王老师',
      dueTime: '2026-04-06 22:00:00',
      submitTime: '2026-04-06 20:45:00',
      status: 'submitted',
      score: 95,
      feedback: '解题步骤完整，压轴题思路优秀。',
      created_at: '2026-04-05 09:00:00',
      updated_at: '2026-04-06 20:45:00'
    },
    {
      id: 2,
      assignmentTitle: '阅读理解专项练习',
      courseName: '英语',
      teacherName: '陈老师',
      dueTime: '2026-04-07 21:30:00',
      submitTime: '2026-04-07 21:20:00',
      status: 'submitted',
      score: 89,
      feedback: '主旨把握准确，细节定位可再提升。',
      created_at: '2026-04-06 08:40:00',
      updated_at: '2026-04-07 21:20:00'
    },
    {
      id: 3,
      assignmentTitle: '电磁感应单元作业',
      courseName: '物理',
      teacherName: '张老师',
      dueTime: '2026-04-08 22:00:00',
      submitTime: '',
      status: 'pending',
      score: null,
      feedback: '尚未提交',
      created_at: '2026-04-07 10:20:00',
      updated_at: '2026-04-07 10:20:00'
    },
    {
      id: 4,
      assignmentTitle: '有机化学推断训练',
      courseName: '化学',
      teacherName: '刘老师',
      dueTime: '2026-04-08 20:00:00',
      submitTime: '2026-04-08 20:16:00',
      status: 'late',
      score: 82,
      feedback: '提交逾期，建议提前规划复习与作业时间。',
      created_at: '2026-04-07 11:10:00',
      updated_at: '2026-04-08 20:16:00'
    }
  ]
})
const loading = ref(false)

const queryForm = ref({
  status: '',
  courseName: ''
})

const detailVisible = ref(false)
const currentRecord = ref(null)

const courseOptions = computed(() => [...new Set(homeworkData.value.records.map((item) => item.courseName))])

const summaryCards = computed(() => [
  { key: 'totalCount', title: '作业总数', value: `${homeworkData.value.summary.totalCount}`, icon: DocumentChecked, tone: 'blue' },
  { key: 'submittedCount', title: '已提交', value: `${homeworkData.value.summary.submittedCount}`, icon: CircleCheckFilled, tone: 'green' },
  { key: 'onTimeCount', title: '按时提交', value: `${homeworkData.value.summary.onTimeCount}`, icon: DataAnalysis, tone: 'purple' },
  { key: 'pendingCount', title: '待提交', value: `${homeworkData.value.summary.pendingCount}`, icon: WarningFilled, tone: 'orange' }
])

const filteredRecords = computed(() =>
  homeworkData.value.records.filter((item) => {
    const matchStatus = queryForm.value.status ? item.status === queryForm.value.status : true
    const matchCourse = queryForm.value.courseName ? item.courseName === queryForm.value.courseName : true
    return matchStatus && matchCourse
  })
)

function getStatusLabel(status) {
  if (status === 'submitted') return '已提交'
  if (status === 'late') return '逾期提交'
  return '待提交'
}

function getStatusType(status) {
  if (status === 'submitted') return 'success'
  if (status === 'late') return 'warning'
  return 'danger'
}

function formatScore(score) {
  return typeof score === 'number' ? `${score}` : '-'
}

function openDetail(record) {
  currentRecord.value = record
  detailVisible.value = true
}

function resetFilters() {
  queryForm.value.status = ''
  queryForm.value.courseName = ''
}

async function loadHomeworkData() {
  loading.value = true
  try {
    const [profile, page] = await Promise.all([
      fetchCurrentStudentProfile(),
      fetchStudentAssignmentSubmissionPage({ pageNum: 1, pageSize: 100 })
    ])
    homeworkData.value.profile = {
      ...homeworkData.value.profile,
      studentNo: profile.studentNo || homeworkData.value.profile.studentNo,
      studentName: profile.name || homeworkData.value.profile.studentName,
      gradeClass: profile.className || homeworkData.value.profile.gradeClass,
      updated_at: profile.updatedAt || homeworkData.value.profile.updated_at
    }
    homeworkData.value.records = page.list
    homeworkData.value.summary = {
      ...homeworkData.value.summary,
      totalCount: page.list.length,
      submittedCount: page.list.filter((item) => item.status === 'submitted' || item.status === 'late').length,
      onTimeCount: page.list.filter((item) => item.status === 'submitted').length,
      pendingCount: page.list.filter((item) => item.status === 'pending').length
    }
  } catch (error) {
    ElMessage.error('作业记录加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadHomeworkData()
})
</script>

<template>
  <PageShell title="作业提交记录" subtitle="我的作业 / 作业提交记录">
    <div class="submission-page">
      <el-card v-loading="loading" shadow="never" class="profile-card">
        <div class="profile-title">
          {{ homeworkData.profile.studentName }}，这里是你的作业提交进度总览
        </div>
        <div class="profile-meta">
          学号：{{ homeworkData.profile.studentNo }} ｜ 班级：{{ homeworkData.profile.gradeClass }} ｜ 学期：{{ homeworkData.profile.semester }}
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
          <el-form-item label="提交状态">
            <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 170px">
              <el-option label="已提交" value="submitted" />
              <el-option label="逾期提交" value="late" />
              <el-option label="待提交" value="pending" />
            </el-select>
          </el-form-item>
          <el-form-item label="课程">
            <el-select v-model="queryForm.courseName" placeholder="全部课程" clearable style="width: 170px">
              <el-option v-for="name in courseOptions" :key="name" :label="name" :value="name" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button @click="resetFilters">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card v-loading="loading" shadow="never" class="table-card">
        <template #header>
          <div class="card-header">提交明细</div>
        </template>
        <el-table :data="filteredRecords" stripe>
          <el-table-column type="index" label="#" width="56" />
          <el-table-column prop="assignmentTitle" label="作业名称" min-width="180" />
          <el-table-column prop="courseName" label="课程" min-width="90" />
          <el-table-column prop="teacherName" label="任课教师" min-width="100" />
          <el-table-column label="提交状态" min-width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="dueTime" label="截止时间" min-width="155" />
          <el-table-column prop="submitTime" label="提交时间" min-width="155">
            <template #default="{ row }">
              {{ row.submitTime || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="得分" min-width="80">
            <template #default="{ row }">
              {{ formatScore(row.score) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="88" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-drawer v-model="detailVisible" title="作业提交详情" size="460px" append-to-body>
        <div v-if="currentRecord" class="detail-panel">
          <div class="detail-row"><span class="detail-label">作业名称</span><span>{{ currentRecord.assignmentTitle }}</span></div>
          <div class="detail-row"><span class="detail-label">课程</span><span>{{ currentRecord.courseName }}</span></div>
          <div class="detail-row"><span class="detail-label">任课教师</span><span>{{ currentRecord.teacherName }}</span></div>
          <div class="detail-row"><span class="detail-label">提交状态</span><span>{{ getStatusLabel(currentRecord.status) }}</span></div>
          <div class="detail-row"><span class="detail-label">截止时间</span><span>{{ currentRecord.dueTime }}</span></div>
          <div class="detail-row"><span class="detail-label">提交时间</span><span>{{ currentRecord.submitTime || '-' }}</span></div>
          <div class="detail-row"><span class="detail-label">得分</span><span>{{ formatScore(currentRecord.score) }}</span></div>
          <div class="comment-block">
            <div class="detail-label">教师反馈</div>
            <div class="comment-content">{{ currentRecord.feedback }}</div>
          </div>
          <div class="timestamps">
            <div>created_at：{{ currentRecord.createdAt }}</div>
            <div>updated_at：{{ currentRecord.updatedAt }}</div>
          </div>
        </div>
      </el-drawer>
    </div>
  </PageShell>
</template>

<style scoped>
.submission-page {
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
