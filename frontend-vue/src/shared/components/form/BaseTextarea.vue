<!--
  BaseTextarea 컴포넌트
  AIDEV-NOTE: 자동 크기 조정, 글자 수 제한이 있는 텍스트 영역
  @example
  <BaseTextarea 
    v-model="content" 
    label="설명" 
    :rows="4" 
    :maxlength="500"
    :autoResize="true"
  />
-->
<template>
  <div :class="containerClasses">
    <!-- 라벨 -->
    <label
      v-if="label || $slots.label"
      :for="textareaId"
      :class="labelClasses"
    >
      <slot name="label">
        {{ label }}
        <span v-if="required" class="text-red-500 ml-1" aria-label="필수">*</span>
      </slot>
    </label>

    <!-- 텍스트 영역 컨테이너 -->
    <div :class="textareaContainerClasses">
      <!-- 텍스트 영역 -->
      <textarea
        :id="textareaId"
        ref="textareaRef"
        :class="textareaClasses"
        :placeholder="placeholder"
        :disabled="disabled"
        :readonly="readonly"
        :required="required"
        :rows="computedRows"
        :cols="cols"
        :maxlength="maxlength"
        :aria-describedby="ariaDescribedby"
        :aria-invalid="hasError"
        :value="modelValue"
        @input="handleInput"
        @blur="handleBlur"
        @focus="handleFocus"
        @keydown="handleKeydown"
        @keyup="handleKeyup"
      ></textarea>

      <!-- 리사이즈 핸들 (커스텀) -->
      <div
        v-if="resizable && !autoResize"
        :class="resizeHandleClasses"
        @mousedown="handleResizeStart"
      >
        <svg class="h-3 w-3" fill="currentColor" viewBox="0 0 20 20">
          <path d="M10 6L6 10l4 4 1.41-1.41L9.83 11H16V9H9.83l1.58-1.59L10 6z"/>
        </svg>
      </div>
    </div>

    <!-- 도움말 텍스트 / 에러 메시지 -->
    <div v-if="helpText || error || $slots.help" :class="helpClasses">
      <slot name="help">
        <p v-if="error" class="text-red-600 text-sm">{{ error }}</p>
        <p v-else-if="helpText" class="text-gray-500 text-sm">{{ helpText }}</p>
      </slot>
    </div>

    <!-- 글자 수 카운터 -->
    <div
      v-if="showCharCount && (maxlength || showWordCount)"
      :class="charCountClasses"
    >
      <span v-if="showWordCount" class="mr-3">
        단어: {{ wordCount }}
      </span>
      <span v-if="maxlength">
        {{ charCount }}/{{ maxlength }}
      </span>
      <span v-else>
        글자: {{ charCount }}
      </span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BaseTextarea',
  props: {
    // v-model
    modelValue: {
      type: String,
      default: ''
    },

    // 기본 속성
    label: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: ''
    },

    // 크기
    rows: {
      type: Number,
      default: 3
    },
    cols: {
      type: Number,
      default: null
    },
    minRows: {
      type: Number,
      default: 2
    },
    maxRows: {
      type: Number,
      default: 10
    },

    // 상태
    disabled: {
      type: Boolean,
      default: false
    },
    readonly: {
      type: Boolean,
      default: false
    },
    required: {
      type: Boolean,
      default: false
    },

    // 크기 및 스타일
    size: {
      type: String,
      default: 'default',
      validator: (value) => ['sm', 'default', 'lg'].includes(value)
    },
    variant: {
      type: String,
      default: 'default',
      validator: (value) => ['default', 'filled', 'outlined', 'borderless'].includes(value)
    },

    // 기능
    autoResize: {
      type: Boolean,
      default: false
    },
    resizable: {
      type: Boolean,
      default: true
    },
    showCharCount: {
      type: Boolean,
      default: false
    },
    showWordCount: {
      type: Boolean,
      default: false
    },

    // HTML 속성
    maxlength: {
      type: Number,
      default: null
    },

    // 검증
    error: {
      type: String,
      default: ''
    },
    helpText: {
      type: String,
      default: ''
    },

    // 접근성
    ariaLabel: {
      type: String,
      default: null
    },
    ariaDescribedby: {
      type: String,
      default: null
    }
  },

  emits: [
    'update:modelValue',
    'input', 'blur', 'focus',
    'keydown', 'keyup', 'resize'
  ],

  data() {
    return {
      isFocused: false,
      currentRows: this.rows,
      isResizing: false,
      textareaId: `textarea-${Math.random().toString(36).substr(2, 9)}`
    };
  },

  computed: {
    hasError() {
      return Boolean(this.error);
    },

    charCount() {
      return String(this.modelValue || '').length;
    },

    wordCount() {
      const text = String(this.modelValue || '').trim();
      return text ? text.split(/\s+/).length : 0;
    },

    computedRows() {
      return this.autoResize ? this.currentRows : this.rows;
    },

    containerClasses() {
      return 'base-textarea-container';
    },

    labelClasses() {
      const classes = ['textarea-label', 'block', 'text-sm', 'font-medium', 'mb-1'];

      if (this.hasError) {
        classes.push('text-red-700');
      } else if (this.disabled) {
        classes.push('text-gray-400');
      } else {
        classes.push('text-gray-700');
      }

      return classes.join(' ');
    },

    textareaContainerClasses() {
      const classes = ['textarea-container', 'relative'];

      if (this.variant === 'filled') {
        classes.push('bg-gray-50');
      }

      return classes.join(' ');
    },

    textareaClasses() {
      const classes = ['textarea-field', 'block', 'w-full', 'transition-colors', 'duration-200'];

      // 크기
      switch (this.size) {
        case 'sm':
          classes.push('px-3', 'py-1.5', 'text-sm');
          break;
        case 'lg':
          classes.push('px-4', 'py-3', 'text-base');
          break;
        default:
          classes.push('px-3', 'py-2', 'text-sm');
      }

      // 변형
      switch (this.variant) {
        case 'filled':
          classes.push('bg-gray-50', 'border-0', 'focus:bg-white');
          break;
        case 'outlined':
          classes.push('bg-white', 'border-2');
          break;
        case 'borderless':
          classes.push('bg-transparent', 'border-0', 'focus:bg-gray-50');
          break;
        default:
          classes.push('bg-white', 'border');
      }

      // 상태별 스타일
      if (this.hasError) {
        classes.push('border-red-300', 'focus:border-red-500', 'focus:ring-red-500');
      } else if (this.disabled) {
        classes.push('bg-gray-50', 'text-gray-400', 'cursor-not-allowed', 'border-gray-200');
      } else {
        classes.push('border-gray-300', 'focus:border-blue-500', 'focus:ring-blue-500');
      }

      // 기본 스타일
      if (this.variant !== 'borderless') {
        classes.push('rounded-md');
      }

      if (!this.disabled && this.variant !== 'borderless') {
        classes.push('focus:outline-none', 'focus:ring-1');
      }

      // 리사이즈 설정
      if (!this.resizable || this.autoResize) {
        classes.push('resize-none');
      }

      return classes.join(' ');
    },

    resizeHandleClasses() {
      const classes = [
        'absolute', 'bottom-1', 'right-1',
        'w-4', 'h-4', 'text-gray-400',
        'cursor-se-resize', 'hover:text-gray-600',
        'transition-colors', 'duration-200'
      ];

      return classes.join(' ');
    },

    helpClasses() {
      return 'mt-1';
    },

    charCountClasses() {
      const classes = ['text-xs', 'text-right', 'mt-1'];

      if (this.maxlength && this.charCount > this.maxlength * 0.9) {
        classes.push('text-yellow-600');
      } else if (this.maxlength && this.charCount >= this.maxlength) {
        classes.push('text-red-600');
      } else {
        classes.push('text-gray-400');
      }

      return classes.join(' ');
    }
  },

  watch: {
    modelValue: {
      handler() {
        if (this.autoResize) {
          this.$nextTick(() => {
            this.adjustHeight();
          });
        }
      },
      immediate: true
    }
  },

  mounted() {
    if (this.autoResize) {
      this.adjustHeight();
    }
    
    // 리사이즈 이벤트 리스너
    document.addEventListener('mousemove', this.handleResizeMove);
    document.addEventListener('mouseup', this.handleResizeEnd);
  },

  beforeUnmount() {
    document.removeEventListener('mousemove', this.handleResizeMove);
    document.removeEventListener('mouseup', this.handleResizeEnd);
  },

  methods: {
    handleInput(event) {
      const value = event.target.value;
      this.$emit('update:modelValue', value);
      this.$emit('input', event);

      if (this.autoResize) {
        this.adjustHeight();
      }
    },

    handleBlur(event) {
      this.isFocused = false;
      this.$emit('blur', event);
    },

    handleFocus(event) {
      this.isFocused = true;
      this.$emit('focus', event);
    },

    handleKeydown(event) {
      this.$emit('keydown', event);
    },

    handleKeyup(event) {
      this.$emit('keyup', event);
    },

    adjustHeight() {
      if (!this.$refs.textareaRef || !this.autoResize) return;

      const textarea = this.$refs.textareaRef;
      
      // 임시로 높이를 auto로 설정하여 scrollHeight를 정확히 측정
      textarea.style.height = 'auto';
      
      const scrollHeight = textarea.scrollHeight;
      const lineHeight = parseInt(getComputedStyle(textarea).lineHeight);
      
      // 행 수 계산
      const rows = Math.max(
        this.minRows,
        Math.min(this.maxRows, Math.ceil(scrollHeight / lineHeight))
      );
      
      this.currentRows = rows;
      textarea.style.height = `${scrollHeight}px`;
    },

    handleResizeStart(event) {
      if (!this.resizable || this.autoResize) return;
      
      this.isResizing = true;
      this.initialY = event.clientY;
      this.initialHeight = this.$refs.textareaRef.offsetHeight;
      
      event.preventDefault();
    },

    handleResizeMove(event) {
      if (!this.isResizing) return;
      
      const deltaY = event.clientY - this.initialY;
      const newHeight = Math.max(60, this.initialHeight + deltaY);
      
      this.$refs.textareaRef.style.height = `${newHeight}px`;
      
      this.$emit('resize', { height: newHeight });
    },

    handleResizeEnd() {
      if (this.isResizing) {
        this.isResizing = false;
      }
    },

    focus() {
      this.$refs.textareaRef.focus();
    },

    blur() {
      this.$refs.textareaRef.blur();
    },

    select() {
      this.$refs.textareaRef.select();
    },

    setSelection(start, end) {
      this.$refs.textareaRef.setSelectionRange(start, end);
    }
  }
};
</script>

