import request from '../request'

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-'
}

export async function fetchAdminSettingsOverview() {
  const data = await request({
    url: '/admin/settings/overview',
    method: 'get'
  })
  return {
    systemSettingCount: Number(data.systemSettingCount || 0),
    logStrategyCount: Number(data.logStrategyCount || 0),
    displayTemplateCount: Number(data.displayTemplateCount || 0),
    pendingApprovalCount: Number(data.pendingApprovalCount || 0),
    moduleStatusList: Array.isArray(data.moduleStatusList)
      ? data.moduleStatusList.map((item) => ({
          module: item.module || '-',
          owner: item.owner || '-',
          state: item.state || '待初始化',
          createdAt: formatDateTime(item.createdAt),
          updatedAt: formatDateTime(item.updatedAt)
        }))
      : []
  }
}
