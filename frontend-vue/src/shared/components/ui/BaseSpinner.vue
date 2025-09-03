<!--
  BaseSpinner 컴포넌트
  AIDEV-NOTE: 다양한 스타일과 크기의 로딩 스피너
  @example
  <BaseSpinner size="lg" variant="primary" />
  <BaseSpinner type="pulse" :show="loading" />
-->
<template>
  <div 
    v-if="show"
    :class="containerClasses"
    role="status"
    :aria-label="ariaLabel"
  >
    <!-- 스핀 타입 -->
    <div 
      v-if="type === 'spin'"
      :class="spinClasses"
    >
      <svg 
        class="animate-spin" 
        :class="iconClasses"
        xmlns="http://www.w3.org/2000/svg" 
        fill="none" 
        viewBox="0 0 24 24"
      >
        <circle 
          class="opacity-25" 
          cx="12" 
          cy="12" 
          r="10" 
          stroke="currentColor" 
          stroke-width="4"
        />
        <path 
          class="opacity-75" 
          fill="currentColor" 
          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
        />
      </svg>
    </div>
    
    <!-- 도트 타입 -->
    <div 
      v-else-if="type === 'dots'"
      :class="dotsClasses"
    >
      <div class="dot" :style="dotStyle"></div>
      <div class="dot" :style="dotStyle"></div>
      <div class="dot" :style="dotStyle"></div>
    </div>
    
    <!-- 펄스 타입 -->
    <div 
      v-else-if="type === 'pulse'"
      :class="pulseClasses"
    ></div>
    
    <!-- 바 타입 -->
    <div 
      v-else-if="type === 'bars'"
      :class="barsClasses"
    >
      <div class="bar" :style="barStyle"></div>
      <div class="bar" :style="barStyle"></div>
      <div class="bar" :style="barStyle"></div>
      <div class="bar" :style="barStyle"></div>
    </div>
    
    <!-- 링 타입 -->
    <div 
      v-else-if="type === 'ring'"
      :class="ringClasses"
    ></div>
    
    <!-- 텍스트 -->
    <div 
      v-if="text || $slots.default" 
      :class="textClasses"
    >
      <slot>{{ text }}</slot>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BaseSpinner',
  props: {
    // 기본 속성
    show: {
      type: Boolean,
      default: true
    },
    
    // 타입
    type: {
      type: String,
      default: 'spin',
      validator: (value) => ['spin', 'dots', 'pulse', 'bars', 'ring'].includes(value)
    },
    
    // 크기
    size: {
      type: String,
      default: 'default',
      validator: (value) => ['xs', 'sm', 'default', 'lg', 'xl'].includes(value)
    },
    
    // 변형
    variant: {
      type: String,
      default: 'primary',
      validator: (value) => ['primary', 'secondary', 'success', 'danger', 'warning', 'info', 'white'].includes(value)
    },
    
    // 텍스트
    text: {
      type: String,
      default: ''
    },
    
    // 중앙 정렬
    center: {
      type: Boolean,
      default: false
    },
    
    // 오버레이
    overlay: {
      type: Boolean,
      default: false
    },
    
    // 접근성
    ariaLabel: {
      type: String,
      default: '로딩 중...'
    }
  },
  
  computed: {
    containerClasses() {
      const classes = ['base-spinner'];
      
      if (this.center) {
        classes.push('flex', 'items-center', 'justify-center');
      } else {
        classes.push('inline-flex', 'items-center');
      }
      
      if (this.overlay) {
        classes.push(
          'fixed', 'inset-0', 'bg-white', 'bg-opacity-75', 'z-50',
          'flex', 'items-center', 'justify-center'
        );
      }
      
      return classes.join(' ');
    },
    
    spinClasses() {
      const classes = ['spinner-spin'];
      this.addVariantClasses(classes);
      return classes.join(' ');
    },
    
    dotsClasses() {
      const classes = ['spinner-dots', 'flex', 'space-x-1'];
      return classes.join(' ');
    },
    
    pulseClasses() {
      const classes = ['spinner-pulse', 'animate-pulse', 'rounded-full'];
      this.addSizeClasses(classes);
      this.addVariantClasses(classes);
      return classes.join(' ');
    },
    
    barsClasses() {
      const classes = ['spinner-bars', 'flex', 'space-x-1'];
      return classes.join(' ');
    },
    
    ringClasses() {
      const classes = ['spinner-ring', 'animate-spin', 'rounded-full', 'border-4'];
      this.addSizeClasses(classes);
      this.addRingVariantClasses(classes);
      return classes.join(' ');
    },
    
    iconClasses() {
      const classes = [];
      this.addSizeClasses(classes);
      return classes.join(' ');
    },
    
    textClasses() {
      const classes = ['spinner-text', 'text-sm', 'font-medium'];
      
      if (this.type !== 'pulse') {
        classes.push('ml-3');
      } else {
        classes.push('mt-2');
      }
      
      this.addTextVariantClasses(classes);
      
      return classes.join(' ');
    },
    
    dotStyle() {
      const color = this.getColor();
      const size = this.getDotSize();
      
      return {
        width: size,
        height: size,
        backgroundColor: color,
        borderRadius: '50%',
        animationName: 'bounce',
        animationDuration: '1.4s',
        animationIterationCount: 'infinite',
        animationFillMode: 'both'
      };
    },
    
    barStyle() {
      const color = this.getColor();
      const width = this.getBarWidth();
      const height = this.getBarHeight();
      
      return {
        width: width,
        height: height,
        backgroundColor: color,
        borderRadius: '2px',
        animationName: 'scale',
        animationDuration: '1s',
        animationIterationCount: 'infinite',
        animationFillMode: 'both'
      };
    }
  },
  
  methods: {
    addSizeClasses(classes) {
      switch (this.size) {
        case 'xs':
          classes.push('w-4', 'h-4');
          break;
        case 'sm':
          classes.push('w-5', 'h-5');
          break;
        case 'lg':
          classes.push('w-8', 'h-8');
          break;
        case 'xl':
          classes.push('w-12', 'h-12');
          break;
        default:
          classes.push('w-6', 'h-6');
      }
    },
    
    addVariantClasses(classes) {
      switch (this.variant) {
        case 'primary':
          classes.push('text-blue-600');
          break;
        case 'secondary':
          classes.push('text-gray-600');
          break;
        case 'success':
          classes.push('text-green-600');
          break;
        case 'danger':
          classes.push('text-red-600');
          break;
        case 'warning':
          classes.push('text-yellow-600');
          break;
        case 'info':
          classes.push('text-sky-600');
          break;
        case 'white':
          classes.push('text-white');
          break;
      }
    },
    
    addRingVariantClasses(classes) {
      switch (this.variant) {
        case 'primary':
          classes.push('border-blue-200', 'border-t-blue-600');
          break;
        case 'secondary':
          classes.push('border-gray-200', 'border-t-gray-600');
          break;
        case 'success':
          classes.push('border-green-200', 'border-t-green-600');
          break;
        case 'danger':
          classes.push('border-red-200', 'border-t-red-600');
          break;
        case 'warning':
          classes.push('border-yellow-200', 'border-t-yellow-600');
          break;
        case 'info':
          classes.push('border-sky-200', 'border-t-sky-600');
          break;
        case 'white':
          classes.push('border-white', 'border-opacity-30', 'border-t-white');
          break;
      }
    },
    
    addTextVariantClasses(classes) {
      switch (this.variant) {
        case 'primary':
          classes.push('text-blue-600');
          break;
        case 'secondary':
          classes.push('text-gray-600');
          break;
        case 'success':
          classes.push('text-green-600');
          break;
        case 'danger':
          classes.push('text-red-600');
          break;
        case 'warning':
          classes.push('text-yellow-600');
          break;
        case 'info':
          classes.push('text-sky-600');
          break;
        case 'white':
          classes.push('text-white');
          break;
        default:
          classes.push('text-gray-500');
      }
    },
    
    getColor() {
      const colorMap = {
        primary: '#2563eb',
        secondary: '#4b5563',
        success: '#059669',
        danger: '#dc2626',
        warning: '#d97706',
        info: '#0284c7',
        white: '#ffffff'
      };
      
      return colorMap[this.variant] || colorMap.primary;
    },
    
    getDotSize() {
      const sizeMap = {
        xs: '6px',
        sm: '8px',
        default: '10px',
        lg: '12px',
        xl: '16px'
      };
      
      return sizeMap[this.size] || sizeMap.default;
    },
    
    getBarWidth() {
      const sizeMap = {
        xs: '2px',
        sm: '3px',
        default: '4px',
        lg: '5px',
        xl: '6px'
      };
      
      return sizeMap[this.size] || sizeMap.default;
    },
    
    getBarHeight() {
      const sizeMap = {
        xs: '16px',
        sm: '20px',
        default: '24px',
        lg: '32px',
        xl: '40px'
      };
      
      return sizeMap[this.size] || sizeMap.default;
    }
  }
};
</script>

