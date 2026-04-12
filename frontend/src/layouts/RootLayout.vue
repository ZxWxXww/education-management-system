<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from '../components/Sidebar.vue'
import AppHeader from '../components/AppHeader.vue'
import { useUserStore } from '../stores/user'
import { useThemeStore } from '../stores/theme'

const route = useRoute()
const userStore = useUserStore()
const themeStore = useThemeStore()
const isSidebarCollapsed = ref(false)

onMounted(() => {
  themeStore.init()
})

const activePath = computed(() => route.path)

function toggleSidebar() {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}

</script>

<template>
  <el-container class="layout-container">
    <el-aside :width="isSidebarCollapsed ? '64px' : '240px'" class="layout-aside">
      <Sidebar :role="userStore.role" :permissions="userStore.permissions" :active-path="activePath" :collapsed="isSidebarCollapsed" />
    </el-aside>
    <el-container>
      <el-header height="56px" class="layout-header">
        <AppHeader :role="userStore.role" :collapsed="isSidebarCollapsed" @toggle-sidebar="toggleSidebar" />
        <el-button size="small" @click="themeStore.toggle()">{{ themeStore.mode === 'dark' ? '浅色模式' : '深色模式' }}</el-button>
      </el-header>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-container {
  height: 100%;
}

.layout-aside {
  transition: width 0.2s ease;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--el-border-color-light);
  background-color: var(--el-bg-color);
}

.layout-main {
  padding: 16px;
}
</style>

