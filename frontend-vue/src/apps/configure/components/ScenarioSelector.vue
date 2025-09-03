<template>
  <div class="bg-white border border-gray-200 rounded-lg p-6">
    <h3 class="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
      <i class="fas fa-route"></i>
      테스트 시나리오
    </h3>
    
    <div class="space-y-3">
      <div
        v-for="scenario in scenarios"
        :key="scenario.value"
        :class="[
          'border rounded-lg p-4 cursor-pointer transition-all duration-200',
          'hover:border-blue-300 hover:shadow-sm',
          testRequest.scenario === scenario.value 
            ? 'border-blue-500 bg-blue-50 shadow-sm' 
            : 'border-gray-200 bg-white'
        ]"
        @click="selectScenario(scenario.value)"
      >
        <label class="flex items-start gap-3 cursor-pointer">
          <input
            type="radio"
            :value="scenario.value"
            v-model="testRequest.scenario"
            class="mt-1 w-4 h-4 text-blue-600 border-gray-300 focus:ring-blue-500"
          >
          
          <div class="flex-1">
            <div class="flex items-center justify-between mb-2">
              <div class="font-medium text-gray-900">
                {{ scenario.label }}
              </div>
              <div class="flex items-center gap-2">
                <!-- 시나리오별 아이콘 -->
                <i
                  :class="getScenarioIcon(scenario.value)"
                  class="text-sm"
                ></i>
                <!-- 난이도 표시 -->
                <div class="flex">
                  <span
                    v-for="level in getScenarioDifficulty(scenario.value)"
                    :key="level"
                    class="w-2 h-2 rounded-full mr-1"
                    :class="level <= getDifficultyLevel(scenario.value) 
                      ? 'bg-orange-400' 
                      : 'bg-gray-200'"
                  ></span>
                </div>
              </div>
            </div>
            
            <div class="text-sm text-gray-600 mb-3">
              {{ scenario.description }}
            </div>
            
            <!-- 시나리오별 특성 표시 -->
            <div class="flex flex-wrap gap-2">
              <span
                v-for="feature in getScenarioFeatures(scenario.value)"
                :key="feature"
                class="inline-flex items-center px-2 py-1 text-xs font-medium rounded"
                :class="testRequest.scenario === scenario.value 
                  ? 'bg-blue-100 text-blue-700' 
                  : 'bg-gray-100 text-gray-600'"
              >
                {{ feature }}
              </span>
            </div>
            
            <!-- 예상 부하 정보 -->
            <div class="mt-3 pt-3 border-t border-gray-100">
              <div class="text-xs text-gray-500">
                예상 부하: 
                <span class="font-medium">{{ getScenarioLoadInfo(scenario.value) }}</span>
              </div>
            </div>
          </div>
        </label>
      </div>
    </div>
    
    <!-- 시나리오 상세 정보 -->
    <div v-if="selectedScenarioDetails" class="mt-6 p-4 bg-gray-50 border border-gray-200 rounded-lg">
      <h4 class="font-medium text-gray-900 mb-2 flex items-center gap-2">
        <i class="fas fa-info-circle text-blue-500"></i>
        선택된 시나리오 상세
      </h4>
      <div class="text-sm text-gray-600 space-y-2">
        <p><strong>테스트 범위:</strong> {{ selectedScenarioDetails.scope }}</p>
        <p><strong>주요 기능:</strong> {{ selectedScenarioDetails.features }}</p>
        <p><strong>권장 사용:</strong> {{ selectedScenarioDetails.recommendation }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useTestConfig } from '@shared/composables/useTestConfig.js'

// AIDEV-NOTE: 테스트 시나리오 선택 컴포넌트 - 시각적 선택 및 상세 정보 제공
const {
  testRequest,
  scenarios
} = useTestConfig()

// 시나리오 선택
const selectScenario = (scenarioValue) => {
  testRequest.scenario = scenarioValue
}

// 시나리오별 아이콘 반환
const getScenarioIcon = (scenario) => {
  switch (scenario) {
    case 'BASIC':
      return 'fas fa-play-circle text-green-500'
    case 'COMPLETE':
      return 'fas fa-check-circle text-blue-500'
    case 'STRESS':
      return 'fas fa-fire text-red-500'
    default:
      return 'fas fa-circle text-gray-500'
  }
}

// 시나리오별 난이도 레벨 반환 (1-5)
const getDifficultyLevel = (scenario) => {
  switch (scenario) {
    case 'BASIC': return 2
    case 'COMPLETE': return 4
    case 'STRESS': return 5
    default: return 1
  }
}

// 난이도 표시용 배열
const getScenarioDifficulty = () => [1, 2, 3, 4, 5]

// 시나리오별 특성 반환
const getScenarioFeatures = (scenario) => {
  switch (scenario) {
    case 'BASIC':
      return ['로그인', '기본조회', '가벼운부하']
    case 'COMPLETE':
      return ['모든기능', '실제워크플로우', '표준부하']
    case 'STRESS':
      return ['한계테스트', '고부하', '안정성검증']
    default:
      return []
  }
}

// 시나리오별 부하 정보
const getScenarioLoadInfo = (scenario) => {
  switch (scenario) {
    case 'BASIC':
      return '낮음 (시스템 부하 30%)'
    case 'COMPLETE':
      return '보통 (시스템 부하 70%)'
    case 'STRESS':
      return '높음 (시스템 부하 100%+)'
    default:
      return '알 수 없음'
  }
}

// 선택된 시나리오의 상세 정보
const selectedScenarioDetails = computed(() => {
  const scenario = scenarios.value.find(s => s.value === testRequest.scenario)
  if (!scenario) return null
  
  const details = {
    'BASIC': {
      scope: '핵심 기능만 포함된 최소한의 테스트',
      features: '로그인, 기본 조회, 간단한 CRUD 작업',
      recommendation: '초기 성능 검증, 빠른 테스트가 필요할 때'
    },
    'COMPLETE': {
      scope: '실제 사용자 워크플로우를 반영한 포괄적인 테스트',
      features: '모든 주요 기능, 복합 업무 플로우, 파일 업로드/다운로드',
      recommendation: '정기적인 성능 검증, 배포 전 검증'
    },
    'STRESS': {
      scope: '시스템 한계 및 안정성을 검증하는 고강도 테스트',
      features: '최대 부하, 동시 대량 처리, 장시간 연속 실행',
      recommendation: '시스템 한계 확인, 장애 시뮬레이션'
    }
  }
  
  return details[scenario.value] || null
})
</script>

<style scoped>
/* 라디오 버튼 선택 애니메이션 */
input[type="radio"] {
  transition: all 0.2s ease;
}

/* 시나리오 카드 선택 효과 */
.border-blue-500 {
  animation: pulse-blue 2s infinite;
}

@keyframes pulse-blue {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(59, 130, 246, 0.4);
  }
  50% {
    box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
  }
}

/* 난이도 표시 애니메이션 */
.bg-orange-400 {
  animation: fill 0.3s ease forwards;
}

@keyframes fill {
  from {
    transform: scale(0);
  }
  to {
    transform: scale(1);
  }
}

/* 특성 태그 호버 효과 */
.bg-gray-100:hover {
  background-color: #e5e7eb;
  transform: scale(1.05);
}

.bg-blue-100:hover {
  background-color: #dbeafe;
  transform: scale(1.05);
}
</style>