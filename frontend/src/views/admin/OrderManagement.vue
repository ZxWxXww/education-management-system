<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createAdminOrder, fetchAdminOrderDetail, fetchAdminOrderPage, fetchOrderReferenceOptions } from '../../api/admin/order'
import { BILL_STATUS_OPTIONS, PAY_STATUS_OPTIONS, getStatusMeta } from '../../constants/status'

const loading = ref(false)
const createLoading = ref(false)
const detailLoading = ref(false)
const createDialogVisible = ref(false)
const detailVisible = ref(false)
const orderRows = ref([])
const studentOptions = ref([])
const courseOptions = ref([])
const currentDetail = ref(null)

const filterForm = reactive({
  keyword: '',
  orderType: '全部类型',
  payStatus: '全部状态'
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const orderTypeOptions = ['全部类型', '课时包缴费', '教材费', '考试费', '其他']
const createOrderTypeOptions = orderTypeOptions.filter((item) => item !== '全部类型')
const payStatusOptions = ['全部状态', ...PAY_STATUS_OPTIONS.map((item) => item.label)]

function createEmptyItem() {
  return {
    courseId: null,
    itemName: '',
    quantity: 1,
    unitPrice: 0,
    hourCount: ''
  }
}

const createForm = reactive({
  studentId: null,
  orderType: '课时包缴费',
  dueDate: '',
  remark: '',
  items: [createEmptyItem()]
})

const hasStudentOptions = computed(() => studentOptions.value.length > 0)

const totalAmount = computed(() => orderRows.value.reduce((sum, row) => sum + Number(row.amountTotal || 0), 0))
const totalPaid = computed(() => orderRows.value.reduce((sum, row) => sum + Number(row.amountPaid || 0), 0))
const unpaidAmount = computed(() => Math.max(0, totalAmount.value - totalPaid.value))
const unpaidCount = computed(() => orderRows.value.filter((row) => row.payStatusValue === 'UNPAID').length)

const summaryCards = computed(() => [
  { label: '当前页订单数', value: `${orderRows.value.length}`, tip: `总记录 ${pagination.total}` },
  { label: '当前页订单金额', value: formatCurrency(totalAmount.value), tip: '基于真实订单汇总' },
  { label: '当前页已支付', value: formatCurrency(totalPaid.value), tip: '由支付登记回写' },
  { label: '当前页未支付', value: formatCurrency(unpaidAmount.value), tip: `未支付订单 ${unpaidCount.value} 笔` }
])

const createButtonDisabled = computed(() => {
  if (!hasStudentOptions.value || !createForm.studentId) {
    return true
  }
  if (!Array.isArray(createForm.items) || createForm.items.length === 0) {
    return true
  }
  return createForm.items.some((item) => !item.itemName.trim() || Number(item.quantity) <= 0 || Number(item.unitPrice) <= 0)
})

function formatCurrency(value) {
  return `￥ ${Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

function getPayStatusType(status) {
  return getStatusMeta(PAY_STATUS_OPTIONS, status).tagType
}

function getBillStatusType(status) {
  return getStatusMeta(BILL_STATUS_OPTIONS, status).tagType
}

async function loadReferenceOptions() {
  try {
    const data = await fetchOrderReferenceOptions()
    studentOptions.value = data.students
    courseOptions.value = data.courses
  } catch (error) {
    ElMessage.error(error.message || '订单基础选项加载失败')
  }
}

async function loadOrderPage() {
  loading.value = true
  try {
    const page = await fetchAdminOrderPage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: filterForm.keyword.trim(),
      orderType: filterForm.orderType,
      payStatus: filterForm.payStatus
    })
    orderRows.value = page.list
    pagination.total = page.total
  } catch (error) {
    ElMessage.error(error.message || '订单列表加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  loadOrderPage()
}

function handleReset() {
  filterForm.keyword = ''
  filterForm.orderType = '全部类型'
  filterForm.payStatus = '全部状态'
  pagination.pageNum = 1
  loadOrderPage()
}

function handleCurrentChange(pageNum) {
  pagination.pageNum = pageNum
  loadOrderPage()
}

function handleSizeChange(pageSize) {
  pagination.pageSize = pageSize
  pagination.pageNum = 1
  loadOrderPage()
}

function resetCreateForm() {
  createForm.studentId = null
  createForm.orderType = '课时包缴费'
  createForm.dueDate = ''
  createForm.remark = ''
  createForm.items = [createEmptyItem()]
}

function openCreateDialog() {
  resetCreateForm()
  createDialogVisible.value = true
}

function addItem() {
  createForm.items.push(createEmptyItem())
}

function removeItem(index) {
  if (createForm.items.length === 1) {
    return
  }
  createForm.items.splice(index, 1)
}

function getItemAmount(item) {
  return Number(item.quantity || 0) * Number(item.unitPrice || 0)
}

async function saveOrder() {
  if (createButtonDisabled.value) {
    return
  }

  createLoading.value = true
  try {
    await createAdminOrder({
      studentId: createForm.studentId,
      orderType: createForm.orderType,
      dueDate: createForm.dueDate,
      remark: createForm.remark,
      items: createForm.items.map((item) => ({
        courseId: item.courseId,
        itemName: item.itemName.trim(),
        quantity: item.quantity,
        unitPrice: item.unitPrice,
        hourCount: item.hourCount
      }))
    })
    ElMessage.success('订单创建成功，账单已自动生成')
    createDialogVisible.value = false
    await loadOrderPage()
  } catch (error) {
    ElMessage.error(error.message || '订单创建失败')
  } finally {
    createLoading.value = false
  }
}

async function openDetail(row) {
  detailLoading.value = true
  detailVisible.value = true
  currentDetail.value = null
  try {
    currentDetail.value = await fetchAdminOrderDetail(row.id)
  } catch (error) {
    ElMessage.error(error.message || '订单详情加载失败')
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

onMounted(async () => {
  await loadReferenceOptions()
  await loadOrderPage()
})
</script>

<template>
  <div class="order-page">
    <section class="order-head">
      <div>
        <h1 class="order-title">订单管理</h1>
        <p class="order-subtitle">管理员端 / 财务统计 / 订单管理</p>
      </div>
      <div class="order-head-actions">
        <el-button type="primary" size="large" @click="openCreateDialog">新建订单</el-button>
      </div>
    </section>

    <section class="summary-grid">
      <article v-for="item in summaryCards" :key="item.label" class="summary-card">
        <p class="summary-label">{{ item.label }}</p>
        <p class="summary-value">{{ item.value }}</p>
        <p class="summary-tip">{{ item.tip }}</p>
      </article>
    </section>

    <section class="filter-panel">
      <el-input
        v-model="filterForm.keyword"
        placeholder="请输入订单号/备注"
        clearable
        @keyup.enter="handleSearch"
      />
      <el-select v-model="filterForm.orderType">
        <el-option v-for="option in orderTypeOptions" :key="option" :label="option" :value="option" />
      </el-select>
      <el-select v-model="filterForm.payStatus">
        <el-option v-for="option in payStatusOptions" :key="option" :label="option" :value="option" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </section>

    <section class="panel">
      <header class="panel-head panel-head--single">
        <h2>订单列表</h2>
      </header>
      <el-table v-loading="loading" :data="orderRows" stripe>
        <el-table-column prop="orderNo" label="订单号" min-width="170" />
        <el-table-column prop="studentName" label="学员" width="130" />
        <el-table-column prop="orderType" label="订单类型" width="120" />
        <el-table-column label="订单金额" width="130">
          <template #default="{ row }">
            {{ formatCurrency(row.amountTotal) }}
          </template>
        </el-table-column>
        <el-table-column label="已支付" width="130">
          <template #default="{ row }">
            {{ formatCurrency(row.amountPaid) }}
          </template>
        </el-table-column>
        <el-table-column label="支付状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getPayStatusType(row.payStatusValue)" effect="light">{{ row.payStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="itemCount" label="明细数" width="84" />
        <el-table-column prop="billNo" label="关联账单" min-width="170" />
        <el-table-column label="账单状态" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.billStatusValue" :type="getBillStatusType(row.billStatusValue)" effect="plain">
              {{ row.billStatus }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="dueDate" label="账单截止日" width="120" />
        <el-table-column prop="createdAt" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="96" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看</el-button>
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

    <el-dialog v-model="createDialogVisible" title="新建订单" width="860px">
      <el-form label-width="96px" class="create-order-form">
        <el-alert
          v-if="!hasStudentOptions"
          type="warning"
          :closable="false"
          title="当前没有可用于建单的学员档案，请先补齐学员资料。"
          show-icon
        />
        <div class="create-grid">
          <el-form-item label="学员">
            <el-select v-model="createForm.studentId" placeholder="请选择学员" filterable style="width: 100%">
              <el-option v-for="item in studentOptions" :key="item.id" :label="item.label" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="订单类型">
            <el-select v-model="createForm.orderType" style="width: 100%">
              <el-option v-for="option in createOrderTypeOptions" :key="option" :label="option" :value="option" />
            </el-select>
          </el-form-item>
          <el-form-item label="账单截止日">
            <el-date-picker
              v-model="createForm.dueDate"
              type="date"
              value-format="YYYY-MM-DD"
              format="YYYY-MM-DD"
              placeholder="请选择截止日期"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="订单备注" class="create-grid__remark">
            <el-input v-model="createForm.remark" type="textarea" :rows="2" placeholder="请输入订单备注" />
          </el-form-item>
        </div>

        <div class="item-head">
          <h3>订单明细</h3>
          <el-button type="primary" link @click="addItem">新增明细</el-button>
        </div>
        <div class="item-list">
          <div v-for="(item, index) in createForm.items" :key="index" class="item-card">
            <div class="item-card__grid">
              <el-form-item label="明细名称">
                <el-input v-model="item.itemName" placeholder="如：数学 20 课时包" />
              </el-form-item>
              <el-form-item label="关联课程">
                <el-select v-model="item.courseId" placeholder="可选" clearable filterable style="width: 100%">
                  <el-option v-for="course in courseOptions" :key="course.id" :label="course.label" :value="course.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="数量">
                <el-input-number v-model="item.quantity" :min="1" :step="1" style="width: 100%" />
              </el-form-item>
              <el-form-item label="单价">
                <el-input-number v-model="item.unitPrice" :min="0" :step="100" style="width: 100%" />
              </el-form-item>
              <el-form-item label="课时数">
                <el-input-number v-model="item.hourCount" :min="0" :step="1" style="width: 100%" />
              </el-form-item>
              <el-form-item label="小计">
                <div class="line-amount">{{ formatCurrency(getItemAmount(item)) }}</div>
              </el-form-item>
            </div>
            <div class="item-card__footer">
              <el-button link type="danger" :disabled="createForm.items.length === 1" @click="removeItem(index)">删除明细</el-button>
            </div>
          </div>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <span class="dialog-footer__total">订单合计 {{ formatCurrency(createForm.items.reduce((sum, item) => sum + getItemAmount(item), 0)) }}</span>
          <div class="dialog-footer__actions">
            <el-button @click="createDialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="createLoading" :disabled="createButtonDisabled" @click="saveOrder">
              创建订单
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="订单详情" width="860px">
      <div v-loading="detailLoading" class="order-detail">
        <template v-if="currentDetail">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单号">{{ currentDetail.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="学员">{{ currentDetail.studentName }}</el-descriptions-item>
            <el-descriptions-item label="订单类型">{{ currentDetail.orderType }}</el-descriptions-item>
            <el-descriptions-item label="支付状态">{{ currentDetail.payStatus }}</el-descriptions-item>
            <el-descriptions-item label="订单金额">{{ formatCurrency(currentDetail.amountTotal) }}</el-descriptions-item>
            <el-descriptions-item label="已支付">{{ formatCurrency(currentDetail.amountPaid) }}</el-descriptions-item>
            <el-descriptions-item label="关联账单">{{ currentDetail.billNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="账单状态">{{ currentDetail.billStatus || '-' }}</el-descriptions-item>
            <el-descriptions-item label="账单截止日">{{ currentDetail.dueDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ currentDetail.createdAt }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ currentDetail.remark || '-' }}</el-descriptions-item>
          </el-descriptions>

          <div class="detail-table">
            <h3>订单明细</h3>
            <el-table :data="currentDetail.items" stripe>
              <el-table-column prop="itemName" label="明细名称" min-width="180" />
              <el-table-column prop="courseName" label="关联课程" min-width="160" />
              <el-table-column prop="quantity" label="数量" width="80" />
              <el-table-column label="单价" width="120">
                <template #default="{ row }">
                  {{ formatCurrency(row.unitPrice) }}
                </template>
              </el-table-column>
              <el-table-column prop="hourCount" label="课时数" width="90" />
              <el-table-column label="小计" width="120">
                <template #default="{ row }">
                  {{ formatCurrency(row.lineAmount) }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.order-page {
  max-width: 1240px;
  margin: 0 auto;
  padding: 8px 4px 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.order-title {
  margin: 0;
  color: #111827;
  font-size: 28px;
  line-height: 38px;
  font-weight: 700;
}

.order-subtitle {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 14px;
  line-height: 22px;
}

.order-head-actions {
  display: flex;
  gap: 12px;
}

.summary-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.summary-card,
.panel,
.filter-panel,
.item-card {
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
}

.summary-card {
  padding: 16px;
}

.summary-label {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}

.summary-value {
  margin: 8px 0 4px;
  color: #111827;
  font-size: 24px;
  line-height: 32px;
  font-weight: 700;
}

.summary-tip {
  margin: 0;
  color: #9ca3af;
  font-size: 12px;
}

.filter-panel {
  padding: 14px;
  display: grid;
  gap: 10px;
  grid-template-columns: minmax(220px, 1.8fr) repeat(2, minmax(140px, 1fr)) auto auto;
}

.panel {
  padding: 18px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.panel-head--single {
  justify-content: flex-start;
}

.panel-head h2 {
  margin: 0;
  color: #111827;
  font-size: 18px;
  line-height: 28px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}

.create-order-form {
  padding-right: 6px;
}

.create-grid {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.create-grid__remark {
  grid-column: 1 / -1;
}

.item-head,
.item-card__footer,
.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.item-head {
  margin: 8px 0 12px;
}

.item-head h3,
.detail-table h3 {
  margin: 0;
  color: #111827;
  font-size: 16px;
}

.item-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item-card {
  padding: 14px;
}

.item-card__grid {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.line-amount {
  display: flex;
  min-height: 32px;
  align-items: center;
  color: #111827;
  font-weight: 600;
}

.dialog-footer {
  width: 100%;
  gap: 12px;
}

.dialog-footer__total {
  color: #111827;
  font-weight: 600;
}

.dialog-footer__actions {
  display: flex;
  gap: 8px;
}

.order-detail {
  min-height: 120px;
}

.detail-table {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

@media (max-width: 1120px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .filter-panel,
  .create-grid,
  .item-card__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .order-head {
    flex-direction: column;
  }

  .order-head-actions {
    width: 100%;
  }

  .order-head-actions :deep(.el-button) {
    flex: 1;
  }

  .summary-grid,
  .filter-panel,
  .create-grid,
  .item-card__grid {
    grid-template-columns: 1fr;
  }

  .pagination-wrap {
    justify-content: center;
    overflow-x: auto;
  }

  .dialog-footer {
    flex-direction: column;
    align-items: stretch;
  }

  .dialog-footer__actions {
    width: 100%;
  }

  .dialog-footer__actions :deep(.el-button) {
    flex: 1;
  }
}

:global(html.dark) .order-title,
:global(html.dark) .panel-head h2,
:global(html.dark) .summary-value,
:global(html.dark) .item-head h3,
:global(html.dark) .detail-table h3,
:global(html.dark) .line-amount,
:global(html.dark) .dialog-footer__total {
  color: #e5e7eb;
}

:global(html.dark) .order-subtitle,
:global(html.dark) .summary-label,
:global(html.dark) .summary-tip {
  color: #9ca3af;
}

:global(html.dark) .summary-card,
:global(html.dark) .panel,
:global(html.dark) .filter-panel,
:global(html.dark) .item-card {
  background: #1f2937;
  border-color: #374151;
}
</style>
