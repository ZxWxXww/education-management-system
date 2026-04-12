USE `edu`;
SET NAMES utf8mb4;

START TRANSACTION;

SET @seed_admin_username := 'admin';
SET @seed_teacher_username := 'user_test_01';
SET @seed_student_username := 'user_auth_02';

SET @seed_teacher_no := 'TCH-SEED-0001';
SET @seed_student_no := 'STU-AUTH-0001';
SET @seed_course_code := 'COURSE-SEED-MATH-001';
SET @seed_class_code := 'CLASS-SEED-MATH-001';
SET @seed_order_no := 'ORDER-SEED-HOUR-0001';
SET @seed_bill_no := 'BILL-SEED-HOUR-0001';
SET @seed_pay_no := 'PAY-SEED-HOUR-0001';

SET @seed_resource_title := 'Seed Math Notes';
SET @seed_assignment_01 := 'Seed Homework 01 - Functions';
SET @seed_assignment_02 := 'Seed Homework 02 - Sequences';
SET @seed_assignment_03 := 'Seed Homework 03 - Trigonometry Review';
SET @seed_exam_01 := 'Seed Monthly Quiz';
SET @seed_exam_02 := 'Seed Midterm Mock';
SET @seed_exam_03 := 'Seed Stage Review';

SET @seed_notice_01 := 'Seed Notice - Homework 02 Published';
SET @seed_notice_02 := 'Seed Notice - Billing Completed';

SET @seed_admin_user_id := (SELECT id FROM edu_user WHERE BINARY username = @seed_admin_username LIMIT 1);
SET @seed_teacher_user_id := (SELECT id FROM edu_user WHERE BINARY username = @seed_teacher_username LIMIT 1);
SET @seed_student_user_id := (SELECT id FROM edu_user WHERE BINARY username = @seed_student_username LIMIT 1);

INSERT INTO `edu_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `edu_user` u
JOIN `edu_role` r ON r.`role_code` = 'ADMIN'
WHERE BINARY u.`username` = @seed_admin_username
ON DUPLICATE KEY UPDATE `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `edu_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `edu_user` u
JOIN `edu_role` r ON r.`role_code` = 'TEACHER'
WHERE BINARY u.`username` = @seed_teacher_username
ON DUPLICATE KEY UPDATE `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `edu_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `edu_user` u
JOIN `edu_role` r ON r.`role_code` = 'STUDENT'
WHERE BINARY u.`username` = @seed_student_username
ON DUPLICATE KEY UPDATE `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `edu_teacher_profile` (
  `user_id`,
  `teacher_no`,
  `title`,
  `subject`,
  `intro`,
  `hire_date`
)
SELECT
  u.`id`,
  @seed_teacher_no,
  'Senior Teacher',
  'Mathematics',
  'Seed linkage teacher profile for integration testing.',
  '2026-04-06'
FROM `edu_user` u
WHERE BINARY u.`username` = @seed_teacher_username
ON DUPLICATE KEY UPDATE
  `teacher_no` = VALUES(`teacher_no`),
  `title` = VALUES(`title`),
  `subject` = VALUES(`subject`),
  `intro` = VALUES(`intro`),
  `hire_date` = VALUES(`hire_date`),
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `edu_student_profile` (
  `user_id`,
  `student_no`,
  `grade`,
  `class_name_text`,
  `guardian_name`,
  `guardian_phone`,
  `address`,
  `intro`,
  `entry_date`
)
SELECT
  u.`id`,
  @seed_student_no,
  'Grade 10',
  'Seed Math Cohort',
  'Seed Guardian',
  '13900000001',
  'Seed Campus, Shanghai',
  'Seed linkage student profile for integration testing.',
  '2026-04-06'
