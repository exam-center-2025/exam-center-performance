<template>
  <div class="bg-white border border-gray-200 p-4">
    <h3 class="text-lg font-semibold text-gray-900 mb-4">실시간 메트릭</h3>
    
    <!-- Current Status Grid -->
    <div class="grid grid-cols-2 gap-2 mb-4">
      <div 
        v-for="metric in metrics" 
        :key="metric.key"
        class="metric-card"
        :class="{ 'updated': metric.updated }"
      >
        <div class="metric-value" :class="metric.valueClass">
          {{ formatMetricValue(metric.value) }}
          <span class="metric-unit">{{ metric.unit }}</span>
        </div>
        <div class="metric-label">{{ metric.label }}</div>
        <div class="metric-change" :class="metric.changeClass" v-if="metric.change">
          <i :class="metric.changeIcon" class="text-xs"></i>
          {{ Math.abs(metric.change).toFixed(1) }}{{ metric.unit }}
        </div>
      </div>
    </div>
    
    <!-- Quick Stats -->
    <div class="border-t pt-3">
      <h4 class="font-medium text-gray-700 mb-3">요약 통계</h4>
      <div class="grid grid-cols-2 gap-2 text-sm">
        <div class="stat-item">
          <span class="stat-label">평균 TPS</span>
          <span class="stat-value">{{ formatValue(avgTps, 1) }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">최대 TPS</span>
          <span class="stat-value">{{ formatValue(maxTps, 1) }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">평균 응답시간</span>
          <span class="stat-value">{{ formatValue(avgResponseTime, 0) }}ms</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">총 에러 수</span>
          <span class="stat-value">{{ totalErrors }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { usePerformanceStore } from '@shared/stores/performance'

const performanceStore = usePerformanceStore()
const previousMetrics = ref({})

// AIDEV-NOTE: Real-time metrics with change detection and formatting
const currentMetrics = computed(() => performanceStore.currentMetrics)
const metricsHistory = computed(() => performanceStore.metricsHistory)

const metrics = computed(() => {
  const current = currentMetrics.value
  const previous = previousMetrics.value

  return [
    {
      key: 'tps',
      label: 'TPS',
      value: current.tps || 0,
      unit: '',
      valueClass: getTpsValueClass(current.tps),
      change: getChange(current.tps, previous.tps),
      changeClass: getChangeClass(current.tps, previous.tps),
      changeIcon: getChangeIcon(current.tps, previous.tps),
      updated: hasUpdated('tps', current.tps, previous.tps)
    },
    {
      key: 'responseTime',
      label: '응답시간',
      value: current.avgResponseTime || 0,
      unit: 'ms',
      valueClass: getResponseTimeValueClass(current.avgResponseTime),
      change: getChange(current.avgResponseTime, previous.avgResponseTime),
      changeClass: getChangeClass(current.avgResponseTime, previous.avgResponseTime, true), // reverse for response time
      changeIcon: getChangeIcon(current.avgResponseTime, previous.avgResponseTime, true),
      updated: hasUpdated('responseTime', current.avgResponseTime, previous.avgResponseTime)
    },
    {
      key: 'activeUsers',
      label: '활성사용자',
      value: current.activeUsers || 0,
      unit: '명',
      valueClass: getUsersValueClass(current.activeUsers),
      change: getChange(current.activeUsers, previous.activeUsers),
      changeClass: getChangeClass(current.activeUsers, previous.activeUsers),
      changeIcon: getChangeIcon(current.activeUsers, previous.activeUsers),
      updated: hasUpdated('activeUsers', current.activeUsers, previous.activeUsers)
    },
    {
      key: 'errorRate',
      label: '에러율',
      value: current.errorRate || 0,
      unit: '%',
      valueClass: getErrorRateValueClass(current.errorRate),
      change: getChange(current.errorRate, previous.errorRate),
      changeClass: getChangeClass(current.errorRate, previous.errorRate, true), // reverse for error rate
      changeIcon: getChangeIcon(current.errorRate, previous.errorRate, true),
      updated: hasUpdated('errorRate', current.errorRate, previous.errorRate)
    }
  ]
})

// Computed statistics
const avgTps = computed(() => {
  if (metricsHistory.value.length === 0) return 0
  const sum = metricsHistory.value.reduce((acc, m) => acc + (m.tps || 0), 0)
  return sum / metricsHistory.value.length
})

const maxTps = computed(() => {
  if (metricsHistory.value.length === 0) return 0
  return Math.max(...metricsHistory.value.map(m => m.tps || 0))
})

const avgResponseTime = computed(() => {
  if (metricsHistory.value.length === 0) return 0
  const sum = metricsHistory.value.reduce((acc, m) => acc + (m.avgResponseTime || 0), 0)
  return sum / metricsHistory.value.length
})

const totalErrors = computed(() => {
  // This would need to be calculated based on error count data
  // For now, use error rate * active users as approximation
  return Math.round((currentMetrics.value.errorRate || 0) * (currentMetrics.value.activeUsers || 0) / 100)
})

// Helper functions
function formatMetricValue(value) {
  if (value === null || value === undefined) return 'N/A'
  
  if (typeof value === 'number') {
    if (value >= 1000) {
      return (value / 1000).toFixed(1) + 'K'
    }
    return value.toFixed(value < 10 ? 1 : 0)
  }
  
  return value.toString()
}

function formatValue(value, decimals = 1) {
  if (value === null || value === undefined) return 'N/A'
  return typeof value === 'number' ? value.toFixed(decimals) : value.toString()
}

function getChange(current, previous) {
  if (!previous || !current) return null
  return current - previous
}

function getChangeClass(current, previous, reverse = false) {
  const change = getChange(current, previous)
  if (!change) return ''
  
  const isPositive = change > 0
  const isGood = reverse ? !isPositive : isPositive
  
  return isGood ? 'text-green-600' : 'text-red-600'
}

function getChangeIcon(current, previous, reverse = false) {
  const change = getChange(current, previous)
  if (!change) return ''
  
  const isPositive = change > 0
  return isPositive ? 'fas fa-arrow-up' : 'fas fa-arrow-down'
}

function hasUpdated(key, current, previous) {
  return current !== previous && previous !== undefined
}

function getTpsValueClass(value) {
  if (value > 50) return 'text-green-600'
  if (value > 20) return 'text-yellow-600'
  return 'text-red-600'
}

function getResponseTimeValueClass(value) {
  if (value < 100) return 'text-green-600'
  if (value < 500) return 'text-yellow-600'
  return 'text-red-600'
}

function getUsersValueClass(value) {
  return 'text-blue-600'
}

function getErrorRateValueClass(value) {
  if (value < 1) return 'text-green-600'
  if (value < 5) return 'text-yellow-600'
  return 'text-red-600'
}

// Watch for metrics changes and store previous values
watch(currentMetrics, (newMetrics) => {
  // Store previous metrics for change calculation
  setTimeout(() => {
    previousMetrics.value = { ...newMetrics }
  }, 100)
}, { deep: true })
</script>

<style scoped>
.metric-card {
  @apply bg-gray-50 border border-gray-200 p-2 text-center relative transition-all duration-300;
}

.metric-card.updated {
  @apply bg-blue-50 border border-blue-300;
  animation: pulse-blue 0.5s ease-out;
}

.metric-value {
  @apply text-2xl font-bold mb-1 transition-colors duration-300;
}

.metric-unit {
  @apply text-sm font-normal ml-1 opacity-75;
}

.metric-label {
  @apply text-sm text-gray-600 font-medium;
}

.metric-change {
  @apply absolute top-2 right-2 text-xs font-medium;
}

.stat-item {
  @apply flex justify-between items-center py-1;
}

.stat-label {
  @apply text-gray-600;
}

.stat-value {
  @apply font-semibold text-gray-900;
}

@keyframes pulse-blue {
  0% {
    background-color: theme('colors.blue.100');
    border-color: theme('colors.blue.300');
  }
  100% {
    background-color: theme('colors.blue.50');
    border-color: theme('colors.blue.200');
  }
}
</style>