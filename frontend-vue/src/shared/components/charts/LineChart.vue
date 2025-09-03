<!--
  LineChart 컴포넌트
  AIDEV-NOTE: Chart.js 기반 선 차트, 시계열 데이터 시각화
  @example
  <LineChart 
    :data="chartData" 
    :options="chartOptions"
    title="사용자 증가 추이"
    height="300"
  />
-->
<template>
  <div :class="containerClasses">
    <!-- 차트 헤더 -->
    <div v-if="title || $slots.header" :class="headerClasses">
      <div class="flex-1">
        <h3 v-if="title" :class="titleClasses">{{ title }}</h3>
        <p v-if="subtitle" :class="subtitleClasses">{{ subtitle }}</p>
      </div>
      <div v-if="$slots.header" class="flex-shrink-0">
        <slot name="header"></slot>
      </div>
    </div>

    <!-- 차트 컨테이너 -->
    <div :class="chartContainerClasses" :style="chartContainerStyle">
      <!-- 로딩 상태 -->
      <div v-if="loading" :class="loadingClasses">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-3"></div>
        <p class="text-sm text-gray-500">{{ loadingText }}</p>
      </div>

      <!-- 에러 상태 -->
      <div v-else-if="error" :class="errorClasses">
        <svg class="h-8 w-8 text-red-500 mx-auto mb-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <p class="text-sm text-red-600 text-center">{{ error }}</p>
      </div>

      <!-- 빈 데이터 상태 -->
      <div v-else-if="!hasData" :class="emptyClasses">
        <svg class="h-12 w-12 text-gray-400 mx-auto mb-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
        </svg>
        <p class="text-sm text-gray-500 text-center">{{ emptyText }}</p>
      </div>

      <!-- 차트 캔버스 -->
      <canvas
        v-else
        ref="chartCanvas"
        :class="canvasClasses"
        role="img"
        :aria-label="ariaLabel"
      ></canvas>
    </div>

    <!-- 범례 -->
    <div v-if="showLegend && hasData" :class="legendClasses">
      <div
        v-for="(dataset, index) in data.datasets"
        :key="index"
        class="flex items-center space-x-2"
      >
        <div
          class="w-3 h-3 rounded"
          :style="{ backgroundColor: dataset.backgroundColor || dataset.borderColor }"
        ></div>
        <span class="text-sm text-gray-700">{{ dataset.label }}</span>
      </div>
    </div>

    <!-- 푸터 -->
    <div v-if="$slots.footer" :class="footerClasses">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script>
import { Chart, registerables } from 'chart.js';

// Chart.js 컴포넌트 등록
Chart.register(...registerables);

