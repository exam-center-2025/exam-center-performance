<template>
  <div class="bg-white border border-gray-200 rounded-lg p-6">
    <h3 class="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
      <i class="fas fa-chart-line"></i>
      설정 미리보기
    </h3>
    
    <div class="bg-gray-50 border border-gray-200 rounded-md p-4">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <!-- 예상 최대 부하 -->
        <div class="flex flex-col items-center p-3 bg-white rounded border">
          <div class="text-2xl font-bold text-blue-600 mb-1">
            {{ metrics.maxLoad }}
          </div>
          <div class="text-sm text-gray-600 text-center">
            예상 최대 부하
            <br>
            <span class="text-xs text-gray-500">TPS</span>
          </div>
        </div>
        
        <!-- 총 예상 요청 수 -->
        <div class="flex flex-col items-center p-3 bg-white rounded border">
          <div class="text-2xl font-bold text-green-600 mb-1">
            {{ formatNumber(metrics.totalRequests) }}
          </div>
          <div class="text-sm text-gray-600 text-center">
            총 예상 요청 수
            <br>
            <span class="text-xs text-gray-500">requests</span>
          </div>
        </div>
        
        <!-- 예상 소요 시간 -->
        <div class="flex flex-col items-center p-3 bg-white rounded border">
          <div class="text-2xl font-bold text-purple-600 mb-1">
            {{ metrics.duration }}
          </div>
          <div class="text-sm text-gray-600 text-center">
            예상 소요 시간
            <br>
            <span class="text-xs text-gray-500">램프업 + 테스트</span>
          </div>
        </div>
      </div>
      
      <!-- 상세 정보 -->
      <div class="mt-4 pt-4 border-t border-gray-200">
        <div class="grid grid-cols-2 gap-4 text-sm">
          <div class="space-y-2">
            <div class="flex justify-between">
              <span class="text-gray-600">사용자당 평균 TPS:</span>
              <span class="font-medium">0.5</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">평균 부하 비율:</span>
              <span class="font-medium">70%</span>
            </div>
          </div>
          <div class="space-y-2">
            <div class="flex justify-between">
              <span class="text-gray-600">램프업 단계:</span>
              <span class="font-medium">{{ testRequest.rampUpSeconds }}초</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">유지 단계:</span>
              <span class="font-medium">{{ testRequest.testDurationSeconds }}초</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 경고 메시지 -->
      <div v-if="showHighLoadWarning" class="mt-4 p-3 bg-yellow-50 border border-yellow-200 rounded">
        <div class="flex items-start">
          <i class="fas fa-exclamation-triangle text-yellow-500 mt-0.5 mr-2"></i>
          <div class="text-sm text-yellow-700">
            <strong>높은 부하 경고:</strong>
            설정된 부하가 매우 높습니다. 시스템 안정성을 위해 점진적으로 테스트하는 것을 권장합니다.
          </div>
        </div>
      </div>
      
      <div v-if="showLongDurationWarning" class="mt-4 p-3 bg-orange-50 border border-orange-200 rounded">
        <div class="flex items-start">
          <i class="fas fa-clock text-orange-500 mt-0.5 mr-2"></i>
          <div class="text-sm text-orange-700">
            <strong>장시간 테스트:</strong>
            테스트 시간이 길어 리소스 사용량이 많을 수 있습니다. 모니터링을 지속적으로 확인하세요.
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useTestConfig } from '@shared/composables/useTestConfig.js'

// AIDEV-NOTE: 설정 미리보기 컴포넌트 - 실시간 계산된 메트릭스 표시
const {
  testRequest,
  metrics
} = useTestConfig()

// 숫자 포맷팅
const formatNumber = (num) => {
  return new Intl.NumberFormat('ko-KR').format(num)
}

// 높은 부하 경고 표시 조건
const showHighLoadWarning = computed(() => {
  return testRequest.maxUsers > 500 || metrics.value.maxLoad > 250
})

// 장시간 테스트 경고 표시 조건
const showLongDurationWarning = computed(() => {
  const totalSeconds = testRequest.rampUpSeconds + testRequest.testDurationSeconds
  return totalSeconds > 1800 // 30분 이상
})
</script>

<style scoped>
/* 메트릭 카드 애니메이션 */
.bg-white {
  transition: all 0.3s ease;
}

.bg-white:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

/* 숫자 강조 효과 */
.text-2xl {
  font-family: 'Courier New', monospace;
  letter-spacing: -0.5px;
}
</style>