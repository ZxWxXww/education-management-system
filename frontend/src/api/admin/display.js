import request from '../request'

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-'
}

function adaptDisplayConfig(raw = {}) {
  return {
    siteTitle: raw.siteTitle || '',
    siteSubtitle: raw.siteSubtitle || '',
    darkModeDefault: Boolean(raw.darkModeDefault),
    dashboardLayout: raw.dashboardLayout || 'card-grid',
    showAttendanceWidget: raw.showAttendanceWidget !== false,
    showFinanceWidget: raw.showFinanceWidget !== false,
    loginNotice: raw.loginNotice || '',
    brandColor: raw.brandColor || '#2563EB',
    locale: raw.locale || 'zh-CN',
    previewRecords: Array.isArray(raw.previewRecords)
      ? raw.previewRecords.map((item) => ({
          title: item.title || '-',
          value: item.value || '-',
          createdAt: formatDateTime(item.createdAt),
          updatedAt: formatDateTime(item.updatedAt)
        }))
      : []
  }
}

export async function fetchAdminDisplayCurrent() {
  const data = await request({
    url: '/admin/display/current',
    method: 'get'
  })
  return adaptDisplayConfig(data)
}

export async function updateAdminDisplayCurrent(payload = {}) {
  await request({
    url: '/admin/display/current',
    method: 'put',
    data: {
      siteTitle: payload.siteTitle,
      siteSubtitle: payload.siteSubtitle,
      darkModeDefault: payload.darkModeDefault,
      dashboardLayout: payload.dashboardLayout,
      showAttendanceWidget: payload.showAttendanceWidget,
      showFinanceWidget: payload.showFinanceWidget,
      loginNotice: payload.loginNotice,
      brandColor: payload.brandColor,
      locale: payload.locale
    }
  })
}

export async function resetAdminDisplayCurrent() {
  const data = await request({
    url: '/admin/display/reset',
    method: 'post'
  })
  return adaptDisplayConfig(data)
}
