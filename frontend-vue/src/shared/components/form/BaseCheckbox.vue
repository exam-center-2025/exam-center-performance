<!--
  BaseCheckbox 컴포넌트
  AIDEV-NOTE: 체크박스 입력, 그룹 및 개별 선택 지원
  @example
  <BaseCheckbox v-model="checked" label="동의합니다" />
  <BaseCheckbox v-model="items" value="option1" label="옵션 1" />
-->
<template>
  <div :class="containerClasses">
    <div :class="checkboxContainerClasses">
      <!-- 체크박스 입력 -->
      <input
        :id="checkboxId"
        ref="inputRef"
        type="checkbox"
        :class="inputClasses"
        :value="value"
        :checked="isChecked"
        :disabled="disabled"
        :required="required"
        :aria-describedby="ariaDescribedby"
        :aria-invalid="hasError"
        @change="handleChange"
        @focus="handleFocus"
        @blur="handleBlur"
      />
      
      <!-- 커스텀 체크박스 (선택적) -->
      <div v-if="customStyle" :class="customCheckboxClasses" @click="toggle">
        <!-- 체크 아이콘 -->
        <svg
          v-if="isChecked"
          :class="checkIconClasses"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path 
            stroke-linecap="round" 
            stroke-linejoin="round" 
            stroke-width="2" 
            d="M5 13l4 4L19 7" 
          />
        </svg>
        
        <!-- 부분 체크 아이콘 (indeterminate) -->
        <svg
          v-else-if="indeterminate"
          :class="checkIconClasses"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path 
            stroke-linecap="round" 
            stroke-linejoin="round" 
            stroke-width="2" 
            d="M20 12H4" 
          />
        </svg>
      </div>
      
      <!-- 라벨 -->
      <label
        v-if="label || $slots.default"
        :for="checkboxId"
        :class="labelClasses"
      >
        <slot>{{ label }}</slot>
        <span v-if="required" class="text-red-500 ml-1" aria-label="필수">*</span>
      </label>
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
  name: 'BaseCheckbox',
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
    
    // 상태
    disabled: {
      type: Boolean,
      default: false
    },
    required: {
      type: Boolean,
      default: false
    },
    indeterminate: {
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
    
    // 스타일
    customStyle: {
      type: Boolean,
      default: false
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
    return {\n      checkboxId: `checkbox-${Math.random().toString(36).substr(2, 9)}`\n    };\n  },\n  \n  computed: {\n    isChecked() {\n      if (Array.isArray(this.modelValue)) {\n        return this.value !== null && this.modelValue.includes(this.value);\n      }\n      return Boolean(this.modelValue);\n    },\n    \n    hasError() {\n      return Boolean(this.error);\n    },\n    \n    containerClasses() {\n      return 'base-checkbox-container';\n    },\n    \n    checkboxContainerClasses() {\n      const classes = ['checkbox-container', 'flex', 'items-start'];\n      \n      if (this.disabled) {\n        classes.push('opacity-50', 'cursor-not-allowed');\n      }\n      \n      return classes.join(' ');\n    },\n    \n    inputClasses() {\n      if (this.customStyle) {\n        return 'sr-only';\n      }\n      \n      const classes = ['checkbox-input', 'rounded', 'border-gray-300', 'focus:ring-2', 'focus:ring-offset-2'];\n      \n      // 크기\n      switch (this.size) {\n        case 'sm':\n          classes.push('h-3', 'w-3');\n          break;\n        case 'lg':\n          classes.push('h-5', 'w-5');\n          break;\n        default:\n          classes.push('h-4', 'w-4');\n      }\n      \n      // 색상\n      switch (this.color) {\n        case 'primary':\n          classes.push('text-blue-600', 'focus:ring-blue-500');\n          break;\n        case 'secondary':\n          classes.push('text-gray-600', 'focus:ring-gray-500');\n          break;\n        case 'success':\n          classes.push('text-green-600', 'focus:ring-green-500');\n          break;\n        case 'danger':\n          classes.push('text-red-600', 'focus:ring-red-500');\n          break;\n        case 'warning':\n          classes.push('text-yellow-600', 'focus:ring-yellow-500');\n          break;\n      }\n      \n      if (this.disabled) {\n        classes.push('cursor-not-allowed');\n      }\n      \n      return classes.join(' ');\n    },\n    \n    customCheckboxClasses() {\n      const classes = [\n        'custom-checkbox',\n        'flex', 'items-center', 'justify-center',\n        'border-2', 'rounded', 'cursor-pointer',\n        'transition-all', 'duration-200'\n      ];\n      \n      // 크기\n      switch (this.size) {\n        case 'sm':\n          classes.push('h-4', 'w-4');\n          break;\n        case 'lg':\n          classes.push('h-6', 'w-6');\n          break;\n        default:\n          classes.push('h-5', 'w-5');\n      }\n      \n      // 상태별 색상\n      if (this.isChecked || this.indeterminate) {\n        switch (this.color) {\n          case 'primary':\n            classes.push('bg-blue-600', 'border-blue-600', 'text-white');\n            break;\n          case 'secondary':\n            classes.push('bg-gray-600', 'border-gray-600', 'text-white');\n            break;\n          case 'success':\n            classes.push('bg-green-600', 'border-green-600', 'text-white');\n            break;\n          case 'danger':\n            classes.push('bg-red-600', 'border-red-600', 'text-white');\n            break;\n          case 'warning':\n            classes.push('bg-yellow-600', 'border-yellow-600', 'text-white');\n            break;\n        }\n      } else {\n        classes.push('bg-white', 'border-gray-300', 'hover:border-gray-400');\n      }\n      \n      if (this.disabled) {\n        classes.push('cursor-not-allowed', 'opacity-50');\n      }\n      \n      return classes.join(' ');\n    },\n    \n    checkIconClasses() {\n      const classes = [];\n      \n      switch (this.size) {\n        case 'sm':\n          classes.push('h-2.5', 'w-2.5');\n          break;\n        case 'lg':\n          classes.push('h-4', 'w-4');\n          break;\n        default:\n          classes.push('h-3', 'w-3');\n      }\n      \n      return classes.join(' ');\n    },\n    \n    labelClasses() {\n      const classes = ['checkbox-label', 'ml-2', 'block', 'cursor-pointer'];\n      \n      // 크기\n      switch (this.size) {\n        case 'sm':\n          classes.push('text-sm');\n          break;\n        case 'lg':\n          classes.push('text-base');\n          break;\n        default:\n          classes.push('text-sm');\n      }\n      \n      // 상태\n      if (this.hasError) {\n        classes.push('text-red-700');\n      } else if (this.disabled) {\n        classes.push('text-gray-400', 'cursor-not-allowed');\n      } else {\n        classes.push('text-gray-700');\n      }\n      \n      return classes.join(' ');\n    },\n    \n    helpClasses() {\n      const classes = ['mt-1', 'ml-6'];\n      \n      return classes.join(' ');\n    }\n  },\n  \n  watch: {\n    indeterminate(newVal) {\n      if (this.$refs.inputRef) {\n        this.$refs.inputRef.indeterminate = newVal;\n      }\n    }\n  },\n  \n  mounted() {\n    if (this.$refs.inputRef) {\n      this.$refs.inputRef.indeterminate = this.indeterminate;\n    }\n  },\n  \n  methods: {\n    handleChange(event) {\n      const checked = event.target.checked;\n      \n      if (Array.isArray(this.modelValue)) {\n        let newValue = [...this.modelValue];\n        \n        if (checked) {\n          if (!newValue.includes(this.value)) {\n            newValue.push(this.value);\n          }\n        } else {\n          newValue = newValue.filter(item => item !== this.value);\n        }\n        \n        this.$emit('update:modelValue', newValue);\n        this.$emit('change', newValue);\n      } else {\n        this.$emit('update:modelValue', checked);\n        this.$emit('change', checked);\n      }\n    },\n    \n    handleFocus(event) {\n      this.$emit('focus', event);\n    },\n    \n    handleBlur(event) {\n      this.$emit('blur', event);\n    },\n    \n    toggle() {\n      if (this.disabled) return;\n      \n      const newChecked = !this.isChecked;\n      \n      if (Array.isArray(this.modelValue)) {\n        let newValue = [...this.modelValue];\n        \n        if (newChecked) {\n          if (!newValue.includes(this.value)) {\n            newValue.push(this.value);\n          }\n        } else {\n          newValue = newValue.filter(item => item !== this.value);\n        }\n        \n        this.$emit('update:modelValue', newValue);\n        this.$emit('change', newValue);\n      } else {\n        this.$emit('update:modelValue', newChecked);\n        this.$emit('change', newChecked);\n      }\n    },\n    \n    focus() {\n      this.$refs.inputRef.focus();\n    },\n    \n    blur() {\n      this.$refs.inputRef.blur();\n    }\n  }\n};\n</script>\n\n<style scoped>\n/* 커스텀 체크박스 호버 효과 */\n.custom-checkbox:hover:not(.disabled) {\n  transform: scale(1.05);\n}\n\n/* 포커스 스타일 */\n.checkbox-input:focus {\n  outline: none;\n}\n\n.custom-checkbox:focus-within {\n  @apply ring-2 ring-offset-2;\n}\n\n.custom-checkbox:focus-within.text-blue-600 {\n  @apply ring-blue-500;\n}\n\n.custom-checkbox:focus-within.text-green-600 {\n  @apply ring-green-500;\n}\n\n.custom-checkbox:focus-within.text-red-600 {\n  @apply ring-red-500;\n}\n\n.custom-checkbox:focus-within.text-yellow-600 {\n  @apply ring-yellow-500;\n}\n\n.custom-checkbox:focus-within.text-gray-600 {\n  @apply ring-gray-500;\n}\n\n/* 라벨 클릭 애니메이션 */\n.checkbox-label {\n  transition: color 0.2s ease;\n}\n\n/* 체크박스 그룹 스타일링 */\n.checkbox-group .base-checkbox-container + .base-checkbox-container {\n  margin-top: 0.5rem;\n}\n\n/* 인라인 체크박스 */\n.checkbox-inline .checkbox-container {\n  display: inline-flex;\n  margin-right: 1rem;\n}\n\n/* 반응형 */\n@media (max-width: 640px) {\n  .checkbox-label {\n    font-size: 0.875rem;\n  }\n  \n  .help-text {\n    font-size: 0.75rem;\n  }\n}\n</style>