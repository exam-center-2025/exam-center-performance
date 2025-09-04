import api from './api'

// AIDEV-NOTE: Performance test API service for Vue migration
export const performanceApi = {
  // Test management
  async startTest(testConfig) {
    return await api.post('/tests/start', testConfig)
  },

  async getTestStatus(testId) {
    return await api.get(`/tests/${testId}/result`)
  },

  async getTestResults(testId) {
    return await api.get(`/tests/${testId}/results`)
  },

  async stopTest(testId) {
    return await api.post(`/tests/${testId}/stop`)
  },

  async deleteTest(testId) {
    return await api.delete(`/tests/${testId}`)
  },

  async getAllTests() {
    return await api.get('/tests')
  },

  // Plans and groups
  async getPlans() {
    return await api.get('/plans')
  },

  async getPlanGroups(planId) {
    return await api.get(`/plans/${planId}/groups`)
  },

  // Health check
  async checkDatabaseHealth() {
    return await api.get('/health/database')
  },

  // Metrics
  async getRealtimeMetrics(testId) {
    return await api.get(`/tests/${testId}/metrics/realtime`)
  },

  async getHistoricalMetrics(testId, startTime, endTime) {
    return await api.get(`/tests/${testId}/metrics/history`, {
      params: { startTime, endTime }
    })
  },

  // Monitoring APIs - AIDEV-NOTE: Extended APIs for Vue monitor page
  async getTestMetricsHistory(testId, maxPoints = 300) {
    return await api.get(`/tests/${testId}/metrics`, {
      params: { maxPoints }
    })
  },

  async getTestLogs(testId, count = 100) {
    return await api.get(`/tests/${testId}/logs`, {
      params: { count }
    })
  },

  async getTestDetails(testId) {
    return await api.get(`/tests/${testId}`)
  },

  async getTestConfiguration(testId) {
    return await api.get(`/tests/${testId}/config`)
  },

  // Dashboard enhancement APIs - AIDEV-NOTE: New APIs for enhanced dashboard features
  async getTestHistory(params = {}) {
    return await api.get('/tests/history', { params })
  },

  async getQuickStats() {
    return await api.get('/stats')
  },

  async getSystemHealth() {
    return await api.get('/system/health')
  },

  async exportResults(params = {}) {
    const response = await api.get('/tests/export', {
      params,
      responseType: 'blob'
    })
    return response
  }
}

export default performanceApi