FROM `edu_user` u
WHERE BINARY u.`username` = @seed_student_username
ON DUPLICATE KEY UPDATE
  `student_no` = VALUES(`student_no`),
  `grade` = VALUES(`grade`),
  `class_name_text` = VALUES(`class_name_text`),
  `guardian_name` = VALUES(`guardian_name`),
  `guardian_phone` = VALUES(`guardian_phone`),
  `address` = VALUES(`address`),
  `intro` = VALUES(`intro`),
  `entry_date` = VALUES(`entry_date`),
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `edu_course` (
  `course_code`,
  `course_name`,
  `subject`,
  `description`,
  `status`
)
VALUES (
  @seed_course_code,
  'Seed Math Intensive',
  'Mathematics',
  'Seed course used by admin, teacher, and student linkage checks.',
  1
)
ON DUPLICATE KEY UPDATE
  `course_name` = VALUES(`course_name`),
  `subject` = VALUES(`subject`),
  `description` = VALUES(`description`),
  `status` = VALUES(`status`),
  `updated_at` = CURRENT_TIMESTAMP;

SET @seed_teacher_profile_id := (
  SELECT id FROM `edu_teacher_profile` WHERE `user_id` = @seed_teacher_user_id LIMIT 1
);
SET @seed_student_profile_id := (
  SELECT id FROM `edu_student_profile` WHERE `user_id` = @seed_student_user_id LIMIT 1
);
SET @seed_course_id := (
  SELECT id FROM `edu_course` WHERE BINARY `course_code` = @seed_course_code LIMIT 1
);

INSERT INTO `edu_class` (
  `class_code`,
  `class_name`,
  `course_id`,
  `head_teacher_id`,
  `start_date`,
  `end_date`,
  `status`
)
VALUES (
  @seed_class_code,
  'Seed Math Cohort',
  @seed_course_id,
  @seed_teacher_profile_id,
  '2026-04-06',
  '2026-08-31',
  1
)
ON DUPLICATE KEY UPDATE
  `class_name` = VALUES(`class_name`),
  `course_id` = VALUES(`course_id`),
  `head_teacher_id` = VALUES(`head_teacher_id`),
  `start_date` = VALUES(`start_date`),
  `end_date` = VALUES(`end_date`),
  `status` = VALUES(`status`),
  `updated_at` = CURRENT_TIMESTAMP;

SET @seed_class_id := (
  SELECT id FROM `edu_class` WHERE BINARY `class_code` = @seed_class_code LIMIT 1
);

DELETE FROM `edu_class_teacher`
WHERE `class_id` = @seed_class_id
  AND `teacher_id` = @seed_teacher_profile_id;

INSERT INTO `edu_class_teacher` (`class_id`, `teacher_id`, `role_in_class`)
VALUES (@seed_class_id, @seed_teacher_profile_id, 'Lead Teacher');

DELETE FROM `edu_class_student`
WHERE `class_id` = @seed_class_id
  AND `student_id` = @seed_student_profile_id;

INSERT INTO `edu_class_student` (`class_id`, `student_id`, `join_date`, `leave_date`, `status`)
VALUES (@seed_class_id, @seed_student_profile_id, '2026-04-06', NULL, 1);

DELETE FROM `edu_class_session`
WHERE `class_id` = @seed_class_id
  AND `session_date` IN ('2026-04-06', '2026-04-08', '2026-04-10', '2026-04-14');

INSERT INTO `edu_class_session` (
  `class_id`,
  `course_id`,
  `teacher_id`,
  `session_date`,
  `start_time`,
  `end_time`,
  `planned_hours`,
  `status`
)
VALUES
  (@seed_class_id, @seed_course_id, @seed_teacher_profile_id, '2026-04-06', '2026-04-06 18:30:00', '2026-04-06 20:30:00', 2.00, 'DONE'),
  (@seed_class_id, @seed_course_id, @seed_teacher_profile_id, '2026-04-08', '2026-04-08 18:30:00', '2026-04-08 20:30:00', 2.00, 'DONE'),
  (@seed_class_id, @seed_course_id, @seed_teacher_profile_id, '2026-04-10', '2026-04-10 18:30:00', '2026-04-10 20:30:00', 2.00, 'DONE'),
  (@seed_class_id, @seed_course_id, @seed_teacher_profile_id, '2026-04-14', '2026-04-14 18:30:00', '2026-04-14 20:30:00', 2.00, 'PLANNED');

