<template>
  <div class="card">
    <h2 class="card-header">Current Metrics</h2>
    
    <div v-if="!store.currentTest" class="text-gray-500 text-center py-8">
      No test is currently running
    </div>
    
    <div v-else class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <!-- Active Users -->
      <div class="bg-blue-50 p-4 rounded-lg">
        <div class="text-sm text-blue-600 font-medium">Active Users</div>
        <div class="text-2xl font-bold text-blue-900">{{ metrics.activeUsers || 0 }}</div>
      </div>
      
      <!-- Requests/sec -->
      <div class="bg-green-50 p-4 rounded-lg">
        <div class="text-sm text-green-600 font-medium">Requests/sec</div>
        <div class="text-2xl font-bold text-green-900">{{ metrics.requestsPerSecond || 0 }}</div>
      </div>
      
      <!-- Response Time -->
      <div class="bg-yellow-50 p-4 rounded-lg">
        <div class="text-sm text-yellow-600 font-medium">Avg Response (ms)</div>
        <div class="text-2xl font-bold text-yellow-900">{{ metrics.avgResponseTime || 0 }}</div>
      </div>
      
      <!-- Error Rate -->
      <div class="bg-red-50 p-4 rounded-lg">
        <div class="text-sm text-red-600 font-medium">Error Rate</div>
        <div class="text-2xl font-bold text-red-900">{{ metrics.errorRate || 0 }}%</div>
      </div>
      
      <!-- Total Requests -->
      <div class="bg-purple-50 p-4 rounded-lg">
        <div class="text-sm text-purple-600 font-medium">Total Requests</div>
        <div class="text-2xl font-bold text-purple-900">{{ metrics.totalRequests || 0 }}</div>
      </div>
      
      <!-- Success Rate -->
      <div class="bg-indigo-50 p-4 rounded-lg">
        <div class="text-sm text-indigo-600 font-medium">Success Rate</div>
        <div class="text-2xl font-bold text-indigo-900">{{ metrics.successRate || 0 }}%</div>
      </div>
      
      <!-- Min Response -->
      <div class="bg-gray-50 p-4 rounded-lg">
        <div class="text-sm text-gray-600 font-medium">Min Response (ms)</div>
        <div class="text-2xl font-bold text-gray-900">{{ metrics.minResponseTime || 0 }}</div>
      </div>
      
      <!-- Max Response -->
      <div class="bg-gray-50 p-4 rounded-lg">
        <div class="text-sm text-gray-600 font-medium">Max Response (ms)</div>
        <div class="text-2xl font-bold text-gray-900">{{ metrics.maxResponseTime || 0 }}</div>
      </div>
    </div>
    
    <!-- Test Info -->
    <div v-if="store.currentTest" class="mt-6 pt-6 border-t border-gray-200">
      <div class="grid grid-cols-2 gap-4 text-sm">
        <div>
          <span class="text-gray-500">Test ID:</span>
          <span class="ml-2 font-medium">{{ store.currentTest?.id || 'N/A' }}</span>
        </div>
        <div>
          <span class="text-gray-500">Status:</span>
          <span class="ml-2 font-medium">{{ store.currentTest?.status || 'N/A' }}</span>
        </div>
        <div>
          <span class="text-gray-500">Started:</span>
          <span class="ml-2 font-medium">{{ formatTime(store.currentTest?.startTime) }}</span>
        </div>
        <div>
          <span class="text-gray-500">Duration:</span>
          <span class="ml-2 font-medium">{{ elapsedTime }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { usePerformanceStore } from '@shared/stores/performance'
import { useWebSocket } from '@shared/composables/useWebSocket'

const store = usePerformanceStore()
const ws = useWebSocket()

// Metrics state
const metrics = ref({
  activeUsers: 0,
  requestsPerSecond: 0,
  avgResponseTime: 0,
  errorRate: 0,
  totalRequests: 0,
  successRate: 0,
  minResponseTime: 0,
  maxResponseTime: 0
})

// Timer for elapsed time
const elapsedTime = ref('00:00:00')
let timerInterval = null

// Format time
function formatTime(timestamp) {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  return date.toLocaleTimeString()
}

// Update elapsed time
function updateElapsedTime() {
  if (store.currentTest?.startTime) {
    const start = new Date(store.currentTest?.startTime || Date.now())
    const now = new Date()
    const diff = Math.floor((now - start) / 1000)
    
    const hours = Math.floor(diff / 3600)
    const minutes = Math.floor((diff % 3600) / 60)
    const seconds = diff % 60
    
    elapsedTime.value = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
  }
}

// Watch for test changes
watch(() => store.currentTest, (newTest, oldTest) => {
  // Unsubscribe from old test
  if (oldTest?.id) {
    ws.unsubscribe(`/topic/metrics/${oldTest.id}`)
  }
  
  // Subscribe to new test
  if (newTest?.id && newTest.status === 'RUNNING') {
    ws.subscribe(`/topic/metrics/${newTest.id}`, (data) => {
      metrics.value = { ...metrics.value, ...data }
    })
    
    // Start timer
    if (timerInterval) clearInterval(timerInterval)
    timerInterval = setInterval(updateElapsedTime, 1000)
    updateElapsedTime()
  } else {
    // Clear timer
    if (timerInterval) {
      clearInterval(timerInterval)
      timerInterval = null
    }
  }
})

// Cleanup
onUnmounted(() => {
  if (timerInterval) {
    clearInterval(timerInterval)
  }
  
  if (store.currentTest?.id) {
    ws.unsubscribe(`/topic/metrics/${store.currentTest.id}`)
  }
})
</script>