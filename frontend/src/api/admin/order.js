import request from '../request'
import { adaptPageData, buildPageQuery } from '../_adapters/page'
import {
  BILL_STATUS_OPTIONS,
  PAY_STATUS_OPTIONS,
  getBillStatusLabel,
  getPayStatusLabel,
  getStatusValueByLabel
} from '../../constants/status'

const orderTypeLabelMap = {
  HOUR_PACKAGE: '课时包缴费',
  MATERIAL: '教材费',
  EXAM_FEE: '考试费',
  OTHER: '其他'
}

const orderTypeValueMap = {
  课时包缴费: 'HOUR_PACKAGE',
  教材费: 'MATERIAL',
  考试费: 'EXAM_FEE',
  其他: 'OTHER'
}

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-'
}

function formatDate(value) {
  return value ? String(value).slice(0, 10) : ''
}

function toNumber(value) {
  return Number(value || 0)
}

function getOrderTypeLabel(value) {
  return orderTypeLabelMap[value] || value || '-'
}

function adaptOrderRow(raw) {
  return {
    id: raw.id,
    orderNo: raw.orderNo || '-',
    studentId: raw.studentId,
    studentName: raw.studentName || '-',
    orderType: getOrderTypeLabel(raw.orderType),
    orderTypeValue: raw.orderType,
    amountTotal: toNumber(raw.amountTotal),
    amountPaid: toNumber(raw.amountPaid),
    payStatus: getPayStatusLabel(raw.payStatus),
    payStatusValue: raw.payStatus,
    itemCount: Number(raw.itemCount || 0),
    billId: raw.billId || null,
    billNo: raw.billNo || '-',
    billStatus: getBillStatusLabel(raw.billStatus),
    billStatusValue: raw.billStatus || '',
    dueDate: formatDate(raw.dueDate),
    remark: raw.remark || '',
    createdAt: formatDateTime(raw.createdAt),
    updatedAt: formatDateTime(raw.updatedAt)
  }
}

function adaptOrderDetailItem(raw) {
  return {
    id: raw.id,
    courseId: raw.courseId || null,
    courseName: raw.courseName || '',
    itemName: raw.itemName || '-',
    quantity: Number(raw.quantity || 0),
    unitPrice: toNumber(raw.unitPrice),
    lineAmount: toNumber(raw.lineAmount),
    hourCount: raw.hourCount == null ? null : Number(raw.hourCount)
  }
}

export async function fetchAdminOrderPage(params) {
  const data = await request({
    url: '/admin/orders/page',
    method: 'post',
    data: buildPageQuery({
      ...params,
      orderType:
        params.orderType && params.orderType !== '全部类型'
          ? orderTypeValueMap[params.orderType] || params.orderType
          : undefined,
      payStatus:
        params.payStatus && params.payStatus !== '全部状态'
          ? getStatusValueByLabel(PAY_STATUS_OPTIONS, params.payStatus) || params.payStatus
          : undefined
    })
  })

  const page = adaptPageData(data)
  return {
    ...page,
    list: page.list.map(adaptOrderRow)
  }
}

export async function fetchAdminOrderDetail(id) {
  const data = await request({
    url: `/admin/orders/${id}`,
    method: 'get'
  })

  return {
    ...adaptOrderRow(data),
    items: Array.isArray(data.items) ? data.items.map(adaptOrderDetailItem) : []
  }
}

export function createAdminOrder(data) {
  return request({
    url: '/admin/orders',
    method: 'post',
    data: {
      studentId: data.studentId,
      orderType: orderTypeValueMap[data.orderType] || data.orderType,
      dueDate: data.dueDate || null,
      remark: data.remark || null,
      items: (data.items || []).map((item) => ({
        courseId: item.courseId || null,
        itemName: item.itemName,
        quantity: Number(item.quantity || 0),
        unitPrice: Number(item.unitPrice || 0),
        hourCount: item.hourCount == null || item.hourCount === '' ? null : Number(item.hourCount)
      }))
    }
  })
}

export async function fetchOrderReferenceOptions() {
  const [studentData, courseData] = await Promise.all([
    request({
      url: '/admin/bills/student-options',
      method: 'get'
    }),
    request({
      url: '/admin/courses/page',
      method: 'post',
      data: buildPageQuery({ pageNum: 1, pageSize: 100 })
    })
  ])

  const coursePage = adaptPageData(courseData)
  return {
    students: studentData.map((item) => ({
      id: item.id,
      label: `${item.studentName} / ${item.studentNo || item.id}`
    })),
    courses: coursePage.list.map((item) => ({
      id: item.id,
      label: item.courseName || item.courseCode || `课程 ${item.id}`
    }))
  }
}
