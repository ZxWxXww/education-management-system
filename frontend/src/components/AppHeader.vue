<script setup>
import RoleSwitch from './common/RoleSwitch.vue'

const props = defineProps({
  role: { type: String, required: true },
  collapsed: { type: Boolean, default: false }
})

const emit = defineEmits(['role-change', 'toggle-sidebar'])

const roleOptions = [
  { label: '管理员端', value: 'admin' },
  { label: '教师端', value: 'teacher' },
  { label: '学生端', value: 'student' }
]

function onSelect(command) {
  emit('role-change', command)
}
</script>

<template>
  <div class="header-container">
    <div class="header-left">
      <el-button text @click="emit('toggle-sidebar')">
        {{ props.collapsed ? '展开菜单' : '收起菜单' }}
      </el-button>
      <div class="header-title">教培智管系统 · {{ roleOptions.find((x) => x.value === props.role)?.label }}</div>
    </div>
    <div class="header-right">
      <RoleSwitch />
    </div>
  </div>
</template>

<style scoped>
.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex: 1;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-title {
  font-weight: 700;
}
</style>