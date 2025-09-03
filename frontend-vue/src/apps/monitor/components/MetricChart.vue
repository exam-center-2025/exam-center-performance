<template>
  <div class="bg-white border border-gray-200 p-3">
    <div class="chart-header mb-2">
      <h3 class="text-sm font-medium text-gray-900">{{ title }}</h3>
      <div class="text-xs text-gray-500">
        현재: <span class="font-semibold">{{ formatCurrentValue() }}</span>
      </div>
    </div>
    
    <div class="chart-container">
      <canvas 
        :id="chartId" 
        :ref="chartId"
        class="w-full"
        style="height: 250px;"
      ></canvas>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  LineController,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  LineController,
  Title,
  Tooltip,
  Legend,
  Filler
)

const props = defineProps({
  chartId: {
    type: String,
    required: true
  },
  title: {
    type: String,
    required: true
  },
  data: {
    type: Array,
    default: () => []
  },
  currentValue: {
    type: [Number, String],
    default: 0
  },
  unit: {
    type: String,
    default: ''
  },
  color: {
    type: String,
    default: '#3b82f6'
  },
  maxDataPoints: {
    type: Number,
    default: 300
  },
  yAxisMax: {
    type: Number,
    default: null
  },
  decimals: {
    type: Number,
    default: 1
  }
})

let chart = null
const chartData = ref([])

// AIDEV-NOTE: Chart.js configuration for real-time line charts
const chartConfig = {
  type: 'line',
  data: {
    labels: [],
    datasets: [{
      label: props.title,
      data: [],
      borderColor: props.color,
      backgroundColor: `${props.color}20`,
      fill: true,
      tension: 0.4,
      pointRadius: 0,
      pointHoverRadius: 4,
      borderWidth: 2
    }]
  },
  options: {
    responsive: true,
    maintainAspectRatio: false,
    interaction: {
      intersect: false,
      mode: 'index'
    },
    plugins: {
      legend: {
        display: false
      },
      tooltip: {
        mode: 'index',
        intersect: false,
        bodyFont: {
          size: 11,
          family: "'Inter', sans-serif"
        },
        titleFont: {
          size: 11,
          family: "'Inter', sans-serif"
        },
        callbacks: {
          label: function(context) {
            return `${context.dataset.label}: ${context.parsed.y}${props.unit ? ' ' + props.unit : ''}`
          }
        }
      }
    },
    scales: {
      x: {
        display: true,
        title: {
          display: false
        },
        grid: {
          display: false
        },
        ticks: {
          maxTicksLimit: 10,
          font: {
            size: 10,
            family: "'Inter', sans-serif"
          },
          callback: function(value, index) {
            if (index % Math.ceil(this.chart.data.labels.length / 5) === 0) {
              return this.chart.data.labels[index]
            }
            return ''
          }
        }
      },
      y: {
        display: true,
        beginAtZero: true,
        max: props.yAxisMax,
        title: {
          display: true,
          text: props.unit,
          font: {
            size: 10,
            family: "'Inter', sans-serif"
          }
        },
        grid: {
          color: '#f3f4f6'
        },
        ticks: {
          font: {
            size: 10,
            family: "'Inter', sans-serif"
          },
          callback: function(value) {
            return value.toFixed(props.decimals) + (props.unit ? ' ' + props.unit : '')
          }
        }
      }
    },
    animation: {
      duration: 500,
      easing: 'easeInOutQuart'
    },
    elements: {
      point: {
        radius: 0
      }
    }
  }
}

function formatCurrentValue() {
  const value = typeof props.currentValue === 'number' 
    ? props.currentValue.toFixed(props.decimals)
    : props.currentValue
  return `${value}${props.unit ? ' ' + props.unit : ''}`
}

function formatTimestamp(timestamp) {
  const date = new Date(timestamp)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

function updateChart() {
  if (!chart) return

  const labels = chartData.value.map(item => formatTimestamp(item.timestamp))
  const data = chartData.value.map(item => item.value)

  chart.data.labels = labels
  chart.data.datasets[0].data = data
  chart.update('none') // No animation for real-time updates
}

function addDataPoint(value, timestamp) {
  chartData.value.push({
    value: typeof value === 'number' ? value : 0,
    timestamp: timestamp || Date.now()
  })

  // Limit data points
  if (chartData.value.length > props.maxDataPoints) {
    chartData.value.shift()
  }

  updateChart()
}

function initializeChart() {
  nextTick(() => {
    const canvas = document.getElementById(props.chartId)
    if (!canvas) {
      console.error(`Canvas element with id "${props.chartId}" not found`)
      return
    }

    // Update chart config with current props
    chartConfig.data.datasets[0].borderColor = props.color
    chartConfig.data.datasets[0].backgroundColor = `${props.color}20`
    chartConfig.data.datasets[0].label = props.title
    chartConfig.options.scales.y.max = props.yAxisMax

    chart = new ChartJS(canvas, chartConfig)
    
    // Initialize with existing data if any
    if (props.data && props.data.length > 0) {
      chartData.value = props.data.slice(-props.maxDataPoints)
      updateChart()
    }
  })
}

function destroyChart() {
  if (chart) {
    chart.destroy()
    chart = null
  }
}

// Watch for data changes
watch(() => props.data, (newData) => {
  if (newData && newData.length > 0) {
    // Take the most recent data points
    const recentData = newData.slice(-props.maxDataPoints)
    chartData.value = recentData
    updateChart()
  }
}, { deep: true })

// Watch for current value changes to add new data points
watch(() => props.currentValue, (newValue) => {
  if (typeof newValue === 'number' && newValue !== 0) {
    addDataPoint(newValue)
  }
})

onMounted(() => {
  initializeChart()
})

onUnmounted(() => {
  destroyChart()
})

// Expose methods for parent components
defineExpose({
  addDataPoint,
  updateChart,
  clearData: () => {
    chartData.value = []
    updateChart()
  }
})
</script>

<style scoped>
.chart-container {
  position: relative;
  height: 250px;
}
</style>