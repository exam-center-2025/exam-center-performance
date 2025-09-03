<template>
  <div class="bg-white border border-gray-200 p-4">
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-lg font-semibold text-gray-900">실시간 로그</h3>
      <div class="flex items-center space-x-2">
        <!-- Log Level Filter -->
        <select 
          v-model="selectedLevel"
          class="text-sm border border-gray-300 px-2 py-1"
        >
          <option value="">모든 레벨</option>
          <option value="ERROR">ERROR</option>
          <option value="WARN">WARN</option>
          <option value="INFO">INFO</option>
          <option value="DEBUG">DEBUG</option>
        </select>
        
        <!-- Auto Scroll Toggle -->
        <label class="flex items-center text-sm">
          <input 
            type="checkbox" 
            v-model="autoScroll"
            class="mr-1"
          />
          자동스크롤
        </label>
        
        <!-- Clear Button -->
        <button 
          @click="clearLogs"
          class="text-sm text-gray-600 hover:text-gray-800 px-2 py-1 border border-gray-300 hover:bg-gray-50"
        >
          <i class="fas fa-trash-alt mr-1"></i>
          지우기
        </button>
      </div>
    </div>

    <!-- Log Container -->
    <div 
      ref="logContainer"
      class="log-container bg-gray-900 text-gray-100 font-mono text-sm border border-gray-300 p-4 overflow-y-auto"
      style="height: 320px;"
      @scroll="onScroll"
    >
      <div v-if="filteredLogs.length === 0" class="text-gray-400 text-center py-8">
        {{ logEntries.length === 0 ? '로그 로딩 중...' : '필터된 로그가 없습니다.' }}
      </div>
      
      <div 
        v-for="log in filteredLogs" 
        :key="log.id"
        :class="getLogEntryClass(log.level)"
        class="log-entry mb-1 py-1 px-2 hover:bg-gray-800 transition-colors duration-200"
      >
        <span class="log-timestamp text-gray-400 mr-2">
          [{{ formatTimestamp(log.timestamp) }}]
        </span>
        <span :class="getLogLevelClass(log.level)" class="log-level font-semibold mr-2 w-14 inline-block">
          {{ log.level || 'INFO' }}
        </span>
        <span class="log-message">
          {{ log.message || '' }}
        </span>
      </div>
      
      <!-- Auto-scroll anchor -->
      <div ref="scrollAnchor"></div>
    </div>

    <!-- Log Stats -->
    <div class="mt-4 pt-4 border-t">
      <div class="flex justify-between items-center text-sm text-gray-600">
        <span>총 {{ logEntries.length }}개 로그 (필터링: {{ filteredLogs.length }}개)</span>
        <span>마지막 업데이트: {{ lastUpdateTime }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch, nextTick, onMounted } from 'vue'
import { usePerformanceStore } from '@shared/stores/performance'

const props = defineProps({
  testId: {
    type: String,
    required: true
  },
  maxLogs: {
    type: Number,
    default: 100
  }
})

const performanceStore = usePerformanceStore()

// Component state
const logContainer = ref(null)
const scrollAnchor = ref(null)
const selectedLevel = ref('')
const autoScroll = ref(true)
const userScrolledUp = ref(false)
const lastUpdateTime = ref('')

// AIDEV-NOTE: Log data from store with filtering capabilities
const logEntries = computed(() => performanceStore.logEntries)

const filteredLogs = computed(() => {
  if (!selectedLevel.value) {
    return logEntries.value
  }
  return logEntries.value.filter(log => log.level === selectedLevel.value)
})

// Log styling functions
function getLogEntryClass(level) {
  switch (level) {
    case 'ERROR':
      return 'border-l-2 border-red-500 bg-red-900 bg-opacity-20'
    case 'WARN':
      return 'border-l-2 border-yellow-500 bg-yellow-900 bg-opacity-20'
    case 'INFO':
      return 'border-l-2 border-blue-500 bg-blue-900 bg-opacity-10'
    case 'DEBUG':
      return 'border-l-2 border-gray-500 bg-gray-800 bg-opacity-50'
    default:
      return 'border-l-2 border-gray-600'
  }
}

function getLogLevelClass(level) {
  switch (level) {
    case 'ERROR':
      return 'text-red-400'
    case 'WARN':
      return 'text-yellow-400'
    case 'INFO':
      return 'text-blue-400'
    case 'DEBUG':
      return 'text-gray-400'
    default:
      return 'text-gray-300'
  }
}

function formatTimestamp(timestamp) {
  if (!timestamp) return ''
  return new Date(timestamp).toLocaleTimeString('ko-KR', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

// Scroll management
function scrollToBottom() {
  if (!logContainer.value || !autoScroll.value) return
  
  nextTick(() => {
    if (scrollAnchor.value) {
      scrollAnchor.value.scrollIntoView({ behavior: 'smooth' })
    } else {
      logContainer.value.scrollTop = logContainer.value.scrollHeight
    }
  })
}

function onScroll(event) {
  if (!logContainer.value) return
  
  const { scrollTop, scrollHeight, clientHeight } = logContainer.value
  const isNearBottom = scrollTop + clientHeight >= scrollHeight - 10
  
  userScrolledUp.value = !isNearBottom
  
  // Auto-disable auto-scroll if user scrolls up
  if (userScrolledUp.value && autoScroll.value) {
    // Don't immediately disable, give user a chance
    setTimeout(() => {
      if (userScrolledUp.value) {
        autoScroll.value = false
      }
    }, 2000)
  }
}

function clearLogs() {
  performanceStore.logEntries = []
  lastUpdateTime.value = new Date().toLocaleTimeString('ko-KR')
}

// Watch for new logs and auto-scroll
watch(logEntries, (newLogs) => {
  if (newLogs.length > 0) {
    lastUpdateTime.value = new Date().toLocaleTimeString('ko-KR')
    
    if (autoScroll.value) {
      scrollToBottom()
    }
  }
}, { deep: true })

// Watch auto-scroll toggle
watch(autoScroll, (newValue) => {
  if (newValue) {
    userScrolledUp.value = false
    scrollToBottom()
  }
})

// Initialize
onMounted(() => {
  lastUpdateTime.value = new Date().toLocaleTimeString('ko-KR')
  scrollToBottom()
})

// Expose methods for parent components
defineExpose({
  scrollToBottom,
  clearLogs,
  addLogEntry: (log) => {
    performanceStore.addLogEntry(log)
  }
})
</script>

<style scoped>
.log-container {
  /* Custom scrollbar for dark theme */
  scrollbar-width: thin;
  scrollbar-color: #4a5568 #2d3748;
}

.log-container::-webkit-scrollbar {
  width: 8px;
}

.log-container::-webkit-scrollbar-track {
  background: #2d3748;
  border-radius: 4px;
}

.log-container::-webkit-scrollbar-thumb {
  background: #4a5568;
  border-radius: 4px;
}

.log-container::-webkit-scrollbar-thumb:hover {
  background: #718096;
}

.log-entry {
  word-break: break-word;
  line-height: 1.4;
}

.log-timestamp {
  user-select: none;
}

.log-level {
  text-transform: uppercase;
  font-size: 0.75rem;
  user-select: none;
}
</style>