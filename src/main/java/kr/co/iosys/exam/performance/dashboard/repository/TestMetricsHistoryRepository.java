package kr.co.iosys.exam.performance.dashboard.repository;

import kr.co.iosys.exam.performance.dashboard.entity.TestMetricsHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TestMetricsHistoryRepository extends JpaRepository<TestMetricsHistory, Long> {
    
    List<TestMetricsHistory> findByTestIdOrderByTimestamp(String testId);
    
    List<TestMetricsHistory> findByTestIdAndTimestampBetween(String testId, 
                                                             LocalDateTime startTime, 
                                                             LocalDateTime endTime);
    
    @Query("SELECT t FROM TestMetricsHistory t WHERE t.testId = :testId ORDER BY t.timestamp DESC")
    List<TestMetricsHistory> findLatestByTestId(@Param("testId") String testId);
}