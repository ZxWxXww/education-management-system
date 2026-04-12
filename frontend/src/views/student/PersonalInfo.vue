<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { EditPen } from '@element-plus/icons-vue'
import PageShell from '../../components/PageShell.vue'
import { fetchCurrentStudentProfile, updateCurrentStudentProfile } from '../../api/student/profile'
import { fetchStudentNotificationPage, markStudentNotificationRead } from '../../api/student/notification'
import { fetchStudentBillDetail, fetchStudentBillPage, fetchStudentHourPackageSummary } from '../../api/student/bill'
import { fetchStudentCoursePage } from '../../api/student/course'
import { fetchStudentAssignmentSubmissionPage } from '../../api/student/assignment'
import { fetchStudentScorePage } from '../../api/student/score'
import { ASSIGNMENT_STATUS, BILL_STATUS, BILL_STATUS_OPTIONS, HOUR_PACKAGE_STATUS_OPTIONS, getStatusMeta } from '../../constants/status'

const props = defineProps({
  embedded: { type: Boolean, default: false }
})

function createEmptyProfile() {
  return {
    userId: null,
    studentProfileId: null,
    studentNo: '-',
    name: '',
    gender: '未知',
    grade: '',
    className: '',
    phone: '',
    email: '',
    guardianName: '',
    guardianPhone: '',
    address: '',
    intro: '',
    createdAt: '-',
    updatedAt: '-'
  }
}

const profileForm = reactive(createEmptyProfile())
const profileSnapshot = ref(createEmptyProfile())
const profileLoading = ref(false)
const saveLoading = ref(false)
const announcementLoading = ref(false)
const billLoading = ref(false)
const hourPackageLoading = ref(false)
const announcementList = ref([])
const billList = ref([])
const hourPackageSummary = ref({
  totalRemainingHours: 0,
  activePackageCount: 0,
  packages: [],
  deductions: []
})
const courseList = ref([])
const assignmentList = ref([])
const scoreList = ref([])
const billDialogVisible = ref(false)
const currentBill = ref(null)
const billDetailLoading = ref(false)

const subjectTags = computed(() => courseList.value.map((item) => item.courseName).filter(Boolean).slice(0, 4))
const learningStats = computed(() => {
  const totalAssignments = assignmentList.value.length
  const submittedAssignments = assignmentList.value.filter(
    (item) => item.status === ASSIGNMENT_STATUS.SUBMITTED || item.status === ASSIGNMENT_STATUS.LATE
  ).length
  const avgScore = scoreList.value.length
    ? (scoreList.value.reduce((sum, item) => sum + Number(item.score || 0), 0) / scoreList.value.length).toFixed(1)
    : '0.0'
  return [
    { label: '当前课程数', value: `${courseList.value.length} 门` },
    { label: '作业提交率', value: `${totalAssignments ? Math.round((submittedAssignments / totalAssignments) * 100) : 0}%` },
    { label: '最近成绩均分', value: avgScore },
    { label: '未读通知', value: `${unreadAnnouncementCount.value} 条` }
  ]
})

const unreadAnnouncementCount = computed(() =>
  announcementList.value.filter((item) => !item.isRead).length
)
const unpaidBillCount = computed(() =>
  billList.value.filter((item) => item.paymentStatus !== 'paid').length
)
const recentHourPackages = computed(() => hourPackageSummary.value.packages.slice(0, 3))
const recentHourDeductions = computed(() => hourPackageSummary.value.deductions.slice(0, 5))
const hourPackageStats = computed(() => [
  { label: '剩余课时', value: `${hourPackageSummary.value.totalRemainingHours.toFixed(2)} 课时` },
  { label: '生效课时包', value: `${hourPackageSummary.value.activePackageCount} 个` }
])

function syncProfileForm(profile) {
  Object.assign(profileForm, createEmptyProfile(), profile)
  profileSnapshot.value = { ...profileForm }
}

function handleReset() {
  Object.assign(profileForm, profileSnapshot.value)
  ElMessage.success('已恢复为最近一次同步的真实资料')
}

async function handleSave() {
  saveLoading.value = true
  try {
    await updateCurrentStudentProfile(profileForm)
    await loadProfile()
    ElMessage.success('个人信息保存成功')
  } finally {
    saveLoading.value = false
  }
}

