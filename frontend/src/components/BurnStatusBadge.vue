<script setup>
const props = defineProps({ status: { type: String, default: 'HEALTHY' } })
const labels = { HEALTHY: 'Healthy', AT_RISK: 'At risk', BURNED: 'Burned' }
</script>

<template>
  <span class="badge" :class="`badge--${props.status.toLowerCase()}`">{{ labels[props.status] ?? props.status }}</span>
</template>

<style scoped>
.badge { padding: 2px 8px; border-radius: 999px; font-size: 12px; }
.badge--healthy { background: #dcfce7; color: #166534; }
.badge--at_risk { background: #fef3c7; color: #92400e; }
.badge--burned  { background: #fee2e2; color: #991b1b; }
</style>
