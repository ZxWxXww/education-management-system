import AdminLayout from '../../layouts/AdminLayout.vue'

export default [
  {
    path: '/admin',
    component: AdminLayout,
    meta: { role: 'admin' },
    redirect: '/admin/dashboard',
    children: [
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('../../views/admin/Dashboard.vue'), meta: { role: 'admin' } },
      { path: 'user', name: 'AdminUserManagement', component: () => import('../../views/admin/UserManagement.vue'), meta: { role: 'admin' } },
      { path: 'user/roles', name: 'AdminRolePermission', component: () => import('../../views/admin/RolePermission.vue'), meta: { role: 'admin' } },
      { path: 'user/authorization', name: 'AdminUserAuthorization', component: () => import('../../views/admin/UserAuthorization.vue'), meta: { role: 'admin' } },
      { path: 'course', name: 'AdminCourseManagement', component: () => import('../../views/admin/CourseManagement.vue'), meta: { role: 'admin' } },
      { path: 'course/classes', name: 'AdminClassManagement', component: () => import('../../views/admin/ClassManagement.vue'), meta: { role: 'admin' } },
      { path: 'resources', name: 'AdminTeachingResources', component: () => import('../../views/admin/TeachingResources.vue'), meta: { role: 'admin' } },
      { path: 'resources/assignments', name: 'AdminAssignmentManagement', component: () => import('../../views/admin/AssignmentManagement.vue'), meta: { role: 'admin' } },
      { path: 'resources/grades', name: 'AdminGradeManagement', component: () => import('../../views/admin/GradeManagement.vue'), meta: { role: 'admin' } },
      { path: 'attendance', name: 'AdminAttendanceManagement', component: () => import('../../views/admin/AttendanceManagement.vue'), meta: { role: 'admin' } },
      { path: 'attendance/abnormal', name: 'AdminAbnormalAttendance', component: () => import('../../views/admin/AbnormalAttendance.vue'), meta: { role: 'admin' } },
      { path: 'finance', name: 'AdminFinanceStatistics', component: () => import('../../views/admin/FinanceStatistics.vue'), meta: { role: 'admin' } },
      { path: 'finance/bills', name: 'AdminBillManagement', component: () => import('../../views/admin/BillManagement.vue'), meta: { role: 'admin' } },
      { path: 'reports', name: 'AdminDataReports', component: () => import('../../views/admin/DataReports.vue'), meta: { role: 'admin' } },
      { path: 'reports/comparative', name: 'AdminComparativeAnalysis', component: () => import('../../views/admin/ComparativeAnalysis.vue'), meta: { role: 'admin' } },
      { path: 'settings', name: 'AdminInstitutionSettings', component: () => import('../../views/admin/InstitutionSettings.vue'), meta: { role: 'admin' } },
      { path: 'settings/logs', name: 'AdminLogManagement', component: () => import('../../views/admin/LogManagement.vue'), meta: { role: 'admin' } },
      { path: 'settings/display', name: 'AdminDisplaySettings', component: () => import('../../views/admin/DisplaySettings.vue'), meta: { role: 'admin' } }
    ]
  }
]
