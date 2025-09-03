<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Header -->
    <MonitorHeader 
      :test-id="testId"
      :ws-connected="monitoring.connected.value"
      :ws-connecting="monitoring.connecting.value"
      @stop-test="handleStopTest"
    />

    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
      <!-- Error Display -->
      <div v-if="monitoring.error.value" class="mb-6">
        <div class="bg-red-50 border border-red-200 p-4">
          <div class="flex items-center">
            <i class="fas fa-exclamation-triangle text-red-600 mr-2"></i>
            <span class="text-red-800">{{ monitoring.error.value }}</span>
          </div>
        </div>
      </div>

      <!-- Main Grid -->
      <div class="grid grid-cols-1 xl:grid-cols-3 gap-4">
        <!-- Charts Section -->
        <div class="xl:col-span-2">
          <ChartsGrid ref="chartsGrid" />
        </div>

        <!-- Right Panel -->
        <div class="space-y-4">
          <!-- Test Status -->
          <TestStatus 
            :test-id="testId"
            :test-config="testConfig"
            :show-config="true"
          />
          
          <!-- Metrics Panel -->
          <MetricsPanel />
        </div>
      </div>

      <!-- Log Viewer -->
      <div class="mt-4">
        <LogViewer 
          ref="logViewer"
          :test-id="testId"
          :max-logs="100"
        />
      </div>
    </main>

    <!-- Loading Overlay -->
    <div v-if="isInitializing" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white border border-gray-200 p-4 text-center">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-4"></div>
        <p class="text-gray-600">모니터링을 초기화하는 중...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useMonitoring } from '@shared/composables/useMonitoring'
import { usePerformanceStore } from '@shared/stores/performance'
import performanceApi from '@shared/services/performanceApi'

// Components
import MonitorHeader from './components/MonitorHeader.vue'
import ChartsGrid from './components/ChartsGrid.vue'
import MetricsPanel from './components/MetricsPanel.vue'
import TestStatus from './components/TestStatus.vue'
import LogViewer from './components/LogViewer.vue'

// AIDEV-NOTE: Get testId from URL params or props
// AIDEV-NOTE: Applied flat design style - removed rounded corners, reduced padding/margins, thin borders
const props = defineProps({
  testId: {
    type: String,
    default: () => {
      // Extract testId from URL path (e.g., /monitor/TestId_123)
      const pathParts = window.location.pathname.split('/')
      return pathParts[pathParts.length - 1] || 'unknown'
    }
  }
})

const performanceStore = usePerformanceStore()
const monitoring = useMonitoring(props.testId)

// Component refs
const chartsGrid = ref(null)
const logViewer = ref(null)

// Local state
const isInitializing = ref(true)
const testConfig = ref(null)

// Initialize monitoring
async function initializeMonitoring() {
  try {
    console.log('Initializing monitoring for test:', props.testId)
    
    // Load test configuration
    try {
      testConfig.value = await performanceApi.getTestConfiguration(props.testId)
    } catch (err) {
      console.warn('Failed to load test config:', err)
      // Continue without config
    }
    
    // Start monitoring
    await monitoring.startMonitoring()
    
    console.log('Monitoring initialized successfully')
  } catch (error) {
    console.error('Failed to initialize monitoring:', error)
  } finally {
    isInitializing.value = false
  }
}

// Handle test stop
async function handleStopTest(testId) {
  try {
    await monitoring.stopMonitoring()
    // Redirect will be handled by the header component
  } catch (error) {
    console.error('Failed to handle test stop:', error)
  }
}

// Cleanup on page unload
function handleBeforeUnload() {
  monitoring.stopMonitoring()
}

// Lifecycle
onMounted(() => {
  console.log('Monitor App mounted with testId:', props.testId)
  initializeMonitoring()
  
  // Add page unload handler
  window.addEventListener('beforeunload', handleBeforeUnload)
})

onUnmounted(() => {
  monitoring.stopMonitoring()
  window.removeEventListener('beforeunload', handleBeforeUnload)
})

// Expose for debugging
if (process.env.NODE_ENV === 'development') {
  window.monitorApp = {
    testId: props.testId,
    monitoring,
    performanceStore,
    testConfig
  }
}
</script>