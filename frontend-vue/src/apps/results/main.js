/*
Results 앱 엔트리 포인트
테스트 결과 상세 페이지를 위한 Vue 3 앱 초기화

AIDEV-NOTE: Pinia 스토어 연동, 전역 스타일 적용
*/
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'

// 공유 스타일 및 유틸리티
import '../../shared/styles/common.css'

// Pinia 스토어 생성
const pinia = createPinia()

// Vue 앱 생성 및 설정
const app = createApp(App)

app.use(pinia)

// 에러 핸들러 설정
app.config.errorHandler = (err, vm, info) => {
  console.error('Vue 앱 에러:', err, info)
  
  // 개발 환경에서는 상세 에러 정보 표시
  if (import.meta.env.DEV) {
    console.error('컴포넌트:', vm)
  }
  
  // 사용자에게 에러 알림 (추후 toast 시스템 연동)
  if (window.showNotification) {
    window.showNotification('앱 오류가 발생했습니다.', 'error')
  }
}

// 전역 속성 설정
app.config.globalProperties.$formatNumber = (num, decimals = 0) => {
  if (num == null) return 'N/A'
  return new Intl.NumberFormat('ko-KR', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  }).format(num)
}

app.config.globalProperties.$formatDateTime = (dateTime, format = 'full') => {
  if (!dateTime) return 'N/A'
  
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
}

// 앱 마운트
app.mount('#app')

// 개발 환경에서 Vue DevTools 활성화
if (import.meta.env.DEV) {
  app.config.devtools = true
  console.log('Results 앱이 개발 모드로 시작되었습니다.')
}

export default app