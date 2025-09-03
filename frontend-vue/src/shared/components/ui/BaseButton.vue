<!--
  BaseButton 컴포넌트
  AIDEV-NOTE: 재사용 가능한 버튼 컴포넌트, 다양한 변형과 상태 지원
  @example
  <BaseButton variant="primary" size="large" @click="handleClick">
    클릭하세요
  </BaseButton>
-->
<template>
  <button 
    :type="type"
    :class="buttonClasses"
    :disabled="disabled || loading"
    :aria-label="ariaLabel"
    :aria-describedby="ariaDescribedBy"
    @click="handleClick"
    @focus="$emit('focus', $event)"
    @blur="$emit('blur', $event)"
  >
    <!-- 로딩 스피너 -->
    <div 
      v-if="loading" 
      class="inline-block w-4 h-4 mr-2 animate-spin rounded-full border-2 border-transparent border-t-current"
      aria-hidden="true"
    ></div>
    
    <!-- 아이콘 (앞) -->
    <slot v-if="!loading && $slots.icon" name="icon"></slot>
    
    <!-- 텍스트 -->
    <span v-if="$slots.default" :class="{ 'ml-2': $slots.icon && !loading }">
      <slot></slot>
    </span>
    
    <!-- 아이콘 (뒤) -->
    <slot v-if="!loading && $slots.iconAfter" name="iconAfter"></slot>
  </button>
</template>

<script>
export default {
  name: 'BaseButton',
  props: {
    // 기본 속성
    type: {
      type: String,
      default: 'button',
      validator: (value) => ['button', 'submit', 'reset'].includes(value)
    },
    
    // 버튼 변형
    variant: {
      type: String,
      default: 'primary',
      validator: (value) => [
        'primary', 'secondary', 'danger', 'success', 'warning', 'info',
        'outline', 'ghost', 'link'
      ].includes(value)
    },
    
    // 크기
    size: {
      type: String,
      default: 'default',
      validator: (value) => ['xs', 'sm', 'default', 'lg', 'xl'].includes(value)
    },
    
    // 상태
    disabled: {
      type: Boolean,
      default: false
    },
    loading: {
      type: Boolean,
      default: false
    },
    
    // 스타일 옵션
    fullWidth: {
      type: Boolean,
      default: false
    },
    rounded: {
      type: Boolean,
      default: false
    },
    
    // 접근성
    ariaLabel: {
      type: String,
      default: null
    },
    ariaDescribedBy: {
      type: String,
      default: null
    }
  },
  
  emits: ['click', 'focus', 'blur'],
  
  computed: {
    buttonClasses() {
      const classes = [
        'btn', 
        'inline-flex', 
        'items-center', 
        'justify-center', 
        'font-medium',
        'transition-all',
        'duration-200',
        'focus:outline-none',
        'focus:ring-2',
        'focus:ring-offset-2'
      ];
      
      // 크기에 따른 클래스
      switch (this.size) {
        case 'xs':
          classes.push('px-2', 'py-1', 'text-xs');
          break;
        case 'sm':
          classes.push('px-3', 'py-1.5', 'text-sm');
          break;
        case 'lg':
          classes.push('px-6', 'py-3', 'text-base');
          break;
        case 'xl':
          classes.push('px-8', 'py-4', 'text-lg');
          break;
        default:
          classes.push('px-4', 'py-2', 'text-sm');
      }
      
      // 변형에 따른 클래스
      switch (this.variant) {
        case 'primary':
          classes.push(
            'bg-blue-600', 'text-white', 'border-blue-600',
            'hover:bg-blue-700', 'hover:border-blue-700',
            'focus:ring-blue-500',
            'active:bg-blue-800'
          );
          break;
        case 'secondary':
          classes.push(
            'bg-gray-600', 'text-white', 'border-gray-600',
            'hover:bg-gray-700', 'hover:border-gray-700',
            'focus:ring-gray-500',
            'active:bg-gray-800'
          );
          break;
        case 'danger':
          classes.push(
            'bg-red-600', 'text-white', 'border-red-600',
            'hover:bg-red-700', 'hover:border-red-700',
            'focus:ring-red-500',
            'active:bg-red-800'
          );
          break;
        case 'success':
          classes.push(
            'bg-green-600', 'text-white', 'border-green-600',
            'hover:bg-green-700', 'hover:border-green-700',
            'focus:ring-green-500',
            'active:bg-green-800'
          );
          break;
        case 'warning':
          classes.push(
            'bg-yellow-600', 'text-white', 'border-yellow-600',
            'hover:bg-yellow-700', 'hover:border-yellow-700',
            'focus:ring-yellow-500',
            'active:bg-yellow-800'
          );
          break;
        case 'info':
          classes.push(
            'bg-sky-600', 'text-white', 'border-sky-600',
            'hover:bg-sky-700', 'hover:border-sky-700',
            'focus:ring-sky-500',
            'active:bg-sky-800'
          );
          break;
        case 'outline':
          classes.push(
            'border-2', 'border-gray-300', 'text-gray-700', 'bg-transparent',
            'hover:bg-gray-50', 'hover:border-gray-400',
            'focus:ring-gray-500',
            'active:bg-gray-100'
          );
          break;
        case 'ghost':
          classes.push(
            'text-gray-700', 'bg-transparent', 'border-transparent',
            'hover:bg-gray-100', 'hover:text-gray-900',
            'focus:ring-gray-500',
            'active:bg-gray-200'
          );
          break;
        case 'link':
          classes.push(
            'text-blue-600', 'bg-transparent', 'border-transparent',
            'hover:text-blue-800', 'hover:underline',
            'focus:ring-blue-500',
            'p-0', 'h-auto'
          );
          break;
      }
      
      // 모양 옵션
      if (this.rounded) {
        classes.push('rounded-full');
      } else {
        classes.push('rounded-md');
      }
      
      // 전체 너비
      if (this.fullWidth) {
        classes.push('w-full');
      }
      
      // 비활성화 상태
      if (this.disabled || this.loading) {
        classes.push('opacity-50', 'cursor-not-allowed');
      }
      
      return classes.join(' ');
    }
  },
  
  methods: {
    handleClick(event) {
      if (!this.disabled && !this.loading) {
        this.$emit('click', event);
      }
    }
  }
};
</script>

<style scoped>
.btn {
  border-width: 1px;
  border-style: solid;
}

/* 포커스 링 커스터마이징 */
.btn:focus {
  @apply ring-offset-white;
}

/* 로딩 상태 애니메이션 */
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>