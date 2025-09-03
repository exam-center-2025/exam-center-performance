import { ref, computed, watch } from 'vue'
import { useTestConfigStore } from '@shared/stores/testConfig.js'
import { configApi } from '@shared/services/configApi.js'

// AIDEV-NOTE: 테스트 설정 관련 비즈니스 로직 및 API 호출 관리
export function useTestConfig() {
  const store = useTestConfigStore()
  
  // 메시지 상태
  const message = ref('')
  const messageType = ref('') // 'success' | 'error' | 'info'
  
  // 초기 데이터 로드
  const loadInitialData = async () => {
    store.isLoading = true
    
    try {
      // 시험 계획 목록 로드
      const examPlans = await configApi.getExamPlans()
      store.examPlans = examPlans
      
      // 시나리오 목록 로드
      try {
        const scenarios = await configApi.getScenarios()
        store.scenarios = scenarios
      } catch (error) {
        // 기본 시나리오 사용
        console.warn('Using default scenarios:', error.message)
      }
      
    } catch (error) {
      showMessage(error.message, 'error')
    } finally {
      store.isLoading = false
    }
  }
  
  // 테스트 시작
  const startTest = async () => {
    // 유효성 검사
    if (!store.validateForm()) {
      showMessage('입력값을 확인해주세요', 'error')
      return false
    }
    
    store.isSubmitting = true
    
    try {
      // 서버 유효성 검사
      await configApi.validateConfig(store.testRequest)
      
      // 테스트 시작
      const result = await configApi.startTest(store.testRequest)
      
      if (result.success) {
        showMessage('테스트가 성공적으로 시작되었습니다!', 'success')
        
        // 2초 후 모니터링 페이지로 이동
        setTimeout(() => {
          const monitorUrl = `/dashboard/monitor/${result.data.testId}`
          window.location.href = monitorUrl
        }, 2000)
        
        return true
      } else {
        showMessage(result.message || '테스트 시작에 실패했습니다', 'error')
        return false
      }
      
    } catch (error) {
      showMessage(error.message, 'error')
      return false
    } finally {
      store.isSubmitting = false
    }
  }
  
  // 템플릿 저장
  const saveAsTemplate = async (templateName) => {
    if (!templateName || !templateName.trim()) {
      showMessage('템플릿 이름을 입력해주세요', 'error')
      return false
    }
    
    try {
      // 로컬 스토리지에 저장
      const success = store.saveTemplate(templateName.trim())
      
      if (success) {
        showMessage(`템플릿이 저장되었습니다: ${templateName}`, 'success')
        return true
      } else {
        showMessage('템플릿 저장에 실패했습니다', 'error')
        return false
      }
    } catch (error) {
      showMessage(error.message, 'error')
      return false
    }
  }
  
  // 템플릿 로드
  const loadTemplate = (templateName) => {
    try {
      const success = store.loadTemplate(templateName)
      
      if (success) {
        showMessage(`템플릿이 로드되었습니다: ${templateName}`, 'success')
        return true
      } else {
        showMessage('템플릿을 찾을 수 없습니다', 'error')
        return false
      }
    } catch (error) {
      showMessage(error.message, 'error')
      return false
    }
  }
  
  // 프리셋 적용
  const applyLoadPreset = (presetType) => {
    store.applyPreset(presetType)
    showMessage(`${presetType} 부하 프리셋이 적용되었습니다`, 'info')
  }
  
  // 메시지 표시
  const showMessage = (msg, type = 'info') => {
    message.value = msg
    messageType.value = type
    
    // 5초 후 자동 숨김
    if (type !== 'error') {
      setTimeout(() => {
        message.value = ''
        messageType.value = ''
      }, 5000)
    }
  }
  
  // 메시지 숨김
  const hideMessage = () => {
    message.value = ''
    messageType.value = ''
  }
  
  // 필드 변경 감지 및 실시간 유효성 검사
  watch(
    () => store.testRequest.planId,
    () => store.validateField('planId')
  )
  
  watch(
    () => store.testRequest.maxUsers,
    () => store.validateField('maxUsers')
  )
  
  watch(
    () => store.testRequest.rampUpSeconds,
    () => store.validateField('rampUpSeconds')
  )
  
  watch(
    () => store.testRequest.testDurationSeconds,
    () => store.validateField('testDurationSeconds')
  )
  
  watch(
    () => store.testRequest.additionalConfig,
    () => store.validateField('additionalConfig')
  )
  
  // 계산된 메트릭스 (편의를 위한 재노출)
  const metrics = computed(() => store.calculatedMetrics)
  
  // 폼 유효성 상태
  const isFormValid = computed(() => {
    return Object.keys(store.errors).length === 0 && 
           store.testRequest.planId && 
           store.testRequest.maxUsers > 0 &&
           store.testRequest.testDurationSeconds >= 60
  })
  
  // 템플릿 목록
  const templates = computed(() => store.getTemplates())
  
  return {
    // Store 상태 및 메서드
    testRequest: store.testRequest,
    examPlans: store.examPlans,
    scenarios: store.scenarios,
    runTypes: store.runTypes,
    errors: store.errors,
    isLoading: store.isLoading,
    isSubmitting: store.isSubmitting,
    
    // 계산된 값
    metrics,
    isFormValid,
    templates,
    
    // 메시지 상태
    message,
    messageType,
    
    // 메서드
    loadInitialData,
    startTest,
    saveAsTemplate,
    loadTemplate,
    applyLoadPreset,
    showMessage,
    hideMessage,
    validateField: store.validateField,
    resetForm: store.resetForm
  }
}