<template>
  <div class="bg-white border border-gray-200 rounded-lg p-6">
    <div class="text-center space-y-4">
      <!-- 메인 액션 버튼들 -->
      <div class="flex flex-col sm:flex-row justify-center gap-3">
        <!-- 템플릿 저장 버튼 -->
        <button
          @click="$emit('save-template')"
          type="button"
          class="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors"
        >
          <i class="fas fa-save mr-2"></i>
          템플릿 저장
        </button>
        
        <!-- 폼 검증 버튼 -->
        <button
          @click="validateAndPreview"
          type="button"
          :disabled="isSubmitting"
          class="inline-flex items-center px-4 py-2 border border-blue-300 text-sm font-medium rounded-md text-blue-700 bg-blue-50 hover:bg-blue-100 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors"
        >
          <i class="fas fa-check mr-2"></i>
          설정 검증
        </button>
        
        <!-- 테스트 시작 버튼 -->
        <button
          @click="$emit('start-test')"
          type="submit"
          :disabled="!isFormValid || isSubmitting"
          :class="[
            'inline-flex items-center px-6 py-2 text-sm font-medium rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2 transition-all',
            isFormValid && !isSubmitting
              ? 'border-transparent text-white bg-blue-600 hover:bg-blue-700 focus:ring-blue-500 transform hover:scale-105'
              : 'border-gray-300 text-gray-400 bg-gray-100 cursor-not-allowed'
          ]"
        >
          <i 
            :class="isSubmitting ? 'fas fa-spinner fa-spin' : 'fas fa-play'"
            class="mr-2"
          ></i>
          {{ isSubmitting ? '테스트 시작 중...' : '테스트 시작' }}
        </button>
      </div>
      
      <!-- 폼 유효성 상태 표시 -->
      <div class="flex justify-center">
        <div 
          v-if="validationMessage"
          :class="[
            'inline-flex items-center px-3 py-1 rounded-full text-xs font-medium',
            isFormValid 
              ? 'bg-green-100 text-green-700' 
              : 'bg-yellow-100 text-yellow-700'
          ]"
        >
          <i 
            :class="isFormValid ? 'fas fa-check-circle' : 'fas fa-exclamation-triangle'"
            class="mr-1"
          ></i>
          {{ validationMessage }}
        </div>
      </div>
      
      <!-- 추가 옵션들 -->
      <div class="pt-4 border-t border-gray-200">
        <div class="flex flex-wrap justify-center gap-4 text-sm text-gray-600">
          <!-- 폼 초기화 -->
          <button
            @click="confirmReset"
            type="button"
            class="inline-flex items-center hover:text-red-600 transition-colors"
          >
            <i class="fas fa-redo mr-1"></i>
            설정 초기화
          </button>
          
          <!-- 최근 설정 히스토리 -->
          <button
            v-if="hasHistory"
            @click="showHistory = !showHistory"
            type="button"
            class="inline-flex items-center hover:text-blue-600 transition-colors"
          >
            <i class="fas fa-history mr-1"></i>
            최근 설정
          </button>
          
          <!-- 도움말 -->
          <button
            @click="showHelp = !showHelp"
            type="button"
            class="inline-flex items-center hover:text-green-600 transition-colors"
          >
            <i class="fas fa-question-circle mr-1"></i>
            도움말
          </button>
        </div>
      </div>
      
      <!-- 최근 설정 히스토리 -->
      <div v-if="showHistory && templates.length > 0" class="pt-4 border-t border-gray-100">
        <h4 class="text-sm font-medium text-gray-900 mb-3">최근 사용한 템플릿</h4>
        <div class="flex flex-wrap justify-center gap-2">
          <button
            v-for="template in recentTemplates"
            :key="template.templateName"
            @click="loadRecentTemplate(template)"
            type="button"
            class="inline-flex items-center px-3 py-1 text-xs border border-gray-300 rounded-full hover:bg-gray-50 transition-colors"
          >
            <i class="fas fa-clock mr-1"></i>
            {{ template.templateName }}
          </button>
        </div>
      </div>
      
      <!-- 도움말 섹션 -->
      <div v-if="showHelp" class="pt-4 border-t border-gray-100">
        <div class="text-left bg-blue-50 border border-blue-200 rounded-lg p-4">
          <h4 class="text-sm font-medium text-blue-900 mb-2 flex items-center">
            <i class="fas fa-lightbulb mr-2"></i>
            테스트 설정 가이드
          </h4>
          <div class="text-sm text-blue-700 space-y-2">
            <p><strong>1. 기본 설정:</strong> 시험 계획과 실행 타입을 먼저 선택하세요.</p>
            <p><strong>2. 부하 설정:</strong> 프리셋을 사용하거나 직접 입력하세요. Low → Medium → High 순으로 테스트를 권장합니다.</p>
            <p><strong>3. 시나리오:</strong> BASIC(가벼움) → COMPLETE(표준) → STRESS(고부하) 순으로 선택하세요.</p>
            <p><strong>4. 검증:</strong> '설정 검증' 버튼으로 설정을 미리 확인할 수 있습니다.</p>
          </div>
        </div>
      </div>
      
      <!-- 안내 메시지 -->
      <div class="pt-4 text-center">
        <p class="text-xs text-gray-500 flex items-center justify-center">
          <i class="fas fa-info-circle mr-2"></i>
          테스트 시작 후 실시간 모니터링 페이지로 자동 이동됩니다.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useTestConfig } from '@shared/composables/useTestConfig.js'

