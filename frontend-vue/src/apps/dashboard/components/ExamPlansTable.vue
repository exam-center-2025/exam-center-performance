팀장님 🙆 (버전: **GPT-5**)

아래는 주신 **메시지 설계 문서**(v1.0)를 기준으로, 바로 적용 가능한 **개선안(v1.1 제안)** 과 **실전 스키마/운영 가이드**입니다. 코드는 그대로 복붙해 검증/배포 파이프라인에 넣을 수 있게 최소·명확하게 드렸습니다.

---

# 0) 한눈에 보는 핵심 개선 포인트

1. **키/파티셔닝 정합성**

* `commands`: *전략 단위 순서 보장*이 중요 → **`strategy_id`를 메시지 키**로 사용하고, 파티션도 `strategy_id` 해시 기반(모듈로)으로 가는 것이 안전합니다. (현재 문서의 `strategy_type` 기반 분산은 동일 전략이 여러 파티션에 흩어질 수 있어 순서 보장 어려움)
* `events`: 이미 `strategy_id % N` 개념 유지 → **키도 `strategy_id`** 로 통일(생산자 측에서 `key=strategy_id` 설정).

2. **헤더 확장 & 표준화**

* 공통 헤더에 **`env`, `tenant_id`(멀티테넌시 시), `schema_version`** 추가.
* **idempotency**: `message_id`를 **메시지 키**로도 활용 가능(중복 처리 방지).
* **서명/암호화** 필드 분리: `signature`, `encryption` 메타.

3. **명령 수명주기 & 상태 세분화**

* 명령 처리 3단계: `ACCEPTED`(큐 적재) → `IN_PROGRESS` → `ACKED`/`FAILED` 이벤트.
* `events`에 **에러 분류코드**(`error_code`) 및 **복구 힌트**(`retry_after`, `recoverable`) 표준화.

4. **스키마 강제 & 호환성**

* JSON Schema를 **리포지토리 내 단일 출처**로 두고, **Producer/Consumer 미들웨어에서 검증**.
* **하위 호환 정책**: 필드 추가만 허용, 삭제/타입 변경 금지. `schema_version` 명시.

5. **운영 가드레일**

* 토픽별 설정 권고: `min.insync.replicas`, `acks=all`, `retention.ms`, `max.message.bytes`, `compression.type=gzip`.
* **DLQ 포맷 표준화** + **재처리 툴** 전제(원본 토픽/오프셋 포함).
* **Outbox 패턴**(DB outbox\_events → Kafka): Manager/전략 서비스 양쪽에 채택 권장.

---

# 1) 폴더 구조(권장)

```
message_schemas/
  common.json          # $defs: header, enums, 공통 타입
  commands.json        # tradebot.strategy.commands
  events.json          # tradebot.strategy.events
  metrics.json         # tradebot.strategy.metrics
  alerts.json          # tradebot.system.alerts
  dlq.json             # tradebot.dlq.*
libs/messaging/
  validator.py         # jsonschema 검증 유틸
  producer.py          # 검증 내장 Kafka producer
  consumer.py          # 검증 내장 Kafka consumer (DLQ 연동)
```

---

# 2) 공통 \$defs (common.json 예시)

```json
{
  "$id": "https://tradebot/schemas/common.json",
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$defs": {
    "Header": {
      "type": "object",
      "required": ["message_id", "timestamp", "schema_version", "source"],
      "properties": {
        "message_id": { "type": "string", "format": "uuid" },
        "timestamp":  { "type": "string", "format": "date-time" },
        "schema_version": { "type": "string", "pattern": "^[0-9]+\\.[0-9]+$" },
        "source": { "type": "string", "minLength": 1 },
        "correlation_id": { "type": "string" },
        "env": { "type": "string", "enum": ["dev","stg","prod"] },
        "tenant_id": { "type": "string" },
        "signature": { "type": "string" },
        "encryption": {
          "type": "object",
          "properties": {
            "alg": { "type": "string", "enum": ["AES-256-GCM"] },
            "encrypted_fields": { "type": "array", "items": {"type": "string"} }
          },
          "additionalProperties": false
        }
      },
      "additionalProperties": false
    },
    "StrategyId": { "type": "integer", "minimum": 1 },
    "InstanceId": { "type": "integer", "minimum": 1 },
    "Severity": { "type": "string", "enum": ["INFO","WARNING","ERROR","CRITICAL","HIGH","LOW"] },
    "Status": { "type": "string", "enum": ["pending","running","paused","stopped","error","success","failed"] }
  }
}
```

