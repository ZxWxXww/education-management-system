<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminHeader from '../components/admin/AdminHeader.vue'
import AdminSidebar from '../components/admin/AdminSidebar.vue'
import { useThemeStore } from '../stores/theme'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const themeStore = useThemeStore()
const userStore = useUserStore()
const collapsed = ref(false)
const activePath = computed(() => route.path)

onMounted(() => {
  themeStore.init()
})

function toggleSidebar() {
  collapsed.value = !collapsed.value
}

function handleRoleChange(role) {
  const roleHomePathMap = {
    admin: '/admin/dashboard',
    teacher: '/teacher/workspace',
    student: '/student/learning-center'
  }
  
  userStore.setRole(role)
  router.push(roleHomePathMap[role])
}
</script>

<template>
  <el-container class="admin-layout">
    <el-aside :width="collapsed ? '64px' : '240px'" class="admin-layout__aside">
      <AdminSidebar :collapsed="collapsed" :active-path="activePath" />
    </el-aside>
    <el-container>
      <el-header height="56px" class="admin-layout__header">
        <AdminHeader :collapsed="collapsed" @toggle-sidebar="toggleSidebar" @role-change="handleRoleChange" />
        <el-button size="small" @click="themeStore.toggle()">{{ themeStore.mode === 'dark' ? '浅色模式' : '深色模式' }}</el-button>
      </el-header>
      <el-main class="admin-layout__main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.admin-layout {
  height: 100%;
}

.admin-layout__aside {
  transition: width 0.2s ease;
}

.admin-layout__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--el-border-color-light);
  background-color: var(--el-bg-color);
}

.admin-layout__main {
  padding: 16px;
}
</style>