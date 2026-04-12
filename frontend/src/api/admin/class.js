import request from '../request'
import { adaptPageData, buildPageQuery } from '../_adapters/page'
import { fetchAdminCoursePage } from './course'
import { fetchAdminUserPage } from './user'

const statusLabelMap = {
  0: '停用',
  1: '进行中',
  2: '招生中'
}

const statusValueMap = {
  全部状态: undefined,
  进行中: 1,
  招生中: 2,
  停用: 0
}

function formatDate(value) {
  return value ? String(value).slice(0, 10) : ''
}

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-'
}

function adaptClassRow(raw = {}) {
  return {
    id: raw.id,
    classCode: raw.classCode || '-',
    className: raw.className || '-',
    courseId: raw.courseId || null,
    courseName: '',
    headTeacherId: raw.headTeacherId || null,
    teacherName: '',
    startDate: formatDate(raw.startDate),
    endDate: formatDate(raw.endDate),
    status: statusLabelMap[raw.status] || '进行中',
    statusValue: Number(raw.status ?? 1),
    createdAt: formatDateTime(raw.createdAt),
    updatedAt: formatDateTime(raw.updatedAt)
  }
}

function mergeReferenceInfo(list, courses = [], teachers = []) {
  const courseMap = new Map(courses.map((item) => [item.id, item]))
  const teacherMap = new Map(teachers.map((item) => [item.id, item]))
  return list.map((item) => ({
    ...item,
    courseName: courseMap.get(item.courseId)?.courseName || '-',
    teacherName: teacherMap.get(item.headTeacherId)?.name || '-'
  }))
}

export async function fetchAdminClassPage(params = {}) {
  const [pageData, coursePage, teacherPage] = await Promise.all([
    request({
      url: '/admin/classes/page',
      method: 'post',
      data: buildPageQuery({
        pageNum: params.pageNum || 1,
        pageSize: params.pageSize || 200,
        keyword: params.keyword || undefined,
        courseId: params.courseId || undefined,
        status:
          typeof params.status === 'number'
            ? params.status
            : statusValueMap[params.status] !== undefined
              ? statusValueMap[params.status]
              : undefined
      })
    }),
    fetchAdminCoursePage({ pageNum: 1, pageSize: 300 }),
    fetchAdminUserPage({ pageNum: 1, pageSize: 300 })
  ])
  const page = adaptPageData(pageData)
  const rows = page.list.map(adaptClassRow)
  const teachers = teacherPage.list.filter((item) => item.roleCodes.includes('TEACHER') || String(item.role).includes('教师'))
  return {
    ...page,
    list: mergeReferenceInfo(rows, coursePage.list, teachers)
  }
}

export async function fetchAdminClassDetail(id) {
  const [data, coursePage, teacherPage] = await Promise.all([
    request({
      url: `/admin/classes/${id}`,
      method: 'get'
    }),
    fetchAdminCoursePage({ pageNum: 1, pageSize: 300 }),
    fetchAdminUserPage({ pageNum: 1, pageSize: 300 })
  ])
  const row = adaptClassRow(data)
  const teachers = teacherPage.list.filter((item) => item.roleCodes.includes('TEACHER') || String(item.role).includes('教师'))
  return mergeReferenceInfo([row], coursePage.list, teachers)[0]
}

export async function fetchAdminClassReferenceOptions() {
  const [coursePage, teacherPage] = await Promise.all([
    fetchAdminCoursePage({ pageNum: 1, pageSize: 300 }),
    fetchAdminUserPage({ pageNum: 1, pageSize: 300 })
  ])
  return {
    courses: coursePage.list.map((item) => ({
      id: item.id,
      label: `${item.courseName} / ${item.courseCode}`
    })),
    teachers: teacherPage.list
      .filter((item) => item.roleCodes.includes('TEACHER') || String(item.role).includes('教师'))
      .map((item) => ({
        id: item.id,
        label: `${item.name} / ${item.username}`
      }))
  }
}

export function createAdminClass(payload) {
  return request({
    url: '/admin/classes',
    method: 'post',
    data: {
      classCode: payload.classCode,
      className: payload.className,
      courseId: payload.courseId,
      headTeacherId: payload.headTeacherId || null,
      startDate: payload.startDate || null,
      endDate: payload.endDate || null,
      status: payload.statusValue ?? 1
    }
  })
}

export function updateAdminClass(id, payload) {
  return request({
    url: `/admin/classes/${id}`,
    method: 'put',
    data: {
      classCode: payload.classCode,
      className: payload.className,
      courseId: payload.courseId,
      headTeacherId: payload.headTeacherId || null,
      startDate: payload.startDate || null,
      endDate: payload.endDate || null,
      status: payload.statusValue ?? 1
    }
  })
}

export function deleteAdminClass(id) {
  return request({
    url: `/admin/classes/${id}`,
    method: 'delete'
  })
}
