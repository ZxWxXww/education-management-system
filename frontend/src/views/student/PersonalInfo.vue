<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { EditPen } from '@element-plus/icons-vue'
import PageShell from '../../components/PageShell.vue'

const props = defineProps({
  embedded: { type: Boolean, default: false }
})

// 学生个人信息 Mock 数据（切换真实后端时，请替换为 src/api/student/profile.js 的接口返回）
const initialProfile = {
  studentNo: 'STU-2026-0931',
  name: '李思源',
  gender: '男',
  grade: '高二',
  className: '高二（3）班',
  phone: '13900001122',
  email: 'li.siyuan@student.edusmart.com',
  guardianName: '李建国',
  guardianPhone: '13812345678',
  address: '浙江省杭州市西湖区文二路 188 号',
  intro: '学习目标明确，擅长理科综合，近期重点提升英语写作与物理实验题稳定性。',
  created_at: '2026-04-06 08:00:00',
  updated_at: '2026-04-06 08:00:00'
}

const profileForm = reactive({ ...initialProfile })

const subjectTags = ['数学拔高', '物理竞赛', '英语写作', '周测稳定']

const learningStats = [
  { label: '本学期出勤率', value: '98.6%' },
  { label: '作业提交率', value: '96.2%' },
  { label: '最近月考排名', value: '年级 37/426' },
  { label: '班级综合评级', value: 'A' }
]

function handleReset() {
  Object.assign(profileForm, initialProfile)
  ElMessage.success('已恢复为初始资料')
}

function handleSave() {
  profileForm.updated_at = '2026-04-06 09:10:00'
  ElMessage.success('个人信息保存成功（Mock）')
}

const shellProps = {
  title: '个人信息',
  subtitle: '个人信息 / 修改密码'
}
</script>

<template>
  <component :is="props.embedded ? 'div' : PageShell" v-bind="props.embedded ? {} : shellProps">
    <div class="personal-info">
      <section class="profile-overview">
        <div class="avatar-box">{{ profileForm.name.slice(0, 1) }}</div>
        <p class="profile-name">{{ profileForm.name }}</p>
        <p class="profile-meta">{{ profileForm.grade }} · {{ profileForm.className }}</p>
        <div class="tag-group">
          <el-tag v-for="tag in subjectTags" :key="tag" effect="plain" round>{{ tag }}</el-tag>
        </div>
        <div class="meta-list">
          <div class="meta-item">
            <span>学号</span>
            <strong>{{ profileForm.studentNo }}</strong>
          </div>
          <div class="meta-item">
            <span>监护人</span>
            <strong>{{ profileForm.guardianName }}</strong>
          </div>
          <div class="meta-item">
            <span>创建时间</span>
            <strong>{{ profileForm.created_at }}</strong>
          </div>
          <div class="meta-item">
            <span>更新时间</span>
            <strong>{{ profileForm.updated_at }}</strong>
          </div>
        </div>
      </section>

      <section class="profile-form">
        <div class="section-head">
          <h3 class="section-title">基础信息</h3>
          <el-icon><EditPen /></el-icon>
        </div>
        <el-form :model="profileForm" label-width="92px" class="form-grid">
          <el-form-item label="学生学号">
            <el-input v-model="profileForm.studentNo" disabled />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="profileForm.name" />
          </el-form-item>
          <el-form-item label="性别">
            <el-select v-model="profileForm.gender" style="width: 100%">
              <el-option label="男" value="男" />
              <el-option label="女" value="女" />
            </el-select>
          </el-form-item>
          <el-form-item label="年级">
            <el-select v-model="profileForm.grade" style="width: 100%">
              <el-option label="高一" value="高一" />
              <el-option label="高二" value="高二" />
              <el-option label="高三" value="高三" />
            </el-select>
          </el-form-item>
          <el-form-item label="班级">
            <el-input v-model="profileForm.className" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="profileForm.phone" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="profileForm.email" />
          </el-form-item>
          <el-form-item label="监护人">
            <el-input v-model="profileForm.guardianName" />
          </el-form-item>
          <el-form-item label="监护人电话">
            <el-input v-model="profileForm.guardianPhone" />
          </el-form-item>
          <el-form-item label="家庭住址" class="form-grid__full">
            <el-input v-model="profileForm.address" />
          </el-form-item>
          <el-form-item label="学习备注" class="form-grid__full">
            <el-input v-model="profileForm.intro" type="textarea" :rows="4" />
          </el-form-item>
        </el-form>

        <h3 class="section-title section-title--spaced">学习概览</h3>
        <div class="stat-grid">
          <div v-for="item in learningStats" :key="item.label" class="stat-item">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
          </div>
        </div>

        <div class="action-row">
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleSave">保存信息</el-button>
        </div>
      </section>
    </div>
  </component>
</template>

<style scoped>
.personal-info {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 16px;
}

.profile-overview,
.profile-form {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  background: #ffffff;
  padding: 20px;
}

.profile-overview {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.avatar-box {
  width: 74px;
  height: 74px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  color: #ffffff;
  display: inline-flex;
  justify-content: center;
  align-items: center;
  font-size: 28px;
  font-weight: 700;
}

.profile-name {
  margin: 2px 0 0;
  color: #111827;
  font-size: 20px;
  line-height: 28px;
  font-weight: 700;
}

.profile-meta {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
  line-height: 20px;
}

.tag-group {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
  margin-bottom: 8px;
}

.meta-list {
  width: 100%;
  border-top: 1px dashed #d1d5db;
  padding-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  color: #6b7280;
  font-size: 12px;
}

.meta-item strong {
  color: #111827;
  font-size: 12px;
  font-weight: 600;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.section-head .el-icon {
  color: #6b7280;
}

.section-title {
  margin: 0;
  color: #111827;
  font-size: 15px;
  line-height: 22px;
  font-weight: 700;
}

.section-title--spaced {
  margin-top: 2px;
  margin-bottom: 14px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 16px;
}

.form-grid :deep(.el-form-item) {
  margin-bottom: 14px;
}

.form-grid__full {
  grid-column: 1 / -1;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.stat-item {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px 12px;
  background: #f9fafb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.stat-item span {
  color: #6b7280;
  font-size: 12px;
}

.stat-item strong {
  color: #111827;
  font-size: 13px;
  font-weight: 700;
}

.action-row {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 980px) {
  .personal-info {
    grid-template-columns: 1fr;
  }

  .profile-overview {
    align-items: flex-start;
  }

  .tag-group {
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .form-grid,
  .stat-grid {
    grid-template-columns: 1fr;
  }
}

:global(html.dark) .profile-overview,
:global(html.dark) .profile-form,
:global(html.dark) .stat-item {
  background: #1a2028;
  border-color: #2b3442;
}

:global(html.dark) .stat-item {
  background: #141920;
}

:global(html.dark) .profile-name,
:global(html.dark) .meta-item strong,
:global(html.dark) .section-title,
:global(html.dark) .stat-item strong {
  color: #e5eaf3;
}

:global(html.dark) .profile-meta,
:global(html.dark) .meta-item,
:global(html.dark) .section-head .el-icon,
:global(html.dark) .stat-item span {
  color: #9ba7ba;
}

:global(html.dark) .meta-list {
  border-top-color: #334155;
}
</style>
