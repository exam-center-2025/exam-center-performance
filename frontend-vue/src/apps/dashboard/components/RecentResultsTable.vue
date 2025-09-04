<!--
  최근 테스트 결과 테이블 컴포넌트
  AIDEV-NOTE: 최근 완료된 테스트 결과 표시
-->
<template>
  <Card title="최근 테스트 결과" class="recent-results-table">
    <template #header>
      <div class="flex items-center space-x-2">
        <span class="text-base text-gray-500 bg-gray-50 px-3 py-1 border border-gray-200 rounded">
          최근 {{ results.length }}개
        </span>
      </div>
    </template>

    <!-- 로딩 상태 -->
    <div v-if="loading" class="flex items-center justify-center py-8">
      <div class="loading-spinner"></div>
      <span class="ml-2 text-base text-gray-600">결과를 불러오는 중...</span>
    </div>

    <!-- 빈 상태 -->
    <div v-else-if="!results.length" class="text-center py-8">
      <i class="fas fa-chart-line text-gray-400 text-3xl mb-3"></i>
      <p class="text-gray-500">최근 테스트 결과가 없습니다.</p>
      <p class="text-base text-gray-400 mt-1">테스트를 실행하면 결과가 표시됩니다.</p>
    </div>

    <!-- 결과 목록 -->
    <div v-else class="space-y-3">
      <div 
        v-for="result in results" 
        :key="result.id || result.testId"
        class="border border-gray-200 rounded-lg p-4 hover:bg-gray-50 transition-colors"
      >
        <div class="flex items-center justify-between mb-2">
          <div class="flex items-center space-x-2">
            <h4 class="text-base font-medium text-gray-900">
              {{ result.testName || result.planName || 'Unknown Test' }}
            </h4>
            <span 
              class="inline-flex items-center px-2 py-1 rounded-full text-base font-medium"
              :class="getStatusBadgeClass(result.status)"
            >
              {{ getStatusText(result.status) }}
            </span>
          </div>
          <div class="text-base text-gray-500">
            {{ formatDateTime(result.completedAt || result.endTime) }}
          </div>
        </div>

        <!-- 메트릭 정보 -->
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-base">
          <div>
            <span class="text-gray-500">성공률:</span>
            <span 
              class="ml-1 font-medium"
              :class="getSuccessRateColorClass(result.successRate)"
            >
              {{ formatPercentage(result.successRate) }}
            </span>
          </div>
          <div>
            <span class="text-gray-500">평균 응답시간:</span>
            <span class="ml-1 font-medium text-gray-900">
              {{ formatDuration(result.averageResponseTime) }}
            </span>
          </div>
          <div>
            <span class="text-gray-500">총 요청:</span>
            <span class="ml-1 font-medium text-gray-900">
              {{ formatNumber(result.totalRequests) }}
            </span>
          </div>
          <div>
            <span class="text-gray-500">오류율:</span>
            <span 
              class="ml-1 font-medium"
              :class="getErrorRateColorClass(result.errorRate)"
            >
              {{ formatPercentage(result.errorRate) }}
            </span>
          </div>
        </div>

        <!-- 추가 액션 -->
        <div class="flex items-center justify-end mt-3 pt-3 border-t border-gray-100">
          <div class="flex items-center space-x-2">
            <Button
              @click="viewDetails(result)"
              size="small"
              variant="outline"
            >
              <i class="fas fa-eye mr-1"></i>
              상세보기
            </Button>
            <Button
              @click="downloadReport(result)"
              size="small"
              variant="ghost"
            >
              <i class="fas fa-download mr-1"></i>
              보고서
            </Button>
          </div>
        </div>
      </div>
    </div>
  </Card>
</template>

<script>
export default {
  name: 'RecentResultsTable',
  props: {
    results: {
      type: Array,
      default: () => []
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  
  emits: ['view-details', 'download-report'],
  
  methods: {
    /**
     * 상태 배지 클래스 반환
     */
    getStatusBadgeClass(status) {
      switch (status?.toLowerCase()) {
        case 'completed':
        case 'success':
          return 'bg-green-100 text-green-800'
        case 'failed':
        case 'error':
          return 'bg-red-100 text-red-800'
        case 'cancelled':
        case 'stopped':
          return 'bg-yellow-100 text-yellow-800'
        case 'running':
          return 'bg-blue-100 text-blue-800'
        default:
          return 'bg-gray-100 text-gray-800'
      }
    },

    /**
     * 상태 텍스트 반환
     */
    getStatusText(status) {
      switch (status?.toLowerCase()) {
        case 'completed':
        case 'success':
          return '완료'
        case 'failed':
        case 'error':
          return '실패'
        case 'cancelled':
          return '취소'
        case 'stopped':
          return '중지'
        case 'running':
          return '실행 중'
        default:
          return status || '알 수 없음'
      }
    },

    /**
     * 성공률 색상 클래스 반환
     */
    getSuccessRateColorClass(rate) {
      const numRate = parseFloat(rate) || 0
      if (numRate >= 95) return 'text-green-600'
      if (numRate >= 80) return 'text-yellow-600'
      return 'text-red-600'
    },

    /**
     * 오류율 색상 클래스 반환
     */
    getErrorRateColorClass(rate) {
      const numRate = parseFloat(rate) || 0
      if (numRate <= 1) return 'text-green-600'
      if (numRate <= 5) return 'text-yellow-600'
      return 'text-red-600'
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
      const num = parseInt(value)
      return isNaN(num) ? 'N/A' : num.toLocaleString('ko-KR')
    },

    /**
     * 지속시간 포맷팅
     */
    formatDuration(value) {
      if (value === null || value === undefined) return 'N/A'
      const num = parseFloat(value)
      if (isNaN(num)) return 'N/A'
      
      if (num < 1000) return `${num.toFixed(0)}ms`
      if (num < 60000) return `${(num / 1000).toFixed(1)}s`
      return `${(num / 60000).toFixed(1)}m`
    },

    /**
     * 날짜시간 포맷팅
     */
    formatDateTime(dateString) {
      if (!dateString) return 'N/A'
      
      try {
        const date = new Date(dateString)
        return date.toLocaleString('ko-KR', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        })
      } catch (error) {
        return 'Invalid Date'
      }
    },

    /**
     * 상세보기 처리
     */
    viewDetails(result) {
      this.$emit('view-details', result)
    },

    /**
     * 보고서 다운로드 처리
     */
    downloadReport(result) {
      this.$emit('download-report', result)
    }
  }
}
</script>

<style scoped>
.recent-results-table {
  border-left: 4px solid #8b5cf6;
}
</style>