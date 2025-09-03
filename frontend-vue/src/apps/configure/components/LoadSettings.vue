<template>
  <div class="bg-white border border-gray-200 rounded-lg p-6">
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-lg font-semibold text-gray-800 flex items-center gap-2">
        <i class="fas fa-users"></i>
        부하 설정
      </h3>
      
      <!-- 프리셋 버튼들 -->
      <div class="flex gap-2">
        <button
          @click="applyPreset('LOW')"
          type="button"
          class="px-3 py-1 text-xs border border-gray-300 rounded hover:bg-gray-50 transition-colors"
        >
          Low
        </button>
        <button
          @click="applyPreset('MEDIUM')"
          type="button"
          class="px-3 py-1 text-xs border border-gray-300 rounded hover:bg-gray-50 transition-colors"
        >
          Medium
        </button>
        <button
          @click="applyPreset('HIGH')"
          type="button"
          class="px-3 py-1 text-xs border border-gray-300 rounded hover:bg-gray-50 transition-colors"
        >
          High
        </button>
      </div>
    </div>
    
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <!-- 최대 사용자 수 -->
      <div>
        <label for="maxUsers" class="block text-sm font-medium text-gray-700 mb-2">
          최대 사용자 수 *
        </label>
        <input
          id="maxUsers"
          v-model.number="testRequest.maxUsers"
          type="number"
          min="1"
          max="10000"
          step="1"
          :class="[
            'w-full border px-3 py-2 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent',
            errors.maxUsers ? 'border-red-300' : 'border-gray-300'
          ]"
          required
        >
        <p class="text-xs text-gray-500 mt-1">동시 접속할 최대 사용자 수</p>
        <p v-if="errors.maxUsers" class="text-xs text-red-600 mt-1">
          {{ errors.maxUsers }}
        </p>
      </div>
      
      <!-- 램프업 시간 -->
      <div>
        <label for="rampUpSeconds" class="block text-sm font-medium text-gray-700 mb-2">
          램프업 시간 (초) *
        </label>
        <input
          id="rampUpSeconds"
          v-model.number="testRequest.rampUpSeconds"
          type="number"
          min="0"
          max="3600"
          step="1"
          :class="[
            'w-full border px-3 py-2 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent',
            errors.rampUpSeconds ? 'border-red-300' : 'border-gray-300'
          ]"
          required
        >
        <p class="text-xs text-gray-500 mt-1">최대 사용자까지 도달하는 시간</p>
        <p v-if="errors.rampUpSeconds" class="text-xs text-red-600 mt-1">
          {{ errors.rampUpSeconds }}
        </p>
      </div>
      
      <!-- 테스트 지속시간 -->
      <div>
        <label for="testDurationSeconds" class="block text-sm font-medium text-gray-700 mb-2">
          테스트 지속시간 (초) *
        </label>
        <input
          id="testDurationSeconds"
          v-model.number="testRequest.testDurationSeconds"
          type="number"
          min="60"
          max="7200"
          step="1"
          :class="[
            'w-full border px-3 py-2 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent',
            errors.testDurationSeconds ? 'border-red-300' : 'border-gray-300'
          ]"
          required
        >
        <p class="text-xs text-gray-500 mt-1">테스트를 실행할 총 시간</p>
        <p v-if="errors.testDurationSeconds" class="text-xs text-red-600 mt-1">
          {{ errors.testDurationSeconds }}
        </p>
      </div>
    </div>
    
    <!-- 프리셋 정보 툴팁 -->
    <div class="mt-4 p-3 bg-blue-50 border border-blue-200 rounded-md">
      <div class="flex items-start">
        <i class="fas fa-lightbulb text-blue-500 mt-0.5 mr-2"></i>
        <div class="text-sm text-blue-700">
          <strong>부하 프리셋:</strong>
          <span class="block mt-1">
            Low (50명/30초/3분) • Medium (200명/1분/5분) • High (500명/2분/10분)
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useTestConfig } from '@shared/composables/useTestConfig.js'

// AIDEV-NOTE: 부하 설정 폼 컴포넌트 - 사용자 수, 램프업, 지속시간 + 프리셋 기능
const {
  testRequest,
  errors,
  applyLoadPreset
} = useTestConfig()

// 프리셋 적용
const applyPreset = (presetType) => {
  applyLoadPreset(presetType)
}
</script>

<style scoped>
/* 숫자 입력 필드 스타일 */
input[type="number"] {
  -moz-appearance: textfield;
}

input[type="number"]::-webkit-outer-spin-button,
input[type="number"]::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

/* 프리셋 버튼 호버 효과 */
button:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>