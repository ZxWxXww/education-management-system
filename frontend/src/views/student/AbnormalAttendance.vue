<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell, Calendar, CircleClose, Clock, Search } from '@element-plus/icons-vue'
import PageShell from '../../components/PageShell.vue'
import { fetchStudentAttendanceExceptionPage } from '../../api/student/attendance'

const loading = ref(false)
const attendanceRecords = ref([])

const filterForm = ref({
  keyword: '',
  abnormalType: '',
  status: '',
  dateRange: []
})

const abnormalTypeOptions = [
  { label: '全部类型', value: '' },
  { label: '迟到', value: '迟到' },
  { label: '缺勤', value: '缺勤' },
  { label: '早退', value: '早退' }
]

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '已确认', value: '已确认' },
  { label: '需补充说明', value: '需补充说明' }
]

const detailVisible = ref(false)
const selectedRecord = ref(null)

const summaryCards = computed(() => [
  { key: 'monthAbnormalCount', title: '异常总数', value: `${attendanceRecords.value.length}`, icon: Bell, tone: 'blue' },
  { key: 'lateCount', title: '迟到次数', value: `${attendanceRecords.value.filter((item) => item.abnormalType === '迟到').length}`, icon: Clock, tone: 'orange' },
  { key: 'absentCount', title: '缺勤次数', value: `${attendanceRecords.value.filter((item) => item.abnormalType === '缺勤').length}`, icon: CircleClose, tone: 'red' },
  { key: 'leaveEarlyCount', title: '早退次数', value: `${attendanceRecords.value.filter((item) => item.abnormalType === '早退').length}`, icon: Calendar, tone: 'purple' }
])

const filteredRecords = computed(() => {
  const keyword = filterForm.value.keyword.trim()
  const [startDate, endDate] = filterForm.value.dateRange || []
  return attendanceRecords.value.filter((item) => {
    const matchKeyword =
      !keyword ||
      item.courseName.includes(keyword) ||
      item.teacherName.includes(keyword) ||
      item.reason.includes(keyword) ||
      item.handleNote.includes(keyword)
    const matchType = !filterForm.value.abnormalType || item.abnormalType === filterForm.value.abnormalType
    const matchStatus = !filterForm.value.status || item.status === filterForm.value.status
    const matchDate = !startDate || !endDate || (item.date >= startDate && item.date <= endDate)
    return matchKeyword && matchType && matchStatus && matchDate
  })
})

const latestAbnormal = computed(() => filteredRecords.value[0] || null)
const latestUpdatedAt = computed(() => attendanceRecords.value[0]?.updatedAt || '-')

function resetFilters() {
  filterForm.value.keyword = ''
  filterForm.value.abnormalType = ''
  filterForm.value.status = ''
  filterForm.value.dateRange = []
}

