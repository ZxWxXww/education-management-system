import request from '../request'
import { adaptPageData, buildPageQuery } from '../_adapters/page'

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-'
}

function buildLogQueryParams(params = {}) {
  const [startDate, endDate] = params.dateRange || []
  return {
    keyword: params.keyword || undefined,
    module: params.module && params.module !== '全部模块' ? params.module : undefined,
    level: params.level && params.level !== '全部等级' ? params.level : undefined,
    startDate,
    endDate
  }
}

function adaptLogRow(raw = {}) {
  return {
    id: raw.id ? `LOG-${String(raw.id).padStart(8, '0')}` : '-',
    operator: raw.operator || 'system',
    module: raw.module || '-',
    action: raw.action || '-',
    ip: raw.ip || '-',
    level: raw.level || 'INFO',
    detail: raw.detail || '',
    createdAt: formatDateTime(raw.createdAt),
    updatedAt: formatDateTime(raw.updatedAt)
  }
}

export async function fetchAdminLogPage(params = {}) {
  const data = await request({
    url: '/admin/logs/page',
    method: 'post',
    data: buildPageQuery({
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 200,
      ...buildLogQueryParams(params)
    })
  })
  const page = adaptPageData(data)
  return { ...page, list: page.list.map(adaptLogRow) }
}

export async function fetchAdminLogArchiveStrategy() {
  const data = await request({
    url: '/admin/logs/archive-strategy',
    method: 'get'
  })
  return {
    autoArchiveEnabled: Boolean(data.autoArchiveEnabled),
    retentionDays: Number(data.retentionDays || 180),
    updatedAt: formatDateTime(data.updatedAt)
  }
}

export async function updateAdminLogArchiveStrategy(payload = {}) {
  await request({
    url: '/admin/logs/archive-strategy',
    method: 'put',
    data: {
      autoArchiveEnabled: Boolean(payload.autoArchiveEnabled),
      retentionDays: Number(payload.retentionDays || 180)
    }
  })
}

export async function exportAdminLogs(params = {}) {
  const blob = await request({
    url: '/admin/logs/export',
    method: 'get',
    params: buildLogQueryParams(params),
    responseType: 'blob'
  })
  const downloadBlob = blob instanceof Blob ? blob : new Blob([blob], { type: 'text/csv;charset=utf-8' })
  const url = window.URL.createObjectURL(downloadBlob)
  const link = document.createElement('a')
  link.href = url
  link.download = `admin-log-export-${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}
