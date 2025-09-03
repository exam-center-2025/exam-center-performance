import api from './api.js'

// AIDEV-NOTE: 테스트 설정 관련 API 서비스
export const configApi = {
  // 시험 계획 목록 조회
  async getExamPlans() {
    try {
      const response = await api.get('/dashboard/exam-plans')
      return response
    } catch (error) {
      console.error('Failed to fetch exam plans:', error)
      throw new Error('시험 계획 목록을 불러오는데 실패했습니다')
    }
  },

  // 테스트 시작
  async startTest(testRequest) {
    try {
      const response = await api.post('/dashboard/tests/start', testRequest)
      return response
    } catch (error) {
      console.error('Failed to start test:', error)
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message)
      }
      throw new Error('테스트 시작에 실패했습니다')
    }
  },

  // 설정 유효성 검사
  async validateConfig(testRequest) {
    try {
      const response = await api.post('/dashboard/tests/validate', testRequest)
      return response
    } catch (error) {
      console.error('Failed to validate config:', error)
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message)
      }
      throw new Error('설정 검증에 실패했습니다')
    }
  },

  // 테스트 템플릿 저장 (서버)
  async saveTemplate(templateData) {
    try {
      const response = await api.post('/dashboard/templates', templateData)
      return response
    } catch (error) {
      console.error('Failed to save template:', error)
      throw new Error('템플릿 저장에 실패했습니다')
    }
  },

  // 테스트 템플릿 목록 조회 (서버)
  async getTemplates() {
    try {
      const response = await api.get('/dashboard/templates')
      return response
    } catch (error) {
      console.error('Failed to fetch templates:', error)
      throw new Error('템플릿 목록을 불러오는데 실패했습니다')
    }
  },

  // 시나리오 목록 조회
  async getScenarios() {
    try {
      const response = await api.get('/dashboard/scenarios')
      return response
    } catch (error) {
      console.error('Failed to fetch scenarios:', error)
      // 기본 시나리오 반환
      return [
        {
          value: 'BASIC',
          label: '기본 테스트',
          description: '핵심 기능만 테스트하는 가벼운 시나리오'
        },
        {
          value: 'COMPLETE',
          label: '완전 테스트',
          description: '모든 기능을 포함하는 포괄적인 시나리오'
        },
        {
          value: 'STRESS',
          label: '스트레스 테스트',
          description: '시스템 한계를 테스트하는 고강도 시나리오'
        }
      ]
    }
  }
}