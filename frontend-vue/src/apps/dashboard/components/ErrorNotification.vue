<!--
  에러 알림 컴포넌트
  AIDEV-NOTE: 전역 에러 상태를 사용자에게 표시
-->
<template>
  <Teleport to="body">
    <div 
      v-if="hasVisibleErrors"
      class="fixed bottom-4 right-4 z-50 space-y-2"
    >
      <div
        v-for="(error, key) in visibleErrors"
        :key="key"
        class="bg-red-50 border border-red-200 rounded-lg shadow-lg p-4 max-w-sm animate-slide-in"
      >
        <div class="flex items-start">
          <div class="flex-shrink-0">
            <i class="fas fa-exclamation-circle text-red-500 text-lg"></i>
          </div>
          <div class="ml-3 flex-1">
            <h3 class="text-sm font-medium text-red-800">
              {{ getErrorTitle(key) }}
            </h3>
            <p class="mt-1 text-sm text-red-700">
              {{ error }}
            </p>
          </div>
          <div class="ml-4 flex-shrink-0">
            <button
              @click="dismissError(key)"
              class="inline-flex text-red-400 hover:text-red-600 focus:outline-none focus:text-red-600"
            >
              <i class="fas fa-times text-sm"></i>
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'ErrorNotification',
  props: {
    errors: {
      type: Object,
      required: true
    }
  },
  
  emits: ['close', 'dismiss'],
  
  setup(props, { emit }) {
    const visibleErrors = computed(() => {
      const visible = {}
      Object.keys(props.errors).forEach(key => {
        if (props.errors[key]) {
          visible[key] = props.errors[key]
        }
      })
      return visible
    })
    
    const hasVisibleErrors = computed(() => {
      return Object.keys(visibleErrors.value).length > 0
    })
    
    const getErrorTitle = (errorType) => {
      const titles = {
        loadData: '데이터 로드 실패',
        startTest: '테스트 시작 실패',
        stopTest: '테스트 중지 실패',
        connection: '연결 오류',
        validation: '입력 검증 오류'
      }
      return titles[errorType] || '오류 발생'
    }
    
    const dismissError = (errorType) => {
      emit('dismiss', errorType)
    }
    
    // 5초 후 자동 해제
    setTimeout(() => {
      if (hasVisibleErrors.value) {
        emit('close')
      }
    }, 5000)
    
    return {
      visibleErrors,
      hasVisibleErrors,
      getErrorTitle,
      dismissError
    }
  }
}
</script>

<style scoped>
@keyframes slide-in {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.animate-slide-in {
  animation: slide-in 0.3s ease-out;
}
</style>