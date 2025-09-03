<!-- 
DetailTable 컴포넌트
테스트의 상세 정보를 테이블 형태로 표시

AIDEV-NOTE: 기존 results.html의 상세 정보 테이블을 Vue로 전환
플랫 디자인 스타일, 상태 배지, 에러 메시지 표시
-->
<template>
  <div class="flat-card p-4 mb-4">
    <!-- 테이블 헤더 -->
    <h3 class="table-title">
      <i class="fas fa-list-alt mr-2 text-gray-600"></i>
      <span>테스트 상세 정보</span>
    </h3>
    
    <!-- 상세 정보 테이블 -->
    <div class="overflow-x-auto">
      <table class="detail-table">
        <tbody>
          <!-- 테스트 이름 -->
          <tr>
            <th class="table-header">
              <i class="fas fa-tag mr-2 text-gray-500"></i>
              테스트 이름
            </th>
            <td class="table-cell">
              <span class="font-medium">{{ testResult.testName || 'N/A' }}</span>
            </td>
          </tr>
          
          <!-- Plan ID -->
          <tr>
            <th class="table-header">
              <i class="fas fa-clipboard mr-2 text-gray-500"></i>
              Plan ID
            </th>
            <td class="table-cell font-mono">
              {{ testResult.planId || 'N/A' }}
            </td>
          </tr>
          
          <!-- 테스트 상태 -->
          <tr>
            <th class="table-header">
              <i class="fas fa-info-circle mr-2 text-gray-500"></i>
              상태
            </th>
            <td class="table-cell">
              <span 
                :class="getStatusBadgeClass(testResult.status)"
                class="status-badge"
              >
                <i :class="getStatusIcon(testResult.status)" class="mr-1"></i>
                {{ getStatusText(testResult.status) }}
              </span>
            </td>
          </tr>
          
          <!-- 최대 동시 사용자 -->
          <tr>
            <th class="table-header">
              <i class="fas fa-users mr-2 text-gray-500"></i>
              최대 동시 사용자
            </th>
            <td class="table-cell font-mono">
              {{ formatNumber(testResult.maxConcurrentUsers) }}
            </td>
          </tr>
          
          <!-- 테스트 유형 (확장 필드) -->
          <tr v-if="testResult.testType">
            <th class="table-header">
              <i class="fas fa-cog mr-2 text-gray-500"></i>
              테스트 유형
            </th>
            <td class="table-cell">
              {{ testResult.testType }}
            </td>
          </tr>
          
          <!-- 시나리오 (확장 필드) -->
          <tr v-if="testResult.scenario">
            <th class="table-header">
              <i class="fas fa-play mr-2 text-gray-500"></i>
              시나리오
            </th>
            <td class="table-cell">
              {{ testResult.scenario }}
            </td>
          </tr>
          
          <!-- 설명 -->
          <tr v-if="testResult.description">
            <th class="table-header">
              <i class="fas fa-file-alt mr-2 text-gray-500"></i>
              설명
            </th>
            <td class="table-cell">
              <span class="text-gray-700">{{ testResult.description }}</span>
            </td>
          </tr>
          
          <!-- 에러 메시지 (오류가 있는 경우만 표시) -->
          <tr v-if="testResult.errorMessage" class="error-row">
            <th class="table-header text-red-700">
              <i class="fas fa-exclamation-triangle mr-2 text-red-500"></i>
              에러 메시지
            </th>
            <td class="table-cell">
              <div class="error-message">
                <i class="fas fa-times-circle text-red-500 mr-2"></i>
                <span class="text-red-600 font-medium">{{ testResult.errorMessage }}</span>
              </div>
            </td>
          </tr>
          
          <!-- 생성 일시 -->
          <tr v-if="testResult.createdAt">
            <th class="table-header">
              <i class="fas fa-calendar-plus mr-2 text-gray-500"></i>
              생성 일시
            </th>
            <td class="table-cell font-mono">
              {{ formatDateTime(testResult.createdAt) }}
            </td>
          </tr>
          
          <!-- 수정 일시 -->
          <tr v-if="testResult.updatedAt">
            <th class="table-header">
              <i class="fas fa-calendar-check mr-2 text-gray-500"></i>
              수정 일시
            </th>
            <td class="table-cell font-mono">
              {{ formatDateTime(testResult.updatedAt) }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- 추가 정보 섹션 (확장 가능) -->
    <div v-if="hasAdditionalInfo" class="mt-6 pt-6 border-t border-gray-200">
      <h4 class="text-base font-semibold text-gray-800 mb-2">
        <i class="fas fa-info mr-2 text-gray-600"></i>
        추가 정보
      </h4>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
        <div v-if="testResult.version" class="info-item">
          <span class="info-label">버전:</span>
          <span class="info-value">{{ testResult.version }}</span>
        </div>
        
        <div v-if="testResult.environment" class="info-item">
          <span class="info-label">환경:</span>
          <span class="info-value">{{ testResult.environment }}</span>
        </div>
        
        <div v-if="testResult.tags && testResult.tags.length > 0" class="info-item">
          <span class="info-label">태그:</span>
          <div class="flex flex-wrap gap-1 mt-1">
            <span 
              v-for="tag in testResult.tags" 
              :key="tag"
              class="inline-block bg-gray-100 text-gray-700 px-2 py-1 border border-gray-200 text-xs"
            >
              {{ tag }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

// Props 정의
const props = defineProps({
  testResult: {
    type: Object,
    required: true,
    default: () => ({})
  }
})

// 숫자 포매터
const formatNumber = (num) => {
  if (num == null) return 'N/A'
  return new Intl.NumberFormat('ko-KR').format(num)
}

// 날짜시간 포매터
const formatDateTime = (dateTime) => {
  if (!dateTime) return 'N/A'
  
  try {
    const date = new Date(dateTime)
    if (isNaN(date.getTime())) return 'Invalid Date'
    
    return new Intl.DateTimeFormat('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit', 
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    }).format(date)
  } catch (error) {
    console.warn('날짜 포맷팅 오류:', error)
    return 'N/A'
  }
}

// 상태 텍스트 변환
const getStatusText = (status) => {
  const statusMap = {
    'COMPLETED': '완료',
    'RUNNING': '실행 중', 
    'FAILED': '실패',
    'PENDING': '대기 중',
    'CANCELLED': '취소됨'
  }
  return statusMap[status?.toUpperCase()] || status || 'N/A'
}

// 상태 배지 CSS 클래스
const getStatusBadgeClass = (status) => {
  const baseClass = 'status-badge'
  switch (status?.toLowerCase()) {
    case 'completed':
      return `${baseClass} status-completed`
    case 'running':
      return `${baseClass} status-running`
    case 'failed':
      return `${baseClass} status-failed`
    case 'pending':
      return `${baseClass} status-pending`
    case 'cancelled':
      return `${baseClass} status-cancelled`
    default:
      return `${baseClass} status-unknown`
  }
}

// 상태 아이콘
const getStatusIcon = (status) => {
  switch (status?.toLowerCase()) {
    case 'completed':
      return 'fas fa-check-circle'
    case 'running':
      return 'fas fa-spinner fa-spin'
    case 'failed':
      return 'fas fa-times-circle'
    case 'pending':
      return 'fas fa-clock'
    case 'cancelled':
      return 'fas fa-ban'
    default:
      return 'fas fa-question-circle'
  }
}

// 추가 정보 존재 여부
const hasAdditionalInfo = computed(() => {
  const result = props.testResult
  return result.version || result.environment || (result.tags && result.tags.length > 0)
})
</script>

<style scoped>
/* AIDEV-NOTE: 플랫 디자인 카드 스타일 - 대시보드와 일치 */
.flat-card {
  @apply bg-white border border-gray-200;
}

/* 테이블 제목 */
.table-title {
  @apply text-base font-bold text-gray-900 mb-2 pb-2 border-b border-gray-200;
}

/* 상세 테이블 스타일 */
.detail-table {
  @apply w-full border-collapse;
}

.table-header {
  @apply px-3 py-2 text-left font-semibold bg-gray-50 border border-gray-200 text-gray-800 text-sm;
  width: 180px;
  min-width: 180px;
}

.table-cell {
  @apply px-3 py-2 bg-white border border-gray-200 text-gray-900 font-medium text-sm;
}

/* 상태 배지 */
.status-badge {
  @apply inline-flex items-center px-2 py-1 text-xs font-medium border;
}

.status-badge.status-completed {
  @apply bg-green-500 border-green-600 text-white;
}

.status-badge.status-running {
  @apply bg-blue-500 border-blue-600 text-white;
}

.status-badge.status-failed {
  @apply bg-red-500 border-red-600 text-white;
}

.status-badge.status-pending {
  @apply bg-yellow-500 border-yellow-600 text-white;
}

.status-badge.status-cancelled {
  @apply bg-gray-500 border-gray-600 text-white;
}

.status-badge.status-unknown {
  @apply bg-gray-400 border-gray-500 text-white;
}

/* 에러 행 스타일 */
.error-row .table-header {
  @apply bg-red-50 border-red-200;
}

.error-row .table-cell {
  @apply bg-red-50 border-red-200;
}

.error-message {
  @apply flex items-center p-3 bg-red-100 border border-red-300;
}

/* 추가 정보 스타일 */
.info-item {
  @apply flex items-start gap-2;
}

.info-label {
  @apply font-semibold text-gray-600 min-w-20;
}

.info-value {
  @apply text-gray-800 font-mono;
}

/* 반응형 조정 */
@media (max-width: 768px) {
  .table-header {
    @apply px-2 py-2 text-sm;
    width: 140px;
    min-width: 140px;
  }
  
  .table-cell {
    @apply px-2 py-2 text-sm;
  }
  
  .table-title {
    @apply text-lg;
  }
  
  .error-message {
    @apply flex-col items-start p-2;
  }
}

/* 테이블 행 호버 효과 */
.detail-table tbody tr:hover .table-header,
.detail-table tbody tr:hover .table-cell {
  @apply bg-blue-50 border-blue-200;
}

.error-row:hover .table-header,
.error-row:hover .table-cell {
  @apply bg-red-100 border-red-300;
}
</style>