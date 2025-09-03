<!--
  BaseTooltip 컴포넌트
  AIDEV-NOTE: 호버/포커스 시 나타나는 툴팁, 접근성 고려
  @example
  <BaseTooltip content="도움말 텍스트" placement="top">
    <button>호버하세요</button>
  </BaseTooltip>
-->
<template>
  <div
    ref="triggerRef"
    class="tooltip-wrapper inline-block"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
    @focus="handleFocus"
    @blur="handleBlur"
  >
    <!-- 트리거 요소 -->
    <slot :show="show" :hide="hide" :toggle="toggle"></slot>
    
    <!-- 툴팁 포털 -->
    <Teleport to="body">
      <transition
        enter-active-class="transition-opacity duration-200"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-active-class="transition-opacity duration-150"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <div
          v-if="visible"
          ref="tooltipRef"
          :class="tooltipClasses"
          :style="tooltipStyle"
          role="tooltip"
          :aria-describedby="tooltipId"
          @mouseenter="handleTooltipMouseEnter"
          @mouseleave="handleTooltipMouseLeave"
        >
          <!-- 화살표 -->
          <div
            v-if="showArrow"
            :class="arrowClasses"
            :style="arrowStyle"
          ></div>
          
          <!-- 툴팁 내용 -->
          <div class="tooltip-content" :class="contentClasses">
            <slot name="content">
              {{ content }}
            </slot>
          </div>
        </div>
      </transition>
    </Teleport>
  </div>
</template>