function statusTagType(status) {
  return getStatusMeta(BILL_STATUS_OPTIONS, status).tagType
}

function paymentTagType(status) {
  return status === 'paid' ? 'success' : 'warning'
}

function paymentTagText(status) {
  return status === 'paid' ? '已支付' : '待支付'
}

function hourPackageTagType(status) {
  return getStatusMeta(HOUR_PACKAGE_STATUS_OPTIONS, status).tagType
}

async function openBillDetail(bill) {
  billDetailLoading.value = true
  billDialogVisible.value = true
  try {
    currentBill.value = await fetchStudentBillDetail(bill.id)
  } finally {
    billDetailLoading.value = false
  }
}

async function openAnnouncement(item) {
  if (item.isRead) return
  await markStudentNotificationRead(item.id)
  item.isRead = true
}

async function loadProfile() {
  profileLoading.value = true
  try {
    const profile = await fetchCurrentStudentProfile()
    syncProfileForm(profile)
  } finally {
    profileLoading.value = false
  }
}

async function loadAnnouncements() {
  announcementLoading.value = true
  try {
    const page = await fetchStudentNotificationPage({ pageNum: 1, pageSize: 5 })
    announcementList.value = page.list
  } finally {
    announcementLoading.value = false
  }
}

async function loadBills() {
  billLoading.value = true
  try {
    const page = await fetchStudentBillPage({ pageNum: 1, pageSize: 5 })
    billList.value = page.list
  } finally {
    billLoading.value = false
  }
}

async function loadHourPackages() {
  hourPackageLoading.value = true
  try {
    hourPackageSummary.value = await fetchStudentHourPackageSummary()
  } finally {
    hourPackageLoading.value = false
  }
}

async function loadStudyData() {
  const [coursePage, assignmentPage, scorePage] = await Promise.all([
    fetchStudentCoursePage({ pageNum: 1, pageSize: 20 }),
    fetchStudentAssignmentSubmissionPage({ pageNum: 1, pageSize: 20 }),
    fetchStudentScorePage({ pageNum: 1, pageSize: 20 })
  ])
  courseList.value = coursePage.list
  assignmentList.value = assignmentPage.list
  scoreList.value = scorePage.list
}

async function loadPageData() {
  try {
    await Promise.all([loadProfile(), loadAnnouncements(), loadBills(), loadHourPackages(), loadStudyData()])
  } catch (error) {
    // 统一请求层已处理通用报错，这里只补充页面级上下文提示。
    ElMessage.error('学生个人中心数据加载失败，请稍后重试')
  }
}

onMounted(() => {
  loadPageData()
})

const shellProps = {
  title: '个人信息',
  subtitle: '个人信息 / 修改密码'
}
</script>