// AIDEV-NOTE: 액션 버튼 컴포넌트 - 테스트 시작, 템플릿 저장, 유효성 검사 등
const emit = defineEmits(['start-test', 'save-template'])

const {
  testRequest,
  templates,
  errors,
  isFormValid,
  isSubmitting,
  resetForm,
  validateForm,
  loadTemplate,
  showMessage
} = useTestConfig()

// 추가 UI 상태
const showHistory = ref(false)
const showHelp = ref(false)

// 유효성 검사 메시지
const validationMessage = computed(() => {
  if (isFormValid.value) {
    return '모든 설정이 올바릅니다'
  }
  
  const errorCount = Object.keys(errors).length
  if (errorCount > 0) {
    return `${errorCount}개의 항목을 확인해주세요`
  }
  
  if (!testRequest.planId) {
    return '시험 계획을 선택해주세요'
  }
  
  return '필수 항목을 입력해주세요'
})

// 히스토리가 있는지 확인
const hasHistory = computed(() => templates.value.length > 0)

// 최근 템플릿 (최대 5개)
const recentTemplates = computed(() => {
  return templates.value
    .sort((a, b) => {
      const aTime = new Date(a.updatedAt || a.createdAt)
      const bTime = new Date(b.updatedAt || b.createdAt)
      return bTime - aTime
    })
    .slice(0, 5)
})

// 설정 검증 및 미리보기
const validateAndPreview = () => {
  const isValid = validateForm()
  
  if (isValid) {
    showMessage('설정이 올바릅니다! 테스트를 시작할 수 있습니다.', 'success')
  } else {
    const errorFields = Object.keys(errors)
    showMessage(`다음 항목을 확인해주세요: ${errorFields.join(', ')}`, 'error')
  }
}

// 설정 초기화 확인
const confirmReset = () => {
  if (confirm('현재 설정을 모두 초기화하시겠습니까?')) {
    resetForm()
    showMessage('설정이 초기화되었습니다', 'info')
  }
}

// 최근 템플릿 로드
const loadRecentTemplate = (template) => {
  const success = loadTemplate(template.templateName)
  if (success) {
    showHistory.value = false
  }
}
</script>

<style scoped>
/* 버튼 애니메이션 */
button {
  transition: all 0.2s ease;
}

button:not(:disabled):hover {
  transform: translateY(-1px);
}

button:not(:disabled):active {
  transform: translateY(0);
}

/* 메인 버튼 특별 효과 */
.bg-blue-600:hover {
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

/* 로딩 스피너 */
.fa-spinner {
  animation-duration: 1s;
}

/* 도움말 섹션 애니메이션 */
.bg-blue-50 {
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 상태 배지 애니메이션 */
.bg-green-100,
.bg-yellow-100 {
  animation: bounceIn 0.3s ease-out;
}

@keyframes bounceIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

/* 템플릿 버튼 호버 효과 */
.border-gray-300:hover {
  border-color: #60a5fa;
  background-color: #eff6ff;
}
</style>