import { defineStore } from 'pinia'

const USER_STORAGE_KEY = 'edu-user-session'

function getDefaultState() {
  return {
    role: '',
    token: '',
    userInfo: null,
    roles: [],
    permissions: [],
    initialized: false
  }
}

function readStoredSession() {
  try {
    const raw = localStorage.getItem(USER_STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch (error) {
    return null
  }
}

function normalizeRoleCode(role) {
  return String(role || '').trim().toLowerCase()
}

function decodeJwtPayload(token) {
  try {
    const parts = String(token || '').split('.')
    if (parts.length < 2) return null
    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=')
    return JSON.parse(window.atob(padded))
  } catch (error) {
    return null
  }
}

function resolveRoleFromToken(token) {
  const payload = decodeJwtPayload(token)
  return normalizeRoleCode(payload?.role)
}

function isTokenExpired(token) {
  const payload = decodeJwtPayload(token)
  const exp = Number(payload?.exp)
  if (!Number.isFinite(exp) || exp <= 0) {
    return false
  }
  return exp * 1000 <= Date.now()
}

function normalizePermissionCode(permission) {
  return String(permission || '').trim()
}

export const useUserStore = defineStore('user', {
  state: () => getDefaultState(),
  getters: {
    isLoggedIn(state) {
      return Boolean(state.token)
    },
    availableRoleValues(state) {
      return state.roles
        .map((item) => normalizeRoleCode(item?.roleCode))
        .filter(Boolean)
    }
  },
  actions: {
    persistSession() {
      localStorage.setItem(
        USER_STORAGE_KEY,
        JSON.stringify({
          role: this.role,
          token: this.token,
          userInfo: this.userInfo,
          roles: this.roles,
          permissions: this.permissions
        })
      )
    },
    clearSession() {
      Object.assign(this, getDefaultState(), { initialized: true })
      localStorage.removeItem(USER_STORAGE_KEY)
    },
    restoreSession() {
      if (this.initialized) return

      const stored = readStoredSession()
      if (stored?.token) {
        const tokenRole = resolveRoleFromToken(stored.token)
        if (!tokenRole || isTokenExpired(stored.token)) {
          localStorage.removeItem(USER_STORAGE_KEY)
          this.initialized = true
          return
        }

        this.role = tokenRole
        this.token = stored.token || ''
        this.userInfo = stored.userInfo || null
        this.roles = Array.isArray(stored.roles) ? stored.roles : []
        this.permissions = Array.isArray(stored.permissions) ? stored.permissions : []
      }

      this.initialized = true
    },
    setLoginInfo(loginData) {
      const nextToken = loginData?.token || ''
      if (nextToken && isTokenExpired(nextToken)) {
        this.clearSession()
        return
      }
      this.token = nextToken
      this.role = resolveRoleFromToken(loginData?.token) || normalizeRoleCode(loginData?.activeRole)
      this.userInfo = loginData?.userInfo || null
      this.roles = Array.isArray(loginData?.roles) ? loginData.roles : []
      this.permissions = Array.isArray(loginData?.permissions) ? loginData.permissions : []
      this.initialized = true
      this.persistSession()
    },
    setToken(token) {
      if (token && isTokenExpired(token)) {
        this.clearSession()
        return
      }
      this.token = token
      const tokenRole = resolveRoleFromToken(token)
      if (tokenRole) {
        this.role = tokenRole
      }
      this.persistSession()
    },
    hasPermission(permCode) {
      const requiredPermissions = Array.isArray(permCode) ? permCode : [permCode]
      if (!requiredPermissions.length) {
        return true
      }

      const permissionSet = new Set(
        this.permissions
          .map((item) => normalizePermissionCode(item?.permCode || item))
          .filter(Boolean)
      )
      return requiredPermissions.some((item) => permissionSet.has(normalizePermissionCode(item)))
    },
    hasRole(role) {
      return this.availableRoleValues.includes(normalizeRoleCode(role))
    },
    hasAnyRole(roleList = []) {
      return roleList.some((role) => this.hasRole(role))
    },
    logout() {
      this.clearSession()
    }
  }
})