<template>
  <component :is="props.embedded ? 'div' : PageShell" v-bind="props.embedded ? {} : shellProps">
    <div class="personal-info">
      <section v-loading="profileLoading" class="profile-overview">
        <div class="avatar-box">{{ profileForm.name.slice(0, 1) }}</div>
        <p class="profile-name">{{ profileForm.name }}</p>
        <p class="profile-meta">{{ profileForm.grade }} · {{ profileForm.className }}</p>
        <div class="tag-group">
          <el-tag v-for="tag in subjectTags" :key="tag" effect="plain" round>{{ tag }}</el-tag>
        </div>
        <div class="meta-list">
          <div class="meta-item">
            <span>学号</span>
            <strong>{{ profileForm.studentNo }}</strong>
          </div>
          <div class="meta-item">
            <span>监护人</span>
            <strong>{{ profileForm.guardianName }}</strong>
          </div>
          <div class="meta-item">
            <span>创建时间</span>
            <strong>{{ profileForm.createdAt }}</strong>
          </div>
          <div class="meta-item">
            <span>更新时间</span>
            <strong>{{ profileForm.updatedAt }}</strong>
          </div>
        </div>
      </section>

      <section v-loading="profileLoading" class="profile-form">
        <div class="section-head">
          <h3 class="section-title">基础信息</h3>
          <el-icon><EditPen /></el-icon>
        </div>
        <el-form :model="profileForm" label-width="92px" class="form-grid">
          <el-form-item label="学生学号">
            <el-input v-model="profileForm.studentNo" disabled />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="profileForm.name" />
          </el-form-item>
          <el-form-item label="性别">
            <el-input v-model="profileForm.gender" disabled />
          </el-form-item>
          <el-form-item label="年级">
            <el-select v-model="profileForm.grade" style="width: 100%">
              <el-option label="高一" value="高一" />
              <el-option label="高二" value="高二" />
              <el-option label="高三" value="高三" />
            </el-select>
          </el-form-item>
          <el-form-item label="班级">
            <el-input v-model="profileForm.className" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="profileForm.phone" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="profileForm.email" />
          </el-form-item>
          <el-form-item label="监护人">
            <el-input v-model="profileForm.guardianName" />
          </el-form-item>
          <el-form-item label="监护人电话">
            <el-input v-model="profileForm.guardianPhone" />
          </el-form-item>
          <el-form-item label="家庭住址" class="form-grid__full">
            <el-input v-model="profileForm.address" />
          </el-form-item>
          <el-form-item label="学习备注" class="form-grid__full">
            <el-input v-model="profileForm.intro" type="textarea" :rows="4" />
          </el-form-item>
        </el-form>

        <h3 class="section-title section-title--spaced">学习概览</h3>
        <div class="stat-grid">
          <div v-for="item in learningStats" :key="item.label" class="stat-item">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
          </div>
        </div>

        <div class="notice-head">
          <h3 class="section-title section-title--spaced">通知公告</h3>
          <el-tag :type="unreadAnnouncementCount > 0 ? 'warning' : 'success'" effect="light">
            未读通知 {{ unreadAnnouncementCount }} 条
          </el-tag>
        </div>
        <div v-loading="announcementLoading" v-if="announcementList.length > 0" class="notice-list">
          <article v-for="notice in announcementList" :key="notice.id" class="notice-item">
            <div class="notice-main">
              <p class="notice-title">{{ notice.title }}</p>
              <p class="notice-content">{{ notice.content }}</p>
              <p class="notice-meta">
                发布时间：{{ notice.publishTime }} ｜ 类型：{{ notice.noticeType || '公告' }}
              </p>
            </div>
            <div class="notice-actions">
              <el-tag :type="notice.isRead ? 'success' : 'warning'" effect="light">
                {{ notice.isRead ? '已读' : '未读' }}
              </el-tag>
              <el-button type="primary" link @click="openAnnouncement(notice)">
                {{ notice.isRead ? '已查看' : '标为已读' }}
              </el-button>
            </div>
          </article>
        </div>
        <el-empty v-else description="暂无通知公告" :image-size="86" />

        <div class="notice-head">
          <h3 class="section-title section-title--spaced">账单信息</h3>
          <el-tag :type="unpaidBillCount > 0 ? 'warning' : 'success'" effect="light">
            待处理账单 {{ unpaidBillCount }} 条
          </el-tag>
        </div>
        <div v-loading="billLoading" v-if="billList.length > 0" class="notice-list">
          <article v-for="bill in billList" :key="bill.id" class="notice-item">
            <div class="notice-main">
              <p class="notice-title">{{ bill.billTypeLabel }}</p>
              <p class="notice-content">{{ bill.remark || '暂无账单备注' }}</p>
              <p class="notice-meta">
                财单号：{{ bill.billNo }} ｜ 班级：{{ bill.className }} ｜ 到期：{{ bill.dueDate }}
              </p>
            </div>
            <div class="notice-actions">
              <el-tag :type="paymentTagType(bill.paymentStatus)" effect="light">
                {{ paymentTagText(bill.paymentStatus) }}
              </el-tag>
              <el-button type="primary" link @click="openBillDetail(bill)">查看账单</el-button>
            </div>
          </article>
        </div>
        <el-empty v-else description="暂无账单信息" :image-size="86" />

        <div class="notice-head">
          <h3 class="section-title section-title--spaced">课时包与扣课</h3>
          <el-tag :type="hourPackageSummary.totalRemainingHours > 0 ? 'success' : 'info'" effect="light">
            剩余 {{ hourPackageSummary.totalRemainingHours.toFixed(2) }} 课时
          </el-tag>
        </div>
        <div v-loading="hourPackageLoading" class="hour-package-panel">
          <div class="hour-package-grid">
            <div v-for="item in hourPackageStats" :key="item.label" class="stat-item">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </div>
          </div>

          <div class="hour-package-block">
            <div class="mini-head">
              <strong>当前课时包</strong>
              <span>{{ recentHourPackages.length }} 条</span>
            </div>
            <div v-if="recentHourPackages.length > 0" class="notice-list">
              <article v-for="item in recentHourPackages" :key="item.id" class="notice-item">
                <div class="notice-main">
                  <p class="notice-title">{{ item.courseName }}</p>
                  <p class="notice-content">
                    总课时 {{ item.totalHours.toFixed(2) }} ｜ 已用 {{ item.usedHours.toFixed(2) }} ｜ 剩余 {{ item.remainingHours.toFixed(2) }}
                  </p>
                  <p class="notice-meta">
                    生效：{{ item.effectiveDate }} ｜ 到期：{{ item.expireDate }} ｜ 更新：{{ item.updatedAt }}
                  </p>
                </div>
                <div class="notice-actions">
                  <el-tag :type="hourPackageTagType(item.statusCode)" effect="light">
                    {{ item.statusLabel }}
                  </el-tag>
                </div>
              </article>
            </div>
            <el-empty v-else description="暂无课时包" :image-size="72" />
          </div>

          <div class="hour-package-block">
            <div class="mini-head">
              <strong>最近扣课</strong>
              <span>{{ recentHourDeductions.length }} 条</span>
            </div>
            <div v-if="recentHourDeductions.length > 0" class="notice-list">
              <article v-for="item in recentHourDeductions" :key="item.id" class="notice-item">
                <div class="notice-main">
                  <p class="notice-title">{{ item.courseName }} · {{ item.className }}</p>
                  <p class="notice-content">扣减 {{ item.deductHours.toFixed(2) }} 课时</p>
                  <p class="notice-meta">
                    业务日期：{{ item.bizDate }} ｜ 记录时间：{{ item.createdAt }}<span v-if="item.remark"> ｜ {{ item.remark }}</span>
                  </p>
                </div>
              </article>
            </div>
            <el-empty v-else description="暂无扣课记录" :image-size="72" />
          </div>
        </div>

        <div class="action-row">
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" :loading="saveLoading" @click="handleSave">保存信息</el-button>
        </div>
      </section>
    </div>

    <el-dialog v-model="billDialogVisible" title="账单详情" width="520px">
      <div v-loading="billDetailLoading" v-if="currentBill" class="bill-detail">
        <div class="bill-row">
          <span>财单号</span>
          <strong>{{ currentBill.billNo }}</strong>
        </div>
        <div class="bill-row">
          <span>班级</span>
          <strong>{{ currentBill.className }}</strong>
        </div>
        <div class="bill-row">
          <span>账单类型</span>
          <strong>{{ currentBill.billTypeLabel }}</strong>
        </div>
        <div class="bill-row">
          <span>应收金额</span>
          <strong>¥ {{ Number(currentBill.amount || 0).toLocaleString('zh-CN') }}</strong>
        </div>
        <div class="bill-row">
          <span>实收金额</span>
          <strong>¥ {{ Number(currentBill.paidAmount || 0).toLocaleString('zh-CN') }}</strong>
        </div>
        <div class="bill-row">
          <span>账单状态</span>
          <el-tag :type="statusTagType(currentBill.billStatusCode)" effect="light">
            {{ currentBill.billStatusLabel }}
          </el-tag>
        </div>
        <div class="bill-row">
          <span>支付状态</span>
          <el-tag :type="paymentTagType(currentBill.paymentStatus)" effect="light">
            {{ paymentTagText(currentBill.paymentStatus) }}
          </el-tag>
        </div>
        <div class="bill-row">
          <span>到期时间</span>
          <strong>{{ currentBill.dueDate }}</strong>
        </div>
        <div class="bill-row">
          <span>更新时间</span>
          <strong>{{ currentBill.updatedAt }}</strong>
        </div>
      </div>
      <template #footer>
        <div class="bill-footer">
          <el-button @click="billDialogVisible = false">关闭</el-button>
          <span class="bill-footer__tip" v-if="currentBill && currentBill.billStatusCode === BILL_STATUS.PENDING">
            当前仅支持查看真实账单，请联系管理员完成支付登记
          </span>
        </div>
      </template>
    </el-dialog>
  </component>
