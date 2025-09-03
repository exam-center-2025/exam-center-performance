/*
testResults Pinia 스토어
테스트 결과 데이터 및 메트릭 히스토리 관리

AIDEV-NOTE: 결과 조회, 메트릭 히스토리, 리포트 URL 관리
캐싱, 에러 처리, 로딩 상태 포함
*/

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../services/api'

export const useTestResultsStore = defineStore('testResults', () => {
  // 상태 정의
  const testResult = ref(null)
  const metricsHistory = ref([])
  const reportUrl = ref(null)
  const loading = ref(false)
  const chartsLoading = ref(false)
  const error = ref(null)
  
  // 캐시 관리
  const cache = ref(new Map())
  const cacheTimeout = 5 * 60 * 1000 // 5분 캐시

  // Getters
  const hasData = computed(() => !!testResult.value)
  const hasMetrics = computed(() => metricsHistory.value.length > 0)
  const hasError = computed(() => !!error.value)
  
  const testStatus = computed(() => {
    return testResult.value?.status?.toUpperCase() || 'UNKNOWN'
  })
  
  const isCompleted = computed(() => {
    return testStatus.value === 'COMPLETED'
  })
  
  const summary = computed(() => {
    if (!testResult.value) return null
    
    return {
      successRate: testResult.value.successRate || 0,
      totalRequests: testResult.value.totalRequests || 0,
      avgResponseTime: testResult.value.avgResponseTime || 0,
      maxTps: testResult.value.maxTps || 0,
      p95ResponseTime: testResult.value.p95ResponseTime || 0,
      maxConcurrentUsers: testResult.value.maxConcurrentUsers || 0,
      duration: testResult.value.actualDurationSeconds || 0
    }
  })

  // Actions
  
  /**
   * 테스트 결과 조회
   * @param {string} testId - 테스트 ID
   * @param {boolean} useCache - 캐시 사용 여부
   */
  const fetchTestResult = async (testId, useCache = true) => {
    if (!testId) {
      throw new Error('테스트 ID가 필요합니다.')
    }
    
    // 캐시 확인
    const cacheKey = `result_${testId}`
    if (useCache && cache.value.has(cacheKey)) {
      const cached = cache.value.get(cacheKey)
      if (Date.now() - cached.timestamp < cacheTimeout) {
        testResult.value = cached.data
        return cached.data
      }
    }
    
    loading.value = true
    error.value = null
    
    try {
      console.log('Fetching test result from API for:', testId)
      const data = await api.get(`/tests/${testId}/result`)
      console.log('API Response received:', data)
      
      // 데이터 검증
      if (!data || !data.testId) {
        console.error('Invalid data structure:', data)
        throw new Error('유효하지 않은 테스트 결과 데이터입니다.')
      }
      
      testResult.value = data
      console.log('Test result stored in state:', testResult.value)
      
      // 캐시 저장
      cache.value.set(cacheKey, {
        data: data,
        timestamp: Date.now()
      })
      
      return data
      
    } catch (err) {
      const errorMessage = err.response?.data?.message || err.message || '테스트 결과를 불러오는데 실패했습니다.'
      error.value = errorMessage
      throw new Error(errorMessage)
      
    } finally {
      loading.value = false
    }
  }

  /**
   * 메트릭 히스토리 조회
   * @param {string} testId - 테스트 ID
   * @param {boolean} useCache - 캐시 사용 여부
   */
  const fetchMetricsHistory = async (testId, useCache = true) => {
    if (!testId) {
      throw new Error('테스트 ID가 필요합니다.')
    }
    
    // 캐시 확인
    const cacheKey = `metrics_${testId}`
    if (useCache && cache.value.has(cacheKey)) {
      const cached = cache.value.get(cacheKey)
      if (Date.now() - cached.timestamp < cacheTimeout) {
        metricsHistory.value = cached.data
        return cached.data
      }
    }
    
    chartsLoading.value = true
    
    try {
      const data = await api.get(`/tests/${testId}/metrics`) || []
      
      // 데이터 정렬 및 검증
      const validMetrics = data
        .filter(m => m.timestamp && !isNaN(new Date(m.timestamp).getTime()))
        .sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp))
        .slice(-500) // 최대 500개 포인트만 유지
      
      metricsHistory.value = validMetrics
      
      // 캐시 저장
      cache.value.set(cacheKey, {
        data: validMetrics,
        timestamp: Date.now()
      })
      
      return validMetrics
      
    } catch (err) {
      console.warn('메트릭 히스토리 로드 실패:', err)
      // 메트릭은 필수가 아니므로 빈 배열로 설정
      metricsHistory.value = []
      return []
      
    } finally {
      chartsLoading.value = false
    }
  }

  /**
   * Gatling 리포트 URL 조회
   * @param {string} testId - 테스트 ID
   */
  const fetchReportUrl = async (testId) => {
    if (!testId) return null
    
    try {
      const data = await api.get(`/tests/${testId}/report-url`)
      const url = data?.reportUrl || data?.url
      
      reportUrl.value = url
      return url
      
    } catch (err) {
      console.warn('리포트 URL 조회 실패:', err)
      reportUrl.value = null
      return null
    }
  }

  /**
   * 전체 데이터 로드 (결과 + 메트릭 + 리포트)
   * @param {string} testId - 테스트 ID
   */
  const loadAllData = async (testId) => {
    if (!testId) {
      throw new Error('테스트 ID가 필요합니다.')
    }
    
    try {
      // 메인 결과 먼저 로드
      await fetchTestResult(testId)
      
      // 메트릭과 리포트 URL은 병렬로 로드
      await Promise.allSettled([
        fetchMetricsHistory(testId),
        fetchReportUrl(testId)
      ])
      
    } catch (err) {
      throw err
    }
  }

  /**
   * 데이터 새로고침
   * @param {string} testId - 테스트 ID
   */
  const refresh = async (testId) => {
    // 캐시 클리어
    const keys = [`result_${testId}`, `metrics_${testId}`]
    keys.forEach(key => cache.value.delete(key))
    
    // 데이터 다시 로드
    await loadAllData(testId)
  }

  /**
   * 상태 초기화
   */
  const reset = () => {
    testResult.value = null
    metricsHistory.value = []
    reportUrl.value = null
    loading.value = false
    chartsLoading.value = false
    error.value = null
  }

  /**
   * 특정 테스트의 캐시 클리어
   * @param {string} testId - 테스트 ID
   */
  const clearCache = (testId = null) => {
    if (testId) {
      const keys = [`result_${testId}`, `metrics_${testId}`]
      keys.forEach(key => cache.value.delete(key))
    } else {
      cache.value.clear()
    }
  }

  /**
   * 통계 계산
   */
  const getStatistics = computed(() => {
    if (!hasMetrics.value) return null
    
    const metrics = metricsHistory.value
    const tpsValues = metrics.map(m => m.tps || 0).filter(v => v > 0)
    const responseTimeValues = metrics.map(m => m.avgResponseTime || 0).filter(v => v > 0)
    
    if (tpsValues.length === 0) return null
    
    return {
      avgTps: tpsValues.reduce((a, b) => a + b, 0) / tpsValues.length,
      maxTps: Math.max(...tpsValues),
      minTps: Math.min(...tpsValues),
      avgResponseTime: responseTimeValues.reduce((a, b) => a + b, 0) / responseTimeValues.length,
      maxResponseTime: Math.max(...responseTimeValues),
      minResponseTime: Math.min(...responseTimeValues),
      dataPoints: metrics.length
    }
  })

  return {
    // State
    testResult,
    metricsHistory,
    reportUrl,
    loading,
    chartsLoading,
    error,
    
    // Getters
    hasData,
    hasMetrics,
    hasError,
    testStatus,
    isCompleted,
    summary,
    getStatistics,
    
    // Actions
    fetchTestResult,
    fetchMetricsHistory,
    fetchReportUrl,
    loadAllData,
    refresh,
    reset,
    clearCache
  }
})