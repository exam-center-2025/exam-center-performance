<!--
  Card 컴포넌트
  AIDEV-NOTE: 재사용 가능한 카드 UI, Tailwind 기반 스타일링
-->
<template>
  <div 
    :class="cardClasses"
    :style="cardStyles"
  >
    <!-- 헤더 슬롯 -->
    <div 
      v-if="$slots.header || title || subtitle" 
      class="card-header"
      :class="headerClasses"
    >
      <div v-if="title || subtitle" class="flex-1">
        <h3 v-if="title" class="card-title" :class="titleClasses">
          {{ title }}
        </h3>
        <p v-if="subtitle" class="text-sm text-gray-500 mt-1">
          {{ subtitle }}
        </p>
      </div>
      <div v-if="$slots.header" class="flex-shrink-0">
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
    <div 
      v-if="loading" 
      class="absolute inset-0 bg-white bg-opacity-75 flex items-center justify-center rounded-lg"
    >
      <div class="loading-spinner"></div>
      <span v-if="loadingText" class="ml-2 text-sm text-gray-600">
        {{ loadingText }}
      </span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Card',
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
      validator: (value) => ['default', 'outlined', 'elevated', 'flat'].includes(value)
    },
    
    // 크기
    size: {
      type: String,
      default: 'default',
      validator: (value) => ['compact', 'default', 'large'].includes(value)
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
    
    // 인터랙션
    clickable: {
      type: Boolean,
      default: false
    },
    disabled: {
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
    }
  },
  
  emits: ['click'],
  
  computed: {
    cardClasses() {
      const classes = ['card', 'relative'];
      
      // 변형에 따른 스타일
      switch (this.variant) {
        case 'outlined':
          classes.push('border-2', 'shadow-none');
          break;
        case 'elevated':
          classes.push('shadow-lg');
          break;
        case 'flat':
          classes.push('shadow-none', 'border-none');
          break;
        default:
          classes.push('shadow-sm', 'border');
      }
      
      // 크기에 따른 패딩
      switch (this.size) {
        case 'compact':
          classes.push('p-4');
          break;
        case 'large':
          classes.push('p-8');
          break;
        default:
          classes.push('p-6');
      }
      
      // 상태에 따른 스타일
      if (this.clickable && !this.disabled) {
        classes.push('cursor-pointer', 'hover:shadow-md', 'transition-shadow');
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
      
      return classes;
    },
    
    headerClasses() {
      const classes = [];
      
      switch (this.size) {
        case 'compact':
          classes.push('mb-2', 'pb-2');
          break;
        case 'large':
          classes.push('mb-6', 'pb-4');
          break;
        default:
          classes.push('mb-4', 'pb-2');
      }
      
      return classes;
    },
    
    titleClasses() {
      const classes = [];
      
      switch (this.size) {
        case 'compact':
          classes.push('text-base', 'font-medium');
          break;
        case 'large':
          classes.push('text-xl', 'font-bold');
          break;
        default:
          classes.push('text-lg', 'font-semibold');
      }
      
      return classes;
    },
    
    contentClasses() {
      return [];
    },
    
    footerClasses() {
      const classes = ['pt-4', 'mt-4', 'border-t', 'border-gray-200'];
      
      return classes;
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
    }
  }
};
</script>