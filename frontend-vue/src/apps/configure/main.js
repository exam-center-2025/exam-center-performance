import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import '@/assets/styles/main.css'

// AIDEV-NOTE: Configure 앱 엔트리 포인트 - Vue 앱 초기화 및 서버 데이터 연동

// DOM이 준비될 때까지 대기
document.addEventListener('DOMContentLoaded', () => {
  const appElement = document.getElementById('configure-app')
  
  if (!appElement) {
    console.error('Configure app mount point not found')
    return
  }
  
  // 서버에서 전달된 초기 데이터 파싱
  const getDataAttribute = (name) => {
    const data = appElement.dataset[name]
    try {
      return data ? JSON.parse(data) : null
    } catch (error) {
      console.warn(`Failed to parse ${name}:`, error)
      return null
    }
  }
  
  const initialData = {
    examPlans: getDataAttribute('examPlans') || [],
    scenarios: getDataAttribute('scenarios') || [],
    runTypes: getDataAttribute('runTypes') || [],
    selectedPlan: getDataAttribute('selectedPlan'),
    defaultRequest: getDataAttribute('defaultRequest') || {
      planId: null,
      runType: 'TEST',
      testName: '',
      maxUsers: 100,
      rampUpSeconds: 60,
      testDurationSeconds: 300,
      scenario: 'COMPLETE',
      additionalConfig: ''
    },
    vueConfig: getDataAttribute('vueConfig') || {},
    user: getDataAttribute('user') || {}
  }
  
  console.log('초기 데이터 로드:', initialData)
  
  // Vue 앱 생성
  const app = createApp(App, {
    initialData
  })
  
  // Pinia 설정
  const pinia = createPinia()
  app.use(pinia)
  
  // 전역 속성 설정
  app.config.globalProperties.$formatNumber = (num) => {
    return new Intl.NumberFormat('ko-KR').format(num)
  }
  
  app.config.globalProperties.$initialData = initialData
  
  // 에러 핸들링
  app.config.errorHandler = (error, vm, info) => {
    console.error('Vue 앱 오류:', error, info)
  }
  
  // 앱 마운트
  try {
    app.mount('#configure-app')
    
    // 로드 성공 마커 추가
    appElement.classList.add('vue-app-loaded')
    
    console.log('Configure Vue 앱 마운트 완료')
    
  } catch (error) {
    console.error('Vue 앱 마운트 실패:', error)
    
    // 오류 시 대체 UI 표시
    appElement.innerHTML = `
      <div class="min-h-screen bg-gray-50 flex items-center justify-center">
        <div class="text-center">
          <div class="bg-red-100 rounded-full p-3 mx-auto mb-4 w-16 h-16 flex items-center justify-center">
            <i class="fas fa-exclamation-triangle text-red-600 text-2xl"></i>
          </div>
          <h3 class="text-lg font-medium text-gray-900 mb-2">앱 초기화 실패</h3>
          <p class="text-gray-600 mb-4">Vue 앱 초기화 중 오류가 발생했습니다.</p>
          <div class="space-x-3">
            <a href="/dashboard" class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700">
              대시보드로
            </a>
            <a href="/dashboard/configure" class="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50">
              기존 버전 사용
            </a>
          </div>
        </div>
      </div>
    `
  }
  
  // 개발 환경에서 전역 접근 허용
  if (import.meta.env.DEV) {
    window.__VUE_CONFIGURE_APP__ = app
    window.__VUE_INITIAL_DATA__ = initialData
  }
})