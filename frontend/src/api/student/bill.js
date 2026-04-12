import request from '../request'
import { adaptPageData, buildPageQuery } from '../_adapters/page'
import { BILL_STATUS_OPTIONS, HOUR_PACKAGE_STATUS_OPTIONS, getStatusMeta } from '../../constants/status'

const billTypeLabelMap = {
  HOUR_PACKAGE: '课时包缴费',
  MATERIAL: '教材费',
  EXAM_FEE: '考试费',
  REFUND: '退费',
  OTHER: '其他'
}

function formatDate(value) {
  if (!value) return '-'
  return String(value).slice(0, 10)
}

function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

function adaptBillItem(item = {}) {
  const amount = Number(item.amount || 0)
  const paidAmount = Number(item.paidAmount || 0)
  const billStatus = getStatusMeta(BILL_STATUS_OPTIONS, item.status, '未知')
  return {
    id: item.id,
    billNo: item.billNo || '-',
    studentId: item.studentId || null,
    classId: item.classId || null,
    className: item.className || '-',
    billTypeCode: item.billType || '',
    billTypeLabel: billTypeLabelMap[item.billType] || item.billType || '-',
    amount,
    paidAmount,
    billStatusCode: billStatus.value || '',
    billStatusLabel: billStatus.label,
    billStatusTagType: billStatus.tagType,
    paymentStatus: paidAmount >= amount && amount > 0 ? 'paid' : 'pending',
    dueDate: formatDate(item.dueDate),
    remark: item.remark || '',
    createdAt: formatDateTime(item.createdAt),
    updatedAt: formatDateTime(item.updatedAt)
  }
}

function adaptHourPackageItem(item = {}) {
  const packageStatus = getStatusMeta(HOUR_PACKAGE_STATUS_OPTIONS, item.status, '未知状态')
  return {
    id: item.id,
    orderId: item.orderId || null,
    courseId: item.courseId || null,
    courseName: item.courseName || '-',
    totalHours: Number(item.totalHours || 0),
    usedHours: Number(item.usedHours || 0),
    remainingHours: Number(item.remainingHours || 0),
    effectiveDate: formatDate(item.effectiveDate),
    expireDate: formatDate(item.expireDate),
    statusCode: packageStatus.value || '',
    statusLabel: packageStatus.label,
    statusTagType: packageStatus.tagType,
    updatedAt: formatDateTime(item.updatedAt)
  }
}

function adaptHourDeductionItem(item = {}) {
  return {
    id: item.id,
    hourPackageId: item.hourPackageId || null,
    classId: item.classId || null,
    sessionId: item.sessionId || null,
    className: item.className || '-',
    courseId: item.courseId || null,
    courseName: item.courseName || '-',
    deductHours: Number(item.deductHours || 0),
    deductType: item.deductType || '',
    bizDate: formatDate(item.bizDate),
    remark: item.remark || '',
    createdAt: formatDateTime(item.createdAt)
  }
}

export async function fetchStudentBillPage(params = {}) {
  const data = await request({
    url: '/student/bills/page',
    method: 'post',
    data: buildPageQuery({
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 5,
      status: params.status,
      month: params.month
    })
  })

  const page = adaptPageData(data)
  return {
    ...page,
    list: page.list.map(adaptBillItem)
  }
}

export async function fetchStudentBillDetail(id) {
  const data = await request({
    url: `/student/bills/${id}`,
    method: 'get'
  })

  return adaptBillItem(data)
}

export async function fetchStudentHourPackageSummary() {
  const data = await request({
    url: '/student/bills/hour-packages/summary',
    method: 'get'
  })

  return {
    totalRemainingHours: Number(data.totalRemainingHours || 0),
    activePackageCount: Number(data.activePackageCount || 0),
    packages: Array.isArray(data.packages) ? data.packages.map(adaptHourPackageItem) : [],
    deductions: Array.isArray(data.deductions) ? data.deductions.map(adaptHourDeductionItem) : []
  }
}
