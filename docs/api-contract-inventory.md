# EduSmart Manager API Contract Inventory

更新时间：2026-04-12

本文档用于固定当前联调阶段的主契约基线，约束“前端 `src/api/` 调用、页面使用、后端控制器路径”三者一致。以下仅记录当前真实使用的主模块，不保留历史兼容读路径。

## Auth

| 模块 | 前端 API 文件 | 主要页面 | 后端控制器 / 真实路径 | 备注 |
| --- | --- | --- | --- | --- |
| 登录与角色切换 | `frontend/src/api/auth.js` | `frontend/src/views/common/Login.vue`、`frontend/src/components/common/RoleSwitch.vue` | `AuthController.java` -> `/api/auth/login`、`/api/auth/switch-role` | 登录、切换端身份都以 JWT 契约为准 |
| 健康检查 | 无页面封装 | 联调验收 / 运维检查 | `HealthController.java` -> `/api/health` | 用于环境连通性验收 |

## Admin

| 模块 | 前端 API 文件 | 主要页面 | 后端控制器 / 真实路径 | 备注 |
| --- | --- | --- | --- | --- |
| 用户管理 | `frontend/src/api/admin/user.js` | `frontend/src/views/admin/UserManagement.vue`、`frontend/src/views/admin/Dashboard.vue` | `AdminUserController.java` -> `/api/admin/users` | 主路径：分页 `/page`、新增 `/` |
| 角色授权 | `frontend/src/api/admin/roleAuth.js` | `frontend/src/views/admin/UserAuthorization.vue` | `AdminRoleController.java` -> `/api/admin/roles`；`AdminUserController.java` -> `/api/admin/users/{userId}/roles` | 已移除未使用的 `buildUserAuthPageQuery` 导出 |
| 课程管理 | `frontend/src/api/admin/course.js` | `frontend/src/views/admin/CourseManagement.vue`、`frontend/src/views/admin/Dashboard.vue` | `AdminCourseController.java` -> `/api/admin/courses` | 主路径：分页 `/page`、详情 `/{id}`、新增 `/`、更新 `/{id}`、状态更新 `/{id}` |
| 班级管理 | `frontend/src/api/admin/class.js` | `frontend/src/views/admin/ClassManagement.vue`、`frontend/src/views/admin/CourseManagement.vue`、`frontend/src/views/admin/Dashboard.vue` | `AdminClassController.java` -> `/api/admin/classes` | 主路径：分页 `/page`、详情 `/{id}`、新增 `/`、更新 `/{id}`、状态更新 `/{id}` |
| 作业管理 | `frontend/src/api/admin/assignment.js` | `frontend/src/views/admin/AssignmentManagement.vue` | `AdminAssignmentController.java` -> `/api/admin/assignments` | 当前页面只使用分页 `/page` |
| 考勤总览与异常 | `frontend/src/api/admin/attendance.js` | `frontend/src/views/admin/AttendanceManagement.vue`、`frontend/src/views/admin/AbnormalAttendance.vue`、`frontend/src/views/admin/Dashboard.vue` | `AdminAttendanceController.java` -> `/api/admin/attendance`；`AdminAttendanceExceptionController.java` -> `/api/admin/attendance-exceptions` | 主路径：概览 `/overview`、异常分页 `/page`、异常详情与处理 `/{id}` |
| 成绩管理 | `frontend/src/api/admin/grade.js` | `frontend/src/views/admin/GradeManagement.vue` | `AdminScoreController.java` -> `/api/admin/scores` | 当前页面只使用分页 `/page` |
| 订单管理 | `frontend/src/api/admin/order.js` | `frontend/src/views/admin/OrderManagement.vue` | `AdminOrderController.java` -> `/api/admin/orders` | 复用 `/api/admin/bills/student-options` 与 `/api/admin/courses/page` 作为联动下拉数据 |
| 账单管理 | `frontend/src/api/admin/bill.js` | `frontend/src/views/admin/BillManagement.vue`、`frontend/src/views/admin/FinanceStatistics.vue`、`frontend/src/views/admin/Dashboard.vue` | `AdminBillController.java` -> `/api/admin/bills` | 主路径：分页 `/page`、创建 `/`、缴费 `/{id}/payments`、退款 `/{id}/refunds`、学生选项 `/student-options` |
| 资源总览 | `frontend/src/api/admin/resource.js` | `frontend/src/views/admin/TeachingResources.vue` | `AdminTeachingResourceController.java` -> `/api/admin/resources` | 当前页面只使用概览 `/overview` |
| 展示设置 | `frontend/src/api/admin/display.js` | `frontend/src/views/admin/DisplaySettings.vue` | `AdminDisplayController.java` -> `/api/admin/display` | 主路径：当前配置 `/current`、保存 `/current`、重置 `/reset` |
| 机构设置 | `frontend/src/api/admin/settings.js` | `frontend/src/views/admin/InstitutionSettings.vue` | `AdminSettingsController.java` -> `/api/admin/settings` | 当前页面使用总览 `/overview` |
| 日志管理 | `frontend/src/api/admin/log.js` | `frontend/src/views/admin/LogManagement.vue` | `AdminLogController.java` -> `/api/admin/logs` | 主路径：分页 `/page`、归档策略 `/archive-strategy`、导出 `/export` |
| 综合报表 | `frontend/src/api/admin/report.js` | `frontend/src/views/admin/ComparativeAnalysis.vue`、`frontend/src/views/admin/DataReports.vue` | 无单独控制器，聚合现有 admin 模块接口 | 前端组合层，不作为独立后端契约 |

## Teacher

