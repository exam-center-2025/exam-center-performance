import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import performanceApi from '../services/performanceApi'

// AIDEV-NOTE: Performance test state management store
export const usePerformanceStore = defineStore('performance', () => {
  // State
  const tests = ref([])
  const currentTest = ref(null)
  const plans = ref([])
  const selectedPlan = ref(null)
  const loading = ref(false)
  const error = ref(null)

  // Dashboard enhancement states
  const testHistory = ref([])
  const quickStats = ref({
    todayTests: 0,
    avgSuccessRate: 0,
    avgTps: 0,
    avgResponseTime: 0,
    activeTestsCount: 0
  })
  
  const systemHealth = ref({
    serverCpu: 0,
    serverMemory: 0,
    dbStatus: 'unknown',
    wsStatus: 'unknown',
    activeSessions: 0,
    lastUpdated: null
  })
  
  const testFilters = ref({
    dateRange: { start: null, end: null },
    status: 'all',
    planId: null,
    searchText: '',
    sortBy: 'startTime',
    sortOrder: 'desc'
  })

  // Monitoring state - AIDEV-NOTE: Real-time monitoring data for charts and metrics
  const currentMetrics = ref({
    tps: 0,
    avgResponseTime: 0,
    activeUsers: 0,
    errorRate: 0,
    timestamp: null
  })
  
  const metricsHistory = ref([])
  const maxHistorySize = ref(300) // 5 minutes of data at 1-second intervals
  
  const testStatus = ref({
    status: 'UNKNOWN',
    message: '',
    progress: 0,
    startTime: null,
    endTime: null
  })
  
  const logEntries = ref([])
  const maxLogEntries = ref(100)

  // Test configuration
  const testConfig = ref({
    testName: '',
    planId: null,
    maxUsers: 100,
    rampUpSeconds: 60,
    testDurationSeconds: 300,
    runType: 'TEST',
    scenario: 'NORMAL_USER'
  })

  // Getters
  const activeTests = computed(() => 
    tests.value.filter(test => test.status === 'RUNNING')
  )

  const completedTests = computed(() => 
    tests.value.filter(test => test.status === 'COMPLETED')
  )

  const isTestRunning = computed(() => 
    currentTest.value?.status === 'RUNNING'
  )

  // Actions
  async function loadPlans() {
    try {
      loading.value = true
      error.value = null
      const result = await performanceApi.getPlans()
      plans.value = result || []
    } catch (err) {
      error.value = err.message
      console.error('Failed to load plans:', err)
      plans.value = [] // Ensure plans is always an array
    } finally {
      loading.value = false
    }
  }

  async function loadTests() {
    try {
      loading.value = true
      error.value = null
      const result = await performanceApi.getAllTests()
      tests.value = result || []
    } catch (err) {
      error.value = err.message
      console.error('Failed to load tests:', err)
      tests.value = [] // Ensure tests is always an array
    } finally {
      loading.value = false
    }
  }

  async function startTest(config = testConfig.value) {
    try {
      loading.value = true
      error.value = null
      
      const result = await performanceApi.startTest(config)
      currentTest.value = result
      tests.value.unshift(result)
      
      return result
    } catch (err) {
      error.value = err.message
      console.error('Failed to start test:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  async function stopTest(testId) {
    try {
      loading.value = true
      error.value = null
      
      await performanceApi.stopTest(testId)
      
      if (currentTest.value?.id === testId) {
        currentTest.value.status = 'STOPPED'
      }
      
      const test = tests.value.find(t => t.id === testId)
      if (test) {
        test.status = 'STOPPED'
      }
    } catch (err) {
      error.value = err.message
      console.error('Failed to stop test:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  async function updateTestStatus(testId) {
    try {
      const status = await performanceApi.getTestStatus(testId)
      
      if (currentTest.value?.id === testId) {
        Object.assign(currentTest.value, status)
      }
      
      const test = tests.value.find(t => t.id === testId)
      if (test) {
        Object.assign(test, status)
      }
      
      return status
    } catch (err) {
      console.error('Failed to update test status:', err)
      throw err
    }
  }

  async function getTestResults(testId) {
    try {
      loading.value = true
      error.value = null
      
      const results = await performanceApi.getTestResults(testId)
      return results
    } catch (err) {
      error.value = err.message
      console.error('Failed to get test results:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  function setTestConfig(config) {
    testConfig.value = { ...testConfig.value, ...config }
  }

  function resetTestConfig() {
    testConfig.value = {
      testName: '',
      planId: null,
      maxUsers: 100,
      rampUpSeconds: 60,
      testDurationSeconds: 300,
      runType: 'TEST',
      scenario: 'NORMAL_USER'
    }
  }

  // Monitoring actions - AIDEV-NOTE: Real-time data management for monitor page
  function updateMetrics(metrics) {
    currentMetrics.value = {
      ...metrics,
      timestamp: Date.now()
    }
    
    // Add to history
    metricsHistory.value.push({
      ...metrics,
      timestamp: Date.now()
    })
    
    // Limit history size
    if (metricsHistory.value.length > maxHistorySize.value) {
      metricsHistory.value.shift()
    }
  }
  
  function updateTestStatus(status) {
    testStatus.value = { ...testStatus.value, ...status }
  }
  
  function addLogEntry(log) {
    logEntries.value.unshift({
      ...log,
      id: Date.now() + Math.random(),
      timestamp: log.timestamp || Date.now()
    })
    
    // Limit log entries
    if (logEntries.value.length > maxLogEntries.value) {
      logEntries.value = logEntries.value.slice(0, maxLogEntries.value)
    }
  }
  
  function clearMonitoringData() {
    currentMetrics.value = {
      tps: 0,
      avgResponseTime: 0,
      activeUsers: 0,
      errorRate: 0,
      timestamp: null
    }
    metricsHistory.value = []
    testStatus.value = {
      status: 'UNKNOWN',
      message: '',
      progress: 0,
      startTime: null,
      endTime: null
    }
    logEntries.value = []
  }
  
  function getRecentMetricsHistory(minutes = 5) {
    const cutoff = Date.now() - (minutes * 60 * 1000)
    return metricsHistory.value.filter(metric => metric.timestamp > cutoff)
  }

  // Dashboard enhancement actions
  async function loadTestHistory(params = {}) {
    try {
      loading.value = true
      error.value = null
      
      const response = await performanceApi.getTestHistory(params)
      testHistory.value = response?.data || response || []
      
      return response
    } catch (err) {
      error.value = err.message
      console.error('Failed to load test history:', err)
      testHistory.value = [] // Ensure testHistory is always an array
      return { data: [] }
    } finally {
      loading.value = false
    }
  }

  async function loadQuickStats() {
    try {
      const stats = await performanceApi.getQuickStats()
      quickStats.value = {
        todayTests: stats.todayTests || 0,
        avgSuccessRate: stats.avgSuccessRate || 0,
        avgTps: stats.avgTps || 0,
        avgResponseTime: stats.avgResponseTime || 0,
        activeTestsCount: stats.activeTestsCount || 0
      }
    } catch (err) {
      console.error('Failed to load quick stats:', err)
    }
  }

  async function loadSystemHealth() {
    try {
      const health = await performanceApi.getSystemHealth()
      systemHealth.value = {
        serverCpu: health.serverCpu || 0,
        serverMemory: health.serverMemory || 0,
        dbStatus: health.dbStatus || 'unknown',
        wsStatus: health.wsStatus || 'unknown',
        activeSessions: health.activeSessions || 0,
        lastUpdated: Date.now()
      }
    } catch (err) {
      console.error('Failed to load system health:', err)
    }
  }

  async function exportTestResults(params = {}) {
    try {
      loading.value = true
      const blob = await performanceApi.exportResults(params)
      
      // Create download link
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `test-results-${Date.now()}.csv`
      document.body.appendChild(a)
      a.click()
      window.URL.revokeObjectURL(url)
      document.body.removeChild(a)
    } catch (err) {
      error.value = err.message
      console.error('Failed to export results:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  function setTestFilters(filters) {
    testFilters.value = { ...testFilters.value, ...filters }
  }

  function resetTestFilters() {
    testFilters.value = {
      dateRange: { start: null, end: null },
      status: 'all',
      planId: null,
      searchText: '',
      sortBy: 'startTime',
      sortOrder: 'desc'
    }
  }

  function updateSystemHealth(health) {
    systemHealth.value = {
      ...systemHealth.value,
      ...health,
      lastUpdated: Date.now()
    }
  }

  return {
    // State
    tests,
    currentTest,
    plans,
    selectedPlan,
    loading,
    error,
    testConfig,
    
    // Dashboard enhancement state
    testHistory,
    quickStats,
    systemHealth,
    testFilters,
    
    // Monitoring state
    currentMetrics,
    metricsHistory,
    maxHistorySize,
    testStatus,
    logEntries,
    maxLogEntries,
    
    // Getters
    activeTests,
    completedTests,
    isTestRunning,
    
    // Actions
    loadPlans,
    loadTests,
    startTest,
    stopTest,
    updateTestStatus,
    getTestResults,
    setTestConfig,
    resetTestConfig,
    
    // Dashboard enhancement actions
    loadTestHistory,
    loadQuickStats,
    loadSystemHealth,
    exportTestResults,
    setTestFilters,
    resetTestFilters,
    updateSystemHealth,
    
    // Monitoring actions
    updateMetrics,
    updateTestStatus,
    addLogEntry,
    clearMonitoringData,
    getRecentMetricsHistory
  }
})