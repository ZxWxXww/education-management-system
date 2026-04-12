﻿﻿﻿﻿﻿﻿﻿const adminMenu = [
  { title: '仪表盘', path: '/admin/dashboard', permission: 'dashboard:view' },
  {
    title: '用户管理',
    path: '/admin/user',
    permission: ['user:view', 'user:manage', 'user:authorization:manage'],
    children: [
      { title: '角色权限管理', path: '/admin/user/roles', permission: ['user:role:manage', 'user:authorization:manage'] },
      {
        title: '用户角色授权',
        path: '/admin/user/authorization',
        permission: 'user:authorization:manage',
        icon: 'UserFilled'
      }
    ]
  },
  {
    title: '课程管理',
    path: '/admin/course',
    permission: ['course:view', 'course:manage'],
    children: [{ title: '班级管理', path: '/admin/course/classes', permission: ['class:view', 'class:manage'] }]
  },
  {
    title: '教学资源',
    path: '/admin/resources',
    permission: 'resource:view',
    children: [
      { title: '作业管理', path: '/admin/resources/assignments', permission: 'assignment:manage' },
      { title: '成绩管理', path: '/admin/resources/grades', permission: 'score:manage' }
    ]
  },
  {
    title: '考勤管理',
    path: '/admin/attendance',
    permission: 'attendance:view',
    children: [{ title: '异常考勤', path: '/admin/attendance/abnormal', permission: ['attendance:abnormal:view', 'attendance:abnormal:manage'] }]
  },
  {
    title: '财务统计',
    path: '/admin/finance',
    permission: 'finance:bill:manage',
    children: [
      { title: '订单管理', path: '/admin/finance/orders', permission: 'finance:order:manage' },
      { title: '财单管理', path: '/admin/finance/bills', permission: 'finance:bill:manage' }
    ]
  },
  {
    title: '数据报表',
    path: '/admin/reports',
    permission: 'report:view',
    children: [{ title: '对比分析', path: '/admin/reports/comparative', permission: 'report:compare:view' }]
  },
  {
    title: '机构设置',
    path: '/admin/settings',
    permission: 'setting:view',
    children: [
      { title: '日志管理', path: '/admin/settings/logs', permission: ['setting:log:view', 'setting:log:manage'] },
      { title: '展示设置', path: '/admin/settings/display', permission: 'setting:display:manage' }
    ]
  }
]

const teacherMenu = [
  { title: '教师工作台', path: '/teacher/workspace', permission: 'teacher:workspace:view' },
  {
    title: '我的课程',
    path: '/teacher/courses',
    children: [
      { title: '班级详情', path: '/teacher/courses/classes/1', permission: 'teacher:class:view' },
      { title: '班级管理', path: '/teacher/courses/manage', permission: 'teacher:class:manage' }
    ]
  },
  {
    title: '教学资源',
    path: '/teacher/resources',
    children: [
      { title: '作业管理', path: '/teacher/resources/assignments', permission: 'assignment:manage' },
      { title: '成绩管理', path: '/teacher/resources/grades', permission: 'score:manage' },
      {
        title: '教学资料库',
        path: '/teacher/resources/library',
        permission: 'resource:view',
        icon: 'FolderOpened',
        menuClass: 'teacher-resource-library-item'
      }
    ]
  },
  {
    title: '考勤记录',
    path: '/teacher/attendance',
    children: [
      { title: '异常考勤', path: '/teacher/attendance/abnormal', permission: 'attendance:abnormal:view', icon: 'WarningFilled' },
      {
        title: '班级考勤批量处理',
        path: '/teacher/attendance/batch-processing',
        permission: 'teacher:attendance:batch:manage',
        icon: 'Operation',
        menuClass: 'teacher-attendance-batch-item'
      }
    ]
  },
  {
    title: '教学数据',
    path: '/teacher/data',
    children: [{ title: '成绩分析', path: '/teacher/data/grade-analysis', permission: 'teacher:grade:analysis:view' }]
  },
  {
    title: '个人信息',
    path: '/teacher/profile',
    children: [{ title: '修改密码', path: '/teacher/profile/change-password', permission: 'teacher:profile:password:update' }]
  }
]

const studentMenu = [
  { title: '学生学习中心', path: '/student/learning-center', permission: 'student:center:view' },
  {
    title: '课程列表',
    path: '/student/courses',
    children: [{ title: '班级信息', path: '/student/courses/classes/1', permission: 'student:class:view' }]
  },
  {
    title: '我的作业',
    path: '/student/assignments',
    children: [
      { title: '考试成绩', path: '/student/assignments/exam-scores', permission: 'student:exam:score:view' },
      {
        title: '作业提交记录',
        path: '/student/assignments/submissions',
        permission: 'student:assignment:submission:view',
        icon: 'DocumentChecked'
      },
      {
        title: '学习资料库',
        path: '/student/assignments/resources',
        permission: 'student:resource:view',
        icon: 'FolderOpened'
      }
    ]
  },
  {
    title: '我的考勤',
    path: '/student/attendance',
    children: [{ title: '考勤异常', path: '/student/attendance/abnormal', permission: 'student:attendance:abnormal:view' }]
  },
  {
    title: '学习统计',
    path: '/student/statistics',
    children: [{ title: '成绩分析', path: '/student/statistics/grade-analysis', permission: 'student:grade:analysis:view' }]
  },
  {
    title: '个人信息',
    path: '/student/profile',
    children: [{ title: '修改密码', path: '/student/profile/change-password', permission: 'student:profile:password:update' }]
  }
]

const roleMenuMap = {
  admin: adminMenu,
  teacher: teacherMenu,
  student: studentMenu
}

function buildPermissionSet(permissions = []) {
  return new Set(
    permissions
      .map((item) => (typeof item === 'string' ? item : item?.permCode))
      .map((item) => String(item || '').trim())
      .filter(Boolean)
  )
}

function hasPermission(permissionSet, permission) {
  if (!permission) return true
  if (Array.isArray(permission)) {
    return permission.some((item) => permissionSet.has(item))
  }
  return permissionSet.has(permission)
}

function filterMenuItems(items, permissionSet) {
  return items.reduce((result, item) => {
    const children = Array.isArray(item.children) ? filterMenuItems(item.children, permissionSet) : []
    const visible = hasPermission(permissionSet, item.permission)

    if (!visible && children.length === 0) {
      return result
    }

    result.push({
      ...item,
      children
    })
    return result
  }, [])
}

function pickDefaultPath(item) {
  if (Array.isArray(item.children) && item.children.length > 0) {
    return pickDefaultPath(item.children[0])
  }
  return item.path
}

export function getMenuByRole(role, permissions = []) {
  const menu = roleMenuMap[role] || studentMenu
  return filterMenuItems(menu, buildPermissionSet(permissions))
}

export function getFirstAccessiblePath(role, permissions = []) {
  const menu = getMenuByRole(role, permissions)
  if (!menu.length) {
    return '/login'
  }
  return pickDefaultPath(menu[0]) || '/login'
}

export { adminMenu, teacherMenu, studentMenu }
