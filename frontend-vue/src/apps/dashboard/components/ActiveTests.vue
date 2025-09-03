<template>
  <div>
    <div class="flat-header flex justify-between items-center">
      <h2 class="text-lg font-bold">Active Tests</h2>
      <span class="text-sm text-gray-500">{{ activeTests.length }} running</span>
    </div>
    
    <div v-if="activeTests.length === 0" class="text-gray-500 text-center py-8">
      No active tests
    </div>
    
    <div v-else class="space-y-2">
      <div
        v-for="test in activeTests"
        :key="test.id"
        class="test-card"
      >
        <!-- Test Header -->
        <div class="flex justify-between items-start mb-2">
          <div>
            <h3 class="font-medium text-gray-900">{{ test.testName || `Test #${test.id}` }}</h3>
            <p class="text-sm text-gray-500">
              Started: {{ formatDate(test.startTime) }}
            </p>
          </div>
          <div class="flex space-x-2">
            <button
              @click="viewMonitoring(test)"
              class="flat-btn-sm flat-btn-info"
            >
              Monitor
            </button>
            <button
              @click="stopTest(test)"
              class="flat-btn-sm flat-btn-danger"
              :disabled="stopping.includes(test.id)"
            >
              {{ stopping.includes(test.id) ? 'Stopping...' : 'Stop' }}
            </button>
          </div>
        </div>

        <!-- Progress Bar -->
        <div class="mb-2">
          <div class="flex justify-between text-xs text-gray-600 mb-1">
            <span>Progress</span>
            <span>{{ getProgress(test) }}%</span>
          </div>
          <div class="progress-bar">
            <div
              class="progress-fill"
              :style="{ width: `${getProgress(test)}%` }"
            ></div>
          </div>
        </div>

        <!-- Real-time Metrics -->
        <div class="grid grid-cols-4 gap-2 text-xs">
          <div class="text-center">
            <p class="text-gray-500">TPS</p>
            <p class="font-medium">{{ formatNumber(getCurrentMetric(test.id, 'tps')) }}</p>
          </div>
          <div class="text-center">
            <p class="text-gray-500">Users</p>
            <p class="font-medium">{{ getCurrentMetric(test.id, 'activeUsers') }}/{{ test.maxUsers }}</p>
          </div>
          <div class="text-center">
            <p class="text-gray-500">Response</p>
            <p class="font-medium">{{ formatNumber(getCurrentMetric(test.id, 'avgResponseTime')) }}ms</p>
          </div>
          <div class="text-center">
            <p class="text-gray-500">Error %</p>
            <p class="font-medium" :class="getErrorClass(getCurrentMetric(test.id, 'errorRate'))">
              {{ formatPercentage(getCurrentMetric(test.id, 'errorRate')) }}%
            </p>
          </div>
        </div>

        <!-- Elapsed Time -->
        <div class="mt-2 pt-2 border-t border-gray-200">
          <div class="flex justify-between text-xs text-gray-500">
            <span>Elapsed: {{ getElapsedTime(test.startTime) }}</span>
            <span>Remaining: {{ getRemainingTime(test) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { usePerformanceStore } from '@shared/stores/performance'
import { useWebSocket } from '@shared/composables/useWebSocket'

const store = usePerformanceStore()
const ws = useWebSocket()
const stopping = ref([])
const metricsData = ref({}) // Store real-time metrics per test

const activeTests = computed(() => store.activeTests)

// Real-time metrics tracking
const subscriptions = ref([])

onMounted(() => {
  // Subscribe to active test metrics
  activeTests.value.forEach(test => {
    subscribeToTestMetrics(test.id)
  })
})

onUnmounted(() => {
  // Cleanup subscriptions
  subscriptions.value.forEach(unsubscribe => unsubscribe())
})

function subscribeToTestMetrics(testId) {
  const unsubscribe = ws.subscribe(`/topic/metrics/${testId}`, (metrics) => {
    metricsData.value[testId] = {
      ...metrics,
      timestamp: Date.now()
    }
  })
  subscriptions.value.push(unsubscribe)
}

function getCurrentMetric(testId, metricKey) {
  const metrics = metricsData.value[testId]
  return metrics ? metrics[metricKey] || 0 : 0
}

function getProgress(test) {
  if (!test.startTime || !test.testDurationSeconds) return 0
  
  const elapsed = (Date.now() - new Date(test.startTime)) / 1000
  const progress = Math.min((elapsed / test.testDurationSeconds) * 100, 100)
  return Math.round(progress)
}

function getElapsedTime(startTime) {
  if (!startTime) return '0:00'
  
  const elapsed = Math.floor((Date.now() - new Date(startTime)) / 1000)
  const minutes = Math.floor(elapsed / 60)
  const seconds = elapsed % 60
  return `${minutes}:${seconds.toString().padStart(2, '0')}`
}

function getRemainingTime(test) {
  if (!test.startTime || !test.testDurationSeconds) return 'Unknown'
  
  const elapsed = (Date.now() - new Date(test.startTime)) / 1000
  const remaining = Math.max(test.testDurationSeconds - elapsed, 0)
  const minutes = Math.floor(remaining / 60)
  const seconds = Math.floor(remaining % 60)
  
  if (remaining <= 0) return 'Completing...'
  return `${minutes}:${seconds.toString().padStart(2, '0')}`
}

async function stopTest(test) {
  if (stopping.value.includes(test.id)) return
  
  stopping.value.push(test.id)
  try {
    await store.stopTest(test.id)
    // Remove metrics subscription
    const index = subscriptions.value.findIndex(sub => sub.testId === test.id)
    if (index !== -1) {
      subscriptions.value[index]()
      subscriptions.value.splice(index, 1)
    }
    delete metricsData.value[test.id]
  } catch (error) {
    console.error('Failed to stop test:', error)
    // Handle error notification here
  } finally {
    const index = stopping.value.indexOf(test.id)
    if (index !== -1) stopping.value.splice(index, 1)
  }
}

function viewMonitoring(test) {
  // Navigate to monitoring page
  const monitorUrl = `/performance/vue-dist/monitor.html?testId=${test.testId}`
  window.open(monitorUrl, '_blank')
}

function formatNumber(value) {
  if (!value) return '0'
  return value.toFixed(2)
}

function formatPercentage(value) {
  if (!value) return '0'
  return (value * 100).toFixed(1)
}

function formatDate(timestamp) {
  if (!timestamp) return '-'
  return new Date(timestamp).toLocaleTimeString()
}

function getErrorClass(errorRate) {
  if (errorRate > 0.1) return 'text-red-600'
  if (errorRate > 0.05) return 'text-orange-600'
  return 'text-green-600'
}
</script>

<style scoped>
/* 플랫 헤더 스타일 */
.flat-header {
  @apply text-base font-bold text-gray-900 pb-2 mb-2 border-b border-gray-300;
}

/* 플랫 테스트 카드 */
.test-card {
  @apply border border-gray-300 p-2 hover:border-gray-400 transition-colors bg-white;
  border-radius: 0;
  box-shadow: none;
}

/* 플랫 소형 버튼 */
.flat-btn-sm {
  @apply text-xs px-2 py-0.5 font-bold uppercase border;
  border-radius: 0;
  transition: all 0.2s ease;
}

.flat-btn-info {
  @apply bg-blue-500 text-white border-blue-600 hover:bg-blue-600;
}

.flat-btn-danger {
  @apply bg-red-500 text-white border-red-600 hover:bg-red-600;
}

.flat-btn-sm:disabled {
  @apply opacity-50 cursor-not-allowed;
}

/* 플랫 프로그레스 바 */
.progress-bar {
  @apply w-full bg-gray-200 h-2 border border-gray-300;
  border-radius: 0;
}

.progress-fill {
  @apply bg-blue-500 h-full transition-all duration-300;
  border-radius: 0;
}
</style>