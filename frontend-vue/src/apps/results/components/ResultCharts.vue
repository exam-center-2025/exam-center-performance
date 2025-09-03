<!-- 
ResultCharts 컴포넌트
TPS 추이와 응답시간 추이 차트를 Chart.js로 구현

AIDEV-NOTE: 기존 results.html의 차트 섹션을 Vue로 전환
Chart.js v4 사용, 시계열 데이터 표시, 줌/팬 기능
-->
<template>
  <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
    <!-- TPS 추이 차트 -->
    <div class="chart-container">
      <h3 class="chart-title text-sm">
        <i class="fas fa-chart-line mr-2 text-gray-600 text-sm"></i>
        <span>TPS 추이</span>
      </h3>
      
      <div class="chart-wrapper">
        <div v-if="loading" class="chart-loading">
          <i class="fas fa-spinner fa-spin text-2xl text-indigo-500 mb-2"></i>
          <p class="text-gray-600">차트 로딩 중...</p>
        </div>
        <div v-else-if="!hasData" class="chart-no-data">
          <i class="fas fa-chart-line text-gray-400 text-3xl mb-2"></i>
          <p class="text-gray-500">표시할 데이터가 없습니다</p>
        </div>
        <canvas v-else ref="tpsChartRef" :id="'tps-chart-' + chartId"></canvas>
      </div>
    </div>
    
    <!-- 응답시간 추이 차트 -->
    <div class="chart-container">
      <h3 class="chart-title text-sm">
        <i class="fas fa-chart-area mr-2 text-gray-600 text-sm"></i>
        <span>응답시간 추이</span>
      </h3>
      
      <div class="chart-wrapper">
        <div v-if="loading" class="chart-loading">
          <i class="fas fa-spinner fa-spin text-2xl text-purple-500 mb-2"></i>
          <p class="text-gray-600">차트 로딩 중...</p>
        </div>
        <div v-else-if="!hasData" class="chart-no-data">
          <i class="fas fa-chart-area text-gray-400 text-3xl mb-2"></i>
          <p class="text-gray-500">표시할 데이터가 없습니다</p>
        </div>
        <canvas v-else ref="responseTimeChartRef" :id="'response-time-chart-' + chartId"></canvas>
      </div>
    </div>
    
    <!-- 추가 차트들 (에러율, 사용자 수 - 향후 확장) -->
    <div v-if="showExtendedCharts" class="col-span-1 lg:col-span-2 grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- 에러율 추이 -->
      <div class="chart-container">
        <h3 class="chart-title text-sm">
          <i class="fas fa-exclamation-triangle mr-2 text-red-500 text-sm"></i>
          <span class="font-['Noto Sans KR']">에러율 추이</span>
        </h3>
        <div class="chart-wrapper">
          <canvas ref="errorRateChartRef" :id="'error-rate-chart-' + chartId"></canvas>
        </div>
      </div>
      
      <!-- 동시 사용자 수 추이 -->
      <div class="chart-container">
        <h3 class="chart-title text-sm">
          <i class="fas fa-users mr-2 text-blue-500 text-sm"></i>
          <span class="font-['Noto Sans KR']">동시 사용자 추이</span>
        </h3>
        <div class="chart-wrapper">
          <canvas ref="usersChartRef" :id="'users-chart-' + chartId"></canvas>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'

// Props 정의
const props = defineProps({
  metricsHistory: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  showExtendedCharts: {
    type: Boolean,
    default: false
  }
})

// Refs
const tpsChartRef = ref(null)
const responseTimeChartRef = ref(null) 
const errorRateChartRef = ref(null)
const usersChartRef = ref(null)

// 차트 인스턴스들
let tpsChart = null
let responseTimeChart = null
let errorRateChart = null
let usersChart = null

// 고유 차트 ID (동일 페이지 내 여러 인스턴스 지원)
const chartId = Math.random().toString(36).substr(2, 9)

