<script setup>
import { onMounted, ref } from 'vue'
import Chart from 'chart.js/auto'

const props = defineProps({ stats: { type: String, required: true } })
const canvas = ref(null)

onMounted(() => {
  const rows = JSON.parse(props.stats)
  new Chart(canvas.value, {
    type: 'line',
    data: {
      labels: rows.map((r) => r.day),
      datasets: [
        { label: 'Bounce rate', data: rows.map((r) => (r.sent ? r.bounces / r.sent : 0)) },
        { label: 'Reply rate', data: rows.map((r) => (r.sent ? r.replies / r.sent : 0)) },
        { label: 'Inbox placement', data: rows.map((r) => r.inboxPlacement) }
      ]
    }
  })
})
</script>

<template>
  <canvas ref="canvas" height="120"></canvas>
</template>
