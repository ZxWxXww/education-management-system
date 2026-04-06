<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { getMenuByRole } from '../menus'

const props = defineProps({
  role: { type: String, required: true },
  activePath: { type: String, required: true },
  collapsed: { type: Boolean, default: false }
})

const router = useRouter()

const menu = computed(() => getMenuByRole(props.role))

const activeRootMenuPath = computed(() => {
  const target = menu.value.find((item) => {
    if (!item.children || item.children.length === 0) return false
    return props.activePath === item.path || props.activePath.startsWith(`${item.path}/`)
  })
  return target ? target.path : ''
})

const defaultOpeneds = computed(() => (activeRootMenuPath.value ? [activeRootMenuPath.value] : []))

const menuRenderKey = computed(() => `${props.role}-${activeRootMenuPath.value}-${props.collapsed ? '1' : '0'}`)

function onSelect(index) {
  router.push(index)
}
</script>

<template>
  <div class="sidebar-wrap">
    <div class="sidebar-brand">
      {{ role === 'admin' ? '管理员端' : role === 'teacher' ? '教师端' : '学生端' }}
    </div>
    <el-scrollbar class="sidebar-scroll">
      <el-menu
        :key="menuRenderKey"
        :default-active="activePath"
        :default-openeds="defaultOpeneds"
        :collapse="collapsed"
        :unique-opened="true"
        :router="false"
        @select="onSelect"
        class="sidebar-menu"
      >
        <template v-for="item in menu" :key="item.path">
          <el-sub-menu v-if="item.children && item.children.length" :index="item.path">
            <template #title>
              <span class="menu-title">{{ item.title }}</span>
            </template>
            <el-menu-item v-for="sub in item.children" :key="sub.path" :index="sub.path">
              {{ sub.title }}
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="item.path">
            <span class="menu-title">{{ item.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<style scoped>
.sidebar-wrap {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--el-border-color);
  background-color: var(--el-bg-color);
}

.sidebar-brand {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  font-weight: 700;
  white-space: nowrap;
}

.sidebar-scroll {
  flex: 1;
}

.sidebar-menu {
  border-right: none;
}

.menu-title {
  font-weight: 700;
}
</style>

