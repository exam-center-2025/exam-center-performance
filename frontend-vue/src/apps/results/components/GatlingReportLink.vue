<!-- 
GatlingReportLink 컴포넌트
Gatling 리포트 링크 표시 및 접근성 개선

AIDEV-NOTE: 기존 results.html의 Gatling 리포트 링크를 Vue로 전환
링크 유효성 검사, 새 창 열기, 로딩 상태 표시
-->
<template>
  <div class="text-center mt-4">
    <!-- 리포트 링크가 있는 경우 -->
    <div v-if="reportUrl" class="report-link-container">
      <a 
        :href="reportUrl"
        target="_blank"
        rel="noopener noreferrer"
        class="report-link"
        @click="handleReportClick"
      >
        <i class="fas fa-external-link-alt mr-2"></i>
        <span class="text-sm">Gatling 상세 리포트 보기</span>
        <i class="fas fa-chart-line ml-2 text-white"></i>
      </a>
      
      <!-- 리포트 정보 -->
      <p class="report-info">
        <i class="fas fa-info-circle mr-1 text-gray-500"></i>
        새 창에서 상세한 성능 분석 리포트를 확인할 수 있습니다
      </p>
    </div>
    
    <!-- 리포트 생성 중 상태 -->
    <div v-else-if="isGenerating" class="report-generating">
      <div class="flex items-center justify-center mb-4">
        <i class="fas fa-cog fa-spin text-xl text-gray-600 mr-2"></i>
        <span class="text-base font-semibold text-gray-700">리포트 생성 중...</span>
      </div>
      <p class="text-gray-600">
        Gatling 리포트를 생성하고 있습니다. 잠시만 기다려주세요.
      </p>
      <div class="progress-bar">
        <div class="progress-fill"></div>
      </div>
    </div>
    
    <!-- 리포트 없음 상태 -->
    <div v-else-if="showNoReportMessage" class="report-unavailable">
      <div class="flex items-center justify-center mb-4">
        <i class="fas fa-file-slash text-xl text-gray-400 mr-2"></i>
        <span class="text-base font-semibold text-gray-600">리포트를 사용할 수 없음</span>
      </div>
      <p class="text-gray-500 mb-4">
        이 테스트에 대한 Gatling 리포트가 생성되지 않았거나 삭제되었습니다.
      </p>
      
      <!-- 리포트 재생성 버튼 (관리자용) -->
      <button 
        v-if="allowRegenerate"
        @click="regenerateReport"
        :disabled="regenerating"
        class="btn-regenerate"
      >
        <i :class="regenerating ? 'fas fa-spinner fa-spin' : 'fas fa-redo'" class="mr-2"></i>
        {{ regenerating ? '재생성 중...' : '리포트 재생성' }}
      </button>
    </div>
    
    <!-- 로딩 상태 -->
    <div v-else class="report-loading">
      <i class="fas fa-spinner fa-spin text-xl text-gray-400"></i>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'

// Props 정의
const props = defineProps({
  reportUrl: {
    type: String,
    default: null
  },
  testId: {
    type: String,
    default: null
  },
  testStatus: {
    type: String,
    default: 'COMPLETED'
  },
  allowRegenerate: {
    type: Boolean,
    default: false
  }
})

// Emits 정의
const emit = defineEmits(['report-clicked', 'regenerate-requested'])

// 상태 관리
const isGenerating = ref(false)
const regenerating = ref(false)
const showNoReportMessage = ref(false)
const linkValidated = ref(false)

// 리포트 생성 중 여부 계산
const isReportGenerating = computed(() => {
  return props.testStatus === 'COMPLETED' && !props.reportUrl && !showNoReportMessage.value
})

// 리포트 링크 클릭 핸들러
const handleReportClick = (event) => {
  // 링크 클릭 이벤트 추적
  emit('report-clicked', {
    url: props.reportUrl,
    testId: props.testId,
    timestamp: new Date().toISOString()
  })
  
  // 분석을 위한 로그
  if (window.gtag) {
    window.gtag('event', 'gatling_report_click', {
      test_id: props.testId,
      report_url: props.reportUrl
    })
  }
  
  console.log('Gatling 리포트 열기:', props.reportUrl)
}

// 리포트 재생성 요청
const regenerateReport = async () => {
  if (regenerating.value) return
  
  regenerating.value = true
  
  try {
    emit('regenerate-requested', props.testId)
    
    // 임시로 생성 중 상태로 전환
    setTimeout(() => {
      isGenerating.value = true
      showNoReportMessage.value = false
    }, 500)
    
  } catch (error) {
    console.error('리포트 재생성 요청 오류:', error)
  } finally {
    regenerating.value = false
  }
}

