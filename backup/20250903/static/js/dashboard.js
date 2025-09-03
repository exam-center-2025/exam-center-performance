/*
 * 대시보드 메인 JavaScript
 * 페이지 로드, 데이터 갱신, 사용자 상호작용 처리
 * 
 * AIDEV-NOTE: 모듈화된 구조, 에러 처리, 사용자 피드백 포함
 */

/**
 * Context Path 가져오기
 */
function getContextPath() {
    const path = window.location.pathname;
    const parts = path.split('/');
    if (parts.length > 1 && parts[1] !== '') {
        // URL이 /performance/... 형태인 경우
        return '/' + parts[1];
    }
    return '';
}

/**
 * 대시보드 관리 클래스
 */
class DashboardManager {
    constructor() {
        this.refreshInterval = 30000; // 30초마다 데이터 새로고침
        this.refreshTimer = null;
        this.isLoading = false;
        
        // API 엔드포인트
        const contextPath = getContextPath();
        this.apiEndpoints = {
            plans: contextPath + '/api/dashboard/plans',
            activeTests: contextPath + '/api/dashboard/tests/active',
            recentResults: contextPath + '/api/dashboard/tests/recent',
            startTest: contextPath + '/api/dashboard/tests/start',
            stopTest: contextPath + '/api/dashboard/tests/{testId}/stop',
            testMetrics: contextPath + '/api/dashboard/tests/{testId}/metrics'
        };
    }
    
    /**
     * 대시보드 초기화
     */
    async initialize() {
        console.log('대시보드 초기화 시작');
        
        try {
            // 초기 데이터 로드
            await this.loadInitialData();
            
            // 이벤트 리스너 설정
            this.setupEventListeners();
            
            // 자동 새로고침 시작
            this.startAutoRefresh();
            
            console.log('대시보드 초기화 완료');
            
        } catch (error) {
            console.error('대시보드 초기화 실패:', error);
            this.showError('대시보드 초기화에 실패했습니다.');
        }
    }
    
    /**
     * 초기 데이터 로드
     */
    async loadInitialData() {
        this.showLoading(true);
        
        try {
            // 병렬로 데이터 로드
            const [plansResult, activeTestsResult, recentResultsResult] = await Promise.allSettled([
                this.fetchData(this.apiEndpoints.plans),
                this.fetchData(this.apiEndpoints.activeTests),
                this.fetchData(this.apiEndpoints.recentResults)
            ]);
            
            // 시험 계획 목록 업데이트
            if (plansResult.status === 'fulfilled' && plansResult.value.success) {
                this.updateExamPlans(plansResult.value.data);
            }
            
            // 실행 중인 테스트 업데이트
            if (activeTestsResult.status === 'fulfilled' && activeTestsResult.value.success) {
                this.updateActiveTests(activeTestsResult.value.data);
            }
            
            // 최근 결과 업데이트
            if (recentResultsResult.status === 'fulfilled' && recentResultsResult.value.success) {
                this.updateRecentResults(recentResultsResult.value.data);
            }
            
        } catch (error) {
            console.error('초기 데이터 로드 실패:', error);
        } finally {
            this.showLoading(false);
        }
    }
    
    /**
     * 데이터 새로고침
     */
    async refreshData() {
        if (this.isLoading) {
            console.log('이미 로딩 중입니다.');
            return;
        }
        
        try {
            // 실행 중인 테스트와 최근 결과만 새로고침
            const [activeTestsResult, recentResultsResult] = await Promise.allSettled([
                this.fetchData(this.apiEndpoints.activeTests),
                this.fetchData(this.apiEndpoints.recentResults)
            ]);
            
            if (activeTestsResult.status === 'fulfilled' && activeTestsResult.value.success) {
                this.updateActiveTests(activeTestsResult.value.data);
            }
            
            if (recentResultsResult.status === 'fulfilled' && recentResultsResult.value.success) {
                this.updateRecentResults(recentResultsResult.value.data);
            }
            
            this.updateLastRefreshTime();
            
        } catch (error) {
            console.error('데이터 새로고침 실패:', error);
        }
    }
    
    /**
     * API 데이터 fetch
     */
    async fetchData(url) {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        return await response.json();
    }
    
    /**
     * 시험 계획 목록 업데이트
     */
    updateExamPlans(plans) {
        const container = document.getElementById('exam-plans-table');
        if (!container) return;
        
        if (plans.length === 0) {
            container.innerHTML = this.getEmptyState('등록된 시험 계획이 없습니다.');
            return;
        }
        
        let html = `
            <table class="table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>계획명</th>
                        <th>상태</th>
                        <th>시작일</th>
                        <th>액션</th>
                    </tr>
                </thead>
                <tbody>
        `;
        
        plans.forEach(plan => {
            html += `
                <tr>
                    <td>${plan.planId}</td>
                    <td>${this.escapeHtml(plan.name)}</td>
                    <td><span class="badge badge-primary">${plan.status}</span></td>
                    <td>${this.formatDate(plan.startTime)}</td>
                    <td>
                        <a href="${getContextPath()}/dashboard/configure?planId=${plan.planId}" class="btn btn-primary btn-sm">
                            <i class="fas fa-play"></i> 테스트
                        </a>
                    </td>
                </tr>
            `;
        });
        
        html += '</tbody></table>';
        container.innerHTML = html;
    }
    
