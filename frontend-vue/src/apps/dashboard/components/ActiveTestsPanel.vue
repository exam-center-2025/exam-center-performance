<!--
  실행 중인 테스트 패널 컴포넌트
  AIDEV-NOTE: 현재 실행 중인 테스트들의 실시간 상태 표시
-->
<template>
  <Card title="실행 중인 테스트" class="active-tests-panel">
    <template #header>
      <div class="flex items-center space-x-3">
        <div class="flex items-center space-x-2">
          <div class="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
          <span class="text-sm text-green-600 font-medium">진행 중</span>
        </div>
        <span class="text-xs text-gray-500 bg-gray-50 px-3 py-1 border border-gray-200 rounded">
          {{ activeTests.length }}개 실행 중
        </span>
      </div>
    </template>

    <div class="space-y-4">
      <div 
        v-for="test in activeTests" 
        :key="test.id || test.testId"
        class="border rounded-lg p-4 bg-white"
        :class="{ 'ring-2 ring-blue-500': isCurrentTest(test) }"
      >
        <!-- 테스트 헤더 -->
        <div class="flex items-center justify-between mb-3">
          <div class="flex items-center space-x-2">
            <h4 class="text-sm font-semibold text-gray-900">
              {{ test.testName || test.planName || `Test ${test.id}` }}
            </h4>
            <span class="text-xs text-gray-500 bg-gray-100 px-2 py-1 rounded">
              ID: {{ test.id || test.testId }}
            </span>
            <button
              @click="selectTest(test)"
              class="text-xs text-blue-600 hover:text-blue-800 font-medium"
              :class="{ 'text-blue-800 font-semibold': isCurrentTest(test) }"
            >
              {{ isCurrentTest(test) ? '선택됨' : '선택' }}
            </button>
          </div>
          
          <div class="flex items-center space-x-2">
            <span class="text-xs text-gray-500">
              {{ formatDuration(test.elapsedTime || test.runningTime) }}
            </span>
            <Button
              @click="stopTest(test)"
              size="small"
              variant="danger"
            >
              <i class="fas fa-stop mr-1"></i>
              중지
            </Button>
          </div>
        </div>

        <!-- 진행률 바 -->
        <div v-if="test.progress !== undefined" class="mb-3">
          <div class="flex items-center justify-between text-xs text-gray-600 mb-1">
            <span>진행률</span>
            <span>{{ Math.round(test.progress || 0) }}%</span>
          </div>
          <div class="w-full bg-gray-200 rounded-full h-2">
            <div 
              class="bg-blue-600 h-2 rounded-full transition-all duration-300"
              :style="{ width: `${test.progress || 0}%` }"
            ></div>
          </div>
        </div>

        <!-- 실시간 메트릭 -->
        <div class="grid grid-cols-2 md:grid-cols-4 gap-3 text-sm">
          <div class="text-center p-2 bg-gray-50 rounded">
            <div class="text-xs text-gray-500 mb-1">가상 사용자</div>
            <div class="font-semibold text-gray-900">
              {{ test.virtualUsers || test.currentUsers || 'N/A' }}
            </div>
          </div>
          
          <div class="text-center p-2 bg-gray-50 rounded">
            <div class="text-xs text-gray-500 mb-1">응답시간</div>
            <div class="font-semibold" :class="getResponseTimeColorClass(test.avgResponseTime)">
              {{ formatResponseTime(test.avgResponseTime || test.averageResponseTime) }}
            </div>
          </div>
          
          <div class="text-center p-2 bg-gray-50 rounded">
            <div class="text-xs text-gray-500 mb-1">성공률</div>
            <div class="font-semibold" :class="getSuccessRateColorClass(test.successRate)">
              {{ formatPercentage(test.successRate) }}
            </div>
          </div>
          
          <div class="text-center p-2 bg-gray-50 rounded">
            <div class="text-xs text-gray-500 mb-1">TPS</div>
            <div class="font-semibold text-gray-900">
              {{ formatNumber(test.tps || test.requestsPerSecond) }}
            </div>
          </div>
        </div>

        <!-- 추가 정보 -->
        <div class="mt-3 pt-3 border-t border-gray-100">
          <div class="flex items-center justify-between text-xs text-gray-500">
            <span>시작: {{ formatDateTime(test.startTime) }}</span>
            <span>예상 종료: {{ formatDateTime(test.estimatedEndTime) }}</span>
          </div>
        </div>
      </div>
    </div>
  </Card>
</template>

<script>
export default {
  name: 'ActiveTestsPanel',
  props: {
    activeTests: {
      type: Array,
      default: () => []
    },
    currentTest: {
      type: Object,
      default: null
    }
  },
  
  emits: ['stop-test', 'select-test'],
  
  methods: {
    /**
     * 현재 선택된 테스트인지 확인
     */
    isCurrentTest(test) {
      return this.currentTest && 
             (this.currentTest.id === test.id || this.currentTest.testId === test.testId)
    },

    /**
     * 테스트 선택
     */
    selectTest(test) {
      this.$emit('select-test', test)
    },

    /**
     * 테스트 중지
     */
    stopTest(test) {
      this.$emit('stop-test', test.id || test.testId)
    },

    /**
     * 응답시간 색상 클래스
     */
    getResponseTimeColorClass(responseTime) {
      const time = parseFloat(responseTime) || 0
      if (time <= 500) return 'text-green-600'
      if (time <= 1000) return 'text-yellow-600'
      return 'text-red-600'
    },

    /**
     * 성공률 색상 클래스
     */
    getSuccessRateColorClass(successRate) {
      const rate = parseFloat(successRate) || 0
      if (rate >= 95) return 'text-green-600'
      if (rate >= 80) return 'text-yellow-600'
      return 'text-red-600'
    },

    /**
     * 지속시간 포맷팅
     */
    formatDuration(milliseconds) {
      if (!milliseconds) return '00:00'
      
      const seconds = Math.floor(milliseconds / 1000)
      const minutes = Math.floor(seconds / 60)
      const hours = Math.floor(minutes / 60)
      
      if (hours > 0) {
        return `${hours.toString().padStart(2, '0')}:${(minutes % 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}`
      }
      
      return `${minutes.toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}`
    },

    /**
     * 응답시간 포맷팅
     */
    formatResponseTime(value) {
      if (value === null || value === undefined) return 'N/A'
      const num = parseFloat(value)
      return isNaN(num) ? 'N/A' : `${num.toFixed(0)}ms`
    },

    /**
     * 백분율 포맷팅
     */
    formatPercentage(value) {
      if (value === null || value === undefined) return 'N/A'
      const num = parseFloat(value)
      return isNaN(num) ? 'N/A' : `${num.toFixed(1)}%`
    },

    /**
     * 숫자 포맷팅
     */
    formatNumber(value) {
      if (value === null || value === undefined) return 'N/A'
      const num = parseFloat(value)
      return isNaN(num) ? 'N/A' : num.toFixed(1)
    },

    /**
     * 날짜시간 포맷팅
     */
    formatDateTime(dateString) {
      if (!dateString) return 'N/A'
      
      try {
        const date = new Date(dateString)
        return date.toLocaleTimeString('ko-KR', {
          hour: '2-digit',
          minute: '2-digit'
        })
      } catch (error) {
        return 'N/A'
      }
    }
  }
}
</script>

<style scoped>
.active-tests-panel {
  border-left: 4px solid #ef4444;
}
</style>