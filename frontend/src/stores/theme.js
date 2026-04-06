import { defineStore } from 'pinia'

const THEME_KEY = 'edu_theme'

function applyTheme(mode) {
  const html = document.documentElement
  if (mode === 'dark') html.classList.add('dark')
  else html.classList.remove('dark')
}

export const useThemeStore = defineStore('theme', {
  state: () => ({
    mode: 'light'
  }),
  actions: {
    init() {
      const saved = localStorage.getItem(THEME_KEY)
      if (saved === 'dark' || saved === 'light') this.mode = saved
      applyTheme(this.mode)
    },
    toggle() {
      this.mode = this.mode === 'dark' ? 'light' : 'dark'
      localStorage.setItem(THEME_KEY, this.mode)
      applyTheme(this.mode)
    }
  }
})

