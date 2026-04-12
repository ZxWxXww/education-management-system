<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  createAdminBill,
  fetchAdminBillPage,
  fetchAdminBillPayments,
  fetchBillReferenceOptions,
  registerAdminBillPayment,
  registerAdminBillRefund
} from '../../api/admin/bill'
import { BILL_STATUS_OPTIONS, PAY_CHANNEL, PAY_CHANNEL_OPTIONS, getStatusMeta } from '../../constants/status'

const ALL_BILL_TYPES = '全部类型'
const ALL_BILL_STATUS = '全部状态'

const loading = ref(false)
const createLoading = ref(false)
const paymentLoading = ref(false)
const refundLoading = ref(false)
const detailLoading = ref(false)
const billRows = ref([])
const studentOptions = ref([])
const classOptions = ref([])
const paymentRows = ref([])
const currentDetail = ref(null)

const createDialogVisible = ref(false)
const paymentDialogVisible = ref(false)
const refundDialogVisible = ref(false)
const detailVisible = ref(false)

const filterForm = reactive({
  keyword: '',
  billType: ALL_BILL_TYPES,
  status: ALL_BILL_STATUS,
  month: new Date().toISOString().slice(0, 7)
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const billTypeOptions = [ALL_BILL_TYPES, '课时包缴费', '教材费', '考试费', '退款', '其他']
const createBillTypeOptions = billTypeOptions.filter((item) => item !== ALL_BILL_TYPES)
const statusOptions = [ALL_BILL_STATUS, ...BILL_STATUS_OPTIONS.map((item) => item.label)]
const payChannelOptions = PAY_CHANNEL_OPTIONS
const refundActionOptions = [
  { label: '退款', value: 'REFUND' },
  { label: '冲正', value: 'REVERSAL' },
  { label: '反结算', value: 'UNSETTLE' }
]

const createForm = reactive({
  studentId: null,
  classId: null,
  billType: '课时包缴费',
  amount: 0,
  dueDate: '',
  remark: ''
})

const paymentForm = reactive({
  billId: null,
  billNo: '',
  studentName: '',
  payableAmount: 0,
  paidAmount: 0,
  remainingAmount: 0,
  payAmount: 0,
  payChannel: PAY_CHANNEL.ALIPAY,
  payTime: ''
})

const refundForm = reactive({
  billId: null,
  billNo: '',
  studentName: '',
  paidAmount: 0,
  refundAmount: 0,
  payChannel: PAY_CHANNEL.ALIPAY,
  payTime: '',
  actionType: 'REFUND'
})

const totalAmount = computed(() => billRows.value.reduce((sum, row) => sum + Number(row.amount || 0), 0))
const totalPaid = computed(() => billRows.value.reduce((sum, row) => sum + Number(row.paidAmount || 0), 0))
const totalPending = computed(() => Math.max(0, totalAmount.value - totalPaid.value))
const overdueCount = computed(() => billRows.value.filter((row) => row.statusValue === 'OVERDUE').length)
const hasStudentOptions = computed(() => studentOptions.value.length > 0)

const financeCards = computed(() => {
  const paidRate = totalAmount.value > 0 ? ((totalPaid.value / totalAmount.value) * 100).toFixed(1) : '0.0'
  return [
    { label: '当前页应收总额', value: formatCurrency(totalAmount.value), tip: `账单 ${billRows.value.length} 条` },
    { label: '当前页实收总额', value: formatCurrency(totalPaid.value), tip: `回款率 ${paidRate}%` },
    { label: '当前页待回款', value: formatCurrency(totalPending.value), tip: '仅来源于真实支付记录' },
    { label: '逾期账单数', value: `${overdueCount.value}`, tip: '逾期账单仍可登记支付' }
  ]
})

const createButtonDisabled = computed(() => !hasStudentOptions.value || !createForm.studentId || Number(createForm.amount) <= 0)
const paymentButtonDisabled = computed(() => {
  return !paymentForm.billId || Number(paymentForm.payAmount) <= 0 || Number(paymentForm.payAmount) > Number(paymentForm.remainingAmount)
})
const refundButtonDisabled = computed(() => {
  return !refundForm.billId || Number(refundForm.refundAmount) <= 0 || Number(refundForm.refundAmount) > Number(refundForm.paidAmount)
})

function formatCurrency(value) {
  return `¥${Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

function getStatusType(status) {
  const statusValue = BILL_STATUS_OPTIONS.find((item) => item.value === status || item.label === status)?.value || status
  return getStatusMeta(BILL_STATUS_OPTIONS, statusValue).tagType
}

function resetCreateForm() {
  createForm.studentId = null
  createForm.classId = null
  createForm.billType = '课时包缴费'
  createForm.amount = 0
  createForm.dueDate = ''
  createForm.remark = ''
}

async function loadReferenceOptions() {
  try {
    const data = await fetchBillReferenceOptions()
    studentOptions.value = data.students
    classOptions.value = data.classes
  } catch (error) {
    ElMessage.error(error.message || '账单基础选项加载失败')
  }
}

async function loadBillPage() {
  loading.value = true
  try {
    const page = await fetchAdminBillPage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: filterForm.keyword.trim(),
      billType: filterForm.billType,
      status: filterForm.status,
      month: filterForm.month
    })
    billRows.value = page.list
    pagination.total = page.total
  } catch (error) {
    ElMessage.error(error.message || '账单列表加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  loadBillPage()
}

function handleReset() {
  filterForm.keyword = ''
  filterForm.billType = ALL_BILL_TYPES
  filterForm.status = ALL_BILL_STATUS
  filterForm.month = new Date().toISOString().slice(0, 7)
  pagination.pageNum = 1
  loadBillPage()
}

function handleCurrentChange(pageNum) {
  pagination.pageNum = pageNum
  loadBillPage()
}

function handleSizeChange(pageSize) {
  pagination.pageSize = pageSize
  pagination.pageNum = 1
  loadBillPage()
}

function openCreateDialog() {
  resetCreateForm()
  createDialogVisible.value = true
}

async function saveNewBill() {
  if (createButtonDisabled.value) {
    return
  }

  createLoading.value = true
  try {
    await createAdminBill({
      studentId: createForm.studentId,
      classId: createForm.classId,
      billType: createForm.billType,
      amount: createForm.amount,
      dueDate: createForm.dueDate,
      remark: createForm.remark
    })
    ElMessage.success('账单创建成功')
    createDialogVisible.value = false
    await loadBillPage()
  } catch (error) {
    ElMessage.error(error.message || '账单创建失败')
  } finally {
    createLoading.value = false
  }
}

function openPaymentDialog(row) {
  if (row.statusValue === 'VOID') {
    ElMessage.warning('已作废账单不能登记支付')
    return
  }
  if (row.remainingAmount <= 0) {
    ElMessage.info('该账单已结清，无需重复登记支付')
    return
  }

  paymentForm.billId = row.id
  paymentForm.billNo = row.billNo
  paymentForm.studentName = row.studentName
  paymentForm.payableAmount = row.amount
  paymentForm.paidAmount = row.paidAmount
  paymentForm.remainingAmount = row.remainingAmount
  paymentForm.payAmount = row.remainingAmount
  paymentForm.payChannel = PAY_CHANNEL.ALIPAY
  paymentForm.payTime = new Date().toISOString().slice(0, 19)
  paymentDialogVisible.value = true
}

function openRefundDialog(row) {
  if (row.statusValue === 'VOID') {
    ElMessage.warning('已作废账单不能继续退款或反结算')
    return
  }
  if (Number(row.paidAmount) <= 0) {
    ElMessage.info('该账单当前没有可回退的实收金额')
    return
  }

  refundForm.billId = row.id
  refundForm.billNo = row.billNo
  refundForm.studentName = row.studentName
  refundForm.paidAmount = row.paidAmount
  refundForm.refundAmount = row.paidAmount
  refundForm.payChannel = PAY_CHANNEL.ALIPAY
  refundForm.payTime = new Date().toISOString().slice(0, 19)
  refundForm.actionType = 'REFUND'
  refundDialogVisible.value = true
}

async function savePayment() {
  if (paymentButtonDisabled.value) {
    return
  }

  paymentLoading.value = true
  try {
    await registerAdminBillPayment(paymentForm.billId, {
      payAmount: paymentForm.payAmount,
      payChannel: paymentForm.payChannel,
      payTime: paymentForm.payTime
    })
    ElMessage.success('支付登记成功，账单与订单状态已同步')
    paymentDialogVisible.value = false
    await loadBillPage()
    if (currentDetail.value?.id === paymentForm.billId) {
      await openDetail(currentDetail.value)
    }
  } catch (error) {
    ElMessage.error(error.message || '支付登记失败')
  } finally {
    paymentLoading.value = false
  }
}

async function saveRefund() {
  if (refundButtonDisabled.value) {
    return
  }

  refundLoading.value = true
  try {
    await registerAdminBillRefund(refundForm.billId, {
      refundAmount: refundForm.refundAmount,
      payChannel: refundForm.payChannel,
      payTime: refundForm.payTime,
      actionType: refundForm.actionType
    })
    ElMessage.success('退款/冲正/反结算登记成功，账单、订单与课时包已同步')
    refundDialogVisible.value = false
    await loadBillPage()
    if (currentDetail.value?.id === refundForm.billId) {
      await openDetail(currentDetail.value)
    }
  } catch (error) {
    ElMessage.error(error.message || '退款或冲正登记失败')
  } finally {
    refundLoading.value = false
  }
}

async function openDetail(row) {
  detailVisible.value = true
  detailLoading.value = true
  currentDetail.value = row
  paymentRows.value = []
  try {
    paymentRows.value = await fetchAdminBillPayments(row.id)
  } catch (error) {
    ElMessage.error(error.message || '支付记录加载失败')
  } finally {
    detailLoading.value = false
  }
}

onMounted(async () => {
  await loadReferenceOptions()
  await loadBillPage()
})
</script>

<template>
  <div class="bill-page">
    <section class="bill-head">
      <div>
        <h1 class="bill-title">账单管理</h1>
        <p class="bill-subtitle">管理员端 / 财务统计 / 账单管理</p>
      </div>
      <div class="bill-head-actions">
        <el-button type="primary" size="large" @click="openCreateDialog">新建账单</el-button>
      </div>
    </section>

    <section class="kpi-grid">
      <article v-for="card in financeCards" :key="card.label" class="kpi-card">
        <p class="kpi-label">{{ card.label }}</p>
        <p class="kpi-value">{{ card.value }}</p>
        <p class="kpi-tip">{{ card.tip }}</p>
      </article>
    </section>

    <section class="filter-panel">
      <el-input
        v-model="filterForm.keyword"
        placeholder="请输入账单号/备注"
        clearable
        @keyup.enter="handleSearch"
      />
      <el-select v-model="filterForm.billType">
        <el-option v-for="option in billTypeOptions" :key="option" :label="option" :value="option" />
      </el-select>
      <el-select v-model="filterForm.status">
        <el-option v-for="option in statusOptions" :key="option" :label="option" :value="option" />
      </el-select>
      <el-date-picker
        v-model="filterForm.month"
        value-format="YYYY-MM"
        format="YYYY-MM"
        type="month"
        placeholder="选择月份"
      />
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </section>

    <section class="panel">
      <header class="panel-head panel-head--single">
        <h2>账单明细</h2>
      </header>
      <el-table v-loading="loading" :data="billRows" stripe>
        <el-table-column prop="billNo" label="账单号" min-width="160" />
        <el-table-column prop="studentName" label="学员" width="120" />
        <el-table-column prop="className" label="班级" min-width="140" />
        <el-table-column prop="billType" label="类型" width="120" />
        <el-table-column label="应收金额" width="120">
          <template #default="{ row }">
            {{ formatCurrency(row.amount) }}
          </template>
        </el-table-column>
        <el-table-column label="实收金额" width="120">
          <template #default="{ row }">
            {{ formatCurrency(row.paidAmount) }}
          </template>
        </el-table-column>
        <el-table-column label="待收金额" width="120">
          <template #default="{ row }">
            {{ formatCurrency(row.remainingAmount) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.statusValue)" effect="light">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dueDate" label="截止日期" width="120" />
        <el-table-column prop="updatedAt" label="更新时间" min-width="170" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看</el-button>
            <el-button link :disabled="row.remainingAmount <= 0 || row.statusValue === 'VOID'" @click="openPaymentDialog(row)">
              支付登记
            </el-button>
            <el-button link :disabled="row.paidAmount <= 0 || row.statusValue === 'VOID'" @click="openRefundDialog(row)">
              退款/冲正
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

    <el-dialog v-model="createDialogVisible" title="新建账单" width="540px">
      <el-form label-width="96px" class="create-bill-form">
        <el-form-item label="学员">
          <el-select v-model="createForm.studentId" placeholder="请选择学员" filterable style="width: 100%">
            <el-option v-for="item in studentOptions" :key="item.id" :label="item.label" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级">
          <el-select v-model="createForm.classId" placeholder="请选择班级" clearable filterable style="width: 100%">
            <el-option v-for="item in classOptions" :key="item.id" :label="item.label" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="账单类型">
          <el-select v-model="createForm.billType" style="width: 100%">
            <el-option v-for="option in createBillTypeOptions" :key="option" :label="option" :value="option" />
          </el-select>
        </el-form-item>
        <el-form-item label="应收金额">
          <el-input-number v-model="createForm.amount" :min="0" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker
            v-model="createForm.dueDate"
            type="date"
            value-format="YYYY-MM-DD"
            format="YYYY-MM-DD"
            placeholder="请选择截止日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="3" placeholder="请输入账单备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="createLoading" :disabled="createButtonDisabled" @click="saveNewBill">
            创建账单
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="paymentDialogVisible" title="支付登记" width="540px">
      <el-form label-width="110px" class="create-bill-form">
        <el-form-item label="账单号">
          <div class="readonly-text">{{ paymentForm.billNo }}</div>
        </el-form-item>
        <el-form-item label="学员">
          <div class="readonly-text">{{ paymentForm.studentName }}</div>
        </el-form-item>
        <el-form-item label="应收金额">
          <div class="readonly-text">{{ formatCurrency(paymentForm.payableAmount) }}</div>
        </el-form-item>
        <el-form-item label="已收金额">
          <div class="readonly-text">{{ formatCurrency(paymentForm.paidAmount) }}</div>
        </el-form-item>
        <el-form-item label="待收金额">
          <div class="readonly-text readonly-text--danger">{{ formatCurrency(paymentForm.remainingAmount) }}</div>
        </el-form-item>
        <el-form-item label="本次支付金额">
          <el-input-number
            v-model="paymentForm.payAmount"
            :min="0"
            :max="paymentForm.remainingAmount"
            :step="100"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="支付渠道">
          <el-select v-model="paymentForm.payChannel" style="width: 100%">
            <el-option v-for="option in payChannelOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付时间">
          <el-date-picker
            v-model="paymentForm.payTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择支付时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="paymentDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="paymentLoading" :disabled="paymentButtonDisabled" @click="savePayment">
            确认登记
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="refundDialogVisible" title="退款 / 冲正 / 反结算" width="540px">
      <el-form label-width="110px" class="create-bill-form">
        <el-form-item label="账单号">
          <div class="readonly-text">{{ refundForm.billNo }}</div>
        </el-form-item>
        <el-form-item label="学员">
          <div class="readonly-text">{{ refundForm.studentName }}</div>
        </el-form-item>
        <el-form-item label="当前实收金额">
          <div class="readonly-text readonly-text--danger">{{ formatCurrency(refundForm.paidAmount) }}</div>
        </el-form-item>
        <el-form-item label="动作类型">
          <el-select v-model="refundForm.actionType" style="width: 100%">
            <el-option v-for="option in refundActionOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="本次回退金额">
          <el-input-number
            v-model="refundForm.refundAmount"
            :min="0"
            :max="refundForm.paidAmount"
            :step="100"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="退款渠道">
          <el-select v-model="refundForm.payChannel" style="width: 100%">
            <el-option v-for="option in payChannelOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="退款时间">
          <el-date-picker
            v-model="refundForm.payTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择退款时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="refundDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="refundLoading" :disabled="refundButtonDisabled" @click="saveRefund">
            确认登记
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="账单详情" width="860px">
      <div v-loading="detailLoading">
        <el-descriptions v-if="currentDetail" :column="2" border>
          <el-descriptions-item label="账单号">{{ currentDetail.billNo }}</el-descriptions-item>
          <el-descriptions-item label="学员">{{ currentDetail.studentName }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ currentDetail.className }}</el-descriptions-item>
          <el-descriptions-item label="账单类型">{{ currentDetail.billType }}</el-descriptions-item>
          <el-descriptions-item label="应收金额">{{ formatCurrency(currentDetail.amount) }}</el-descriptions-item>
          <el-descriptions-item label="实收金额">{{ formatCurrency(currentDetail.paidAmount) }}</el-descriptions-item>
          <el-descriptions-item label="待收金额">{{ formatCurrency(currentDetail.remainingAmount) }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ currentDetail.status }}</el-descriptions-item>
          <el-descriptions-item label="截止日期">{{ currentDetail.dueDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ currentDetail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-payments">
          <div class="detail-payments__head">
            <h3>支付记录</h3>
            <div v-if="currentDetail" class="detail-payments__actions">
              <el-button
                link
                type="primary"
                :disabled="currentDetail.remainingAmount <= 0 || currentDetail.statusValue === 'VOID'"
                @click="openPaymentDialog(currentDetail)"
              >
                再次登记支付
              </el-button>
              <el-button
                link
                :disabled="currentDetail.paidAmount <= 0 || currentDetail.statusValue === 'VOID'"
                @click="openRefundDialog(currentDetail)"
              >
                登记退款/冲正
              </el-button>
            </div>
          </div>
          <el-table :data="paymentRows" stripe>
            <el-table-column prop="payNo" label="支付流水号" min-width="180" />
            <el-table-column prop="tradeType" label="流水类型" width="110" />
            <el-table-column label="支付金额" width="120">
              <template #default="{ row }">
                {{ formatCurrency(row.payAmount) }}
              </template>
            </el-table-column>
            <el-table-column prop="payChannel" label="支付渠道" width="120" />
            <el-table-column prop="payTime" label="支付时间" min-width="170" />
            <el-table-column prop="operatorName" label="登记人" width="120" />
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.bill-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 8px 4px 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.bill-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.bill-title {
  margin: 0;
  color: #111827;
  font-size: 28px;
  line-height: 38px;
  font-weight: 700;
}

.bill-subtitle {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 14px;
  line-height: 22px;
}

.bill-head-actions {
  display: flex;
  gap: 12px;
}

.kpi-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.kpi-card,
.panel,
.filter-panel {
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
}

.kpi-card {
  padding: 18px;
}

.kpi-label,
.kpi-tip {
  margin: 0;
  color: #6b7280;
}

.kpi-label {
  font-size: 13px;
}

.kpi-tip {
  margin-top: 6px;
  font-size: 12px;
}

.kpi-value {
  margin: 10px 0 0;
  color: #111827;
  font-size: 26px;
  line-height: 34px;
  font-weight: 700;
}

.filter-panel {
  padding: 14px;
  display: grid;
  gap: 10px;
  grid-template-columns: minmax(220px, 1.6fr) repeat(3, minmax(140px, 1fr)) auto auto;
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

.panel-head h2,
.detail-payments__head h3 {
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

.create-bill-form {
  padding-right: 6px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.readonly-text {
  color: #111827;
  font-weight: 600;
}

.readonly-text--danger {
  color: #c2410c;
}

.detail-payments {
  margin-top: 18px;
}

.detail-payments__head {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.detail-payments__actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

@media (max-width: 1100px) {
  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .filter-panel {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .bill-head {
    flex-direction: column;
  }

  .bill-head-actions {
    width: 100%;
  }

  .bill-head-actions :deep(.el-button) {
    flex: 1;
  }

  .kpi-grid,
  .filter-panel {
    grid-template-columns: 1fr;
  }

  .pagination-wrap {
    justify-content: center;
    overflow-x: auto;
  }
}

:global(html.dark) .bill-title,
:global(html.dark) .panel-head h2,
:global(html.dark) .detail-payments__head h3,
:global(html.dark) .kpi-value,
:global(html.dark) .readonly-text {
  color: #e5e7eb;
}

:global(html.dark) .bill-subtitle,
:global(html.dark) .kpi-label,
:global(html.dark) .kpi-tip {
  color: #9ca3af;
}

:global(html.dark) .panel,
:global(html.dark) .kpi-card,
:global(html.dark) .filter-panel {
  background: #1f2937;
  border-color: #374151;
}
</style>