<style scoped>
/* 도트 애니메이션 */
.spinner-dots .dot:nth-child(1) {
  animation-delay: -0.32s;
}

.spinner-dots .dot:nth-child(2) {
  animation-delay: -0.16s;
}

.spinner-dots .dot:nth-child(3) {
  animation-delay: 0s;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 바 애니메이션 */
.spinner-bars .bar:nth-child(1) {
  animation-delay: -0.4s;
}

.spinner-bars .bar:nth-child(2) {
  animation-delay: -0.3s;
}

.spinner-bars .bar:nth-child(3) {
  animation-delay: -0.2s;
}

.spinner-bars .bar:nth-child(4) {
  animation-delay: -0.1s;
}

@keyframes scale {
  0%, 40%, 100% {
    transform: scaleY(0.4);
  }
  20% {
    transform: scaleY(1);
  }
}

/* 펄스 애니메이션 커스터마이징 */
.spinner-pulse {
  animation-duration: 2s;
}

/* 스핀 애니메이션 속도 조정 */
.spinner-spin .animate-spin {
  animation-duration: 1s;
}

/* 링 애니메이션 */
.spinner-ring {
  animation-duration: 1s;
}

/* 반응형 조정 */
@media (max-width: 640px) {
  .base-spinner.fixed {
    padding: 1rem;
  }
  
  .spinner-text {
    font-size: 0.875rem;
  }
}
</style>