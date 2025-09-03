<template>
  <div class="bg-white border border-gray-200 rounded-lg p-6">
    <h3 class="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
      <i class="fas fa-cogs"></i>
      기본 설정
    </h3>
    
    <div class="space-y-4">
      <!-- 시험 계획 선택 -->
      <div>
        <label for="planId" class="block text-sm font-medium text-gray-700 mb-2">
          시험 계획 *
        </label>
        <select
          id="planId"
          v-model="testRequest.planId"
          :class="[
            'w-full border px-3 py-2 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent',
            errors.planId ? 'border-red-300' : 'border-gray-300'
          ]"
          required
        >
          <option value="">시험 계획을 선택하세요</option>
          <option
            v-for="plan in examPlans"
            :key="plan.planId"
            :value="plan.planId"
          >
            {{ plan.planId }} - {{ plan.name }}
          </option>
        </select>
        <p class="text-xs text-gray-500 mt-1">
          성능 테스트를 실행할 시험 계획을 선택하세요.
        </p>
        <p v-if="errors.planId" class="text-xs text-red-600 mt-1">
          {{ errors.planId }}
        </p>
      </div>
      
      <!-- 실행 타입 -->
      <div>
        <label for="runType" class="block text-sm font-medium text-gray-700 mb-2">
          실행 타입 *
        </label>
        <select
          id="runType"
          v-model="testRequest.runType"
          class="w-full border border-gray-300 px-3 py-2 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          required
        >
          <option
            v-for="runType in runTypes"
            :key="runType.value"
            :value="runType.value"
          >
            {{ runType.label }}
          </option>
        </select>
        <p class="text-xs text-gray-500 mt-1">
          테스트 환경 타입을 선택하세요.
        </p>
      </div>
      
      <!-- 테스트 이름 -->
      <div>
        <label for="testName" class="block text-sm font-medium text-gray-700 mb-2">
          테스트 이름 (선택)
        </label>
        <input
          id="testName"
          v-model="testRequest.testName"
          type="text"
          class="w-full border border-gray-300 px-3 py-2 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          placeholder="예: 정기시험_부하테스트_2025"
        >
        <p class="text-xs text-gray-500 mt-1">
          식별하기 쉬운 테스트 이름을 입력하세요.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useTestConfig } from '@shared/composables/useTestConfig.js'

// AIDEV-NOTE: 기본 설정 폼 컴포넌트 - 시험 계획, 실행 타입, 테스트 이름
const {
  testRequest,
  examPlans,
  runTypes,
  errors
} = useTestConfig()
</script>

<style scoped>
/* 포커스 상태 스타일 개선 */
select:focus,
input:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* 에러 상태 스타일 */
.border-red-300:focus {
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}
</style>