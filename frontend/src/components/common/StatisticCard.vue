<script setup>
const props = defineProps({
  title: { type: String, required: true },
  value: { type: String, required: true },
  desc: { type: String, default: '' },
  icon: { type: [Object, Function], default: null },
  tone: { type: String, default: 'blue' }
})
</script>

<template>
  <el-card shadow="hover" class="stat-card" :class="`tone-${props.tone}`">
    <div class="stat-card__head">
      <span class="stat-card__title">{{ props.title }}</span>
      <el-icon v-if="props.icon" class="stat-card__icon">
        <component :is="props.icon" />
      </el-icon>
    </div>
    <div class="stat-card__value">{{ props.value }}</div>
    <div v-if="props.desc" class="stat-card__desc">{{ props.desc }}</div>
  </el-card>
</template>

<style scoped>
.stat-card {
  border-radius: 12px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.stat-card__title {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.stat-card__icon {
  font-size: 20px;
}

.stat-card__value {
  margin-top: 10px;
  font-size: 28px;
  line-height: 1.2;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.stat-card__desc {
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.tone-blue .stat-card__icon {
  color: #3b82f6;
}

.tone-green .stat-card__icon {
  color: #22c55e;
}

.tone-purple .stat-card__icon {
  color: #8b5cf6;
}

.tone-orange .stat-card__icon {
  color: #f59e0b;
}

@media (max-width: 768px) {
  .stat-card__value {
    font-size: 24px;
  }
}
</style>
