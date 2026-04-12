<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Lock, WarningFilled } from '@element-plus/icons-vue'
import PageShell from '../../components/PageShell.vue'
import PersonalInfo from './PersonalInfo.vue'
import { useUserStore } from '../../stores/user'
import { updateStudentPassword } from '../../api/student/profile'

const passwordFormRef = ref()
const activeTab = ref('profile')
const submitting = ref(false)
const userStore = useUserStore()
userStore.restoreSession()

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const securityPolicy = [
  '密码长度不少于 8 位',
  '必须包含大小写字母与数字',
  '建议每 90 天更新一次密码',
  '不要与历史 3 次密码重复'
]

const currentSession = computed(() => ({
  device: typeof navigator === 'undefined' ? '当前设备' : navigator.userAgent.split(') ')[0]?.replace('Mozilla/5.0 (', '') || '当前设备',
  identity: userStore.userInfo?.realName || userStore.userInfo?.username || '当前登录用户'
}))

function validateConfirmPassword(rule, value, callback) {
  if (!value) {
    callback(new Error('请再次输入新密码'))
    return
  }
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
    return
  }
  callback()
}

const rules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    {
      pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[A-Za-z\d@$!%*?&]{8,20}$/,
      message: '密码需为 8-20 位，且包含大小写字母和数字',
      trigger: 'blur'
    }
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
}

const passwordStrength = computed(() => {
  const value = passwordForm.newPassword
  if (!value) return { label: '未输入', score: 0, type: 'weak' }
  let score = 0
  if (value.length >= 8) score += 1
  if (/[a-z]/.test(value) && /[A-Z]/.test(value)) score += 1
  if (/\d/.test(value)) score += 1
  if (/[@$!%*?&]/.test(value)) score += 1
  if (score <= 1) return { label: '弱', score: 25, type: 'weak' }
  if (score <= 3) return { label: '中', score: 65, type: 'medium' }
  return { label: '强', score: 100, type: 'strong' }
})

function resetPasswordForm() {
  passwordFormRef.value?.resetFields()
}

function submitPasswordForm() {
  passwordFormRef.value?.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await updateStudentPassword({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      ElMessage.success('密码修改成功，请使用新密码重新登录')
      resetPasswordForm()
    } catch (error) {
      ElMessage.error(error.message || '密码修改失败')
    } finally {
      submitting.value = false
    }
  })
}
</script>

<template>
  <PageShell title="修改密码" subtitle="个人信息 / 修改密码">
    <el-tabs v-model="activeTab" class="profile-tabs">
      <el-tab-pane label="个人信息" name="profile">
        <PersonalInfo embedded />
      </el-tab-pane>
      <el-tab-pane label="修改密码" name="password">
        <div class="password-layout">
          <section class="password-panel">
            <header class="password-panel__head">
              <h3><el-icon><Lock /></el-icon> 修改登录密码</h3>
              <span>请妥善保管账号信息</span>
            </header>
            <el-form ref="passwordFormRef" :model="passwordForm" :rules="rules" label-width="96px" class="password-form">
              <el-form-item label="旧密码" prop="oldPassword">
                <el-input v-model="passwordForm.oldPassword" type="password" show-password clearable placeholder="请输入旧密码" />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="passwordForm.newPassword" type="password" show-password clearable placeholder="请输入新密码" />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="passwordForm.confirmPassword" type="password" show-password clearable placeholder="请再次输入新密码" />
              </el-form-item>
              <el-form-item label="强度评估">
                <div class="strength">
                  <el-progress :percentage="passwordStrength.score" :show-text="false" :stroke-width="8" :status="passwordStrength.type === 'weak' ? 'exception' : passwordStrength.type === 'medium' ? 'warning' : 'success'" />
                  <span :class="`strength__text strength__text--${passwordStrength.type}`">{{ passwordStrength.label }}</span>
                </div>
              </el-form-item>
              <el-form-item>
                <div class="action-row">
                  <el-button @click="resetPasswordForm">重置</el-button>
                  <el-button type="primary" :loading="submitting" @click="submitPasswordForm">提交修改</el-button>
                </div>
              </el-form-item>
            </el-form>
          </section>

          <section class="security-panel">
            <article class="tip-card">
              <h4><el-icon><WarningFilled /></el-icon> 密码策略</h4>
              <ul>
                <li v-for="item in securityPolicy" :key="item">{{ item }}</li>
              </ul>
            </article>
            <article class="device-card">
              <h4>当前登录会话</h4>
              <div class="device-list">
                <div class="device-item">
                  <p class="device-item__title">{{ currentSession.device }}</p>
                  <p class="device-item__meta">{{ currentSession.identity }}</p>
                </div>
              </div>
            </article>
          </section>
        </div>
      </el-tab-pane>
    </el-tabs>
  </PageShell>
</template>

<style scoped>
.password-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.65fr) minmax(280px, 1fr);
  gap: 16px;
}

.password-panel,
.tip-card,
.device-card {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  background: #ffffff;
}

.password-panel {
  padding: 18px;
}

.password-panel__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.password-panel__head h3 {
  margin: 0;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #111827;
  font-size: 16px;
  line-height: 24px;
  font-weight: 700;
}

.password-panel__head span {
  color: #6b7280;
  font-size: 12px;
}

.password-form {
  max-width: 560px;
}

.strength {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
}

.strength__text {
  font-size: 12px;
  line-height: 18px;
  font-weight: 600;
}

.strength__text--weak {
  color: #dc2626;
}

.strength__text--medium {
  color: #d97706;
}

.strength__text--strong {
  color: #16a34a;
}

.action-row {
  display: flex;
  gap: 10px;
}

.security-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tip-card,
.device-card {
  padding: 16px;
}

.tip-card h4,
.device-card h4 {
  margin: 0 0 10px;
  color: #111827;
  font-size: 15px;
  line-height: 22px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.tip-card ul {
  margin: 0;
  padding-left: 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #374151;
  font-size: 13px;
  line-height: 20px;
}

.device-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.device-item {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px 12px;
  background: #f9fafb;
}

.device-item__title,
.device-item__meta {
  margin: 0;
}

.device-item__title {
  color: #111827;
  font-size: 13px;
  line-height: 20px;
  font-weight: 600;
}

.device-item__meta {
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
  line-height: 18px;
}

@media (max-width: 980px) {
  .password-layout {
    grid-template-columns: 1fr;
  }
}

:global(html.dark) .password-panel,
:global(html.dark) .tip-card,
:global(html.dark) .device-card,
:global(html.dark) .device-item {
  background: #1a2028;
  border-color: #2b3442;
}

:global(html.dark) .device-item {
  background: #141920;
}

:global(html.dark) .password-panel__head h3,
:global(html.dark) .tip-card h4,
:global(html.dark) .device-card h4,
:global(html.dark) .device-item__title {
  color: #e5eaf3;
}

:global(html.dark) .password-panel__head span,
:global(html.dark) .tip-card ul,
:global(html.dark) .device-item__meta {
  color: #9ba7ba;
}
</style>

