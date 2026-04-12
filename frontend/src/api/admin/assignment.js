import request from '../request'
import { adaptPageData, buildPageQuery } from '../_adapters/page'

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-'
}

function adaptStatus(status, deadline) {
  if (!status) return '未开始'
  const upper = String(status).toUpperCase()
  if (upper === 'CLOSED') return '已截止'
  if (upper === 'PUBLISHED') {
    if (deadline && new Date(deadline).getTime() < Date.now()) return '已截止'
    return '进行中'
  }
  if (upper === 'DRAFT') return '未开始'
  return status
}

function adaptAssignmentRow(raw = {}) {
  return {
    id: raw.id ? `A-${String(raw.id).padStart(6, '0')}` : '-',
    rawId: raw.id,
    courseId: raw.courseId || null,
    title: raw.title || '-',
    course: raw.courseName || '-',
    className: raw.className || '-',
    teacher: raw.teacherName || '-',
    deadline: formatDateTime(raw.deadline),
    status: adaptStatus(raw.status, raw.deadline),
    submitted: Number(raw.submitted || 0),
    total: Number(raw.total || 0),
    createdAt: formatDateTime(raw.createdAt),
    updatedAt: formatDateTime(raw.updatedAt)
  }
}

export async function fetchAdminAssignmentPage(params = {}) {
  const data = await request({
    url: '/admin/assignments/page',
    method: 'post',
    data: buildPageQuery({
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 200,
      keyword: params.keyword || undefined,
      courseId: params.courseId || undefined,
      status: params.status || undefined
    })
  })
  const page = adaptPageData(data)
  return { ...page, list: page.list.map(adaptAssignmentRow) }
}
