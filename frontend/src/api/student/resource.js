import request from '../request'
import { adaptPageData, buildPageQuery } from '../_adapters/page'

function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

function adaptResourceItem(item = {}) {
  return {
    id: item.id,
    resourceId: item.id ? String(item.id) : '-',
    title: item.title || '-',
    courseName: item.courseName || '-',
    teacherName: item.teacherName || '-',
    category: item.category || '-',
    fileType: item.fileType || '-',
    uploadTime: formatDateTime(item.createdAt),
    downloadCount: Number(item.downloadCount || 0),
    description: item.description || '',
    fileName: item.fileName || '',
    fileUrl: item.fileUrl || '',
    fileSizeKb: Number(item.fileSizeKb || 0),
    createdAt: formatDateTime(item.createdAt),
    updatedAt: formatDateTime(item.updatedAt)
  }
}

export async function fetchStudentResourcePage(params = {}) {
  const data = await request({
    url: '/student/resources/page',
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
    list: page.list.map(adaptResourceItem)
  }
}

export async function fetchStudentResourceDetail(id) {
  const data = await request({
    url: `/student/resources/${id}`,
    method: 'get'
  })
  return adaptResourceItem(data)
}
