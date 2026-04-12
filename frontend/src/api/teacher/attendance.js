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

function adaptAttendanceRecord(item = {}) {
  return {
    id: item.id,
    classId: item.classId || null,
    studentName: item.studentName || '',
    studentNo: item.studentNo || '',
    className: item.className || '',
    courseName: item.courseName || '',
    teacherName: item.teacherName || '',
    date: formatDate(item.attendanceDate),
    status: item.attendanceStatus || '',
    remark: item.remark || '',
    sessionTime: item.sessionTime || '待排课',
    createdAt: formatDateTime(item.createdAt),
    updatedAt: formatDateTime(item.updatedAt)
  }
}

function adaptExceptionRecord(item = {}) {
  return {
    id: item.id ? `ATT-EX-${String(item.id).padStart(6, '0')}` : '-',
    rawId: item.id,
    studentName: item.studentName || '',
    studentNo: item.studentNo || '',
    className: item.className || '',
    courseName: item.courseName || '',
    abnormalType: item.exceptionType || '',
    checkInTime: `${formatDate(item.attendanceDate)} ${item.sessionTime || ''}`.trim(),
    teacherName: item.teacherName || '',
    handleStatus: item.handleStatus || '',
    remark: item.resolveRemark || item.remark || '',
    createdAt: formatDateTime(item.createdAt),
    updatedAt: formatDateTime(item.updatedAt)
  }
}

function adaptBatchTask(item = {}) {
  return {
    id: item.id ? `LOG-${String(item.id).padStart(6, '0')}` : '-',
    action: '批量处理',
    detail: `${item.className || '未知班级'} ${formatDate(item.attendanceDate)} ${item.remark || ''}`.trim(),
    result: item.taskStatus === 'FAILED' ? '失败' : '成功',
    operatorName: item.operatorName || '',
    createdAt: formatDateTime(item.createdAt),
    updatedAt: formatDateTime(item.updatedAt)
  }
}

export async function fetchTeacherAttendancePage(params = {}) {
  const data = await request({
    url: '/teacher/attendance/page',
    method: 'post',
    data: buildPageQuery({
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 200,
      classId: params.classId,
      studentId: params.studentId,
      status: params.status,
      attendanceDate: params.attendanceDate
    })
  })
  const page = adaptPageData(data)
  return { ...page, list: page.list.map(adaptAttendanceRecord) }
}

export async function fetchTeacherAttendanceExceptionPage(params = {}) {
  const data = await request({
    url: '/teacher/attendance-exceptions/page',
    method: 'post',
    data: buildPageQuery({
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 200,
      isResolved: params.isResolved,
      exceptionType: params.exceptionType
    })
  })
  const page = adaptPageData(data)
  return { ...page, list: page.list.map(adaptExceptionRecord) }
}

export async function batchUpdateTeacherAttendance(payload) {
  return request({
    url: '/teacher/attendance/batch-update',
    method: 'put',
    data: payload
  })
}

export async function createTeacherAttendanceBatchTask(payload) {
  return request({
    url: '/teacher/attendance/batch',
    method: 'post',
    data: payload
  })
}

export async function fetchTeacherAttendanceBatchTaskPage(params = {}) {
  const data = await request({
    url: '/teacher/attendance/batch/page',
    method: 'post',
    data: buildPageQuery({
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 50
    })
  })
  const page = adaptPageData(data)
  return { ...page, list: page.list.map(adaptBatchTask) }
}
