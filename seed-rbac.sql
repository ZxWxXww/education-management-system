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

-- 2) 默认管理员账号
-- 默认密码明文建议：Admin@123456（请上线前务必修改）
-- password_hash 占位：YOUR_BCRYPT_HASH_HERE
INSERT INTO `edu_user` (`username`, `password_hash`, `real_name`, `phone`, `email`, `status`)
VALUES ('admin', 'YOUR_BCRYPT_HASH_HERE', '系统管理员', '13800000000', 'admin@edusmart.local', 1)
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
  ('user:role:manage', '管理用户角色权限', 'MENU', '/admin/user/roles', 'GET'),
  ('course:view', '查看课程', 'MENU', '/admin/course', 'GET'),
  ('class:view', '查看班级', 'MENU', '/admin/course/classes', 'GET'),
  ('resource:view', '查看教学资源', 'MENU', '/admin/resources', 'GET'),
  ('assignment:manage', '管理作业', 'MENU', '/admin/resources/assignments', 'GET'),
  ('score:manage', '管理成绩', 'MENU', '/admin/resources/grades', 'GET'),
  ('attendance:view', '查看考勤', 'MENU', '/admin/attendance', 'GET'),
  ('attendance:abnormal:view', '查看异常考勤', 'MENU', '/admin/attendance/abnormal', 'GET'),
  ('finance:bill:manage', '管理财单', 'MENU', '/admin/finance/bills', 'GET'),
  ('report:compare:view', '查看对比分析报表', 'MENU', '/admin/reports/comparative', 'GET'),
  ('setting:display:manage', '管理展示设置', 'MENU', '/admin/settings/display', 'GET'),
  ('setting:log:view', '查看日志管理', 'MENU', '/admin/settings/logs', 'GET'),
  ('teacher:workspace:view', '查看教师工作台', 'MENU', '/teacher/workspace', 'GET'),
  ('student:center:view', '查看学生学习中心', 'MENU', '/student/learning-center', 'GET')
ON DUPLICATE KEY UPDATE
  `perm_name` = VALUES(`perm_name`),
  `perm_type` = VALUES(`perm_type`),
  `resource_path` = VALUES(`resource_path`),
  `http_method` = VALUES(`http_method`),
  `updated_at` = CURRENT_TIMESTAMP;

-- 4) 管理员绑定 ADMIN 角色
INSERT INTO `edu_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `edu_user` u
JOIN `edu_role` r ON r.`role_code` = 'ADMIN'
WHERE u.`username` = 'admin'
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
  'resource:view',
  'assignment:manage',
  'score:manage',
  'attendance:view',
  'attendance:abnormal:view'
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
  'resource:view'
)
WHERE r.`role_code` = 'STUDENT'
ON DUPLICATE KEY UPDATE
  `updated_at` = CURRENT_TIMESTAMP;