SET @seed_session_0606 := (
  SELECT id FROM `edu_class_session`
  WHERE `class_id` = @seed_class_id AND `session_date` = '2026-04-06'
  ORDER BY id DESC LIMIT 1
);
SET @seed_session_0608 := (
  SELECT id FROM `edu_class_session`
  WHERE `class_id` = @seed_class_id AND `session_date` = '2026-04-08'
  ORDER BY id DESC LIMIT 1
);
SET @seed_session_0610 := (
  SELECT id FROM `edu_class_session`
  WHERE `class_id` = @seed_class_id AND `session_date` = '2026-04-10'
  ORDER BY id DESC LIMIT 1
);
SET @seed_session_0614 := (
  SELECT id FROM `edu_class_session`
  WHERE `class_id` = @seed_class_id AND `session_date` = '2026-04-14'
  ORDER BY id DESC LIMIT 1
);

DELETE s
FROM `edu_assignment_submission` s
JOIN `edu_assignment` a ON a.`id` = s.`assignment_id`
WHERE a.`class_id` = @seed_class_id
  AND a.`teacher_id` = @seed_teacher_profile_id
  AND BINARY a.`title` IN (@seed_assignment_01, @seed_assignment_02, @seed_assignment_03);

DELETE FROM `edu_assignment`
WHERE `class_id` = @seed_class_id
  AND `teacher_id` = @seed_teacher_profile_id
  AND BINARY `title` IN (@seed_assignment_01, @seed_assignment_02, @seed_assignment_03);

INSERT INTO `edu_assignment` (
  `class_id`,
  `course_id`,
  `teacher_id`,
  `title`,
  `content`,
  `attachment_type`,
  `attachment_url`,
  `publish_time`,
  `deadline`,
  `status`
)
VALUES
  (@seed_class_id, @seed_course_id, @seed_teacher_profile_id, @seed_assignment_01, 'Complete the function transformation worksheet and submit the answer summary.', 'PDF', '/seed/resources/functions-homework-01.pdf', '2026-04-07 09:00:00', '2026-04-09 23:00:00', 'PUBLISHED'),
  (@seed_class_id, @seed_course_id, @seed_teacher_profile_id, @seed_assignment_02, 'Finish the sequence practice set before the next class meeting.', 'PDF', '/seed/resources/sequences-homework-02.pdf', '2026-04-11 09:00:00', '2026-04-15 23:00:00', 'PUBLISHED'),
  (@seed_class_id, @seed_course_id, @seed_teacher_profile_id, @seed_assignment_03, 'Review trigonometric identities and upload the correction notes.', 'DOCX', '/seed/resources/trigonometry-review-03.docx', '2026-04-05 09:00:00', '2026-04-08 22:00:00', 'CLOSED');

SET @seed_assignment_id_01 := (
  SELECT id FROM `edu_assignment`
  WHERE `class_id` = @seed_class_id AND `teacher_id` = @seed_teacher_profile_id AND BINARY `title` = @seed_assignment_01
  ORDER BY id DESC LIMIT 1
);
SET @seed_assignment_id_02 := (
  SELECT id FROM `edu_assignment`
  WHERE `class_id` = @seed_class_id AND `teacher_id` = @seed_teacher_profile_id AND BINARY `title` = @seed_assignment_02
  ORDER BY id DESC LIMIT 1
);
SET @seed_assignment_id_03 := (
  SELECT id FROM `edu_assignment`
  WHERE `class_id` = @seed_class_id AND `teacher_id` = @seed_teacher_profile_id AND BINARY `title` = @seed_assignment_03
  ORDER BY id DESC LIMIT 1
);

INSERT INTO `edu_assignment_submission` (
  `assignment_id`,
  `student_id`,
  `submit_time`,
  `submit_content`,
  `attachment_type`,
  `attachment_url`,
  `score`,
  `feedback`,
  `status`
)
VALUES
  (@seed_assignment_id_01, @seed_student_profile_id, '2026-04-09 20:00:00', 'Function worksheet completed with full derivation steps.', 'PDF', '/seed/submissions/functions-01.pdf', 94.00, 'Work is complete and the derivation is clear.', 'GRADED'),
  (@seed_assignment_id_03, @seed_student_profile_id, '2026-04-09 23:30:00', 'Late upload with corrected trigonometry notes.', 'DOCX', '/seed/submissions/trigonometry-03.docx', 78.00, 'Late submission recorded. Review identity conversion again.', 'LATE');

