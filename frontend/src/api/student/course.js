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

function adaptCourseClassItem(item = {}) {
  return {
    id: item.id,
    classId: item.classCode || '-',
    className: item.className || '-',
    courseId: item.courseId || null,
    courseName: item.courseName || '-',
    teacherName: item.teacherName || '-',
    cycle: item.startDate && item.endDate ? `${formatDate(item.startDate)} 至 ${formatDate(item.endDate)}` : '',
    totalLessons: Number(item.totalLessons || 0),
    completedLessons: Number(item.completedLessons || 0),
    nextLessonTime: item.nextLessonTime || '',
    progress: Number(item.totalLessons || 0) > 0 ? Number(((Number(item.completedLessons || 0) / Number(item.totalLessons || 0)) * 100).toFixed(1)) : 0,
    createdAt: formatDateTime(item.createdAt),
    updatedAt: formatDateTime(item.updatedAt)
  }
}

function adaptScheduleItem(item = {}) {
  return {
    id: item.id,
    sessionDate: formatDate(item.sessionDate),
    startTime: item.startTime || '',
    endTime: item.endTime || '',
    status: item.status || '',
    updatedAt: formatDateTime(item.updatedAt)
  }
}

function adaptClassmateItem(item = {}) {
  return {
    studentProfileId: item.studentProfileId || null,
    studentNo: item.studentNo || '-',
    studentName: item.studentName || '-',
    attendanceRate: formatPercent(item.attendanceRate),
    latestScore: formatPercent(item.latestScore),
    updatedAt: formatDateTime(item.updatedAt)
  }
}

export async function fetchStudentCoursePage(params = {}) {
  const data = await request({
    url: '/student/courses/page',
    method: 'post',
    data: buildPageQuery({
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 20,
      keyword: params.keyword || ''
    })
  })
  const page = adaptPageData(data)
  return {
    ...page,
    list: page.list.map(adaptCourseClassItem)
  }
}

export async function fetchStudentClassDetail(id) {
  const data = await request({
    url: `/student/courses/classes/${id}`,
    method: 'get'
  })
  return {
    ...adaptCourseClassItem(data),
    studentCount: Number(data.studentCount || 0),
    averageAttendanceRate: formatPercent(data.averageAttendanceRate),
    averageScore: formatPercent(data.averageScore),
    scheduleList: Array.isArray(data.scheduleList) ? data.scheduleList.map(adaptScheduleItem) : [],
    classmates: Array.isArray(data.classmates) ? data.classmates.map(adaptClassmateItem) : []
  }
}
