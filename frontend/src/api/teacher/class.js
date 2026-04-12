import request from '../request'
import { adaptPageData, buildPageQuery } from '../_adapters/page'

function formatDate(value) {
  if (!value) return ''
  return String(value).slice(0, 10)
}

function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

function formatPercent(value) {
  const numeric = Number(value || 0)
  return Number.isFinite(numeric) ? Number(numeric.toFixed(1)) : 0
}

function adaptClassItem(item = {}) {
  return {
    id: item.id,
    classId: item.classCode || '-',
    className: item.className || '-',
    courseId: item.courseId || null,
    courseName: item.courseName || '-',
    headTeacherName: item.headTeacherName || '',
    grade: '',
    schedule: item.startDate && item.endDate ? `${formatDate(item.startDate)} 至 ${formatDate(item.endDate)}` : '',
    classroom: '',
    studentCount: Number(item.studentCount || 0),
    maxStudentCount: Number(item.studentCount || 0),
    sessionCount: Number(item.sessionCount || 0),
    attendanceRate: formatPercent(item.attendanceRate),
    status: Number(item.status) === 1 ? '进行中' : '已结课',
    statusCode: Number(item.status || 0),
    createdAt: formatDateTime(item.createdAt),
    updatedAt: formatDateTime(item.updatedAt)
  }
}

function adaptClassSession(item = {}) {
  return {
    id: item.id,
    sessionTitle: item.sessionTitle || '-',
    sessionDate: formatDate(item.sessionDate),
    startTime: item.startTime || '',
    endTime: item.endTime || '',
    status: item.status || '',
    remark: item.remark || '',
    createdAt: formatDateTime(item.createdAt),
    updatedAt: formatDateTime(item.updatedAt)
  }
}

function adaptClassStudent(item = {}) {
  return {
    studentProfileId: item.studentProfileId || null,
    userId: item.userId || null,
    studentNo: item.studentNo || '-',
    studentName: item.studentName || '-',
    grade: item.grade || '',
    joinedAt: formatDateTime(item.joinedAt),
    attendanceCount: Number(item.attendanceCount || 0),
    attendanceRate: formatPercent(item.attendanceRate),
    averageScore: formatPercent(item.averageScore),
    enrollmentStatus: item.enrollmentStatus || '-'
  }
}

export async function fetchTeacherClassPage(params = {}) {
  const data = await request({
    url: '/teacher/classes/page',
    method: 'post',
    data: buildPageQuery({
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 100,
      keyword: params.keyword || ''
    })
  })

  const page = adaptPageData(data)
  return {
    ...page,
    list: page.list.map(adaptClassItem)
  }
}

export async function fetchTeacherClassDetail(id) {
  const data = await request({
    url: `/teacher/classes/${id}`,
    method: 'get'
  })

  return {
    ...adaptClassItem(data),
    teacherTitle: data.headTeacherTitle || '',
    subject: data.subject || '',
    averageScore: formatPercent(data.averageScore),
    recentSessions: Array.isArray(data.recentSessions) ? data.recentSessions.map(adaptClassSession) : [],
    students: Array.isArray(data.students) ? data.students.map(adaptClassStudent) : []
  }
}
