<!--
  BaseInput 컴포넌트
  AIDEV-NOTE: 검증, 에러 상태, 도움말 텍스트를 지원하는 입력 필드
  @example
  <BaseInput 
    v-model="value" 
    label="이름" 
    placeholder="이름을 입력하세요"
    :required="true"
    :error="errorMessage"
  />
-->
<template>
  <div :class="containerClasses">
    <!-- 라벨 -->
    <label
      v-if="label || $slots.label"
      :for="inputId"
      :class="labelClasses"
    >
      <slot name="label">
        {{ label }}
        <span v-if="required" class="text-red-500 ml-1" aria-label="필수">*</span>
      </slot>
    </label>

    <!-- 입력 컨테이너 -->
    <div :class="inputContainerClasses">
      <!-- 앞쪽 아이콘/슬롯 -->
      <div
        v-if="$slots.prefix || prefixIcon"
        class="input-prefix"
        :class="prefixClasses"
      >
        <slot name="prefix">
          <component
            v-if="prefixIcon"
            :is="prefixIcon"
            class="h-4 w-4 text-gray-400"
          />
        </slot>
      </div>

      <!-- 입력 필드 -->
      <input
        :id="inputId"
        ref="inputRef"
        :type="inputType"
        :class="inputClasses"
        :placeholder="placeholder"
        :disabled="disabled"
        :readonly="readonly"
        :required="required"
        :min="min"
        :max="max"
        :step="step"
        :maxlength="maxlength"
        :pattern="pattern"
        :autocomplete="autocomplete"
        :aria-describedby="ariaDescribedby"
        :aria-invalid="hasError"
        :value="modelValue"
        @input="handleInput"
        @blur="handleBlur"
        @focus="handleFocus"
        @keydown="handleKeydown"
        @keyup="handleKeyup"
      />

      <!-- 뒤쪽 아이콘/슬롯 -->
      <div
        v-if="$slots.suffix || suffixIcon || showPasswordToggle || clearable"
        class="input-suffix"
        :class="suffixClasses"
      >
        <!-- 클리어 버튼 -->
        <button
          v-if="clearable && modelValue && !disabled && !readonly"
          type="button"
          class="clear-button"
          :class="clearButtonClasses"
          @click="handleClear"
          :aria-label="clearLabel"
        >
          <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>

        <!-- 비밀번호 토글 -->
        <button
          v-if="showPasswordToggle"
          type="button"
          class="password-toggle"
          :class="passwordToggleClasses"
          @click="togglePasswordVisibility"
          :aria-label="passwordToggleLabel"
        >
          <svg v-if="showPassword" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
          </svg>
          <svg v-else class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L12 12m-6.5-6.5L12 12" />
          </svg>
        </button>

        <!-- 커스텀 접미사 -->
        <slot name="suffix">
          <component
            v-if="suffixIcon"
            :is="suffixIcon"
            class="h-4 w-4 text-gray-400"
          />
        </slot>
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
      v-if="showCharCount && maxlength"
      class="char-count text-xs text-gray-400 text-right mt-1"
    >
      {{ charCount }}/{{ maxlength }}
    </div>
  </div>
</template>

