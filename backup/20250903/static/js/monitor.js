/*
 * 실시간 모니터링 페이지 JavaScript
 * WebSocket + Chart.js 실시간 업데이트
 * 
 * AIDEV-NOTE: 메모리 누수 방지, 에러 처리, 사용자 경험 최적화
 */

/**
 * 실시간 모니터링 관리 클래스
 */
class MonitoringManager {
    constructor(testId) {
        this.testId = testId;
        this.isMonitoring = false;
        this.wsManager = null;
        this.metricsManager = null;
        this.chartManager = null;
        
        // 차트 인스턴스
        this.charts = {
            tps: null,
            responseTime: null,
            activeUsers: null,
            errorRate: null
        };
        
        // 메트릭 데이터 저장
        this.metricsHistory = [];
        this.maxHistorySize = 300; // 5분간 데이터 (1초 간격)
        
        // UI 업데이트 주기
        this.uiUpdateInterval = 1000; // 1초
        this.uiUpdateTimer = null;
        
        // 로그 관리
        this.maxLogEntries = 100;
        
        console.log('MonitoringManager 초기화:', testId);
    }
    
    /**
     * 모니터링 시작
     */
    async startMonitoring() {
        if (this.isMonitoring) {
            console.log('이미 모니터링 중입니다.');
            return;
        }
        
        console.log('실시간 모니터링 시작:', this.testId);
        this.isMonitoring = true;
        
        try {
            // WebSocket 초기화
            await this.initializeWebSocket();
            
            // 차트 초기화
            this.initializeCharts();
            
            // WebSocket 구독
            this.subscribeToMetrics();
            this.subscribeToStatus();
            this.subscribeToLogs();
            
            // UI 업데이트 시작
            this.startUIUpdates();
            
            // 초기 데이터 로드
            await this.loadInitialData();
            
            this.showStatus('실시간 모니터링이 시작되었습니다.', 'success');
            
        } catch (error) {
            console.error('모니터링 시작 실패:', error);
            this.showStatus('모니터링 시작에 실패했습니다.', 'danger');
            this.isMonitoring = false;
        }
    }
    
    /**
     * 모니터링 중단
     */
    stopMonitoring() {
        console.log('실시간 모니터링 중단:', this.testId);
        this.isMonitoring = false;
        
        // WebSocket 구독 해제
        if (this.metricsManager) {
            this.metricsManager.unsubscribeFromTestMetrics(this.testId);
        }
        
        // UI 업데이트 타이머 정리
        if (this.uiUpdateTimer) {
            clearInterval(this.uiUpdateTimer);
            this.uiUpdateTimer = null;
        }
        
        this.showStatus('실시간 모니터링이 중단되었습니다.', 'warning');
    }
    
    /**
     * WebSocket 초기화
     */
    async initializeWebSocket() {
        const { wsManager, metricsManager } = window.initializeWebSocket();
        this.wsManager = wsManager;
        this.metricsManager = metricsManager;
        
        // 연결 대기 (최대 5초)
        let attempts = 0;
        while (!this.wsManager.isWebSocketConnected() && attempts < 50) {
            await this.sleep(100);
            attempts++;
        }
        
        if (!this.wsManager.isWebSocketConnected()) {
            throw new Error('WebSocket 연결 실패');
        }
    }
    
    /**
     * 차트 초기화
     */
    initializeCharts() {
        this.chartManager = window.initializeCharts();
        
        // 각 차트 생성
        this.charts.tps = this.chartManager.createTpsChart('tps-chart', this.testId);
        this.charts.responseTime = this.chartManager.createResponseTimeChart('response-time-chart', this.testId);
        this.charts.activeUsers = this.chartManager.createActiveUsersChart('active-users-chart', this.testId);
        this.charts.errorRate = this.chartManager.createErrorRateChart('error-rate-chart', this.testId);
        
        console.log('차트 초기화 완료');
    }
    
    /**
     * 메트릭 구독
     */
    subscribeToMetrics() {
        this.metricsManager.subscribeToTestMetrics(this.testId, (metrics) => {
            this.onMetricsReceived(metrics);
        });
    }
    
    /**
     * 상태 구독
     */
    subscribeToStatus() {
        this.metricsManager.subscribeToTestStatus(this.testId, (status) => {
            this.onStatusReceived(status);
        });
    }
    
    /**
     * 로그 구독
     */
    subscribeToLogs() {
        this.metricsManager.subscribeToTestLogs(this.testId, (log) => {
            this.onLogReceived(log);
        });
    }
    
    /**
     * 메트릭 수신 처리
     */
    onMetricsReceived(metrics) {
        if (!metrics || !this.isMonitoring) return;
        
        console.debug('메트릭 수신:', metrics);
        
        // 히스토리에 추가
        this.metricsHistory.push({
            ...metrics,
            timestamp: Date.now()
        });
        
        // 히스토리 크기 제한
        if (this.metricsHistory.length > this.maxHistorySize) {
            this.metricsHistory.shift();
        }
        
        // 차트 업데이트
        this.updateCharts(metrics);
        
        // 메트릭 카드 업데이트
        this.updateMetricCards(metrics);
    }
    
