/*
 * Chart.js 기반 실시간 차트 관리
 * TPS, 응답시간, 활성사용자, 에러율 차트
 * 
 * AIDEV-NOTE: 실시간 업데이트, 시계열 데이터 처리, 메모리 관리
 */

/**
 * 차트 관리 클래스
 */
class ChartManager {
    constructor() {
        this.charts = new Map();
        this.startTime = Date.now(); // 차트 시작 시간 기록
        this.maxDataPoints = 60; // 최대 60개 데이터 포인트 유지
        this.defaultOptions = {
            responsive: true,
            maintainAspectRatio: false,
            animation: {
                duration: 0 // 실시간 업데이트를 위해 애니메이션 비활성화
            },
            interaction: {
                mode: 'index',
                intersect: false,
            },
            elements: {
                bar: {
                    borderRadius: 0, // 막대 차트 모서리 각지게
                },
                point: {
                    radius: 3,
                    borderWidth: 1
                },
                line: {
                    borderWidth: 2
                }
            },
            scales: {
                x: {
                    type: 'linear',
                    grid: {
                        display: true,
                        color: '#E5E7EB',
                        lineWidth: 1
                    },
                    border: {
                        display: true,
                        color: '#E5E7EB'
                    },
                    title: {
                        display: true,
                        text: '시간 (초)',
                        font: {
                            size: 11,
                            weight: 500
                        },
                        color: '#6B7280'
                    },
                    ticks: {
                        font: {
                            size: 10
                        },
                        color: '#6B7280',
                        callback: function(value) {
                            // 초를 분:초 형식으로 변환
                            const minutes = Math.floor(value / 60);
                            const seconds = value % 60;
                            return `${minutes}:${seconds.toString().padStart(2, '0')}`;
                        }
                    }
                },
                y: {
                    beginAtZero: true,
                    grid: {
                        display: true,
                        color: '#E5E7EB',
                        lineWidth: 1
                    },
                    border: {
                        display: true,
                        color: '#E5E7EB'
                    },
                    title: {
                        display: true,
                        text: '값',
                        font: {
                            size: 11,
                            weight: 500
                        },
                        color: '#6B7280'
                    },
                    ticks: {
                        font: {
                            size: 10
                        },
                        color: '#6B7280'
                    }
                }
            },
            plugins: {
                legend: {
                    display: true,
                    position: 'top',
                    labels: {
                        boxWidth: 12,
                        boxHeight: 12,
                        padding: 10,
                        font: {
                            size: 11,
                            weight: 500
                        },
                        color: '#6B7280',
                        usePointStyle: false,
                        generateLabels: function(chart) {
                            const original = Chart.defaults.plugins.legend.labels.generateLabels;
                            const labels = original.call(this, chart);
                            labels.forEach(label => {
                                label.borderRadius = 0;
                            });
                            return labels;
                        }
                    }
                },
                tooltip: {
                    mode: 'index',
                    intersect: false,
                    backgroundColor: '#FFFFFF',
                    titleColor: '#1F2937',
                    bodyColor: '#374151',
                    borderColor: '#E5E7EB',
                    borderWidth: 1,
                    cornerRadius: 0, // 툴팁도 각지게
                    padding: 8,
                    titleFont: {
                        size: 11,
                        weight: 600
                    },
                    bodyFont: {
                        size: 10
                    }
                }
            }
        };
        
        // 최대 데이터 포인트 수 (메모리 관리)
        this.maxDataPoints = 120; // 2분간 데이터 (1초 간격)
    }
    
