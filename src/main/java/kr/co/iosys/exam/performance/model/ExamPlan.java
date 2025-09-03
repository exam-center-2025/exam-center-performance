package kr.co.iosys.exam.performance.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * AIDEV-NOTE: 시험 계획 정보 모델 - 데이터베이스 exam_plans 테이블과 매핑
 * 실제 테이블 구조에 맞게 수정
 */
@Entity
@Table(name = "exam_plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamPlan {
    
    @Id
    @Column(name = "plan_id")
    private Long planId;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "title")
    private String title;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "campus")
    private String campus;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "enable_yn")
    @Builder.Default
    private Character enableYn = 'Y';
}