    /**
     * 실행 중인 테스트 업데이트
     */
    updateActiveTests(tests) {
        const container = document.getElementById('active-tests-table');
        if (!container) return;
        
        if (tests.length === 0) {
            container.innerHTML = this.getEmptyState('현재 실행 중인 테스트가 없습니다.');
            return;
        }
        
        let html = `
            <table class="table">
                <thead>
                    <tr>
                        <th>테스트 ID</th>
                        <th>상태</th>
                        <th>진행률</th>
                        <th>경과시간</th>
                        <th>액션</th>
                    </tr>
                </thead>
                <tbody>
        `;
        
        tests.forEach(test => {
            const progress = this.calculateProgress(test);
            const elapsed = this.calculateElapsedTime(test.startTime);
            
            html += `
                <tr>
                    <td class="font-english">${test.testId}</td>
                    <td><span class="badge badge-primary">${test.status}</span></td>
                    <td>
                        <div class="progress-bar-custom">
                            <div class="progress-bar-fill" style="width: ${progress}%"></div>
                        </div>
                        <div class="progress-text">${progress}%</div>
                    </td>
                    <td>${elapsed}</td>
                    <td>
                        <a href="${getContextPath()}/dashboard/monitor/${test.testId}" class="btn btn-primary btn-sm">
                            <i class="fas fa-chart-line"></i> 모니터링
                        </a>
                        <button onclick="stopTest('${test.testId}')" class="btn btn-danger btn-sm">
                            <i class="fas fa-stop"></i> 중단
                        </button>
                    </td>
                </tr>
            `;
        });
        
        html += '</tbody></table>';
        container.innerHTML = html;
    }
    
    /**
     * 최근 테스트 결과 업데이트
     */
    updateRecentResults(results) {
        const container = document.getElementById('recent-results-table');
        if (!container) return;
        
        if (results.length === 0) {
            container.innerHTML = this.getEmptyState('최근 테스트 결과가 없습니다.');
            return;
        }
        
        let html = `
            <table class="table">
                <thead>
                    <tr>
                        <th>테스트 ID</th>
                        <th>상태</th>
                        <th>성공률</th>
                        <th>평균 응답시간</th>
                        <th>완료시간</th>
                        <th>액션</th>
                    </tr>
                </thead>
                <tbody>
        `;
        
        results.slice(0, 10).forEach(result => { // 최근 10개만 표시
            html += `
                <tr>
                    <td class="font-english">${result.testId}</td>
                    <td><span class="badge badge-${result.status === 'COMPLETED' ? 'success' : 'primary'}">${result.status}</span></td>
                    <td>${result.successRate ? result.successRate.toFixed(2) + '%' : 'N/A'}</td>
                    <td>${result.avgResponseTime ? result.avgResponseTime.toFixed(0) + 'ms' : 'N/A'}</td>
                    <td>${this.formatDate(result.endTime)}</td>
                    <td>
                        <a href="/performance/dashboard/results/${result.testId}" class="btn btn-secondary btn-sm">
                            <i class="fas fa-chart-bar"></i> 결과보기
                        </a>
                    </td>
                </tr>
            `;
        });
        
        html += '</tbody></table>';
        container.innerHTML = html;
    }
    
    /**
     * 테스트 중단
     */
    async stopTest(testId) {
        if (!confirm('테스트를 중단하시겠습니까?')) {
            return;
        }
        
        try {
            this.showLoading(true);
            
            const url = this.apiEndpoints.stopTest.replace('{testId}', testId);
            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });
            
            const result = await response.json();
            
