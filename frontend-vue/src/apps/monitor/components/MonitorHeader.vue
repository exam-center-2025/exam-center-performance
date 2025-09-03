<template>
  <header class="bg-white border-b border-gray-200">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between items-center py-3">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">실시간 모니터링</h1>
          <p class="text-gray-500">
            테스트 ID: <span class="font-mono font-semibold">{{ testId }}</span>
          </p>
        </div>
        
        <div class="flex items-center space-x-3">
          <!-- WebSocket 연결 상태 -->
          <div class="flex items-center space-x-2">
            <div 
              :class="wsStatusIndicatorClass"
              class="w-2 h-2 rounded-full"
            ></div>
            <span 
              :class="wsStatusClass" 
              class="text-sm font-medium"
            >
              {{ wsStatusText }}
            </span>
          </div>

          <!-- 현재 시간 -->
          <div class="text-sm text-gray-500">
            {{ currentTime }}
          </div>

          <!-- 제어 버튼 -->
          <div class="flex gap-1">
            <Button 
              @click="goToDashboard"
              variant="secondary"
              size="sm"
            >
              <i class="fas fa-arrow-left mr-1"></i>
              대시보드로
            </Button>
            
            <Button 
              @click="handleStopTest"
              variant="danger"
              size="sm"
              :disabled="!isTestActive"
            >
              <i class="fas fa-stop mr-1"></i>
              테스트 중단
            </Button>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { usePerformanceStore } from '@shared/stores/performance'
import Button from '@shared/components/ui/Button.vue'

const props = defineProps({
  testId: {
    type: String,
    required: true
  },
  wsConnected: {
    type: Boolean,
    default: false
  },
  wsConnecting: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['stop-test'])

const performanceStore = usePerformanceStore()

const currentTime = ref('')
let timeInterval = null

// AIDEV-NOTE: WebSocket connection status display logic
const wsStatusText = computed(() => {
  if (props.wsConnecting) return '연결 중...'
  if (props.wsConnected) return '연결됨'
  return '연결 끊어짐'
})

const wsStatusClass = computed(() => ({
  'text-green-600': props.wsConnected,
  'text-yellow-600': props.wsConnecting,
  'text-red-600': !props.wsConnected && !props.wsConnecting,
}))

const wsStatusIndicatorClass = computed(() => ({
  'bg-green-500': props.wsConnected,
  'bg-yellow-500': props.wsConnecting,
  'bg-red-500': !props.wsConnected && !props.wsConnecting,
}))

const isTestActive = computed(() => {
  const status = performanceStore.testStatus.status
  return status === 'RUNNING' || status === 'STARTING'
})

// AIDEV-NOTE: Navigation and control handlers
function goToDashboard() {
  // In a real router setup, this would navigate to dashboard
  // For now, redirect to Spring Boot dashboard
  window.location.href = '/performance/dashboard'
}

async function handleStopTest() {
  if (!confirm('테스트를 중단하시겠습니까?')) {
    return
  }
  
  try {
    await performanceStore.stopTest(props.testId)
    emit('stop-test', props.testId)
    
    // Redirect after 2 seconds
    setTimeout(() => {
      window.location.href = `/performance/dashboard/results/${props.testId}`
    }, 2000)
  } catch (error) {
    console.error('Failed to stop test:', error)
    alert('테스트 중단 중 오류가 발생했습니다.')
  }
}

function updateCurrentTime() {
  currentTime.value = new Date().toLocaleTimeString('ko-KR')
}

onMounted(() => {
  updateCurrentTime()
  timeInterval = setInterval(updateCurrentTime, 1000)
})

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})
</script>