</template>

<style scoped>
.personal-info {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 16px;
}

.profile-overview,
.profile-form {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  background: #ffffff;
  padding: 20px;
}

.profile-overview {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.avatar-box {
  width: 74px;
  height: 74px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  color: #ffffff;
  display: inline-flex;
  justify-content: center;
  align-items: center;
  font-size: 28px;
  font-weight: 700;
}

.profile-name {
  margin: 2px 0 0;
  color: #111827;
  font-size: 20px;
  line-height: 28px;
  font-weight: 700;
}

.profile-meta {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
  line-height: 20px;
}

.tag-group {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
  margin-bottom: 8px;
}

.meta-list {
  width: 100%;
  border-top: 1px dashed #d1d5db;
  padding-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  color: #6b7280;
  font-size: 12px;
}

.meta-item strong {
  color: #111827;
  font-size: 12px;
  font-weight: 600;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.section-head .el-icon {
  color: #6b7280;
}

.section-title {
  margin: 0;
  color: #111827;
  font-size: 15px;
  line-height: 22px;
  font-weight: 700;
}

.section-title--spaced {
  margin-top: 2px;
  margin-bottom: 14px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 16px;
}

.form-grid :deep(.el-form-item) {
  margin-bottom: 14px;
}

.form-grid__full {
  grid-column: 1 / -1;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.stat-item {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px 12px;
  background: #f9fafb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.stat-item span {
  color: #6b7280;
  font-size: 12px;
}

.stat-item strong {
  color: #111827;
  font-size: 13px;
  font-weight: 700;
}

.action-row {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.notice-head {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.hour-package-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hour-package-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.hour-package-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.mini-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  color: #6b7280;
  font-size: 12px;
}

.mini-head strong {
  color: #111827;
  font-size: 13px;
}

.notice-item {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #f9fafb;
  padding: 12px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.notice-main {
  min-width: 0;
}

.notice-title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #111827;
}

.notice-content {
  margin: 6px 0 0;
  font-size: 13px;
  color: #4b5563;
  line-height: 1.6;
}

.notice-meta {
  margin: 6px 0 0;
  font-size: 12px;
  color: #6b7280;
}

.notice-actions {
  display: inline-flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.bill-detail {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.bill-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  color: #6b7280;
  font-size: 14px;
}

.bill-row strong {
  color: #111827;
  font-weight: 600;
}

.bill-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

@media (max-width: 980px) {
  .personal-info {
    grid-template-columns: 1fr;
  }

  .profile-overview {
    align-items: flex-start;
  }

  .tag-group {
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .form-grid,
  .stat-grid,
  .hour-package-grid {
    grid-template-columns: 1fr;
  }
}

:global(html.dark) .profile-overview,
:global(html.dark) .profile-form,
:global(html.dark) .stat-item,
:global(html.dark) .notice-item {
  background: #1a2028;
  border-color: #2b3442;
}

:global(html.dark) .stat-item {
  background: #141920;
}

:global(html.dark) .profile-name,
:global(html.dark) .meta-item strong,
:global(html.dark) .section-title,
:global(html.dark) .mini-head strong,
:global(html.dark) .stat-item strong,
:global(html.dark) .notice-title,
:global(html.dark) .bill-row strong {
  color: #e5eaf3;
}

:global(html.dark) .profile-meta,
:global(html.dark) .meta-item,
:global(html.dark) .section-head .el-icon,
:global(html.dark) .mini-head,
:global(html.dark) .stat-item span,
:global(html.dark) .notice-content,
:global(html.dark) .notice-meta,
:global(html.dark) .bill-row {
  color: #9ba7ba;
}

:global(html.dark) .meta-list {
  border-top-color: #334155;
}
</style>