export default {
  name: 'LineChart',
  props: {
    // 데이터
    data: {
      type: Object,
      required: true,
      validator: (data) => data && data.labels && data.datasets
    },
    
    // 옵션
    options: {
      type: Object,
      default: () => ({})
    },
    
    // 기본 속성
    title: {
      type: String,
      default: ''
    },
    subtitle: {
      type: String,
      default: ''
    },
    
    // 크기
    width: {
      type: [String, Number],
      default: '100%'
    },
    height: {
      type: [String, Number],
      default: 400
    },
    
    // 상태
    loading: {
      type: Boolean,
      default: false
    },
    error: {
      type: String,
      default: ''
    },
    
    // 텍스트
    loadingText: {
      type: String,
      default: '차트를 불러오는 중...'
    },
    emptyText: {
      type: String,
      default: '표시할 데이터가 없습니다'
    },
    
    // 스타일
    theme: {
      type: String,
      default: 'default',
      validator: (value) => ['default', 'minimal', 'dark'].includes(value)
    },
    
    // 기능
    responsive: {
      type: Boolean,
      default: true
    },
    showLegend: {
      type: Boolean,
      default: true
    },
    animated: {
      type: Boolean,
      default: true
    },
    
    // 접근성
    ariaLabel: {
      type: String,
      default: '선형 차트'
    }
  },
  
  emits: ['chart-ready', 'data-point-click', 'legend-click'],
  
  data() {
    return {
      chartInstance: null,
      isDestroyed: false
    };
  },
  
  computed: {
    hasData() {
      return this.data && 
             this.data.datasets && 
             this.data.datasets.length > 0 &&
             this.data.datasets.some(dataset => dataset.data && dataset.data.length > 0);
    },
    
    containerClasses() {
      const classes = ['line-chart-container', 'bg-white', 'rounded-lg'];
      
      return classes.join(' ');
    },
    
    headerClasses() {
      return 'chart-header flex items-start justify-between p-4 pb-0';
    },
    
    titleClasses() {
      const classes = ['chart-title', 'text-lg', 'font-semibold', 'text-gray-900'];
      
      return classes.join(' ');
    },
    
    subtitleClasses() {
      return 'chart-subtitle text-sm text-gray-500 mt-1';
    },
    
    chartContainerClasses() {
      return 'chart-container relative p-4';
    },
    
    chartContainerStyle() {
      return {
        width: this.width,
        height: typeof this.height === 'number' ? `${this.height}px` : this.height
      };
    },
    
    loadingClasses() {
      return 'loading-state flex flex-col items-center justify-center h-64';
    },
    
    errorClasses() {
      return 'error-state flex flex-col items-center justify-center h-64';
    },
    
    emptyClasses() {
      return 'empty-state flex flex-col items-center justify-center h-64';
    },
    
    canvasClasses() {
      return 'chart-canvas max-w-full h-auto';
    },
    
    legendClasses() {
      return 'chart-legend flex flex-wrap gap-4 p-4 pt-0';
    },
    
    footerClasses() {
      return 'chart-footer p-4 pt-0';
    },
    
    computedOptions() {
      const baseOptions = {
        responsive: this.responsive,
        maintainAspectRatio: false,
        animation: {
          duration: this.animated ? 750 : 0
        },
        interaction: {
          intersect: false,
          mode: 'index'
        },
        plugins: {
          legend: {
            display: false // 커스텀 범례 사용
          },
          tooltip: {
            backgroundColor: 'rgba(0, 0, 0, 0.8)',
            titleColor: '#fff',
            bodyColor: '#fff',
            borderColor: 'rgba(255, 255, 255, 0.1)',
            borderWidth: 1,
            cornerRadius: 6,
            displayColors: true,
            callbacks: {
              title: (tooltipItems) => {
                return tooltipItems[0]?.label || '';
              },
              label: (context) => {
                const label = context.dataset.label || '';
                const value = context.parsed.y;
                return `${label}: ${this.formatValue(value)}`;
              }
            }
          }
        },
        scales: {
          x: {
            grid: {
              display: true,
              color: 'rgba(0, 0, 0, 0.1)'
            },
            ticks: {
              color: '#6B7280'
            }
          },
          y: {
            grid: {
              display: true,
              color: 'rgba(0, 0, 0, 0.1)'
            },
            ticks: {
              color: '#6B7280',\n              callback: (value) => this.formatValue(value)\n            }\n          }\n        }\n      };\n      \n      // 테마별 옵션 적용\n      this.applyTheme(baseOptions);\n      \n      // 사용자 옵션과 병합\n      return this.mergeOptions(baseOptions, this.options);\n    }\n  },\n  \n  watch: {\n    data: {\n      handler() {\n        this.updateChart();\n      },\n      deep: true\n    },\n    \n    options: {\n      handler() {\n        this.updateChart();\n      },\n      deep: true\n    }\n  },\n  \n  mounted() {\n    this.initChart();\n  },\n  \n  beforeUnmount() {\n    this.destroyChart();\n  },\n  \n  methods: {\n    initChart() {\n      if (!this.$refs.chartCanvas || !this.hasData) return;\n      \n      const ctx = this.$refs.chartCanvas.getContext('2d');\n      \n      this.chartInstance = new Chart(ctx, {\n        type: 'line',\n        data: this.data,\n        options: {\n          ...this.computedOptions,\n          onClick: this.handleChartClick,\n          onHover: this.handleChartHover\n        }\n      });\n      \n      this.$emit('chart-ready', this.chartInstance);\n    },\n    \n    updateChart() {\n      if (!this.chartInstance) {\n        this.$nextTick(() => {\n          this.initChart();\n        });\n        return;\n      }\n      \n      // 데이터 업데이트\n      this.chartInstance.data = this.data;\n      \n      // 옵션 업데이트\n      Object.assign(this.chartInstance.options, this.computedOptions);\n      \n      // 차트 다시 그리기\n      this.chartInstance.update(this.animated ? undefined : 'none');\n    },\n    \n    destroyChart() {\n      if (this.chartInstance && !this.isDestroyed) {\n        this.chartInstance.destroy();\n        this.chartInstance = null;\n        this.isDestroyed = true;\n      }\n    },\n    \n    handleChartClick(event, elements) {\n      if (elements.length > 0) {\n        const element = elements[0];\n        const datasetIndex = element.datasetIndex;\n        const index = element.index;\n        \n        const dataset = this.data.datasets[datasetIndex];\n        const value = dataset.data[index];\n        const label = this.data.labels[index];\n        \n        this.$emit('data-point-click', {\n          datasetIndex,\n          index,\n          dataset,\n          value,\n          label,\n          event\n        });\n      }\n    },\n    \n    handleChartHover(event, elements) {\n      const canvas = this.$refs.chartCanvas;\n      if (canvas) {\n        canvas.style.cursor = elements.length > 0 ? 'pointer' : 'default';\n      }\n    },\n    \n    applyTheme(options) {\n      switch (this.theme) {\n        case 'minimal':\n          options.scales.x.grid.display = false;\n          options.scales.y.grid.display = false;\n          options.scales.x.ticks.display = false;\n          options.scales.y.ticks.display = false;\n          break;\n          \n        case 'dark':\n          options.scales.x.grid.color = 'rgba(255, 255, 255, 0.1)';\n          options.scales.y.grid.color = 'rgba(255, 255, 255, 0.1)';\n          options.scales.x.ticks.color = '#D1D5DB';\n          options.scales.y.ticks.color = '#D1D5DB';\n          break;\n      }\n    },\n    \n    mergeOptions(baseOptions, userOptions) {\n      // 깊은 병합 구현\n      const merge = (target, source) => {\n        for (const key in source) {\n          if (source[key] && typeof source[key] === 'object' && !Array.isArray(source[key])) {\n            target[key] = target[key] || {};\n            merge(target[key], source[key]);\n          } else {\n            target[key] = source[key];\n          }\n        }\n        return target;\n      };\n      \n      return merge({ ...baseOptions }, userOptions);\n    },\n    \n    formatValue(value) {\n      if (typeof value === 'number') {\n        // 천 단위 구분자 추가\n        return new Intl.NumberFormat('ko-KR').format(value);\n      }\n      return value;\n    },\n    \n    // 공개 메서드\n    getChart() {\n      return this.chartInstance;\n    },\n    \n    downloadImage(filename = 'chart.png') {\n      if (!this.chartInstance) return;\n      \n      const canvas = this.chartInstance.canvas;\n      const link = document.createElement('a');\n      link.download = filename;\n      link.href = canvas.toDataURL('image/png');\n      link.click();\n    },\n    \n    resize() {\n      if (this.chartInstance) {\n        this.chartInstance.resize();\n      }\n    }\n  }\n};\n</script>\n\n<style scoped>\n.line-chart-container {\n  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);\n}\n\n/* 차트 반응형 */\n.chart-container {\n  position: relative;\n  min-height: 200px;\n}\n\n.chart-canvas {\n  display: block;\n  box-sizing: border-box;\n}\n\n/* 로딩 애니메이션 */\n@keyframes spin {\n  to {\n    transform: rotate(360deg);\n  }\n}\n\n/* 호버 효과 */\n.chart-container:hover {\n  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);\n}\n\n/* 다크 테마 지원 */\n@media (prefers-color-scheme: dark) {\n  .line-chart-container {\n    background-color: #1f2937;\n    color: #f9fafb;\n  }\n  \n  .chart-title {\n    color: #f9fafb;\n  }\n  \n  .chart-subtitle {\n    color: #9ca3af;\n  }\n}\n\n/* 반응형 */\n@media (max-width: 640px) {\n  .chart-header {\n    flex-direction: column;\n    align-items: flex-start;\n    gap: 0.5rem;\n  }\n  \n  .chart-legend {\n    flex-direction: column;\n    gap: 0.5rem;\n  }\n}\n\n/* 접근성 */\n@media (prefers-reduced-motion: reduce) {\n  .animate-spin {\n    animation: none;\n  }\n}\n</style>