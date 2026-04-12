import request from '../request'
import { adaptPageData, buildPageQuery } from '../_adapters/page'

function formatDate(value) {
  return value ? String(value).slice(0, 10) : ''
}

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-'
}

function toNumber(value) {
  return Number(value || 0)
}

function adaptOverview(raw = {}) {
  return {
    totalAttendanceCount: toNumber(raw.totalAttendanceCount),
    punctualRate: toNumber(raw.punctualRate),
    leaveCount: toNumber(raw.leaveCount),
    exceptionCount: toNumber(raw.exceptionCount),
    trend: Array.isArray(raw.trend)
      ? raw.trend.map((item) => ({
          label: item.label || '',
          attendanceDate: formatDate(item.attendanceDate),
          punctualRate: toNumber(item.punctualRate)
        }))
      : [],
    classOverview: Array.isArray(raw.classOverview)
      ? raw.classOverview.map((item) => ({
          classId: item.classId,
          className: item.className || '-',
          teacherName: item.teacherName || '-',
          shouldAttend: toNumber(item.shouldAttend),
          actualAttend: toNumber(item.actualAttend),
          normalRate: toNumber(item.normalRate),
          abnormalCount: toNumber(item.abnormalCount)
        }))
      : [],
    focusAlerts: Array.isArray(raw.focusAlerts)
      ? raw.focusAlerts.map((item) => ({
          level: item.level || '低',
          text: item.text || ''
        }))
      : []
  }
}

function adaptExceptionRow(raw = {}) {
  return {
    id: raw.id,
    displayId: raw.id ? `ATT-EX-${String(raw.id).padStart(6, '0')}` : '-',
    attendanceRecordId: raw.attendanceRecordId,
    studentName: raw.studentName || '-',
    studentNo: raw.studentNo || '-',
    className: raw.className || '-',
    teacherName: raw.teacherName || '-',
    courseName: raw.courseName || '-',
    checkTime: formatDateTime(raw.checkTime),
    abnormalType: raw.abnormalType || '',
    abnormalTypeValue: raw.abnormalTypeValue || '',
    severity: raw.severity || '',
    isResolved: Number(raw.isResolved || 0),
    handleStatus: raw.handleStatus || '待处理',
    remark: raw.remark || '',
    resolveRemark: raw.resolveRemark || '',
    resolvedByName: raw.resolvedByName || '',
    resolvedAt: formatDateTime(raw.resolvedAt),
    createdAt: formatDateTime(raw.createdAt),
    updatedAt: formatDateTime(raw.updatedAt)
  }
}

export async function fetchAdminAttendanceOverview() {
  const data = await request({
    url: '/admin/attendance/overview',
    method: 'get'
  })
  return adaptOverview(data)
}

export async function fetchAdminAttendanceExceptionPage(params = {}) {
  const data = await request({
    url: '/admin/attendance-exceptions/page',
    method: 'post',
    data: buildPageQuery({
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 200,
      isResolved: params.isResolved,
      exceptionType: params.exceptionType
    })
  })
  const page = adaptPageData(data)
  return { ...page, list: page.list.map(adaptExceptionRow) }
}

export async function fetchAdminAttendanceExceptionDetail(id) {
  const data = await request({
    url: `/admin/attendance-exceptions/${id}`,
    method: 'get'
  })
  return adaptExceptionRow(data)
}

export function updateAdminAttendanceException(id, payload) {
  return request({
    url: `/admin/attendance-exceptions/${id}`,
    method: 'put',
    data: {
      attendanceRecordId: payload.attendanceRecordId,
      exceptionType: payload.abnormalTypeValue,
      severity: payload.severity,
      isResolved: payload.isResolved,
      resolveRemark: payload.resolveRemark || ''
    }
  })
}
