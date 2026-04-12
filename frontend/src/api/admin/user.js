import request from '../request'
import { adaptPageData, buildPageQuery } from '../_adapters/page'

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-'
}

function getRoleLabel(roleNames = [], roleCodes = []) {
  if (roleNames.length) return roleNames.join(' / ')

  const roleCodeMap = {
    ADMIN: '管理员',
    TEACHER: '教师',
    STUDENT: '学生'
  }

  return roleCodes.map((item) => roleCodeMap[item] || item).join(' / ') || '未分配'
}

function adaptAdminUserRow(raw) {
  return {
    id: raw.id,
    displayId: String(raw.id ?? '-'),
    username: raw.username || '-',
    name: raw.realName || raw.username || '-',
    role: getRoleLabel(raw.roleNames, raw.roleCodes),
    phone: raw.phone || '-',
    email: raw.email || '-',
    status: raw.status === 1 ? '启用' : '停用',
    statusValue: raw.status ?? 0,
    createdAt: formatDateTime(raw.createdAt),
    updatedAt: formatDateTime(raw.updatedAt),
    lastLoginAt: formatDateTime(raw.lastLoginAt),
    roleIds: Array.isArray(raw.roleIds) ? raw.roleIds : [],
    roleCodes: Array.isArray(raw.roleCodes) ? raw.roleCodes : [],
    roleNames: Array.isArray(raw.roleNames) ? raw.roleNames : []
  }
}

export async function fetchAdminUserPage(params) {
  const data = await request({
    url: '/admin/users/page',
    method: 'post',
    data: buildPageQuery(params)
  })

  const page = adaptPageData(data)
  return {
    ...page,
    list: page.list.map(adaptAdminUserRow)
  }
}

export function createAdminUser(data) {
  return request({
    url: '/admin/users',
    method: 'post',
    data
  })
}
