<template>
  <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-2">
    <!-- Today Tests -->
    <div class="metric-card">
      <div class="flex items-center">
        <div class="icon-box border-l-4 border-blue-500">
          <svg class="w-6 h-6 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
          </svg>
        </div>
        <div class="ml-3">
          <p class="text-xs font-medium text-gray-500 uppercase">Today Tests</p>
          <p class="text-lg font-bold text-gray-900">{{ store.quickStats.todayTests }}</p>
        </div>
      </div>
    </div>

    <!-- Average Success Rate -->
    <div class="metric-card">
      <div class="flex items-center">
        <div class="icon-box border-l-4 border-green-500">
          <svg class="w-6 h-6 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <div class="ml-3">
          <p class="text-xs font-medium text-gray-500 uppercase">Success Rate</p>
          <p class="text-lg font-bold text-gray-900">{{ formatPercentage(store.quickStats.avgSuccessRate) }}%</p>
        </div>
      </div>
    </div>

    <!-- Average TPS -->
    <div class="metric-card">
      <div class="flex items-center">
        <div class="icon-box border-l-4 border-purple-500">
          <svg class="w-6 h-6 text-purple-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
          </svg>
        </div>
        <div class="ml-3">
          <p class="text-xs font-medium text-gray-500 uppercase">Avg TPS</p>
          <p class="text-lg font-bold text-gray-900">{{ formatNumber(store.quickStats.avgTps) }}</p>
        </div>
      </div>
    </div>

    <!-- Average Response Time -->
    <div class="metric-card">
      <div class="flex items-center">
        <div class="icon-box border-l-4 border-yellow-500">
          <svg class="w-6 h-6 text-yellow-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <div class="ml-3">
          <p class="text-xs font-medium text-gray-500 uppercase">Avg Response</p>
          <p class="text-lg font-bold text-gray-900">{{ formatNumber(store.quickStats.avgResponseTime) }}ms</p>
        </div>
      </div>
    </div>

    <!-- Active Tests -->
    <div class="metric-card">
      <div class="flex items-center">
        <div class="icon-box border-l-4 border-red-500">
          <div class="w-3 h-3 bg-red-500 animate-pulse"></div>
        </div>
        <div class="ml-3">
          <p class="text-xs font-medium text-gray-500 uppercase">Active Tests</p>
          <p class="text-lg font-bold text-gray-900">{{ store.quickStats.activeTestsCount }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { usePerformanceStore } from '@shared/stores/performance'

const store = usePerformanceStore()

function formatNumber(value) {
  if (!value) return '0'
  return value.toFixed(2)
}

function formatPercentage(value) {
  if (!value) return '0'
  return (value * 100).toFixed(1)
}
</script>

<style scoped>
/* 플랫 디자인 메트릭 카드 */
.metric-card {
  @apply bg-white border border-gray-300 p-2;
  border-radius: 0;
  box-shadow: none;
  transition: all 0.2s ease;
}

.metric-card:hover {
  @apply border-gray-400;
  transform: translateY(-2px);
}

/* 플랫 아이콘 박스 */
.icon-box {
  @apply p-2 bg-white;
  border-radius: 0;
}
</style>