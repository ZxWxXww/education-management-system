<script setup>
const props = defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' }
})
</script>

<template>
  <el-card shadow="never">
    <div style="display: flex; flex-direction: column; gap: 6px">
      <div style="font-size: 18px; font-weight: 700">{{ props.title }}</div>
      <div v-if="props.subtitle" style="color: var(--el-text-color-secondary); font-size: 13px">{{ props.subtitle }}</div>
    </div>
    <div style="margin-top: 16px">
      <slot />
    </div>
  </el-card>
</template>

