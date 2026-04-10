<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { Avatar, SwitchButton, User, School } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const roleOptions = [
  { label: '管理员', value: 'admin', icon: Avatar },
  { label: '教师', value: 'teacher', icon: User },
  { label: '学生', value: 'student', icon: School }
]

const roleHomePathMap = {
  admin: '/admin/dashboard',
  teacher: '/teacher/workspace',
  student: '/student/learning-center'
}

function switchRole(role) {
  if (role === userStore.role) return
  
  // 更新用户角色
  userStore.setRole(role)
  
  // 跳转到对应角色的首页
  router.push(roleHomePathMap[role])
}

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <el-dropdown trigger="click" placement="bottom-end">
    <el-button 
      type="primary" 
      plain 
      circle 
      :icon="SwitchButton"
      size="small"
    />
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item disabled>
          <div class="role-info">
            <el-icon><component :is="roleOptions.find(r => r.value === userStore.role)?.icon" /></el-icon>
            <span>当前：{{ roleOptions.find(r => r.value === userStore.role)?.label }}</span>
          </div>
        </el-dropdown-item>
        <el-dropdown-item divided disabled>
          <span class="switch-title">切换角色</span>
        </el-dropdown-item>
        <el-dropdown-item 
          v-for="role in roleOptions.filter(r => r.value !== userStore.role)"
          :key="role.value"
          @click="switchRole(role.value)"
        >
          <div class="role-item">
            <el-icon><component :is="role.icon" /></el-icon>
            <span>{{ role.label }}</span>
          </div>
        </el-dropdown-item>
        <el-dropdown-item divided @click="logout">
          <div class="logout-item">
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </div>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style scoped>
.role-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6b7280;
  font-size: 14px;
}

.switch-title {
  font-size: 12px;
  color: #9ca3af;
  font-weight: 500;
}

.role-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
}

.logout-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #ef4444;
}

:global(html.dark) .role-info {
  color: #9ca3af;
}

:global(html.dark) .switch-title {
  color: #6b7280;
}

:global(html.dark) .role-item {
  color: #d1d5db;
}

:global(html.dark) .logout-item {
  color: #f87171;
}
</style>