// 링크 유효성 검사 (옵션)
const validateReportLink = async (url) => {
  if (!url) return false
  
  try {
    // HEAD 요청으로 링크 존재 확인 (CORS 정책에 따라 작동하지 않을 수 있음)
    const response = await fetch(url, { 
      method: 'HEAD', 
      mode: 'no-cors' 
    })
    return true
  } catch (error) {
    console.warn('리포트 링크 검증 실패:', error)
    return true // CORS 오류는 무시하고 링크 유효하다고 간주
  }
}

// Props 변경 감시
watch(() => props.reportUrl, async (newUrl) => {
  if (newUrl) {
    linkValidated.value = await validateReportLink(newUrl)
    showNoReportMessage.value = false
    isGenerating.value = false
  } else {
    // 완료된 테스트인데 리포트가 없으면 시간차를 두고 없음 상태 표시
    if (props.testStatus === 'COMPLETED') {
      setTimeout(() => {
        if (!props.reportUrl) {
          showNoReportMessage.value = true
          isGenerating.value = false
        }
      }, 3000) // 3초 후 없음 상태 표시
    }
  }
})

watch(() => props.testStatus, (newStatus) => {
  if (newStatus === 'COMPLETED' && !props.reportUrl) {
    isGenerating.value = true
    showNoReportMessage.value = false
  }
})

// 컴포넌트 마운트
onMounted(() => {
  if (props.reportUrl) {
    validateReportLink(props.reportUrl)
  } else if (props.testStatus === 'COMPLETED') {
    isGenerating.value = true
    
    // 5초 후에도 리포트가 없으면 없음 상태 표시
    setTimeout(() => {
      if (!props.reportUrl) {
        isGenerating.value = false
        showNoReportMessage.value = true
      }
    }, 5000)
  }
})
</script>

<style scoped>
/* 리포트 링크 컨테이너 */
.report-link-container {
  @apply space-y-3;
}

/* 리포트 링크 버튼 */
.report-link {
  @apply inline-flex items-center gap-2 px-4 py-2 bg-blue-500 text-white font-semibold border border-blue-600;
  @apply hover:bg-blue-600 transition-colors duration-200;
  @apply focus:outline-none;
  
  /* 플랫 디자인 - 둥글기 없음 */
  border-radius: 0;
}

/* AIDEV-NOTE: 플랫 디자인으로 애니메이션 효과 제거 */

/* 리포트 정보 */
.report-info {
  @apply text-sm text-gray-600 mt-2;
}

/* 리포트 생성 중 상태 */
.report-generating {
  @apply bg-blue-50 border border-blue-200 p-4;
}

/* 리포트 없음 상태 */
.report-unavailable {
  @apply bg-gray-50 border border-gray-200 p-4;
}

/* 재생성 버튼 */
.btn-regenerate {
  @apply inline-flex items-center px-4 py-2 bg-gray-600 text-white font-medium border border-gray-600;
  @apply hover:bg-gray-700 hover:border-gray-700 disabled:opacity-50 disabled:cursor-not-allowed;
  @apply transition-all duration-200;
  border-radius: 0; /* 플랫 디자인 */
}

/* 로딩 상태 */
.report-loading {
  @apply py-4;
}

/* 진행률 바 */
.progress-bar {
  @apply w-full bg-gray-200 h-2 mt-4;
  border-radius: 0;
}

.progress-fill {
  @apply h-full bg-indigo-500;
  animation: progress 2s ease-in-out infinite;
}

@keyframes progress {
  0% { width: 0%; }
  50% { width: 70%; }
  100% { width: 0%; }
}

/* 반응형 조정 */
@media (max-width: 768px) {
  .report-link {
    @apply px-4 py-3 text-sm;
  }
  
  .report-link .fas {
    @apply text-sm;
  }
  
  .report-generating,
  .report-unavailable {
    @apply p-4;
  }
}

/* 접근성 개선 */
.report-link:focus {
  outline: 2px solid #6366f1;
  outline-offset: 2px;
}

/* 다크 모드 준비 */
@media (prefers-color-scheme: dark) {
  .report-info {
    @apply text-gray-400;
  }
  
  .report-generating {
    @apply bg-blue-900 border-blue-700;
  }
  
  .report-unavailable {
    @apply bg-gray-800 border-gray-600;
  }
}
</style>