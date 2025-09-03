<template>
  <div class="card">
    <h2 class="card-header">Real-time Performance Metrics</h2>
    
    <div v-if="!store.isTestRunning" class="text-gray-500 text-center py-16">
      <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
      </svg>
      <p class="mt-2">Start a test to see real-time metrics</p>
    </div>
    
    <div v-else>
      <canvas ref="chartCanvas" class="w-full h-64"></canvas>
      
      <!-- Chart Controls -->
      <div class="mt-4 flex justify-between items-center">
        <div class="flex space-x-2">
          <button
            v-for="metric in availableMetrics"
            :key="metric.key"
            @click="toggleMetric(metric.key)"
            :class="getMetricButtonClass(metric.key)"
            class="px-3 py-1 text-sm rounded-lg transition-colors"
          >
            {{ metric.label }}
          </button>
        </div>
        
        <div class="flex space-x-2">
          <button
            @click="pauseChart = !pauseChart"
            class="px-3 py-1 text-sm bg-gray-100 hover:bg-gray-200 rounded-lg"
          >
            {{ pauseChart ? 'Resume' : 'Pause' }}
          </button>
          <button
            @click="clearChart"
            class="px-3 py-1 text-sm bg-red-100 hover:bg-red-200 text-red-700 rounded-lg"
          >
            Clear
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { usePerformanceStore } from '@shared/stores/performance'
import { useWebSocket } from '@shared/composables/useWebSocket'
import Chart from 'chart.js/auto'

const store = usePerformanceStore()
const ws = useWebSocket()

// Chart references
const chartCanvas = ref(null)
let chartInstance = null
const pauseChart = ref(false)

// Metrics configuration
const availableMetrics = [
  { key: 'requestsPerSecond', label: 'Requests/s', color: 'rgb(34, 197, 94)', enabled: true },
  { key: 'avgResponseTime', label: 'Avg Response', color: 'rgb(59, 130, 246)', enabled: true },
  { key: 'activeUsers', label: 'Active Users', color: 'rgb(168, 85, 247)', enabled: false },
  { key: 'errorRate', label: 'Error Rate', color: 'rgb(239, 68, 68)', enabled: false }
]

const enabledMetrics = ref(['requestsPerSecond', 'avgResponseTime'])

// Chart data
const chartData = ref({
  labels: [],
  datasets: availableMetrics.map(metric => ({
    label: metric.label,
    data: [],
    borderColor: metric.color,
    backgroundColor: metric.color + '20',
    borderWidth: 2,
    tension: 0.4,
    hidden: !metric.enabled,
    yAxisID: metric.key === 'requestsPerSecond' || metric.key === 'activeUsers' ? 'y' : 'y1'
  }))
})

// Initialize chart
function initChart() {
  if (!chartCanvas.value) return
  
  const ctx = chartCanvas.value.getContext('2d')
  
  chartInstance = new Chart(ctx, {
    type: 'line',
    data: chartData.value,
    options: {
      responsive: true,
      maintainAspectRatio: false,
      interaction: {
        mode: 'index',
        intersect: false
      },
      plugins: {
        legend: {
          display: true,
          position: 'top'
        },
        tooltip: {
          mode: 'index',
          intersect: false
        }
      },
      scales: {
        x: {
          display: true,
          title: {
            display: true,
            text: 'Time'
          }
        },
        y: {
          type: 'linear',
          display: true,
          position: 'left',
          title: {
            display: true,
            text: 'Count'
          }
        },
        y1: {
          type: 'linear',
          display: true,
          position: 'right',
          title: {
            display: true,
            text: 'Time (ms) / Rate (%)'
          },
          grid: {
            drawOnChartArea: false
          }
        }
      }
    }
  })
}

// Update chart with new data
function updateChart(metrics) {
  if (!chartInstance || pauseChart.value) return
  
  const timestamp = new Date().toLocaleTimeString()
  
  // Add new label
  chartData.value.labels.push(timestamp)
  
  // Keep only last 30 data points
  if (chartData.value.labels.length > 30) {
    chartData.value.labels.shift()
  }
  
  // Update each dataset
  availableMetrics.forEach((metric, index) => {
    const value = metrics[metric.key] || 0
    chartData.value.datasets[index].data.push(value)
    
    // Keep only last 30 data points
    if (chartData.value.datasets[index].data.length > 30) {
      chartData.value.datasets[index].data.shift()
    }
  })
  
  chartInstance.update('none')
}

// Toggle metric visibility
function toggleMetric(metricKey) {
  const index = enabledMetrics.value.indexOf(metricKey)
  if (index > -1) {
    enabledMetrics.value.splice(index, 1)
  } else {
    enabledMetrics.value.push(metricKey)
  }
  
  // Update chart dataset visibility
  const datasetIndex = availableMetrics.findIndex(m => m.key === metricKey)
  if (chartInstance && datasetIndex > -1) {
    chartInstance.data.datasets[datasetIndex].hidden = !enabledMetrics.value.includes(metricKey)
    chartInstance.update()
  }
}

// Get metric button class
function getMetricButtonClass(metricKey) {
  if (enabledMetrics.value.includes(metricKey)) {
    const metric = availableMetrics.find(m => m.key === metricKey)
    return `bg-opacity-20 border-2 font-medium`
  }
  return 'bg-gray-100 hover:bg-gray-200'
}

// Clear chart data
function clearChart() {
  chartData.value.labels = []
  chartData.value.datasets.forEach(dataset => {
    dataset.data = []
  })
  
  if (chartInstance) {
    chartInstance.update()
  }
}

// Watch for test changes
watch(() => store.isTestRunning, async (isRunning) => {
  if (isRunning && store.currentTest?.id) {
    await nextTick()
    
    if (!chartInstance && chartCanvas.value) {
      initChart()
    }
    
    // Subscribe to metrics
    ws.subscribe(`/topic/metrics/${store.currentTest.id}`, (data) => {
      updateChart(data)
    })
  } else {
    // Unsubscribe if test stopped
    if (store.currentTest?.id) {
      ws.unsubscribe(`/topic/metrics/${store.currentTest.id}`)
    }
  }
})

// Cleanup
onUnmounted(() => {
  if (chartInstance) {
    chartInstance.destroy()
  }
  
  if (store.currentTest?.id) {
    ws.unsubscribe(`/topic/metrics/${store.currentTest.id}`)
  }
})
</script>