            if (result.success) {
                this.showSuccess('테스트가 중단되었습니다.');
                await this.refreshData(); // 데이터 새로고침
            } else {
                this.showError(result.message || '테스트 중단에 실패했습니다.');
            }
            
        } catch (error) {
            console.error('테스트 중단 오류:', error);
            this.showError('테스트 중단 중 오류가 발생했습니다.');
        } finally {
            this.showLoading(false);
        }
    }
    
    /**
     * 이벤트 리스너 설정
     */
    setupEventListeners() {
        // 새로고침 버튼
        const refreshBtn = document.getElementById('refresh-btn');
        if (refreshBtn) {
            refreshBtn.addEventListener('click', () => {
                this.refreshData();
            });
        }
        
        // 자동 새로고침 토글
        const autoRefreshToggle = document.getElementById('auto-refresh-toggle');
        if (autoRefreshToggle) {
            autoRefreshToggle.addEventListener('change', (e) => {
                if (e.target.checked) {
                    this.startAutoRefresh();
                } else {
                    this.stopAutoRefresh();
                }
            });
        }
    }
    
    /**
     * 자동 새로고침 시작
     */
    startAutoRefresh() {
        if (this.refreshTimer) {
            clearInterval(this.refreshTimer);
        }
        
        this.refreshTimer = setInterval(() => {
            this.refreshData();
        }, this.refreshInterval);
        
        console.log('자동 새로고침 시작:', this.refreshInterval + 'ms');
    }
    
    /**
     * 자동 새로고침 중단
     */
    stopAutoRefresh() {
        if (this.refreshTimer) {
            clearInterval(this.refreshTimer);
            this.refreshTimer = null;
            console.log('자동 새로고침 중단');
        }
    }
    
    /**
     * 로딩 상태 표시
     */
    showLoading(show) {
        this.isLoading = show;
        
        const loadingElement = document.getElementById('loading-indicator');
        if (loadingElement) {
            // Use Tailwind classes instead of inline styles
            if (show) {
                loadingElement.classList.remove('hidden');
            } else {
                loadingElement.classList.add('hidden');
            }
        }
        
        // 새로고침 버튼 비활성화
        const refreshBtn = document.getElementById('refresh-btn');
        if (refreshBtn) {
            refreshBtn.disabled = show;
        }
    }
    
    /**
     * 성공 메시지 표시
     */
    showSuccess(message) {
        this.showAlert(message, 'success');
    }
    
    /**
     * 에러 메시지 표시
     */
    showError(message) {
        this.showAlert(message, 'danger');
    }
    
    /**
     * 알림 메시지 표시
     */
    showAlert(message, type = 'info') {
        const alertContainer = document.getElementById('alert-container') || document.body;
        
        const alert = document.createElement('div');
        alert.className = `alert-custom alert-${type}`;
        alert.innerHTML = `
            <span>${this.escapeHtml(message)}</span>
            <button type="button" class="close" onclick="this.parentElement.remove()">
                <span>&times;</span>
            </button>
        `;
        
        alertContainer.appendChild(alert);
        
        // 5초 후 자동 제거
        setTimeout(() => {
            if (alert.parentElement) {
                alert.remove();
            }
        }, 5000);
    }
    
    /**
     * 마지막 새로고침 시간 업데이트
     */
    updateLastRefreshTime() {
        const element = document.getElementById('last-refresh-time');
        if (element) {
            element.textContent = new Date().toLocaleTimeString();
        }
    }
    
    /**
     * 빈 상태 HTML 생성
     */
    getEmptyState(message) {
        return `
            <div class="p-6 text-center bg-gray-50">
                <div class="text-2xl mb-2 text-gray-400">📊</div>
                <div class="text-sm font-medium text-gray-700">데이터가 없습니다</div>
                <div class="text-xs text-gray-500 mt-1">${this.escapeHtml(message)}</div>
            </div>
        `;
    }
    
    /**
     * 진행률 계산
     */
    calculateProgress(test) {
        if (!test.startTime || !test.testDurationSeconds) {
            return 0;
        }
        
        const startTime = new Date(test.startTime);
        const now = new Date();
        const elapsed = (now - startTime) / 1000; // 초
        const progress = Math.min((elapsed / test.testDurationSeconds) * 100, 100);
        
        return Math.round(progress);
    }
    
    /**
     * 경과 시간 계산
     */
    calculateElapsedTime(startTime) {
        if (!startTime) return 'N/A';
        
        const start = new Date(startTime);
        const now = new Date();
        const elapsed = Math.floor((now - start) / 1000); // 초
        
        const hours = Math.floor(elapsed / 3600);
        const minutes = Math.floor((elapsed % 3600) / 60);
        const seconds = elapsed % 60;
        
        if (hours > 0) {
            return `${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        } else {
            return `${minutes}:${seconds.toString().padStart(2, '0')}`;
        }
    }
    
    /**
     * 날짜 포맷
     */
    formatDate(dateString) {
        if (!dateString) return 'N/A';
        
        const date = new Date(dateString);
        return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
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
}

// 전역 대시보드 매니저 인스턴스
let dashboardManager = null;

/**
 * 대시보드 초기화 함수
 */
async function initializeDashboard() {
    if (!dashboardManager) {
        dashboardManager = new DashboardManager();
    }
    
    await dashboardManager.initialize();
    return dashboardManager;
}

/**
 * 테스트 중단 (전역 함수)
 */
async function stopTest(testId) {
    if (dashboardManager) {
        await dashboardManager.stopTest(testId);
    }
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', () => {
    initializeDashboard().catch(error => {
        console.error('대시보드 초기화 오류:', error);
    });
});

// 페이지 언로드 시 정리
window.addEventListener('beforeunload', () => {
    if (dashboardManager) {
        dashboardManager.stopAutoRefresh();
    }
});

// 전역 함수 노출
window.initializeDashboard = initializeDashboard;
window.stopTest = stopTest;
window.dashboardManager = null; // 초기화 후 설정됨