<script>
export default {
  name: 'BaseTooltip',
  props: {
    // 내용
    content: {
      type: String,
      default: ''
    },
    
    // 위치
    placement: {
      type: String,
      default: 'top',
      validator: (value) => [
        'top', 'top-start', 'top-end',
        'bottom', 'bottom-start', 'bottom-end',
        'left', 'left-start', 'left-end',
        'right', 'right-start', 'right-end'
      ].includes(value)
    },
    
    // 트리거
    trigger: {
      type: String,
      default: 'hover',
      validator: (value) => ['hover', 'click', 'focus', 'manual'].includes(value)
    },
    
    // 지연
    delay: {
      type: Number,
      default: 0
    },
    hideDelay: {
      type: Number,
      default: 0
    },
    
    // 스타일
    variant: {
      type: String,
      default: 'dark',
      validator: (value) => ['dark', 'light', 'primary', 'danger', 'warning', 'success'].includes(value)
    },
    size: {
      type: String,
      default: 'default',
      validator: (value) => ['sm', 'default', 'lg'].includes(value)
    },
    
    // 옵션
    disabled: {
      type: Boolean,
      default: false
    },
    showArrow: {
      type: Boolean,
      default: true
    },
    offset: {
      type: Number,
      default: 8
    },
    maxWidth: {
      type: String,
      default: '320px'
    },
    
    // 수동 제어
    visible: {
      type: Boolean,
      default: false
    }
  },
  
  emits: ['show', 'hide', 'update:visible'],
  
  data() {
    return {
      isVisible: false,
      showTimer: null,
      hideTimer: null,
      tooltipId: `tooltip-${Math.random().toString(36).substr(2, 9)}`,
      position: {
        top: 0,
        left: 0
      },
      actualPlacement: this.placement
    };
  },
  
  computed: {
    isManual() {
      return this.trigger === 'manual';
    },
    
    shouldShow() {
      return this.isManual ? this.visible : this.isVisible;
    },
    
    tooltipClasses() {
      const classes = [
        'tooltip',
        'absolute',
        'z-50',
        'px-3',
        'py-2',
        'text-sm',
        'rounded-md',
        'shadow-lg',
        'pointer-events-none',
        'whitespace-nowrap'
      ];
      
      // 크기
      switch (this.size) {
        case 'sm':
          classes.push('px-2', 'py-1', 'text-xs');
          break;
        case 'lg':
          classes.push('px-4', 'py-3', 'text-base');
          break;
      }
      
      // 변형
      switch (this.variant) {
        case 'dark':
          classes.push('bg-gray-900', 'text-white');
          break;
        case 'light':
          classes.push('bg-white', 'text-gray-900', 'border', 'border-gray-200');
          break;
        case 'primary':
          classes.push('bg-blue-600', 'text-white');
          break;
        case 'danger':
          classes.push('bg-red-600', 'text-white');
          break;
        case 'warning':
          classes.push('bg-yellow-500', 'text-white');
          break;
        case 'success':
          classes.push('bg-green-600', 'text-white');
          break;
      }
      
      return classes.join(' ');
    },
    
    contentClasses() {
      const classes = [];
      
      if (this.maxWidth !== 'none') {
        classes.push('max-w-xs');
      }
      
      return classes.join(' ');
    },
    
    arrowClasses() {
      const classes = ['tooltip-arrow', 'absolute', 'w-2', 'h-2'];
      
      // 화살표 방향에 따른 클래스
      if (this.actualPlacement.includes('top')) {
        classes.push('bottom-[-4px]', 'border-t-4', 'border-x-4', 'border-x-transparent');
      } else if (this.actualPlacement.includes('bottom')) {
        classes.push('top-[-4px]', 'border-b-4', 'border-x-4', 'border-x-transparent');
      } else if (this.actualPlacement.includes('left')) {
        classes.push('right-[-4px]', 'border-l-4', 'border-y-4', 'border-y-transparent');
      } else if (this.actualPlacement.includes('right')) {
        classes.push('left-[-4px]', 'border-r-4', 'border-y-4', 'border-y-transparent');
      }
      
      // 변형에 따른 화살표 색상
      switch (this.variant) {
        case 'dark':
          classes.push('border-t-gray-900');
          break;
        case 'light':
          classes.push('border-t-white');
          break;
        case 'primary':
          classes.push('border-t-blue-600');
          break;
        case 'danger':
          classes.push('border-t-red-600');
          break;
        case 'warning':
          classes.push('border-t-yellow-500');
          break;
        case 'success':
          classes.push('border-t-green-600');
          break;
      }
      
      return classes.join(' ');
    },
    
    tooltipStyle() {
      return {
        top: `${this.position.top}px`,
        left: `${this.position.left}px`,
        maxWidth: this.maxWidth === 'none' ? undefined : this.maxWidth,
        zIndex: 9999
      };
    },
    
    arrowStyle() {
      const style = {};
      
      // 화살표 위치 조정
      if (this.actualPlacement.includes('-start')) {
        if (this.actualPlacement.includes('top') || this.actualPlacement.includes('bottom')) {
          style.left = '12px';
        } else {
          style.top = '12px';
        }
      } else if (this.actualPlacement.includes('-end')) {
        if (this.actualPlacement.includes('top') || this.actualPlacement.includes('bottom')) {
          style.right = '12px';
        } else {
          style.bottom = '12px';
        }
      } else {
        if (this.actualPlacement.includes('top') || this.actualPlacement.includes('bottom')) {
          style.left = '50%';
          style.transform = 'translateX(-50%)';
        } else {
          style.top = '50%';
          style.transform = 'translateY(-50%)';
        }
      }
      
      return style;
    }
  },
  
  watch: {
    visible(newVal) {
      if (this.isManual) {
        this.isVisible = newVal;
        if (newVal) {
          this.$nextTick(() => this.updatePosition());
        }
      }
    }
  },
  
  methods: {
    show() {
      if (this.disabled) return;
      
      this.clearHideTimer();
      
      if (this.delay > 0) {
        this.showTimer = setTimeout(() => {
          this.isVisible = true;
          this.$emit('show');
          this.$emit('update:visible', true);
          this.$nextTick(() => this.updatePosition());
        }, this.delay);
      } else {
        this.isVisible = true;
        this.$emit('show');
        this.$emit('update:visible', true);
        this.$nextTick(() => this.updatePosition());
      }
    },
    
    hide() {
      this.clearShowTimer();
      
      if (this.hideDelay > 0) {
        this.hideTimer = setTimeout(() => {
          this.isVisible = false;
          this.$emit('hide');
          this.$emit('update:visible', false);
        }, this.hideDelay);
      } else {
        this.isVisible = false;
        this.$emit('hide');
        this.$emit('update:visible', false);
      }
    },
    
    toggle() {
      if (this.shouldShow) {
        this.hide();
      } else {
        this.show();
      }
    },
    
    handleMouseEnter() {
      if (this.trigger === 'hover' && !this.isManual) {
        this.show();
      }
    },
    
    handleMouseLeave() {
      if (this.trigger === 'hover' && !this.isManual) {
        this.hide();
      }
    },
    
    handleFocus() {
      if (this.trigger === 'focus' && !this.isManual) {
        this.show();
      }
    },
    
    handleBlur() {
      if (this.trigger === 'focus' && !this.isManual) {
        this.hide();
      }
    },
    
    handleTooltipMouseEnter() {
      if (this.trigger === 'hover') {
        this.clearHideTimer();
      }
    },
    
    handleTooltipMouseLeave() {
      if (this.trigger === 'hover') {
        this.hide();
      }
    },
    
    clearShowTimer() {
      if (this.showTimer) {
        clearTimeout(this.showTimer);
        this.showTimer = null;
      }
    },
    
    clearHideTimer() {
      if (this.hideTimer) {
        clearTimeout(this.hideTimer);
        this.hideTimer = null;
      }
    },
    
    updatePosition() {
      if (!this.$refs.triggerRef || !this.$refs.tooltipRef) return;
      
      const trigger = this.$refs.triggerRef;
      const tooltip = this.$refs.tooltipRef;
      
      const triggerRect = trigger.getBoundingClientRect();
      const tooltipRect = tooltip.getBoundingClientRect();
      
      const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
      const scrollLeft = window.pageXOffset || document.documentElement.scrollLeft;
      
      let top = 0;
      let left = 0;
      
      // 기본 위치 계산
      switch (this.actualPlacement) {
        case 'top':
          top = triggerRect.top + scrollTop - tooltipRect.height - this.offset;
          left = triggerRect.left + scrollLeft + (triggerRect.width - tooltipRect.width) / 2;
          break;
        case 'top-start':
          top = triggerRect.top + scrollTop - tooltipRect.height - this.offset;
          left = triggerRect.left + scrollLeft;
          break;
        case 'top-end':
          top = triggerRect.top + scrollTop - tooltipRect.height - this.offset;
          left = triggerRect.right + scrollLeft - tooltipRect.width;
          break;
        case 'bottom':
          top = triggerRect.bottom + scrollTop + this.offset;
          left = triggerRect.left + scrollLeft + (triggerRect.width - tooltipRect.width) / 2;
          break;
        case 'bottom-start':
          top = triggerRect.bottom + scrollTop + this.offset;
          left = triggerRect.left + scrollLeft;
          break;
        case 'bottom-end':
          top = triggerRect.bottom + scrollTop + this.offset;
          left = triggerRect.right + scrollLeft - tooltipRect.width;
          break;
        case 'left':
          top = triggerRect.top + scrollTop + (triggerRect.height - tooltipRect.height) / 2;
          left = triggerRect.left + scrollLeft - tooltipRect.width - this.offset;
          break;
        case 'left-start':
          top = triggerRect.top + scrollTop;
          left = triggerRect.left + scrollLeft - tooltipRect.width - this.offset;
          break;
        case 'left-end':
          top = triggerRect.bottom + scrollTop - tooltipRect.height;
          left = triggerRect.left + scrollLeft - tooltipRect.width - this.offset;
          break;
        case 'right':
          top = triggerRect.top + scrollTop + (triggerRect.height - tooltipRect.height) / 2;
          left = triggerRect.right + scrollLeft + this.offset;
          break;
        case 'right-start':
          top = triggerRect.top + scrollTop;
          left = triggerRect.right + scrollLeft + this.offset;
          break;
        case 'right-end':
          top = triggerRect.bottom + scrollTop - tooltipRect.height;
          left = triggerRect.right + scrollLeft + this.offset;
          break;
      }
      
      // 뷰포트 경계 체크 및 조정
      const viewport = {
        width: window.innerWidth,
        height: window.innerHeight
      };
      
      if (left < 0) {
        left = 8;
      } else if (left + tooltipRect.width > viewport.width) {
        left = viewport.width - tooltipRect.width - 8;
      }
      
      if (top < 0) {
        top = 8;
      } else if (top + tooltipRect.height > viewport.height) {
        top = viewport.height - tooltipRect.height - 8;
      }
      
      this.position = { top, left };
    }
  },
  
  beforeUnmount() {
    this.clearShowTimer();
    this.clearHideTimer();
  }
};
</script>

<style scoped>
.tooltip {
  word-wrap: break-word;
}

.tooltip-arrow {
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.1));
}

/* 화살표 보더 스타일 */
.tooltip-arrow {
  border-style: solid;
}
</style>