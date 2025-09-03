<!--
  BaseCard 컴포넌트
  AIDEV-NOTE: 재사용 가능한 카드 UI, 헤더/본문/푸터 슬롯 지원
  @example
  <BaseCard title="제목" subtitle="부제목">
    <template #header>
      <button>액션</button>
    </template>
    <p>카드 내용입니다.</p>
    <template #footer>
      <div>푸터 내용</div>
    </template>
  </BaseCard>
-->
<template>
  <div 
    :class="cardClasses"
    :style="cardStyles"
    @click="handleClick"
    :role="clickable ? 'button' : null"
    :tabindex="clickable && !disabled ? '0' : null"
    @keydown.enter="handleKeydown"
    @keydown.space="handleKeydown"
  >
    <!-- 헤더 슬롯 -->
    <div 
      v-if="$slots.header || title || subtitle" 
      class="card-header"
      :class="headerClasses"
    >
      <div v-if="title || subtitle" class="flex-1 min-w-0">
        <h3 v-if="title" class="card-title" :class="titleClasses">
          {{ title }}
        </h3>
        <p v-if="subtitle" class="text-sm text-gray-500 mt-1 truncate">
          {{ subtitle }}
        </p>
      </div>
      <div v-if="$slots.header" class="flex-shrink-0 ml-4">
        <slot name="header"></slot>
      </div>
    </div>

    <!-- 메인 콘텐츠 -->
    <div 
      v-if="$slots.default" 
      class="card-content"
      :class="contentClasses"
    >
      <slot></slot>
    </div>

    <!-- 푸터 슬롯 -->
    <div 
      v-if="$slots.footer" 
      class="card-footer"
      :class="footerClasses"
    >
      <slot name="footer"></slot>
    </div>

    <!-- 로딩 오버레이 -->
    <transition 
      enter-active-class="transition-opacity duration-200"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-200"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div 
        v-if="loading" 
        class="absolute inset-0 bg-white bg-opacity-75 flex items-center justify-center rounded-lg"
        :class="{ 'rounded-xl': variant === 'elevated' }"
      >
        <div class="flex items-center">
          <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
          <span v-if="loadingText" class="ml-3 text-sm text-gray-600 font-medium">
            {{ loadingText }}
          </span>
        </div>
      </div>
    </transition>

    <!-- 에러 상태 -->
    <div v-if="error && !loading" class="p-4 bg-red-50 border border-red-200 rounded-md">
      <div class="flex">
        <div class="flex-shrink-0">
          <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
          </svg>
        </div>
        <div class="ml-3">
          <p class="text-sm text-red-800">{{ errorMessage || '오류가 발생했습니다.' }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BaseCard',
  props: {
    // 기본 속성
    title: {
      type: String,
      default: ''
    },
    subtitle: {
      type: String,
      default: ''
    },
    
    // 스타일 변형
    variant: {
      type: String,
      default: 'default',
      validator: (value) => ['default', 'outlined', 'elevated', 'flat', 'bordered'].includes(value)
    },
    
    // 크기
    size: {
      type: String,
      default: 'default',
      validator: (value) => ['xs', 'sm', 'default', 'lg', 'xl'].includes(value)
    },
    
    // 상태
    loading: {
      type: Boolean,
      default: false
    },
    loadingText: {
      type: String,
      default: ''
    },
    error: {
      type: Boolean,
      default: false
    },
    errorMessage: {
      type: String,
      default: ''
    },
    
    // 인터랙션
    clickable: {
      type: Boolean,
      default: false
    },
    disabled: {
      type: Boolean,
      default: false
    },
    hoverable: {
      type: Boolean,
      default: false
    },
    
    // 커스텀 클래스
    customClass: {
      type: [String, Array, Object],
      default: ''
    },
    
    // 커스텀 스타일
    customStyle: {
      type: Object,
      default: () => ({})
    },
    
    // 패딩 제거
    noPadding: {
      type: Boolean,
      default: false
    }
  },
  
  emits: ['click'],
  
  computed: {
    cardClasses() {
      const classes = ['card', 'relative', 'bg-white'];
      
      // 변형에 따른 스타일
      switch (this.variant) {
        case 'outlined':
          classes.push('border-2', 'border-gray-200', 'shadow-none');
          break;
        case 'elevated':
          classes.push('shadow-xl', 'border-0', 'rounded-xl');
          break;
        case 'bordered':
          classes.push('border', 'border-gray-200', 'shadow-sm');
          break;
        case 'flat':
          classes.push('shadow-none', 'border-none');
          break;
        default:
          classes.push('shadow-md', 'border', 'border-gray-100');
      }
      
      // 크기에 따른 패딩 (noPadding이 false일 때만)
      if (!this.noPadding) {
        switch (this.size) {
          case 'xs':
            classes.push('p-2');
            break;
          case 'sm':
            classes.push('p-4');
            break;
          case 'lg':
            classes.push('p-8');
            break;
          case 'xl':
            classes.push('p-10');
            break;
          default:
            classes.push('p-6');
        }
      }
      
      // 모서리 둥글기
      if (this.variant !== 'elevated') {
        classes.push('rounded-lg');
      }
      
      // 상태에 따른 스타일
      if (this.clickable && !this.disabled) {
        classes.push('cursor-pointer', 'transition-all', 'duration-200');
        if (this.hoverable) {
          classes.push('hover:shadow-lg', 'hover:-translate-y-1');
        } else {
          classes.push('hover:shadow-lg');
        }
      }
      
      if (this.disabled) {
        classes.push('opacity-50', 'cursor-not-allowed');
      }
      
      // 커스텀 클래스 추가
      if (this.customClass) {
        if (Array.isArray(this.customClass)) {
          classes.push(...this.customClass);
        } else if (typeof this.customClass === 'string') {
          classes.push(this.customClass);
        }
      }
      
      return classes.join(' ');
    },
    
    headerClasses() {
      const classes = ['flex', 'items-start'];
      
      switch (this.size) {
        case 'xs':
          classes.push('mb-1', 'pb-1');
          break;
        case 'sm':
          classes.push('mb-2', 'pb-2');
          break;
        case 'lg':
          classes.push('mb-6', 'pb-4', 'border-b', 'border-gray-100');
          break;
        case 'xl':
          classes.push('mb-8', 'pb-6', 'border-b', 'border-gray-100');
          break;
        default:
          classes.push('mb-4', 'pb-2');
      }
      
      return classes.join(' ');
    },
    
    titleClasses() {
      const classes = ['text-gray-900', 'font-semibold'];
      
      switch (this.size) {
        case 'xs':
          classes.push('text-sm');
          break;
        case 'sm':
          classes.push('text-base');
          break;
        case 'lg':
          classes.push('text-xl');
          break;
        case 'xl':
          classes.push('text-2xl', 'font-bold');
          break;
        default:
          classes.push('text-lg');
      }
      
      return classes.join(' ');
    },
    
    contentClasses() {
      const classes = [];
      
      if (this.size === 'xs') {
        classes.push('text-sm');
      }
      
      return classes.join(' ');
    },
    
    footerClasses() {
      const classes = ['mt-4', 'pt-4', 'border-t', 'border-gray-100'];
      
      if (this.size === 'xs') {
        classes.push('mt-2', 'pt-2');
      } else if (this.size === 'lg' || this.size === 'xl') {
        classes.push('mt-6', 'pt-6');
      }
      
      return classes.join(' ');
    },
    
    cardStyles() {
      return {
        ...this.customStyle
      };
    }
  },
  
  methods: {
    handleClick(event) {
      if (!this.disabled && this.clickable) {
        this.$emit('click', event);
      }
    },
    
    handleKeydown(event) {
      if (!this.disabled && this.clickable && (event.key === 'Enter' || event.key === ' ')) {
        event.preventDefault();
        this.$emit('click', event);
      }
    }
  }
};
</script>

<style scoped>
.card {
  overflow: hidden;
}

/* 포커스 스타일 */
.card[role="button"]:focus {
  @apply outline-none ring-2 ring-blue-500 ring-offset-2;
}

/* 호버 애니메이션 */
.card {
  transform: translateZ(0);
  will-change: transform, box-shadow;
}
</style>