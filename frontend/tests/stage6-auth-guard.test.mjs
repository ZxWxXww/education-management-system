import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { pathToFileURL } from 'node:url'

const SESSION_KEY = 'edu-user-session'

function createStorage() {
  const store = new Map()
  return {
    getItem(key) {
      return store.has(key) ? store.get(key) : null
    },
    setItem(key, value) {
      store.set(key, String(value))
    },
    removeItem(key) {
      store.delete(key)
    },
    clear() {
      store.clear()
    }
  }
}

function encodeBase64Url(value) {
  return Buffer.from(value, 'utf8')
    .toString('base64')
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/g, '')
}

function createJwt(payload) {
  return `${encodeBase64Url(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))}.${encodeBase64Url(JSON.stringify(payload))}.sig`
}

async function importFresh(relativePath) {
  const url = new URL(relativePath, pathToFileURL(`${process.cwd()}/`))
  url.searchParams.set('t', `${Date.now()}-${Math.random()}`)
  return import(url.href)
}

function flattenMenuPaths(items) {
  return items.flatMap((item) => [
    item.path,
    ...(Array.isArray(item.children) ? flattenMenuPaths(item.children) : [])
  ])
}

function readFrontendFile(relativePath) {
  return readFileSync(new URL(relativePath, pathToFileURL(`${process.cwd()}/`)), 'utf8')
}

test('restoreSession clears expired token from local storage', async () => {
  global.localStorage = createStorage()
  global.window = {
    atob(value) {
      return Buffer.from(value, 'base64').toString('binary')
    }
  }

  const expiredToken = createJwt({
    role: 'admin',
    exp: Math.floor(Date.now() / 1000) - 60
  })

  localStorage.setItem(
    SESSION_KEY,
    JSON.stringify({
      role: 'admin',
      token: expiredToken,
      userInfo: { id: 1 },
      roles: [{ roleCode: 'ADMIN' }],
      permissions: [{ permCode: 'dashboard:view' }]
    })
  )

  const { createPinia, setActivePinia } = await import('pinia')
  const { useUserStore } = await importFresh('./src/stores/user.js')

  setActivePinia(createPinia())
  const userStore = useUserStore()
  userStore.restoreSession()

  assert.equal(userStore.isLoggedIn, false)
  assert.equal(userStore.role, '')
  assert.equal(localStorage.getItem(SESSION_KEY), null)
})

test('admin read-only permissions stay visible in routes and menus', async () => {
  const adminRouteSource = readFrontendFile('./src/router/routes/admin.js')
  const { getMenuByRole } = await importFresh('./src/menus/index.js')

  assert.equal(adminRouteSource.includes("name: 'AdminUserManagement'") && adminRouteSource.includes("permission: ['user:view', 'user:manage', 'user:authorization:manage']"), true)
  assert.equal(adminRouteSource.includes("name: 'AdminRolePermission'") && adminRouteSource.includes("permission: ['user:role:manage', 'user:authorization:manage']"), true)
  assert.equal(adminRouteSource.includes("name: 'AdminCourseManagement'") && adminRouteSource.includes("permission: ['course:view', 'course:manage']"), true)
  assert.equal(adminRouteSource.includes("name: 'AdminClassManagement'") && adminRouteSource.includes("permission: ['class:view', 'class:manage']"), true)
  assert.equal(adminRouteSource.includes("name: 'AdminAbnormalAttendance'") && adminRouteSource.includes("permission: ['attendance:abnormal:view', 'attendance:abnormal:manage']"), true)
  assert.equal(adminRouteSource.includes("name: 'AdminLogManagement'") && adminRouteSource.includes("permission: ['setting:log:view', 'setting:log:manage']"), true)

  const visibleMenu = getMenuByRole('admin', [
    { permCode: 'dashboard:view' },
    { permCode: 'user:view' },
    { permCode: 'course:view' },
    { permCode: 'class:view' },
    { permCode: 'attendance:abnormal:view' },
    { permCode: 'setting:view' },
    { permCode: 'setting:log:view' }
  ])
  const visiblePaths = flattenMenuPaths(visibleMenu)

  assert.equal(visiblePaths.includes('/admin/user'), true)
  assert.equal(visiblePaths.includes('/admin/course'), true)
  assert.equal(visiblePaths.includes('/admin/course/classes'), true)
  assert.equal(visiblePaths.includes('/admin/attendance/abnormal'), true)
  assert.equal(visiblePaths.includes('/admin/settings/logs'), true)
})
