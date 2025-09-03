<!--
  BaseSelect 컴포넌트
  AIDEV-NOTE: 드롭다운 선택 박스, 검색 및 다중 선택 지원
  @example
  <BaseSelect 
    v-model="selected" 
    :options="options" 
    label="카테고리 선택"
    :searchable="true"
  />
-->
<template>
  <div :class="containerClasses">
    <!-- 라벨 -->
    <label
      v-if="label || $slots.label"
      :for="selectId"
      :class="labelClasses"
    >
      <slot name="label">
        {{ label }}
        <span v-if="required" class="text-red-500 ml-1" aria-label="필수">*</span>
      </slot>
    </label>

    <!-- 선택 박스 컨테이너 -->
    <div :class="selectContainerClasses" ref="containerRef">
      <!-- 메인 선택 버튼 -->
      <button
        :id="selectId"
        ref="buttonRef"
        type="button"
        :class="buttonClasses"
        :disabled="disabled"
        :aria-expanded="isOpen"
        :aria-haspopup="true"
        :aria-labelledby="selectId + '-label'"
        @click="toggleDropdown"
        @keydown="handleButtonKeydown"
        @blur="handleButtonBlur"
      >
        <!-- 선택된 값 표시 -->
        <span :class="valueClasses">
          <slot name="selected" :selected="selectedOptions" :placeholder="placeholder">
            <span v-if="hasSelection" class="flex items-center">
              <!-- 다중 선택 태그들 -->
              <div v-if="multiple && selectedOptions.length > 0" class="flex flex-wrap gap-1">
                <span
                  v-for="option in selectedOptions.slice(0, maxTags)"
                  :key="getOptionValue(option)"
                  class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800"
                >
                  {{ getOptionLabel(option) }}
                  <button
                    type="button"
                    class="ml-1 h-3 w-3 text-blue-400 hover:text-blue-600"
                    @click.stop="removeOption(option)"
                  >
                    ×
                  </button>
                </span>
                <span v-if="selectedOptions.length > maxTags" class="text-sm text-gray-500">
                  +{{ selectedOptions.length - maxTags }}
                </span>
              </div>
              
              <!-- 단일 선택 -->
              <span v-else-if="!multiple && selectedOptions.length > 0">
                {{ getOptionLabel(selectedOptions[0]) }}
              </span>
            </span>
            
            <!-- 플레이스홀더 -->
            <span v-else class="text-gray-400">{{ placeholder }}</span>
          </slot>
        </span>

        <!-- 드롭다운 아이콘 -->
        <span class="dropdown-icon" :class="iconClasses">
          <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
          </svg>
        </span>
      </button>

      <!-- 드롭다운 목록 -->
      <transition
        enter-active-class="transition duration-200 ease-out"
        enter-from-class="opacity-0 scale-95"
        enter-to-class="opacity-100 scale-100"
        leave-active-class="transition duration-150 ease-in"
        leave-from-class="opacity-100 scale-100"
        leave-to-class="opacity-0 scale-95"
      >
        <div
          v-if="isOpen"
          :class="dropdownClasses"
          @click.stop
        >
          <!-- 검색 입력 -->
          <div v-if="searchable" class="search-container p-2 border-b">
            <input
              ref="searchRef"
              type="text"
              :class="searchInputClasses"
              :placeholder="searchPlaceholder"
              v-model="searchQuery"
              @keydown="handleSearchKeydown"
            />
          </div>

          <!-- 옵션 목록 -->
          <div :class="optionsClasses">
            <!-- 로딩 상태 -->
            <div v-if="loading" class="p-3 text-center text-gray-500">
              <div class="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-600 mx-auto mb-2"></div>
              {{ loadingText }}
            </div>
            
            <!-- 검색 결과 없음 -->
            <div v-else-if="filteredOptions.length === 0" class="p-3 text-center text-gray-500">
              {{ emptyText }}
            </div>
            
            <!-- 옵션 항목들 -->
            <div
              v-else
              v-for="(option, index) in filteredOptions"
              :key="getOptionValue(option)"
              :class="getOptionClasses(option, index)"
              @click="selectOption(option)"
              @mouseenter="highlightedIndex = index"
              :aria-selected="isSelected(option)"
              role="option"
            >
              <slot name="option" :option="option" :selected="isSelected(option)">
                <!-- 체크박스 (다중 선택) -->
                <div v-if="multiple" class="flex items-center">
                  <input
                    type="checkbox"
                    :checked="isSelected(option)"
                    class="h-4 w-4 text-blue-600 rounded border-gray-300 focus:ring-blue-500"
                    @click.stop
                  />
                  <span class="ml-2">{{ getOptionLabel(option) }}</span>
                </div>
                
                <!-- 단일 선택 -->
                <div v-else class="flex items-center justify-between">
                  <span>{{ getOptionLabel(option) }}</span>
                  <svg
                    v-if="isSelected(option)"
                    class="h-4 w-4 text-blue-600"
                    fill="currentColor"
                    viewBox="0 0 20 20"
                  >
                    <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
                  </svg>
                </div>
              </slot>
            </div>
          </div>
        </div>
      </transition>
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
  name: 'BaseSelect',
  props: {
    // v-model
    modelValue: {
      type: [String, Number, Array, Object],
      default: null
    },

    // 기본 속성
    options: {
      type: Array,
      default: () => []
    },
    label: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: '선택하세요'
    },

    // 옵션 설정
    valueKey: {
      type: String,
      default: 'value'
    },
    labelKey: {
      type: String,
      default: 'label'
    },

    // 기능
    multiple: {
      type: Boolean,
      default: false
    },
    searchable: {
      type: Boolean,
      default: false
    },
    clearable: {
      type: Boolean,
      default: false
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
    loading: {
      type: Boolean,
      default: false
    },

    // 크기
    size: {
      type: String,
      default: 'default',
      validator: (value) => ['sm', 'default', 'lg'].includes(value)
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

    // 다중 선택 옵션
    maxTags: {
      type: Number,
      default: 3
    },

    // 텍스트
    searchPlaceholder: {
      type: String,
      default: '검색...'
    },
    loadingText: {
      type: String,
      default: '로딩 중...'
    },
    emptyText: {
      type: String,
      default: '결과가 없습니다'
    }
  },

  emits: ['update:modelValue', 'change', 'search', 'open', 'close'],

  data() {
    return {
      isOpen: false,
      searchQuery: '',
      highlightedIndex: -1,
      selectId: `select-${Math.random().toString(36).substr(2, 9)}`
    };
  },

  computed: {
    selectedOptions() {
      if (!this.modelValue) return [];
      
      if (this.multiple) {
        return Array.isArray(this.modelValue) ? this.modelValue : [];
      } else {
        return [this.modelValue];
      }
    },

    hasSelection() {
      return this.selectedOptions.length > 0;
    },

    filteredOptions() {
      if (!this.searchable || !this.searchQuery) {
        return this.options;
      }

      const query = this.searchQuery.toLowerCase();
      return this.options.filter(option => 
        this.getOptionLabel(option).toLowerCase().includes(query)
      );
    },

    containerClasses() {
      return 'base-select-container';
    },

    labelClasses() {
      const classes = ['select-label', 'block', 'text-sm', 'font-medium', 'mb-1'];

      if (this.error) {
        classes.push('text-red-700');
      } else if (this.disabled) {
        classes.push('text-gray-400');
      } else {
        classes.push('text-gray-700');
      }

      return classes.join(' ');
    },

    selectContainerClasses() {
      return 'relative';
    },

    buttonClasses() {
      const classes = [
        'select-button',
        'relative', 'w-full', 'pl-3', 'pr-10', 'text-left',
        'bg-white', 'border', 'rounded-md', 'shadow-sm',
        'focus:outline-none', 'focus:ring-1',
        'transition-colors', 'duration-200'
      ];

      // 크기
      switch (this.size) {
        case 'sm':
          classes.push('py-1.5', 'text-sm');
          break;
        case 'lg':
          classes.push('py-3', 'text-base');
          break;
        default:
          classes.push('py-2', 'text-sm');
      }

      // 상태
      if (this.error) {
        classes.push('border-red-300', 'focus:border-red-500', 'focus:ring-red-500');
      } else if (this.disabled) {
        classes.push('bg-gray-50', 'text-gray-400', 'cursor-not-allowed', 'border-gray-200');
      } else {
        classes.push('border-gray-300', 'focus:border-blue-500', 'focus:ring-blue-500', 'cursor-pointer');
      }

      return classes.join(' ');
    },

    valueClasses() {
      const classes = ['block', 'truncate', 'flex-1'];
      
      if (!this.hasSelection) {
        classes.push('text-gray-400');
      }
      
      return classes.join(' ');
    },

    iconClasses() {
      const classes = ['absolute', 'inset-y-0', 'right-0', 'flex', 'items-center', 'pr-2', 'pointer-events-none'];
      
      if (this.disabled) {
        classes.push('text-gray-400');
      } else {
        classes.push('text-gray-500');
      }
      
      return classes.join(' ');
    },

    dropdownClasses() {
      const classes = [
        'absolute', 'z-50', 'mt-1', 'w-full',
        'bg-white', 'shadow-lg', 'border', 'border-gray-200',
        'rounded-md', 'py-1', 'max-h-60', 'overflow-auto'
      ];

      return classes.join(' ');
    },

    searchInputClasses() {
      const classes = [
        'w-full', 'px-3', 'py-1.5', 'text-sm',
        'border', 'border-gray-300', 'rounded-md',
        'focus:outline-none', 'focus:ring-1', 'focus:ring-blue-500', 'focus:border-blue-500'
      ];

      return classes.join(' ');
    },

    optionsClasses() {
      return 'max-h-48 overflow-auto';
    },

    helpClasses() {
      return 'mt-1';
    }
  },

  watch: {
    isOpen(newVal) {
      if (newVal) {
        this.$emit('open');
        this.$nextTick(() => {
          if (this.searchable && this.$refs.searchRef) {
            this.$refs.searchRef.focus();
          }
        });
      } else {
        this.$emit('close');
        this.searchQuery = '';
        this.highlightedIndex = -1;
      }
    },

    searchQuery(newVal) {
      this.$emit('search', newVal);
    }
  },

  mounted() {
    document.addEventListener('click', this.handleClickOutside);
  },

  beforeUnmount() {
    document.removeEventListener('click', this.handleClickOutside);
  },

  methods: {
    getOptionValue(option) {
      return typeof option === 'object' ? option[this.valueKey] : option;
    },

    getOptionLabel(option) {
      return typeof option === 'object' ? option[this.labelKey] : option;
    },

    getOptionClasses(option, index) {
      const classes = [
        'cursor-pointer', 'select-none', 'relative', 'py-2', 'px-3',
        'hover:bg-gray-100', 'focus:bg-gray-100',
        'transition-colors', 'duration-200'
      ];

      if (this.isSelected(option)) {
        classes.push('bg-blue-50', 'text-blue-900');
      }

      if (index === this.highlightedIndex) {
        classes.push('bg-gray-100');
      }

      return classes.join(' ');
    },

    isSelected(option) {
      const value = this.getOptionValue(option);
      
      if (this.multiple) {
        return this.selectedOptions.some(selected => 
          this.getOptionValue(selected) === value
        );
      } else {
        return this.selectedOptions.length > 0 && 
               this.getOptionValue(this.selectedOptions[0]) === value;
      }
    },

    selectOption(option) {
      if (this.multiple) {
        let newValue;
        if (this.isSelected(option)) {
          newValue = this.selectedOptions.filter(selected =>
            this.getOptionValue(selected) !== this.getOptionValue(option)
          );
        } else {
          newValue = [...this.selectedOptions, option];
        }
        this.$emit('update:modelValue', newValue);
        this.$emit('change', newValue);
      } else {
        this.$emit('update:modelValue', option);
        this.$emit('change', option);
        this.closeDropdown();
      }
    },

    removeOption(option) {
      if (this.multiple) {
        const newValue = this.selectedOptions.filter(selected =>
          this.getOptionValue(selected) !== this.getOptionValue(option)
        );
        this.$emit('update:modelValue', newValue);
        this.$emit('change', newValue);
      }
    },

    toggleDropdown() {
      if (this.disabled || this.readonly) return;
      
      this.isOpen = !this.isOpen;
    },

    openDropdown() {
      if (this.disabled || this.readonly) return;
      this.isOpen = true;
    },

    closeDropdown() {
      this.isOpen = false;
    },

    handleClickOutside(event) {
      if (!this.$refs.containerRef.contains(event.target)) {
        this.closeDropdown();
      }
    },

    handleButtonKeydown(event) {
      switch (event.key) {
        case 'Enter':
        case ' ':
          event.preventDefault();
          this.toggleDropdown();
          break;
        case 'ArrowDown':
          event.preventDefault();
          if (this.isOpen) {
            this.highlightNext();
          } else {
            this.openDropdown();
          }
          break;
        case 'ArrowUp':
          event.preventDefault();
          if (this.isOpen) {
            this.highlightPrevious();
          } else {
            this.openDropdown();
          }
          break;
        case 'Escape':
          this.closeDropdown();
          break;
      }
    },

    handleSearchKeydown(event) {
      switch (event.key) {
        case 'ArrowDown':
          event.preventDefault();
          this.highlightNext();
          break;
        case 'ArrowUp':
          event.preventDefault();
          this.highlightPrevious();
          break;
        case 'Enter':
          event.preventDefault();
          if (this.highlightedIndex >= 0) {
            this.selectOption(this.filteredOptions[this.highlightedIndex]);
          }
          break;
        case 'Escape':
          this.closeDropdown();
          this.$refs.buttonRef.focus();
          break;
      }
    },

    handleButtonBlur() {
      // 짧은 지연을 두어 클릭 이벤트가 처리되도록 함
      setTimeout(() => {
        if (!this.$refs.containerRef.contains(document.activeElement)) {
          this.closeDropdown();
        }
      }, 100);
    },

    highlightNext() {
      this.highlightedIndex = Math.min(
        this.highlightedIndex + 1,
        this.filteredOptions.length - 1
      );
    },

    highlightPrevious() {
      this.highlightedIndex = Math.max(this.highlightedIndex - 1, 0);
    }
  }
};
</script>

<style scoped>
/* 드롭다운 애니메이션 */
.dropdown-icon {
  transition: transform 0.2s ease;
}

.select-button[aria-expanded="true"] .dropdown-icon {
  transform: rotate(180deg);
}

/* 스크롤바 스타일링 */
.options-container::-webkit-scrollbar {
  width: 6px;
}

.options-container::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.options-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.options-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 포커스 스타일 */
.select-button:focus {
  box-shadow: 0 0 0 1px theme('colors.blue.500');
}

/* 태그 애니메이션 */
.tag {
  transition: all 0.2s ease;
}

.tag:hover {
  transform: translateY(-1px);
}

/* 반응형 */
@media (max-width: 640px) {
  .dropdown {
    @apply max-h-48;
  }
}
</style>