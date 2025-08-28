package kr.co.iosys.exam.performance.repository;

import kr.co.iosys.exam.performance.model.ExamPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AIDEV-NOTE: 시험 계획 JPA Repository
 */
@Repository
public interface ExamPlanRepository extends JpaRepository<ExamPlan, Long> {
    
    List<ExamPlan> findByStatusOrderByCreatedAtDesc(String status);
}