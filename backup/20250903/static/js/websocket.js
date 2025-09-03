/*
 * WebSocket 연결 및 실시간 통신 관리
 * SockJS + STOMP 프로토콜 사용
 * 
 * AIDEV-NOTE: 자동 재연결, 오류 처리, 구독 관리 포함
 */

class WebSocketManager {
    constructor() {
        this.stompClient = null;
        this.subscriptions = new Map();
        this.isConnected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectInterval = 3000; // 3초
        this.pingInterval = 30000; // 30초
        this.pingTimer = null;
        
        // 이벤트 콜백
        this.onConnected = null;
        this.onDisconnected = null;
        this.onError = null;
        
        // 디버그 모드
        this.debug = window.location.search.includes('debug=true');
    }
    
    /**
     * WebSocket 연결 초기화
     */
    connect() {
        if (this.isConnected) {
            this.log('이미 연결되어 있습니다.');
            return;
        }
        
        this.log('WebSocket 연결 시도...');
        
        // SockJS 소켓 생성 - context path 포함
        const contextPath = window.location.pathname.split('/')[1] || '';
        const wsUrl = contextPath ? `/${contextPath}/ws` : '/ws';
        const socket = new SockJS(wsUrl);
        this.stompClient = Stomp.over(socket);
        
        // 디버그 모드가 아니면 콘솔 로그 비활성화
        if (!this.debug) {
            this.stompClient.debug = function() {};
        }
        
        // 연결 설정
        const connectHeaders = {};
        
        this.stompClient.connect(
            connectHeaders,
            (frame) => this.onConnectionSuccess(frame),
            (error) => this.onConnectionError(error)
        );
    }
    
    /**
     * WebSocket 연결 해제
     */
    disconnect() {
        if (this.stompClient && this.stompClient.connected) {
            // 모든 구독 해제
            this.subscriptions.forEach((subscription) => {
                subscription.unsubscribe();
            });
            this.subscriptions.clear();
            
            // Ping 타이머 정리
            if (this.pingTimer) {
                clearInterval(this.pingTimer);
                this.pingTimer = null;
            }
            
            this.stompClient.disconnect(() => {
                this.log('WebSocket 연결이 해제되었습니다.');
            });
        }
        
        this.isConnected = false;
        this.reconnectAttempts = 0;
        
        if (this.onDisconnected) {
            this.onDisconnected();
        }
    }
    
    /**
     * 연결 성공 처리
     */
    onConnectionSuccess(frame) {
        this.log('WebSocket 연결 성공:', frame);
        this.isConnected = true;
        this.reconnectAttempts = 0;
        
        // Ping 시작
        this.startPing();
        
        if (this.onConnected) {
            this.onConnected();
        }
    }
    
    /**
     * 연결 오류 처리
     */
    onConnectionError(error) {
        this.log('WebSocket 연결 실패:', error);
        this.isConnected = false;
        
        if (this.onError) {
            this.onError(error);
        }
        
        // 자동 재연결 시도
        this.attemptReconnect();
    }
    
    /**
     * 자동 재연결 시도
     */
    attemptReconnect() {
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            this.log('최대 재연결 시도 횟수에 도달했습니다.');
            return;
        }
        
        this.reconnectAttempts++;
        this.log(`재연결 시도 ${this.reconnectAttempts}/${this.maxReconnectAttempts} (${this.reconnectInterval}ms 후)`);
        
        setTimeout(() => {
            this.connect();
        }, this.reconnectInterval);
    }
    
    /**
     * 토픽 구독
     */
    subscribe(destination, callback, headers = {}) {
        if (!this.isConnected || !this.stompClient) {
            this.log('WebSocket이 연결되지 않았습니다:', destination);
            return null;
        }
        
        try {
            const subscription = this.stompClient.subscribe(destination, (message) => {
                try {
                    const data = JSON.parse(message.body);
                    callback(data);
                } catch (e) {
                    this.log('메시지 파싱 오류:', e, message.body);
                    callback(null);
                }
            }, headers);
            
            this.subscriptions.set(destination, subscription);
            this.log('구독 시작:', destination);
            
            return subscription;
        } catch (error) {
            this.log('구독 실패:', destination, error);
            return null;
        }
    }
    
    /**
     * 구독 해제
     */
    unsubscribe(destination) {
        const subscription = this.subscriptions.get(destination);
        if (subscription) {
            subscription.unsubscribe();
            this.subscriptions.delete(destination);
            this.log('구독 해제:', destination);
            return true;
        }
        return false;
    }
    
    /**
     * 메시지 전송
     */
    send(destination, message = {}, headers = {}) {
        if (!this.isConnected || !this.stompClient) {
            this.log('WebSocket이 연결되지 않았습니다. 메시지 전송 실패:', destination);
            return false;
        }
        
        try {
            this.stompClient.send(destination, headers, JSON.stringify(message));
            this.log('메시지 전송:', destination, message);
            return true;
        } catch (error) {
            this.log('메시지 전송 실패:', destination, error);
            return false;
        }
    }
    
    /**
     * Ping 시작 (연결 유지)
     */
    startPing() {
        if (this.pingTimer) {
            clearInterval(this.pingTimer);
        }
        
        this.pingTimer = setInterval(() => {
            if (this.isConnected) {
                this.send('/app/ping', {
                    clientId: this.generateClientId(),
                    timestamp: Date.now()
                });
            }
        }, this.pingInterval);
    }
    
    /**
     * 클라이언트 ID 생성
     */
    generateClientId() {
        return 'dashboard-' + Math.random().toString(36).substr(2, 9) + '-' + Date.now();
    }
    
    /**
     * 연결 상태 확인
     */
    isWebSocketConnected() {
        return this.isConnected && this.stompClient && this.stompClient.connected;
    }
    
    /**
     * 로그 출력 (디버그 모드에서만)
     */
    log(...args) {
        if (this.debug) {
            console.log('[WebSocket]', ...args);
        }
    }
}

