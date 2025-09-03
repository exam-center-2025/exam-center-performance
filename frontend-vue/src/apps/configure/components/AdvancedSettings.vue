<template>
  <div class="bg-white border border-gray-200 rounded-lg p-6">
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-lg font-semibold text-gray-800 flex items-center gap-2">
        <i class="fas fa-cog"></i>
        고급 설정
      </h3>
      
      <!-- 접기/펼치기 버튼 -->
      <button
        @click="toggleExpanded"
        type="button"
        class="inline-flex items-center px-3 py-1 text-sm border border-gray-300 rounded hover:bg-gray-50 transition-colors"
      >
        <i 
          :class="isExpanded ? 'fas fa-chevron-up' : 'fas fa-chevron-down'"
          class="mr-2"
        ></i>
        {{ isExpanded ? '접기' : '펼치기' }}
      </button>
    </div>
    
    <!-- 고급 설정 내용 -->
    <div
      v-show="isExpanded"
      class="space-y-6"
      :class="{ 'animate-slide-down': isExpanded, 'animate-slide-up': !isExpanded }"
    >
      <!-- 템플릿 관리 -->
      <div class="border-b border-gray-200 pb-6">
        <h4 class="font-medium text-gray-900 mb-3 flex items-center gap-2">
          <i class="fas fa-save text-blue-500"></i>
          템플릿 관리
        </h4>
        
        <div class="flex flex-wrap gap-3">
          <!-- 템플릿 저장 -->
          <div class="flex gap-2">
            <input
              v-model="templateName"
              type="text"
              placeholder="템플릿 이름"
              class="flex-1 min-w-32 border border-gray-300 px-3 py-2 text-sm rounded focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
            <button
              @click="handleSaveTemplate"
              :disabled="!templateName.trim()"
              type="button"
              class="px-3 py-2 text-sm bg-blue-600 text-white rounded hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
            >
              저장
            </button>
          </div>
          
          <!-- 템플릿 로드 -->
          <div v-if="templates.length > 0" class="flex gap-2">
            <select
              v-model="selectedTemplate"
              class="border border-gray-300 px-3 py-2 text-sm rounded focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="">템플릿 선택</option>
              <option
                v-for="template in templates"
                :key="template.templateName"
                :value="template.templateName"
              >
                {{ template.templateName }}
              </option>
            </select>
            <button
              @click="handleLoadTemplate"
              :disabled="!selectedTemplate"
              type="button"
              class="px-3 py-2 text-sm bg-green-600 text-white rounded hover:bg-green-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
            >
              로드
            </button>
          </div>
        </div>
      </div>
      
      <!-- 추가 JSON 설정 -->
      <div>
        <h4 class="font-medium text-gray-900 mb-3 flex items-center gap-2">
          <i class="fas fa-code text-purple-500"></i>
          추가 설정 (JSON)
        </h4>
        
        <div class="space-y-3">
          <textarea
            v-model="testRequest.additionalConfig"
            rows="6"
            :class="[
              'w-full border px-3 py-2 text-sm font-mono rounded focus:ring-2 focus:ring-blue-500 focus:border-transparent',
              errors.additionalConfig ? 'border-red-300' : 'border-gray-300'
            ]"
            placeholder='{
  "timeout": 30000,
  "retries": 3,
  "headers": {
    "User-Agent": "LoadTest/1.0"
  },
  "connectionPool": {
    "maxConnections": 100,
    "keepAlive": true
  }
}'
          ></textarea>
          
          <div class="flex items-start gap-2">
            <i class="fas fa-info-circle text-blue-500 mt-0.5"></i>
            <div class="text-sm text-gray-600 flex-1">
              JSON 형태로 추가 설정을 입력하세요. 타임아웃, 재시도 횟수, 커스텀 헤더 등을 설정할 수 있습니다.
            </div>
          </div>
          
          <p v-if="errors.additionalConfig" class="text-sm text-red-600 flex items-center gap-1">
            <i class="fas fa-exclamation-circle"></i>
            {{ errors.additionalConfig }}
          </p>
          
          <!-- JSON 유효성 표시 -->
          <div v-if="jsonValidationStatus" class="flex items-center gap-2 text-sm">
            <i 
              :class="jsonValidationStatus.isValid 
                ? 'fas fa-check-circle text-green-500' 
                : 'fas fa-times-circle text-red-500'"
            ></i>
            <span 
              :class="jsonValidationStatus.isValid 
                ? 'text-green-600' 
                : 'text-red-600'"
            >
              {{ jsonValidationStatus.message }}
            </span>
          </div>
        </div>
      </div>
      
      <!-- 설정 미리 정의된 옵션들 -->
      <div>
        <h4 class="font-medium text-gray-900 mb-3 flex items-center gap-2">
          <i class="fas fa-sliders-h text-orange-500"></i>
          빠른 설정
        </h4>
        
        <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
          <button
            @click="applyQuickConfig('timeout')"
            type="button"
            class="p-3 text-sm border border-gray-300 rounded hover:bg-gray-50 text-left transition-colors"
          >
            <div class="font-medium">타임아웃 연장</div>
            <div class="text-xs text-gray-500 mt-1">60초로 설정</div>
          </button>
          
          <button
            @click="applyQuickConfig('retry')"
            type="button"
            class="p-3 text-sm border border-gray-300 rounded hover:bg-gray-50 text-left transition-colors"
          >
            <div class="font-medium">재시도 활성화</div>
            <div class="text-xs text-gray-500 mt-1">5회 재시도</div>
          </button>
          
          <button
            @click="applyQuickConfig('keepalive')"
            type="button"
            class="p-3 text-sm border border-gray-300 rounded hover:bg-gray-50 text-left transition-colors"
          >
            <div class="font-medium">연결 유지</div>
            <div class="text-xs text-gray-500 mt-1">Keep-Alive 활성화</div>
          </button>
          
          <button
            @click="applyQuickConfig('clear')"
            type="button"
            class="p-3 text-sm border border-red-300 text-red-700 rounded hover:bg-red-50 text-left transition-colors"
          >
            <div class="font-medium">설정 초기화</div>
            <div class="text-xs text-red-500 mt-1">모든 추가 설정 제거</div>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useTestConfig } from '@shared/composables/useTestConfig.js'

