# 데이터베이스 스키마 문서

## PostgreSQL 스키마

### 1. performance_tests (성능 테스트)
```sql
CREATE TABLE performance_tests (
    test_id VARCHAR(100) PRIMARY KEY,
    test_name VARCHAR(200) NOT NULL,
    plan_id BIGINT NOT NULL,
    run_type VARCHAR(50),
    max_users INTEGER NOT NULL,
    ramp_up_seconds INTEGER NOT NULL,
    test_duration_seconds INTEGER NOT NULL,
    scenario VARCHAR(50),
    status VARCHAR(50) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    error_message TEXT,
    result_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_performance_tests_plan_id ON performance_tests(plan_id);
CREATE INDEX idx_performance_tests_status ON performance_tests(status);
CREATE INDEX idx_performance_tests_created_at ON performance_tests(created_at DESC);
```

### 2. test_metrics_history (테스트 메트릭 히스토리)
```sql
CREATE TABLE test_metrics_history (
    id BIGSERIAL PRIMARY KEY,
    test_id VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    active_users INTEGER,
    tps DECIMAL(10,2),
    avg_response_time DECIMAL(10,2),
    min_response_time DECIMAL(10,2),
    max_response_time DECIMAL(10,2),
    p95_response_time DECIMAL(10,2),
    p99_response_time DECIMAL(10,2),
    success_count BIGINT,
    error_count BIGINT,
    error_rate DECIMAL(5,2),
    FOREIGN KEY (test_id) REFERENCES performance_tests(test_id) ON DELETE CASCADE
);

CREATE INDEX idx_metrics_history_test_id ON test_metrics_history(test_id);
CREATE INDEX idx_metrics_history_timestamp ON test_metrics_history(test_id, timestamp DESC);
```

### 3. test_results_summary (테스트 결과 요약)
```sql
CREATE TABLE test_results_summary (
    test_id VARCHAR(100) PRIMARY KEY,
    total_requests BIGINT,
    successful_requests BIGINT,
    failed_requests BIGINT,
    success_rate DECIMAL(5,2),
    avg_response_time DECIMAL(10,2),
    min_response_time DECIMAL(10,2),
    max_response_time DECIMAL(10,2),
    p50_response_time DECIMAL(10,2),
    p75_response_time DECIMAL(10,2),
    p95_response_time DECIMAL(10,2),
    p99_response_time DECIMAL(10,2),
    max_tps DECIMAL(10,2),
    avg_tps DECIMAL(10,2),
    total_data_kb BIGINT,
    test_duration_seconds INTEGER,
    max_concurrent_users INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (test_id) REFERENCES performance_tests(test_id) ON DELETE CASCADE
);
```

### 4. test_templates (테스트 템플릿)
```sql
CREATE TABLE test_templates (
    template_id BIGSERIAL PRIMARY KEY,
    template_name VARCHAR(200) NOT NULL UNIQUE,
    description TEXT,
    max_users INTEGER NOT NULL,
    ramp_up_seconds INTEGER NOT NULL,
    test_duration_seconds INTEGER NOT NULL,
    scenario VARCHAR(50),
    additional_config JSONB,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 5. test_schedules (테스트 스케줄)
```sql
CREATE TABLE test_schedules (
    schedule_id BIGSERIAL PRIMARY KEY,
    schedule_name VARCHAR(200) NOT NULL,
    plan_id BIGINT NOT NULL,
    template_id BIGINT,
    cron_expression VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    last_run_time TIMESTAMP,
    next_run_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (template_id) REFERENCES test_templates(template_id)
);

CREATE INDEX idx_test_schedules_active ON test_schedules(is_active, next_run_time);
```

## 기존 테이블 참조 (exam_db)

### exam_plans (기존)
- plan_id: BIGINT
- name: VARCHAR(255)
- status: VARCHAR(50)
- start_time: TIMESTAMP
- end_time: TIMESTAMP

### exam_groups (기존)
- group_id: BIGINT
- plan_id: BIGINT
- run_type: VARCHAR(50)
- name: VARCHAR(255)

### exam_users (기존)
- user_id: BIGINT
- plan_id: BIGINT
- run_type: VARCHAR(50)
- user_name: VARCHAR(100)
- access_key: VARCHAR(255)