<script setup>
import { useRouter } from 'vue-router'
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { switchRole as switchRoleApi } from '../../api/auth'
import { Avatar, SwitchButton, User, School } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const switching = ref(false)

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

const switchableRoles = computed(() =>
  roleOptions.filter(
    (role) => userStore.availableRoleValues.includes(role.value) && role.value !== userStore.role
  )
)

async function switchRole(role) {
  if (role === userStore.role) return
  
  if (!userStore.availableRoleValues.includes(role)) {
    ElMessage.warning('当前账号未分配该角色，不能切换')
    return
  }

  switching.value = true
  try {
    const loginData = await switchRoleApi({ role })
    userStore.setLoginInfo(loginData)
    router.push(roleHomePathMap[userStore.role] || '/login')
  } catch (error) {
    ElMessage.error(error.message || '角色切换失败')
  } finally {
    switching.value = false
  }
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
      :loading="switching"
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
          v-for="role in switchableRoles"
          :key="role.value"
          @click="switchRole(role.value)"
        >
          <div class="role-item">
            <el-icon><component :is="role.icon" /></el-icon>
            <span>{{ role.label }}</span>
          </div>
        </el-dropdown-item>
        <el-dropdown-item v-if="!switchableRoles.length" disabled>
          <span class="switch-empty">当前账号暂无其他可切换角色</span>
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

.switch-empty {
  font-size: 12px;
  color: #9ca3af;
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

:global(html.dark) .switch-empty {
  color: #6b7280;
}

:global(html.dark) .role-item {
  color: #d1d5db;
}

:global(html.dark) .logout-item {
  color: #f87171;
}
</style>
