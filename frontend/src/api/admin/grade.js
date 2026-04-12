import request from '../request'
import { adaptPageData, buildPageQuery } from '../_adapters/page'

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-'
}

function adaptGradeRow(raw = {}) {
  return {
    id: raw.id ? `G-${String(raw.id).padStart(6, '0')}` : '-',
    rawId: raw.id,
    courseId: raw.courseId || null,
    studentName: raw.studentName || '-',
    studentNo: raw.studentNo || '-',
    course: raw.courseName || '-',
    className: raw.className || '-',
    assignment: raw.examName || '-',
    score: Number(raw.score || 0),
    rank: Number(raw.rankInClass || 0),
    teacher: raw.teacherName || '-',
    publishTime: formatDateTime(raw.publishTime),
    createdAt: formatDateTime(raw.createdAt),
    updatedAt: formatDateTime(raw.updatedAt)
  }
}

export async function fetchAdminGradePage(params = {}) {
  const data = await request({
    url: '/admin/scores/page',
    method: 'post',
    data: buildPageQuery({
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 200,
      keyword: params.keyword || undefined,
      courseId: params.courseId || undefined
    })
  })
  const page = adaptPageData(data)
  return { ...page, list: page.list.map(adaptGradeRow) }
}
