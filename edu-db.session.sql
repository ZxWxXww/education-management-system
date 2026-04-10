USE `edu`;
SET NAMES utf8mb4;

-- =========================================================
-- 0. RBAC（用户、角色、权限）
-- =========================================================
CREATE TABLE IF NOT EXISTS `edu_user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(64) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `real_name` VARCHAR(64) NOT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `email` VARCHAR(128) DEFAULT NULL,
  `avatar_url` VARCHAR(500) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `last_login_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_user_username` (`username`),
  UNIQUE KEY `uk_edu_user_phone` (`phone`),
  KEY `idx_edu_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `edu_role` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `role_code` VARCHAR(64) NOT NULL COMMENT 'ADMIN/TEACHER/STUDENT',
  `role_name` VARCHAR(64) NOT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `edu_permission` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `perm_code` VARCHAR(128) NOT NULL,
  `perm_name` VARCHAR(128) NOT NULL,
  `perm_type` ENUM('MENU','API','BUTTON') NOT NULL DEFAULT 'API',
  `resource_path` VARCHAR(255) DEFAULT NULL,
  `http_method` VARCHAR(16) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_permission_code` (`perm_code`),
  KEY `idx_edu_permission_type` (`perm_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `edu_user_role` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `role_id` BIGINT UNSIGNED NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_user_role` (`user_id`,`role_id`),
  KEY `idx_edu_user_role_role_id` (`role_id`),
  CONSTRAINT `fk_edu_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `edu_user` (`id`),
  CONSTRAINT `fk_edu_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `edu_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `edu_role_permission` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT UNSIGNED NOT NULL,
  `permission_id` BIGINT UNSIGNED NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_role_permission` (`role_id`,`permission_id`),
  KEY `idx_edu_role_permission_permission_id` (`permission_id`),
  CONSTRAINT `fk_edu_role_permission_role` FOREIGN KEY (`role_id`) REFERENCES `edu_role` (`id`),
  CONSTRAINT `fk_edu_role_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `edu_permission` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 1. 组织与个人信息（管理员/教师/学生）
-- =========================================================
CREATE TABLE IF NOT EXISTS `edu_teacher_profile` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `teacher_no` VARCHAR(64) NOT NULL,
  `title` VARCHAR(64) DEFAULT NULL,
  `subject` VARCHAR(64) DEFAULT NULL,
  `intro` VARCHAR(500) DEFAULT NULL,
  `hire_date` DATE DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_teacher_profile_user_id` (`user_id`),
  UNIQUE KEY `uk_edu_teacher_profile_teacher_no` (`teacher_no`),
  CONSTRAINT `fk_edu_teacher_profile_user` FOREIGN KEY (`user_id`) REFERENCES `edu_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `edu_student_profile` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `student_no` VARCHAR(64) NOT NULL,
  `grade` VARCHAR(32) DEFAULT NULL,
  `class_name_text` VARCHAR(128) DEFAULT NULL,
  `guardian_name` VARCHAR(64) DEFAULT NULL,
  `guardian_phone` VARCHAR(20) DEFAULT NULL,
  `address` VARCHAR(255) DEFAULT NULL,
  `intro` VARCHAR(500) DEFAULT NULL,
  `entry_date` DATE DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_student_profile_user_id` (`user_id`),
  UNIQUE KEY `uk_edu_student_profile_student_no` (`student_no`),
  KEY `idx_edu_student_profile_guardian_phone` (`guardian_phone`),
  CONSTRAINT `fk_edu_student_profile_user` FOREIGN KEY (`user_id`) REFERENCES `edu_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 2. 课程、班级、课节（课程管理/班级管理/班级详情）
