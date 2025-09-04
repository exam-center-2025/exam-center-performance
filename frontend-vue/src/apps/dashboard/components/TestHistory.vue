<template>
  <div>
    <div class="flat-header flex justify-between items-center">
      <h2 class="text-lg font-bold">Test History</h2>
      <button
        @click="refreshHistory"
        :disabled="loading"
        class="flat-btn flat-btn-secondary text-sm disabled:opacity-50"
      >
        {{ loading ? 'Loading...' : 'Refresh' }}
      </button>
    </div>
    
    <!-- Search and Filters -->
    <div class="mb-2 space-y-1">
      <div class="flex space-x-2">
        <input
          v-model="searchText"
          type="text"
          placeholder="Search tests..."
          class="flex-1 flat-input text-sm"
        />
        <select
          v-model="selectedStatus"
          class="flat-input text-sm"
        >
          <option value="">All Status</option>
          <option value="RUNNING">Running</option>
          <option value="COMPLETED">Completed</option>
          <option value="FAILED">Failed</option>
          <option value="STOPPED">Stopped</option>
        </select>
      </div>
    </div>
    
    <div v-if="filteredTests.length === 0" class="text-gray-500 text-center py-8">
      {{ searchText || selectedStatus ? 'No tests match your filters' : 'No test history available' }}
    </div>
    
    <!-- Table View -->
    <div v-else class="overflow-hidden border border-gray-200 rounded">
      <div class="overflow-x-auto">
        <table class="min-w-full border border-gray-200 text-sm">
          <thead class="bg-gray-50/70 border-b border-gray-200">
            <tr>
              <th class="px-3 py-2 text-left text-sm font-semibold text-gray-700 uppercase cursor-pointer" @click="sortBy('testName')">
                Test Name
                <span v-if="sortField === 'testName'" class="ml-1">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
              </th>
              <th class="px-3 py-2 text-left text-sm font-semibold text-gray-700 uppercase cursor-pointer" @click="sortBy('startTime')">
                Start Time
                <span v-if="sortField === 'startTime'" class="ml-1">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
              </th>
              <th class="px-3 py-2 text-left text-sm font-semibold text-gray-700 uppercase cursor-pointer" @click="sortBy('status')">
                Status
                <span v-if="sortField === 'status'" class="ml-1">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
              </th>
              <th class="px-3 py-2 text-left text-sm font-semibold text-gray-700 uppercase">Metrics</th>
              <th class="px-3 py-2 text-left text-sm font-semibold text-gray-700 uppercase">Actions</th>
            </tr>
          </thead>
          <tbody class="bg-white">
            <tr
              v-for="test in paginatedTests"
              :key="test.testId"
              class="border-b border-gray-100 hover:bg-gray-50/50 transition-colors cursor-pointer"
              @click="selectTest(test)"
            >
              <td class="px-3 py-2 whitespace-nowrap">
                <div>
                  <div class="text-base font-medium text-gray-900">{{ test.testName || `Test #${test.testId}` }}</div>
                  <div class="text-sm text-gray-500">ID: {{ test.testId }}</div>
                </div>
              </td>
              <td class="px-3 py-2 whitespace-nowrap">
                <div class="text-base text-gray-900">{{ formatDate(test.startTime) }}</div>
                <div class="text-sm text-gray-500">{{ formatDuration(test.testDurationSeconds) }}</div>
              </td>
              <td class="px-3 py-2 whitespace-nowrap">
                <span :class="getStatusBadgeClass(test.status)" class="status-badge">
                  {{ test.status }}
                </span>
              </td>
              <td class="px-3 py-2 whitespace-nowrap">
                <div class="text-sm text-gray-600">
                  <span class="font-semibold">Users:</span> {{ test.maxConcurrentUsers || 'N/A' }}
                </div>
                <div class="text-sm text-gray-600">
                  <span class="font-semibold">Type:</span> {{ test.runType || 'TEST' }}
                </div>
                <div v-if="test.avgTps" class="text-sm text-gray-600">
                  <span class="font-semibold">TPS:</span> {{ test.avgTps.toFixed(2) }}
                </div>
              </td>
              <td class="px-3 py-2 whitespace-nowrap text-base font-medium space-x-2">
                <button
                  @click.stop="viewDetails(test)"
                  class="text-blue-600 hover:text-blue-900 text-sm font-semibold"
                >
                  Details
                </button>
                <button
                  @click.stop="confirmDelete(test)"
                  class="text-red-600 hover:text-red-900 text-sm font-semibold ml-2"
                >
                  Delete
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- Pagination -->
      <div class="bg-gray-50/50 px-3 py-2 flex items-center justify-between border-t border-gray-200 sm:px-4">
        <div class="flex-1 flex justify-between sm:hidden">
          <button
            @click="previousPage"
            :disabled="currentPage === 1"
            class="relative inline-flex items-center px-3 py-1 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
          >
            Previous
          </button>
          <button
            @click="nextPage"
            :disabled="currentPage === totalPages"
            class="ml-3 relative inline-flex items-center px-3 py-1 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
          >
            Next
          </button>
        </div>
        <div class="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
          <div>
            <p class="text-sm text-gray-700">
              Showing
              <span class="font-medium">{{ (currentPage - 1) * pageSize + 1 }}</span>
              to
              <span class="font-medium">{{ Math.min(currentPage * pageSize, filteredTests.length) }}</span>
              of
              <span class="font-medium">{{ filteredTests.length }}</span>
              results
            </p>
          </div>
          <div>
            <nav class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
              <button
                @click="previousPage"
                :disabled="currentPage === 1"
                class="relative inline-flex items-center px-2 py-1 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
              >
                Previous
              </button>
              <button
                v-for="page in visiblePages"
                :key="page"
                @click="goToPage(page)"
                :class="{
                  'bg-blue-50 border-blue-500 text-blue-600': page === currentPage,
                  'bg-white border-gray-300 text-gray-500 hover:bg-gray-50': page !== currentPage
                }"
                class="relative inline-flex items-center px-3 py-1 border text-sm font-medium"
              >
                {{ page }}
              </button>
              <button
                @click="nextPage"
                :disabled="currentPage === totalPages"
                class="relative inline-flex items-center px-2 py-1 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
              >
                Next
              </button>
            </nav>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 삭제 확인 다이얼로그 -->
  <div v-if="showDeleteDialog" class="fixed inset-0 bg-gray-500 bg-opacity-75 flex items-center justify-center z-50">
    <div class="bg-white border border-gray-300 p-6 w-full max-w-md">
      <h3 class="text-lg font-bold text-gray-900 mb-4">테스트 삭제 확인</h3>
      
      <div class="mb-4">
        <p class="text-sm text-gray-600 mb-2">다음 테스트를 삭제하시겠습니까?</p>
        <div class="bg-gray-50 p-3 border border-gray-200">
          <p class="text-sm font-medium">{{ testToDelete?.testName || 'Unknown Test' }}</p>
          <p class="text-xs text-gray-500">ID: {{ testToDelete?.testId }}</p>
        </div>
      </div>
      
      <div class="bg-yellow-50 border border-yellow-200 p-3 mb-4">
        <p class="text-sm text-yellow-800">
          <i class="fas fa-exclamation-triangle mr-2"></i>
          이 작업은 되돌릴 수 없습니다. 데이터베이스, Redis, 리포트 파일이 모두 삭제됩니다.
        </p>
      </div>
      
      <div class="flex justify-end space-x-3">
        <button
          @click="cancelDelete"
          class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 border border-gray-300 hover:bg-gray-200"
        >
          취소
        </button>
        <button
          @click="executeDelete"
          :disabled="isDeleting"
          class="px-4 py-2 text-sm font-medium text-white bg-red-600 border border-red-700 hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <span v-if="!isDeleting">삭제</span>
          <span v-else>
            <i class="fas fa-spinner fa-spin mr-2"></i>삭제 중...
          </span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { usePerformanceStore } from '@shared/stores/performance'