DELETE s
FROM `edu_exam_score` s
JOIN `edu_exam` e ON e.`id` = s.`exam_id`
WHERE e.`class_id` = @seed_class_id
  AND e.`teacher_id` = @seed_teacher_profile_id
  AND BINARY e.`exam_name` IN (@seed_exam_01, @seed_exam_02, @seed_exam_03);

DELETE FROM `edu_exam`
WHERE `class_id` = @seed_class_id
  AND `teacher_id` = @seed_teacher_profile_id
  AND BINARY `exam_name` IN (@seed_exam_01, @seed_exam_02, @seed_exam_03);

INSERT INTO `edu_exam` (
  `exam_name`,
  `class_id`,
  `course_id`,
  `teacher_id`,
  `exam_time`,
  `full_score`,
  `status`
)
VALUES
  (@seed_exam_01, @seed_class_id, @seed_course_id, @seed_teacher_profile_id, '2026-04-06 19:30:00', 100.00, 'PUBLISHED'),
  (@seed_exam_02, @seed_class_id, @seed_course_id, @seed_teacher_profile_id, '2026-04-09 19:30:00', 100.00, 'PUBLISHED'),
  (@seed_exam_03, @seed_class_id, @seed_course_id, @seed_teacher_profile_id, '2026-04-11 19:30:00', 100.00, 'ARCHIVED');

SET @seed_exam_id_01 := (
  SELECT id FROM `edu_exam`
  WHERE `class_id` = @seed_class_id AND `teacher_id` = @seed_teacher_profile_id AND BINARY `exam_name` = @seed_exam_01
  ORDER BY id DESC LIMIT 1
);
SET @seed_exam_id_02 := (
  SELECT id FROM `edu_exam`
  WHERE `class_id` = @seed_class_id AND `teacher_id` = @seed_teacher_profile_id AND BINARY `exam_name` = @seed_exam_02
  ORDER BY id DESC LIMIT 1
);
SET @seed_exam_id_03 := (
  SELECT id FROM `edu_exam`
  WHERE `class_id` = @seed_class_id AND `teacher_id` = @seed_teacher_profile_id AND BINARY `exam_name` = @seed_exam_03
  ORDER BY id DESC LIMIT 1
);

INSERT INTO `edu_exam_score` (
  `exam_id`,
  `student_id`,
  `score`,
  `rank_in_class`,
  `teacher_comment`,
  `publish_time`
)
VALUES
  (@seed_exam_id_01, @seed_student_profile_id, 91.00, 1, 'Strong work on function analysis.', '2026-04-06 21:00:00'),
  (@seed_exam_id_02, @seed_student_profile_id, 88.00, 1, 'Sequence proof needs tighter notation.', '2026-04-09 21:00:00'),
  (@seed_exam_id_03, @seed_student_profile_id, 93.00, 1, 'Stage review score is stable and improving.', '2026-04-11 21:00:00');

DELETE FROM `edu_teaching_resource`
WHERE `class_id` = @seed_class_id
  AND `teacher_id` = @seed_teacher_profile_id
  AND BINARY `title` = @seed_resource_title;

INSERT INTO `edu_teaching_resource` (
  `class_id`,
  `course_id`,
  `teacher_id`,
  `title`,
  `category`,
  `file_type`,
  `file_name`,
  `file_url`,
  `file_size_kb`,
  `description`,
  `download_count`,
  `status`
)
VALUES (
  @seed_class_id,
  @seed_course_id,
  @seed_teacher_profile_id,
  @seed_resource_title,
  'Lecture Notes',
  'PDF',
  'seed-math-notes.pdf',
  '/seed/resources/seed-math-notes.pdf',
  512,
  'Reference notes used by the student and teacher homepages.',
  12,
  1
);

