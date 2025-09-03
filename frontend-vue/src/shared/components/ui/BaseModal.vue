<!--
  BaseModal 컴포넌트
  AIDEV-NOTE: 접근성을 고려한 모달 다이얼로그, 포커스 트랩 및 ESC 키 지원
  @example
  <BaseModal v-model="showModal" title="확인" size="lg">
    <p>정말로 삭제하시겠습니까?</p>
    <template #footer>
      <BaseButton @click="confirm">확인</BaseButton>
      <BaseButton variant="outline" @click="cancel">취소</BaseButton>
    </template>
  </BaseModal>
-->
<template>
  <Teleport to="body">
    <transition
      enter-active-class="transition-opacity duration-200"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-200"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
      @enter="onEnter"
      @after-leave="onAfterLeave"
    >
      <div 
        v-if="modelValue"
        class="fixed inset-0 z-50 overflow-y-auto"
        aria-labelledby="modal-title"
        :aria-describedby="ariaDescribedby"
        role="dialog"
        aria-modal="true"
        @click="handleBackdropClick"
        @keydown.esc="handleEscapeKey"
      >
        <!-- 백드롭 -->
        <div 
          class="fixed inset-0 transition-opacity"
          :class="backdropClasses"
          aria-hidden="true"
        ></div>

        <!-- 모달 컨테이너 -->
        <div class="flex min-h-full items-center justify-center p-4 text-center sm:p-0">
          <transition
            enter-active-class="transition-all duration-200"
            enter-from-class="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
            enter-to-class="opacity-100 translate-y-0 sm:scale-100"
            leave-active-class="transition-all duration-150"
            leave-from-class="opacity-100 translate-y-0 sm:scale-100"
            leave-to-class="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
          >
            <div
              v-if="modelValue"
              ref="modalRef"
              :class="modalClasses"
              @click.stop
            >
              <!-- 닫기 버튼 -->
              <button
                v-if="closable && !hideCloseButton"
                type="button"
                class="absolute top-4 right-4 text-gray-400 hover:text-gray-600 transition-colors duration-200"
                aria-label="닫기"
                @click="handleClose"
                @focus="$refs.closeButton = $event.target"
                ref="closeButton"
              >
                <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>

              <!-- 헤더 -->
              <div v-if="$slots.header || title" :class="headerClasses">
                <div v-if="title" class="flex items-center">
                  <!-- 아이콘 -->
                  <div v-if="icon" class="flex-shrink-0 mr-3">
                    <div :class="iconClasses">
                      <slot name="icon">
                        <svg v-if="variant === 'danger'" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z" />
                        </svg>
                        <svg v-else-if="variant === 'success'" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        <svg v-else-if="variant === 'warning'" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        <svg v-else class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                      </slot>
                    </div>
                  </div>
                  
                  <!-- 제목 -->
                  <h3 
                    id="modal-title" 
                    :class="titleClasses"
                  >
                    {{ title }}
                  </h3>
                </div>
                
                <!-- 커스텀 헤더 -->
                <slot name="header"></slot>
              </div>

              <!-- 본문 -->
              <div :class="bodyClasses">
                <slot></slot>
              </div>

              <!-- 푸터 -->
              <div 
                v-if="$slots.footer || showDefaultButtons" 
                :class="footerClasses"
              >
                <slot name="footer">
                  <!-- 기본 버튼들 -->
                  <div v-if="showDefaultButtons" class="flex space-x-3">
                    <button
                      type="button"
                      :class="confirmButtonClasses"
                      @click="handleConfirm"
                    >
                      {{ confirmText }}
                    </button>
                    <button
                      type="button"
                      class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                      @click="handleCancel"
                    >
                      {{ cancelText }}
                    </button>
                  </div>
                </slot>
              </div>
            </div>
          </transition>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script>
