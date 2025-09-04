<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Header - 플랫 디자인 적용 -->
    <header class="bg-white border-b border-gray-300">
      <div class="max-w-full px-4">
        <div class="flex justify-between items-center h-12">
          <h1 class="text-xl font-bold text-gray-900">Performance Dashboard</h1>
          <div class="flex items-center gap-4">
            <div class="flex items-center gap-2">
              <span class="text-sm font-medium text-gray-600">WebSocket</span>
              <span :class="['px-2 py-0.5 text-xs font-bold uppercase', wsStatusClass]">{{ wsStatus }}</span>
            </div>
            <div class="flex items-center gap-2">
              <span class="text-sm font-medium text-gray-600">Database</span>
              <span :class="['px-2 py-0.5 text-xs font-bold uppercase', dbStatusClass]">{{ dbStatus }}</span>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- Main Content - 재정렬된 레이아웃 -->
    <main class="px-4 py-3">
      <!-- 상단: Quick Stats (전체 너비) -->
      <section class="mb-3">
        <QuickStats />
      </section>

      <!-- 중간: 주요 대시보드 영역 -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-3 mb-3">
        <!-- Test Control -->
        <div class="flat-card">
          <TestControlPanel />
        </div>

        <!-- Active Tests -->
        <div class="flat-card">
          <ActiveTests />
        </div>
      </div>

      <!-- 하단: Test History (전체 너비) -->
      <section class="flat-card">
        <TestHistory />
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { usePerformanceStore } from '@shared/stores/performance'
import { useWebSocket } from '@shared/composables/useWebSocket'
import TestControlPanel from './components/TestControlPanel.vue'
import TestHistory from './components/TestHistory.vue'
import QuickStats from './components/QuickStats.vue'
import ActiveTests from './components/ActiveTests.vue'
import performanceApi from '@shared/services/performanceApi'

// Store and WebSocket
const store = usePerformanceStore()
const ws = useWebSocket()

// Status states
const dbStatus = ref('checking')
const wsStatus = computed(() => {
  if (ws.connecting.value) return 'connecting'
  if (ws.connected.value) return 'connected'
  if (ws.error.value) return 'error'
  return 'disconnected'
})

// Status classes
const dbStatusClass = computed(() => ({
  'status-active': dbStatus.value === 'connected',
  'status-inactive': dbStatus.value === 'disconnected',
  'status-error': dbStatus.value === 'error'
}))

const wsStatusClass = computed(() => ({
  'status-active': wsStatus.value === 'connected',
  'status-inactive': wsStatus.value === 'disconnected' || wsStatus.value === 'connecting',
  'status-error': wsStatus.value === 'error'
}))

// Check database health
async function checkDatabaseHealth() {
  try {
    await performanceApi.checkDatabaseHealth()
    dbStatus.value = 'connected'
  } catch (err) {
    dbStatus.value = 'error'
    console.error('Database health check failed:', err)
  }
}


// WebSocket 연결 대기 함수
const waitForWebSocketConnection = async (maxRetries = 20, retryDelay = 200) => {
  for (let i = 0; i < maxRetries; i++) {
    if (ws.connected.value) {
      console.log('WebSocket connected successfully')
      return true
    }
    await new Promise(resolve => setTimeout(resolve, retryDelay))
  }
  console.warn('WebSocket connection timeout')
  return false
}

// Initialize
onMounted(async () => {
  // AIDEV-NOTE: Make store available globally for debugging
  if (import.meta.env.DEV) {
    window.debugStore = store
  }
  
  // Connect WebSocket first
  ws.connect('/ws')
  
  // Load initial data (can run in parallel with WebSocket connection)
  const dataLoadPromise = Promise.all([
    store.loadPlans(),
    store.loadTests(),
    store.loadTestHistory(),
    store.loadQuickStats(),
    checkDatabaseHealth()
  ])
  
  // Wait for WebSocket connection
  const isConnected = await waitForWebSocketConnection()
  
  // Wait for data load to complete
  await dataLoadPromise
  
  // Only subscribe if WebSocket is connected
  if (isConnected) {
    // Subscribe to metrics if test is running
    if (store.isTestRunning && store.currentTest?.id) {
      ws.subscribe(`/topic/metrics/${store.currentTest.id}`, (data) => {
        console.log('Received metrics:', data)
      })
    }
    
    // Subscribe to test started events
    ws.subscribe('/topic/test-started', async (data) => {
      console.log('Test started event received:', data)
      console.log('Store state before loadTests:', store.debugStoreState())
      
      // Reload tests to update active tests list
      await store.loadTests()
      
      console.log('Store state after loadTests:', store.debugStoreState())
      
      // Also refresh quick stats
      store.loadQuickStats()
      // ActiveTests 컴포넌트가 자동으로 메트릭 구독 처리
    })
    
    // Subscribe to test completion events
    ws.subscribe('/topic/test-completed', (data) => {
      console.log('Test completed:', data)
      // Refresh quick stats and history
      store.loadQuickStats()
      store.loadTestHistory()
      store.loadTests()
    })
  } else {
    console.error('WebSocket connection failed, real-time features will be disabled')
  }
  
  // Set up periodic data refresh
  setInterval(() => {
    store.loadQuickStats()
  }, 60000) // Every minute
})

// Cleanup
onUnmounted(() => {
  ws.disconnect()
})
</script>

<style scoped>
/* 플랫 디자인 스타일 */
.flat-card {
  @apply bg-white border border-gray-300 p-3;
  border-radius: 0;
  box-shadow: none;
  transition: border-color 0.2s ease;
}

.flat-card:hover {
  @apply border-gray-400;
}

/* 상태 표시 - 플랫 스타일 */
.status-active {
  @apply bg-green-500 text-white border border-green-600;
  border-radius: 0;
}

.status-inactive {
  @apply bg-gray-400 text-white border border-gray-500;
  border-radius: 0;
}

.status-error {
  @apply bg-red-500 text-white border border-red-600;
  border-radius: 0;
}

/* 로딩 오버레이 - 플랫 스타일 */
.loading-overlay {
  @apply absolute inset-0 bg-white bg-opacity-90 flex items-center justify-center;
  border: 2px solid var(--color-gray-200);
}

.loading-spinner {
  @apply h-8 w-8;
  border: 3px solid var(--color-gray-300);
  border-top-color: var(--color-primary);
  animation: spin 1s linear infinite;
  border-radius: 0;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 플랫 버튼 스타일 */
.flat-btn {
  @apply px-3 py-1 text-sm font-semibold border transition-colors duration-200;
  border-radius: 0;
  box-shadow: none;
}

.flat-btn-primary {
  @apply bg-blue-500 text-white border-blue-600 hover:bg-blue-600;
}

.flat-btn-secondary {
  @apply bg-gray-50 text-gray-700 border-gray-300 hover:bg-gray-100;
}
</style>