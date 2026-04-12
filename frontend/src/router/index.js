import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getFirstAccessiblePath } from '../menus'
import adminRoutes from './routes/admin'
import teacherRoutes from './routes/teacher'
import studentRoutes from './routes/student'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/common/Login.vue')
  },
  ...adminRoutes,
  ...teacherRoutes,
  ...studentRoutes,
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

function normalizeRole(role) {
  return String(role || '').trim().toLowerCase()
}

function resolveHomePath(userStore) {
  const currentRole = normalizeRole(userStore.role)
  const hasOwnedRoles = userStore.availableRoleValues.length > 0
  const currentRoleIsOwned = !hasOwnedRoles || userStore.availableRoleValues.includes(currentRole)

  if (currentRole && currentRoleIsOwned) {
    return getFirstAccessiblePath(currentRole, userStore.permissions)
  }

  return '/login'
}

router.beforeEach((to) => {
  const userStore = useUserStore()
  userStore.restoreSession()
  const homePath = resolveHomePath(userStore)
  const activeRole = normalizeRole(userStore.role)
  const hasOwnedRoles = userStore.availableRoleValues.length > 0
  const activeRoleIsOwned = !hasOwnedRoles || userStore.availableRoleValues.includes(activeRole)
  
  // 已登录用户回到登录页时，直接跳转到对应角色首页。
  if (to.path === '/login') {
    if (userStore.isLoggedIn) {
      if (homePath === '/login') {
        userStore.clearSession()
        return true
      }
      return homePath
    }
    return true
  }
  
  // 未登录则跳转到登录页
  if (!userStore.isLoggedIn) {
    return '/login'
  }

  // 如果本地活跃角色不属于当前账号真实角色集合，强制回退到安全首页。
  if (!activeRoleIsOwned) {
    return homePath
  }
  
  // 先校验当前活跃角色是否和目标路由要求一致。
  const requiredRole = normalizeRole(to.meta?.role)
  if (requiredRole && activeRole !== requiredRole) {
    return homePath
  }

  // 预留权限码扩展能力，后续路由只需加 meta.permission 即可接入。
  const requiredPermission = to.meta?.permission
  if (requiredPermission && !userStore.hasPermission(requiredPermission)) {
    return homePath
  }
  
  return true
})

export default router
