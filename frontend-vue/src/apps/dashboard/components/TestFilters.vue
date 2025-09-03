<template>
  <div class="card">
    <div class="card-header flex justify-between items-center">
      <h2 class="text-lg font-semibold">Filters & Search</h2>
      <button
        @click="resetFilters"
        class="text-sm text-gray-500 hover:text-gray-700"
      >
        Reset All
      </button>
    </div>
    
    <div class="space-y-4">
      <!-- Search -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Search</label>
        <input
          v-model="localFilters.searchText"
          type="text"
          placeholder="Search by test name or ID..."
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
        />
      </div>

      <!-- Date Range -->
      <div class="grid grid-cols-2 gap-2">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Start Date</label>
          <input
            v-model="localFilters.dateRange.start"
            type="datetime-local"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500 text-sm"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">End Date</label>
          <input
            v-model="localFilters.dateRange.end"
            type="datetime-local"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500 text-sm"
          />
        </div>
      </div>

      <!-- Status Filter -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Status</label>
        <select
          v-model="localFilters.status"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
        >
          <option value="all">All Status</option>
          <option value="RUNNING">Running</option>
          <option value="COMPLETED">Completed</option>
          <option value="FAILED">Failed</option>
          <option value="STOPPED">Stopped</option>
          <option value="ERROR">Error</option>
        </select>
      </div>

      <!-- Plan Filter -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Plan</label>
        <select
          v-model="localFilters.planId"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
        >
          <option value="">All Plans</option>
          <option
            v-for="plan in store.plans"
            :key="plan.id"
            :value="plan.id"
          >
            {{ plan.name }}
          </option>
        </select>
      </div>

      <!-- Sort Options -->
      <div class="grid grid-cols-2 gap-2">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Sort By</label>
          <select
            v-model="localFilters.sortBy"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
          >
            <option value="startTime">Start Time</option>
            <option value="status">Status</option>
            <option value="testName">Test Name</option>
            <option value="maxUsers">Users</option>
            <option value="testDurationSeconds">Duration</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Order</label>
          <select
            v-model="localFilters.sortOrder"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
          >
            <option value="desc">Descending</option>
            <option value="asc">Ascending</option>
          </select>
        </div>
      </div>

      <!-- Apply Button -->
      <button
        @click="applyFilters"
        class="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
      >
        Apply Filters
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { usePerformanceStore } from '@shared/stores/performance'

const emit = defineEmits(['filters-applied'])
const store = usePerformanceStore()

// Local filters for form handling
const localFilters = ref({
  dateRange: { start: null, end: null },
  status: 'all',
  planId: null,
  searchText: '',
  sortBy: 'startTime',
  sortOrder: 'desc'
})

// Initialize with store filters
localFilters.value = { ...store.testFilters }

// Watch for real-time search changes
watch(() => localFilters.value.searchText, (newValue) => {
  if (!newValue) {
    applyFilters()
  }
}, { debounce: 500 })

function applyFilters() {
  store.setTestFilters(localFilters.value)
  emit('filters-applied', localFilters.value)
}

function resetFilters() {
  localFilters.value = {
    dateRange: { start: null, end: null },
    status: 'all',
    planId: null,
    searchText: '',
    sortBy: 'startTime',
    sortOrder: 'desc'
  }
  store.resetTestFilters()
  emit('filters-applied', localFilters.value)
}
</script>