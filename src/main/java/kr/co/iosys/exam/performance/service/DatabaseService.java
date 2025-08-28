package kr.co.iosys.exam.performance.service;

import kr.co.iosys.exam.performance.config.PerformanceTestProperties;
import kr.co.iosys.exam.performance.exception.PerformanceTestException;
import kr.co.iosys.exam.performance.model.ExamPlan;
import kr.co.iosys.exam.performance.model.ExamGroup;
import kr.co.iosys.exam.performance.repository.ExamPlanRepository;
import kr.co.iosys.exam.performance.repository.ExamGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AIDEV-NOTE: PostgreSQL 데이터베이스에서 시험 관련 정보 조회 서비스
 * Gatling 테스트에서 사용할 사용자 데이터와 시험 정보를 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseService {

    private final ExamPlanRepository examPlanRepository;
    private final ExamGroupRepository examGroupRepository;
    private final PerformanceTestProperties properties;

    /**
     * 시험 계획 목록 조회
     */
    public List<ExamPlan> getExamPlans() {
        try {
            return examPlanRepository.findByStatusOrderByCreatedAtDesc("ACTIVE");
        } catch (Exception e) {
            log.error("시험 계획 목록 조회 실패", e);
            throw new PerformanceTestException(
                    "시험 계획 목록 조회 실패: " + e.getMessage(),
                    "DATABASE_ERROR",
                    e);
        }
    }

    /**
     * 특정 계획의 그룹 목록 조회
     */
    public List<ExamGroup> getExamGroups(Long planId) {
        try {
            return examGroupRepository.findByPlanIdOrderByName(planId);
        } catch (Exception e) {
            log.error("그룹 목록 조회 실패", e);
            throw new PerformanceTestException(
                    "그룹 목록 조회 실패: " + e.getMessage(),
                    "DATABASE_ERROR",
                    e);
        }
    }

    /**
     * 성능 테스트용 사용자 데이터 조회 (JDBC 동기 방식)
     * AIDEV-NOTE: Gatling에서 사용하기 위해 동기식으로 구현
     */
    public List<Map<String, Object>> getTestUsers(Long planId, int limit) {
        List<Map<String, Object>> users = new ArrayList<>();
        
        String sql = """
            SELECT 
                egm.user_id,
                eu.user_name,
                eu.access_key
            FROM exam_group_members egm
            JOIN exam_users eu ON egm.user_id = eu.user_id AND egm.plan_id = eu.plan_id
            WHERE egm.plan_id = ? 
                AND egm.run_type = 'TEST'
                AND egm.group_role = 'EXAMINEE'
                AND eu.access_key IS NOT NULL
            ORDER BY egm.user_id
            LIMIT ?
            """;

        PerformanceTestProperties.Database dbConfig = properties.getDatabase();
        
        try (Connection conn = DriverManager.getConnection(
                dbConfig.getJdbcUrl(), 
                dbConfig.getUsername(), 
                dbConfig.getPassword());
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, planId);
            stmt.setInt(2, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("userId", rs.getString("user_id"));
                    user.put("userName", rs.getString("user_name"));
                    user.put("accessKey", rs.getString("access_key"));
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            log.error("테스트 사용자 데이터 조회 실패", e);
            throw new PerformanceTestException(
                    "테스트 사용자 데이터 조회 실패: " + e.getMessage(),
                    "DATABASE_ERROR",
                    e);
        }
        
        log.info("조회된 테스트 사용자 수: {}", users.size());
        return users;
    }

    /**
     * 데이터베이스 연결 상태 확인
     */
    public boolean checkConnection() {
        try {
            examPlanRepository.count(); // 간단한 쿼리로 연결 확인
            log.info("데이터베이스 연결 상태: 정상");
            return true;
        } catch (Exception e) {
            log.error("데이터베이스 연결 실패", e);
            return false;
        }
    }
}