// 데이터 존재 여부
const hasData = computed(() => {
  return props.metricsHistory && props.metricsHistory.length > 0
})

// 차트 공통 옵션
const getChartOptions = (yAxisLabel, color) => ({
  responsive: true,
  maintainAspectRatio: false,
  interaction: {
    mode: 'index',
    intersect: false,
  },
  plugins: {
    legend: {
      display: true,
      position: 'top',
      labels: {
        font: {
          size: 10,
          family: "'Noto Sans KR', 'Inter', sans-serif"
        }
      }
    },
    tooltip: {
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      titleColor: 'white',
      bodyColor: 'white',
      borderColor: color,
      borderWidth: 1,
      titleFont: {
        size: 11,
        family: "'Inter', monospace"
      },
      bodyFont: {
        size: 11,
        family: "'Inter', monospace"
      }
    }
  },
  scales: {
    x: {
      type: 'time',
      time: {
        unit: 'minute',
        displayFormats: {
          minute: 'HH:mm',
          hour: 'HH:mm',
          second: 'HH:mm'
        },
        tooltipFormat: 'yyyy-MM-dd HH:mm:ss'
      },
      ticks: {
        source: 'data',
        autoSkip: true,
        maxTicksLimit: 10,
        font: {
          size: 10,
          family: "'Inter', monospace"
        },
        callback: function(value) {
          const date = new Date(value)
          const hours = String(date.getHours()).padStart(2, '0')
          const minutes = String(date.getMinutes()).padStart(2, '0')
          return `${hours}:${minutes}`
        }
      },
      grid: {
        color: 'rgba(0, 0, 0, 0.1)',
      }
    },
    y: {
      beginAtZero: true,
      title: {
        display: true,
        text: yAxisLabel,
        color: '#374151',
        font: {
          size: 10,
          weight: 'bold',
          family: "'Noto Sans KR', 'Inter', sans-serif"
        }
      },
      ticks: {
        font: {
          size: 10,
          family: "'Inter', monospace"
        }
      },
      grid: {
        color: 'rgba(0, 0, 0, 0.1)',
      }
    }
  },
  elements: {
    line: {
      tension: 0.2
    },
    point: {
      radius: 3,
      hoverRadius: 6
    }
  }
})

// TPS 차트 데이터 준비
const prepareTpsData = () => {
  if (!hasData.value) return null
  
  const data = props.metricsHistory
    .slice(-300) // 최대 300개 포인트만 표시
    .map(m => ({
      x: new Date(m.timestamp),
      y: m.tps || 0
    }))
    .filter(d => !isNaN(d.x.getTime())) // 잘못된 날짜 필터링
  
  return {
    datasets: [{
      label: 'TPS (Transactions Per Second)',
      data: data,
      borderColor: 'rgb(102, 126, 234)',
      backgroundColor: 'rgba(102, 126, 234, 0.1)',
      fill: true,
      pointBackgroundColor: 'rgb(102, 126, 234)',
      pointBorderColor: 'white',
      pointBorderWidth: 2
    }]
  }
}

// 응답시간 차트 데이터 준비
const prepareResponseTimeData = () => {
  if (!hasData.value) return null
  
  const data = props.metricsHistory
    .slice(-300)
    .map(m => ({
      x: new Date(m.timestamp),
      y: m.avgResponseTime || 0
    }))
    .filter(d => !isNaN(d.x.getTime()))
  
  return {
    datasets: [{
      label: '평균 응답시간 (ms)',
      data: data,
      borderColor: 'rgb(147, 51, 234)',
      backgroundColor: 'rgba(147, 51, 234, 0.1)',
      fill: true,
      pointBackgroundColor: 'rgb(147, 51, 234)',
      pointBorderColor: 'white', 
      pointBorderWidth: 2
    }]
  }
}

