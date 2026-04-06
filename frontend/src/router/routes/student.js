import RootLayout from '../../layouts/RootLayout.vue'

export default [
  {
    path: '/student',
    component: RootLayout,
    meta: { role: 'student' },
    redirect: '/student/learning-center',
    children: [
      { path: 'learning-center', name: 'StudentLearningCenter', component: () => import('../../views/student/LearningCenter.vue'), meta: { role: 'student' } },
      { path: 'courses/classes/:id', name: 'StudentClassInformation', component: () => import('../../views/student/ClassInformation.vue'), meta: { role: 'student' } },
      { path: 'assignments/exam-scores', name: 'StudentExamScores', component: () => import('../../views/student/ExamScores.vue'), meta: { role: 'student' } },
      { path: 'attendance/abnormal', name: 'StudentAbnormalAttendance', component: () => import('../../views/student/AbnormalAttendance.vue'), meta: { role: 'student' } },
      { path: 'statistics/grade-analysis', name: 'StudentGradeAnalysis', component: () => import('../../views/student/GradeAnalysis.vue'), meta: { role: 'student' } },
      { path: 'profile/change-password', name: 'StudentChangePassword', component: () => import('../../views/student/ChangePassword.vue'), meta: { role: 'student' } }
    ]
  }
]