import performanceApi from '@shared/services/performanceApi'

const store = usePerformanceStore()
const loading = ref(false)
const searchText = ref('')
const selectedStatus = ref('')
const sortField = ref('startTime')
const sortOrder = ref('desc')
const currentPage = ref(1)
const pageSize = ref(10)

// 삭제 관련 상태
const showDeleteDialog = ref(false)
const testToDelete = ref(null)
const isDeleting = ref(false)

// Computed filtered and sorted tests
const filteredTests = computed(() => {
  let tests = [...store.testHistory]
  
  // Apply search filter
  if (searchText.value) {
    const search = searchText.value.toLowerCase()
    tests = tests.filter(test => 
      (test.testName && test.testName.toLowerCase().includes(search)) ||
      (test.testId && test.testId.toString().includes(search))
    )
  }
  
  // Apply status filter
  if (selectedStatus.value) {
    tests = tests.filter(test => test.status === selectedStatus.value)
  }
  
  // Apply sorting
  tests.sort((a, b) => {
    let aVal = a[sortField.value]
    let bVal = b[sortField.value]
    
    if (sortField.value === 'startTime') {
      aVal = new Date(aVal)
      bVal = new Date(bVal)
    }
    
    if (sortOrder.value === 'asc') {
      return aVal > bVal ? 1 : -1
    } else {
      return aVal < bVal ? 1 : -1
    }
  })
  
  return tests
})

