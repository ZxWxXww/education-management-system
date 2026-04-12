USE `edu`;
SET NAMES utf8mb4;

-- =========================================================
-- RBAC 初始化数据
-- 注意：
-- 1) 默认管理员密码哈希为占位，请替换为你后端实际加密后的值
-- 2) 该脚本可重复执行（使用 UPSERT 语义）
-- =========================================================

-- 1) 角色
INSERT INTO `edu_role` (`role_code`, `role_name`, `description`, `status`)
VALUES
  ('ADMIN', '管理员', '系统管理员角色', 1),
  ('TEACHER', '教师', '教师角色', 1),
  ('STUDENT', '学生', '学生角色', 1)
ON DUPLICATE KEY UPDATE
  `role_name` = VALUES(`role_name`),
  `description` = VALUES(`description`),
  `status` = VALUES(`status`),
  `updated_at` = CURRENT_TIMESTAMP;

-- 2) 联调种子账号
-- 默认密码明文：Admin@123456（3 个联调用账号一致，请上线前务必修改）
-- BCrypt 哈希对应 Admin@123456
INSERT INTO `edu_user` (`username`, `password_hash`, `real_name`, `phone`, `email`, `status`)
VALUES
  ('admin', '$2a$10$4Ryo8IKvF2FQV71mmtyYf.lRg5LPBVkINYxX2g6TxToXGxckbEBgW', '系统管理员', '13800000000', 'admin@edusmart.local', 1),
  ('user_test_01', '$2a$10$4Ryo8IKvF2FQV71mmtyYf.lRg5LPBVkINYxX2g6TxToXGxckbEBgW', '联调测试用户', '13912345678', 'user_test_01@example.com', 1),
  ('user_auth_02', '$2a$10$4Ryo8IKvF2FQV71mmtyYf.lRg5LPBVkINYxX2g6TxToXGxckbEBgW', '授权测试学生', '13612345678', 'user_auth_02@example.com', 1)
ON DUPLICATE KEY UPDATE
  `real_name` = VALUES(`real_name`),
  `phone` = VALUES(`phone`),
  `email` = VALUES(`email`),
  `status` = VALUES(`status`),
  `updated_at` = CURRENT_TIMESTAMP;

-- 3) 基础权限
INSERT INTO `edu_permission` (`perm_code`, `perm_name`, `perm_type`, `resource_path`, `http_method`)
VALUES
  ('dashboard:view', '查看仪表盘', 'MENU', '/admin/dashboard', 'GET'),
  ('user:view', '查看用户', 'MENU', '/admin/user', 'GET'),
  ('user:manage', '管理用户', 'MENU', '/admin/user', 'GET'),
  ('user:authorization:manage', '管理用户角色授权', 'MENU', '/admin/user/authorization', 'GET'),
  ('user:role:manage', '管理用户角色权限', 'MENU', '/admin/user/roles', 'GET'),
  ('course:view', '查看课程', 'MENU', '/admin/course', 'GET'),
  ('course:manage', '管理课程', 'MENU', '/admin/course', 'GET'),
  ('class:view', '查看班级', 'MENU', '/admin/course/classes', 'GET'),
  ('class:manage', '管理班级', 'MENU', '/admin/course/classes', 'GET'),
  ('resource:view', '查看教学资源', 'MENU', '/admin/resources', 'GET'),
  ('resource:manage', '管理教学资源', 'MENU', '/teacher/resources/library', 'GET'),
  ('assignment:manage', '管理作业', 'MENU', '/admin/resources/assignments', 'GET'),
  ('score:manage', '管理成绩', 'MENU', '/admin/resources/grades', 'GET'),
  ('attendance:view', '查看考勤', 'MENU', '/admin/attendance', 'GET'),
  ('attendance:abnormal:view', '查看异常考勤', 'MENU', '/admin/attendance/abnormal', 'GET'),
  ('attendance:abnormal:manage', '管理异常考勤', 'MENU', '/admin/attendance/abnormal', 'GET'),
  ('finance:order:manage', '管理订单', 'MENU', '/admin/finance/orders', 'GET'),
  ('finance:bill:manage', '管理财单', 'MENU', '/admin/finance/bills', 'GET'),
  ('report:view', '查看数据报表', 'MENU', '/admin/reports', 'GET'),
  ('report:compare:view', '查看对比分析报表', 'MENU', '/admin/reports/comparative', 'GET'),
  ('setting:view', '查看机构设置', 'MENU', '/admin/settings', 'GET'),
  ('setting:display:manage', '管理展示设置', 'MENU', '/admin/settings/display', 'GET'),
  ('setting:log:view', '查看日志管理', 'MENU', '/admin/settings/logs', 'GET'),
  ('setting:log:manage', '管理日志策略', 'MENU', '/admin/settings/logs', 'GET'),
  ('teacher:workspace:view', '查看教师工作台', 'MENU', '/teacher/workspace', 'GET'),
  ('teacher:class:view', '查看教师班级', 'MENU', '/teacher/courses/manage', 'GET'),
  ('teacher:class:manage', '管理教师班级', 'MENU', '/teacher/courses/manage', 'GET'),
  ('teacher:attendance:batch:manage', '管理教师考勤批量处理', 'MENU', '/teacher/attendance/batch-processing', 'GET'),
  ('teacher:grade:analysis:view', '查看教师成绩分析', 'MENU', '/teacher/data/grade-analysis', 'GET'),
  ('teacher:profile:password:update', '修改教师密码', 'MENU', '/teacher/profile/change-password', 'PUT'),
  ('student:center:view', '查看学生学习中心', 'MENU', '/student/learning-center', 'GET'),
  ('student:class:view', '查看学生班级信息', 'MENU', '/student/courses/classes/1', 'GET'),
  ('student:exam:score:view', '查看学生考试成绩', 'MENU', '/student/assignments/exam-scores', 'GET'),
  ('student:assignment:submission:view', '查看学生作业提交记录', 'MENU', '/student/assignments/submissions', 'GET'),
  ('student:resource:view', '查看学生学习资料库', 'MENU', '/student/assignments/resources', 'GET'),
  ('student:attendance:abnormal:view', '查看学生考勤异常', 'MENU', '/student/attendance/abnormal', 'GET'),
  ('student:grade:analysis:view', '查看学生成绩分析', 'MENU', '/student/statistics/grade-analysis', 'GET'),
  ('student:profile:password:update', '修改学生密码', 'MENU', '/student/profile/change-password', 'PUT')