DELETE e
FROM `edu_attendance_exception` e
JOIN `edu_attendance_record` r ON r.`id` = e.`attendance_record_id`
WHERE r.`class_id` = @seed_class_id
  AND r.`student_id` = @seed_student_profile_id
  AND r.`attendance_date` IN ('2026-04-06', '2026-04-08', '2026-04-10');

DELETE FROM `edu_attendance_record`
WHERE `class_id` = @seed_class_id
  AND `student_id` = @seed_student_profile_id
  AND `attendance_date` IN ('2026-04-06', '2026-04-08', '2026-04-10');

INSERT INTO `edu_attendance_record` (
  `class_id`,
  `session_id`,
  `student_id`,
  `teacher_id`,
  `attendance_date`,
  `status`,
  `remark`
)
VALUES
  (@seed_class_id, @seed_session_0606, @seed_student_profile_id, @seed_teacher_profile_id, '2026-04-06', 'PRESENT', 'Student checked in on time.'),
  (@seed_class_id, @seed_session_0608, @seed_student_profile_id, @seed_teacher_profile_id, '2026-04-08', 'LATE', 'Student arrived 15 minutes late.'),
  (@seed_class_id, @seed_session_0610, @seed_student_profile_id, @seed_teacher_profile_id, '2026-04-10', 'ABSENT', 'Student absent without prior leave.');

SET @seed_attendance_id_late := (
  SELECT id FROM `edu_attendance_record`
  WHERE `class_id` = @seed_class_id AND `student_id` = @seed_student_profile_id AND `attendance_date` = '2026-04-08'
  LIMIT 1
);
SET @seed_attendance_id_absent := (
  SELECT id FROM `edu_attendance_record`
  WHERE `class_id` = @seed_class_id AND `student_id` = @seed_student_profile_id AND `attendance_date` = '2026-04-10'
  LIMIT 1
);

INSERT INTO `edu_attendance_exception` (
  `attendance_record_id`,
  `exception_type`,
  `severity`,
  `is_resolved`,
  `resolved_by`,
  `resolved_at`,
  `resolve_remark`
)
VALUES
  (@seed_attendance_id_late, 'LATE', 'MEDIUM', 0, NULL, NULL, NULL),
  (@seed_attendance_id_absent, 'ABSENT', 'HIGH', 1, @seed_admin_user_id, '2026-04-11 10:00:00', 'Guardian confirmed make-up plan.');

SET @seed_existing_order_id := (
  SELECT id FROM `edu_order` WHERE BINARY `order_no` = @seed_order_no LIMIT 1
);
SET @seed_existing_bill_id := (
  SELECT id FROM `edu_bill` WHERE BINARY `bill_no` = @seed_bill_no LIMIT 1
);

DELETE FROM `edu_hour_deduction_record`
WHERE `hour_package_id` IN (
  SELECT id FROM `edu_student_hour_package` WHERE `order_id` = @seed_existing_order_id
);

DELETE FROM `edu_student_hour_package`
WHERE `order_id` = @seed_existing_order_id;

DELETE FROM `edu_order_item`
WHERE `order_id` = @seed_existing_order_id;

DELETE FROM `edu_bill_payment`
WHERE `bill_id` = @seed_existing_bill_id;

DELETE FROM `edu_bill`
WHERE BINARY `bill_no` = @seed_bill_no;

DELETE FROM `edu_order`
WHERE BINARY `order_no` = @seed_order_no;

INSERT INTO `edu_order` (
  `order_no`,
  `student_id`,
  `order_type`,
  `amount_total`,
  `amount_paid`,
  `pay_status`,
  `remark`
)
VALUES (
  @seed_order_no,
  @seed_student_profile_id,
  'HOUR_PACKAGE',
  2400.00,
  2400.00,
  'PAID',
  'Seed order for linkage verification.'
);

SET @seed_order_id := (
  SELECT id FROM `edu_order` WHERE BINARY `order_no` = @seed_order_no LIMIT 1
);

INSERT INTO `edu_order_item` (
  `order_id`,
  `course_id`,
  `item_name`,
  `quantity`,
  `unit_price`,
  `line_amount`,
  `hour_count`
)
VALUES (
  @seed_order_id,
  @seed_course_id,
  'Seed Math Hour Package',
  1,
  2400.00,
  2400.00,
  24.00
);

