<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { login } from '../../api/auth'
import { Avatar, Lock, School, User } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const loginForm = ref({
  username: '',
  password: '',
  role: 'admin'
})

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

async function handleLogin() {
  if (!loginForm.value.username.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (!loginForm.value.password) {
    ElMessage.warning('请输入密码')
    return
  }

  loading.value = true
  try {
    const loginData = await login({
      username: loginForm.value.username.trim(),
      password: loginForm.value.password,
      role: loginForm.value.role
    })

    userStore.setLoginInfo(loginData)
    ElMessage.success('登录成功')
    router.push(roleHomePathMap[userStore.role] || '/login')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

function quickLogin(role) {
  loginForm.value.role = role
  ElMessage.info('已切换登录角色，请输入真实账号和密码')
}
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <div class="logo">
          <el-icon size="48" color="#2563eb"><School /></el-icon>
        </div>
        <h1 class="title">教培智管系统</h1>
        <p class="subtitle">EduSmart Manager</p>
      </div>

      <el-form :model="loginForm" class="login-form" @submit.prevent="handleLogin">
        <el-form-item>
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item>
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-radio-group v-model="loginForm.role" class="role-radio-group">
            <el-radio-button
              v-for="role in roleOptions"
              :key="role.value"
              :label="role.value"
              :value="role.value"
            >
              <el-icon><component :is="role.icon" /></el-icon>
              <span>{{ role.label }}</span>
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-button"
            native-type="submit"
            :loading="loading"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="quick-login">
        <p class="quick-login-title">快速选择角色</p>
        <div class="quick-login-buttons">
          <el-button
            v-for="role in roleOptions"
            :key="role.value"
            :type="role.value === 'admin' ? 'primary' : 'default'"
            @click="quickLogin(role.value)"
          >
            <el-icon><component :is="role.icon" /></el-icon>
            {{ role.label }}
          </el-button>
        </div>
      </div>
    </div>

    <div class="login-footer">
      <p>© 2026 教培智管系统 - 严格遵守项目规则开发</p>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
  text-align: center;
}

.login-header {
  margin-bottom: 32px;
}

.logo {
  margin-bottom: 16px;
}

.title {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 16px;
  color: #6b7280;
  margin: 0;
}

.login-form {
  margin-bottom: 24px;
}

.role-radio-group {
  width: 100%;
}

.role-radio-group :deep(.el-radio-button) {
  flex: 1;
}

.role-radio-group :deep(.el-radio-button__inner) {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}

.quick-login {
  border-top: 1px solid #e5e7eb;
  padding-top: 24px;
}

.quick-login-title {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 16px 0;
}

.quick-login-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.quick-login-buttons .el-button {
  display: flex;
  align-items: center;
  gap: 6px;
}

.login-footer {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  text-align: center;
}

.login-footer p {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
}

/* 深色模式适配 */
:global(html.dark) .login-container {
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
}

:global(html.dark) .login-card {
  background: #1f2937;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

:global(html.dark) .title {
  color: #f9fafb;
}

:global(html.dark) .subtitle {
  color: #9ca3af;
}

:global(html.dark) .quick-login-title {
  color: #9ca3af;
}

:global(html.dark) .quick-login {
  border-top-color: #374151;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-card {
    padding: 32px 24px;
    margin: 0 16px;
  }
  
  .title {
    font-size: 24px;
  }
  
  .quick-login-buttons {
    flex-direction: column;
  }
  
  .quick-login-buttons .el-button {
    width: 100%;
  }
}
</style>