    /**
     * TPS 차트 생성
     */
    createTpsChart(canvasId, testId = null) {
        // 기존 차트가 있으면 먼저 제거
        if (this.charts.has(canvasId)) {
            this.destroyChart(canvasId);
        }
        
        const ctx = document.getElementById(canvasId);
        if (!ctx) {
            console.error('TPS 차트 캔버스를 찾을 수 없습니다:', canvasId);
            return null;
        }
        
        const config = {
            type: 'line',
            data: {
                datasets: [{
                    label: 'TPS (초당 트랜잭션)',
                    data: [],
                    borderColor: '#4F46E5',
                    backgroundColor: 'rgba(79, 70, 229, 0.05)',
                    borderWidth: 2,
                    fill: true,
                    tension: 0, // 직선으로 표시
                    pointRadius: 2,
                    pointBorderColor: '#4F46E5',
                    pointBackgroundColor: '#FFFFFF',
                    pointBorderWidth: 1
                }]
            },
            options: {
                ...this.defaultOptions,
                scales: {
                    ...this.defaultOptions.scales,
                    y: {
                        ...this.defaultOptions.scales.y,
                        title: {
                            display: true,
                            text: 'TPS'
                        }
                    }
                },
                plugins: {
                    ...this.defaultOptions.plugins,
                    title: {
                        display: true,
                        text: 'TPS (초당 트랜잭션 수)'
                    }
                }
            }
        };
        
        const chart = new Chart(ctx, config);
        this.charts.set(canvasId, {
            chart,
            type: 'tps',
            testId
        });
        
        return chart;
    }
    
    /**
     * 응답시간 차트 생성
     */
    createResponseTimeChart(canvasId, testId = null) {
        // 기존 차트가 있으면 먼저 제거
        if (this.charts.has(canvasId)) {
            this.destroyChart(canvasId);
        }
        
        const ctx = document.getElementById(canvasId);
        if (!ctx) {
            console.error('응답시간 차트 캔버스를 찾을 수 없습니다:', canvasId);
            return null;
        }
        
        const config = {
            type: 'line',
            data: {
                datasets: [
                    {
                        label: '평균 응답시간',
                        data: [],
                        borderColor: '#10B981',
                        backgroundColor: 'rgba(16, 185, 129, 0.05)',
                        borderWidth: 2,
                        fill: true,
                        tension: 0, // 직선으로 표시
                        pointRadius: 2,
                        pointBorderColor: '#10B981',
                        pointBackgroundColor: '#FFFFFF',
                        pointBorderWidth: 1
                    },
                    {
                        label: 'P95 응답시간',
                        data: [],
                        borderColor: '#F59E0B',
                        backgroundColor: 'rgba(245, 158, 11, 0.05)',
                        borderWidth: 2,
                        fill: true,
                        tension: 0, // 직선으로 표시
                        pointRadius: 2,
                        pointBorderColor: '#F59E0B',
                        pointBackgroundColor: '#FFFFFF',
                        pointBorderWidth: 1
                    }
                ]
            },
            options: {
                ...this.defaultOptions,
                scales: {
                    ...this.defaultOptions.scales,
                    y: {
                        ...this.defaultOptions.scales.y,
                        title: {
                            display: true,
                            text: '응답시간 (ms)'
                        }
                    }
                },
                plugins: {
                    ...this.defaultOptions.plugins,
                    title: {
                        display: true,
                        text: '응답시간 분포'
                    }
                }
            }
        };
        
        const chart = new Chart(ctx, config);
        this.charts.set(canvasId, {
            chart,
            type: 'responseTime',
            testId
        });
        
        return chart;
    }
    
    /**
     * 활성 사용자 차트 생성
     */
    createActiveUsersChart(canvasId, testId = null) {
        // 기존 차트가 있으면 먼저 제거
        if (this.charts.has(canvasId)) {
            this.destroyChart(canvasId);
        }
        
        const ctx = document.getElementById(canvasId);
        if (!ctx) {
            console.error('활성 사용자 차트 캔버스를 찾을 수 없습니다:', canvasId);
            return null;
        }
        
        const config = {
            type: 'line',
            data: {
                datasets: [{
                    label: '활성 사용자 수',
                    data: [],
                    borderColor: '#8B5CF6',
                    backgroundColor: 'rgba(139, 92, 246, 0.05)',
                    borderWidth: 2,
                    fill: true,
                    tension: 0, // 직선으로 표시
                    pointRadius: 2,
                    pointBorderColor: '#8B5CF6',
                    pointBackgroundColor: '#FFFFFF',
                    pointBorderWidth: 1
                }]
            },
            options: {
                ...this.defaultOptions,
                scales: {
                    ...this.defaultOptions.scales,
                    y: {
                        ...this.defaultOptions.scales.y,
                        title: {
                            display: true,
                            text: '사용자 수'
                        }
                    }
                },
                plugins: {
                    ...this.defaultOptions.plugins,
                    title: {
                        display: true,
                        text: '활성 사용자 수'
                    }
                }
            }
        };
        
        const chart = new Chart(ctx, config);
        this.charts.set(canvasId, {
            chart,
            type: 'activeUsers',
            testId
        });
        
        return chart;
    }
    