INSERT INTO `edu_bill` (
  `bill_no`,
  `order_id`,
  `student_id`,
  `class_id`,
  `bill_type`,
  `amount`,
  `paid_amount`,
  `status`,
  `due_date`,
  `remark`
)
VALUES (
  @seed_bill_no,
  @seed_order_id,
  @seed_student_profile_id,
  @seed_class_id,
  'HOUR_PACKAGE',
  2400.00,
  2400.00,
  'COMPLETED',
  '2026-04-11',
  'Seed bill for linkage verification.'
);

SET @seed_bill_id := (
  SELECT id FROM `edu_bill` WHERE BINARY `bill_no` = @seed_bill_no LIMIT 1
);

INSERT INTO `edu_bill_payment` (
  `bill_id`,
  `pay_no`,
  `pay_amount`,
  `pay_channel`,
  `pay_time`,
  `operator_user_id`
)
VALUES (
  @seed_bill_id,
  @seed_pay_no,
  2400.00,
  'ALIPAY',
  '2026-04-10 10:30:00',
  @seed_admin_user_id
);

INSERT INTO `edu_student_hour_package` (
  `student_id`,
  `course_id`,
  `order_id`,
  `total_hours`,
  `used_hours`,
  `remaining_hours`,
  `effective_date`,
  `expire_date`,
  `status`
)
VALUES (
  @seed_student_profile_id,
  @seed_course_id,
  @seed_order_id,
  24.00,
  4.00,
  20.00,
  '2026-04-06',
  '2026-08-31',
  'ACTIVE'
);

SET @seed_hour_package_id := (
  SELECT id FROM `edu_student_hour_package`
  WHERE `order_id` = @seed_order_id
  ORDER BY id DESC LIMIT 1
);

INSERT INTO `edu_hour_deduction_record` (
  `hour_package_id`,
  `student_id`,
  `class_id`,
  `session_id`,
  `deduct_hours`,
  `deduct_type`,
  `biz_date`,
  `remark`
)
VALUES
  (@seed_hour_package_id, @seed_student_profile_id, @seed_class_id, @seed_session_0608, 2.00, 'CLASS_ATTEND', '2026-04-08', 'Deducted for completed class session on 2026-04-08.'),
  (@seed_hour_package_id, @seed_student_profile_id, @seed_class_id, @seed_session_0610, 2.00, 'CLASS_ATTEND', '2026-04-10', 'Deducted for completed class session on 2026-04-10.');

DELETE FROM `edu_notification_read`
WHERE `notification_id` IN (
  SELECT id FROM `edu_notification`
  WHERE BINARY `title` IN (@seed_notice_01, @seed_notice_02)
);

DELETE FROM `edu_notification`
WHERE BINARY `title` IN (@seed_notice_01, @seed_notice_02);

INSERT INTO `edu_notification` (
  `notice_type`,
  `title`,
  `content`,
  `target_role`,
  `target_user_id`,
  `biz_ref_type`,
  `biz_ref_id`,
  `status`,
  `publish_time`
)
VALUES
  ('ASSIGNMENT', @seed_notice_01, 'Homework 02 has been published for the seed class. Please submit it before the deadline.', 'STUDENT', NULL, 'ASSIGNMENT', @seed_assignment_id_02, 'PUBLISHED', '2026-04-11 09:00:00'),
  ('BILL', @seed_notice_02, 'The seed hour package bill has been paid in full and the hour package is active.', 'STUDENT', @seed_student_user_id, 'BILL', @seed_bill_id, 'PUBLISHED', '2026-04-10 10:35:00');

SET @seed_notice_id_02 := (
  SELECT id FROM `edu_notification` WHERE BINARY `title` = @seed_notice_02 ORDER BY id DESC LIMIT 1
);

INSERT INTO `edu_notification_read` (
  `notification_id`,
  `user_id`,
  `is_read`,
  `read_at`
)
VALUES (
  @seed_notice_id_02,
  @seed_student_user_id,
  1,
  '2026-04-10 11:00:00'
);

