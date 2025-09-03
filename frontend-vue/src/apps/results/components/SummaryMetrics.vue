<!-- 
SummaryMetrics 컴포넌트
성공률, 총 요청 수, 평균 응답시간 등 핵심 메트릭 표시

AIDEV-NOTE: 기존 results.html의 메트릭 카드 섹션을 Vue로 전환
플랫 디자인의 카드 형태, 애니메이션 효과 추가
-->
<template>
  <div class="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-6 gap-4 mb-8">
    <!-- 성공률 (Primary 강조) -->
    <div class="metric-card primary group" :class="getSuccessRateClass(testResult.successRate)">
      <div class="text-3xl font-bold text-gray-900 mb-2 font-['Inter'] transition-transform group-hover:scale-110">
        {{ formatPercentage(testResult.successRate) }}
      </div>
      <div class="text-gray-800 font-semibold text-sm font-['Noto Sans KR']">성공률</div>
      <div class="metric-indicator" :class="getSuccessRateIndicator(testResult.successRate)"></div>
    </div>
    
    <!-- 총 요청 수 -->
    <div class="metric-card group">
      <div class="text-3xl font-bold text-gray-900 mb-2 font-['Inter'] transition-transform group-hover:scale-110">
        {{ formatNumber(testResult.totalRequests) }}
      </div>
      <div class="text-gray-800 font-semibold text-sm font-['Noto Sans KR']">총 요청 수</div>
      <i class="metric-icon fas fa-exchange-alt text-blue-500"></i>
    </div>
    
    <!-- 평균 응답시간 -->
    <div class="metric-card group">
      <div class="text-3xl font-bold text-gray-900 mb-2 font-['Inter'] transition-transform group-hover:scale-110">
        {{ formatResponseTime(testResult.avgResponseTime) }}
      </div>
      <div class="text-gray-800 font-semibold text-sm font-['Noto Sans KR']">평균 응답시간</div>
      <i class="metric-icon fas fa-clock text-green-500"></i>
    </div>
    
    <!-- 최대 TPS -->
    <div class="metric-card group">
      <div class="text-3xl font-bold text-gray-900 mb-2 font-['Inter'] transition-transform group-hover:scale-110">
        {{ formatDecimal(testResult.maxTps, 1) }}
      </div>
      <div class="text-gray-800 font-semibold text-sm font-['Noto Sans KR']">최대 TPS</div>
      <i class="metric-icon fas fa-tachometer-alt text-purple-500"></i>
    </div>
    
    <!-- P95 응답시간 -->
    <div class="metric-card">
      <div class="text-2xl font-bold text-gray-900 mb-1">
        {{ formatResponseTime(testResult.p95ResponseTime) }}
      </div>
      <div class="text-gray-600 font-semibold text-xs uppercase">P95 응답시간</div>
    </div>
    
    <!-- 최대 동시 사용자 -->
    <div class="metric-card group">
      <div class="text-3xl font-bold text-gray-900 mb-2 font-['Inter'] transition-transform group-hover:scale-110">
        {{ formatNumber(testResult.maxConcurrentUsers) }}
      </div>
      <div class="text-gray-800 font-semibold text-sm font-['Noto Sans KR']">최대 동시 사용자</div>
      <i class="metric-icon fas fa-users text-indigo-500"></i>
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

// 숫자 포매터
const formatNumber = (num) => {
  if (num == null) return 'N/A'
  return new Intl.NumberFormat('ko-KR').format(num)
}

// 소수점 포매터
const formatDecimal = (num, decimals = 1) => {
  if (num == null) return 'N/A'
  return new Intl.NumberFormat('ko-KR', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  }).format(num)
}

// 백분율 포매터
const formatPercentage = (num) => {
  if (num == null) return 'N/A'
  return formatDecimal(num, 1) + '%'
}

// 응답시간 포매터
const formatResponseTime = (time) => {
  if (time == null) return 'N/A'
  return Math.round(time) + 'ms'
}

// 성공률에 따른 CSS 클래스
const getSuccessRateClass = (rate) => {
  if (rate == null) return ''
  if (rate >= 95) return 'success-excellent'
  if (rate >= 90) return 'success-good' 
  if (rate >= 80) return 'success-warning'
  return 'success-poor'
}

// 성공률 인디케이터
const getSuccessRateIndicator = (rate) => {
  if (rate == null) return 'bg-gray-400'
  if (rate >= 95) return 'bg-green-500'
  if (rate >= 90) return 'bg-blue-500'
  if (rate >= 80) return 'bg-yellow-500'
  return 'bg-red-500'
}

// 통계 요약 계산 (추후 확장용)
const stats = computed(() => ({
  totalRequests: props.testResult.totalRequests || 0,
  successRate: props.testResult.successRate || 0,
  avgResponseTime: props.testResult.avgResponseTime || 0,
  maxTps: props.testResult.maxTps || 0,
  p95ResponseTime: props.testResult.p95ResponseTime || 0,
  maxConcurrentUsers: props.testResult.maxConcurrentUsers || 0
}))
</script>

<style scoped>
/* 플랫 디자인 메트릭 카드 */
.metric-card {
  @apply p-6 text-center bg-white border border-gray-200 relative overflow-hidden;
  transition: all 0.2s ease-in-out;
}

.metric-card:hover {
  @apply border-indigo-400 shadow-sm;
  transform: translateY(-2px);
}

.metric-card.primary {
  @apply border-2 border-indigo-500 border-l-4;
  border-left-color: #6366f1;
  border-left-width: 4px;
}

/* 성공률 상태별 스타일 */
.metric-card.success-excellent {
  @apply border-green-500 bg-green-50;
}

.metric-card.success-good {
  @apply border-blue-500 bg-blue-50;
}

.metric-card.success-warning {
  @apply border-yellow-500 bg-yellow-50;
}

.metric-card.success-poor {
  @apply border-red-500 bg-red-50;
}

/* 메트릭 아이콘 */
.metric-icon {
  @apply absolute top-3 right-3 text-lg opacity-30;
}

/* 메트릭 인디케이터 */
.metric-indicator {
  @apply absolute bottom-0 left-0 right-0 h-1;
}

/* 반응형 조정 */
@media (max-width: 768px) {
  .metric-card {
    @apply p-4;
  }
  
  .text-3xl {
    @apply text-2xl;
  }
  
  .metric-icon {
    @apply text-base;
  }
}

@media (max-width: 1024px) {
  .grid-cols-6 {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .grid-cols-6,
  .md\:grid-cols-3 {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

/* 애니메이션 강화 */
.group:hover .fas {
  animation: pulse 1s ease-in-out;
}

@keyframes pulse {
  0% { opacity: 0.3; }
  50% { opacity: 0.7; }
  100% { opacity: 0.3; }
}

/* 숫자 폰트 최적화 */
.font-mono, 
.font-\[\'Inter\'\] {
  font-variant-numeric: tabular-nums;
}
</style>