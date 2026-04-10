﻿export function getMenuByRole(role) {
  if (role === 'admin') return adminMenu
  if (role === 'teacher') return teacherMenu
  return studentMenu
}

export const adminMenu = [
  { title: '仪表盘', path: '/admin/dashboard' },
  {
    title: '用户管理',
    path: '/admin/user',
    children: [
      { title: '角色权限管理', path: '/admin/user/roles' },
      {
        title: '用户角色授权',
        path: '/admin/user/authorization',
        icon: 'UserFilled'
      }
    ]
  },
  {
    title: '课程管理',
    path: '/admin/course',
    children: [{ title: '班级管理', path: '/admin/course/classes' }]
  },
  {
    title: '教学资源',
    path: '/admin/resources',
    children: [
      { title: '作业管理', path: '/admin/resources/assignments' },
      { title: '成绩管理', path: '/admin/resources/grades' }
    ]
  },
  {
    title: '考勤管理',
    path: '/admin/attendance',
    children: [{ title: '异常考勤', path: '/admin/attendance/abnormal' }]
  },
  {
    title: '财务统计',
    path: '/admin/finance',
    children: [{ title: '财单管理', path: '/admin/finance/bills' }]
  },
  {
    title: '数据报表',
    path: '/admin/reports',
    children: [{ title: '对比分析', path: '/admin/reports/comparative' }]
  },
  {
    title: '机构设置',
    path: '/admin/settings',
    children: [
      { title: '日志管理', path: '/admin/settings/logs' },
      { title: '展示设置', path: '/admin/settings/display' }
    ]
  }
]

export const teacherMenu = [
  { title: '教师工作台', path: '/teacher/workspace' },
  {
    title: '我的课程',
    path: '/teacher/courses',
    children: [
      { title: '班级详情', path: '/teacher/courses/classes/1' },
      { title: '班级管理', path: '/teacher/courses/manage' }
    ]
  },
  {
    title: '教学资源',
    path: '/teacher/resources',
    children: [
      { title: '作业管理', path: '/teacher/resources/assignments' },
      { title: '成绩管理', path: '/teacher/resources/grades' },
      {
        title: '教学资料库',
        path: '/teacher/resources/library',
        icon: 'FolderOpened',
        menuClass: 'teacher-resource-library-item'
      }
    ]
  },
  {
    title: '考勤记录',
    path: '/teacher/attendance',
    children: [
      { title: '异常考勤', path: '/teacher/attendance/abnormal', icon: 'WarningFilled' },
      {
        title: '班级考勤批量处理',
        path: '/teacher/attendance/batch-processing',
        icon: 'Operation',
        menuClass: 'teacher-attendance-batch-item'
      }
    ]
  },
  {
    title: '教学数据',
    path: '/teacher/data',
    children: [{ title: '成绩分析', path: '/teacher/data/grade-analysis' }]
  },
  {
    title: '个人信息',
    path: '/teacher/profile',
    children: [{ title: '修改密码', path: '/teacher/profile/change-password' }]
  }
]

export const studentMenu = [
  { title: '学生学习中心', path: '/student/learning-center' },
  {
    title: '课程列表',
    path: '/student/courses',
    children: [{ title: '班级信息', path: '/student/courses/classes/1' }]
  },
  {
    title: '我的作业',
    path: '/student/assignments',
    children: [
      { title: '考试成绩', path: '/student/assignments/exam-scores' },
      {
        title: '作业提交记录',
        path: '/student/assignments/submissions',
        icon: 'DocumentChecked'
      },
      {
        title: '学习资料库',
        path: '/student/assignments/resources',
        icon: 'FolderOpened'
      }
    ]
  },
  {
    title: '我的考勤',
    path: '/student/attendance',
    children: [{ title: '考勤异常', path: '/student/attendance/abnormal' }]
  },
  {
    title: '学习统计',
    path: '/student/statistics',
    children: [{ title: '成绩分析', path: '/student/statistics/grade-analysis' }]
  },
  {
    title: '个人信息',
    path: '/student/profile',
    children: [{ title: '修改密码', path: '/student/profile/change-password' }]
  }
]