-- =========================================================
CREATE TABLE IF NOT EXISTS `edu_course` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `course_code` VARCHAR(64) NOT NULL,
  `course_name` VARCHAR(128) NOT NULL,
  `subject` VARCHAR(64) DEFAULT NULL,
  `description` VARCHAR(500) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_course_code` (`course_code`),
  KEY `idx_edu_course_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `edu_class` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `class_code` VARCHAR(64) NOT NULL,
  `class_name` VARCHAR(128) NOT NULL,
  `course_id` BIGINT UNSIGNED NOT NULL,
  `head_teacher_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'edu_teacher_profile.id',
  `start_date` DATE DEFAULT NULL,
  `end_date` DATE DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_class_code` (`class_code`),
  KEY `idx_edu_class_course_id` (`course_id`),
  KEY `idx_edu_class_head_teacher_id` (`head_teacher_id`),
  CONSTRAINT `fk_edu_class_course` FOREIGN KEY (`course_id`) REFERENCES `edu_course` (`id`),
  CONSTRAINT `fk_edu_class_head_teacher` FOREIGN KEY (`head_teacher_id`) REFERENCES `edu_teacher_profile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `edu_class_teacher` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `class_id` BIGINT UNSIGNED NOT NULL,
  `teacher_id` BIGINT UNSIGNED NOT NULL,
  `role_in_class` VARCHAR(32) DEFAULT '主讲',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_class_teacher` (`class_id`,`teacher_id`),
  KEY `idx_edu_class_teacher_teacher_id` (`teacher_id`),
  CONSTRAINT `fk_edu_class_teacher_class` FOREIGN KEY (`class_id`) REFERENCES `edu_class` (`id`),
  CONSTRAINT `fk_edu_class_teacher_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `edu_teacher_profile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `edu_class_student` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `class_id` BIGINT UNSIGNED NOT NULL,
  `student_id` BIGINT UNSIGNED NOT NULL,
  `join_date` DATE DEFAULT NULL,
  `leave_date` DATE DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_class_student` (`class_id`,`student_id`),
  KEY `idx_edu_class_student_student_id` (`student_id`),
  CONSTRAINT `fk_edu_class_student_class` FOREIGN KEY (`class_id`) REFERENCES `edu_class` (`id`),
  CONSTRAINT `fk_edu_class_student_student` FOREIGN KEY (`student_id`) REFERENCES `edu_student_profile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `edu_class_session` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `class_id` BIGINT UNSIGNED NOT NULL,
  `course_id` BIGINT UNSIGNED NOT NULL,
  `teacher_id` BIGINT UNSIGNED NOT NULL,
  `session_date` DATE NOT NULL,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NOT NULL,
  `planned_hours` DECIMAL(10,2) NOT NULL DEFAULT 1.00,
  `status` ENUM('PLANNED','DONE','CANCELLED') NOT NULL DEFAULT 'PLANNED',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_edu_class_session_class_id` (`class_id`),
  KEY `idx_edu_class_session_teacher_id` (`teacher_id`),
  KEY `idx_edu_class_session_date` (`session_date`),
  CONSTRAINT `fk_edu_class_session_class` FOREIGN KEY (`class_id`) REFERENCES `edu_class` (`id`),
  CONSTRAINT `fk_edu_class_session_course` FOREIGN KEY (`course_id`) REFERENCES `edu_course` (`id`),
  CONSTRAINT `fk_edu_class_session_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `edu_teacher_profile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 3. 课时包 + 扣减记录（硬性要求）
-- =========================================================
CREATE TABLE IF NOT EXISTS `edu_student_hour_package` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `student_id` BIGINT UNSIGNED NOT NULL,
  `course_id` BIGINT UNSIGNED NOT NULL,
  `order_id` BIGINT UNSIGNED DEFAULT NULL,
  `total_hours` DECIMAL(10,2) NOT NULL,
  `used_hours` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `remaining_hours` DECIMAL(10,2) NOT NULL,
  `effective_date` DATE DEFAULT NULL,
  `expire_date` DATE DEFAULT NULL,
  `status` ENUM('ACTIVE','EXPIRED','CLOSED') NOT NULL DEFAULT 'ACTIVE',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_edu_hour_pkg_student_id` (`student_id`),
  KEY `idx_edu_hour_pkg_course_id` (`course_id`),
  KEY `idx_edu_hour_pkg_order_id` (`order_id`),
  CONSTRAINT `fk_edu_hour_pkg_student` FOREIGN KEY (`student_id`) REFERENCES `edu_student_profile` (`id`),
  CONSTRAINT `fk_edu_hour_pkg_course` FOREIGN KEY (`course_id`) REFERENCES `edu_course` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `edu_hour_deduction_record` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `hour_package_id` BIGINT UNSIGNED NOT NULL,
  `student_id` BIGINT UNSIGNED NOT NULL,
  `class_id` BIGINT UNSIGNED DEFAULT NULL,
  `session_id` BIGINT UNSIGNED DEFAULT NULL,
  `deduct_hours` DECIMAL(10,2) NOT NULL,
  `deduct_type` ENUM('CLASS_ATTEND','MANUAL_ADJUST') NOT NULL DEFAULT 'CLASS_ATTEND',
  `biz_date` DATE NOT NULL,
  `remark` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_edu_hour_deduct_pkg_id` (`hour_package_id`),
  KEY `idx_edu_hour_deduct_student_id` (`student_id`),
  KEY `idx_edu_hour_deduct_class_id` (`class_id`),
  KEY `idx_edu_hour_deduct_biz_date` (`biz_date`),
  CONSTRAINT `fk_edu_hour_deduct_pkg` FOREIGN KEY (`hour_package_id`) REFERENCES `edu_student_hour_package` (`id`),
  CONSTRAINT `fk_edu_hour_deduct_student` FOREIGN KEY (`student_id`) REFERENCES `edu_student_profile` (`id`),
  CONSTRAINT `fk_edu_hour_deduct_class` FOREIGN KEY (`class_id`) REFERENCES `edu_class` (`id`),
  CONSTRAINT `fk_edu_hour_deduct_session` FOREIGN KEY (`session_id`) REFERENCES `edu_class_session` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 4. 财务（订单、财单、支付）- 财单管理
-- =========================================================
CREATE TABLE IF NOT EXISTS `edu_order` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(64) NOT NULL,
  `student_id` BIGINT UNSIGNED NOT NULL,
  `order_type` ENUM('HOUR_PACKAGE','MATERIAL','EXAM_FEE','OTHER') NOT NULL DEFAULT 'HOUR_PACKAGE',
  `amount_total` DECIMAL(12,2) NOT NULL,
  `amount_paid` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `pay_status` ENUM('UNPAID','PARTIAL','PAID','REFUNDED') NOT NULL DEFAULT 'UNPAID',
  `remark` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_order_no` (`order_no`),
  KEY `idx_edu_order_student_id` (`student_id`),
  KEY `idx_edu_order_pay_status` (`pay_status`),
  CONSTRAINT `fk_edu_order_student` FOREIGN KEY (`student_id`) REFERENCES `edu_student_profile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `edu_order_item` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT UNSIGNED NOT NULL,
  `course_id` BIGINT UNSIGNED DEFAULT NULL,
  `item_name` VARCHAR(128) NOT NULL,
  `quantity` INT NOT NULL DEFAULT 1,
  `unit_price` DECIMAL(12,2) NOT NULL,
  `line_amount` DECIMAL(12,2) NOT NULL,
  `hour_count` DECIMAL(10,2) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_edu_order_item_order_id` (`order_id`),
  KEY `idx_edu_order_item_course_id` (`course_id`),
  CONSTRAINT `fk_edu_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `edu_order` (`id`),
  CONSTRAINT `fk_edu_order_item_course` FOREIGN KEY (`course_id`) REFERENCES `edu_course` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `edu_bill` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `bill_no` VARCHAR(64) NOT NULL,
  `order_id` BIGINT UNSIGNED DEFAULT NULL,
  `student_id` BIGINT UNSIGNED NOT NULL,
  `class_id` BIGINT UNSIGNED DEFAULT NULL,
  `bill_type` ENUM('HOUR_PACKAGE','MATERIAL','EXAM_FEE','REFUND','OTHER') NOT NULL DEFAULT 'HOUR_PACKAGE',
  `amount` DECIMAL(12,2) NOT NULL,
  `paid_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `status` ENUM('PENDING','COMPLETED','OVERDUE','VOID') NOT NULL DEFAULT 'PENDING',
  `due_date` DATE DEFAULT NULL,
  `remark` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_bill_no` (`bill_no`),
  KEY `idx_edu_bill_student_id` (`student_id`),
  KEY `idx_edu_bill_status` (`status`),
  KEY `idx_edu_bill_due_date` (`due_date`),
  CONSTRAINT `fk_edu_bill_order` FOREIGN KEY (`order_id`) REFERENCES `edu_order` (`id`),
  CONSTRAINT `fk_edu_bill_student` FOREIGN KEY (`student_id`) REFERENCES `edu_student_profile` (`id`),
  CONSTRAINT `fk_edu_bill_class` FOREIGN KEY (`class_id`) REFERENCES `edu_class` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `edu_bill_payment` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `bill_id` BIGINT UNSIGNED NOT NULL,
  `pay_no` VARCHAR(64) NOT NULL,
  `pay_amount` DECIMAL(12,2) NOT NULL,
  `pay_channel` ENUM('CASH','ALIPAY','WECHAT','BANK_TRANSFER','OTHER') NOT NULL DEFAULT 'OTHER',
  `pay_time` DATETIME NOT NULL,
  `operator_user_id` BIGINT UNSIGNED DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_bill_payment_pay_no` (`pay_no`),
  KEY `idx_edu_bill_payment_bill_id` (`bill_id`),
  KEY `idx_edu_bill_payment_pay_time` (`pay_time`),
  CONSTRAINT `fk_edu_bill_payment_bill` FOREIGN KEY (`bill_id`) REFERENCES `edu_bill` (`id`),
  CONSTRAINT `fk_edu_bill_payment_operator` FOREIGN KEY (`operator_user_id`) REFERENCES `edu_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 5. 教学资源、作业、成绩（禁止视频）
-- =========================================================
CREATE TABLE IF NOT EXISTS `edu_teaching_resource` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `class_id` BIGINT UNSIGNED NOT NULL,
  `course_id` BIGINT UNSIGNED NOT NULL,
  `teacher_id` BIGINT UNSIGNED NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  `category` VARCHAR(64) DEFAULT NULL,
  `file_type` ENUM('IMAGE','PDF','WORD','PPT','EXCEL','DOC','DOCX','OTHER_DOC') NOT NULL,
  `file_name` VARCHAR(255) NOT NULL,
  `file_url` VARCHAR(500) NOT NULL,
  `file_size_kb` BIGINT UNSIGNED DEFAULT NULL,
  `description` VARCHAR(500) DEFAULT NULL,
  `download_count` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_edu_teaching_resource_class_id` (`class_id`),
  KEY `idx_edu_teaching_resource_course_id` (`course_id`),
  KEY `idx_edu_teaching_resource_teacher_id` (`teacher_id`),
  KEY `idx_edu_teaching_resource_file_type` (`file_type`),
  CONSTRAINT `fk_edu_teaching_resource_class` FOREIGN KEY (`class_id`) REFERENCES `edu_class` (`id`),
  CONSTRAINT `fk_edu_teaching_resource_course` FOREIGN KEY (`course_id`) REFERENCES `edu_course` (`id`),
  CONSTRAINT `fk_edu_teaching_resource_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `edu_teacher_profile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `edu_assignment` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `class_id` BIGINT UNSIGNED NOT NULL,
  `course_id` BIGINT UNSIGNED NOT NULL,
  `teacher_id` BIGINT UNSIGNED NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  `content` TEXT,
  `attachment_type` ENUM('IMAGE','PDF','WORD','PPT','EXCEL','DOC','DOCX','OTHER_DOC') DEFAULT NULL,
  `attachment_url` VARCHAR(500) DEFAULT NULL,
  `publish_time` DATETIME DEFAULT NULL,
  `deadline` DATETIME DEFAULT NULL,
  `status` ENUM('DRAFT','PUBLISHED','CLOSED') NOT NULL DEFAULT 'PUBLISHED',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_edu_assignment_class_id` (`class_id`),
  KEY `idx_edu_assignment_course_id` (`course_id`),
  KEY `idx_edu_assignment_teacher_id` (`teacher_id`),
  KEY `idx_edu_assignment_deadline` (`deadline`),
  CONSTRAINT `fk_edu_assignment_class` FOREIGN KEY (`class_id`) REFERENCES `edu_class` (`id`),
  CONSTRAINT `fk_edu_assignment_course` FOREIGN KEY (`course_id`) REFERENCES `edu_course` (`id`),
  CONSTRAINT `fk_edu_assignment_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `edu_teacher_profile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `edu_assignment_submission` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `assignment_id` BIGINT UNSIGNED NOT NULL,
  `student_id` BIGINT UNSIGNED NOT NULL,
  `submit_time` DATETIME DEFAULT NULL,
  `submit_content` TEXT,
  `attachment_type` ENUM('IMAGE','PDF','WORD','PPT','EXCEL','DOC','DOCX','OTHER_DOC') DEFAULT NULL,
  `attachment_url` VARCHAR(500) DEFAULT NULL,
  `score` DECIMAL(5,2) DEFAULT NULL,
  `feedback` VARCHAR(500) DEFAULT NULL,
  `status` ENUM('PENDING','SUBMITTED','LATE','GRADED') NOT NULL DEFAULT 'PENDING',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_assignment_submission_once` (`assignment_id`,`student_id`),
  KEY `idx_edu_assignment_submission_student_id` (`student_id`),
  KEY `idx_edu_assignment_submission_status` (`status`),
  CONSTRAINT `fk_edu_assignment_submission_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `edu_assignment` (`id`),
  CONSTRAINT `fk_edu_assignment_submission_student` FOREIGN KEY (`student_id`) REFERENCES `edu_student_profile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `edu_exam` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `exam_name` VARCHAR(128) NOT NULL,
  `class_id` BIGINT UNSIGNED NOT NULL,
  `course_id` BIGINT UNSIGNED NOT NULL,
  `teacher_id` BIGINT UNSIGNED NOT NULL,
  `exam_time` DATETIME NOT NULL,
  `full_score` DECIMAL(6,2) NOT NULL DEFAULT 100.00,
  `status` ENUM('DRAFT','PUBLISHED','ARCHIVED') NOT NULL DEFAULT 'PUBLISHED',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_edu_exam_class_id` (`class_id`),
  KEY `idx_edu_exam_course_id` (`course_id`),
  KEY `idx_edu_exam_exam_time` (`exam_time`),
  CONSTRAINT `fk_edu_exam_class` FOREIGN KEY (`class_id`) REFERENCES `edu_class` (`id`),
  CONSTRAINT `fk_edu_exam_course` FOREIGN KEY (`course_id`) REFERENCES `edu_course` (`id`),
  CONSTRAINT `fk_edu_exam_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `edu_teacher_profile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `edu_exam_score` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `exam_id` BIGINT UNSIGNED NOT NULL,
  `student_id` BIGINT UNSIGNED NOT NULL,
  `score` DECIMAL(6,2) NOT NULL,
  `rank_in_class` INT DEFAULT NULL,
  `teacher_comment` VARCHAR(500) DEFAULT NULL,
  `publish_time` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_exam_score_once` (`exam_id`,`student_id`),
  KEY `idx_edu_exam_score_student_id` (`student_id`),
  CONSTRAINT `fk_edu_exam_score_exam` FOREIGN KEY (`exam_id`) REFERENCES `edu_exam` (`id`),
  CONSTRAINT `fk_edu_exam_score_student` FOREIGN KEY (`student_id`) REFERENCES `edu_student_profile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 6. 考勤与异常考勤（含批量处理）
-- =========================================================
CREATE TABLE IF NOT EXISTS `edu_attendance_record` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `class_id` BIGINT UNSIGNED NOT NULL,
  `session_id` BIGINT UNSIGNED DEFAULT NULL,
  `student_id` BIGINT UNSIGNED NOT NULL,
  `teacher_id` BIGINT UNSIGNED NOT NULL,
  `attendance_date` DATE NOT NULL,
  `status` ENUM('PRESENT','LATE','LEAVE','ABSENT') NOT NULL DEFAULT 'PRESENT',
  `remark` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_attendance_unique` (`class_id`,`student_id`,`attendance_date`),
  KEY `idx_edu_attendance_status` (`status`),
  KEY `idx_edu_attendance_student_id` (`student_id`),
  CONSTRAINT `fk_edu_attendance_class` FOREIGN KEY (`class_id`) REFERENCES `edu_class` (`id`),
  CONSTRAINT `fk_edu_attendance_session` FOREIGN KEY (`session_id`) REFERENCES `edu_class_session` (`id`),
  CONSTRAINT `fk_edu_attendance_student` FOREIGN KEY (`student_id`) REFERENCES `edu_student_profile` (`id`),
  CONSTRAINT `fk_edu_attendance_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `edu_teacher_profile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `edu_attendance_exception` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `attendance_record_id` BIGINT UNSIGNED NOT NULL,
  `exception_type` ENUM('LATE','ABSENT','EARLY_LEAVE') NOT NULL,
  `severity` ENUM('LOW','MEDIUM','HIGH') NOT NULL DEFAULT 'MEDIUM',
  `is_resolved` TINYINT NOT NULL DEFAULT 0,
  `resolved_by` BIGINT UNSIGNED DEFAULT NULL,
  `resolved_at` DATETIME DEFAULT NULL,
  `resolve_remark` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_edu_attendance_exception_record_id` (`attendance_record_id`),
  KEY `idx_edu_attendance_exception_resolved` (`is_resolved`),
  CONSTRAINT `fk_edu_attendance_exception_record` FOREIGN KEY (`attendance_record_id`) REFERENCES `edu_attendance_record` (`id`),
  CONSTRAINT `fk_edu_attendance_exception_resolved_by` FOREIGN KEY (`resolved_by`) REFERENCES `edu_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `edu_attendance_batch_task` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `operator_teacher_id` BIGINT UNSIGNED NOT NULL,
  `class_id` BIGINT UNSIGNED NOT NULL,
  `attendance_date` DATE NOT NULL,
  `task_status` ENUM('PENDING','DONE','FAILED') NOT NULL DEFAULT 'PENDING',
  `remark` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_edu_attendance_batch_teacher_id` (`operator_teacher_id`),
  KEY `idx_edu_attendance_batch_class_date` (`class_id`,`attendance_date`),
  CONSTRAINT `fk_edu_attendance_batch_teacher` FOREIGN KEY (`operator_teacher_id`) REFERENCES `edu_teacher_profile` (`id`),
  CONSTRAINT `fk_edu_attendance_batch_class` FOREIGN KEY (`class_id`) REFERENCES `edu_class` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 7. 报表与对比分析
-- =========================================================
CREATE TABLE IF NOT EXISTS `edu_metric_daily` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `biz_date` DATE NOT NULL,
  `metric_code` VARCHAR(64) NOT NULL COMMENT 'revenue_total/attendance_rate/assignment_submit_rate...',
  `metric_value` DECIMAL(18,4) NOT NULL,
  `dimension_json` JSON DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_metric_daily` (`biz_date`,`metric_code`),
  KEY `idx_edu_metric_code` (`metric_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `edu_report_comparison` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `report_name` VARCHAR(128) NOT NULL,
  `left_period` VARCHAR(32) NOT NULL,
  `right_period` VARCHAR(32) NOT NULL,
  `report_data_json` JSON NOT NULL,
  `generated_by` BIGINT UNSIGNED DEFAULT NULL,
  `generated_at` DATETIME NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_edu_report_generated_at` (`generated_at`),
  CONSTRAINT `fk_edu_report_generated_by` FOREIGN KEY (`generated_by`) REFERENCES `edu_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 8. 机构设置（日志管理、展示设置）
-- =========================================================
CREATE TABLE IF NOT EXISTS `edu_system_setting` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `setting_key` VARCHAR(128) NOT NULL,
  `setting_value` TEXT,
  `setting_group` VARCHAR(64) DEFAULT 'general',
  `description` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_system_setting_key` (`setting_key`),
  KEY `idx_edu_system_setting_group` (`setting_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `edu_display_setting` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `scope` ENUM('SYSTEM','ADMIN','TEACHER','STUDENT') NOT NULL DEFAULT 'SYSTEM',
  `theme_mode` ENUM('LIGHT','DARK','AUTO') NOT NULL DEFAULT 'LIGHT',
  `layout_mode` VARCHAR(32) DEFAULT 'SIDEBAR',
  `custom_json` JSON DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_edu_display_setting_scope` (`scope`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `edu_operation_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED DEFAULT NULL,
  `module` VARCHAR(64) NOT NULL,
  `action` VARCHAR(128) NOT NULL,
  `target_type` VARCHAR(64) DEFAULT NULL,
  `target_id` VARCHAR(64) DEFAULT NULL,
  `ip` VARCHAR(64) DEFAULT NULL,
  `result` ENUM('SUCCESS','FAIL') NOT NULL DEFAULT 'SUCCESS',
  `detail` VARCHAR(500) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_edu_operation_log_user_id` (`user_id`),
  KEY `idx_edu_operation_log_module` (`module`),
  KEY `idx_edu_operation_log_created_at` (`created_at`),
  CONSTRAINT `fk_edu_operation_log_user` FOREIGN KEY (`user_id`) REFERENCES `edu_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 9. 通知公告（含账单通知）
-- =========================================================
CREATE TABLE IF NOT EXISTS `edu_notification` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `notice_type` ENUM('SYSTEM','BILL','COURSE','ASSIGNMENT','ATTENDANCE') NOT NULL DEFAULT 'SYSTEM',
  `title` VARCHAR(200) NOT NULL,
  `content` VARCHAR(1000) NOT NULL,
  `target_role` ENUM('ADMIN','TEACHER','STUDENT','ALL') NOT NULL DEFAULT 'ALL',
  `target_user_id` BIGINT UNSIGNED DEFAULT NULL,
  `biz_ref_type` VARCHAR(64) DEFAULT NULL,
  `biz_ref_id` BIGINT UNSIGNED DEFAULT NULL,
  `status` ENUM('DRAFT','PUBLISHED','CANCELLED') NOT NULL DEFAULT 'PUBLISHED',
  `publish_time` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_edu_notification_type` (`notice_type`),
  KEY `idx_edu_notification_target_user` (`target_user_id`),
  KEY `idx_edu_notification_publish_time` (`publish_time`),
  CONSTRAINT `fk_edu_notification_target_user` FOREIGN KEY (`target_user_id`) REFERENCES `edu_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `edu_notification_read` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `notification_id` BIGINT UNSIGNED NOT NULL,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `is_read` TINYINT NOT NULL DEFAULT 0,
  `read_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_notification_read` (`notification_id`,`user_id`),
  KEY `idx_edu_notification_read_user_id` (`user_id`),
  CONSTRAINT `fk_edu_notification_read_notification` FOREIGN KEY (`notification_id`) REFERENCES `edu_notification` (`id`),
  CONSTRAINT `fk_edu_notification_read_user` FOREIGN KEY (`user_id`) REFERENCES `edu_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
