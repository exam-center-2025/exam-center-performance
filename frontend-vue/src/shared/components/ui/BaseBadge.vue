<!--
  BaseBadge 컴포넌트
  AIDEV-NOTE: 상태 배지, 다양한 변형과 크기 지원
  @example
  <BaseBadge variant="success" size="lg">활성</BaseBadge>
  <BaseBadge variant="warning" :dot="true">대기중</BaseBadge>
-->
<template>
  <span :class="badgeClasses" :aria-label="ariaLabel">
    <!-- 도트 아이콘 -->
    <svg 
      v-if="dot" 
      class="dot-icon" 
      :class="dotClasses"
      fill="currentColor" 
      viewBox="0 0 8 8"
    >
      <circle cx="4" cy="4" r="3" />
    </svg>
    
    <!-- 커스텀 아이콘 -->
    <span v-if="$slots.icon && !dot" class="icon-wrapper" :class="iconClasses">
      <slot name="icon"></slot>
    </span>
    
    <!-- 텍스트 내용 -->
    <span v-if="$slots.default" :class="textClasses">
      <slot></slot>
    </span>
    
    <!-- 닫기 버튼 -->
    <button
      v-if="closable"
      type="button"
      class="close-button"
      :class="closeButtonClasses"
      @click="handleClose"
      :aria-label="closeAriaLabel"
    >
      <svg class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
      </svg>
    </button>
  </span>
</template>

