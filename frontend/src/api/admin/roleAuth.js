import request from '../request'
import { adaptPageData } from '../_adapters/page'
import { createAdminUser, fetchAdminUserPage } from './user'

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-'
}

function adaptRoleItem(raw) {
  return {
    id: raw.id,
    roleCode: raw.roleCode,
    roleName: raw.roleName,
    description: raw.description || '',
    status: raw.status,
    permissionIds: Array.isArray(raw.permissionIds) ? raw.permissionIds : [],
    permissionNames: Array.isArray(raw.permissionNames) ? raw.permissionNames : []
  }
}

function adaptAuthRow(raw, roleMap) {
  const roleId = raw.roleIds[0] || null
  const currentRole = roleId ? roleMap.get(roleId) : null
  return {
    id: raw.id,
    displayId: raw.displayId,
    name: raw.name,
    username: raw.username,
    role: currentRole?.roleName || raw.role || '未分配',
    roleId,
    permissions: currentRole?.permissionNames || [],
    status: raw.status,
    createdAt: raw.createdAt || formatDateTime(raw.createdAt),
    updatedAt: raw.updatedAt || formatDateTime(raw.updatedAt)
  }
}

export async function fetchAdminRoleCatalog(params = { current: 1, size: 100 }) {
  const data = await request({
    url: '/admin/roles/page',
    method: 'get',
    params
  })

  const page = adaptPageData(data)
  return {
    ...page,
    list: page.list.map(adaptRoleItem)
  }
}

export async function fetchAdminUserAuthorizationPage(params) {
  const [userPage, rolePage] = await Promise.all([
    fetchAdminUserPage(params),
    fetchAdminRoleCatalog()
  ])

  const roleMap = new Map(rolePage.list.map((item) => [item.id, item]))
  return {
    ...userPage,
    roles: rolePage.list,
    list: userPage.list.map((item) => adaptAuthRow(item, roleMap))
  }
}

export function assignAdminUserRoles(userId, roleIds) {
  return request({
    url: `/admin/users/${userId}/roles`,
    method: 'put',
    data: { roleIds }
  })
}

export async function createAuthorizedAdminUser(data) {
  const userId = await createAdminUser({
    username: data.username,
    password: data.password,
    realName: data.realName,
    phone: data.phone || null,
    email: data.email || null,
    status: data.status
  })

  await assignAdminUserRoles(userId, [data.roleId])
  return userId
}