    /**
     * 상태 수신 처리
     */
    onStatusReceived(status) {
        if (!status || !this.isMonitoring) return;
        
        console.debug('상태 수신:', status);
        
        // 상태 업데이트
        this.updateTestStatus(status);
        
        // 테스트 완료 시 모니터링 중단
        if (status.status === 'COMPLETED' || status.status === 'FAILED' || status.status === 'CANCELLED') {
            setTimeout(() => {
                this.stopMonitoring();
            }, 5000); // 5초 후 중단
        }
    }
    
    /**
     * 로그 수신 처리
     */
    onLogReceived(log) {
        if (!log || !this.isMonitoring) return;
        
        console.debug('로그 수신:', log);
        this.addLogEntry(log);
    }
    
    /**
     * 차트 업데이트
     */
    updateCharts(metrics) {
        if (!this.chartManager) return;
        
        try {
            this.chartManager.updateAllCharts(metrics);
        } catch (error) {
            console.error('차트 업데이트 오류:', error);
        }
    }
    
    /**
     * 메트릭 카드 업데이트
     */
    updateMetricCards(metrics) {
        // TPS 카드
        this.updateMetricCard('tps-value', metrics.tps, 'TPS');
        
        // 응답시간 카드
        this.updateMetricCard('response-time-value', metrics.avgResponseTime, 'ms');
        
        // 활성 사용자 카드
        this.updateMetricCard('active-users-value', metrics.activeUsers, '명');
        
        // 에러율 카드
        this.updateMetricCard('error-rate-value', metrics.errorRate, '%');
        
        // 진행률 업데이트
        if (metrics.progress !== undefined) {
            this.updateProgress(metrics.progress);
        }
    }
    
    /**
     * 개별 메트릭 카드 업데이트
     */
    updateMetricCard(elementId, value, unit) {
        const element = document.getElementById(elementId);
        if (!element) return;
        
        const formattedValue = this.formatMetricValue(value);
        element.textContent = formattedValue + (unit ? ' ' + unit : '');
        
        // 애니메이션 효과
        element.classList.add('updated');
        setTimeout(() => {
            element.classList.remove('updated');
        }, 500);
    }
    
    /**
     * 진행률 업데이트
     */
    updateProgress(progress) {
        const progressBar = document.getElementById('test-progress-bar');
        const progressText = document.getElementById('test-progress-text');
        
        if (progressBar) {
            // Tailwind classes for width
            progressBar.className = 'h-full bg-primary-600';
            progressBar.setAttribute('style', `width: ${progress}%`);
        }
        
        if (progressText) {
            progressText.textContent = Math.round(progress) + '%';
        }
    }
    
    /**
     * 테스트 상태 업데이트
     */
    updateTestStatus(status) {
        const statusElement = document.getElementById('test-status');
        if (statusElement) {
            statusElement.className = `status-badge status-${status.status.toLowerCase()}`;
            statusElement.textContent = status.status;
        }
        
        const messageElement = document.getElementById('test-message');
        if (messageElement && status.message) {
            messageElement.textContent = status.message;
        }
    }
    
    /**
     * 로그 엔트리 추가
     */
    addLogEntry(log) {
        const logContainer = document.getElementById('log-container');
        if (!logContainer) return;
        
        const logEntry = document.createElement('div');
        logEntry.className = `log-entry log-${log.level.toLowerCase()}`;
        logEntry.innerHTML = `
            <span class="log-timestamp">[${this.formatTime(log.timestamp)}]</span>
            <span class="log-level">${log.level}</span>
            <span class="log-message">${this.escapeHtml(log.message)}</span>
        `;
        
        // 로그 컨테이너 앞쪽에 추가 (최신이 위에)
        logContainer.insertBefore(logEntry, logContainer.firstChild);
        
        // 로그 수 제한
        while (logContainer.children.length > this.maxLogEntries) {
            logContainer.removeChild(logContainer.lastChild);
        }
        
        // 자동 스크롤 (사용자가 스크롤을 위로 올리지 않았을 때만)
        if (logContainer.scrollTop === 0) {
            logContainer.scrollTop = 0;
        }
    }
    
    /**
     * UI 업데이트 시작
     */
    startUIUpdates() {
        this.uiUpdateTimer = setInterval(() => {
            if (!this.isMonitoring) return;
            
            // 현재 시간 업데이트
            this.updateCurrentTime();
            
            // 연결 상태 확인
            this.checkConnectionStatus();
            
        }, this.uiUpdateInterval);
    }
    
