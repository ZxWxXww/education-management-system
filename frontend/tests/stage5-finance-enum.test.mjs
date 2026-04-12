import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const frontendRoot = resolve(process.cwd())

function readFrontendFile(relativePath) {
  return readFileSync(resolve(frontendRoot, relativePath), 'utf8')
}

test('admin bill api and bill page use shared finance status enums', () => {
  const billApiSource = readFrontendFile('src/api/admin/bill.js')
  const billViewSource = readFrontendFile('src/views/admin/BillManagement.vue')
  const statusSource = readFrontendFile('src/constants/status.js')

  assert.equal(statusSource.includes('export const BILL_STATUS = {'), true)
  assert.equal(statusSource.includes('export const BILL_STATUS_OPTIONS = ['), true)
  assert.equal(statusSource.includes('export const PAY_CHANNEL = {'), true)
  assert.equal(statusSource.includes('export const PAY_CHANNEL_OPTIONS = ['), true)

  assert.equal(billApiSource.includes('const billStatusLabelMap = {'), false)
  assert.equal(billApiSource.includes('const billStatusValueMap = {'), false)
  assert.equal(billApiSource.includes('getBillStatusLabel'), true)

  assert.equal(billViewSource.includes("const statusOptions = ['"), false)
  assert.equal(billViewSource.includes('getStatusMeta(BILL_STATUS_OPTIONS'), true)
  assert.equal(billViewSource.includes('PAY_CHANNEL_OPTIONS'), true)
})
