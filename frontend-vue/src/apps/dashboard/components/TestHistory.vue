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
    <div v-else class="overflow-hidden">
      <div class="overflow-x-auto">
        <table class="min-w-full border border-gray-300">
          <thead class="bg-gray-50 border-b border-gray-300">
            <tr>
              <th class="px-2 py-1 text-left text-xs font-medium text-gray-500 uppercase cursor-pointer" @click="sortBy('testName')">
                Test Name
                <span v-if="sortField === 'testName'" class="ml-1">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
              </th>
              <th class="px-2 py-1 text-left text-xs font-medium text-gray-500 uppercase cursor-pointer" @click="sortBy('startTime')">
                Start Time
                <span v-if="sortField === 'startTime'" class="ml-1">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
              </th>
              <th class="px-2 py-1 text-left text-xs font-medium text-gray-500 uppercase cursor-pointer" @click="sortBy('status')">
                Status
                <span v-if="sortField === 'status'" class="ml-1">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Metrics</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
            </tr>
          </thead>
          <tbody class="bg-white">
            <tr
              v-for="test in paginatedTests"
              :key="test.id"
              class="border-b border-gray-200 hover:bg-gray-50 transition-colors"
              @click="selectTest(test)"
            >
              <td class="px-2 py-2 whitespace-nowrap">
                <div>
                  <div class="text-sm font-medium text-gray-900">{{ test.testName || `Test #${test.id}` }}</div>
                  <div class="text-sm text-gray-500">ID: {{ test.id }}</div>
                </div>
              </td>
              <td class="px-2 py-2 whitespace-nowrap">
                <div class="text-sm text-gray-900">{{ formatDate(test.startTime) }}</div>
                <div class="text-sm text-gray-500">Duration: {{ formatDuration(test.testDurationSeconds) }}</div>
              </td>
              <td class="px-2 py-2 whitespace-nowrap">
                <span :class="getStatusBadgeClass(test.status)" class="status-badge">
                  {{ test.status }}
                </span>
              </td>
              <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-500">
                <div>Users: {{ test.maxUsers }}</div>
                <div>Type: {{ test.runType || 'TEST' }}</div>
                <div v-if="test.avgTps">TPS: {{ test.avgTps.toFixed(2) }}</div>
              </td>
              <td class="px-4 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                <button
                  @click.stop="viewDetails(test)"
                  class="text-blue-600 hover:text-blue-900"
                >
                  Details
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- Pagination -->
      <div class="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
        <div class="flex-1 flex justify-between sm:hidden">
          <button
            @click="previousPage"
            :disabled="currentPage === 1"
            class="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
          >
            Previous
          </button>
          <button
            @click="nextPage"
            :disabled="currentPage === totalPages"
            class="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
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
                class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
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
                class="relative inline-flex items-center px-4 py-2 border text-sm font-medium"
              >
                {{ page }}
              </button>
              <button
                @click="nextPage"
                :disabled="currentPage === totalPages"
                class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
              >
                Next
              </button>
            </nav>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { usePerformanceStore } from '@shared/stores/performance'

const store = usePerformanceStore()
const loading = ref(false)
const searchText = ref('')
const selectedStatus = ref('')
const sortField = ref('startTime')
const sortOrder = ref('desc')
const currentPage = ref(1)
const pageSize = ref(10)

// Computed filtered and sorted tests
const filteredTests = computed(() => {
  let tests = [...store.testHistory]
  
  // Apply search filter
  if (searchText.value) {
    const search = searchText.value.toLowerCase()
    tests = tests.filter(test => 
      (test.testName && test.testName.toLowerCase().includes(search)) ||
      (test.id && test.id.toString().includes(search))
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
  @apply text-base font-bold text-gray-900 pb-2 mb-2 border-b border-gray-300;
}

/* 플랫 입력 필드 */
.flat-input {
  @apply px-2 py-1 border border-gray-300 bg-white text-gray-900 text-sm;
  border-radius: 0;
  box-shadow: none;
  transition: border-color 0.2s ease;
}

.flat-input:focus {
  @apply outline-none border-gray-500;
}

/* 플랫 상태 배지 */
.status-badge {
  @apply inline-flex px-2 py-0.5 text-xs font-bold uppercase border;
  border-radius: 0;
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