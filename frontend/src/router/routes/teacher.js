﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿import RootLayout from '../../layouts/RootLayout.vue'

export default [
  {
    path: '/teacher',
    component: RootLayout,
    meta: { role: 'teacher' },
    redirect: '/teacher/workspace',
    children: [
      { path: 'workspace', name: 'TeacherWorkspace', component: () => import('../../views/teacher/Workspace.vue'), meta: { role: 'teacher', permission: 'teacher:workspace:view' } },
      { path: 'courses/classes/:id', name: 'TeacherClassDetails', component: () => import('../../views/teacher/ClassDetails.vue'), meta: { role: 'teacher', permission: 'teacher:class:view' } },
      { path: 'courses/manage', name: 'TeacherClassManagement', component: () => import('../../views/teacher/ClassManagement.vue'), meta: { role: 'teacher', permission: 'teacher:class:view' } },
      { path: 'resources/assignments', name: 'TeacherAssignmentManagement', component: () => import('../../views/teacher/AssignmentManagement.vue'), meta: { role: 'teacher', permission: 'assignment:manage' } },
      { path: 'resources/grades', name: 'TeacherGradeManagement', component: () => import('../../views/teacher/GradeManagement.vue'), meta: { role: 'teacher', permission: 'score:manage' } },
      { path: 'resources/library', name: 'TeacherResourceLibrary', component: () => import('../../views/teacher/ResourceLibrary.vue'), meta: { role: 'teacher', permission: 'resource:view' } },
      { path: 'attendance/abnormal', name: 'TeacherAbnormalAttendance', component: () => import('../../views/teacher/AbnormalAttendance.vue'), meta: { role: 'teacher', permission: 'attendance:abnormal:view' } },
      { path: 'attendance/batch-processing', name: 'TeacherAttendanceBatchProcessing', component: () => import('../../views/teacher/AttendanceBatchProcessing.vue'), meta: { role: 'teacher', permission: 'teacher:attendance:batch:manage' } },
      { path: 'data/grade-analysis', name: 'TeacherGradeAnalysis', component: () => import('../../views/teacher/GradeAnalysis.vue'), meta: { role: 'teacher', permission: 'teacher:grade:analysis:view' } },
      { path: 'profile/change-password', name: 'TeacherChangePassword', component: () => import('../../views/teacher/ChangePassword.vue'), meta: { role: 'teacher', permission: 'teacher:profile:password:update' } }
    ]
  }
]