// AIDEV-NOTE: 고급 설정 컴포넌트 - 템플릿 관리, JSON 설정, 빠른 설정 옵션
const {
  testRequest,
  templates,
  errors,
  saveAsTemplate,
  loadTemplate,
  showMessage
} = useTestConfig()

// 접기/펼치기 상태
const isExpanded = ref(false)

// 템플릿 관련 상태
const templateName = ref('')
const selectedTemplate = ref('')

// 접기/펼치기 토글
const toggleExpanded = () => {
  isExpanded.value = !isExpanded.value
}

// JSON 유효성 검사 상태
const jsonValidationStatus = computed(() => {
  if (!testRequest.additionalConfig || !testRequest.additionalConfig.trim()) {
    return null
  }
  
  try {
    JSON.parse(testRequest.additionalConfig)
    return {
      isValid: true,
      message: '올바른 JSON 형식입니다'
    }
  } catch (error) {
    return {
      isValid: false,
      message: `JSON 오류: ${error.message}`
    }
  }
})

// 템플릿 저장 핸들러
const handleSaveTemplate = async () => {
  const success = await saveAsTemplate(templateName.value)
  if (success) {
    templateName.value = ''
  }
}

// 템플릿 로드 핸들러
const handleLoadTemplate = () => {
  const success = loadTemplate(selectedTemplate.value)
  if (success) {
    selectedTemplate.value = ''
  }
}

// 빠른 설정 적용
const applyQuickConfig = (configType) => {
  const configs = {
    timeout: {
      "timeout": 60000,
      "readTimeout": 60000,
      "connectionTimeout": 30000
    },
    retry: {
      "retries": 5,
      "retryDelay": 1000,
      "retryOnError": true
    },
    keepalive: {
      "connectionPool": {
        "maxConnections": 100,
        "keepAlive": true,
        "keepAliveTimeout": 60000
      }
    },
    clear: {}
  }
  
  const newConfig = configs[configType] || {}
  testRequest.additionalConfig = JSON.stringify(newConfig, null, 2)
  
  const messages = {
    timeout: '타임아웃 설정이 적용되었습니다',
    retry: '재시도 설정이 적용되었습니다', 
    keepalive: '연결 유지 설정이 적용되었습니다',
    clear: '추가 설정이 초기화되었습니다'
  }
  
  showMessage(messages[configType], 'info')
}

// JSON 설정 실시간 검증
watch(
  () => testRequest.additionalConfig,
  (newVal) => {
    if (newVal && newVal.trim()) {
      try {
        JSON.parse(newVal)
        delete errors.additionalConfig
      } catch (error) {
        errors.additionalConfig = '올바른 JSON 형식이 아닙니다'
      }
    } else {
      delete errors.additionalConfig
    }
  },
  { immediate: true }
)
</script>

<style scoped>
/* 애니메이션 효과 */
.animate-slide-down {
  animation: slideDown 0.3s ease-out;
}

.animate-slide-up {
  animation: slideUp 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    max-height: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    max-height: 500px;
    transform: translateY(0);
  }
}

@keyframes slideUp {
  from {
    opacity: 1;
    max-height: 500px;
    transform: translateY(0);
  }
  to {
    opacity: 0;
    max-height: 0;
    transform: translateY(-10px);
  }
}

/* JSON 텍스트에어리어 스타일 */
textarea {
  resize: vertical;
  min-height: 120px;
}

/* 코드 폰트 */
.font-mono {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.4;
}

/* 버튼 호버 효과 */
button:hover {
  transform: translateY(-1px);
}

button:active {
  transform: translateY(0);
}
</style>