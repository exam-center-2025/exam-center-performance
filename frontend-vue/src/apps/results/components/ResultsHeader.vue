<!-- 
ResultsHeader 컴포넌트
테스트 ID, 시작/종료 시간, 소요시간 표시

AIDEV-NOTE: 기존 results.html의 헤더 섹션을 Vue로 전환
플랫 디자인 스타일 유지, 다크 테마 적용
-->
<template>
  <div class="bg-white border border-gray-200 p-4 mb-4">
    <!-- 테스트 ID 표시 -->
    <div class="text-lg font-bold mb-3 text-gray-900">
      <i class="fas fa-chart-bar mr-2 text-gray-600"></i>
      <span>테스트 ID:</span>
      <span class="font-mono text-blue-600 ml-2">{{ testResult.testId || 'N/A' }}</span>
    </div>
    
    <!-- 시간 정보 그리드 -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-2 mt-3 text-sm">
      <!-- 시작 시간 -->
      <div class="flex items-center gap-2 bg-gray-50 p-2 border border-gray-200">
        <i class="fas fa-calendar text-green-600 text-sm"></i>
        <div>
          <div class="text-gray-600 text-xs font-medium uppercase">시작</div>
          <div class="font-mono text-gray-900">
            {{ formatDateTime(testResult.startTime) }}
          </div>
        </div>
      </div>
      
      <!-- 종료 시간 -->
      <div class="flex items-center gap-2 bg-gray-50 p-2 border border-gray-200">
        <i class="fas fa-clock text-red-600 text-sm"></i>
        <div>
          <div class="text-gray-600 text-xs font-medium uppercase">종료</div>
          <div class="font-mono text-gray-900">
            {{ formatDateTime(testResult.endTime) }}
          </div>
        </div>
      </div>
      
      <!-- 소요 시간 -->
      <div class="flex items-center gap-2 bg-gray-50 p-2 border border-gray-200">
        <i class="fas fa-hourglass-half text-yellow-600 text-sm"></i>
        <div>
          <div class="text-gray-600 text-xs font-medium uppercase">소요시간</div>
          <div class="font-mono font-semibold text-gray-900">
            {{ formatDuration(testResult.actualDurationSeconds) }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

// Props 정의
const props = defineProps({
  testResult: {
    type: Object,
    required: true,
    default: () => ({})
  }
})

// 날짜시간 포매터
const formatDateTime = (dateTime) => {
  if (!dateTime) return 'N/A'
  
  try {
    const date = new Date(dateTime)
    if (isNaN(date.getTime())) return 'Invalid Date'
    
    return new Intl.DateTimeFormat('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit', 
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    }).format(date).replace(/\. /g, '-').replace('.', '')
  } catch (error) {
    console.warn('날짜 포맷팅 오류:', error)
    return 'N/A'
  }
}

// 소요시간 포매터 (초 → 시:분:초 형식)
const formatDuration = (seconds) => {
  if (seconds == null || seconds < 0) return 'N/A'
  
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const remainingSeconds = seconds % 60
  
  if (hours > 0) {
    return `${hours}시간 ${minutes}분 ${remainingSeconds}초`
  } else if (minutes > 0) {
    return `${minutes}분 ${remainingSeconds}초`  
  } else {
    return `${seconds}초`
  }
}

// 상태별 스타일 계산
const statusColor = computed(() => {
  const status = props.testResult.status?.toLowerCase()
  switch (status) {
    case 'completed':
      return 'text-green-300'
    case 'failed':
      return 'text-red-300'
    case 'running':
      return 'text-blue-300'
    default:
      return 'text-gray-300'
  }
})
</script>

<style scoped>
/* AIDEV-NOTE: 플랫 디자인으로 변경 - 대시보드 스타일 일치 */

/* 반응형 조정 */
@media (max-width: 768px) {
  .text-lg {
    @apply text-base;
  }
  
  .p-4 {
    @apply p-3;
  }
  
  .gap-2 {
    @apply gap-1;
  }
}

/* 폰트 최적화 */
.font-mono {
  font-variant-numeric: tabular-nums;
}
</style>