import request from '../request'
import { adaptPageData, buildPageQuery } from '../_adapters/page'

function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

function mapStatus(status) {
  if (status === 'PUBLISHED') return '进行中'
  if (status === 'DRAFT') return '待发布'
  if (status === 'CLOSED') return '已截止'
  return status || '未知'
}

function adaptAssignmentItem(item = {}) {
  return {
    id: item.id,
    assignmentId: item.id ? `T-A-${String(item.id).padStart(8, '0')}` : '-',
    title: item.title || '未命名作业',
    className: item.className || '未关联班级',
    courseName: item.courseName || '未关联课程',
    publishTime: formatDateTime(item.publishTime),
    deadline: formatDateTime(item.deadline),
    status: mapStatus(item.status),
    submitted: Number(item.submitted || 0),
    total: Number(item.total || 0),
    avgDuration: item.avgDuration || '--',
    createdAt: formatDateTime(item.createdAt),
    updatedAt: formatDateTime(item.updatedAt)
  }
}

export async function fetchTeacherAssignmentPage(params = {}) {
  const data = await request({
    url: '/teacher/assignments/page',
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
    list: page.list.map(adaptAssignmentItem)
  }
}
