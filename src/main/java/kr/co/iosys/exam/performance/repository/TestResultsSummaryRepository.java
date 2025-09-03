package kr.co.iosys.exam.performance.repository;

import kr.co.iosys.exam.performance.model.TestResultsSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 테스트 결과 요약 Repository
 * test_results_summary 테이블 접근
 */
@Repository
public interface TestResultsSummaryRepository extends JpaRepository<TestResultsSummary, String> {
    
}