/**
 * 테스트 메트릭 구독 관리자
 */
class MetricsSubscriptionManager {
    constructor(wsManager) {
        this.wsManager = wsManager;
        this.activeSubscriptions = new Set();
        this.callbacks = new Map();
    }
    
    /**
     * 테스트 메트릭 구독
     */
    subscribeToTestMetrics(testId, callback) {
        const destination = `/topic/metrics/${testId}`;
        
        // 이미 구독 중이면 콜백만 추가
        if (this.activeSubscriptions.has(testId)) {
            this.callbacks.set(testId, callback);
            return;
        }
        
        // 새 구독 시작
        const subscription = this.wsManager.subscribe(destination, (data) => {
            const cb = this.callbacks.get(testId);
            if (cb && data) {
                cb(data);
            }
        });
        
        if (subscription) {
            this.activeSubscriptions.add(testId);
            this.callbacks.set(testId, callback);
            
            // 구독 시작 메시지 전송
            this.wsManager.send(`/app/test/${testId}/subscribe`, {
                action: 'subscribe',
                timestamp: Date.now()
            });
        }
    }
    
    /**
     * 테스트 메트릭 구독 해제
     */
    unsubscribeFromTestMetrics(testId) {
        const destination = `/topic/metrics/${testId}`;
        
        if (this.wsManager.unsubscribe(destination)) {
            this.activeSubscriptions.delete(testId);
            this.callbacks.delete(testId);
        }
    }
    
    /**
     * 테스트 상태 구독
     */
    subscribeToTestStatus(testId, callback) {
        const destination = `/topic/status/${testId}`;
        
        this.wsManager.subscribe(destination, (data) => {
            if (data && callback) {
                callback(data);
            }
        });
        
        // 상태 구독 시작 메시지
        this.wsManager.send(`/app/test/${testId}/status`, {
            action: 'subscribe',
            timestamp: Date.now()
        });
    }
    
    /**
     * 테스트 로그 구독
     */
    subscribeToTestLogs(testId, callback) {
        const destination = `/topic/logs/${testId}`;
        
        this.wsManager.subscribe(destination, (data) => {
            if (data && callback) {
                callback(data);
            }
        });
        
        // 로그 구독 시작 메시지
        this.wsManager.send(`/app/test/${testId}/logs`, {
            action: 'subscribe',
            timestamp: Date.now()
        });
    }
    
    /**
     * 모든 구독 해제
     */
    unsubscribeAll() {
        this.activeSubscriptions.forEach(testId => {
            this.unsubscribeFromTestMetrics(testId);
        });
        
        this.activeSubscriptions.clear();
        this.callbacks.clear();
    }
}

// 전역 WebSocket 매니저 인스턴스
let wsManager = null;
let metricsManager = null;

/**
 * WebSocket 초기화 (페이지 로드 시 호출)
 */
function initializeWebSocket() {
    if (wsManager) {
        return { wsManager, metricsManager };
    }
    
    wsManager = new WebSocketManager();
    metricsManager = new MetricsSubscriptionManager(wsManager);
    
    // 연결 상태 이벤트 핸들러
    wsManager.onConnected = () => {
        showConnectionStatus(true);
        console.log('WebSocket 연결됨');
    };
    
    wsManager.onDisconnected = () => {
        showConnectionStatus(false);
        console.log('WebSocket 연결 해제됨');
    };
    
    wsManager.onError = (error) => {
        showConnectionStatus(false, 'WebSocket 연결 오류');
        console.error('WebSocket 오류:', error);
    };
    
    // 자동 연결
    wsManager.connect();
    
    return { wsManager, metricsManager };
}

/**
 * 연결 상태 UI 업데이트
 */
function showConnectionStatus(connected, message = null) {
    const statusElement = document.getElementById('websocket-status');
    if (!statusElement) return;
    
    statusElement.className = connected ? 
        'alert alert-success alert-sm' : 
        'alert alert-danger alert-sm';
    
    statusElement.textContent = message || (connected ? 
        '실시간 연결 활성화' : 
        '실시간 연결 끊어짐 - 재연결 시도 중...');
    
    // Use Tailwind classes instead of inline styles
    statusElement.classList.remove('hidden');
    
    // 3초 후 자동 숨김 (성공 시에만)
    if (connected) {
        setTimeout(() => {
            statusElement.classList.add('hidden');
        }, 3000);
    }
}

// 페이지 언로드 시 정리
window.addEventListener('beforeunload', () => {
    if (wsManager) {
        wsManager.disconnect();
    }
});

// 전역 함수로 노출
window.initializeWebSocket = initializeWebSocket;
window.wsManager = null;  // 초기화 후 설정됨
window.metricsManager = null;  // 초기화 후 설정됨