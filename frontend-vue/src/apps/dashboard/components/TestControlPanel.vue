<template>
  <div>
    <h2 class="flat-header">Test Control</h2>
    
    <!-- Test Configuration Form -->
    <form @submit.prevent="handleStartTest" class="space-y-2">
      <!-- Test Name -->
      <div>
        <label class="flat-label">
          Test Name
        </label>
        <input
          v-model="config.testName"
          type="text"
          required
          class="flat-input"
          placeholder="Enter test name"
        />
      </div>

      <!-- Plan Selection -->
      <div>
        <label class="flat-label">
          Test Plan
        </label>
        <select
          v-model="config.planId"
          required
          class="flat-input"
        >
          <option value="" disabled>Select a plan</option>
          <option v-for="plan in (store.plans || [])" :key="plan?.id || plan?.planId" :value="plan?.id || plan?.planId">
            {{ plan?.name || plan?.planName || 'Unnamed Plan' }} (ID: {{ plan?.id || plan?.planId }})
          </option>
        </select>
      </div>

      <!-- Run Type -->
      <div>
        <label class="flat-label">
          Run Type
        </label>
        <select
          v-model="config.runType"
          class="flat-input"
        >
          <option value="TEST">Test</option>
          <option value="REAL">Real</option>
        </select>
      </div>

      <!-- Max Users -->
      <div>
        <label class="flat-label">
          Max Users
        </label>
        <input
          v-model.number="config.maxUsers"
          type="number"
          min="1"
          max="10000"
          required
          class="flat-input"
        />
      </div>

      <!-- Ramp Up Duration -->
      <div>
        <label class="flat-label">
          Ramp Up Duration (seconds)
        </label>
        <input
          v-model.number="config.rampUpSeconds"
          type="number"
          min="1"
          required
          class="flat-input"
        />
      </div>

      <!-- Test Duration -->
      <div>
        <label class="flat-label">
          Test Duration (seconds)
        </label>
        <input
          v-model.number="config.testDurationSeconds"
          type="number"
          min="1"
          required
          class="flat-input"
        />
      </div>

      <!-- Action Buttons -->
      <div class="flex space-x-2 pt-2">
        <button
          type="submit"
          :disabled="store.loading || store.isTestRunning"
          class="flex-1 flat-btn flat-btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {{ store.loading ? 'Starting...' : 'Start Test' }}
        </button>
        
        <button
          v-if="store.isTestRunning"
          type="button"
          @click="handleStopTest"
          :disabled="store.loading"
          class="flex-1 flat-btn flat-btn-danger disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {{ store.loading ? 'Stopping...' : 'Stop Test' }}
        </button>
      </div>
    </form>

    <!-- Error Display -->
    <div v-if="store.error" class="mt-4 p-3 bg-red-50 border-2 border-red-300">
      <p class="text-sm text-red-600">{{ store.error }}</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { usePerformanceStore } from '@shared/stores/performance'

const store = usePerformanceStore()

// Local config state
// AIDEV-NOTE: 백엔드 TestRequest DTO와 일치하도록 필드명 수정
const config = reactive({
  testName: '',
  planId: '',
  runType: 'TEST',
  maxUsers: 100,
  rampUpSeconds: 60,  // rampUpDurationSeconds -> rampUpSeconds로 수정
  testDurationSeconds: 300,
  scenario: 'BASIC'  // NORMAL_USER -> BASIC으로 수정 (백엔드 허용값)
})

// Handle start test
async function handleStartTest() {
  try {
    store.setTestConfig(config)
    const result = await store.startTest()
    console.log('Test started:', result)
    
    // Reset form
    config.testName = ''
  } catch (err) {
    console.error('Failed to start test:', err)
  }
}

// Handle stop test
async function handleStopTest() {
  if (store.currentTest?.id) {
    try {
      await store.stopTest(store.currentTest.id)
      console.log('Test stopped')
    } catch (err) {
      console.error('Failed to stop test:', err)
    }
  }
}

// Load plans on mount
onMounted(() => {
  if (store.plans.length === 0) {
    store.loadPlans()
  }
})
</script>

<style scoped>
/* 플랫 헤더 스타일 */
.flat-header {
  @apply text-base font-bold text-gray-900 pb-2 mb-2 border-b border-gray-300;
}

/* 플랫 입력 필드 */
.flat-input {
  @apply w-full px-3 py-2 border border-gray-300 bg-white text-gray-900 text-base;  /* text-sm을 text-base로, 패딩 증가 */
  border-radius: 0;
  box-shadow: none;
  transition: border-color 0.2s ease;
}

.flat-input:focus {
  @apply outline-none border-gray-500;
}

/* 플랫 라벨 */
.flat-label {
  @apply block text-sm font-semibold text-gray-600 mb-1 uppercase;  /* text-xs를 text-sm으로 개선 */
}

/* 플랫 버튼 - Danger 스타일 */
.flat-btn {
  @apply px-4 py-2 text-base font-semibold border transition-colors duration-200;  /* 패딩과 폰트 크기 증가 */
  border-radius: 0;
  box-shadow: none;
}

.flat-btn-primary {
  @apply bg-blue-500 text-white border-blue-600 hover:bg-blue-600;
}

.flat-btn-danger {
  @apply bg-red-500 text-white border-red-600 hover:bg-red-600;
}
</style>