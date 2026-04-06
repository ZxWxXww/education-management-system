import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import adminRoutes from './routes/admin'
import teacherRoutes from './routes/teacher'
import studentRoutes from './routes/student'

const routes = [
  {
    path: '/',
    redirect: '/admin/dashboard'
  },
  ...adminRoutes,
  ...teacherRoutes,
  ...studentRoutes,
  {
    path: '/:pathMatch(.*)*',
    redirect: '/admin/dashboard'
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
  const requiredRole = to.meta && to.meta.role
  if (!requiredRole) return true
  if (userStore.role !== requiredRole) {
    return roleHomePathMap[userStore.role] || '/admin/dashboard'
  }
  return true
})

export default router
