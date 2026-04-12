import request from '../request'
import { adaptPageData, buildPageQuery } from '../_adapters/page'

function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

function normalizeFileType(type) {
  if (!type) return '未知'
  const upper = String(type).toUpperCase()
  if (upper === 'WORD' || upper === 'DOCX' || upper === 'DOC') return 'Word'
  if (upper === 'PPT') return 'PPT'
  if (upper === 'PDF') return 'PDF'
  if (upper === 'IMAGE') return '图片'
  if (upper === 'EXCEL') return 'Excel'
  return upper
}

function adaptResourceItem(item = {}) {
  return {
    id: item.id,
    resourceId: item.id ? `RES-${item.id}` : '-',
    title: item.title || '未命名资源',
    type: normalizeFileType(item.fileType),
    fileType: normalizeFileType(item.fileType),
    className: item.className || '未关联班级',
    courseName: item.courseName || '未关联课程',
    teacherName: item.teacherName || '未关联教师',
    category: item.category || '未分类',
    description: item.description || '',
    fileName: item.fileName || '',
    fileUrl: item.fileUrl || '',
    fileSizeKb: Number(item.fileSizeKb || 0),
    downloadCount: Number(item.downloadCount || 0),
    createdAt: formatDateTime(item.createdAt),
    updatedAt: formatDateTime(item.updatedAt)
  }
}

export async function fetchTeacherResourcePage(params = {}) {
  const data = await request({
    url: '/teacher/resources/page',
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
