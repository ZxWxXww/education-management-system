<script setup>
import RoleSwitch from '../common/RoleSwitch.vue'

const props = defineProps({
  collapsed: { type: Boolean, default: false }
})

const emit = defineEmits(['toggle-sidebar'])
</script>

<template>
  <div class="admin-header">
    <el-button text @click="emit('toggle-sidebar')">
      {{ props.collapsed ? '展开菜单' : '收起菜单' }}
    </el-button>
    <div class="admin-header__title">教培智管系统 · 管理员端</div>
    <div class="admin-header__actions">
      <RoleSwitch />
    </div>
  </div>
</template>

<style scoped>
.admin-header {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.admin-header__title {
  font-weight: 700;
  flex: 1;
}

.admin-header__actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>