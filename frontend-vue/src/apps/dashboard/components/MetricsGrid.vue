<!--
  메트릭 그리드 컴포넌트
  AIDEV-NOTE: 대시보드 상단의 요약 메트릭 카드들
-->
<template>
  <div class="metrics-grid grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
    <!-- 시험 계획 수 -->
    <div class="metric-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <div class="flex items-center justify-between">
        <div>
          <div class="metric-value text-2xl font-bold text-gray-900">
            {{ plansCount }}
          </div>
          <div class="metric-label text-sm text-gray-500 mt-1">
            시험 계획
          </div>
        </div>
        <div class="metric-icon text-blue-500">
          <i class="fas fa-calendar-alt text-2xl"></i>
        </div>
      </div>
    </div>

    <!-- 실행 중인 테스트 수 -->
    <div class="metric-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <div class="flex items-center justify-between">
        <div>
          <div class="metric-value text-2xl font-bold text-gray-900">
            {{ activeTestsCount }}
          </div>
          <div class="metric-label text-sm text-gray-500 mt-1">
            실행 중인 테스트
          </div>
        </div>
        <div class="metric-icon text-green-500">
          <i class="fas fa-play-circle text-2xl"></i>
        </div>
      </div>
      
      <!-- 상태 표시 -->
      <div class="mt-3">
        <div 
          v-if="activeTestsCount > 0" 
          class="flex items-center text-green-600 text-xs font-medium"
        >
          <div class="w-2 h-2 bg-green-500 rounded-full mr-2 animate-pulse"></div>
          진행 중
        </div>
        <div 
          v-else 
          class="flex items-center text-gray-400 text-xs"
        >
          <div class="w-2 h-2 bg-gray-400 rounded-full mr-2"></div>
          대기 중
        </div>
      </div>
    </div>

    <!-- 최근 완료 수 -->
    <div class="metric-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <div class="flex items-center justify-between">
        <div>
          <div class="metric-value text-2xl font-bold text-gray-900">
            {{ recentResultsCount }}
          </div>
          <div class="metric-label text-sm text-gray-500 mt-1">
            최근 완료
          </div>
        </div>
        <div class="metric-icon text-purple-500">
          <i class="fas fa-chart-line text-2xl"></i>
        </div>
      </div>
      
      <!-- 추가 정보 -->
      <div class="mt-3">
        <div class="text-xs text-gray-400">
          지난 24시간
        </div>
      </div>
    </div>

    <!-- 평균 성공률 -->
    <div class="metric-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <div class="flex items-center justify-between">
        <div>
          <div class="metric-value text-2xl font-bold text-gray-900">
            {{ successRateDisplay }}
          </div>
          <div class="metric-label text-sm text-gray-500 mt-1">
            평균 성공률
          </div>
        </div>
        <div class="metric-icon" :class="successRateColorClass">
          <i class="fas fa-percentage text-2xl"></i>
        </div>
      </div>
      
      <!-- 성공률 바 -->
      <div class="mt-3">
        <div class="w-full bg-gray-200 rounded-full h-2">
          <div 
            class="h-2 rounded-full transition-all duration-300"
            :class="successRateBarClass"
            :style="{ width: `${successRate}%` }"
          ></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MetricsGrid',
  props: {
    plansCount: {
      type: Number,
      default: 0
    },
    activeTestsCount: {
      type: Number,
      default: 0
    },
    recentResultsCount: {
      type: Number,
      default: 0
    },
    successRate: {
      type: Number,
      default: 0
    }
  },
  
  computed: {
    successRateDisplay() {
      return this.successRate > 0 ? `${this.successRate}%` : 'N/A'
    },
    
    successRateColorClass() {
      if (this.successRate >= 90) return 'text-green-500'
      if (this.successRate >= 70) return 'text-yellow-500'
      if (this.successRate >= 50) return 'text-orange-500'
      if (this.successRate > 0) return 'text-red-500'
      return 'text-gray-400'
    },
    
    successRateBarClass() {
      if (this.successRate >= 90) return 'bg-green-500'
      if (this.successRate >= 70) return 'bg-yellow-500'
      if (this.successRate >= 50) return 'bg-orange-500'
      if (this.successRate > 0) return 'bg-red-500'
      return 'bg-gray-300'
    }
  }
}
</script>

<style scoped>
.metric-card {
  transition: transform 0.2s ease-in-out;
}

.metric-card:hover {
  transform: translateY(-2px);
}

.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: .5;
  }
}
</style>