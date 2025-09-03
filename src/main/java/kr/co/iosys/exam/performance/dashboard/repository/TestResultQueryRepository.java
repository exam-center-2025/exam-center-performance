package kr.co.iosys.exam.performance.dashboard.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TestResultQueryRepository {
    
    List<Object[]> findRecentResults();
    
    Optional<Object[]> findByTestId(String testId);
    
    List<Object[]> findByPlanId(Long planId);
    
    List<Object[]> findActiveTests();
    
    List<Object[]> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}