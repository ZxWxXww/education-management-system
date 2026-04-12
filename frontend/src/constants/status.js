export const USER_STATUS = {
  DISABLED: 0,
  ENABLED: 1
}

export const ROLE_STATUS = {
  DISABLED: 0,
  ENABLED: 1
}

export const ATTENDANCE_STATUS = {
  PRESENT: 'PRESENT',
  LATE: 'LATE',
  ABSENT: 'ABSENT',
  LEAVE: 'LEAVE'
}

export const ASSIGNMENT_STATUS = {
  PENDING: 'pending',
  SUBMITTED: 'submitted',
  LATE: 'late',
  GRADED: 'graded'
}

export const SCORE_PUBLISH_STATUS = {
  DRAFT: 'DRAFT',
  PUBLISHED: 'PUBLISHED'
}

export const BILL_STATUS = {
  PENDING: 'PENDING',
  COMPLETED: 'COMPLETED',
  OVERDUE: 'OVERDUE',
  VOID: 'VOID'
}

export const PAY_STATUS = {
  UNPAID: 'UNPAID',
  PARTIAL: 'PARTIAL',
  PAID: 'PAID',
  REFUNDED: 'REFUNDED'
}

export const HOUR_PACKAGE_STATUS = {
  ACTIVE: 'ACTIVE',
  CLOSED: 'CLOSED',
  EXPIRED: 'EXPIRED'
}

export const PAY_CHANNEL = {
  CASH: 'CASH',
  ALIPAY: 'ALIPAY',
  WECHAT: 'WECHAT',
  BANK_TRANSFER: 'BANK_TRANSFER',
  OTHER: 'OTHER'
}

export const ATTENDANCE_STATUS_OPTIONS = [
  { label: '正常', value: ATTENDANCE_STATUS.PRESENT, tagType: 'success' },
  { label: '迟到', value: ATTENDANCE_STATUS.LATE, tagType: 'warning' },
  { label: '缺勤', value: ATTENDANCE_STATUS.ABSENT, tagType: 'danger' },
  { label: '请假', value: ATTENDANCE_STATUS.LEAVE, tagType: 'primary' }
]

export const ASSIGNMENT_STATUS_OPTIONS = [
  { label: '待提交', value: ASSIGNMENT_STATUS.PENDING, tagType: 'warning' },
  { label: '已提交', value: ASSIGNMENT_STATUS.SUBMITTED, tagType: 'success' },
  { label: '逾期提交', value: ASSIGNMENT_STATUS.LATE, tagType: 'danger' },
  { label: '已批改', value: ASSIGNMENT_STATUS.GRADED, tagType: 'info' }
]

export const SCORE_PUBLISH_STATUS_OPTIONS = [
  { label: '未发布', value: SCORE_PUBLISH_STATUS.DRAFT, tagType: 'info' },
  { label: '已发布', value: SCORE_PUBLISH_STATUS.PUBLISHED, tagType: 'success' }
]

export const BILL_STATUS_OPTIONS = [
  { label: '待确认', value: BILL_STATUS.PENDING, tagType: 'warning' },
  { label: '已完成', value: BILL_STATUS.COMPLETED, tagType: 'success' },
  { label: '已逾期', value: BILL_STATUS.OVERDUE, tagType: 'danger' },
  { label: '已作废', value: BILL_STATUS.VOID, tagType: 'info' }
]

export const PAY_STATUS_OPTIONS = [
  { label: '未支付', value: PAY_STATUS.UNPAID, tagType: 'danger' },
  { label: '部分支付', value: PAY_STATUS.PARTIAL, tagType: 'warning' },
  { label: '已支付', value: PAY_STATUS.PAID, tagType: 'success' },
  { label: '已退款', value: PAY_STATUS.REFUNDED, tagType: 'info' }
]

export const HOUR_PACKAGE_STATUS_OPTIONS = [
  { label: '生效中', value: HOUR_PACKAGE_STATUS.ACTIVE, tagType: 'success' },
  { label: '已结清', value: HOUR_PACKAGE_STATUS.CLOSED, tagType: 'info' },
  { label: '已过期', value: HOUR_PACKAGE_STATUS.EXPIRED, tagType: 'danger' }
]

export const PAY_CHANNEL_OPTIONS = [
  { label: '现金', value: PAY_CHANNEL.CASH },
  { label: '支付宝', value: PAY_CHANNEL.ALIPAY },
  { label: '微信', value: PAY_CHANNEL.WECHAT },
  { label: '银行转账', value: PAY_CHANNEL.BANK_TRANSFER },
  { label: '其他', value: PAY_CHANNEL.OTHER }
]

export function getStatusMeta(options, value, fallbackLabel = '-') {
  const currentValue = String(value ?? '').trim()
  const matched = options.find((item) => String(item.value).trim() === currentValue)
  return matched || { label: currentValue || fallbackLabel, value, tagType: 'info' }
}

export function getStatusLabel(options, value, fallbackLabel = '-') {
  return getStatusMeta(options, value, fallbackLabel).label
}

export function getStatusValueByLabel(options, label) {
  const currentLabel = String(label ?? '').trim()
  return options.find((item) => String(item.label).trim() === currentLabel)?.value
}

export function getBillStatusLabel(value, fallbackLabel = '-') {
  return getStatusLabel(BILL_STATUS_OPTIONS, value, fallbackLabel)
}

export function getPayStatusLabel(value, fallbackLabel = '-') {
  return getStatusLabel(PAY_STATUS_OPTIONS, value, fallbackLabel)
}

export function getPayChannelLabel(value, fallbackLabel = '-') {
  return getStatusLabel(PAY_CHANNEL_OPTIONS, value, fallbackLabel)
}
