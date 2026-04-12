import request from '../request'
import { adaptPageData, buildPageQuery } from '../_adapters/page'

function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

function formatDate(value) {
  if (!value) return ''
  return String(value).slice(0, 10)
}

function adaptAttendanceException(item = {}) {
  return {
    id: item.id ? `ATT-S-${String(item.id).padStart(6, '0')}` : '-',
    rawId: item.id,
    date: formatDate(item.attendanceDate),
    weekDay: item.weekDay || '',
    courseName: item.courseName || '',
    classTime: item.classTime || '待排课',
    abnormalType: item.abnormalType || '',
    status: item.status || '',
    checkTime: item.checkTime || '',
    teacherName: item.teacherName || '',
    reason: item.reason || '',
    handleNote: item.handleNote || '',
    createdAt: formatDateTime(item.createdAt),
    updatedAt: formatDateTime(item.updatedAt)
  }
}

export async function fetchStudentAttendanceExceptionPage(params = {}) {
  const data = await request({
    url: '/student/attendance/exceptions/page',
    method: 'post',
    data: buildPageQuery({
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 200,
      isResolved: params.isResolved,
      exceptionType: params.exceptionType
    })
  })
  const page = adaptPageData(data)
  return { ...page, list: page.list.map(adaptAttendanceException) }
}
