<template>
  <div class="charts-section">
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <!-- TPS Chart -->
      <MetricChart
        ref="tpsChart"
        chart-id="tps-chart"
        title="TPS (초당 트랜잭션)"
        :data="tpsData"
        :current-value="currentMetrics.tps"
        unit=""
        color="#3b82f6"
        :decimals="1"
        :y-axis-max="maxTps"
      />

      <!-- Response Time Chart -->
      <MetricChart
        ref="responseTimeChart"
        chart-id="response-time-chart"
        title="응답시간"
        :data="responseTimeData"
        :current-value="currentMetrics.avgResponseTime"
        unit="ms"
        color="#10b981"
        :decimals="0"
        :y-axis-max="maxResponseTime"
      />

      <!-- Active Users Chart -->
      <MetricChart
        ref="activeUsersChart"
        chart-id="active-users-chart"
        title="활성 사용자 수"
        :data="activeUsersData"
        :current-value="currentMetrics.activeUsers"
        unit="명"
        color="#f59e0b"
        :decimals="0"
        :y-axis-max="maxUsers"
      />

      <!-- Error Rate Chart -->
      <MetricChart
        ref="errorRateChart"
        chart-id="error-rate-chart"
        title="에러율"
        :data="errorRateData"
        :current-value="currentMetrics.errorRate"
        unit="%"
        color="#ef4444"
        :decimals="2"
        :y-axis-max="100"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { usePerformanceStore } from '@shared/stores/performance'
import MetricChart from './MetricChart.vue'

const performanceStore = usePerformanceStore()

// Chart refs for direct access
const tpsChart = ref(null)
const responseTimeChart = ref(null)
const activeUsersChart = ref(null)
const errorRateChart = ref(null)

// AIDEV-NOTE: Real-time metrics computation from store
const currentMetrics = computed(() => performanceStore.currentMetrics)
const metricsHistory = computed(() => performanceStore.metricsHistory)

// Chart data computed properties
const tpsData = computed(() => 
  metricsHistory.value.map(m => ({
    value: m.tps || 0,
    timestamp: m.timestamp
  }))
)

const responseTimeData = computed(() => 
  metricsHistory.value.map(m => ({
    value: m.avgResponseTime || 0,
    timestamp: m.timestamp
  }))
)

const activeUsersData = computed(() => 
  metricsHistory.value.map(m => ({
    value: m.activeUsers || 0,
    timestamp: m.timestamp
  }))
)

const errorRateData = computed(() => 
  metricsHistory.value.map(m => ({
    value: m.errorRate || 0,
    timestamp: m.timestamp
  }))
)

// Dynamic Y-axis maximums based on data
const maxTps = computed(() => {
  const maxValue = Math.max(...tpsData.value.map(d => d.value), currentMetrics.value.tps)
  return maxValue > 0 ? Math.ceil(maxValue * 1.2) : 100
})

const maxResponseTime = computed(() => {
  const maxValue = Math.max(...responseTimeData.value.map(d => d.value), currentMetrics.value.avgResponseTime)
  return maxValue > 0 ? Math.ceil(maxValue * 1.2) : 1000
})

const maxUsers = computed(() => {
  const maxValue = Math.max(...activeUsersData.value.map(d => d.value), currentMetrics.value.activeUsers)
  return maxValue > 0 ? Math.ceil(maxValue * 1.2) : 100
})

// AIDEV-NOTE: Chart management functions for external control
function updateAllCharts(metrics) {
  const timestamp = Date.now()
  
  // Add data points to all charts
  if (tpsChart.value) {
    tpsChart.value.addDataPoint(metrics.tps || 0, timestamp)
  }
  if (responseTimeChart.value) {
    responseTimeChart.value.addDataPoint(metrics.avgResponseTime || 0, timestamp)
  }
  if (activeUsersChart.value) {
    activeUsersChart.value.addDataPoint(metrics.activeUsers || 0, timestamp)
  }
  if (errorRateChart.value) {
    errorRateChart.value.addDataPoint(metrics.errorRate || 0, timestamp)
  }
}

function clearAllCharts() {
  if (tpsChart.value) tpsChart.value.clearData()
  if (responseTimeChart.value) responseTimeChart.value.clearData()
  if (activeUsersChart.value) activeUsersChart.value.clearData()
  if (errorRateChart.value) errorRateChart.value.clearData()
}

// Watch for new metrics and update charts
watch(currentMetrics, (newMetrics) => {
  if (newMetrics && newMetrics.timestamp) {
    updateAllCharts(newMetrics)
  }
}, { deep: true })

// Expose methods to parent components
defineExpose({
  updateAllCharts,
  clearAllCharts,
  charts: {
    tpsChart,
    responseTimeChart,
    activeUsersChart,
    errorRateChart
  }
})
</script>

<style scoped>
.charts-section {
  min-height: 600px;
}
</style>