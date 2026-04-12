import request from '../request'

function toNumber(value, digits = 0) {
  const number = Number(value || 0)
  return digits > 0 ? Number(number.toFixed(digits)) : number
}

export async function fetchAdminTeachingResourceOverview() {
  const data = await request({
    url: '/admin/resources/overview',
    method: 'get'
  })
  return {
    totalAssignments: toNumber(data.totalAssignments),
    publishedThisWeek: toNumber(data.publishedThisWeek),
    gradeRecords: toNumber(data.gradeRecords),
    avgScore: toNumber(data.avgScore, 1),
    recentActivities: Array.isArray(data.recentActivities)
      ? data.recentActivities.map((item) => ({
          type: item.type || '',
          title: item.title || '',
          time: item.time || ''
        }))
      : []
  }
}