    /**
     * 에러율 차트 생성
     */
    createErrorRateChart(canvasId, testId = null) {
        // 기존 차트가 있으면 먼저 제거
        if (this.charts.has(canvasId)) {
            this.destroyChart(canvasId);
        }
        
        const ctx = document.getElementById(canvasId);
        if (!ctx) {
            console.error('에러율 차트 캔버스를 찾을 수 없습니다:', canvasId);
            return null;
        }
        
        const config = {
            type: 'line',
            data: {
                datasets: [{
                    label: '에러율 (%)',
                    data: [],
                    borderColor: '#EF4444',
                    backgroundColor: 'rgba(239, 68, 68, 0.05)',
                    borderWidth: 2,
                    fill: true,
                    tension: 0, // 직선으로 표시
                    pointRadius: 2,
                    pointBorderColor: '#EF4444',
                    pointBackgroundColor: '#FFFFFF',
                    pointBorderWidth: 1
                }]
            },
            options: {
                ...this.defaultOptions,
                scales: {
                    ...this.defaultOptions.scales,
                    y: {
                        ...this.defaultOptions.scales.y,
                        max: 100,
                        title: {
                            display: true,
                            text: '에러율 (%)'
                        }
                    }
                },
                plugins: {
                    ...this.defaultOptions.plugins,
                    title: {
                        display: true,
                        text: '에러율'
                    }
                }
            }
        };
        
        const chart = new Chart(ctx, config);
        this.charts.set(canvasId, {
            chart,
            type: 'errorRate',
            testId
        });
        
        return chart;
    }
    
    /**
     * 차트 데이터 업데이트
     */
    updateChart(canvasId, metrics) {
        const chartInfo = this.charts.get(canvasId);
        if (!chartInfo) {
            console.warn('차트를 찾을 수 없습니다:', canvasId);
            return;
        }
        
        const { chart, type } = chartInfo;
        // timestamp를 경과 시간(초)으로 변환
        const elapsedSeconds = Math.floor((Date.now() - this.startTime) / 1000);
        
        switch (type) {
            case 'tps':
                this.addDataPoint(chart, 0, elapsedSeconds, metrics.tps || 0);
                break;
                
            case 'responseTime':
                this.addDataPoint(chart, 0, elapsedSeconds, metrics.avgResponseTime || 0);
                if (chart.data.datasets.length > 1) {
                    this.addDataPoint(chart, 1, elapsedSeconds, metrics.p95ResponseTime || 0);
                }
                break;
                
            case 'activeUsers':
                this.addDataPoint(chart, 0, elapsedSeconds, metrics.activeUsers || 0);
                break;
                
            case 'errorRate':
                this.addDataPoint(chart, 0, elapsedSeconds, metrics.errorRate || 0);
                break;
        }
        
        chart.update('none'); // 애니메이션 없이 업데이트
    }
    
    /**
     * 차트에 데이터 포인트 추가
     */
    addDataPoint(chart, datasetIndex, x, value) {
        const dataset = chart.data.datasets[datasetIndex];
        if (!dataset) return;
        
        // 새 데이터 포인트 추가 (x는 경과 시간(초), y는 값)
        dataset.data.push({
            x: x,
            y: value
        });
        
        // 최대 데이터 포인트 수 제한
        if (dataset.data.length > this.maxDataPoints) {
            dataset.data.shift(); // 가장 오래된 데이터 제거
        }
    }
    
