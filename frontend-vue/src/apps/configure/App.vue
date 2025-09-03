<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 페이지 헤더 -->
    <ConfigHeader />
    
    <!-- 메인 콘텐츠 -->
    <main class="max-w-4xl mx-auto px-4 py-6">
      <!-- 로딩 상태 -->
      <div v-if="isLoading" class="text-center py-12">
        <i class="fas fa-spinner fa-spin text-3xl text-blue-500 mb-4"></i>
        <p class="text-gray-600">설정 정보를 불러오는 중...</p>
      </div>
      
      <!-- 메인 폼 -->
      <form v-else @submit.prevent="handleSubmit" class="space-y-6">
        <!-- 기본 설정 -->
        <BasicSettings />
        
        <!-- 부하 설정 -->
        <LoadSettings />
        
        <!-- 설정 미리보기 -->
        <ConfigPreview />
        
        <!-- 시나리오 선택 -->
        <ScenarioSelector />
        
        <!-- 고급 설정 -->
        <AdvancedSettings />
        
        <!-- 액션 버튼 -->
        <ActionButtons @start-test="handleSubmit" @save-template="handleSaveTemplate" />
      </form>
      
      <!-- 메시지 표시 -->
      <div v-if="message" :class="messageClasses" class="mt-6 p-4 border">
        <div class="flex items-center">
          <i :class="messageIcon" class="mr-2"></i>
          <span>{{ message }}</span>
          <button
            @click="hideMessage"
            class="ml-auto text-xl leading-none"
            type="button"
            aria-label="닫기"
          >
            &times;
          </button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useTestConfig } from '@shared/composables/useTestConfig.js'
import { useTestConfigStore } from '@shared/stores/testConfig.js'

import ConfigHeader from './components/ConfigHeader.vue'
import BasicSettings from './components/BasicSettings.vue'
import LoadSettings from './components/LoadSettings.vue'
import ConfigPreview from './components/ConfigPreview.vue'
import ScenarioSelector from './components/ScenarioSelector.vue'
import AdvancedSettings from './components/AdvancedSettings.vue'
import ActionButtons from './components/ActionButtons.vue'

// AIDEV-NOTE: Configure 페이지 메인 컴포넌트 - 서버 데이터 연동 및 전체 플로우 관리

// 초기 데이터 props
const props = defineProps({
  initialData: {
    type: Object,
    default: () => ({})
  }
})

const store = useTestConfigStore()
const {
  isLoading,
  isSubmitting,
  message,
  messageType,
  loadInitialData,
  startTest,
  saveAsTemplate,
  hideMessage
} = useTestConfig()

// 서버에서 전달받은 초기 데이터로 스토어 초기화
const initializeWithServerData = () => {
  const { initialData } = props
  
  console.log('서버 데이터로 초기화:', initialData)
  
  // 참조 데이터 설정
  if (initialData.examPlans) {
    store.examPlans = initialData.examPlans
  }
  
  if (initialData.scenarios) {
    store.scenarios = initialData.scenarios
  }
  
  if (initialData.runTypes) {
    store.runTypes = initialData.runTypes
  }
  
  // 기본 요청 설정
  if (initialData.defaultRequest) {
    Object.assign(store.testRequest, initialData.defaultRequest)
  }
  
  // 선택된 플랜 설정
  if (initialData.selectedPlan) {
    store.testRequest.planId = initialData.selectedPlan.planId
  }
}

// 메시지 스타일
const messageClasses = computed(() => {
  const baseClasses = 'border'
  switch (messageType.value) {
    case 'success':
      return `${baseClasses} bg-green-100 text-green-700 border-green-300`
    case 'error':
      return `${baseClasses} bg-red-100 text-red-700 border-red-300`
    case 'info':
      return `${baseClasses} bg-blue-100 text-blue-700 border-blue-300`
    default:
      return `${baseClasses} bg-gray-100 text-gray-700 border-gray-300`
  }
})

// 메시지 아이콘
const messageIcon = computed(() => {
  switch (messageType.value) {
    case 'success':
      return 'fas fa-check-circle'
    case 'error':
      return 'fas fa-exclamation-circle'
    case 'info':
      return 'fas fa-info-circle'
    default:
      return 'fas fa-info-circle'
  }
})

// 테스트 시작 핸들러
const handleSubmit = async () => {
  await startTest()
}

// 템플릿 저장 핸들러
const handleSaveTemplate = async () => {
  const templateName = prompt('템플릿 이름을 입력하세요:')
  if (templateName) {
    await saveAsTemplate(templateName)
  }
}

// 초기화
onMounted(() => {
  // 서버 데이터로 초기화
  initializeWithServerData()
  
  // 추가 데이터가 필요한 경우만 API 호출
  if (!props.initialData?.examPlans?.length) {
    loadInitialData()
  }
  
  console.log('Configure 앱 초기화 완료')
})
</script>

<style scoped>
/* 추가적인 스타일이 필요한 경우 여기에 작성 */
</style>