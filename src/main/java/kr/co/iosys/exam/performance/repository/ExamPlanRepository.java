package kr.co.iosys.exam.performance.repository;

import kr.co.iosys.exam.performance.model.ExamPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 시험 계획 Repository
 * exam_plans 테이블 접근
 */
@Repository
public interface ExamPlanRepository extends JpaRepository<ExamPlan, Long> {
    
    /**
     * 활성화된 시험 계획 목록 조회
     */
    List<ExamPlan> findByEnableYn(Character enableYn);
    
    /**
     * 상태별 시험 계획 조회
     */
    List<ExamPlan> findByStatusAndEnableYn(String status, Character enableYn);
    
    /**
     * 현재 진행중인 시험 계획 조회
     */
    @Query("SELECT e FROM ExamPlan e WHERE e.startTime <= :now AND e.endTime >= :now AND e.enableYn = 'Y'")
    List<ExamPlan> findActiveExamPlans(@Param("now") LocalDateTime now);
    
    /**
     * 최근 시험 계획 조회 (최대 10개)
     */
    @Query("SELECT e FROM ExamPlan e WHERE e.enableYn = 'Y' ORDER BY e.createdAt DESC")
    List<ExamPlan> findRecentExamPlans();
}