    /**
     * 차트 초기화 (히스토리 데이터 로드)
     */
    async initializeChartWithHistory(canvasId, testId, minutes = 5) {
        const chartInfo = this.charts.get(canvasId);
        if (!chartInfo) return;
        
        const { type } = chartInfo;
        
        try {
            // context path 포함한 API URL 생성
            const contextPath = window.location.pathname.split('/')[1] || '';
            const apiUrl = contextPath ? `/${contextPath}/api/dashboard/tests/${testId}/timeline?type=${type}&minutes=${minutes}` : `/api/dashboard/tests/${testId}/timeline?type=${type}&minutes=${minutes}`;
            const response = await fetch(apiUrl);
            if (!response.ok) {
                console.warn('히스토리 데이터 로드 실패:', response.status);
                return;
            }
            
            const timelineData = await response.json();
            if (timelineData.success && timelineData.data) {
                this.loadHistoryData(canvasId, timelineData.data);
            }
            
        } catch (error) {
            console.error('히스토리 데이터 로드 오류:', error);
        }
    }
    
    /**
     * 히스토리 데이터 로드
     */
    loadHistoryData(canvasId, historyData) {
        const chartInfo = this.charts.get(canvasId);
        if (!chartInfo || !historyData || historyData.length === 0) return;
        
        const { chart } = chartInfo;
        
        // 데이터셋 초기화
        chart.data.datasets.forEach(dataset => {
            dataset.data = [];
        });
        
        // 히스토리 데이터 추가
        historyData.forEach(dataPoint => {
            const timestamp = new Date(dataPoint.timestamp);
            chart.data.datasets[0].data.push({
                x: timestamp,
                y: dataPoint.value || 0
            });
        });
        
        chart.update();
    }
    
    /**
     * 모든 차트 업데이트
     */
    updateAllCharts(metrics) {
        this.charts.forEach((chartInfo, canvasId) => {
            if (chartInfo.testId === metrics.testId || !chartInfo.testId) {
                this.updateChart(canvasId, metrics);
            }
        });
    }
    
    /**
     * 차트 제거
     */
    destroyChart(canvasId) {
        const chartInfo = this.charts.get(canvasId);
        if (chartInfo) {
            chartInfo.chart.destroy();
            this.charts.delete(canvasId);
        }
    }
    
    /**
     * 모든 차트 제거
     */
    destroyAllCharts() {
        this.charts.forEach((chartInfo, canvasId) => {
            chartInfo.chart.destroy();
        });
        this.charts.clear();
    }
    
    /**
     * 차트 리사이즈 (반응형)
     */
    resizeCharts() {
        this.charts.forEach((chartInfo) => {
            chartInfo.chart.resize();
        });
    }
}

/**
 * 도넛 차트 생성 (성공률 등)
 */
function createDonutChart(canvasId, data, options = {}) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return null;
    
    const config = {
        type: 'doughnut',
        data: data,
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom'
                },
                ...options.plugins
            },
            ...options
        }
    };
    
    return new Chart(ctx, config);
}

/**
 * 바 차트 생성 (응답시간 분포 등)
 */
function createBarChart(canvasId, data, options = {}) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return null;
    
    const config = {
        type: 'bar',
        data: data,
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true
                }
            },
            plugins: {
                legend: {
                    display: false
                },
                ...options.plugins
            },
            ...options
        }
    };
    
    return new Chart(ctx, config);
}

// 전역 차트 매니저 인스턴스
let chartManager = null;

/**
 * 차트 매니저 초기화
 */
function initializeCharts() {
    if (!chartManager) {
        chartManager = new ChartManager();
    }
    
    return chartManager;
}

// 윈도우 리사이즈 이벤트 처리
window.addEventListener('resize', () => {
    if (chartManager) {
        chartManager.resizeCharts();
    }
});

// 전역 함수로 노출
window.initializeCharts = initializeCharts;
window.createDonutChart = createDonutChart;
window.createBarChart = createBarChart;
window.chartManager = null; // 초기화 후 설정됨