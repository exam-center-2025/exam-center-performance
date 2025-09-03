package kr.co.iosys.exam.performance.dashboard.repository;

import java.time.LocalDateTime;
import java.util.List;

public interface TestMetricsQueryRepository {
    
    List<Object[]> findByTestIdOrderByTimestamp(String testId);
    
    List<Object[]> findRecentMetricsByTestId(String testId);
    
    List<Object[]> findByTestIdAndTimeRange(String testId, LocalDateTime startTime, LocalDateTime endTime);
    
    Object[] findPeakMetricsByTestId(String testId);
    
    List<Object[]> findRecentHourMetrics();
}