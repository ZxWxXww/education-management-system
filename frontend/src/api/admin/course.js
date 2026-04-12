import request from '../request'
import { adaptPageData, buildPageQuery } from '../_adapters/page'

const statusLabelMap = {
  0: '停用',
  1: '进行中',
  2: '排课中'
}

const statusValueMap = {
  全部状态: undefined,
  进行中: 1,
  排课中: 2,
  停用: 0
}

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-'
}

function adaptCourseRow(raw = {}) {
  return {
    id: raw.id,
    courseCode: raw.courseCode || '-',
    courseName: raw.courseName || '-',
    subject: raw.subject || '-',
    description: raw.description || '',
    status: statusLabelMap[raw.status] || '进行中',
    statusValue: Number(raw.status ?? 1),
    createdAt: formatDateTime(raw.createdAt),
    updatedAt: formatDateTime(raw.updatedAt)
  }
}

export async function fetchAdminCoursePage(params = {}) {
  const data = await request({
    url: '/admin/courses/page',
    method: 'post',
    data: buildPageQuery({
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 200,
      keyword: params.keyword || undefined,
      status:
        typeof params.status === 'number'
          ? params.status
          : statusValueMap[params.status] !== undefined
            ? statusValueMap[params.status]
            : undefined
    })
  })
  const page = adaptPageData(data)
  return { ...page, list: page.list.map(adaptCourseRow) }
}

export async function fetchAdminCourseDetail(id) {
  const data = await request({
    url: `/admin/courses/${id}`,
    method: 'get'
  })
  return adaptCourseRow(data)
}

export function createAdminCourse(payload) {
  return request({
    url: '/admin/courses',
    method: 'post',
    data: {
      courseCode: payload.courseCode,
      courseName: payload.courseName,
      subject: payload.subject || '',
      description: payload.description || '',
      status: payload.statusValue ?? 1
    }
  })
}

export function updateAdminCourse(id, payload) {
  return request({
    url: `/admin/courses/${id}`,
    method: 'put',
    data: {
      courseCode: payload.courseCode,
      courseName: payload.courseName,
      subject: payload.subject || '',
      description: payload.description || '',
      status: payload.statusValue ?? 1
    }
  })
}

export function deleteAdminCourse(id) {
  return request({
    url: `/admin/courses/${id}`,
    method: 'delete'
  })
}
