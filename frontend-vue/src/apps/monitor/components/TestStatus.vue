<template>
  <div class="bg-white border border-gray-200 p-4">
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-lg font-semibold text-gray-900">테스트 상태</h3>
      <span 
        :class="statusBadgeClass"
        class="px-2 py-1 text-sm font-medium border border-gray-200"
      >
        {{ statusText }}
      </span>
    </div>

    <!-- Progress Bar -->
    <div class="mb-4" v-if="showProgress">
      <div class="flex justify-between items-center mb-2">
        <span class="text-sm font-medium text-gray-700">진행률</span>
        <span class="text-sm text-gray-600">{{ Math.round(progress) }}%</span>
      </div>
      <div class="w-full bg-gray-200 h-2">
        <div 
          class="h-2 transition-all duration-500 ease-out"
          :class="progressBarClass"
          :style="{ width: `${Math.min(progress, 100)}%` }"
        ></div>
      </div>
    </div>

    <!-- Status Details -->
    <div class="space-y-2">
      <div v-if="startTime" class="flex justify-between">
        <span class="text-sm text-gray-600">시작 시간</span>
        <span class="text-sm font-medium">{{ formatTime(startTime) }}</span>
      </div>
      
      <div v-if="elapsed" class="flex justify-between">
        <span class="text-sm text-gray-600">경과 시간</span>
        <span class="text-sm font-medium">{{ formatDuration(elapsed) }}</span>
      </div>
      
      <div v-if="estimatedEndTime" class="flex justify-between">
        <span class="text-sm text-gray-600">예상 종료</span>
        <span class="text-sm font-medium">{{ formatTime(estimatedEndTime) }}</span>
      </div>
      
      <div v-if="endTime" class="flex justify-between">
        <span class="text-sm text-gray-600">종료 시간</span>
        <span class="text-sm font-medium">{{ formatTime(endTime) }}</span>
      </div>

      <div v-if="message" class="pt-3 border-t">
        <p class="text-sm text-gray-600">{{ message }}</p>
      </div>
    </div>

    <!-- Test Configuration Summary -->
    <div class="mt-4 pt-3 border-t" v-if="showConfig && testConfig">
      <h4 class="text-sm font-medium text-gray-700 mb-3">테스트 설정</h4>
      <div class="grid grid-cols-2 gap-2 text-sm">
        <div class="flex justify-between">
          <span class="text-gray-600">최대 사용자</span>
          <span class="font-medium">{{ testConfig.maxUsers }}명</span>
        </div>
        <div class="flex justify-between">
          <span class="text-gray-600">램프업 시간</span>
          <span class="font-medium">{{ testConfig.rampUpSeconds }}초</span>
        </div>
        <div class="flex justify-between">
          <span class="text-gray-600">테스트 시간</span>
          <span class="font-medium">{{ testConfig.testDurationSeconds }}초</span>
        </div>
        <div class="flex justify-between">
          <span class="text-gray-600">시나리오</span>
          <span class="font-medium">{{ formatScenario(testConfig.scenario) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { usePerformanceStore } from '@shared/stores/performance'

const props = defineProps({
  testId: {
    type: String,
    required: true
  },
  testConfig: {
    type: Object,
    default: null
  },
  showConfig: {
    type: Boolean,
    default: true
  }
})

const performanceStore = usePerformanceStore()
const currentTime = ref(Date.now())

let timeInterval = null

// AIDEV-NOTE: Test status and progress computation
const testStatus = computed(() => performanceStore.testStatus)

const statusText = computed(() => {
  const statusMap = {
    'STARTING': '시작 중',
    'RUNNING': '실행 중',
    'STOPPING': '중단 중',
    'COMPLETED': '완료',
    'FAILED': '실패',
    'CANCELLED': '취소됨',
    'STOPPED': '중단됨'
  }
  return statusMap[testStatus.value.status] || testStatus.value.status || '알 수 없음'
})

const statusBadgeClass = computed(() => {
  const baseClass = 'px-3 py-1 rounded-full text-sm font-medium'
  
  switch (testStatus.value.status) {
    case 'RUNNING':
      return 'bg-green-100 text-green-800'
    case 'STARTING':
      return 'bg-blue-100 text-blue-800'
    case 'STOPPING':
      return 'bg-yellow-100 text-yellow-800'
    case 'COMPLETED':
      return 'bg-green-100 text-green-800'
    case 'FAILED':
    case 'CANCELLED':
      return 'bg-red-100 text-red-800'
    case 'STOPPED':
      return 'bg-gray-100 text-gray-800'
    default:
      return 'bg-gray-100 text-gray-600'
  }
})

const showProgress = computed(() => {
  return ['STARTING', 'RUNNING', 'STOPPING'].includes(testStatus.value.status)
})

const progress = computed(() => testStatus.value.progress || 0)

const progressBarClass = computed(() => {
  if (progress.value >= 100) return 'bg-green-500'
  if (progress.value >= 75) return 'bg-blue-500'
  if (progress.value >= 50) return 'bg-yellow-500'
  return 'bg-blue-500'
})

const startTime = computed(() => testStatus.value.startTime)
const endTime = computed(() => testStatus.value.endTime)
const message = computed(() => testStatus.value.message)

const elapsed = computed(() => {
  if (!startTime.value) return null
  const start = new Date(startTime.value).getTime()
  const end = endTime.value ? new Date(endTime.value).getTime() : currentTime.value
  return end - start
})

const estimatedEndTime = computed(() => {
  if (!props.testConfig || !startTime.value || endTime.value) return null
  
  const start = new Date(startTime.value).getTime()
  const totalDuration = (props.testConfig.rampUpSeconds + props.testConfig.testDurationSeconds) * 1000
  return new Date(start + totalDuration)
})

// Formatting functions
function formatTime(timestamp) {
  if (!timestamp) return 'N/A'
  return new Date(timestamp).toLocaleString('ko-KR', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

function formatDuration(milliseconds) {
  if (!milliseconds || milliseconds < 0) return '0초'
  
  const seconds = Math.floor(milliseconds / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  
  if (hours > 0) {
    return `${hours}시간 ${minutes % 60}분 ${seconds % 60}초`
  } else if (minutes > 0) {
    return `${minutes}분 ${seconds % 60}초`
  } else {
    return `${seconds}초`
  }
}

function formatScenario(scenario) {
  const scenarioMap = {
    'NORMAL_USER': '일반 사용자',
    'HEAVY_USER': '헤비 사용자',
    'STRESS_TEST': '스트레스 테스트'
  }
  return scenarioMap[scenario] || scenario || '알 수 없음'
}

function updateCurrentTime() {
  currentTime.value = Date.now()
}

onMounted(() => {
  // Update current time every second for elapsed time calculation
  timeInterval = setInterval(updateCurrentTime, 1000)
})

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})
</script>

<style scoped>
/* Additional status-specific styles can be added here */
</style>