package kr.co.iosys.exam.performance.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * AIDEV-NOTE: 시험 그룹 정보 모델 - 데이터베이스 exam_groups 테이블과 매핑
 * 실제 테이블 구조에 맞게 수정
 */
@Entity
@Table(name = "exam_groups")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamGroup {
    
    @Id
    @Column(name = "group_id")
    private Long groupId;
    
    @Column(name = "plan_id")
    private Long planId;
    
    @Column(name = "run_type")
    private String runType;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "org_rtmcode")
    private String orgRtmcode;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}