<script>
export default {
  name: 'BaseInput',
  props: {
    // v-model
    modelValue: {
      type: [String, Number],
      default: ''
    },

    // 기본 속성
    type: {
      type: String,
      default: 'text',
      validator: (value) => [
        'text', 'password', 'email', 'number', 'tel', 'url', 'search'
      ].includes(value)
    },
    label: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: ''
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

    // 검증
    error: {
      type: String,
      default: ''
    },
    helpText: {
      type: String,
      default: ''
    },

    // HTML 속성
    min: {
      type: [Number, String],
      default: null
    },
    max: {
      type: [Number, String],
      default: null
    },
    step: {
      type: [Number, String],
      default: null
    },
    maxlength: {
      type: Number,
      default: null
    },
    pattern: {
      type: String,
      default: null
    },
    autocomplete: {
      type: String,
      default: null
    },

    // 기능
    clearable: {
      type: Boolean,
      default: false
    },
    showCharCount: {
      type: Boolean,
      default: false
    },

    // 아이콘
    prefixIcon: {
      type: [String, Object],
      default: null
    },
    suffixIcon: {
      type: [String, Object],
      default: null
    },

    // 접근성
    ariaLabel: {
      type: String,
      default: null
    },
    ariaDescribedby: {
      type: String,
      default: null
    },

    // 텍스트
    clearLabel: {
      type: String,
      default: '입력 내용 지우기'
    },
    passwordToggleLabel: {
      type: String,
      default: '비밀번호 보기/숨기기'
    }
  },

  emits: [
    'update:modelValue',
    'input', 'blur', 'focus', 'clear',
    'keydown', 'keyup'
  ],

  data() {
    return {
      isFocused: false,
      showPassword: false,
      inputId: `input-${Math.random().toString(36).substr(2, 9)}`
    };
  },

  computed: {
    inputType() {
      if (this.type === 'password') {
        return this.showPassword ? 'text' : 'password';
      }
      return this.type;
    },

    showPasswordToggle() {
      return this.type === 'password';
    },

    hasError() {
      return Boolean(this.error);
    },

    charCount() {
      return String(this.modelValue || '').length;
    },

    containerClasses() {
      return 'base-input-container';
    },

    labelClasses() {
      const classes = ['input-label', 'block', 'text-sm', 'font-medium', 'mb-1'];

      if (this.hasError) {
        classes.push('text-red-700');
      } else if (this.disabled) {
        classes.push('text-gray-400');
      } else {
        classes.push('text-gray-700');
      }

      return classes.join(' ');
    },

    inputContainerClasses() {
      const classes = ['input-container', 'relative'];

      if (this.variant === 'filled') {
        classes.push('bg-gray-50');
      }

      return classes.join(' ');
    },

    inputClasses() {
      const classes = ['input-field', 'block', 'w-full', 'transition-colors', 'duration-200'];

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

      // 아이콘이 있을 때 패딩 조정
      if (this.$slots.prefix || this.prefixIcon) {
        classes.push('pl-10');
      }

      if (this.$slots.suffix || this.suffixIcon || this.clearable || this.showPasswordToggle) {
        classes.push('pr-10');
      }

      return classes.join(' ');
    },

    prefixClasses() {
      const classes = [
        'absolute', 'inset-y-0', 'left-0', 'flex', 'items-center', 'pl-3',
        'pointer-events-none'
      ];

      return classes.join(' ');
    },

    suffixClasses() {
      const classes = [
        'absolute', 'inset-y-0', 'right-0', 'flex', 'items-center', 'pr-3'
      ];

      return classes.join(' ');
    },

    clearButtonClasses() {
      const classes = [
        'text-gray-400', 'hover:text-gray-600', 'focus:text-gray-600',
        'focus:outline-none', 'transition-colors', 'duration-200'
      ];

      return classes.join(' ');
    },

    passwordToggleClasses() {
      const classes = [
        'text-gray-400', 'hover:text-gray-600', 'focus:text-gray-600',
        'focus:outline-none', 'transition-colors', 'duration-200'
      ];

      return classes.join(' ');
    },

    helpClasses() {
      const classes = ['mt-1'];

      return classes.join(' ');
    }
  },

  methods: {
    handleInput(event) {
      const value = event.target.value;
      this.$emit('update:modelValue', this.type === 'number' ? Number(value) : value);
      this.$emit('input', event);
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

    handleClear() {
      this.$emit('update:modelValue', '');
      this.$emit('clear');
      this.$refs.inputRef.focus();
    },

    togglePasswordVisibility() {
      this.showPassword = !this.showPassword;
      this.$nextTick(() => {
        this.$refs.inputRef.focus();
      });
    },

    focus() {
      this.$refs.inputRef.focus();
    },

    blur() {
      this.$refs.inputRef.blur();
    },

    select() {
      this.$refs.inputRef.select();
    }
  }
};
</script>

<style scoped>
.input-field::placeholder {
  @apply text-gray-400;
}

.input-field:disabled::placeholder {
  @apply text-gray-300;
}

/* 숫자 입력 필드의 스피너 제거 */
.input-field[type="number"] {
  -moz-appearance: textfield;
}

.input-field[type="number"]::-webkit-outer-spin-button,
.input-field[type="number"]::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

/* 검색 입력 필드의 기본 스타일 제거 */
.input-field[type="search"]::-webkit-search-cancel-button {
  -webkit-appearance: none;
}

/* 포커스 링 커스터마이징 */
.input-field:focus {
  box-shadow: 0 0 0 1px var(--ring-color, theme('colors.blue.500'));
}

/* 에러 상태일 때 포커스 링 */
.input-field.error:focus {
  --ring-color: theme('colors.red.500');
}

/* 애니메이션 */
.clear-button,
.password-toggle {
  transition: all 0.2s ease;
}

.clear-button:hover,
.password-toggle:hover {
  transform: scale(1.1);
}

/* 반응형 */
@media (max-width: 640px) {
  .input-field {
    font-size: 16px; /* iOS 줌 방지 */
  }
}
</style>