<script>
export default {
  name: 'BaseBadge',
  props: {
    // 변형
    variant: {
      type: String,
      default: 'default',
      validator: (value) => [
        'default', 'primary', 'secondary', 'success', 'danger', 'warning', 'info',
        'light', 'dark', 'outline'
      ].includes(value)
    },
    
    // 크기
    size: {
      type: String,
      default: 'default',
      validator: (value) => ['xs', 'sm', 'default', 'lg', 'xl'].includes(value)
    },
    
    // 형태
    shape: {
      type: String,
      default: 'rounded',
      validator: (value) => ['rounded', 'pill', 'square'].includes(value)
    },
    
    // 옵션
    dot: {
      type: Boolean,
      default: false
    },
    closable: {
      type: Boolean,
      default: false
    },
    clickable: {
      type: Boolean,
      default: false
    },
    
    // 접근성
    ariaLabel: {
      type: String,
      default: null
    },
    closeAriaLabel: {
      type: String,
      default: '배지 제거'
    }
  },
  
  emits: ['close', 'click'],
  
  computed: {
    badgeClasses() {
      const classes = [
        'badge',
        'inline-flex',
        'items-center',
        'font-medium',
        'transition-all',
        'duration-200'
      ];
      
      // 크기에 따른 클래스
      switch (this.size) {
        case 'xs':
          classes.push('text-xs', 'px-2', 'py-0.5');
          break;
        case 'sm':
          classes.push('text-xs', 'px-2.5', 'py-0.5');
          break;
        case 'lg':
          classes.push('text-sm', 'px-3', 'py-1');
          break;
        case 'xl':
          classes.push('text-base', 'px-4', 'py-1.5');
          break;
        default:
          classes.push('text-xs', 'px-2.5', 'py-0.5');
      }
      
      // 형태에 따른 클래스
      switch (this.shape) {
        case 'pill':
          classes.push('rounded-full');
          break;
        case 'square':
          classes.push('rounded-none');
          break;
        default:
          classes.push('rounded');
      }
      
      // 변형에 따른 색상
      this.addVariantClasses(classes);
      
      // 클릭 가능한 경우
      if (this.clickable) {
        classes.push('cursor-pointer', 'hover:opacity-80');
      }
      
      return classes.join(' ');
    },
    
    dotClasses() {
      const classes = ['flex-shrink-0'];
      
      switch (this.size) {
        case 'xs':
          classes.push('w-1.5', 'h-1.5');
          break;
        case 'sm':
          classes.push('w-2', 'h-2');
          break;
        case 'lg':
          classes.push('w-2.5', 'h-2.5');
          break;
        case 'xl':
          classes.push('w-3', 'h-3');
          break;
        default:
          classes.push('w-2', 'h-2');
      }
      
      if (this.$slots.default) {
        classes.push('mr-1.5');
      }
      
      return classes.join(' ');
    },
    
    iconClasses() {
      const classes = ['flex-shrink-0'];
      
      switch (this.size) {
        case 'xs':
          classes.push('w-3', 'h-3');
          break;
        case 'sm':
          classes.push('w-3', 'h-3');
          break;
        case 'lg':
          classes.push('w-4', 'h-4');
          break;
        case 'xl':
          classes.push('w-5', 'h-5');
          break;
        default:
          classes.push('w-3', 'h-3');
      }
      
      if (this.$slots.default) {
        classes.push('mr-1.5');
      }
      
      return classes.join(' ');
    },
    
    textClasses() {
      const classes = [];
      
      if (this.closable) {
        classes.push('mr-1');
      }
      
      return classes.join(' ');
    },
    
    closeButtonClasses() {
      const classes = [
        'flex-shrink-0',
        'ml-0.5',
        'h-4',
        'w-4',
        'rounded-full',
        'inline-flex',
        'items-center',
        'justify-center',
        'hover:bg-black',
        'hover:bg-opacity-20',
        'focus:outline-none',
        'focus:bg-black',
        'focus:bg-opacity-20',
        'transition-colors',
        'duration-200'
      ];
      
      return classes.join(' ');
    }
  },
  
  methods: {
    addVariantClasses(classes) {
      switch (this.variant) {
        case 'primary':
          if (this.variant === 'outline') {
            classes.push('text-blue-700', 'bg-transparent', 'ring-1', 'ring-inset', 'ring-blue-700/10');
          } else {
            classes.push('text-blue-50', 'bg-blue-500');
          }
          break;
          
        case 'secondary':
          if (this.variant === 'outline') {
            classes.push('text-gray-600', 'bg-transparent', 'ring-1', 'ring-inset', 'ring-gray-500/10');
          } else {
            classes.push('text-gray-50', 'bg-gray-500');
          }
          break;
          
        case 'success':
          if (this.variant === 'outline') {
            classes.push('text-green-700', 'bg-transparent', 'ring-1', 'ring-inset', 'ring-green-700/10');
          } else {
            classes.push('text-green-50', 'bg-green-500');
          }
          break;
          
        case 'danger':
          if (this.variant === 'outline') {
            classes.push('text-red-700', 'bg-transparent', 'ring-1', 'ring-inset', 'ring-red-700/10');
          } else {
            classes.push('text-red-50', 'bg-red-500');
          }
          break;
          
        case 'warning':
          if (this.variant === 'outline') {
            classes.push('text-yellow-800', 'bg-transparent', 'ring-1', 'ring-inset', 'ring-yellow-600/20');
          } else {
            classes.push('text-yellow-50', 'bg-yellow-500');
          }
          break;
          
        case 'info':
          if (this.variant === 'outline') {
            classes.push('text-sky-700', 'bg-transparent', 'ring-1', 'ring-inset', 'ring-sky-700/10');
          } else {
            classes.push('text-sky-50', 'bg-sky-500');
          }
          break;
          
        case 'light':
          classes.push('text-gray-800', 'bg-gray-100');
          break;
          
        case 'dark':
          classes.push('text-gray-50', 'bg-gray-800');
          break;
          
        case 'outline':
          classes.push('text-gray-500', 'bg-transparent', 'ring-1', 'ring-inset', 'ring-gray-500/10');
          break;
          
        default:
          classes.push('text-gray-600', 'bg-gray-50', 'ring-1', 'ring-inset', 'ring-gray-500/10');
      }
    },
    
    handleClose(event) {
      event.stopPropagation();
      this.$emit('close', event);
    },
    
    handleClick(event) {
      if (this.clickable) {
        this.$emit('click', event);
      }
    }
  }
};
</script>

<style scoped>
.badge {
  position: relative;
  white-space: nowrap;
}

/* 도트 아이콘 */
.dot-icon {
  animation: none;
}

/* 펄스 애니메이션 (필요시 적용) */
.badge.pulse .dot-icon {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: .5;
  }
}

/* 호버 효과 */
.badge:hover {
  transform: translateY(-1px);
}

/* 포커스 스타일 */
.badge:focus {
  outline: 2px solid transparent;
  outline-offset: 2px;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.5);
}

/* 닫기 버튼 */
.close-button:hover svg {
  transform: scale(1.1);
}
</style>