const totalPages = computed(() => Math.ceil(filteredTests.value.length / pageSize.value))

const paginatedTests = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredTests.value.slice(start, end)
})

const visiblePages = computed(() => {
  const pages = []
  const total = totalPages.value
  const current = currentPage.value
  
  if (total <= 7) {
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    if (current <= 4) {
      for (let i = 1; i <= 5; i++) pages.push(i)
      pages.push('...')
      pages.push(total)
    } else if (current >= total - 3) {
      pages.push(1)
      pages.push('...')
      for (let i = total - 4; i <= total; i++) pages.push(i)
    } else {
      pages.push(1)
      pages.push('...')
      for (let i = current - 1; i <= current + 1; i++) pages.push(i)
      pages.push('...')
      pages.push(total)
    }
  }
  
  return pages.filter(page => page !== '...' || pages.length > 1)
})

onMounted(() => {
  refreshHistory()
})

async function refreshHistory() {
  loading.value = true
  try {
    await store.loadTestHistory()
  } catch (error) {
    console.error('Failed to refresh history:', error)
  } finally {
    loading.value = false
  }
}



function sortBy(field) {
  if (sortField.value === field) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortOrder.value = 'desc'
  }
  currentPage.value = 1
}

function goToPage(page) {
  if (typeof page === 'number' && page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

function nextPage() {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
  }
}

function previousPage() {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

function viewDetails(test) {
  store.currentTest = test
  // results.html 페이지로 이동하며 testId를 쿼리 파라미터로 전달
  window.location.href = `/performance/vue-dist/results.html?testId=${test.testId}`
}

// 삭제 확인 다이얼로그 표시
function confirmDelete(test) {
  testToDelete.value = test
  showDeleteDialog.value = true
}

// 삭제 취소
function cancelDelete() {
  testToDelete.value = null
  showDeleteDialog.value = false
}

// 테스트 삭제 실행
async function executeDelete() {
  if (!testToDelete.value) return
  
  isDeleting.value = true
  const testId = testToDelete.value.testId
  
  try {
    await performanceApi.deleteTest(testId)
    console.log(`테스트 삭제 성공: ${testId}`)
    
    // 목록 새로고침
    await refreshHistory()
    
    // 다이얼로그 닫기
    showDeleteDialog.value = false
    testToDelete.value = null
    
  } catch (error) {
    console.error('테스트 삭제 실패:', error)
    alert('테스트 삭제 중 오류가 발생했습니다.')
  } finally {
    isDeleting.value = false
  }
}

// Format date
function formatDate(timestamp) {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  return date.toLocaleString()
}

function formatDuration(seconds) {
  if (!seconds) return '-'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return mins > 0 ? `${mins}m ${secs}s` : `${secs}s`
}

// Get status badge class
function getStatusBadgeClass(status) {
  switch (status) {
    case 'RUNNING':
      return 'status-running'
    case 'COMPLETED':
      return 'status-completed'
    case 'FAILED':
    case 'ERROR':
      return 'status-failed'
    case 'STOPPED':
      return 'status-stopped'
    default:
      return 'status-unknown'
  }
}

// Select test
function selectTest(test) {
  store.currentTest = test
  console.log('Selected test:', test)
}
</script>

<style scoped>
/* 플랫 헤더 스타일 */
.flat-header {
  @apply text-sm font-bold text-gray-900 pb-1 mb-2 border-b border-gray-300;
}

/* 플랫 입력 필드 */
.flat-input {
  @apply px-2 py-1 border border-gray-300 bg-white text-gray-900 text-sm;
  border-radius: 0;
  box-shadow: none;
  transition: border-color 0.2s ease;
  height: 32px; /* 고정 높이로 통일성 유지 */
}

.flat-input:focus {
  @apply outline-none border-gray-500;
}

/* 플랫 상태 배지 */
.status-badge {
  @apply inline-flex px-2 py-1 text-sm font-semibold uppercase border;
  border-radius: 0;
  letter-spacing: 0.025em;
}

.status-running {
  @apply bg-blue-500 text-white border-blue-600;
}

.status-completed {
  @apply bg-green-500 text-white border-green-600;
}

.status-failed {
  @apply bg-red-500 text-white border-red-600;
}

.status-stopped {
  @apply bg-yellow-500 text-white border-yellow-600;
}

.status-unknown {
  @apply bg-gray-400 text-white border-gray-500;
}

/* 플랫 버튼 */
.flat-btn {
  @apply px-3 py-1 text-sm font-semibold border transition-colors duration-200;
  border-radius: 0;
  box-shadow: none;
}

.flat-btn-secondary {
  @apply bg-gray-50 text-gray-700 border-gray-300 hover:bg-gray-100;
}
</style>