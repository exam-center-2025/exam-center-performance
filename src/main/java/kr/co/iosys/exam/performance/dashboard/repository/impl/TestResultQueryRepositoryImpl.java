package kr.co.iosys.exam.performance.dashboard.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import kr.co.iosys.exam.performance.dashboard.repository.TestResultQueryRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class TestResultQueryRepositoryImpl implements TestResultQueryRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public List<Object[]> findRecentResults() {
        String sql = """
            SELECT 
                pt.test_id,
                pt.test_name,
                pt.plan_id,
                pt.status,
                pt.start_time,
                pt.end_time,
                trs.total_requests,
                trs.successful_requests,
                trs.failed_requests,
                trs.success_rate,
                trs.avg_response_time,
                trs.min_response_time,
                trs.max_response_time,
                trs.p50_response_time,
                trs.p75_response_time,
                trs.p95_response_time,
                trs.p99_response_time,
                trs.max_tps,
                trs.avg_tps,
                pt.test_duration_seconds,
                trs.max_concurrent_users,
                pt.result_path,
                pt.error_message
            FROM performance_tests pt
            LEFT JOIN test_results_summary trs ON pt.test_id = trs.test_id
            WHERE pt.status IN ('COMPLETED', 'FAILED', 'CANCELLED')
            ORDER BY pt.created_at DESC
            LIMIT 20
            """;
        
        Query query = entityManager.createNativeQuery(sql);
        return query.getResultList();
    }
    
    @Override
    public Optional<Object[]> findByTestId(String testId) {
        String sql = """
            SELECT 
                pt.test_id,
                pt.test_name,
                pt.plan_id,
                pt.status,
                pt.start_time,
                pt.end_time,
                trs.total_requests,
                trs.successful_requests,
                trs.failed_requests,
                trs.success_rate,
                trs.avg_response_time,
                trs.min_response_time,
                trs.max_response_time,
                trs.p50_response_time,
                trs.p75_response_time,
                trs.p95_response_time,
                trs.p99_response_time,
                trs.max_tps,
                trs.avg_tps,
                pt.test_duration_seconds,
                trs.max_concurrent_users,
                pt.result_path,
                pt.error_message
            FROM performance_tests pt
            LEFT JOIN test_results_summary trs ON pt.test_id = trs.test_id
            WHERE pt.test_id = :testId
            """;
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("testId", testId);
        
        List<Object[]> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public List<Object[]> findByPlanId(Long planId) {
        String sql = """
            SELECT 
                pt.test_id,
                pt.test_name,
                pt.plan_id,
                pt.status,
                pt.start_time,
                pt.end_time,
                trs.total_requests,
                trs.successful_requests,
                trs.failed_requests,
                trs.success_rate,
                trs.avg_response_time,
                pt.result_path
            FROM performance_tests pt
            LEFT JOIN test_results_summary trs ON pt.test_id = trs.test_id
            WHERE pt.plan_id = :planId
            ORDER BY pt.created_at DESC
            """;
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("planId", planId);
        return query.getResultList();
    }
    
    @Override
    public List<Object[]> findActiveTests() {
        String sql = """
            SELECT 
                pt.test_id,
                pt.test_name,
                pt.plan_id,
                pt.status,
                pt.start_time,
                pt.test_duration_seconds,
                pt.max_users
            FROM performance_tests pt
            WHERE pt.status IN ('PENDING', 'RUNNING')
            ORDER BY pt.start_time DESC
            """;
        
        Query query = entityManager.createNativeQuery(sql);
        return query.getResultList();
    }
    
    @Override
    public List<Object[]> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT 
                pt.test_id,
                pt.test_name,
                pt.plan_id,
                pt.status,
                pt.start_time,
                pt.end_time,
                trs.success_rate,
                trs.avg_response_time,
                trs.max_tps
            FROM performance_tests pt
            LEFT JOIN test_results_summary trs ON pt.test_id = trs.test_id
            WHERE pt.start_time BETWEEN :startDate AND :endDate
            ORDER BY pt.start_time DESC
            """;
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }
}