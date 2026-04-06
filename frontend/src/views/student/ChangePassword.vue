<script setup>
import { ref } from 'vue'
import PageShell from '../../components/PageShell.vue'

const form = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
</script>

<template>
  <PageShell title="修改密码" subtitle="个人信息 / 修改密码">
    <el-form :model="form" label-width="96px" style="max-width: 420px">
      <el-form-item label="旧密码">
        <el-input v-model="form.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="form.newPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input v-model="form.confirmPassword" type="password" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary">提交</el-button>
      </el-form-item>
    </el-form>
  </PageShell>
</template>