INSERT INTO `edu_system_setting` (`setting_key`, `setting_value`, `setting_group`, `description`)
VALUES
  ('seed.site_title', 'EduSmart Manager Seed', 'display', 'Seed site title for linkage testing.'),
  ('seed.login_notice', 'Use the seeded admin, teacher, and student accounts for integration testing.', 'display', 'Seed login notice.'),
  ('seed.attendance_window_days', '7', 'attendance', 'Seed attendance reminder window.'),
  ('seed.hour_package_unit', 'hour', 'billing', 'Seed hour package unit.')
ON DUPLICATE KEY UPDATE
  `setting_value` = VALUES(`setting_value`),
  `setting_group` = VALUES(`setting_group`),
  `description` = VALUES(`description`),
  `updated_at` = CURRENT_TIMESTAMP;

UPDATE `edu_display_setting`
SET
  `theme_mode` = 'LIGHT',
  `layout_mode` = 'SIDEBAR',
  `custom_json` = '{"accent":"#2563eb","density":"comfortable"}',
  `updated_at` = CURRENT_TIMESTAMP
WHERE `scope` = 'SYSTEM';

INSERT INTO `edu_display_setting` (`scope`, `theme_mode`, `layout_mode`, `custom_json`)
SELECT 'SYSTEM', 'LIGHT', 'SIDEBAR', '{"accent":"#2563eb","density":"comfortable"}'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `edu_display_setting` WHERE `scope` = 'SYSTEM'
);

UPDATE `edu_display_setting`
SET
  `theme_mode` = 'LIGHT',
  `layout_mode` = 'SIDEBAR',
  `custom_json` = '{"accent":"#1d4ed8","home":"admin"}',
  `updated_at` = CURRENT_TIMESTAMP
WHERE `scope` = 'ADMIN';

INSERT INTO `edu_display_setting` (`scope`, `theme_mode`, `layout_mode`, `custom_json`)
SELECT 'ADMIN', 'LIGHT', 'SIDEBAR', '{"accent":"#1d4ed8","home":"admin"}'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `edu_display_setting` WHERE `scope` = 'ADMIN'
);

UPDATE `edu_display_setting`
SET
  `theme_mode` = 'LIGHT',
  `layout_mode` = 'SIDEBAR',
  `custom_json` = '{"accent":"#0f766e","home":"teacher"}',
  `updated_at` = CURRENT_TIMESTAMP
WHERE `scope` = 'TEACHER';

INSERT INTO `edu_display_setting` (`scope`, `theme_mode`, `layout_mode`, `custom_json`)
SELECT 'TEACHER', 'LIGHT', 'SIDEBAR', '{"accent":"#0f766e","home":"teacher"}'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `edu_display_setting` WHERE `scope` = 'TEACHER'
);

UPDATE `edu_display_setting`
SET
  `theme_mode` = 'LIGHT',
  `layout_mode` = 'SIDEBAR',
  `custom_json` = '{"accent":"#ea580c","home":"student"}',
  `updated_at` = CURRENT_TIMESTAMP
WHERE `scope` = 'STUDENT';

INSERT INTO `edu_display_setting` (`scope`, `theme_mode`, `layout_mode`, `custom_json`)
SELECT 'STUDENT', 'LIGHT', 'SIDEBAR', '{"accent":"#ea580c","home":"student"}'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `edu_display_setting` WHERE `scope` = 'STUDENT'
);

INSERT INTO `edu_operation_log` (
  `user_id`,
  `module`,
  `action`,
  `target_type`,
  `target_id`,
  `ip`,
  `result`,
  `detail`
)
SELECT
  @seed_admin_user_id,
  'AUTH',
  'SEED_LOGIN_READY',
  'USER',
  CAST(@seed_student_user_id AS CHAR),
  '127.0.0.1',
  'SUCCESS',
  'Seed student account is ready for linkage testing.'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `edu_operation_log`
  WHERE BINARY `module` = 'AUTH'
    AND BINARY `action` = 'SEED_LOGIN_READY'
    AND BINARY `target_id` = CAST(@seed_student_user_id AS CHAR)
);