---

# 3) Commands 스키마 (commands.json, v1.1)

```json
{
  "$id": "https://tradebot/schemas/commands.json",
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$ref": "common.json#/$defs/Header",
  "type": "object",
  "required": ["header", "command", "payload"],
  "properties": {
    "header": { "$ref": "common.json#/$defs/Header" },
    "command": {
      "type": "string",
      "enum": ["START","STOP","UPDATE_CONFIG","PAUSE","RESUME","RESTART"]
    },
    "payload": {
      "type": "object",
      "required": ["strategy_id","strategy_type"],
      "properties": {
        "strategy_id": { "$ref": "common.json#/$defs/StrategyId" },
        "strategy_type": { "type": "string", "enum": ["transfer","arbitrage","market_making"] },
        "strategy_name": { "type": "string" },
        "force": { "type": "boolean" },
        "reason": { "type": "string" },
        "config": { "type": "object" },
        "config_changes": { "type": "object" }
      },
      "additionalProperties": false
    }
  },
  "additionalProperties": false
}
```

**프로듀싱 규칙**

* Kafka `key` → **`strategy_id`(string 변환)**
* 토픽 → `tradebot.strategy.commands`
* 헤더 `schema_version` → `"1.1"`

---

# 4) Events 스키마 (events.json, v1.1)

```json
{
  "$id": "https://tradebot/schemas/events.json",
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "required": ["header","event","payload"],
  "properties": {
    "header": { "$ref": "common.json#/$defs/Header" },
    "event": {
      "type": "string",
      "enum": [
        "STRATEGY_ACCEPTED","STRATEGY_IN_PROGRESS","STRATEGY_STARTED",
        "STRATEGY_PAUSED","STRATEGY_RESUMED","STRATEGY_STOPPED",
        "EXECUTION_STARTED","EXECUTION_COMPLETED",
        "STRATEGY_ERROR","MESSAGE_PROCESSING_FAILED"
      ]
    },
    "payload": {
      "type": "object",
      "properties": {
        "strategy_id": { "$ref": "common.json#/$defs/StrategyId" },
        "instance_id": { "$ref": "common.json#/$defs/InstanceId" },
        "container_id": { "type": "string" },
        "execution_id": { "type": "integer" },
        "action": { "type": "string" },
        "status": { "$ref": "common.json#/$defs/Status" },
        "details": { "type": "object" },
        "started_at": { "type": "string", "format": "date-time" },
        "stopped_at": { "type": "string", "format": "date-time" },
        "final_metrics": { "type": "object" },
        "error_code": { "type": "string" },
        "message": { "type": "string" },
        "severity": { "$ref": "common.json#/$defs/Severity" },
        "retry_after": { "type": "string", "format": "date-time" },
        "recoverable": { "type": "boolean" }
      },
      "additionalProperties": false
    }
  },
  "additionalProperties": false
}
```

**생산/소비 규칙**

* Kafka `key` → **`strategy_id`**
* 토픽 → `tradebot.strategy.events`
* **명령 수명주기 이벤트 추가**: `STRATEGY_ACCEPTED` → `STRATEGY_IN_PROGRESS` → `STRATEGY_STARTED/FAILED`

---

# 5) Metrics 스키마 (metrics.json, v1.1)

```json
{
  "$id": "https://tradebot/schemas/metrics.json",
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "required": ["header","metric_type","payload"],
  "properties": {
    "header": { "$ref": "common.json#/$defs/Header" },
    "metric_type": {
      "type": "string",
      "enum": ["EXECUTION_METRICS","PERFORMANCE_METRICS","SYSTEM_METRICS"]
    },
    "payload": {
      "type": "object",
      "properties": {
        "strategy_id": { "$ref": "common.json#/$defs/StrategyId" },
        "service": { "type": "string" },
        "window": { "type": "string", "enum": ["1m","5m","15m","1h","1d"] },
        "metrics": { "type": "object" }
      },
      "required": ["metrics"],
      "additionalProperties": false
    }
  },
  "additionalProperties": false
}
```

