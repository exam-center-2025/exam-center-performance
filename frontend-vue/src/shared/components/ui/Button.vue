<!--
  Button 컴포넌트
  AIDEV-NOTE: 다양한 변형과 상태를 지원하는 버튼 컴포넌트
-->
<template>
  <button 
    :type="type"
    :class="buttonClasses"
    :disabled="disabled || loading"
    @click="handleClick"
  >
    <!-- 로딩 스피너 -->
    <div 
      v-if="loading" 
      class="inline-block w-4 h-4 mr-2 animate-spin rounded-full border-2 border-transparent border-t-current"
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
  name: 'Button',
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
      validator: (value) => ['small', 'default', 'large'].includes(value)
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
    }
  },
  
  emits: ['click'],
  
  computed: {
    buttonClasses() {
      const classes = ['btn', 'inline-flex', 'items-center', 'justify-center', 'transition-all-300'];
      
      // 크기에 따른 클래스
      switch (this.size) {
        case 'small':
          classes.push('px-3', 'py-1.5', 'text-sm');
          break;
        case 'large':
          classes.push('px-6', 'py-3', 'text-base');
          break;
        default:
          classes.push('px-4', 'py-2', 'text-sm');
      }
      
      // 변형에 따른 클래스
      switch (this.variant) {
        case 'primary':
          classes.push('btn-primary');
          break;
        case 'secondary':
          classes.push('btn-secondary');
          break;
        case 'danger':
          classes.push('btn-danger');
          break;
        case 'success':
          classes.push('bg-green-600', 'text-white', 'hover:bg-green-700', 
                      'focus:ring-green-500');
          break;
        case 'warning':
          classes.push('bg-yellow-600', 'text-white', 'hover:bg-yellow-700', 
                      'focus:ring-yellow-500');
          break;
        case 'info':
          classes.push('bg-blue-500', 'text-white', 'hover:bg-blue-600', 
                      'focus:ring-blue-500');
          break;
        case 'outline':
          classes.push('border-2', 'border-gray-300', 'text-gray-700', 'bg-transparent',
                      'hover:bg-gray-50', 'focus:ring-gray-500');
          break;
        case 'ghost':
          classes.push('text-gray-700', 'bg-transparent', 'hover:bg-gray-100', 
                      'focus:ring-gray-500');
          break;
        case 'link':
          classes.push('text-blue-600', 'bg-transparent', 'hover:text-blue-800', 
                      'underline', 'p-0', 'h-auto');
          break;
      }
      
      // 공통 포커스 스타일 (link 제외)
      if (this.variant !== 'link') {
        classes.push('focus:outline-none', 'focus:ring-2', 'focus:ring-offset-2');
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
      
      return classes;
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