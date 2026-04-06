<script setup>
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
  <div class="header-left">
    <el-button text @click="emit('toggle-sidebar')">
      {{ props.collapsed ? '展开菜单' : '收起菜单' }}
    </el-button>
    <div class="header-title">教培智管系统</div>
    <el-dropdown @command="onSelect">
      <el-button size="small">
        {{ roleOptions.find((x) => x.value === props.role)?.label || props.role }}
      </el-button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item v-for="item in roleOptions" :key="item.value" :command="item.value">
            {{ item.label }}
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<style scoped>
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-title {
  font-weight: 700;
}
</style>

