import RootLayout from '../../layouts/RootLayout.vue'

export default [
  {
    path: '/student',
    component: RootLayout,
    meta: { role: 'student' },
    redirect: '/student/learning-center',
    children: [
      { path: 'learning-center', name: 'StudentLearningCenter', component: () => import('../../views/student/LearningCenter.vue'), meta: { role: 'student', permission: 'student:center:view' } },
      { path: 'courses/classes/:id', name: 'StudentClassInformation', component: () => import('../../views/student/ClassInformation.vue'), meta: { role: 'student', permission: 'student:class:view' } },
      { path: 'assignments/exam-scores', name: 'StudentExamScores', component: () => import('../../views/student/ExamScores.vue'), meta: { role: 'student', permission: 'student:exam:score:view' } },
      { path: 'assignments/submissions', name: 'StudentHomeworkSubmissions', component: () => import('../../views/student/HomeworkSubmissions.vue'), meta: { role: 'student', permission: 'student:assignment:submission:view' } },
      { path: 'assignments/resources', name: 'StudentLearningResources', component: () => import('../../views/student/LearningResources.vue'), meta: { role: 'student', permission: 'student:resource:view' } },
      { path: 'attendance/abnormal', name: 'StudentAbnormalAttendance', component: () => import('../../views/student/AbnormalAttendance.vue'), meta: { role: 'student', permission: 'student:attendance:abnormal:view' } },
      { path: 'statistics/grade-analysis', name: 'StudentGradeAnalysis', component: () => import('../../views/student/GradeAnalysis.vue'), meta: { role: 'student', permission: 'student:grade:analysis:view' } },
      { path: 'profile/change-password', name: 'StudentChangePassword', component: () => import('../../views/student/ChangePassword.vue'), meta: { role: 'student', permission: 'student:profile:password:update' } }
    ]
  }
]
