# Exam Center Performance Test Service

시험 센터 성능 테스트를 위한 Spring Boot WebFlux 기반 서비스입니다.

## 📋 주요 기능

- **Gatling 프로그래매틱 실행**: Java API를 통한 Gatling 시뮬레이션 실행
- **데이터베이스 연동**: PostgreSQL에서 실제 시험 데이터 조회
- **REST API**: 성능 테스트 생성, 상태 조회, 결과 관리
- **동적 설정**: 테스트 파라미터를 동적으로 설정 가능
- **실시간 모니터링**: 테스트 실행 상태 실시간 추적

## 🚀 빠른 시작

### 1. 애플리케이션 빌드 및 실행

```bash
# 빌드
./gradlew build

# 실행 (기본 포트: 8097)
./gradlew bootRun

# 또는
java -jar build/libs/exam-center-performance-1.0.0-SNAPSHOT.jar
```

### 2. Swagger UI 접속

```
http://localhost:8097/performance/swagger-ui.html
```

## 📡 API 엔드포인트

### 성능 테스트 관리

- `POST /api/performance/tests/start` - 테스트 시작
- `GET /api/performance/tests/{testId}/status` - 테스트 상태 조회
- `GET /api/performance/tests/{testId}/results` - 결과 조회
- `DELETE /api/performance/tests/{testId}` - 테스트 중단
- `GET /api/performance/tests` - 모든 테스트 조회

### 데이터 조회

- `GET /api/performance/plans` - 시험 계획 목록 조회
- `GET /api/performance/plans/{planId}/groups` - 그룹 목록 조회
- `GET /api/performance/health/database` - DB 연결 상태 확인

## 🧪 테스트 실행 예제

### 1. Gatling 시뮬레이션 직접 실행

#### 기본 실행 (plan_id=1, run_type=TEST, user_count=10)
```bash
./gradlew gatlingRun
```

#### 파라미터를 통한 동적 실행
```bash
# 시험 계획 ID 2, 운영 타입 TEST, 사용자 수 50명
./gradlew gatlingRun -Dplan_id=2 -Drun_type=TEST -Duser_count=50

# 소규모 테스트 (사용자 5명)
./gradlew gatlingRun -Dplan_id=1 -Drun_type=TEST -Duser_count=5

# 대규모 테스트 (사용자 200명)
./gradlew gatlingRun -Dplan_id=1 -Drun_type=REAL -Duser_count=200
```

#### 테스트 결과 확인
Gatling 실행 완료 후 콘솔에 출력되는 HTML 리포트 경로를 열어서 결과 확인:
```
file:///home/ldm/exam-center-dev/exam-center-performance/build/reports/gatling/examcentersimulation-[TIMESTAMP]/index.html
```

### 2. REST API를 통한 테스트 실행

#### 기본 테스트 실행
```bash
curl -X POST http://localhost:8097/performance/api/performance/tests/start \
  -H "Content-Type: application/json" \
  -d '{
    "testName": "기본 성능 테스트",
    "planId": 1,
    "maxUsers": 100,
    "rampUpDurationSeconds": 60,
    "testDurationSeconds": 300,
    "runType": "TEST",
    "scenario": "NORMAL_USER"
  }'
```

#### 테스트 상태 확인
```bash
curl http://localhost:8097/performance/api/performance/tests/{testId}/status
```

## ⚙️ 설정

### application.yml 주요 설정

```yaml
performance-test:
  gatling:
    results-directory: "./gatling-results"
    target:
      base-url: "http://localhost:8091"
      websocket-url: "ws://localhost:8099"
    max-concurrent-tests: 3
    test-timeout-minutes: 30
  database:
    host: "192.168.100.105"
    port: 5432
    database: "exam_db"
    username: "postgres"
    password: "postgres"
```

### 환경 변수

- `SERVER_PORT`: 서버 포트 (기본: 8097)
- `POSTGRES_HOST`: PostgreSQL 호스트
- `POSTGRES_PORT`: PostgreSQL 포트
- `POSTGRES_DATABASE`: 데이터베이스 이름
- `POSTGRES_USERNAME`: DB 사용자명
- `POSTGRES_PASSWORD`: DB 비밀번호
- `TARGET_BASE_URL`: 테스트 대상 서버 URL
- `TARGET_WS_URL`: WebSocket 서버 URL

## 🏗️ 아키텍처

```
├── controller/          # REST API 컨트롤러
├── service/            
│   ├── GatlingRunnerService      # Gatling 실행 관리
│   ├── TestConfigurationService  # 테스트 설정 관리
│   └── DatabaseService          # 데이터베이스 조회
├── simulation/         
│   └── DynamicExamSimulation    # 동적 Gatling 시뮬레이션
├── dto/               # 데이터 전송 객체
├── model/            # 데이터 모델
├── config/           # 설정 클래스
└── exception/        # 예외 처리
```

## 🎯 Gatling 시뮬레이션 기능

### ExamCenterSimulation 주요 특징

- **PostgreSQL 연동**: exam_users 테이블에서 실제 사용자 데이터 조회
- **동적 파라미터**: plan_id, run_type, user_count 시스템 속성 지원
- **완전한 시나리오**: Security API → WebSocket → STATUS_MSG 전체 플로우
- **부하 분산**: 점진적 사용자 증가로 서버 부담 최소화
- **실시간 모니터링**: Gatling 내장 리포팅 시스템

### 시나리오 플로우

1. **Security API 인증**: `/security/auth/keyLogin` 호출
2. **WebSocket 연결**: `ws://localhost:8099/ws` 연결
3. **STATUS_MSG 시퀀스**:
   - `SYSTEM_CHECK` (시스템 체크)
   - `INFO_0` 시작 → 완료
   - `TEST_0` 시작 → 완료
4. **WebSocket 종료** 및 **로그아웃**

### 파라미터 설명

- `plan_id`: 시험 계획 ID (기본값: 1)
- `run_type`: 실행 타입 TEST/REAL (기본값: TEST)
- `user_count`: 동시 접속 사용자 수 (기본값: 10)

## 🔧 개발 환경

- **Java**: 17+
- **Spring Boot**: 3.4.2
- **Gatling**: 3.10.5
- **PostgreSQL**: 15+
- **Gradle**: 8.5

## 📊 모니터링

### Actuator 엔드포인트

```
http://localhost:8097/performance/actuator/health
http://localhost:8097/performance/actuator/metrics
http://localhost:8097/performance/actuator/prometheus
```

### 로그 레벨 설정

```yaml
logging:
  level:
    kr.co.iosys.exam.performance: INFO
    io.gatling: WARN
```

## 🧪 테스트

```bash
# 단위 테스트 실행
./gradlew test

# 테스트 리포트 확인
open build/reports/tests/test/index.html
```

## 🚨 주의사항

1. **동시 실행 제한**: 기본적으로 최대 3개의 테스트만 동시 실행 가능
2. **메모리 사용량**: Gatling 실행 시 높은 메모리 사용량 예상
3. **네트워크**: 대상 서버와의 네트워크 연결 상태 확인 필요
4. **데이터베이스**: PostgreSQL 연결 및 테스트 데이터 존재 확인

## 📝 로깅

성능 테스트 실행 과정에서 상세한 로그가 출력됩니다:

- 테스트 시작/완료 시간
- 사용자 데이터 조회 결과
- Gatling 실행 상태
- 오류 및 예외 정보

## 🔗 관련 프로젝트

- `exam-center-manager`: 시험 관리 서비스
- `exam-center-security`: 인증 서비스
- `exam-center-websocket`: WebSocket 서비스
- `load-test/gatling`: 기존 Gatling 테스트 스크립트