import { defineStore } from 'pinia'
import { ref, computed, reactive } from 'vue'

// AIDEV-NOTE: 테스트 설정 중앙 관리 스토어 - 폼 상태 및 유효성 검사
export const useTestConfigStore = defineStore('testConfig', () => {
  // 테스트 요청 상태
  const testRequest = reactive({
    planId: null,
    runType: 'TEST',
    testName: '',
    maxUsers: 100,
    rampUpSeconds: 60,
    testDurationSeconds: 300,
    scenario: 'COMPLETE',
    additionalConfig: ''
  })

  // 참조 데이터
  const examPlans = ref([])
  const scenarios = ref([
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
  ])
  
  const runTypes = ref([
    { value: 'TEST', label: '테스트 환경' },
    { value: 'PROD', label: '운영 환경' }
  ])

  // 로딩 상태
  const isLoading = ref(false)
  const isSubmitting = ref(false)

  // 유효성 검사 에러
  const errors = reactive({})

  // 계산된 값들
  const calculatedMetrics = computed(() => {
    const maxLoad = Math.round(testRequest.maxUsers * 0.5) // 사용자당 0.5 TPS 가정
    const avgTps = maxLoad * 0.7 // 평균 70% 부하 가정  
    const totalRequests = Math.round(avgTps * testRequest.testDurationSeconds)
    const totalSeconds = testRequest.rampUpSeconds + testRequest.testDurationSeconds
    const minutes = Math.floor(totalSeconds / 60)
    const seconds = totalSeconds % 60

    return {
      maxLoad,
      totalRequests,
      duration: `${minutes}분 ${seconds}초`
    }
  })

  // 폼 유효성 검사
  const validateForm = () => {
    const newErrors = {}

    if (!testRequest.planId) {
      newErrors.planId = '시험 계획을 선택해주세요'
    }

    if (testRequest.maxUsers < 1 || testRequest.maxUsers > 10000) {
      newErrors.maxUsers = '최대 사용자 수는 1-10000 사이여야 합니다'
    }

    if (testRequest.rampUpSeconds < 0 || testRequest.rampUpSeconds > 3600) {
      newErrors.rampUpSeconds = '램프업 시간은 0-3600초 사이여야 합니다'
    }

    if (testRequest.testDurationSeconds < 60 || testRequest.testDurationSeconds > 7200) {
      newErrors.testDurationSeconds = '테스트 지속시간은 60-7200초 사이여야 합니다'
    }

    if (testRequest.additionalConfig && testRequest.additionalConfig.trim()) {
      try {
        JSON.parse(testRequest.additionalConfig)
      } catch (e) {
        newErrors.additionalConfig = '올바른 JSON 형식이 아닙니다'
      }
    }

    // 에러 상태 업데이트
    Object.keys(errors).forEach(key => delete errors[key])
    Object.assign(errors, newErrors)

    return Object.keys(newErrors).length === 0
  }

  // 필드별 실시간 유효성 검사
  const validateField = (fieldName) => {
    switch (fieldName) {
      case 'planId':
        if (!testRequest.planId) {
          errors.planId = '시험 계획을 선택해주세요'
        } else {
          delete errors.planId
        }
        break
      case 'maxUsers':
        if (testRequest.maxUsers < 1 || testRequest.maxUsers > 10000) {
          errors.maxUsers = '최대 사용자 수는 1-10000 사이여야 합니다'
        } else {
          delete errors.maxUsers
        }
        break
      case 'rampUpSeconds':
        if (testRequest.rampUpSeconds < 0 || testRequest.rampUpSeconds > 3600) {
          errors.rampUpSeconds = '램프업 시간은 0-3600초 사이여야 합니다'
        } else {
          delete errors.rampUpSeconds
        }
        break
      case 'testDurationSeconds':
        if (testRequest.testDurationSeconds < 60 || testRequest.testDurationSeconds > 7200) {
          errors.testDurationSeconds = '테스트 지속시간은 60-7200초 사이여야 합니다'
        } else {
          delete errors.testDurationSeconds
        }
        break
      case 'additionalConfig':
        if (testRequest.additionalConfig && testRequest.additionalConfig.trim()) {
          try {
            JSON.parse(testRequest.additionalConfig)
            delete errors.additionalConfig
          } catch (e) {
            errors.additionalConfig = '올바른 JSON 형식이 아닙니다'
          }
        } else {
          delete errors.additionalConfig
        }
        break
    }
  }

  // 템플릿 관리
  const saveTemplate = (templateName) => {
    const templates = JSON.parse(localStorage.getItem('performanceTestTemplates') || '[]')
    const templateData = {
      templateName,
      ...testRequest,
      createdAt: new Date().toISOString()
    }

    const existingIndex = templates.findIndex(t => t.templateName === templateName)
    if (existingIndex >= 0) {
      templates[existingIndex] = { ...templateData, updatedAt: new Date().toISOString() }
    } else {
      templates.push(templateData)
    }

    localStorage.setItem('performanceTestTemplates', JSON.stringify(templates))
    return true
  }

  const loadTemplate = (templateName) => {
    const templates = JSON.parse(localStorage.getItem('performanceTestTemplates') || '[]')
    const template = templates.find(t => t.templateName === templateName)
    
    if (template) {
      Object.assign(testRequest, template)
      return true
    }
    return false
  }

  const getTemplates = () => {
    return JSON.parse(localStorage.getItem('performanceTestTemplates') || '[]')
  }

  // 프리셋 설정
  const applyPreset = (presetType) => {
    switch (presetType) {
      case 'LOW':
        testRequest.maxUsers = 50
        testRequest.rampUpSeconds = 30
        testRequest.testDurationSeconds = 180
        break
      case 'MEDIUM':
        testRequest.maxUsers = 200
        testRequest.rampUpSeconds = 60
        testRequest.testDurationSeconds = 300
        break
      case 'HIGH':
        testRequest.maxUsers = 500
        testRequest.rampUpSeconds = 120
        testRequest.testDurationSeconds = 600
        break
    }
  }

  // 폼 초기화
  const resetForm = () => {
    testRequest.planId = null
    testRequest.runType = 'TEST'
    testRequest.testName = ''
    testRequest.maxUsers = 100
    testRequest.rampUpSeconds = 60
    testRequest.testDurationSeconds = 300
    testRequest.scenario = 'COMPLETE'
    testRequest.additionalConfig = ''
    
    // 에러 초기화
    Object.keys(errors).forEach(key => delete errors[key])
  }

  return {
    // 상태
    testRequest,
    examPlans,
    scenarios,
    runTypes,
    isLoading,
    isSubmitting,
    errors,
    
    // 계산된 값
    calculatedMetrics,
    
    // 메서드
    validateForm,
    validateField,
    saveTemplate,
    loadTemplate,
    getTemplates,
    applyPreset,
    resetForm
  }
})