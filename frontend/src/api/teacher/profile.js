import request from '../request'

function formatDate(value) {
  if (!value) return ''
  return String(value).slice(0, 10)
}

function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

function adaptTeacherProfile(data = {}) {
  return {
    userId: data.userId || null,
    teacherProfileId: data.teacherProfileId || null,
    teacherNo: data.teacherNo || '-',
    username: data.username || '',
    name: data.realName || '',
    phone: data.phone || '',
    email: data.email || '',
    avatarUrl: data.avatarUrl || '',
    title: data.title || '',
    subject: data.subject || '',
    intro: data.intro || '',
    hireDate: formatDate(data.hireDate),
    createdAt: formatDateTime(data.createdAt),
    updatedAt: formatDateTime(data.updatedAt)
  }
}

export async function fetchCurrentTeacherProfile() {
  const data = await request({
    url: '/teacher/profile/me',
    method: 'get'
  })
  return adaptTeacherProfile(data)
}

export function updateCurrentTeacherProfile(payload) {
  return request({
    url: '/teacher/profile',
    method: 'put',
    data: {
      realName: payload.name,
      phone: payload.phone,
      email: payload.email,
      title: payload.title,
      subject: payload.subject,
      intro: payload.intro,
      hireDate: payload.hireDate || null
    }
  })
}

export function updateTeacherPassword(payload) {
  return request({
    url: '/teacher/profile/password',
    method: 'put',
    data: {
      oldPassword: payload.oldPassword,
      newPassword: payload.newPassword
    }
  })
}
