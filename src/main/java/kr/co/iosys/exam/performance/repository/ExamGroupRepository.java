package kr.co.iosys.exam.performance.repository;

import kr.co.iosys.exam.performance.model.ExamGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AIDEV-NOTE: 시험 그룹 JPA Repository
 */
@Repository
public interface ExamGroupRepository extends JpaRepository<ExamGroup, Long> {
    
    List<ExamGroup> findByPlanIdOrderByName(Long planId);
}