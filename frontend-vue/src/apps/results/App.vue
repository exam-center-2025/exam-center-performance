<!-- 
Results 페이지 메인 앱 컴포넌트
완료된 테스트의 결과를 상세하게 표시

AIDEV-NOTE: 기존 results.html의 모든 기능을 Vue 3로 전환
- 테스트 결과 표시, 차트, 상세 정보, Gatling 리포트 링크
-->
<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 페이지 헤더 -->
    <div class="flex justify-between items-center mb-4">
      <h1 class="text-2xl font-bold">테스트 결과 상세</h1>
      <a href="/performance/dashboard" class="btn btn-secondary btn-sm">
        <i class="fas fa-arrow-left"></i> 대시보드로
      </a>
    </div>
    
    <!-- 로딩 상태 -->
    <div v-if="loading" class="space-y-8">
      <div class="bg-gray-200 animate-pulse h-32 border border-gray-300"></div>
      <div class="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-6 gap-4">
        <div v-for="i in 6" :key="i" class="bg-gray-200 animate-pulse h-24 border border-gray-300"></div>
      </div>
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div class="bg-gray-200 animate-pulse h-80 border border-gray-300"></div>
        <div class="bg-gray-200 animate-pulse h-80 border border-gray-300"></div>
      </div>
    </div>
    
    <!-- 에러 상태 -->
    <div v-else-if="error" class="bg-red-50 border border-red-200 p-4 text-center">
      <i class="fas fa-exclamation-triangle text-red-500 text-3xl mb-4"></i>
      <h3 class="text-lg font-semibold text-red-800 mb-2">결과 로드 실패</h3>
      <p class="text-red-600 mb-4">{{ error }}</p>
      <button @click="loadResults" class="btn btn-primary">
        <i class="fas fa-redo mr-2"></i>다시 시도
      </button>
    </div>
    
    <!-- 메인 콘텐츠 -->
    <div v-else-if="hasData">
      <!-- 결과 헤더 -->
      <ResultsHeader :test-result="testResult" />
      
      <!-- 요약 메트릭 -->
      <SummaryMetrics :test-result="testResult" />
      
      <!-- 차트 섹션 -->
      <ResultCharts 
        :metrics-history="metricsHistory" 
        :loading="chartsLoading" 
      />
      
      <!-- 상세 정보 테이블 -->
      <DetailTable :test-result="testResult" />
      
      <!-- Gatling 리포트 링크 -->
      <GatlingReportLink 
        v-if="reportUrl" 
        :report-url="reportUrl" 
      />
    </div>
    
    <!-- 결과 없음 -->
    <div v-else class="bg-yellow-50 border border-yellow-200 p-4 text-center">
      <i class="fas fa-search text-yellow-500 text-3xl mb-4"></i>
      <h3 class="text-lg font-semibold text-yellow-800 mb-2">결과를 찾을 수 없습니다</h3>
      <p class="text-yellow-600">요청하신 테스트 결과가 존재하지 않습니다.</p>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useTestResults } from '../../shared/composables/useTestResults'
import ResultsHeader from './components/ResultsHeader.vue'
import SummaryMetrics from './components/SummaryMetrics.vue'  
import ResultCharts from './components/ResultCharts.vue'
import DetailTable from './components/DetailTable.vue'
import GatlingReportLink from './components/GatlingReportLink.vue'

// AIDEV-NOTE: testId는 URL 쿼리 파라미터에서 추출
const urlParams = new URLSearchParams(window.location.search)
const testId = ref(urlParams.get('testId') || null)

// Composable 사용
const {
  testResult,
  metricsHistory,
  reportUrl,
  loading,
  chartsLoading,
  error,
  hasData,
  loadResults,
  loadMetricsHistory,
  loadReportUrl
} = useTestResults()

// 컴포넌트 마운트 시 데이터 로드
onMounted(async () => {
  console.log('Results App mounted with testId:', testId.value)
  console.log('Initial state - loading:', loading.value, 'error:', error.value, 'testResult:', testResult.value, 'hasData:', hasData.value)
  
  if (!testId.value) {
    error.value = '테스트 ID가 제공되지 않았습니다.'
    return
  }
  
  try {
    // 메인 결과 데이터 로드
    console.log('Loading test results for:', testId.value)
    await loadResults(testId.value)
    console.log('After loadResults - loading:', loading.value, 'error:', error.value, 'hasData:', hasData.value)
    console.log('Test result loaded:', testResult.value)
    console.log('Test result has testId:', testResult.value?.testId)
    
    // 병렬로 메트릭 히스토리와 리포트 URL 로드
    await Promise.all([
      loadMetricsHistory(testId.value),
      loadReportUrl(testId.value)
    ])
    console.log('All data loaded successfully')
    console.log('Final state - loading:', loading.value, 'error:', error.value, 'hasData:', hasData.value)
  } catch (err) {
    console.error('결과 로드 실패:', err)
    error.value = err.message || '결과를 불러오는데 실패했습니다.'
  }
})
</script>

<style scoped>
/* AIDEV-NOTE: 플랫 디자인 스타일 - 대시보드와 일치하도록 수정 */
.btn {
  @apply px-3 py-1 font-semibold border transition-colors duration-200 text-sm;
}

.btn-primary {
  @apply bg-blue-500 text-white border-blue-600 hover:bg-blue-600;
}

.btn-secondary {
  @apply bg-gray-100 text-gray-700 border-gray-300 hover:bg-gray-200;
}

.btn-sm {
  @apply px-2 py-1 text-xs;
}
</style>