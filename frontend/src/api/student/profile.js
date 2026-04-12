import request from '../request'

function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

export async function fetchCurrentStudentProfile() {
  const data = await request({
    url: '/student/profile/me',
    method: 'get'
  })

  return {
    userId: data.userId || null,
    studentProfileId: data.studentProfileId || null,
    studentNo: data.studentNo || '-',
    name: data.realName || '-',
    grade: data.grade || '',
    className: data.className || '',
    phone: data.phone || '',
    email: data.email || '',
    guardianName: data.guardianName || '',
    guardianPhone: data.guardianPhone || '',
    address: data.address || '',
    intro: data.intro || '',
    createdAt: formatDateTime(data.createdAt),
    updatedAt: formatDateTime(data.updatedAt)
  }
}

export function updateCurrentStudentProfile(payload) {
  return request({
    url: '/student/profile',
    method: 'put',
    data: {
      realName: payload.name,
      grade: payload.grade,
      className: payload.className,
      phone: payload.phone,
      email: payload.email,
      guardianName: payload.guardianName,
      guardianPhone: payload.guardianPhone,
      address: payload.address,
      intro: payload.intro
    }
  })
}

export function updateStudentPassword(payload) {
  return request({
    url: '/student/profile/password',
    method: 'put',
    data: {
      oldPassword: payload.oldPassword,
      newPassword: payload.newPassword
    }
  })
}
