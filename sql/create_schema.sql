-- Performance Testing Database Schema
-- Date: 2025-01-28
-- AIDEV-NOTE: 성능 테스트 결과 저장용 테이블 스키마

-- 1. 성능 테스트 마스터 테이블
CREATE TABLE IF NOT EXISTS performance_tests (
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

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_performance_tests_plan_id ON performance_tests(plan_id);
CREATE INDEX IF NOT EXISTS idx_performance_tests_status ON performance_tests(status);
CREATE INDEX IF NOT EXISTS idx_performance_tests_start_time ON performance_tests(start_time DESC);
CREATE INDEX IF NOT EXISTS idx_performance_tests_created_at ON performance_tests(created_at DESC);

-- 2. 테스트 결과 요약 테이블
CREATE TABLE IF NOT EXISTS test_results_summary (
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

-- 3. 테스트 메트릭 히스토리 테이블
CREATE TABLE IF NOT EXISTS test_metrics_history (
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

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_test_metrics_history_test_id ON test_metrics_history(test_id);
CREATE INDEX IF NOT EXISTS idx_test_metrics_history_timestamp ON test_metrics_history(test_id, timestamp DESC);

-- 4. 테스트 에러 로그 테이블
CREATE TABLE IF NOT EXISTS test_error_logs (
    id BIGSERIAL PRIMARY KEY,
    test_id VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    error_type VARCHAR(100),
    error_message TEXT,
    endpoint VARCHAR(500),
    status_code INTEGER,
    request_body TEXT,
    response_body TEXT,
    stack_trace TEXT,
    FOREIGN KEY (test_id) REFERENCES performance_tests(test_id) ON DELETE CASCADE
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_test_error_logs_test_id ON test_error_logs(test_id);
CREATE INDEX IF NOT EXISTS idx_test_error_logs_timestamp ON test_error_logs(test_id, timestamp DESC);

-- 5. 테스트 엔드포인트별 통계 테이블
CREATE TABLE IF NOT EXISTS test_endpoint_stats (
    id BIGSERIAL PRIMARY KEY,
    test_id VARCHAR(100) NOT NULL,
    endpoint VARCHAR(500) NOT NULL,
    method VARCHAR(10),
    total_requests BIGINT,
    successful_requests BIGINT,
    failed_requests BIGINT,
    avg_response_time DECIMAL(10,2),
    min_response_time DECIMAL(10,2),
    max_response_time DECIMAL(10,2),
    p95_response_time DECIMAL(10,2),
    p99_response_time DECIMAL(10,2),
    FOREIGN KEY (test_id) REFERENCES performance_tests(test_id) ON DELETE CASCADE
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_test_endpoint_stats_test_id ON test_endpoint_stats(test_id);

-- 6. 테스트 시나리오 설정 테이블
CREATE TABLE IF NOT EXISTS test_scenarios (
    id BIGSERIAL PRIMARY KEY,
    scenario_name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    config_json JSONB,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. 테스트 비교 분석 테이블
CREATE TABLE IF NOT EXISTS test_comparisons (
    id BIGSERIAL PRIMARY KEY,
    comparison_name VARCHAR(200) NOT NULL,
    base_test_id VARCHAR(100) NOT NULL,
    compare_test_id VARCHAR(100) NOT NULL,
    comparison_result JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (base_test_id) REFERENCES performance_tests(test_id),
    FOREIGN KEY (compare_test_id) REFERENCES performance_tests(test_id)
);

-- 테이블 코멘트 추가
COMMENT ON TABLE performance_tests IS '성능 테스트 마스터 정보';
COMMENT ON TABLE test_results_summary IS '테스트 결과 요약 통계';
COMMENT ON TABLE test_metrics_history IS '테스트 실행 중 시계열 메트릭 데이터';
COMMENT ON TABLE test_error_logs IS '테스트 중 발생한 에러 로그';
COMMENT ON TABLE test_endpoint_stats IS 'API 엔드포인트별 성능 통계';
COMMENT ON TABLE test_scenarios IS '재사용 가능한 테스트 시나리오 설정';
COMMENT ON TABLE test_comparisons IS '테스트 간 비교 분석 결과';

-- 권한 부여
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO postgres;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO postgres;