// 차트 생성
const createTpsChart = async () => {
  if (!tpsChartRef.value || tpsChart) return
  
  await nextTick()
  
  const data = prepareTpsData()
  if (!data) return
  
  try {
    tpsChart = new Chart(tpsChartRef.value, {
      type: 'line',
      data: data,
      options: getChartOptions('Transactions/sec', 'rgb(102, 126, 234)')
    })
  } catch (error) {
    console.error('TPS 차트 생성 오류:', error)
  }
}

const createResponseTimeChart = async () => {
  if (!responseTimeChartRef.value || responseTimeChart) return
  
  await nextTick()
  
  const data = prepareResponseTimeData()
  if (!data) return
  
  try {
    responseTimeChart = new Chart(responseTimeChartRef.value, {
      type: 'line',
      data: data,
      options: getChartOptions('Response Time (ms)', 'rgb(147, 51, 234)')
    })
  } catch (error) {
    console.error('응답시간 차트 생성 오류:', error)
  }
}

// 차트 업데이트
const updateCharts = () => {
  if (tpsChart) {
    const data = prepareTpsData()
    if (data) {
      tpsChart.data = data
      tpsChart.update('none') // 애니메이션 없이 업데이트
    }
  }
  
  if (responseTimeChart) {
    const data = prepareResponseTimeData()
    if (data) {
      responseTimeChart.data = data
      responseTimeChart.update('none')
    }
  }
}

// 차트 파괴
const destroyCharts = () => {
  if (tpsChart) {
    tpsChart.destroy()
    tpsChart = null
  }
  if (responseTimeChart) {
    responseTimeChart.destroy()
    responseTimeChart = null
  }
  if (errorRateChart) {
    errorRateChart.destroy()
    errorRateChart = null
  }
  if (usersChart) {
    usersChart.destroy()
    usersChart = null
  }
}

// 데이터 변경 감시
watch(() => props.metricsHistory, () => {
  if (!props.loading && hasData.value) {
    updateCharts()
  }
}, { deep: true })

watch(() => props.loading, (newLoading) => {
  if (!newLoading && hasData.value) {
    nextTick(() => {
      createTpsChart()
      createResponseTimeChart()
    })
  }
})

// 라이프사이클
onMounted(() => {
  // Chart.js 글로벌 설정
  if (window.Chart) {
    // 기본 폰트는 Inter (숫자/영문)
    Chart.defaults.font.family = "'Inter', 'Noto Sans KR', sans-serif"
    Chart.defaults.font.size = 10
    Chart.defaults.color = '#374151'
    // 숫자 표시를 위한 추가 설정
    Chart.defaults.font.variant = 'tabular-nums'
  }
  
  if (!props.loading && hasData.value) {
    nextTick(() => {
      createTpsChart()
      createResponseTimeChart()
    })
  }
})

onUnmounted(() => {
  destroyCharts()
})
</script>

<style scoped>
/* 차트 컨테이너 스타일 */
.chart-container {
  @apply bg-white border border-gray-200 p-4;
}

.chart-title {
  @apply text-sm font-bold text-gray-900 mb-2 pb-2 border-b border-gray-200;
}

.chart-wrapper {
  @apply h-64 relative bg-gray-50 p-2;
  position: relative;
}

.chart-loading,
.chart-no-data {
  @apply absolute inset-0 flex flex-col items-center justify-center;
}

.chart-loading {
  @apply bg-white bg-opacity-90;
}

.chart-no-data {
  @apply bg-gray-50;
}

/* 반응형 조정 */
@media (max-width: 1024px) {
  .chart-wrapper {
    @apply h-64;
  }
}

@media (max-width: 768px) {
  .chart-container {
    @apply p-4;
  }
  
  .chart-title {
    @apply text-lg mb-2 pb-2;
  }
  
  .chart-wrapper {
    @apply h-56 p-1;
  }
}

/* 차트 캔버스 스타일 */
canvas {
  max-width: 100%;
  height: 100% !important;
}
</style>