import { createApp } from 'vue'
import './styles.css'
import DeliverabilityChart from './components/DeliverabilityChart.vue'

// Vue "islands": Thymeleaf renders <div data-vue="Name" data-*>; we mount the component there,
// passing data-* attributes as props.
const registry = { DeliverabilityChart }

document.querySelectorAll('[data-vue]').forEach((el) => {
  const component = registry[el.dataset.vue]
  if (!component) return console.warn(`Unknown Vue island: ${el.dataset.vue}`)
  const { vue, ...props } = el.dataset
  createApp(component, props).mount(el)
})
