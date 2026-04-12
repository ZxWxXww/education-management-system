import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const frontendRoot = resolve(process.cwd())

function readFrontendFile(relativePath) {
  return readFileSync(resolve(frontendRoot, relativePath), 'utf8')
}

test('admin bill page does not keep bill notifications in local storage', () => {
  const source = readFrontendFile('src/views/admin/BillManagement.vue')

  assert.equal(source.includes('BILL_NOTICE_STORAGE_KEY'), false)
  assert.equal(source.includes('localStorage'), false)
  assert.equal(source.includes('appendBillNotice'), false)
  assert.equal(source.includes('getStoredNotices'), false)
})

test('teacher class page does not render hard-coded todo data', () => {
  const source = readFrontendFile('src/views/teacher/ClassManagement.vue')

  assert.equal(source.includes('pendingTodo'), false)
  assert.equal(source.includes('todoType'), false)
})

test('teacher grade management does not keep mock markers or fake export actions', () => {
  const source = readFrontendFile('src/views/teacher/GradeManagement.vue')

  assert.equal(source.includes('Mock'), false)
  assert.equal(source.includes('Download'), false)
  assert.equal(source.includes('Plus'), false)
})

test('teacher grade analysis does not synthesize trend or risk values', () => {
  const source = readFrontendFile('src/views/teacher/GradeAnalysis.vue')

  assert.equal(source.includes('dropValue'), false)
  assert.equal(source.includes('submitRate: 100'), false)
  assert.equal(source.includes('avg - 5'), false)
})

test('admin dashboard activity cards are route-backed instead of no-op text actions', () => {
  const source = readFrontendFile('src/views/admin/Dashboard.vue')

  assert.equal(source.includes("action: '查看详情'"), false)
  assert.equal(source.includes("action: '处理'"), false)
  assert.equal(source.includes('activity-item__action'), false)
})

test('admin finance statistics does not keep fake monthly download flow or hard-coded month', () => {
  const source = readFrontendFile('src/views/admin/FinanceStatistics.vue')

  assert.equal(source.includes('handleDownload'), false)
  assert.equal(source.includes('2026-04'), false)
})

test('admin grade management no longer exposes unsupported create or export actions', () => {
  const source = readFrontendFile('src/views/admin/GradeManagement.vue')

  assert.equal(source.includes('handleCreate'), false)
  assert.equal(source.includes('handleAnalysis'), false)
  assert.equal(source.includes('handleExport'), false)
  assert.equal(source.includes('Download'), false)
  assert.equal(source.includes('Plus'), false)
})

test('teacher resource library does not keep fake upload button or fixed week range', () => {
  const source = readFrontendFile('src/views/teacher/ResourceLibrary.vue')

  assert.equal(source.includes('UploadFilled'), false)
  assert.equal(source.includes('2026-04-03'), false)
})

test('teacher workspace avoids fake comparison wording', () => {
  const source = readFrontendFile('src/views/teacher/Workspace.vue')

  assert.equal(source.includes('较昨日'), false)
})

test('admin user authorization summary cards declare current-page scope', () => {
  const source = readFrontendFile('src/views/admin/UserAuthorization.vue')

  assert.equal(source.includes('当前页授权用户'), true)
})

test('admin abnormal attendance removes fake export action', () => {
  const source = readFrontendFile('src/views/admin/AbnormalAttendance.vue')

  assert.equal(source.includes('handleExport'), false)
  assert.equal(source.includes('批量导出'), false)
})

test('admin assignment management removes unsupported actions and synthetic trend copy', () => {
  const source = readFrontendFile('src/views/admin/AssignmentManagement.vue')

  assert.equal(source.includes('handlePublish'), false)
  assert.equal(source.includes('handleExport'), false)
  assert.equal(source.includes('Calendar'), false)
  assert.equal(source.includes('Plus'), false)
  assert.equal(source.includes('UploadFilled'), false)
  assert.equal(source.includes("trend: '+2'"), false)
  assert.equal(source.includes("trend: '实时'"), false)
  assert.equal(source.includes("trend: '本周'"), false)
  assert.equal(source.includes("trend: '全课程'"), false)
})

test('admin attendance management removes fake daily export action', () => {
  const source = readFrontendFile('src/views/admin/AttendanceManagement.vue')

  assert.equal(source.includes('handleExport'), false)
  assert.equal(source.includes('导出日报'), false)
})

test('admin comparative analysis keeps only real aggregated views', () => {
  const source = readFrontendFile('src/views/admin/ComparativeAnalysis.vue')

  assert.equal(source.includes('handleExport'), false)
  assert.equal(source.includes('handleBrief'), false)
  assert.equal(source.includes('导出分析'), false)
  assert.equal(source.includes('生成简报'), false)
})

test('admin data reports removes unsupported report task creation action', () => {
  const source = readFrontendFile('src/views/admin/DataReports.vue')

  assert.equal(source.includes('handleCreate'), false)
  assert.equal(source.includes('新建报表任务'), false)
})

test('teacher abnormal attendance removes fake export and fixed synthetic scaling', () => {
  const source = readFrontendFile('src/views/teacher/AbnormalAttendance.vue')

  assert.equal(source.includes('handleExport'), false)
  assert.equal(source.includes('(item.count / 8) * 100'), false)
  assert.equal(source.includes('导出记录'), false)
})

test('teacher assignment management removes unsupported actions and placeholder duration fields', () => {
  const source = readFrontendFile('src/views/teacher/AssignmentManagement.vue')

  assert.equal(source.includes('Calendar'), false)
  assert.equal(source.includes('Plus'), false)
  assert.equal(source.includes('Upload'), false)
  assert.equal(source.includes('作业日历'), false)
  assert.equal(source.includes('发布作业'), false)
  assert.equal(source.includes('当前学周'), false)
  assert.equal(source.includes('可一键发布'), false)
  assert.equal(source.includes('avgDuration'), false)
})
test('admin bill payment channels come from shared constants instead of local duplicate maps', () => {
  const billApiSource = readFrontendFile('src/api/admin/bill.js')
  const billViewSource = readFrontendFile('src/views/admin/BillManagement.vue')
  const statusSource = readFrontendFile('src/constants/status.js')

  assert.equal(statusSource.includes('export const PAY_CHANNEL = {'), true)
  assert.equal(statusSource.includes('export const PAY_CHANNEL_OPTIONS = ['), true)
  assert.equal(billApiSource.includes('const payChannelLabelMap = {'), false)
  assert.equal(billApiSource.includes('const payChannelValueMap = {'), false)
  assert.equal(billViewSource.includes("const payChannelOptions = ['"), false)
})

test('admin order and finance pages use shared pay and bill status enums', () => {
  const orderApiSource = readFrontendFile('src/api/admin/order.js')
  const orderViewSource = readFrontendFile('src/views/admin/OrderManagement.vue')
  const financeViewSource = readFrontendFile('src/views/admin/FinanceStatistics.vue')
  const statusSource = readFrontendFile('src/constants/status.js')

  assert.equal(statusSource.includes('export const PAY_STATUS = {'), true)
  assert.equal(statusSource.includes('export const PAY_STATUS_OPTIONS = ['), true)
  assert.equal(orderApiSource.includes('const payStatusLabelMap = {'), false)
  assert.equal(orderApiSource.includes('const billStatusLabelMap = {'), false)
  assert.equal(orderViewSource.includes("const payStatusOptions = ['全部状态', '未支付', '部分支付', '已支付', '已退款']"), false)
  assert.equal(financeViewSource.includes("item.status === '已逾期'"), false)
  assert.equal(financeViewSource.includes("item.status !== '已完成'"), false)
})
