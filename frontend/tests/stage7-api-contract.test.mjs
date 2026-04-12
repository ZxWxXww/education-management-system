import test from 'node:test'
import assert from 'node:assert/strict'
import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const frontendRoot = resolve(process.cwd())
const backendRoot = resolve(frontendRoot, '../backend')
const docsRoot = resolve(frontendRoot, '../docs')

function readFrontendFile(relativePath) {
  return readFileSync(resolve(frontendRoot, relativePath), 'utf8')
}

function readBackendFile(relativePath) {
  return readFileSync(resolve(backendRoot, relativePath), 'utf8')
}

function readDocsFile(relativePath) {
  return readFileSync(resolve(docsRoot, relativePath), 'utf8')
}

function extractBackendRoutes(source) {
  const baseMatch = source.match(/@RequestMapping\("([^"]+)"\)/)
  const basePath = baseMatch?.[1] || ''
  const routeMatches = source.matchAll(/@(GetMapping|PostMapping|PutMapping|DeleteMapping)(?:\("([^"]*)"\))?/g)

  return [...routeMatches].map((match) => ({
    method: match[1].replace('Mapping', '').toUpperCase(),
    path: `${basePath}${match[2] || ''}`.replace(/\/+/g, '/')
  }))
}

function extractFrontendCalls(source) {
  const matches = source.matchAll(/request\(\{\s*[\s\S]*?url:\s*(?:'([^']+)'|"([^"]+)"|`([^`]+)`)\s*,[\s\S]*?method:\s*'([^']+)'/g)
  return [...matches]
    .map((match) => ({
      method: match[4].toUpperCase(),
      path: `/api${match[1] || match[2] || match[3]}`
    }))
    .filter((item) => !item.path.includes('${'))
}

test('student profile controller keeps only the canonical self profile read path', () => {
  const studentController = readBackendFile(
    'src/main/java/com/edusmart/manager/controller/student/StudentProfileController.java'
  )
  const studentApi = readFrontendFile('src/api/student/profile.js')

  assert.equal(studentController.includes('@GetMapping("/me")'), true)
  assert.equal(studentController.includes('@GetMapping("/{userId}")'), false)
  assert.equal(studentApi.includes("url: '/student/profile/me'"), true)
})

test('teacher profile controller keeps only the canonical self profile read path', () => {
  const teacherController = readBackendFile(
    'src/main/java/com/edusmart/manager/controller/teacher/TeacherProfileController.java'
  )
  const teacherApi = readFrontendFile('src/api/teacher/profile.js')

  assert.equal(teacherController.includes('@GetMapping("/me")'), true)
  assert.equal(teacherController.includes('@GetMapping("/{userId}")'), false)
  assert.equal(teacherApi.includes("url: '/teacher/profile/me'"), true)
})

test('frontend API modules do not expose known dead contract helpers', () => {
  const roleAuthApi = readFrontendFile('src/api/admin/roleAuth.js')
  const teacherResourceApi = readFrontendFile('src/api/teacher/resource.js')

  assert.equal(roleAuthApi.includes('export function buildUserAuthPageQuery'), false)
  assert.equal(teacherResourceApi.includes('export async function fetchTeacherResourceDetail'), false)
})

test('frontend concrete API calls all map to an existing backend controller route', () => {
  const controllerFiles = [
    'src/main/java/com/edusmart/manager/controller/AuthController.java',
    'src/main/java/com/edusmart/manager/controller/admin/AdminAssignmentController.java',
    'src/main/java/com/edusmart/manager/controller/admin/AdminAttendanceController.java',
    'src/main/java/com/edusmart/manager/controller/admin/AdminAttendanceExceptionController.java',
    'src/main/java/com/edusmart/manager/controller/admin/AdminBillController.java',
    'src/main/java/com/edusmart/manager/controller/admin/AdminClassController.java',
    'src/main/java/com/edusmart/manager/controller/admin/AdminCourseController.java',
    'src/main/java/com/edusmart/manager/controller/admin/AdminDisplayController.java',
    'src/main/java/com/edusmart/manager/controller/admin/AdminLogController.java',
    'src/main/java/com/edusmart/manager/controller/admin/AdminOrderController.java',
    'src/main/java/com/edusmart/manager/controller/admin/AdminRoleController.java',
    'src/main/java/com/edusmart/manager/controller/admin/AdminScoreController.java',
    'src/main/java/com/edusmart/manager/controller/admin/AdminSettingsController.java',
    'src/main/java/com/edusmart/manager/controller/admin/AdminTeachingResourceController.java',
    'src/main/java/com/edusmart/manager/controller/admin/AdminUserController.java',
    'src/main/java/com/edusmart/manager/controller/student/StudentAssignmentController.java',
    'src/main/java/com/edusmart/manager/controller/student/StudentAttendanceController.java',
    'src/main/java/com/edusmart/manager/controller/student/StudentBillController.java',
    'src/main/java/com/edusmart/manager/controller/student/StudentCourseController.java',
    'src/main/java/com/edusmart/manager/controller/student/StudentNotificationController.java',
    'src/main/java/com/edusmart/manager/controller/student/StudentProfileController.java',
    'src/main/java/com/edusmart/manager/controller/student/StudentResourceController.java',
    'src/main/java/com/edusmart/manager/controller/student/StudentScoreController.java',
    'src/main/java/com/edusmart/manager/controller/teacher/TeacherAssignmentController.java',
    'src/main/java/com/edusmart/manager/controller/teacher/TeacherAttendanceController.java',
    'src/main/java/com/edusmart/manager/controller/teacher/TeacherAttendanceExceptionController.java',
    'src/main/java/com/edusmart/manager/controller/teacher/TeacherClassController.java',
    'src/main/java/com/edusmart/manager/controller/teacher/TeacherProfileController.java',
    'src/main/java/com/edusmart/manager/controller/teacher/TeacherResourceController.java',
    'src/main/java/com/edusmart/manager/controller/teacher/TeacherScoreController.java'
  ]

  const apiFiles = [
    'src/api/auth.js',
    'src/api/admin/assignment.js',
    'src/api/admin/attendance.js',
    'src/api/admin/bill.js',
    'src/api/admin/class.js',
    'src/api/admin/course.js',
    'src/api/admin/display.js',
    'src/api/admin/grade.js',
    'src/api/admin/log.js',
    'src/api/admin/order.js',
    'src/api/admin/resource.js',
    'src/api/admin/roleAuth.js',
    'src/api/admin/settings.js',
    'src/api/admin/user.js',
    'src/api/student/assignment.js',
    'src/api/student/attendance.js',
    'src/api/student/bill.js',
    'src/api/student/course.js',
    'src/api/student/notification.js',
    'src/api/student/profile.js',
    'src/api/student/resource.js',
    'src/api/student/score.js',
    'src/api/teacher/assignment.js',
    'src/api/teacher/attendance.js',
    'src/api/teacher/class.js',
    'src/api/teacher/profile.js',
    'src/api/teacher/resource.js',
    'src/api/teacher/score.js'
  ]

  const backendRoutes = new Set(
    controllerFiles
      .flatMap((file) => extractBackendRoutes(readBackendFile(file)))
      .map((route) => `${route.method} ${route.path}`)
  )
  const frontendCalls = apiFiles.flatMap((file) => extractFrontendCalls(readFrontendFile(file)))
  const missingRoutes = frontendCalls
    .map((call) => `${call.method} ${call.path}`)
    .filter((route) => !backendRoutes.has(route))

  assert.deepEqual(missingRoutes, [])
})

test('API contract inventory documents the canonical student profile path', () => {
  const docPath = resolve(docsRoot, 'api-contract-inventory.md')

  assert.equal(existsSync(docPath), true)

  const inventory = readDocsFile('api-contract-inventory.md')
  assert.equal(inventory.includes('## Student'), true)
  assert.equal(inventory.includes('## Teacher'), true)
  assert.equal(inventory.includes('/api/student/profile/me'), true)
  assert.equal(inventory.includes('/api/teacher/profile/me'), true)
  assert.equal(inventory.includes('/api/student/profile/{userId}'), false)
  assert.equal(inventory.includes('/api/teacher/profile/{userId}'), false)
})
