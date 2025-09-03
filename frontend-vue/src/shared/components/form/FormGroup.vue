<!--
  FormGroup 컴포넌트
  AIDEV-NOTE: 폼 필드를 감싸는 컨테이너, 라벨/입력/에러/도움말 통합 관리
  @example
  <FormGroup 
    label="사용자명" 
    :required="true" 
    :error="errors.username"
    help-text="영문, 숫자 조합 4-20자"
  >
    <BaseInput v-model="username" placeholder="사용자명을 입력하세요" />
  </FormGroup>
-->
<template>
  <div :class="containerClasses">
    <!-- 라벨 -->
    <label
      v-if="label || $slots.label"
      :for="fieldId"
      :class="labelClasses"
    >
      <slot name="label">
        {{ label }}
        <span v-if="required" class="required-marker" :class="requiredClasses" aria-label="필수 입력">*</span>
        <span v-if="optional" class="optional-marker" :class="optionalClasses">(선택사항)</span>
      </slot>
    </label>

    <!-- 라벨 설명 -->
    <div v-if="labelDescription" :class="labelDescriptionClasses">
      {{ labelDescription }}
    </div>

    <!-- 입력 필드 영역 -->
    <div :class="fieldContainerClasses">
      <!-- 메인 입력 필드 -->
      <div :class="inputWrapperClasses">
        <slot 
          :field-id="fieldId" 
          :has-error="hasError" 
          :is-required="required"
          :aria-describedby="ariaDescribedby"
        ></slot>
      </div>

      <!-- 인라인 버튼/액션 -->
      <div v-if="$slots.actions" :class="actionsClasses">
        <slot name="actions"></slot>
      </div>
    </div>

    <!-- 검증 상태 아이콘 -->
    <div v-if="showValidationIcon && (hasError || isValid)" :class="validationIconClasses">
      <!-- 에러 아이콘 -->
      <svg 
        v-if="hasError" 
        class="h-5 w-5 text-red-500" 
        fill="none" 
        viewBox="0 0 24 24" 
        stroke="currentColor"
      >
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
      
      <!-- 성공 아이콘 -->
      <svg 
        v-else-if="isValid" 
        class="h-5 w-5 text-green-500" 
        fill="none" 
        viewBox="0 0 24 24" 
        stroke="currentColor"
      >
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    </div>

    <!-- 에러 메시지 -->
    <transition
      enter-active-class="transition-opacity duration-200"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-150"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div v-if="hasError" :class="errorClasses" :id="errorId">
        <svg class="h-4 w-4 text-red-500 mr-1 flex-shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <span class="flex-1">
          <slot name="error" :error="error">
            {{ error }}
          </slot>
        </span>
      </div>
    </transition>

    <!-- 성공 메시지 -->
    <transition
      enter-active-class="transition-opacity duration-200"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-150"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div v-if="successMessage && !hasError" :class="successClasses">
        <svg class="h-4 w-4 text-green-500 mr-1 flex-shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <span class="flex-1">{{ successMessage }}</span>
      </div>
    </transition>

    <!-- 도움말 텍스트 -->
    <div 
      v-if="helpText && !hasError" 
      :class="helpClasses" 
      :id="helpId"
    >
      <slot name="help" :help-text="helpText">
        {{ helpText }}
      </slot>
    </div>

    <!-- 추가 정보 -->
    <div v-if="$slots.info" :class="infoClasses">
      <slot name="info"></slot>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FormGroup',
  props: {
    // 기본 속성
    label: {
      type: String,
      default: ''
    },
    labelDescription: {
      type: String,
      default: ''
    },
    
    // 상태
    required: {
      type: Boolean,
      default: false
    },
    optional: {
      type: Boolean,
      default: false
    },
    disabled: {
      type: Boolean,
      default: false
    },
    
    // 검증
    error: {
      type: String,
      default: ''
    },
    successMessage: {
      type: String,
      default: ''
    },
    helpText: {
      type: String,
      default: ''
    },
    
    // 크기 및 레이아웃
    size: {
      type: String,
      default: 'default',
      validator: (value) => ['sm', 'default', 'lg'].includes(value)
    },
    layout: {
      type: String,
      default: 'vertical',
      validator: (value) => ['vertical', 'horizontal', 'inline'].includes(value)
    },
    
    // 시각적 옵션
    showValidationIcon: {
      type: Boolean,
      default: false
    },
    
    // ID 설정
    fieldId: {
      type: String,
      default: null
    },
    
    // 접근성
    ariaLabel: {
      type: String,
      default: null
    }
  },
  
  data() {
    return {
      generatedId: `field-${Math.random().toString(36).substr(2, 9)}`
    };
  },
  
  computed: {
    actualFieldId() {
      return this.fieldId || this.generatedId;
    },
    
    errorId() {
      return `${this.actualFieldId}-error`;
    },
    
    helpId() {
      return `${this.actualFieldId}-help`;
    },
    
    hasError() {
      return Boolean(this.error);
    },
    
    isValid() {
      return Boolean(this.successMessage) && !this.hasError;
    },
    
    ariaDescribedby() {\n      const ids = [];\n      \n      if (this.hasError) {\n        ids.push(this.errorId);\n      }\n      \n      if (this.helpText && !this.hasError) {\n        ids.push(this.helpId);\n      }\n      \n      return ids.length > 0 ? ids.join(' ') : null;\n    },\n    \n    containerClasses() {\n      const classes = ['form-group'];\n      \n      // 레이아웃\n      switch (this.layout) {\n        case 'horizontal':\n          classes.push('form-group-horizontal', 'sm:grid', 'sm:grid-cols-3', 'sm:gap-4', 'sm:items-start', 'sm:pt-5');\n          break;\n        case 'inline':\n          classes.push('form-group-inline', 'flex', 'flex-wrap', 'items-center', 'gap-4');\n          break;\n        default:\n          classes.push('form-group-vertical');\n      }\n      \n      // 크기\n      switch (this.size) {\n        case 'sm':\n          classes.push('space-y-1');\n          break;\n        case 'lg':\n          classes.push('space-y-3');\n          break;\n        default:\n          classes.push('space-y-2');\n      }\n      \n      // 상태\n      if (this.disabled) {\n        classes.push('form-group-disabled', 'opacity-50');\n      }\n      \n      if (this.hasError) {\n        classes.push('form-group-error');\n      } else if (this.isValid) {\n        classes.push('form-group-valid');\n      }\n      \n      return classes.join(' ');\n    },\n    \n    labelClasses() {\n      const classes = ['form-label', 'block', 'font-medium', 'text-gray-900'];\n      \n      // 레이아웃\n      if (this.layout === 'horizontal') {\n        classes.push('sm:mt-px', 'sm:pt-2');\n      } else if (this.layout === 'inline') {\n        classes.push('mb-0');\n      }\n      \n      // 크기\n      switch (this.size) {\n        case 'sm':\n          classes.push('text-sm');\n          break;\n        case 'lg':\n          classes.push('text-base');\n          break;\n        default:\n          classes.push('text-sm');\n      }\n      \n      // 상태\n      if (this.hasError) {\n        classes.push('text-red-900');\n      } else if (this.disabled) {\n        classes.push('text-gray-400');\n      }\n      \n      return classes.join(' ');\n    },\n    \n    requiredClasses() {\n      return 'text-red-500 ml-1';\n    },\n    \n    optionalClasses() {\n      return 'text-gray-400 ml-1 font-normal';\n    },\n    \n    labelDescriptionClasses() {\n      const classes = ['form-label-description', 'mt-1', 'text-xs', 'text-gray-500'];\n      \n      if (this.layout === 'horizontal') {\n        classes.push('sm:col-span-1');\n      }\n      \n      return classes.join(' ');\n    },\n    \n    fieldContainerClasses() {\n      const classes = ['form-field-container'];\n      \n      if (this.layout === 'horizontal') {\n        classes.push('sm:col-span-2');\n      }\n      \n      if (this.$slots.actions) {\n        classes.push('flex', 'space-x-3');\n      }\n      \n      return classes.join(' ');\n    },\n    \n    inputWrapperClasses() {\n      const classes = ['input-wrapper'];\n      \n      if (this.$slots.actions) {\n        classes.push('flex-1');\n      }\n      \n      return classes.join(' ');\n    },\n    \n    actionsClasses() {\n      const classes = ['form-actions', 'flex', 'items-start', 'space-x-2'];\n      \n      return classes.join(' ');\n    },\n    \n    validationIconClasses() {\n      const classes = ['validation-icon', 'absolute', 'top-0', 'right-0', 'mt-2', 'mr-2'];\n      \n      return classes.join(' ');\n    },\n    \n    errorClasses() {\n      const classes = ['form-error', 'flex', 'items-start', 'text-sm', 'text-red-600'];\n      \n      if (this.layout === 'horizontal') {\n        classes.push('sm:col-span-2', 'sm:col-start-2');\n      }\n      \n      return classes.join(' ');\n    },\n    \n    successClasses() {\n      const classes = ['form-success', 'flex', 'items-start', 'text-sm', 'text-green-600'];\n      \n      if (this.layout === 'horizontal') {\n        classes.push('sm:col-span-2', 'sm:col-start-2');\n      }\n      \n      return classes.join(' ');\n    },\n    \n    helpClasses() {\n      const classes = ['form-help', 'text-sm', 'text-gray-500'];\n      \n      if (this.layout === 'horizontal') {\n        classes.push('sm:col-span-2', 'sm:col-start-2');\n      }\n      \n      return classes.join(' ');\n    },\n    \n    infoClasses() {\n      const classes = ['form-info', 'text-xs', 'text-gray-400'];\n      \n      if (this.layout === 'horizontal') {\n        classes.push('sm:col-span-2', 'sm:col-start-2');\n      }\n      \n      return classes.join(' ');\n    }\n  },\n  \n  provide() {\n    return {\n      formGroup: {\n        fieldId: this.actualFieldId,\n        hasError: this.hasError,\n        isRequired: this.required,\n        isDisabled: this.disabled,\n        ariaDescribedby: this.ariaDescribedby\n      }\n    };\n  }\n};\n</script>\n\n<style scoped>\n/* 레이아웃별 스타일 */\n.form-group-horizontal {\n  @apply border-b border-gray-200 pb-5;\n}\n\n.form-group-horizontal:last-child {\n  @apply border-b-0 pb-0;\n}\n\n.form-group-inline .form-label {\n  @apply flex-shrink-0;\n}\n\n/* 상태별 스타일 */\n.form-group-error {\n  /* 에러 상태의 추가 스타일 */\n}\n\n.form-group-valid {\n  /* 성공 상태의 추가 스타일 */\n}\n\n.form-group-disabled {\n  @apply cursor-not-allowed;\n}\n\n/* 필수 마커 애니메이션 */\n.required-marker {\n  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;\n}\n\n@keyframes pulse {\n  0%, 100% {\n    opacity: 1;\n  }\n  50% {\n    opacity: .8;\n  }\n}\n\n/* 검증 아이콘 애니메이션 */\n.validation-icon svg {\n  animation: fadeInScale 0.3s ease-out;\n}\n\n@keyframes fadeInScale {\n  0% {\n    opacity: 0;\n    transform: scale(0.8);\n  }\n  100% {\n    opacity: 1;\n    transform: scale(1);\n  }\n}\n\n/* 에러/성공 메시지 스타일 */\n.form-error,\n.form-success {\n  line-height: 1.4;\n}\n\n/* 반응형 조정 */\n@media (max-width: 640px) {\n  .form-group-horizontal {\n    @apply block space-y-2;\n  }\n  \n  .form-group-horizontal .form-label {\n    @apply mt-0 pt-0;\n  }\n  \n  .form-group-horizontal .form-error,\n  .form-group-horizontal .form-success,\n  .form-group-horizontal .form-help {\n    @apply col-span-1 col-start-1;\n  }\n}\n\n/* 접근성 */\n@media (prefers-reduced-motion: reduce) {\n  .required-marker {\n    animation: none;\n  }\n  \n  .validation-icon svg {\n    animation: none;\n  }\n}</style>