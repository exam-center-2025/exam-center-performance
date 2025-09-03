package kr.co.iosys.exam.performance.repository;

import kr.co.iosys.exam.performance.model.PerformanceTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 성능 테스트 Repository
 * performance_tests 테이블 접근
 */
@Repository
public interface PerformanceTestRepository extends JpaRepository<PerformanceTest, String> {
    
    /**
     * Plan ID로 테스트 조회
     */
    List<PerformanceTest> findByPlanIdOrderByStartTimeDesc(Long planId);
    
    /**
     * 상태별 테스트 조회
     */
    List<PerformanceTest> findByStatus(String status);
    
    /**
     * 최근 테스트 조회
     */
    @Query("SELECT p FROM PerformanceTest p ORDER BY p.startTime DESC")
    List<PerformanceTest> findRecentTests();
    
    /**
     * 특정 기간 내 테스트 조회
     */
    @Query("SELECT p FROM PerformanceTest p WHERE p.startTime BETWEEN :startDate AND :endDate ORDER BY p.startTime DESC")
    List<PerformanceTest> findTestsBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);
}