INSERT INTO `edu_operation_log` (
  `user_id`,
  `module`,
  `action`,
  `target_type`,
  `target_id`,
  `ip`,
  `result`,
  `detail`
)
SELECT
  @seed_admin_user_id,
  'BILLING',
  'SEED_BILL_READY',
  'BILL',
  @seed_bill_no,
  '127.0.0.1',
  'SUCCESS',
  'Seed bill and payment records are ready for admin linkage checks.'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `edu_operation_log`
  WHERE BINARY `module` = 'BILLING'
    AND BINARY `action` = 'SEED_BILL_READY'
    AND BINARY `target_id` = @seed_bill_no
);

INSERT INTO `edu_operation_log` (
  `user_id`,
  `module`,
  `action`,
  `target_type`,
  `target_id`,
  `ip`,
  `result`,
  `detail`
)
SELECT
  @seed_teacher_user_id,
  'ATTENDANCE',
  'SEED_EXCEPTION_READY',
  'CLASS',
  @seed_class_code,
  '127.0.0.1',
  'SUCCESS',
  'Seed attendance exception records are ready for teacher and student views.'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `edu_operation_log`
  WHERE BINARY `module` = 'ATTENDANCE'
    AND BINARY `action` = 'SEED_EXCEPTION_READY'
    AND BINARY `target_id` = @seed_class_code
);

COMMIT;

SELECT 'edu_user' AS `table_name`, COUNT(*) AS `row_count` FROM `edu_user`
UNION ALL SELECT 'edu_role', COUNT(*) FROM `edu_role`
UNION ALL SELECT 'edu_user_role', COUNT(*) FROM `edu_user_role`
UNION ALL SELECT 'edu_teacher_profile', COUNT(*) FROM `edu_teacher_profile`
UNION ALL SELECT 'edu_student_profile', COUNT(*) FROM `edu_student_profile`
UNION ALL SELECT 'edu_course', COUNT(*) FROM `edu_course`
UNION ALL SELECT 'edu_class', COUNT(*) FROM `edu_class`
UNION ALL SELECT 'edu_class_teacher', COUNT(*) FROM `edu_class_teacher`
UNION ALL SELECT 'edu_class_student', COUNT(*) FROM `edu_class_student`
UNION ALL SELECT 'edu_class_session', COUNT(*) FROM `edu_class_session`
UNION ALL SELECT 'edu_assignment', COUNT(*) FROM `edu_assignment`
UNION ALL SELECT 'edu_assignment_submission', COUNT(*) FROM `edu_assignment_submission`
UNION ALL SELECT 'edu_exam', COUNT(*) FROM `edu_exam`
UNION ALL SELECT 'edu_exam_score', COUNT(*) FROM `edu_exam_score`
UNION ALL SELECT 'edu_attendance_record', COUNT(*) FROM `edu_attendance_record`
UNION ALL SELECT 'edu_attendance_exception', COUNT(*) FROM `edu_attendance_exception`
UNION ALL SELECT 'edu_teaching_resource', COUNT(*) FROM `edu_teaching_resource`
UNION ALL SELECT 'edu_order', COUNT(*) FROM `edu_order`
UNION ALL SELECT 'edu_order_item', COUNT(*) FROM `edu_order_item`
UNION ALL SELECT 'edu_bill', COUNT(*) FROM `edu_bill`
UNION ALL SELECT 'edu_bill_payment', COUNT(*) FROM `edu_bill_payment`
UNION ALL SELECT 'edu_student_hour_package', COUNT(*) FROM `edu_student_hour_package`
UNION ALL SELECT 'edu_hour_deduction_record', COUNT(*) FROM `edu_hour_deduction_record`
UNION ALL SELECT 'edu_notification', COUNT(*) FROM `edu_notification`
UNION ALL SELECT 'edu_system_setting', COUNT(*) FROM `edu_system_setting`
UNION ALL SELECT 'edu_display_setting', COUNT(*) FROM `edu_display_setting`
UNION ALL SELECT 'edu_operation_log', COUNT(*) FROM `edu_operation_log`;
