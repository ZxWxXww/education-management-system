<script setup>
import { useRoute } from 'vue-router'
import PageShell from '../../components/PageShell.vue'

const route = useRoute()
</script>

<template>
  <PageShell :title="`班级详情（ID: ${route.params.id}）`" subtitle="我的课程 / 班级详情" />
</template>

