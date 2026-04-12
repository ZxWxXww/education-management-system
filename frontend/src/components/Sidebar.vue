﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { DocumentChecked, FolderOpened, Operation, UserFilled, WarningFilled } from '@element-plus/icons-vue'
import { getMenuByRole } from '../menus'

const props = defineProps({
  role: { type: String, required: true },
  permissions: { type: Array, default: () => [] },
  activePath: { type: String, required: true },
  collapsed: { type: Boolean, default: false }
})

const router = useRouter()

const iconMap = {
  Operation,
  WarningFilled,
  FolderOpened,
  DocumentChecked,
  UserFilled
}

const menu = computed(() => getMenuByRole(props.role, props.permissions))

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

function resolveIcon(iconName) {
  return iconName ? iconMap[iconName] : null
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
            <el-menu-item v-for="sub in item.children" :key="sub.path" :index="sub.path" :class="sub.menuClass || ''">
              <span class="sub-menu-content">
                <span v-if="resolveIcon(sub.icon)" class="sub-menu-icon-wrap">
                  <el-icon :size="13"><component :is="resolveIcon(sub.icon)" /></el-icon>
                </span>
                <span class="sub-menu-text">{{ sub.title }}</span>
              </span>
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

.sub-menu-content {
  width: 100%;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.sub-menu-icon-wrap {
  width: 20px;
  height: 20px;
  border-radius: 6px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  color: #64748b;
  transition: all 0.2s ease;
}

.sub-menu-text {
  font-weight: 500;
  letter-spacing: 0.1px;
}

.sidebar-menu :deep(.el-menu-item.teacher-attendance-batch-item),
.sidebar-menu :deep(.el-menu-item.teacher-resource-library-item) {
  margin: 4px 8px 4px 12px;
  border-radius: 10px;
  min-height: 38px;
  height: 38px;
  line-height: 38px;
  padding-left: 10px !important;
  transition: all 0.2s ease;
}

.sidebar-menu :deep(.el-menu-item.teacher-attendance-batch-item:hover),
.sidebar-menu :deep(.el-menu-item.teacher-resource-library-item:hover) {
  background: #e6f0ff;
  color: #1d4ed8;
}

.sidebar-menu :deep(.el-menu-item.teacher-attendance-batch-item:hover .sub-menu-icon-wrap),
.sidebar-menu :deep(.el-menu-item.teacher-resource-library-item:hover .sub-menu-icon-wrap) {
  background: #dbeafe;
  color: #1d4ed8;
}

.sidebar-menu :deep(.el-menu-item.teacher-attendance-batch-item.is-active),
.sidebar-menu :deep(.el-menu-item.teacher-resource-library-item.is-active) {
  background: linear-gradient(90deg, #2563eb 0%, #1d4ed8 100%);
  color: #ffffff;
  box-shadow: 0 8px 16px rgba(37, 99, 235, 0.24);
}

.sidebar-menu :deep(.el-menu-item.teacher-attendance-batch-item.is-active .sub-menu-icon-wrap),
.sidebar-menu :deep(.el-menu-item.teacher-resource-library-item.is-active .sub-menu-icon-wrap) {
  background: rgba(255, 255, 255, 0.2);
  color: #ffffff;
}

@media (max-width: 1024px) {
  .sidebar-menu :deep(.el-menu-item.teacher-attendance-batch-item),
  .sidebar-menu :deep(.el-menu-item.teacher-resource-library-item) {
    margin: 3px 6px 3px 8px;
    border-radius: 9px;
    min-height: 36px;
    height: 36px;
    line-height: 36px;
    padding-left: 8px !important;
  }

  .sub-menu-content {
    gap: 6px;
  }

  .sub-menu-text {
    font-size: 13px;
  }
}

:global(html.dark) .sub-menu-icon-wrap {
  background: #243041;
  color: #9ba7ba;
}

:global(html.dark) .sidebar-menu :deep(.el-menu-item.teacher-attendance-batch-item:hover),
:global(html.dark) .sidebar-menu :deep(.el-menu-item.teacher-resource-library-item:hover) {
  background: #1f3a68;
  color: #dbeafe;
}

:global(html.dark) .sidebar-menu :deep(.el-menu-item.teacher-attendance-batch-item.is-active),
:global(html.dark) .sidebar-menu :deep(.el-menu-item.teacher-resource-library-item.is-active) {
  background: linear-gradient(90deg, #1d4ed8 0%, #1e40af 100%);
}
</style>
