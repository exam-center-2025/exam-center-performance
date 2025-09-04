<template>
  <div>
    <div class="flat-header flex justify-between items-center">
      <h2 class="card-title">Active Tests</h2>
      <span class="card-label">{{ activeTests.length }} running</span>
    </div>
    
    <div v-if="activeTests.length === 0" class="text-gray-500 text-center py-8">
      No active tests
    </div>
    
    <div v-else class="space-y-2">
      <div
        v-for="test in activeTests"
        :key="test.testId"
        class="test-card"
      >
        <!-- Test Header -->
        <div class="flex justify-between items-start mb-2">
          <div>
            <h3 class="card-subtitle">{{ test.testName || `Test #${test.testId}` }}</h3>
            <p class="card-label">
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
              :disabled="stopping.includes(test.testId)"
            >
              {{ stopping.includes(test.testId) ? 'Stopping...' : 'Stop' }}
            </button>
          </div>
        </div>

        <!-- Progress Bar -->
        <div class="mb-2">
          <div class="flex justify-between text-sm text-gray-600 mb-1">  <!-- text-xs를 text-sm으로 개선 -->
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
        <div class="grid grid-cols-4 gap-2">
          <div class="text-center">
            <p class="card-label">TPS</p>
            <p class="card-value">{{ formatNumber(testMetrics[test.testId]?.tps || 0) }}</p>
          </div>
          <div class="text-center">
            <p class="card-label">Users</p>
            <p class="card-value">{{ testMetrics[test.testId]?.activeUsers || 0 }}/{{ test.maxUsers }}</p>
          </div>
          <div class="text-center">
            <p class="card-label">Response</p>
            <p class="card-value">{{ formatNumber(testMetrics[test.testId]?.avgResponseTime || 0) }}ms</p>
          </div>
          <div class="text-center">
            <p class="card-label">Error %</p>
            <p class="card-value" :class="getErrorClass(testMetrics[test.testId]?.errorRate || 0)">
              {{ formatPercentage(testMetrics[test.testId]?.errorRate || 0) }}%
            </p>
          </div>
        </div>

        <!-- Elapsed Time -->
        <div class="mt-2 pt-2 border-t border-gray-200">
          <div class="flex justify-between">
            <span class="card-label">Elapsed: {{ getElapsedTime(test.startTime) }}</span>
            <span class="card-label">Remaining: {{ getRemainingTime(test) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, watchEffect, nextTick } from 'vue'
import { usePerformanceStore } from '@shared/stores/performance'
import { useWebSocket } from '@shared/composables/useWebSocket'

const store = usePerformanceStore()
const ws = useWebSocket()
const stopping = ref([])

// AIDEV-NOTE: Store pattern - 중앙 관리로 반응성 보장
const activeTests = computed(() => store.activeTests)

// AIDEV-NOTE: Store pattern - 직접 Store의 testMetrics에 접근하여 반응성 보장
// 메트릭 데이터를 ref로 관리하여 강제 반응성 보장
const testMetrics = ref({})

// Store의 testMetrics 변경을 감지하여 로컬 ref 업데이트
watchEffect(() => {
  testMetrics.value = { ...store.testMetrics }
})

// AIDEV-NOTE: 메트릭 값 접근 함수 - testMetrics computed에 의존
function getCurrentMetric(testId, metricKey) {
  const metrics = testMetrics.value[testId]
  const value = metrics ? (metrics[metricKey] || 0) : 0
  // console.log(`Getting metric for ${testId}/${metricKey}:`, value)
  return value
}

// AIDEV-NOTE: Store pattern - 구독 관리를 Store에 위임

// 간소화된 구독 함수 - Store 패턴 사용
function subscribeToTestMetrics(testId, retryCount = 0) {
  // 이미 구독 중이면 스킵
  if (store.hasMetricsSubscription(testId)) {
    console.log(`Already subscribed to metrics for test ${testId}`)
    return
  }
  
  console.log(`Attempting to subscribe to test ${testId}, WebSocket connected: ${ws.connected.value}, retry: ${retryCount}`)
  
  if (!ws.connected.value) {
    // 최대 10회 재시도
    if (retryCount < 10) {
      console.log(`WebSocket not connected, retrying in 1 second (attempt ${retryCount + 1}/10)`)
      setTimeout(() => subscribeToTestMetrics(testId, retryCount + 1), 1000)
    } else {
      console.error(`Failed to subscribe to test ${testId} after 10 retries`)
    }
    return
  }
  
  // WebSocket 구독하고 메시지 수신 시 Store 업데이트
  const subscription = ws.subscribe(`/topic/metrics/${testId}`, (metrics) => {
    // console.log(`Received metrics for test ${testId}:`, metrics)
    store.updateTestMetrics(testId, metrics)
    // 디버그: Store 상태 확인
    // console.log('Updated testMetrics in store:', store.testMetrics[testId])
    // UI 업데이트 확인
    // console.log('Current testMetrics computed value:', testMetrics.value[testId])
  })
  
  if (subscription) {
    store.registerMetricsSubscription(testId, subscription)
    console.log(`Successfully subscribed to metrics for test ${testId}`)
  } else {
    console.error(`Failed to create subscription for test ${testId}`)
  }
}

function unsubscribeFromTestMetrics(testId) {
  store.unregisterMetricsSubscription(testId)
  store.clearTestMetrics(testId)
}


onMounted(() => {
  console.log('ActiveTests component mounted')
  console.log('Initial activeTests:', activeTests.value)
  
  // AIDEV-NOTE: testMetrics 변경 감지를 위한 watch 추가
  // watch(testMetrics, (newMetrics) => {
  //   console.log('testMetrics changed:', Object.keys(newMetrics))
  //   Object.keys(newMetrics).forEach(testId => {
  //     console.log(`Metrics for ${testId}:`, newMetrics[testId])
  //   })
  // }, { deep: true, immediate: true })
  
  // AIDEV-NOTE: Force update when metrics change
  // watch(() => store.testMetrics, (newMetrics) => {
  //   console.log('Store testMetrics directly changed, forcing update')
  //   // Force Vue to re-evaluate computed properties
  //   nextTick(() => {
  //     console.log('After nextTick, testMetrics value:', testMetrics.value)
  //   })
  // }, { deep: true })
  
  // AIDEV-NOTE: Store pattern - Store에서 메트릭 구독 관리
  // Watch for new active tests
  watch(activeTests, (newTests, oldTests) => {
    console.log('ActiveTests changed:', {
      newTests: newTests?.length || 0,
      oldTests: oldTests?.length || 0,
      wsConnected: ws.connected.value,
      newTestsList: newTests?.map(t => ({ id: t.testId, status: t.status })) || [],
      oldTestsList: oldTests?.map(t => ({ id: t.testId, status: t.status })) || []
    })
    
    // AIDEV-NOTE: Store pattern - 구독 관리 간소화
    // Subscribe to new tests
    newTests.forEach(test => {
      if (!oldTests?.find(t => t.testId === test.testId)) {
        console.log('New test found, subscribing:', test.testId)
        subscribeToTestMetrics(test.testId)
      }
    })
    
    // Unsubscribe from removed tests
    oldTests?.forEach(test => {
      if (!newTests.find(t => t.testId === test.testId)) {
        console.log('Test removed, unsubscribing:', test.testId)
        unsubscribeFromTestMetrics(test.testId)
      }
    })
  }, { immediate: true, deep: true })
  
  // AIDEV-NOTE: Additional watch on store.tests as backup
  watch(() => store.tests, (newTests) => {
    console.log('Store tests changed:', {
      totalTests: newTests?.length || 0,
      runningTests: newTests?.filter(t => t.status === 'RUNNING').length || 0,
      runningTestIds: newTests?.filter(t => t.status === 'RUNNING').map(t => t.testId) || []
    })
    
    // Force reactivity by triggering nextTick
    nextTick(() => {
      console.log('After nextTick - activeTests count:', activeTests.value.length)
    })
  }, { immediate: true, deep: true })
  
  // WebSocket 연결 상태 감지 - Store pattern
  watch(() => ws.connected.value, (connected) => {
    console.log('WebSocket connection status changed:', connected)
    if (connected) {
      // 연결되면 모든 활성 테스트 구독
      console.log('WebSocket connected, subscribing to all active tests:', activeTests.value.map(t => t.testId))
      activeTests.value.forEach(test => {
        // 약간의 딜레이를 주어 연결 안정화
        setTimeout(() => {
          subscribeToTestMetrics(test.testId)
        }, 500)
      })
    }
  }, { immediate: true })
})

onUnmounted(() => {
  // AIDEV-NOTE: Store pattern - cleanup via Store
  activeTests.value.forEach(test => {
    unsubscribeFromTestMetrics(test.testId)
  })
})



function getProgress(test) {
  if (!test.startTime || !test.testDurationSeconds) return 0
  
  const elapsed = (Date.now() - new Date(test.startTime)) / 1000
  const progress = Math.min((elapsed / test.testDurationSeconds) * 100, 100)
  return Math.round(progress)
}

function getElapsedTime(startTime) {
  if (!startTime) return '0:00'
  
  const elapsed = Math.floor((Date.now() - new Date(startTime).getTime()) / 1000)
  if (elapsed < 0) return '0:00' // 시간 오류 방지
  
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
  if (stopping.value.includes(test.testId)) return
  
  stopping.value.push(test.testId)
  try {
    await store.stopTest(test.testId)
    // AIDEV-NOTE: Store pattern - cleanup via Store
    unsubscribeFromTestMetrics(test.testId)
  } catch (error) {
    console.error('Failed to stop test:', error)
    // Handle error notification here
  } finally {
    const index = stopping.value.indexOf(test.testId)
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
  @apply text-sm px-3 py-1 font-bold uppercase border;  /* text-xs를 text-sm으로 개선 */
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