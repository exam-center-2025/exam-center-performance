<!--
  BaseSwitch 컴포넌트
  AIDEV-NOTE: 토글 스위치, 애니메이션과 접근성 지원
  @example
  <BaseSwitch v-model="enabled" label="알림 활성화" />
  <BaseSwitch v-model="settings" value="darkMode" label="다크 모드" />
-->
<template>
  <div :class="containerClasses">
    <div :class="switchContainerClasses">
      <!-- 숨겨진 체크박스 (접근성) -->
      <input
        :id="switchId"
        ref="inputRef"
        type="checkbox"
        class="sr-only"
        :checked="isChecked"
        :disabled="disabled"
        :required="required"
        :aria-describedby="ariaDescribedby"
        :aria-invalid="hasError"
        @change="handleChange"
        @focus="handleFocus"
        @blur="handleBlur"
      />
      
      <!-- 스위치 버튼 -->
      <button
        type="button"
        :class="switchClasses"
        :aria-pressed="isChecked"
        :aria-labelledby="switchId + '-label'"
        :disabled="disabled"
        @click="toggle"
        @keydown.space.prevent="toggle"
        @keydown.enter.prevent="toggle"
      >
        <!-- 스위치 트랙 -->
        <span :class="trackClasses" aria-hidden="true">
          <!-- 스위치 핸들 -->
          <span :class="handleClasses">
            <!-- 아이콘 (선택적) -->
            <span v-if="showIcons" :class="iconContainerClasses">
              <slot name="checked-icon" v-if="isChecked">
                <svg class="h-3 w-3" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
                </svg>
              </slot>
              <slot name="unchecked-icon" v-else>
                <svg class="h-3 w-3" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
                </svg>
              </slot>
            </span>
          </span>
        </span>
      </button>
      
      <!-- 라벨 -->
      <label
        v-if="label || $slots.default"
        :id="switchId + '-label'"
        :for="switchId"
        :class="labelClasses"
      >
        <slot>{{ label }}</slot>
        <span v-if="required" class="text-red-500 ml-1" aria-label="필수">*</span>
      </label>
    </div>
    
    <!-- 설명 텍스트 -->
    <div v-if="description" :class="descriptionClasses">
      {{ description }}
    </div>
    
    <!-- 도움말 텍스트 / 에러 메시지 -->
    <div v-if="helpText || error || $slots.help" :class="helpClasses">
      <slot name="help">
        <p v-if="error" class="text-red-600 text-sm">{{ error }}</p>
        <p v-else-if="helpText" class="text-gray-500 text-sm">{{ helpText }}</p>
      </slot>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BaseSwitch',
  props: {
    // v-model
    modelValue: {
      type: [Boolean, Array],
      default: false
    },
    
    // 값 (배열 모드에서 사용)
    value: {
      type: [String, Number, Boolean],
      default: null
    },
    
    // 기본 속성
    label: {
      type: String,
      default: ''
    },
    description: {
      type: String,
      default: ''
    },
    
    // 상태
    disabled: {
      type: Boolean,
      default: false
    },
    required: {
      type: Boolean,
      default: false
    },
    
    // 크기
    size: {
      type: String,
      default: 'default',
      validator: (value) => ['sm', 'default', 'lg'].includes(value)
    },
    
    // 색상
    color: {
      type: String,
      default: 'primary',
      validator: (value) => ['primary', 'secondary', 'success', 'danger', 'warning'].includes(value)
    },
    
    // 스타일 옵션
    showIcons: {
      type: Boolean,
      default: false
    },
    labelPosition: {
      type: String,
      default: 'right',
      validator: (value) => ['left', 'right'].includes(value)
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
  
  emits: ['update:modelValue', 'change', 'focus', 'blur'],
  
  data() {
    return {
      isFocused: false,
      switchId: `switch-${Math.random().toString(36).substr(2, 9)}`
    };
  },
  
  computed: {
    isChecked() {
      if (Array.isArray(this.modelValue)) {
        return this.value !== null && this.modelValue.includes(this.value);
      }
      return Boolean(this.modelValue);
    },
    
    hasError() {
      return Boolean(this.error);
    },
    
    containerClasses() {
      return 'base-switch-container';
    },
    
    switchContainerClasses() {
      const classes = ['switch-container', 'flex', 'items-center'];
      
      if (this.labelPosition === 'left') {
        classes.push('flex-row-reverse');
      }
      
      if (this.disabled) {
        classes.push('opacity-50', 'cursor-not-allowed');
      }
      
      return classes.join(' ');
    },
    
    switchClasses() {
      const classes = [
        'switch-button',\n        'relative', 'inline-flex', 'flex-shrink-0',\n        'border-2', 'border-transparent', 'rounded-full',\n        'cursor-pointer', 'transition-colors', 'ease-in-out', 'duration-200',\n        'focus:outline-none', 'focus:ring-2', 'focus:ring-offset-2'\n      ];\n      \n      // 크기\n      switch (this.size) {\n        case 'sm':\n          classes.push('h-5', 'w-9');\n          break;\n        case 'lg':\n          classes.push('h-7', 'w-12');\n          break;\n        default:\n          classes.push('h-6', 'w-11');\n      }\n      \n      // 색상 및 상태\n      if (this.isChecked) {\n        switch (this.color) {\n          case 'primary':\n            classes.push('bg-blue-600', 'focus:ring-blue-500');\n            break;\n          case 'secondary':\n            classes.push('bg-gray-600', 'focus:ring-gray-500');\n            break;\n          case 'success':\n            classes.push('bg-green-600', 'focus:ring-green-500');\n            break;\n          case 'danger':\n            classes.push('bg-red-600', 'focus:ring-red-500');\n            break;\n          case 'warning':\n            classes.push('bg-yellow-600', 'focus:ring-yellow-500');\n            break;\n        }\n      } else {\n        classes.push('bg-gray-200', 'focus:ring-blue-500');\n      }\n      \n      if (this.disabled) {\n        classes.push('cursor-not-allowed', 'opacity-50');\n      }\n      \n      return classes.join(' ');\n    },\n    \n    trackClasses() {\n      const classes = ['switch-track', 'pointer-events-none', 'relative', 'inline-block', 'h-full', 'w-full', 'rounded-full', 'transition', 'ease-in-out', 'duration-200'];\n      \n      return classes.join(' ');\n    },\n    \n    handleClasses() {\n      const classes = [\n        'switch-handle',\n        'pointer-events-none', 'inline-block', 'rounded-full',\n        'bg-white', 'shadow', 'transform', 'ring-0',\n        'transition', 'ease-in-out', 'duration-200'\n      ];\n      \n      // 크기 및 위치\n      switch (this.size) {\n        case 'sm':\n          classes.push('h-4', 'w-4');\n          if (this.isChecked) {\n            classes.push('translate-x-4');\n          } else {\n            classes.push('translate-x-0');\n          }\n          break;\n        case 'lg':\n          classes.push('h-6', 'w-6');\n          if (this.isChecked) {\n            classes.push('translate-x-5');\n          } else {\n            classes.push('translate-x-0');\n          }\n          break;\n        default:\n          classes.push('h-5', 'w-5');\n          if (this.isChecked) {\n            classes.push('translate-x-5');\n          } else {\n            classes.push('translate-x-0');\n          }\n      }\n      \n      return classes.join(' ');\n    },\n    \n    iconContainerClasses() {\n      const classes = ['flex', 'items-center', 'justify-center', 'h-full', 'w-full'];\n      \n      if (this.isChecked) {\n        classes.push('text-green-400');\n      } else {\n        classes.push('text-gray-400');\n      }\n      \n      return classes.join(' ');\n    },\n    \n    labelClasses() {\n      const classes = ['switch-label', 'block', 'cursor-pointer'];\n      \n      if (this.labelPosition === 'left') {\n        classes.push('mr-3');\n      } else {\n        classes.push('ml-3');\n      }\n      \n      // 크기\n      switch (this.size) {\n        case 'sm':\n          classes.push('text-sm');\n          break;\n        case 'lg':\n          classes.push('text-base');\n          break;\n        default:\n          classes.push('text-sm');\n      }\n      \n      // 상태\n      if (this.hasError) {\n        classes.push('text-red-700');\n      } else if (this.disabled) {\n        classes.push('text-gray-400', 'cursor-not-allowed');\n      } else {\n        classes.push('text-gray-900');\n      }\n      \n      // 선택된 상태\n      if (this.isChecked) {\n        classes.push('font-medium');\n      }\n      \n      return classes.join(' ');\n    },\n    \n    descriptionClasses() {\n      const classes = ['switch-description', 'mt-1', 'text-xs', 'text-gray-500'];\n      \n      if (this.labelPosition === 'left') {\n        classes.push('mr-14');\n      } else {\n        classes.push('ml-14');\n      }\n      \n      if (this.disabled) {\n        classes.push('text-gray-400');\n      }\n      \n      return classes.join(' ');\n    },\n    \n    helpClasses() {\n      const classes = ['mt-1'];\n      \n      if (this.labelPosition === 'left') {\n        classes.push('mr-14');\n      } else {\n        classes.push('ml-14');\n      }\n      \n      return classes.join(' ');\n    }\n  },\n  \n  methods: {\n    handleChange(event) {\n      this.updateValue(event.target.checked);\n    },\n    \n    handleFocus(event) {\n      this.isFocused = true;\n      this.$emit('focus', event);\n    },\n    \n    handleBlur(event) {\n      this.isFocused = false;\n      this.$emit('blur', event);\n    },\n    \n    toggle() {\n      if (this.disabled) return;\n      \n      this.updateValue(!this.isChecked);\n    },\n    \n    updateValue(checked) {\n      if (Array.isArray(this.modelValue)) {\n        let newValue = [...this.modelValue];\n        \n        if (checked) {\n          if (!newValue.includes(this.value)) {\n            newValue.push(this.value);\n          }\n        } else {\n          newValue = newValue.filter(item => item !== this.value);\n        }\n        \n        this.$emit('update:modelValue', newValue);\n        this.$emit('change', newValue);\n      } else {\n        this.$emit('update:modelValue', checked);\n        this.$emit('change', checked);\n      }\n    },\n    \n    focus() {\n      this.$refs.inputRef.focus();\n    },\n    \n    blur() {\n      this.$refs.inputRef.blur();\n    }\n  }\n};\n</script>\n\n<style scoped>\n/* 스위치 애니메이션 */\n.switch-handle {\n  transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);\n}\n\n/* 포커스 스타일 */\n.switch-button:focus {\n  outline: none;\n}\n\n/* 호버 효과 */\n.switch-button:hover:not(:disabled) .switch-handle {\n  box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.1);\n}\n\n/* 활성 상태 */\n.switch-button:active:not(:disabled) .switch-handle {\n  transform: scale(0.95);\n}\n\n.switch-button[aria-pressed=\"true\"]:active:not(:disabled) .switch-handle {\n  transform: scale(0.95) translateX(1.25rem); /* default size */\n}\n\n/* 크기별 활성 상태 조정 */\n.h-5.w-9 .switch-button[aria-pressed=\"true\"]:active:not(:disabled) .switch-handle {\n  transform: scale(0.95) translateX(1rem); /* sm size */\n}\n\n.h-7.w-12 .switch-button[aria-pressed=\"true\"]:active:not(:disabled) .switch-handle {\n  transform: scale(0.95) translateX(1.5rem); /* lg size */\n}\n\n/* 라벨 클릭 애니메이션 */\n.switch-label {\n  transition: all 0.2s ease;\n}\n\n/* 스위치 그룹 */\n.switch-group .base-switch-container + .base-switch-container {\n  margin-top: 1rem;\n}\n\n/* 인라인 스위치 */\n.switch-inline .switch-container {\n  display: inline-flex;\n  margin-right: 2rem;\n}\n\n/* 반응형 */\n@media (max-width: 640px) {\n  .switch-label {\n    font-size: 0.875rem;\n  }\n  \n  .switch-description {\n    font-size: 0.75rem;\n  }\n  \n  .help-text {\n    font-size: 0.75rem;\n  }\n}\n\n/* 접근성 - 고대비 모드 */\n@media (prefers-contrast: high) {\n  .switch-button {\n    border-width: 2px;\n    border-color: currentColor;\n  }\n  \n  .switch-handle {\n    border: 1px solid currentColor;\n  }\n}\n\n/* 접근성 - 모션 감소 */\n@media (prefers-reduced-motion: reduce) {\n  .switch-button,\n  .switch-handle,\n  .switch-track,\n  .switch-label {\n    transition: none;\n  }\n  \n  .switch-button:active .switch-handle {\n    transform: none;\n  }\n}\n\n/* 다크 모드 지원 */\n@media (prefers-color-scheme: dark) {\n  .switch-button[aria-pressed=\"false\"] {\n    @apply bg-gray-600;\n  }\n  \n  .switch-handle {\n    @apply bg-gray-200;\n  }\n}\n</style>