async function loadAttendanceExceptions() {
  loading.value = true
  try {
    const page = await fetchStudentAttendanceExceptionPage({ pageNum: 1, pageSize: 200 })
    attendanceRecords.value = page.list
  } catch (error) {
    ElMessage.error('学生异常考勤加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function openDetail(record) {
  selectedRecord.value = record
  detailVisible.value = true
}

function getTypeTag(type) {
  if (type === '迟到') return 'warning'
  if (type === '缺勤') return 'danger'
  if (type === '早退') return 'info'
  return ''
}

function getStatusTag(status) {
  return status === '需补充说明' ? 'warning' : 'success'
}

onMounted(() => {
  loadAttendanceExceptions()
})
</script>

<template>
  <PageShell title="考勤异常" subtitle="我的考勤 / 考勤异常">
    <div class="student-abnormal-page">
      <el-card v-loading="loading" shadow="never" class="profile-card">
        <div class="profile-main">
          <div class="profile-title">以下是你的真实考勤异常记录</div>
          <div class="profile-meta">
            当前已同步 {{ attendanceRecords.length }} 条异常记录，可按课程、异常类型和日期筛选
          </div>
          <div class="profile-time">最近更新时间：{{ latestUpdatedAt }}</div>
        </div>
      </el-card>

      <el-row :gutter="16">
        <el-col v-for="card in summaryCards" :key="card.key" :xs="24" :sm="12" :md="12" :lg="6" :xl="6">
          <el-card shadow="hover" class="summary-card" :class="`tone-${card.tone}`">
            <div class="summary-head">
              <span>{{ card.title }}</span>
              <el-icon><component :is="card.icon" /></el-icon>
            </div>
            <div class="summary-value">{{ card.value }}</div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" class="filter-card">
        <el-form :inline="true" label-position="top" class="filter-form">
          <el-form-item label="关键词">
            <el-input v-model="filterForm.keyword" placeholder="课程 / 教师 / 异常说明" clearable />
          </el-form-item>
          <el-form-item label="异常类型">
            <el-select v-model="filterForm.abnormalType" placeholder="请选择类型" clearable>
              <el-option v-for="item in abnormalTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="处理状态">
            <el-select v-model="filterForm.status" placeholder="请选择状态" clearable>
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="日期范围">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              value-format="YYYY-MM-DD"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            />
          </el-form-item>
          <el-form-item class="filter-actions">
            <el-button type="primary" :icon="Search" @click="loadAttendanceExceptions">刷新真实数据</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-row :gutter="16">
        <el-col :xs="24" :sm="24" :md="24" :lg="16" :xl="16">
          <el-card v-loading="loading" shadow="never" class="table-card">
            <template #header>
              <div class="panel-title">异常记录明细（{{ filteredRecords.length }} 条）</div>
            </template>
            <el-table :data="filteredRecords" stripe>
              <el-table-column prop="id" label="记录编号" min-width="158" />
              <el-table-column prop="date" label="日期" min-width="102" />
              <el-table-column prop="courseName" label="课程" min-width="110" />
              <el-table-column prop="classTime" label="上课时间" min-width="128" />
              <el-table-column label="异常类型" min-width="96">
                <template #default="{ row }">
                  <el-tag :type="getTypeTag(row.abnormalType)" effect="light">{{ row.abnormalType }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="处理状态" min-width="120">
                <template #default="{ row }">
                  <el-tag :type="getStatusTag(row.status)" effect="dark">{{ row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="teacherName" label="任课教师" min-width="96" />
              <el-table-column label="操作" width="92" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="openDetail(row)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="24" :md="24" :lg="8" :xl="8">
          <el-card shadow="never" class="highlight-card">
            <template #header>
              <div class="panel-title">最近异常提醒</div>
            </template>
            <div v-if="latestAbnormal" class="highlight-body">
              <div class="highlight-row">
                <span>日期</span>
                <span>{{ latestAbnormal.date }} {{ latestAbnormal.weekDay }}</span>
              </div>
              <div class="highlight-row">
                <span>课程</span>
                <span>{{ latestAbnormal.courseName }}</span>
              </div>
              <div class="highlight-row">
                <span>类型</span>
                <el-tag size="small" :type="getTypeTag(latestAbnormal.abnormalType)">{{ latestAbnormal.abnormalType }}</el-tag>
              </div>
              <div class="highlight-row">
                <span>考勤时间</span>
                <span>{{ latestAbnormal.checkTime }}</span>
              </div>
              <el-divider />
              <div class="highlight-label">异常说明</div>
              <p class="highlight-text">{{ latestAbnormal.reason }}</p>
              <div class="highlight-label">处理备注</div>
              <p class="highlight-text">{{ latestAbnormal.handleNote }}</p>
            </div>
            <el-empty v-else description="暂无匹配记录" :image-size="84" />
          </el-card>
        </el-col>
      </el-row>

      <el-drawer v-model="detailVisible" title="异常记录详情" size="460px" append-to-body>
        <div v-if="selectedRecord" class="detail-panel">
          <div class="detail-row"><span class="label">记录编号</span><span>{{ selectedRecord.id }}</span></div>
          <div class="detail-row"><span class="label">日期</span><span>{{ selectedRecord.date }} {{ selectedRecord.weekDay }}</span></div>
          <div class="detail-row"><span class="label">课程</span><span>{{ selectedRecord.courseName }}</span></div>
          <div class="detail-row"><span class="label">上课时间</span><span>{{ selectedRecord.classTime }}</span></div>
          <div class="detail-row"><span class="label">考勤时间</span><span>{{ selectedRecord.checkTime }}</span></div>
          <div class="detail-row"><span class="label">异常类型</span><span>{{ selectedRecord.abnormalType }}</span></div>
          <div class="detail-row"><span class="label">处理状态</span><span>{{ selectedRecord.status }}</span></div>
          <div class="detail-row"><span class="label">任课教师</span><span>{{ selectedRecord.teacherName }}</span></div>
          <div class="detail-block">
            <div class="label">异常说明</div>
            <div>{{ selectedRecord.reason }}</div>
          </div>
          <div class="detail-block">
            <div class="label">处理备注</div>
            <div>{{ selectedRecord.handleNote }}</div>
          </div>
          <div class="timestamps">
            <div>创建时间：{{ selectedRecord.createdAt }}</div>
            <div>更新时间：{{ selectedRecord.updatedAt }}</div>
          </div>
        </div>
      </el-drawer>
    </div>
  </PageShell>
</template>

<style scoped>
.student-abnormal-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.profile-card,
.summary-card,
.filter-card,
.table-card,
.highlight-card {
  border-radius: 12px;
}

.profile-main {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.profile-title {
  font-size: 20px;
  line-height: 1.4;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.profile-meta,
.profile-time {
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
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.summary-head .el-icon {
  font-size: 20px;
}

.summary-value {
  margin-top: 10px;
  color: var(--el-text-color-primary);
  font-size: 30px;
  line-height: 1.2;
  font-weight: 700;
}

.tone-blue .el-icon {
  color: #409eff;
}

.tone-orange .el-icon {
  color: #e6a23c;
}

.tone-red .el-icon {
  color: #f56c6c;
}

.tone-purple .el-icon {
  color: #8a65ff;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 12px;
}

.filter-actions :deep(.el-form-item__content) {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.panel-title {
  font-size: 15px;
  font-weight: 700;
}

.highlight-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.highlight-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 14px;
}

.highlight-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.highlight-text {
  margin: 0;
  padding: 10px 12px;
  border-radius: 8px;
  background: var(--el-fill-color-light);
  line-height: 1.6;
  font-size: 13px;
}

.detail-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 14px;
}

.label {
  color: var(--el-text-color-secondary);
}

.detail-block {
  border-radius: 8px;
  padding: 10px 12px;
  background: var(--el-fill-color-light);
  line-height: 1.6;
  font-size: 13px;
}

.timestamps {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  line-height: 1.6;
}

:global(html.dark) .highlight-text,
:global(html.dark) .detail-block {
  background: #1a2029;
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

