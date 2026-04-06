import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    role: 'admin',
    token: ''
  }),
  actions: {
    setRole(role) {
      this.role = role
    },
    setToken(token) {
      this.token = token
    },
    logout() {
      this.token = ''
    }
  }
})
