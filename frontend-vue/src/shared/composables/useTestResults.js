/*
useTestResults Composable
테스트 결과 데이터 관리를 위한 조합 함수

AIDEV-NOTE: Pinia 스토어를 래핑하여 컴포넌트에서 쉽게 사용할 수 있도록 함
차트 데이터 변환, 통계 계산, API 호출 추상화
*/

import { computed, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useTestResultsStore } from '../stores/testResults'

export function useTestResults() {
  const store = useTestResultsStore()
  
  // 추가 상태
  const autoRefresh = ref(false)
  const refreshInterval = ref(null)
  
  // Store 상태를 반응성 유지하며 노출
  const {
    testResult,
    metricsHistory,
    reportUrl,
    loading,
    chartsLoading,
    error,
    hasData,
    hasMetrics,
    hasError,
    testStatus,
    isCompleted,
    summary
  } = storeToRefs(store)

  // 차트 데이터 변환 함수들
  
  /**
   * TPS 차트 데이터 변환
   */
  const tpsChartData = computed(() => {
    if (!hasMetrics.value) return null
    
    const data = metricsHistory.value
      .slice(-300) // 최대 300개 포인트
      .map(m => ({
        x: new Date(m.timestamp),
        y: m.tps || 0
      }))
      .filter(d => !isNaN(d.x.getTime()))
    
    return {
      datasets: [{
        label: 'TPS (Transactions Per Second)',
        data: data,
        borderColor: 'rgb(102, 126, 234)',
        backgroundColor: 'rgba(102, 126, 234, 0.1)',
        fill: true,
        tension: 0.2,
        pointRadius: 2,
        pointHoverRadius: 4
      }]
    }
  })

  /**
   * 응답시간 차트 데이터 변환
   */
  const responseTimeChartData = computed(() => {
    if (!hasMetrics.value) return null
    
    const data = metricsHistory.value
      .slice(-300)
      .map(m => ({
        x: new Date(m.timestamp),
        y: m.avgResponseTime || 0
      }))
      .filter(d => !isNaN(d.x.getTime()))
    
    return {
      datasets: [{
        label: '평균 응답시간 (ms)',
        data: data,
        borderColor: 'rgb(147, 51, 234)',
        backgroundColor: 'rgba(147, 51, 234, 0.1)',
        fill: true,
        tension: 0.2,
        pointRadius: 2,
        pointHoverRadius: 4
      }]
    }
  })

  /**
   * 에러율 차트 데이터 변환 (확장용)
   */
  const errorRateChartData = computed(() => {
    if (!hasMetrics.value) return null
    
    const data = metricsHistory.value
      .slice(-300)
      .map(m => {
        const total = m.totalRequests || 0
        const errors = m.errorCount || 0
        return {
          x: new Date(m.timestamp),
          y: total > 0 ? (errors / total) * 100 : 0
        }
      })
      .filter(d => !isNaN(d.x.getTime()))
    
    return {
      datasets: [{
        label: '에러율 (%)',
        data: data,
        borderColor: 'rgb(239, 68, 68)',
        backgroundColor: 'rgba(239, 68, 68, 0.1)',
        fill: true,
        tension: 0.2,
        pointRadius: 2,
        pointHoverRadius: 4
      }]
    }
  })

  /**
   * 동시 사용자 차트 데이터 변환 (확장용)
   */
  const activeUsersChartData = computed(() => {
    if (!hasMetrics.value) return null
    
    const data = metricsHistory.value
      .slice(-300)
      .map(m => ({
        x: new Date(m.timestamp),
        y: m.activeUsers || 0
      }))
      .filter(d => !isNaN(d.x.getTime()))
    
    return {
      datasets: [{
        label: '동시 사용자 수',
        data: data,
        borderColor: 'rgb(59, 130, 246)',
        backgroundColor: 'rgba(59, 130, 246, 0.1)',
        fill: true,
        tension: 0.2,
        pointRadius: 2,
        pointHoverRadius: 4
      }]
    }
  })

  // 유틸리티 함수들
  
  /**
   * 숫자 포맷팅
   */
  const formatNumber = (num, decimals = 0) => {
    if (num == null) return 'N/A'
    return new Intl.NumberFormat('ko-KR', {
      minimumFractionDigits: decimals,
      maximumFractionDigits: decimals
    }).format(num)
  }

  /**
   * 백분율 포맷팅
   */
  const formatPercentage = (num, decimals = 1) => {
    if (num == null) return 'N/A'
    return formatNumber(num, decimals) + '%'
  }

  /**
   * 응답시간 포맷팅
   */
  const formatResponseTime = (time) => {
    if (time == null) return 'N/A'
    return Math.round(time) + 'ms'
  }

  /**
   * 소요시간 포맷팅
   */
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

  /**
   * 날짜시간 포맷팅
   */
  const formatDateTime = (dateTime, format = 'full') => {
    if (!dateTime) return 'N/A'
    
    try {
      const date = new Date(dateTime)
      if (isNaN(date.getTime())) return 'Invalid Date'
      
      const options = {
        full: {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
          hour12: false
        },
        short: {
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          hour12: false
        },
        time: {
          hour: '2-digit',
          minute: '2-digit',
          hour12: false
        }
      }
      
      return new Intl.DateTimeFormat('ko-KR', options[format] || options.full).format(date)
    } catch (error) {
      console.warn('날짜 포맷팅 오류:', error)
      return 'N/A'
    }
  }

  // API 호출 래퍼 함수들
  
  /**
   * 테스트 결과 로드
   */
  const loadResults = async (testId) => {
    return await store.fetchTestResult(testId)
  }

  /**
   * 메트릭 히스토리 로드
   */
  const loadMetricsHistory = async (testId) => {
    return await store.fetchMetricsHistory(testId)
  }

  /**
   * 리포트 URL 로드
   */
  const loadReportUrl = async (testId) => {
    return await store.fetchReportUrl(testId)
  }

  /**
   * 전체 데이터 로드
   */
  const loadAllData = async (testId) => {
    return await store.loadAllData(testId)
  }

  /**
   * 데이터 새로고침
   */
  const refresh = async (testId) => {
    return await store.refresh(testId)
  }

  // 자동 새로고침 기능
  
  /**
   * 자동 새로고침 시작
   */
  const startAutoRefresh = (testId, intervalSeconds = 30) => {
    if (refreshInterval.value) {
      clearInterval(refreshInterval.value)
    }
    
    autoRefresh.value = true
    refreshInterval.value = setInterval(async () => {
      try {
        await refresh(testId)
      } catch (error) {
        console.warn('자동 새로고침 실패:', error)
      }
    }, intervalSeconds * 1000)
  }

  /**
   * 자동 새로고침 중지
   */
  const stopAutoRefresh = () => {
    if (refreshInterval.value) {
      clearInterval(refreshInterval.value)
      refreshInterval.value = null
    }
    autoRefresh.value = false
  }

  // 통계 계산
  const statistics = computed(() => {
    return store.getStatistics.value
  })

  // 성능 지표 계산
  const performanceGrade = computed(() => {
    if (!summary.value) return null
    
    const { successRate, avgResponseTime } = summary.value
    
    if (successRate >= 99 && avgResponseTime <= 100) return 'A+'
    if (successRate >= 98 && avgResponseTime <= 200) return 'A'
    if (successRate >= 95 && avgResponseTime <= 500) return 'B'
    if (successRate >= 90 && avgResponseTime <= 1000) return 'C'
    return 'D'
  })

  // 클린업
  const cleanup = () => {
    stopAutoRefresh()
    store.reset()
  }

  // 테스트 상태가 변경되면 자동 새로고침 조건 체크
  watch(testStatus, (newStatus) => {
    if (newStatus === 'RUNNING' && !autoRefresh.value) {
      // 실행 중인 테스트는 자동 새로고침 시작을 고려
      console.log('실행 중인 테스트 감지 - 자동 새로고침 가능')
    } else if (newStatus === 'COMPLETED' && autoRefresh.value) {
      // 완료된 테스트는 자동 새로고침 중지
      stopAutoRefresh()
    }
  })

  return {
    // Store 상태
    testResult,
    metricsHistory,
    reportUrl,
    loading,
    chartsLoading,
    error,
    hasData,
    hasMetrics,
    hasError,
    testStatus,
    isCompleted,
    summary,
    statistics,
    
    // 차트 데이터
    tpsChartData,
    responseTimeChartData,
    errorRateChartData,
    activeUsersChartData,
    
    // 포맷팅 함수
    formatNumber,
    formatPercentage,
    formatResponseTime,
    formatDuration,
    formatDateTime,
    
    // API 함수
    loadResults,
    loadMetricsHistory,
    loadReportUrl,
    loadAllData,
    refresh,
    
    // 자동 새로고침
    autoRefresh,
    startAutoRefresh,
    stopAutoRefresh,
    
    // 유틸리티
    performanceGrade,
    cleanup
  }
}