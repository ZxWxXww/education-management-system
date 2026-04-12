import request from '../request'
import { adaptPageData, buildPageQuery } from '../_adapters/page'

function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

function adaptNotificationItem(item = {}) {
  return {
    id: item.id,
    title: item.title || '-',
    content: item.content || '',
    noticeType: item.noticeType || '',
    bizRefType: item.bizRefType || '',
    bizRefId: item.bizRefId || null,
    publishTime: formatDateTime(item.publishTime),
    isRead: Boolean(item.read)
  }
}

export async function fetchStudentNotificationPage(params = {}) {
  const data = await request({
    url: '/student/notifications/page',
    method: 'post',
    data: buildPageQuery({
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 5,
      noticeType: params.noticeType,
      isRead: params.isRead
    })
  })

  const page = adaptPageData(data)
  return {
    ...page,
    list: page.list.map(adaptNotificationItem)
  }
}

export function markStudentNotificationRead(id) {
  return request({
    url: `/student/notifications/${id}/read`,
    method: 'put'
  })
}
