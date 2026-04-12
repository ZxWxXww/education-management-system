import request from './request'

export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function switchRole(data) {
  return request({
    url: '/auth/switch-role',
    method: 'post',
    data
  })
}
