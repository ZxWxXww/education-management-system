import request from '../request'
import { adaptPageData, buildPageQuery } from '../_adapters/page'

function formatDateTime(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 19)
}

function adaptAssignmentRecord(item = {}) {
  return {
    id: item.id,
    submissionId: item.submissionId || null,
    assignmentTitle: item.assignmentTitle || '-',
    courseName: item.courseName || '-',
    teacherName: item.teacherName || '-',
    dueTime: formatDateTime(item.dueTime),
    submitTime: formatDateTime(item.submitTime),
    status: item.status || 'pending',
    score: typeof item.score === 'number' ? item.score : item.score ? Number(item.score) : null,
    feedback: item.feedback || '',
    createdAt: formatDateTime(item.createdAt),
    updatedAt: formatDateTime(item.updatedAt)
  }
}

export async function fetchStudentAssignmentSubmissionPage(params = {}) {
  const data = await request({
    url: '/student/assignments/submissions/page',
    method: 'post',
    data: buildPageQuery({
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 100,
      status: params.status || '',
      assignmentId: params.assignmentId || undefined
    })
  })
  const page = adaptPageData(data)
  return {
    ...page,
    list: page.list.map(adaptAssignmentRecord)
  }
}