export default {
  name: 'BaseModal',
  props: {
    modelValue: {
      type: Boolean,
      default: false
    },
    
    // 기본 속성
    title: {
      type: String,
      default: ''
    },
    
    // 크기
    size: {
      type: String,
      default: 'default',
      validator: (value) => ['xs', 'sm', 'default', 'lg', 'xl', '2xl', 'full'].includes(value)
    },
    
    // 변형
    variant: {
      type: String,
      default: 'default',
      validator: (value) => ['default', 'danger', 'success', 'warning', 'info'].includes(value)
    },
    
    // 옵션
    closable: {
      type: Boolean,
      default: true
    },
    closeOnBackdrop: {
      type: Boolean,
      default: true
    },
    closeOnEsc: {
      type: Boolean,
      default: true
    },
    hideCloseButton: {
      type: Boolean,
      default: false
    },
    
    // 기본 버튼
    showDefaultButtons: {
      type: Boolean,
      default: false
    },
    confirmText: {
      type: String,
      default: '확인'
    },
    cancelText: {
      type: String,
      default: '취소'
    },
    
    // 아이콘
    icon: {
      type: Boolean,
      default: false
    },
    
    // 접근성
    ariaDescribedby: {
      type: String,
      default: null
    },
    
    // 스타일링
    noPadding: {
      type: Boolean,
      default: false
    }
  },
  
  emits: ['update:modelValue', 'close', 'confirm', 'cancel'],
  
  data() {
    return {
      previousActiveElement: null
    };
  },
  
  computed: {
    modalClasses() {
      const classes = [
        'relative',
        'transform',
        'overflow-hidden',
        'rounded-lg',
        'bg-white',
        'text-left',
        'shadow-xl',
        'transition-all',
        'w-full'
      ];
      
      // 크기에 따른 클래스
      switch (this.size) {
        case 'xs':
          classes.push('max-w-xs');
          break;
        case 'sm':
          classes.push('max-w-sm');
          break;
        case 'lg':
          classes.push('max-w-lg');
          break;
        case 'xl':
          classes.push('max-w-xl');
          break;
        case '2xl':
          classes.push('max-w-2xl');
          break;
        case 'full':
          classes.push('max-w-full', 'h-full', 'm-0');
          break;
        default:
          classes.push('max-w-md');
      }
      
      if (this.size !== 'full') {
        classes.push('sm:my-8', 'sm:align-middle');
      }
      
      return classes.join(' ');
    },
    
    backdropClasses() {
      return 'bg-gray-500 bg-opacity-75';
    },
    
    headerClasses() {
      const classes = [];
      
      if (this.noPadding) {
        classes.push('px-6', 'pt-6', 'pb-4');
      } else {
        classes.push('px-6', 'py-4', 'border-b', 'border-gray-200');
      }
      
      return classes.join(' ');
    },
    
    titleClasses() {
      const classes = ['text-lg', 'font-semibold', 'text-gray-900'];
      
      if (this.variant === 'danger') {
        classes.push('text-red-600');
      } else if (this.variant === 'success') {
        classes.push('text-green-600');
      } else if (this.variant === 'warning') {
        classes.push('text-yellow-600');
      }
      
      return classes.join(' ');
    },
    
    bodyClasses() {
      const classes = [];
      
      if (this.noPadding) {
        classes.push('px-6');
      } else {
        classes.push('px-6', 'py-4');
      }
      
      return classes.join(' ');
    },
    
    footerClasses() {
      const classes = ['px-6', 'py-4', 'bg-gray-50', 'flex', 'justify-end'];
      
      return classes.join(' ');
    },
    
    iconClasses() {
      const classes = ['flex', 'h-10', 'w-10', 'items-center', 'justify-center', 'rounded-full'];
      
      switch (this.variant) {
        case 'danger':
          classes.push('bg-red-100', 'text-red-600');
          break;
        case 'success':
          classes.push('bg-green-100', 'text-green-600');
          break;
        case 'warning':
          classes.push('bg-yellow-100', 'text-yellow-600');
          break;
        default:
          classes.push('bg-blue-100', 'text-blue-600');
      }
      
      return classes.join(' ');
    },
    
    confirmButtonClasses() {
      const classes = [
        'px-4', 'py-2', 'text-sm', 'font-medium', 'text-white', 
        'rounded-md', 'focus:outline-none', 'focus:ring-2', 
        'focus:ring-offset-2', 'transition-colors', 'duration-200'
      ];
      
      switch (this.variant) {
        case 'danger':
          classes.push('bg-red-600', 'hover:bg-red-700', 'focus:ring-red-500');
          break;
        case 'success':
          classes.push('bg-green-600', 'hover:bg-green-700', 'focus:ring-green-500');
          break;
        case 'warning':
          classes.push('bg-yellow-600', 'hover:bg-yellow-700', 'focus:ring-yellow-500');
          break;
        default:
          classes.push('bg-blue-600', 'hover:bg-blue-700', 'focus:ring-blue-500');
      }
      
      return classes.join(' ');
    }
  },
  
  watch: {
    modelValue(newVal) {
      if (newVal) {
        this.setupModal();
      } else {
        this.cleanupModal();
      }
    }
  },
  
  methods: {
    handleClose() {
      if (this.closable) {
        this.$emit('update:modelValue', false);
        this.$emit('close');
      }
    },
    
    handleBackdropClick() {
      if (this.closeOnBackdrop && this.closable) {
        this.handleClose();
      }
    },
    
    handleEscapeKey() {
      if (this.closeOnEsc && this.closable) {
        this.handleClose();
      }
    },
    
    handleConfirm() {
      this.$emit('confirm');
    },
    
    handleCancel() {
      this.$emit('cancel');
      this.handleClose();
    },
    
    setupModal() {
      // 현재 포커스된 요소 저장
      this.previousActiveElement = document.activeElement;
      
      // body 스크롤 방지
      document.body.style.overflow = 'hidden';
      
      // 모달이 완전히 렌더링된 후 포커스 설정
      this.$nextTick(() => {
        if (this.$refs.modalRef) {
          this.$refs.modalRef.focus();
        }
      });
    },
    
    cleanupModal() {
      // body 스크롤 복원
      document.body.style.overflow = '';
      
      // 이전 포커스 복원
      if (this.previousActiveElement) {
        this.previousActiveElement.focus();
        this.previousActiveElement = null;
      }
    },
    
    onEnter() {
      this.setupModal();
    },
    
    onAfterLeave() {
      this.cleanupModal();
    }
  },
  
  beforeUnmount() {
    this.cleanupModal();
  }
};
</script>

<style scoped>
/* 모달이 열릴 때 스크롤바 깜빠짐 방지 */
.modal-enter-active,
.modal-leave-active {
  transition: all 0.2s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
</style>