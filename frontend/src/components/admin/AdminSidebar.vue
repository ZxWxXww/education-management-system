<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { getMenuByRole } from '../../menus'

const props = defineProps({
  activePath: { type: String, required: true },
  collapsed: { type: Boolean, default: false }
})

const router = useRouter()

const adminMenu = computed(() => getMenuByRole('admin'))

const activeRootMenuPath = computed(() => {
  const target = adminMenu.value.find((item) => {
    if (!item.children || item.children.length === 0) return false
    return props.activePath === item.path || props.activePath.startsWith(`${item.path}/`)
  })
  return target ? target.path : ''
})

const defaultOpeneds = computed(() => (activeRootMenuPath.value ? [activeRootMenuPath.value] : []))
const menuRenderKey = computed(() => `${activeRootMenuPath.value}-${props.collapsed ? '1' : '0'}`)

function onSelect(index) {
  router.push(index)
}

function goRoot(path) {
  router.push(path)
}
</script>

<template>
  <div class="admin-sidebar">
    <div class="admin-sidebar__brand">管理员端</div>
    <el-scrollbar class="admin-sidebar__scroll">
      <el-menu
        :key="menuRenderKey"
        :default-active="activePath"
        :default-openeds="defaultOpeneds"
        :collapse="collapsed"
        :unique-opened="true"
        :router="false"
        class="admin-sidebar__menu"
        @select="onSelect"
      >
        <template v-for="item in adminMenu" :key="item.path">
          <el-sub-menu v-if="item.children && item.children.length" :index="item.path">
            <template #title>
              <span class="admin-sidebar__menu-title" @click.stop="goRoot(item.path)">{{ item.title }}</span>
            </template>
            <el-menu-item v-for="sub in item.children" :key="sub.path" :index="sub.path">
              {{ sub.title }}
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="item.path">
            <span class="admin-sidebar__menu-title">{{ item.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<style scoped>
.admin-sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--el-border-color);
  background-color: var(--el-bg-color);
}

.admin-sidebar__brand {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  font-weight: 700;
  white-space: nowrap;
}

.admin-sidebar__scroll {
  flex: 1;
}

.admin-sidebar__menu {
  border-right: none;
}

.admin-sidebar__menu-title {
  font-weight: 700;
}
</style>
