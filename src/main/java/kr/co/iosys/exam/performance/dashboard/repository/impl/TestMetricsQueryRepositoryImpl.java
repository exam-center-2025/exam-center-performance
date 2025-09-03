package kr.co.iosys.exam.performance.dashboard.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import kr.co.iosys.exam.performance.dashboard.repository.TestMetricsQueryRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class TestMetricsQueryRepositoryImpl implements TestMetricsQueryRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public List<Object[]> findByTestIdOrderByTimestamp(String testId) {
        String sql = """
            SELECT 
                tmh.test_id,
                EXTRACT(EPOCH FROM tmh.timestamp) * 1000,
                tmh.active_users,
                tmh.tps,
                tmh.avg_response_time,
                tmh.min_response_time,
                tmh.max_response_time,
                tmh.p95_response_time,
                tmh.p99_response_time,
                tmh.success_count,
                tmh.error_count,
                tmh.error_rate
            FROM test_metrics_history tmh
            WHERE tmh.test_id = :testId
            ORDER BY tmh.timestamp ASC
            """;
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("testId", testId);
        return query.getResultList();
    }
    
    @Override
    public List<Object[]> findRecentMetricsByTestId(String testId) {
        String sql = """
            SELECT 
                tmh.test_id,
                EXTRACT(EPOCH FROM tmh.timestamp) * 1000,
                tmh.active_users,
                tmh.tps,
                tmh.avg_response_time,
                tmh.min_response_time,
                tmh.max_response_time,
                tmh.p95_response_time,
                tmh.p99_response_time,
                tmh.success_count,
                tmh.error_count,
                tmh.error_rate
            FROM test_metrics_history tmh
            WHERE tmh.test_id = :testId
            ORDER BY tmh.timestamp DESC
            LIMIT 100
            """;
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("testId", testId);
        return query.getResultList();
    }
    
    @Override
    public List<Object[]> findByTestIdAndTimeRange(String testId, LocalDateTime startTime, LocalDateTime endTime) {
        String sql = """
            SELECT 
                tmh.test_id,
                EXTRACT(EPOCH FROM tmh.timestamp) * 1000,
                tmh.active_users,
                tmh.tps,
                tmh.avg_response_time,
                tmh.error_rate
            FROM test_metrics_history tmh
            WHERE tmh.test_id = :testId 
            AND tmh.timestamp BETWEEN :startTime AND :endTime
            ORDER BY tmh.timestamp ASC
            """;
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("testId", testId);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        return query.getResultList();
    }
    
    @Override
    public Object[] findPeakMetricsByTestId(String testId) {
        String sql = """
            SELECT 
                tmh.test_id,
                MAX(tmh.tps),
                MAX(tmh.active_users),
                AVG(tmh.avg_response_time),
                MAX(tmh.avg_response_time),
                AVG(tmh.error_rate),
                MAX(tmh.error_rate)
            FROM test_metrics_history tmh
            WHERE tmh.test_id = :testId
            GROUP BY tmh.test_id
            """;
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("testId", testId);
        
        List<Object[]> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
    
    @Override
    public List<Object[]> findRecentHourMetrics() {
        String sql = """
            SELECT 
                tmh.test_id,
                EXTRACT(EPOCH FROM tmh.timestamp) * 1000,
                tmh.active_users,
                tmh.tps,
                tmh.avg_response_time,
                tmh.error_rate
            FROM test_metrics_history tmh
            WHERE tmh.timestamp >= NOW() - INTERVAL '1 hour'
            ORDER BY tmh.timestamp DESC
            """;
        
        Query query = entityManager.createNativeQuery(sql);
        return query.getResultList();
    }
}