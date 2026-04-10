import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
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

const roleHomePathMap = {
  admin: '/admin/dashboard',
  teacher: '/teacher/workspace',
  student: '/student/learning-center'
}

router.beforeEach((to) => {
  const userStore = useUserStore()
  
  // 登录页不需要验证
  if (to.path === '/login') {
    return true
  }
  
  // 未登录则跳转到登录页
  if (!userStore.token) {
    return '/login'
  }
  
  // 检查角色权限
  const requiredRole = to.meta && to.meta.role
  if (requiredRole && userStore.role !== requiredRole) {
    // 角色不匹配，跳转到对应角色的首页
    return roleHomePathMap[userStore.role] || '/login'
  }
  
  return true
})

export default router