<style scoped>
.textarea-field::placeholder {
  @apply text-gray-400;
}

.textarea-field:disabled::placeholder {
  @apply text-gray-300;
}

/* 포커스 링 커스터마이징 */
.textarea-field:focus {
  box-shadow: 0 0 0 1px var(--ring-color, theme('colors.blue.500'));
}

/* 에러 상태일 때 포커스 링 */
.textarea-field.error:focus {
  --ring-color: theme('colors.red.500');
}

/* 리사이즈 핸들 */
.resize-handle {
  background: linear-gradient(-45deg, transparent 0%, transparent 40%, currentColor 40%, currentColor 60%, transparent 60%);
}

/* 자동 리사이즈 애니메이션 */
.textarea-field {
  transition: height 0.1s ease;
}

/* 스크롤바 스타일링 (WebKit 기반 브라우저) */
.textarea-field::-webkit-scrollbar {
  width: 8px;
}

.textarea-field::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.textarea-field::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.textarea-field::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 반응형 */
@media (max-width: 640px) {
  .textarea-field {
    font-size: 16px; /* iOS 줌 방지 */
  }
  
  .char-count {
    font-size: 0.75rem;
  }
}

/* 접근성 - 고대비 모드 */
@media (prefers-contrast: high) {
  .textarea-field {
    border-width: 2px;
  }
}

/* 접근성 - 모션 감소 */
@media (prefers-reduced-motion: reduce) {
  .textarea-field {
    transition: none;
  }
}
</style>