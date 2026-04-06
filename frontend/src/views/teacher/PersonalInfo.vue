<script setup>
import { computed, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import PageShell from '../../components/PageShell.vue'

const props = defineProps({
  embedded: { type: Boolean, default: false }
})

const shellProps = computed(() =>
  props.embedded
    ? {}
    : {
        title: '个人信息',
        subtitle: '个人信息 / 修改密码'
      }
)

// 个人信息 Mock 数据（切换真实后端时，请替换为 src/api/teacher/profile.js 的接口返回）
const initialProfile = {
  teacherNo: 'TCH-2024-018',
  name: '王雅静',
  gender: '女',
  phone: '13800138000',
  email: 'wang.yajing@edusmart.com',
  campus: '总部校区',
  department: '数学教研组',
  title: '高级讲师',
  education: '硕士研究生',
  joinDate: '2022-08-15',
  intro:
    '负责高一至高二数学课程，擅长函数与解析几何专项训练。持续推进分层教学与错题闭环管理，提升班级整体达标率。',
  created_at: '2026-04-06 08:00:00',
  updated_at: '2026-04-06 08:00:00'
}

const profileForm = reactive({ ...initialProfile })

const teachingTags = ['高一数学', '高二数学', '竞赛培优', '教研组长']

const certifications = [
  {
    id: 1,
    name: '高级中学教师资格证（数学）',
    issueDate: '2021-06-10',
    created_at: '2026-04-06 08:00:00',
    updated_at: '2026-04-06 08:00:00'
  },
  {
    id: 2,
    name: '信息化教学能力认证',
    issueDate: '2024-09-18',
    created_at: '2026-04-06 08:00:00',
    updated_at: '2026-04-06 08:00:00'
  }
]

function handleReset() {
  Object.assign(profileForm, initialProfile)
  ElMessage.success('已恢复为初始资料')
}

function handleSave() {
  profileForm.updated_at = '2026-04-06 08:30:00'
  ElMessage.success('个人信息保存成功（Mock）')
}
</script>

<template>
  <component :is="props.embedded ? 'div' : PageShell" v-bind="shellProps">
    <div class="personal-info">
    <section class="profile-overview">
      <div class="avatar-box">{{ profileForm.name.slice(0, 1) }}</div>
      <p class="profile-name">{{ profileForm.name }}</p>
      <p class="profile-meta">{{ profileForm.department }} · {{ profileForm.title }}</p>
      <div class="tag-group">
        <el-tag v-for="tag in teachingTags" :key="tag" effect="plain" round>{{ tag }}</el-tag>
      </div>
      <div class="meta-list">
        <div class="meta-item">
          <span>工号</span>
          <strong>{{ profileForm.teacherNo }}</strong>
        </div>
        <div class="meta-item">
          <span>入职日期</span>
          <strong>{{ profileForm.joinDate }}</strong>
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
      <h3 class="section-title">基础信息</h3>
      <el-form :model="profileForm" label-width="92px" class="form-grid">
        <el-form-item label="教师工号">
          <el-input v-model="profileForm.teacherNo" disabled />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="profileForm.name" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="profileForm.gender" style="width: 100%">
            <el-option label="女" value="女" />
            <el-option label="男" value="男" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="profileForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="profileForm.email" />
        </el-form-item>
        <el-form-item label="所属校区">
          <el-input v-model="profileForm.campus" />
        </el-form-item>
        <el-form-item label="所属部门">
          <el-input v-model="profileForm.department" />
        </el-form-item>
        <el-form-item label="职称">
          <el-input v-model="profileForm.title" />
        </el-form-item>
        <el-form-item label="学历">
          <el-input v-model="profileForm.education" />
        </el-form-item>
        <el-form-item label="入职日期">
          <el-date-picker v-model="profileForm.joinDate" type="date" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="个人简介" class="form-grid__full">
          <el-input v-model="profileForm.intro" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>

      <h3 class="section-title">资质证书</h3>
      <div class="certificate-list">
        <div v-for="item in certifications" :key="item.id" class="certificate-item">
          <p class="certificate-name">{{ item.name }}</p>
          <p class="certificate-meta">发证日期：{{ item.issueDate }}</p>
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
  background: linear-gradient(135deg, #1d4ed8 0%, #2563eb 100%);
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

.section-title {
  margin: 0 0 14px;
  color: #111827;
  font-size: 15px;
  line-height: 22px;
  font-weight: 700;
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

.certificate-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.certificate-item {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px 12px;
  background: #f9fafb;
}

.certificate-name,
.certificate-meta {
  margin: 0;
}

.certificate-name {
  color: #111827;
  font-size: 13px;
  line-height: 20px;
  font-weight: 600;
}

.certificate-meta {
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
  line-height: 18px;
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
  .form-grid {
    grid-template-columns: 1fr;
  }
}

:global(html.dark) .profile-overview,
:global(html.dark) .profile-form,
:global(html.dark) .certificate-item {
  background: #1a2028;
  border-color: #2b3442;
}

:global(html.dark) .certificate-item {
  background: #141920;
}

:global(html.dark) .profile-name,
:global(html.dark) .meta-item strong,
:global(html.dark) .section-title,
:global(html.dark) .certificate-name {
  color: #e5eaf3;
}

:global(html.dark) .profile-meta,
:global(html.dark) .meta-item,
:global(html.dark) .certificate-meta {
  color: #9ba7ba;
}

:global(html.dark) .meta-list {
  border-top-color: #334155;
}
</style>
