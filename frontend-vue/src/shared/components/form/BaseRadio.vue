<!--
  BaseRadio 컴포넌트
  AIDEV-NOTE: 라디오 버튼 입력, 그룹 선택 지원
  @example
  <BaseRadio v-model="selected" value="option1" label="옵션 1" name="group1" />
  <BaseRadio v-model="selected" value="option2" label="옵션 2" name="group1" />
-->
<template>
  <div :class="containerClasses">
    <div :class="radioContainerClasses">
      <!-- 라디오 입력 -->
      <input
        :id="radioId"
        ref="inputRef"
        type="radio"
        :class="inputClasses"
        :name="name"
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
      
      <!-- 커스텀 라디오 (선택적) -->
      <div v-if="customStyle" :class="customRadioClasses" @click="select">
        <!-- 선택 표시 -->
        <div
          v-if="isChecked"
          :class="dotClasses"
        ></div>
      </div>
      
      <!-- 라벨 -->
      <label
        v-if="label || $slots.default"
        :for="radioId"
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
  name: 'BaseRadio',
  props: {
    // v-model
    modelValue: {
      type: [String, Number, Boolean],
      default: null
    },
    
    // 값
    value: {
      type: [String, Number, Boolean],
      required: true
    },
    
    // 그룹 이름
    name: {
      type: String,
      required: true
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
    return {
      radioId: `radio-${Math.random().toString(36).substr(2, 9)}`
    };
  },
  
  computed: {
    isChecked() {
      return this.modelValue === this.value;
    },
    
    hasError() {
      return Boolean(this.error);
    },
    
    containerClasses() {
      return 'base-radio-container';
    },
    
    radioContainerClasses() {
      const classes = ['radio-container', 'flex', 'items-start'];
      
      if (this.disabled) {
        classes.push('opacity-50', 'cursor-not-allowed');
      }
      
      return classes.join(' ');
    },
    
    inputClasses() {
      if (this.customStyle) {
        return 'sr-only';
      }
      
      const classes = ['radio-input', 'rounded-full', 'border-gray-300', 'focus:ring-2', 'focus:ring-offset-2'];
      
      // 크기
      switch (this.size) {
        case 'sm':
          classes.push('h-3', 'w-3');
          break;
        case 'lg':
          classes.push('h-5', 'w-5');
          break;
        default:
          classes.push('h-4', 'w-4');
      }
      
      // 색상
      switch (this.color) {
        case 'primary':
          classes.push('text-blue-600', 'focus:ring-blue-500');
          break;
        case 'secondary':
          classes.push('text-gray-600', 'focus:ring-gray-500');
          break;
        case 'success':
          classes.push('text-green-600', 'focus:ring-green-500');
          break;
        case 'danger':
          classes.push('text-red-600', 'focus:ring-red-500');
          break;
        case 'warning':
          classes.push('text-yellow-600', 'focus:ring-yellow-500');
          break;
      }
      
      if (this.disabled) {
        classes.push('cursor-not-allowed');
      }
      
      return classes.join(' ');
    },
    
    customRadioClasses() {
      const classes = [
        'custom-radio',
        'flex', 'items-center', 'justify-center',
        'border-2', 'rounded-full', 'cursor-pointer',
        'transition-all', 'duration-200'
      ];
      
      // 크기
      switch (this.size) {
        case 'sm':
          classes.push('h-4', 'w-4');
          break;
        case 'lg':
          classes.push('h-6', 'w-6');
          break;
        default:
          classes.push('h-5', 'w-5');
      }
      
      // 상태별 색상
      if (this.isChecked) {
        switch (this.color) {
          case 'primary':
            classes.push('border-blue-600', 'bg-blue-50');
            break;
          case 'secondary':
            classes.push('border-gray-600', 'bg-gray-50');
            break;
          case 'success':
            classes.push('border-green-600', 'bg-green-50');
            break;
          case 'danger':
            classes.push('border-red-600', 'bg-red-50');
            break;
          case 'warning':
            classes.push('border-yellow-600', 'bg-yellow-50');
            break;
        }
      } else {
        classes.push('bg-white', 'border-gray-300', 'hover:border-gray-400');
      }
      
      if (this.disabled) {
        classes.push('cursor-not-allowed', 'opacity-50');
      }
      
      return classes.join(' ');
    },
    
    dotClasses() {
      const classes = ['rounded-full'];
      
      // 크기
      switch (this.size) {
        case 'sm':
          classes.push('h-1.5', 'w-1.5');
          break;
        case 'lg':
          classes.push('h-2.5', 'w-2.5');
          break;
        default:
          classes.push('h-2', 'w-2');
      }
      
      // 색상
      switch (this.color) {
        case 'primary':
          classes.push('bg-blue-600');
          break;
        case 'secondary':
          classes.push('bg-gray-600');
          break;
        case 'success':
          classes.push('bg-green-600');
          break;
        case 'danger':
          classes.push('bg-red-600');
          break;
        case 'warning':
          classes.push('bg-yellow-600');
          break;
      }
      
      return classes.join(' ');
    },
    
    labelClasses() {
      const classes = ['radio-label', 'ml-2', 'block', 'cursor-pointer'];
      
      // 크기
      switch (this.size) {
        case 'sm':
          classes.push('text-sm');
          break;
        case 'lg':
          classes.push('text-base');
          break;
        default:
          classes.push('text-sm');
      }
      
      // 상태
      if (this.hasError) {
        classes.push('text-red-700');
      } else if (this.disabled) {
        classes.push('text-gray-400', 'cursor-not-allowed');
      } else {
        classes.push('text-gray-900');
      }
      
      // 선택된 상태
      if (this.isChecked) {
        classes.push('font-medium');
      }
      
      return classes.join(' ');
    },
    
    descriptionClasses() {
      const classes = ['radio-description', 'ml-6', 'mt-1', 'text-xs', 'text-gray-500'];
      
      if (this.disabled) {
        classes.push('text-gray-400');
      }
      
      return classes.join(' ');
    },
    
    helpClasses() {
      const classes = ['mt-1', 'ml-6'];
      
      return classes.join(' ');
    }
  },
  
  methods: {
    handleChange(event) {\n      if (event.target.checked) {\n        this.$emit('update:modelValue', this.value);\n        this.$emit('change', this.value);\n      }\n    },\n    \n    handleFocus(event) {\n      this.$emit('focus', event);\n    },\n    \n    handleBlur(event) {\n      this.$emit('blur', event);\n    },\n    \n    select() {\n      if (this.disabled) return;\n      \n      this.$emit('update:modelValue', this.value);\n      this.$emit('change', this.value);\n    },\n    \n    focus() {\n      this.$refs.inputRef.focus();\n    },\n    \n    blur() {\n      this.$refs.inputRef.blur();\n    }\n  }\n};\n</script>\n\n<style scoped>\n/* 커스텀 라디오 호버 효과 */\n.custom-radio:hover:not(.disabled) {\n  transform: scale(1.05);\n}\n\n/* 포커스 스타일 */\n.radio-input:focus {\n  outline: none;\n}\n\n.custom-radio:focus-within {\n  @apply ring-2 ring-offset-2;\n}\n\n.custom-radio:focus-within.border-blue-600 {\n  @apply ring-blue-500;\n}\n\n.custom-radio:focus-within.border-green-600 {\n  @apply ring-green-500;\n}\n\n.custom-radio:focus-within.border-red-600 {\n  @apply ring-red-500;\n}\n\n.custom-radio:focus-within.border-yellow-600 {\n  @apply ring-yellow-500;\n}\n\n.custom-radio:focus-within.border-gray-600 {\n  @apply ring-gray-500;\n}\n\n/* 라벨 클릭 애니메이션 */\n.radio-label {\n  transition: all 0.2s ease;\n}\n\n/* 선택된 상태 애니메이션 */\n.custom-radio .rounded-full {\n  animation: radioSelect 0.2s ease;\n}\n\n@keyframes radioSelect {\n  0% {\n    transform: scale(0);\n  }\n  50% {\n    transform: scale(1.2);\n  }\n  100% {\n    transform: scale(1);\n  }\n}\n\n/* 라디오 그룹 스타일링 */\n.radio-group .base-radio-container + .base-radio-container {\n  margin-top: 0.75rem;\n}\n\n/* 인라인 라디오 */\n.radio-inline .radio-container {\n  display: inline-flex;\n  margin-right: 1.5rem;\n}\n\n/* 카드 스타일 라디오 */\n.radio-card {\n  @apply border border-gray-200 rounded-lg p-4 hover:border-gray-300 transition-colors;\n}\n\n.radio-card.checked {\n  @apply border-blue-500 bg-blue-50;\n}\n\n/* 반응형 */\n@media (max-width: 640px) {\n  .radio-label {\n    font-size: 0.875rem;\n  }\n  \n  .radio-description {\n    font-size: 0.75rem;\n  }\n  \n  .help-text {\n    font-size: 0.75rem;\n  }\n}\n\n/* 접근성 - 고대비 모드 */\n@media (prefers-contrast: high) {\n  .custom-radio {\n    border-width: 3px;\n  }\n  \n  .custom-radio .rounded-full {\n    border: 1px solid currentColor;\n  }\n}\n\n/* 접근성 - 모션 감소 */\n@media (prefers-reduced-motion: reduce) {\n  .custom-radio,\n  .radio-label,\n  .custom-radio .rounded-full {\n    transition: none;\n    animation: none;\n  }\n  \n  .custom-radio:hover {\n    transform: none;\n  }\n}\n</style>