| 模块 | 前端 API 文件 | 主要页面 | 后端控制器 / 真实路径 | 备注 |
| --- | --- | --- | --- | --- |
| 教师资料 | `frontend/src/api/teacher/profile.js` | `frontend/src/views/teacher/PersonalInfo.vue`、`frontend/src/views/teacher/ChangePassword.vue` | `TeacherProfileController.java` -> `/api/teacher/profile/me`、`/api/teacher/profile`、`/api/teacher/profile/password` | 已移除历史兼容读路径，仅保留当前登录教师自助资料契约 |
| 教师班级 | `frontend/src/api/teacher/class.js` | `frontend/src/views/teacher/ClassManagement.vue`、`frontend/src/views/teacher/ClassDetails.vue`、`frontend/src/views/teacher/Workspace.vue` | `TeacherClassController.java` -> `/api/teacher/classes` | 主路径：分页 `/page`、详情 `/{id}` |
| 教师作业 | `frontend/src/api/teacher/assignment.js` | `frontend/src/views/teacher/AssignmentManagement.vue`、`frontend/src/views/teacher/Workspace.vue` | `TeacherAssignmentController.java` -> `/api/teacher/assignments` | 当前页面使用分页 `/page` |
| 教师考勤与批量处理 | `frontend/src/api/teacher/attendance.js`、`frontend/src/api/teacher/attendanceBatch.js` | `frontend/src/views/teacher/AbnormalAttendance.vue`、`frontend/src/views/teacher/AttendanceBatchProcessing.vue`、`frontend/src/views/teacher/Workspace.vue` | `TeacherAttendanceController.java` -> `/api/teacher/attendance`；`TeacherAttendanceExceptionController.java` -> `/api/teacher/attendance-exceptions` | 主路径：分页 `/page`、异常分页 `/page`、批量更新 `/batch-update`、创建批量任务 `/batch`、批量任务分页 `/batch/page` |
| 教师资源 | `frontend/src/api/teacher/resource.js` | `frontend/src/views/teacher/ResourceLibrary.vue` | `TeacherResourceController.java` -> `/api/teacher/resources/page` | 已移除未使用的详情导出 `fetchTeacherResourceDetail` |
| 教师成绩 | `frontend/src/api/teacher/score.js` | `frontend/src/views/teacher/GradeManagement.vue`、`frontend/src/views/teacher/GradeAnalysis.vue` | `TeacherScoreController.java` -> `/api/teacher/scores` | 当前页面使用分页 `/page` |

## Student

| 模块 | 前端 API 文件 | 主要页面 | 后端控制器 / 真实路径 | 备注 |
| --- | --- | --- | --- | --- |
| 学生资料 | `frontend/src/api/student/profile.js` | `frontend/src/views/student/PersonalInfo.vue`、`frontend/src/views/student/ChangePassword.vue`、`frontend/src/views/student/LearningCenter.vue`、`frontend/src/views/student/LearningResources.vue`、`frontend/src/views/student/ExamScores.vue`、`frontend/src/views/student/GradeAnalysis.vue`、`frontend/src/views/student/HomeworkSubmissions.vue` | `StudentProfileController.java` -> `/api/student/profile/me`、`/api/student/profile`、`/api/student/profile/password` | 已移除历史兼容读路径，仅保留当前登录学生自助资料契约 |
| 学生课程 | `frontend/src/api/student/course.js` | `frontend/src/views/student/ClassInformation.vue`、`frontend/src/views/student/LearningCenter.vue` | `StudentCourseController.java` -> `/api/student/courses` | 主路径：分页 `/page`、班级详情 `/classes/{id}` |
| 学生作业 | `frontend/src/api/student/assignment.js` | `frontend/src/views/student/HomeworkSubmissions.vue`、`frontend/src/views/student/LearningCenter.vue` | `StudentAssignmentController.java` -> `/api/student/assignments` | 当前页面使用提交分页 `/submissions/page` |
| 学生考勤 | `frontend/src/api/student/attendance.js` | `frontend/src/views/student/AbnormalAttendance.vue`、`frontend/src/views/student/LearningCenter.vue` | `StudentAttendanceController.java` -> `/api/student/attendance` | 当前页面使用异常分页 `/exceptions/page` |
| 学生成绩 | `frontend/src/api/student/score.js` | `frontend/src/views/student/ExamScores.vue`、`frontend/src/views/student/GradeAnalysis.vue`、`frontend/src/views/student/LearningCenter.vue` | `StudentScoreController.java` -> `/api/student/scores` | 当前页面使用分页 `/page` |
| 学生通知 | `frontend/src/api/student/notification.js` | `frontend/src/views/student/LearningCenter.vue` | `StudentNotificationController.java` -> `/api/student/notifications` | 主路径：分页 `/page`、已读 `/{id}/read` |
| 学生资源 | `frontend/src/api/student/resource.js` | `frontend/src/views/student/LearningResources.vue` | `StudentResourceController.java` -> `/api/student/resources` | 主路径：分页 `/page`、详情 `/{id}` |
| 学生账单与课时包 | `frontend/src/api/student/bill.js` | `frontend/src/views/student/PersonalInfo.vue` | `StudentBillController.java` -> `/api/student/bills` | 主路径：分页 `/page`、详情 `/{id}`、课时包汇总 `/hour-packages/summary` |

## Current Guardrails

1. 前端真实请求统一来源于 `frontend/src/api/`，页面不得直写后端 URL。
2. `student profile` 与 `teacher profile` 的读取契约统一为 `/me`，不再保留 `/{userId}` 兼容读路径。
3. `frontend/src/api/admin/report.js` 为聚合层，可组合多个 admin 接口，但不得伪造不存在的后端路径。
4. 所有新增页面接口在联调前必须同时更新：页面引用、`src/api/` 封装、后端控制器映射、本文档。
