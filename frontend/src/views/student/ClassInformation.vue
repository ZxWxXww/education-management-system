<script setup>
import { useRoute } from 'vue-router'
import PageShell from '../../components/PageShell.vue'

const route = useRoute()
</script>

<template>
  <PageShell :title="`班级信息（ID: ${route.params.id}）`" subtitle="课程列表 / 班级信息" />
</template>

