import axios from 'axios'
import { useUserStore } from '../stores/user'

const AUTH_EXPIRED_CODE = 40101
const FORBIDDEN_CODE = 40301

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000
})

function getUserStore() {
  const userStore = useUserStore()
  userStore.restoreSession()
  return userStore
}

function redirectToLogin() {
  if (window.location.pathname !== '/login') {
    window.location.replace('/login')
  }
}

function resolveErrorMessage(payload, fallback = '请求失败') {
  if (typeof payload === 'string' && payload.trim()) {
    if (payload.trim().startsWith('<!doctype html') || payload.trim().startsWith('<html')) {
      return fallback
    }
    return payload
  }
  if (payload && typeof payload.message === 'string' && payload.message.trim()) {
    return payload.message
  }
  return fallback
}

function handleAuthError(codeOrStatus) {
  if (codeOrStatus === AUTH_EXPIRED_CODE || codeOrStatus === 401) {
    // 登录态失效时统一清理本地会话，并回到登录页重新认证。
    getUserStore().clearSession()
    redirectToLogin()
  }
}

request.interceptors.request.use((config) => {
  const userStore = getUserStore()
  if (userStore.token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

request.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && typeof body.code === 'number') {
      if (body.code === 0) return body.data
      handleAuthError(body.code)
      const fallback = body.code === FORBIDDEN_CODE ? '无权限访问' : '请求失败'
      return Promise.reject(new Error(resolveErrorMessage(body, fallback)))
    }
    return body
  },
  (err) => {
    const status = err?.response?.status
    handleAuthError(status)
    const fallback =
      status === 401 ? '未登录或登录已过期' : status === 403 ? '无权限访问' : '请求失败'
    const message =
      resolveErrorMessage(err?.response?.data, '') ||
      resolveErrorMessage(err?.response?.data?.error, '') ||
      (status === 401 || status === 403 ? '' : resolveErrorMessage(err?.message, '')) ||
      fallback
    return Promise.reject(new Error(message))
  }
)

export default request