ON DUPLICATE KEY UPDATE
  `perm_name` = VALUES(`perm_name`),
  `perm_type` = VALUES(`perm_type`),
  `resource_path` = VALUES(`resource_path`),
  `http_method` = VALUES(`http_method`),
  `updated_at` = CURRENT_TIMESTAMP;

-- 4) 联调账号绑定角色
INSERT INTO `edu_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `edu_user` u
JOIN `edu_role` r ON r.`role_code` = 'ADMIN'
WHERE u.`username` = 'admin'
ON DUPLICATE KEY UPDATE
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `edu_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `edu_user` u
JOIN `edu_role` r ON r.`role_code` = 'TEACHER'
WHERE u.`username` = 'user_test_01'
ON DUPLICATE KEY UPDATE
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `edu_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `edu_user` u
JOIN `edu_role` r ON r.`role_code` = 'STUDENT'
WHERE u.`username` = 'user_auth_02'
ON DUPLICATE KEY UPDATE
  `updated_at` = CURRENT_TIMESTAMP;

-- 5) ADMIN 角色绑定全部基础权限
INSERT INTO `edu_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `edu_role` r
JOIN `edu_permission` p
WHERE r.`role_code` = 'ADMIN'
ON DUPLICATE KEY UPDATE
  `updated_at` = CURRENT_TIMESTAMP;

-- 6) TEACHER 角色绑定教师常用权限（示例）
INSERT INTO `edu_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `edu_role` r
JOIN `edu_permission` p ON p.`perm_code` IN (
  'teacher:workspace:view',
  'teacher:class:view',
  'teacher:class:manage',
  'resource:view',
  'resource:manage',
  'assignment:manage',
  'score:manage',
  'attendance:abnormal:view',
  'attendance:abnormal:manage',
  'teacher:attendance:batch:manage',
  'teacher:grade:analysis:view',
  'teacher:profile:password:update'
)
WHERE r.`role_code` = 'TEACHER'
ON DUPLICATE KEY UPDATE
  `updated_at` = CURRENT_TIMESTAMP;

-- 7) STUDENT 角色绑定学生常用权限（示例）
INSERT INTO `edu_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `edu_role` r
JOIN `edu_permission` p ON p.`perm_code` IN (
  'student:center:view',
  'student:class:view',
  'student:exam:score:view',
  'student:assignment:submission:view',
  'student:resource:view',
  'student:attendance:abnormal:view',
  'student:grade:analysis:view',
  'student:profile:password:update'
)
WHERE r.`role_code` = 'STUDENT'
ON DUPLICATE KEY UPDATE
  `updated_at` = CURRENT_TIMESTAMP;