* Kafka `key` → 가능하면 `strategy_id`(전략별 집계 용이).
* TimescaleDB 적재 전 **타입/단위** 검증(집계 뷰 안정성).

---

# 6) Alerts 스키마 (alerts.json, v1.1)

```json
{
  "$id": "https://tradebot/schemas/alerts.json",
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "required": ["header","alert_type","severity","payload"],
  "properties": {
    "header": { "$ref": "common.json#/$defs/Header" },
    "alert_type": { "type": "string", "enum": ["RISK_ALERT","SYSTEM_ALERT"] },
    "severity": { "$ref": "common.json#/$defs/Severity" },
    "payload": { "type": "object" }
  },
  "additionalProperties": false
}
```

* 토픽: `tradebot.system.alerts`
* 운영 라우팅(메일/Slack) 전에 **스키마 검증 → 필터링**.

---

# 7) DLQ/에러 스키마 (dlq.json, v1.1)

```json
{
  "$id": "https://tradebot/schemas/dlq.json",
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "required": ["header","event","payload"],
  "properties": {
    "header": { "$ref": "common.json#/$defs/Header" },
    "event": { "type": "string", "const": "MESSAGE_PROCESSING_FAILED" },
    "payload": {
      "type": "object",
      "required": ["original_topic","error","error_message","retry_count","moved_to_dlq"],
      "properties": {
        "original_topic": { "type": "string" },
        "original_partition": { "type": "integer" },
        "original_offset": { "type": "integer" },
        "original_key": { "type": "string" },
        "original_message_id": { "type": "string" },
        "original_message": { "type": "object" },
        "error": { "type": "string" },
        "error_message": { "type": "string" },
        "retry_count": { "type": "integer", "minimum": 0 },
        "moved_to_dlq": { "type": "boolean" }
      },
      "additionalProperties": false
    }
  },
  "additionalProperties": false
}
```

* 토픽: `tradebot.dlq.<원본토픽>`
* **오프셋/파티션/키**까지 기록 → 재처리 자동화에 필수.

---

# 8) 토픽/프로듀서 운영 파라미터(권고)

* **producer**: `acks=all`, `enable.idempotence=true`, `compression.type=gzip`, `max.in.flight.requests.per.connection=1`(순서 보장 필요 시)
* **topic**:

  * `min.insync.replicas=2`(3중 복제 가정)
  * `retention.ms=604800000`(7일), DLQ는 30일
  * `cleanup.policy=delete`(DLQ는 운영 정책에 따라 compact+delete도 고려)
* **키 설정**:

  * commands/events/metrics 공통 → **`key=str(strategy_id)`**(시스템 일관성)

---

# 9) 처리 수명주기(State) 표준 이벤트

* **명령 수신**: `STRATEGY_ACCEPTED` (큐 적재 OK)
* **작업 중**: `STRATEGY_IN_PROGRESS`
* **성공/종료**: `STRATEGY_STARTED` / `STRATEGY_STOPPED`
* **일시정지/재개**: `STRATEGY_PAUSED` / `STRATEGY_RESUMED`
* **실행 이력**: `EXECUTION_STARTED` / `EXECUTION_COMPLETED`
* **오류**: `STRATEGY_ERROR` (필수: `error_code`, `recoverable`, `retry_after`)

---

# 10) 적용 방법(요약)

1. 위 **JSON 스키마 파일**을 `message_schemas/`에 저장
2. **Producer/Consumer 래퍼**에 **스키마 검증 미들웨어** 연결
3. Kafka `send()` 전에 `validate("commands", msg)` 등으로 **런타임 검증**
4. **검증 실패** 시 → 즉시 **DLQ** 전송 + 알림
5. CI에서 **스키마 self-check + 예제 메시지 검증** 테스트 추가
6. 운영 지표: **검증 실패율, DLQ 크기, 소비 지연(lag)** 대시보드화

---

