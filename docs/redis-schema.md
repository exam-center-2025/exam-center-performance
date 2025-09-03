# Redis 스키마 문서

## Redis 키 구조 및 데이터 형식

### 1. 실시간 메트릭 (String)
```
키: metrics:current:{test_id}
TTL: 10초
데이터 형식: JSON
{
    "testId": "TestWithGradle_1_20250828183842",
    "timestamp": 1706430000000,
    "activeUsers": 85,
    "tps": 1250.5,
    "avgResponseTime": 245.3,
    "minResponseTime": 12,
    "maxResponseTime": 1890,
    "successCount": 12500,
    "errorCount": 3,
    "errorRate": 0.024,
    "progress": 65.5
}
```

### 2. 테스트 상태 (String)
```
키: test:status:{test_id}
TTL: 테스트 종료 후 1시간
데이터 형식: JSON
{
    "testId": "TestWithGradle_1_20250828183842",
    "status": "RUNNING",  // PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
    "startTime": 1706430000000,
    "currentUsers": 85,
    "targetUsers": 100,
    "progress": 65.5,
    "message": "Ramping up users..."
}
```

### 3. 시계열 메트릭 (Sorted Set)
```
키: timeline:{test_id}:{metric_type}
TTL: 24시간
Score: timestamp (밀리초)
Member: metric value

예시:
timeline:TestWithGradle_1_20250828183842:tps
timeline:TestWithGradle_1_20250828183842:response_time
timeline:TestWithGradle_1_20250828183842:active_users
timeline:TestWithGradle_1_20250828183842:error_rate
```

### 4. 실시간 로그 (List)
```
키: logs:{test_id}
TTL: 1시간
데이터 형식: JSON
최대 크기: 1000 entries (LTRIM 사용)

LPUSH logs:TestWithGradle_1_20250828183842 
{
    "timestamp": 1706430000000,
    "level": "INFO",
    "message": "User 1 connected successfully",
    "userId": "user_001"
}
```

### 5. 활성 테스트 목록 (Set)
```
키: tests:active
TTL: 없음 (수동 관리)
Member: test_id

SADD tests:active "TestWithGradle_1_20250828183842"
SREM tests:active "TestWithGradle_1_20250828183842"
```

### 6. WebSocket 세션 (Hash)
```
키: websocket:sessions:{test_id}
TTL: 테스트 종료 후 10분
Field: session_id
Value: client info JSON

HSET websocket:sessions:TestWithGradle_1_20250828183842 
     "session_123" 
     '{"clientIp":"192.168.1.100","connectedAt":1706430000000}'
```

### 7. 테스트 큐 (List)
```
키: queue:pending_tests
TTL: 없음
데이터: test configuration JSON

RPUSH queue:pending_tests '{
    "testId": "TestWithGradle_1_20250828183842",
    "planId": 1,
    "maxUsers": 100,
    "priority": 1
}'
```

### 8. 실시간 알림 (Pub/Sub)
```
채널: notifications:{test_id}
메시지 형식: JSON

PUBLISH notifications:TestWithGradle_1_20250828183842 '{
    "type": "TEST_COMPLETED",
    "testId": "TestWithGradle_1_20250828183842",
    "success": true,
    "message": "Test completed successfully"
}'
```

### 9. 메트릭 버퍼 (List)
```
키: buffer:metrics:{test_id}
TTL: 5분
용도: PostgreSQL 배치 저장용 버퍼

RPUSH buffer:metrics:TestWithGradle_1_20250828183842 '{...metric data...}'
```

### 10. 캐시 (String)
```
키: cache:plan:{plan_id}
TTL: 5분
데이터: 시험 계획 정보 JSON

키: cache:groups:{plan_id}:{run_type}
TTL: 5분
데이터: 그룹 목록 JSON

키: cache:users:{plan_id}:{run_type}
TTL: 5분
데이터: 사용자 목록 JSON
```

## Redis 명령어 예시

### 실시간 메트릭 저장
```redis
SET metrics:current:TestWithGradle_1_20250828183842 
    '{"testId":"TestWithGradle_1_20250828183842","activeUsers":85,...}' 
    EX 10
```

### 시계열 데이터 추가
```redis
ZADD timeline:TestWithGradle_1_20250828183842:tps 
     1706430000000 1250.5
```

### 시계열 데이터 조회 (최근 1분)
```redis
ZRANGEBYSCORE timeline:TestWithGradle_1_20250828183842:tps 
              (now-60000) now
```

### 로그 추가 및 크기 제한
```redis
LPUSH logs:TestWithGradle_1_20250828183842 '{"level":"INFO","message":"..."}'
LTRIM logs:TestWithGradle_1_20250828183842 0 999
```

### 활성 테스트 조회
```redis
SMEMBERS tests:active
```

## 메모리 관리 전략

1. **TTL 설정 필수**
   - 실시간 데이터: 5-10초
   - 시계열 데이터: 24시간
   - 로그: 1시간
   - 상태 정보: 테스트 종료 후 1시간

2. **크기 제한**
   - 로그 리스트: 최대 1000개
   - 시계열 데이터: 24시간 이내만 유지

3. **정리 작업**
   - 테스트 완료 시 관련 키 정리
   - Cron job으로 만료된 데이터 정기 삭제