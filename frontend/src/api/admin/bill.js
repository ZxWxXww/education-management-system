import request from '../request'
import { adaptPageData, buildPageQuery } from '../_adapters/page'
import {
  BILL_STATUS_OPTIONS,
  getBillStatusLabel,
  getPayChannelLabel,
  getStatusValueByLabel
} from '../../constants/status'

const billTypeLabelMap = {
  HOUR_PACKAGE: '课时包缴费',
  MATERIAL: '教材费',
  EXAM_FEE: '考试费',
  REFUND: '退款',
  OTHER: '其他'
}

const billTypeValueMap = {
  课时包缴费: 'HOUR_PACKAGE',
  教材费: 'MATERIAL',
  考试费: 'EXAM_FEE',
  退款: 'REFUND',
  其他: 'OTHER'
}

const ALL_BILL_TYPES = '全部类型'
const ALL_BILL_STATUS = '全部状态'

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-'
}

function formatDate(value) {
  return value ? String(value).slice(0, 10) : ''
}

function toNumber(value) {
  return Number(value || 0)
}

function getBillTypeLabel(value) {
  return billTypeLabelMap[value] || value || '-'
}

function adaptBillRow(raw) {
  const amount = toNumber(raw.amount)
  const paidAmount = toNumber(raw.paidAmount)
  const isVoid = raw.status === 'VOID'

  return {
    id: raw.id,
    orderId: raw.orderId || null,
    billNo: raw.billNo || '-',
    studentId: raw.studentId,
    studentName: raw.studentName || '-',
    classId: raw.classId,
    className: raw.className || '-',
    billType: getBillTypeLabel(raw.billType),
    billTypeValue: raw.billType,
    amount,
    paidAmount,
    remainingAmount: isVoid ? 0 : Math.max(0, amount - paidAmount),
    status: getBillStatusLabel(raw.status),
    statusValue: raw.status,
    dueDate: formatDate(raw.dueDate),
    remark: raw.remark || '',
    createdAt: formatDateTime(raw.createdAt),
    updatedAt: formatDateTime(raw.updatedAt)
  }
}

function adaptPaymentRow(raw) {
  const payAmount = toNumber(raw.payAmount)
  const tradeTypeValue = resolveTradeTypeValue(raw, payAmount)

  return {
    id: raw.id,
    payNo: raw.payNo || '-',
    payAmount,
    tradeTypeValue,
    tradeType: getTradeTypeLabel(tradeTypeValue),
    payChannel: getPayChannelLabel(raw.payChannel),
    payChannelValue: raw.payChannel,
    payTime: formatDateTime(raw.payTime),
    operatorUserId: raw.operatorUserId || null,
    operatorName: raw.operatorName || '-',
    createdAt: formatDateTime(raw.createdAt)
  }
}

function resolveTradeTypeValue(raw, payAmount) {
  const payNo = String(raw?.payNo || '').toUpperCase()
  if (payAmount >= 0) return 'PAYMENT'
  if (payNo.startsWith('RF')) return 'REFUND'
  if (payNo.startsWith('RV')) return 'REVERSAL'
  if (payNo.startsWith('RU')) return 'UNSETTLE'
  return 'REFUND'
}

function getTradeTypeLabel(value) {
  if (value === 'PAYMENT') return '支付'
  if (value === 'REVERSAL') return '冲正'
  if (value === 'UNSETTLE') return '反结算'
  return '退款'
}

export async function fetchAdminBillPage(params) {
  const data = await request({
    url: '/admin/bills/page',
    method: 'post',
    data: buildPageQuery({
      ...params,
      month: params.month || undefined,
      billType:
        params.billType && params.billType !== ALL_BILL_TYPES
          ? billTypeValueMap[params.billType] || params.billType
          : undefined,
      status:
        params.status && params.status !== ALL_BILL_STATUS
          ? getStatusValueByLabel(BILL_STATUS_OPTIONS, params.status) || params.status
          : undefined
    })
  })

  const page = adaptPageData(data)
  return {
    ...page,
    list: page.list.map(adaptBillRow)
  }
}

export function createAdminBill(data) {
  return request({
    url: '/admin/bills',
    method: 'post',
    data: {
      studentId: data.studentId,
      classId: data.classId || null,
      billType: billTypeValueMap[data.billType] || data.billType,
      amount: Number(data.amount || 0),
      paidAmount: 0,
      status: 'PENDING',
      dueDate: data.dueDate || null,
      remark: data.remark || null
    }
  })
}

export function registerAdminBillPayment(id, data) {
  return request({
    url: `/admin/bills/${id}/payments`,
    method: 'post',
    data: {
      payAmount: Number(data.payAmount || 0),
      payChannel: data.payChannel,
      payTime: data.payTime || null
    }
  })
}

export function registerAdminBillRefund(id, data) {
  return request({
    url: `/admin/bills/${id}/refunds`,
    method: 'post',
    data: {
      refundAmount: Number(data.refundAmount || 0),
      payChannel: data.payChannel,
      payTime: data.payTime || null,
      actionType: data.actionType || 'REFUND'
    }
  })
}

export async function fetchAdminBillPayments(id) {
  const data = await request({
    url: `/admin/bills/${id}/payments`,
    method: 'get'
  })

  return Array.isArray(data) ? data.map(adaptPaymentRow) : []
}

export async function fetchBillReferenceOptions() {
  const [studentData, classData] = await Promise.all([
    request({
      url: '/admin/bills/student-options',
      method: 'get'
    }),
    request({
      url: '/admin/classes/page',
      method: 'post',
      data: buildPageQuery({ pageNum: 1, pageSize: 100 })
    })
  ])

  const classPage = adaptPageData(classData)
  return {
    students: studentData.map((item) => ({
      id: item.id,
      label: `${item.studentName} / ${item.studentNo || item.id}`,
      classNameText: item.classNameText || ''
    })),
    classes: classPage.list.map((item) => ({
      id: item.id,
      label: item.className || item.classCode || `班级 ${item.id}`
    }))
  }
}