    /**
     * 초기 데이터 로드
     */
    async loadInitialData() {
        try {
            // 히스토리 데이터 로드
            await Promise.all([
                this.chartManager.initializeChartWithHistory('tps-chart', this.testId, 5),
                this.chartManager.initializeChartWithHistory('response-time-chart', this.testId, 5),
                this.chartManager.initializeChartWithHistory('active-users-chart', this.testId, 5),
                this.chartManager.initializeChartWithHistory('error-rate-chart', this.testId, 5)
            ]);
            
            // 최근 로그 로드
            await this.loadRecentLogs();
            
        } catch (error) {
            console.error('초기 데이터 로드 실패:', error);
        }
    }
    
    /**
     * 최근 로그 로드
     */
    async loadRecentLogs() {
        try {
            // context path 포함한 API URL 생성
            const contextPath = window.location.pathname.split('/')[1] || '';
            const apiUrl = contextPath ? `/${contextPath}/api/dashboard/tests/${this.testId}/logs?count=50` : `/api/dashboard/tests/${this.testId}/logs?count=50`;
            const response = await fetch(apiUrl);
            if (!response.ok) return;
            
            const result = await response.json();
            if (result.success && result.data) {
                result.data.reverse().forEach(log => {
                    this.addLogEntry(log);
                });
            }
            
        } catch (error) {
            console.error('로그 로드 실패:', error);
        }
    }
    
    /**
     * 현재 시간 업데이트
     */
    updateCurrentTime() {
        const timeElement = document.getElementById('current-time');
        if (timeElement) {
            timeElement.textContent = new Date().toLocaleTimeString();
        }
    }
    
    /**
     * 연결 상태 확인
     */
    checkConnectionStatus() {
        const connected = this.wsManager && this.wsManager.isWebSocketConnected();
        const statusElement = document.getElementById('connection-status');
        
        if (statusElement) {
            statusElement.className = connected ? 'status-connected' : 'status-disconnected';
            statusElement.textContent = connected ? '연결됨' : '연결 끊어짐';
        }
    }
    
    /**
     * 상태 메시지 표시
     */
    showStatus(message, type = 'info') {
        const statusContainer = document.getElementById('status-container');
        if (!statusContainer) return;
        
        const alert = document.createElement('div');
        alert.className = `alert-custom alert-${type}`;
        alert.textContent = message;
        
        statusContainer.appendChild(alert);
        
        setTimeout(() => {
            if (alert.parentElement) {
                alert.remove();
            }
        }, 5000);
    }
    
    /**
     * 메트릭 값 포맷
     */
    formatMetricValue(value) {
        if (value === null || value === undefined) return 'N/A';
        
        if (typeof value === 'number') {
            if (value >= 1000) {
                return (value / 1000).toFixed(1) + 'K';
            }
            return value.toFixed(1);
        }
        
        return value.toString();
    }
    
    /**
     * 시간 포맷
     */
    formatTime(timestamp) {
        const date = new Date(timestamp);
        return date.toLocaleTimeString();
    }
    
    /**
     * HTML 이스케이프
     */
    escapeHtml(text) {
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return text.replace(/[&<>"']/g, (m) => map[m]);
    }
    
    /**
     * Sleep 함수
     */
    sleep(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }
}

// 전역 모니터링 매니저
let monitoringManager = null;

/**
 * 모니터링 초기화
 */
function initializeMonitoring(testId) {
    if (monitoringManager) {
        monitoringManager.stopMonitoring();
    }
    
    monitoringManager = new MonitoringManager(testId);
    return monitoringManager;
}

/**
 * 모니터링 시작
 */
async function startMonitoring(testId) {
    const manager = initializeMonitoring(testId);
    await manager.startMonitoring();
    return manager;
}

/**
 * 모니터링 중단
 */
function stopMonitoring() {
    if (monitoringManager) {
        monitoringManager.stopMonitoring();
    }
}

/**
 * 테스트 중단
 */
async function stopTest(testId) {
    if (!confirm('테스트를 중단하시겠습니까?')) {
        return;
    }
    
    try {
        // context path 포함한 API URL 생성
        const contextPath = window.location.pathname.split('/')[1] || '';
        const apiUrl = contextPath ? `/${contextPath}/api/dashboard/tests/${testId}/stop` : `/api/dashboard/tests/${testId}/stop`;
        const response = await fetch(apiUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        const result = await response.json();
        
        if (result.success) {
            alert('테스트가 중단되었습니다.');
            // 모니터링도 중단
            stopMonitoring();
            // 결과 페이지로 리다이렉트
            setTimeout(() => {
                window.location.href = `/performance/dashboard/results/${testId}`;
            }, 2000);
        } else {
            alert('테스트 중단에 실패했습니다: ' + (result.message || '알 수 없는 오류'));
        }
        
    } catch (error) {
        console.error('테스트 중단 오류:', error);
        alert('테스트 중단 중 오류가 발생했습니다.');
    }
}

// 페이지 언로드 시 정리
window.addEventListener('beforeunload', () => {
    stopMonitoring();
});

// 전역 함수 노출
window.initializeMonitoring = initializeMonitoring;
window.startMonitoring = startMonitoring;
window.stopMonitoring = stopMonitoring;
window.stopTest = stopTest;