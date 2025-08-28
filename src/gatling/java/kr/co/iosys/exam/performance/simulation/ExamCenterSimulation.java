package kr.co.iosys.exam.performance.simulation;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import io.gatling.javaapi.jdbc.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import static io.gatling.javaapi.jdbc.JdbcDsl.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.util.*;
import java.sql.*;
import java.text.SimpleDateFormat;

/**
 * AIDEV-NOTE: Gatling 성능 테스트 시뮬레이션
 * PostgreSQL DB에서 사용자 조회 후 WebSocket 통신 테스트
 * 시스템 속성: plan_id, run_type, user_count 지원
 */
public class ExamCenterSimulation extends Simulation {

    // 시스템 속성에서 동적 매개변수 사용
    private static final String PLAN_ID = System.getProperty("plan_id", "1");
    private static final String RUN_TYPE = System.getProperty("run_type", "TEST");
    private static final int USER_COUNT = Integer.parseInt(System.getProperty("user_count", "10"));
    
    private static final String DB_URL = "jdbc:postgresql://192.168.100.105:5432/exam_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // HTTP 설정
    private HttpProtocolBuilder httpProtocol = http
        .baseUrl("http://localhost:8091")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling/시험센터");
    
    // WebSocket 설정
    private HttpProtocolBuilder wsProtocol = http
        .baseUrl("ws://localhost:8099");

    /**
     * AIDEV-NOTE: PostgreSQL DB에서 exam_users 조회
     * plan_id, run_type 기반으로 사용자 데이터 생성
     */
    private Iterator<Map<String, Object>> getUsersFromDB() {
        List<Map<String, Object>> users = new ArrayList<>();
        
        // 동적 쿼리 생성
        String query = """
            SELECT 
                egm.user_id,
                eu.user_name,
                eu.access_key
            FROM exam_group_members egm
            JOIN exam_users eu ON egm.user_id = eu.user_id AND egm.plan_id = eu.plan_id
            WHERE egm.plan_id = ? 
                AND egm.run_type = ?
                AND egm.group_role = 'EXAMINEE'
                AND eu.access_key IS NOT NULL
            ORDER BY egm.user_id
            LIMIT 10000
        """;
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            // 동적 매개변수 설정
            stmt.setInt(1, Integer.parseInt(PLAN_ID));
            stmt.setString(2, RUN_TYPE);
            
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
            System.err.println("❌ 데이터베이스 조회 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("데이터베이스 연결 실패. PostgreSQL 드라이버를 확인하세요.", e);
        }
        
        System.out.printf("✅ 데이터베이스 연결 성공: plan_id=%s, run_type=%s, 사용자 수=%d\n", 
                         PLAN_ID, RUN_TYPE, users.size());
        
        return users.iterator();
    }

    /**
     * AIDEV-NOTE: STATUS_MSG 형식 준수
     * 기존 multi-user-performance-test.js와 동일한 방식
     */
    private String createStatusMessage(String userId, String stageKey, String stat, boolean isComplete) {
        java.util.Date now = new java.util.Date();
        
        Map<String, Object> message = new HashMap<>();
        message.put("name", "STATUS_MSG");
        message.put("from", userId.toString());
        message.put("to", new ArrayList<>());
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("mode", RUN_TYPE);
        payload.put("taskType", "UPDATE_EXAM_PROGRESS");
        payload.put("planId", Integer.parseInt(PLAN_ID));
        payload.put("userId", userId.toString());
        payload.put("runType", RUN_TYPE);
        payload.put("stageKey", stageKey);
        
        Map<String, Object> value = new HashMap<>();
        value.put("stat", stat);
        value.put("starttime", formatDate(now));
        
        if (isComplete) {
            value.put("endtime", formatDate(now));
            value.put("endflag", "Y");
            if ("TEST_0".equals(stageKey)) {
                value.put("examstat", "2");
            }
        }
        
        payload.put("value", value);
        message.put("payload", payload);
        
        Map<String, Object> meta = new HashMap<>();
        meta.put("timestampCliente", System.currentTimeMillis());
        meta.put("timestampLocal", System.currentTimeMillis());
        
        List<Map<String, Object>> traceList = new ArrayList<>();
        Map<String, Object> trace = new HashMap<>();
        trace.put("actor", "Gatling부하테스트");
        trace.put("action", "전송");
        trace.put("datetime", toISOString(now));
        traceList.add(trace);
        meta.put("traceList", traceList);
        
        message.put("meta", meta);
        
        try {
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    private String createStatusMessage(String userId, String stageKey, String stat) {
        return createStatusMessage(userId, stageKey, stat, false);
    }
    
    /**
     * AIDEV-NOTE: 날짜 포맷 함수 - yyyyMMddHHmmss
     */
    private String formatDate(java.util.Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(date);
    }
    
    /**
     * AIDEV-NOTE: ISO 8601 형식으로 변환
     */
    private String toISOString(java.util.Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    /**
     * AIDEV-NOTE: 주요 시나리오 - SYSTEM_CHECK → INFO_0 → TEST_0
     * Security API 호출, WebSocket 연결, STATUS_MSG 전송 순서
     */
    private ScenarioBuilder normalUserScenario = scenario("일반 사용자 시나리오")
        .feed(getUsersFromDB())
        
        // 사용자별 랜덤 시작 지연 (0~2000ms)
        .pause(Duration.ofMillis(0), Duration.ofMillis(2000))
        
        // 1. Security API - Access Key 인증
        .exec(
            http("보안 인증 - 액세스키 로그인")
                .post("/security/auth/keyLogin")
                .body(StringBody("{\"access_key\": \"#{accessKey}\"}"))
                .check(status().is(200))
                .check(jsonPath("$.body.accessToken").saveAs("authToken"))
                .check(jsonPath("$.body.examUserId").optional().saveAs("examUserId"))
        )
        .pause(Duration.ofMillis(500), Duration.ofMillis(1500))
        
        // 2. WebSocket 연결
        .exec(ws("웹소켓 연결")
            .connect("ws://localhost:8099/ws?authToken=#{authToken}&runType=" + RUN_TYPE)
        )
        .pause(Duration.ofMillis(100), Duration.ofMillis(500))
        
        // 3. STATUS_MSG 시퀀스
        // 3-1. SYSTEM_CHECK
        .exec(session -> {
            String userId = session.getString("userId");
            String msg = createStatusMessage(userId, "SYSTEM_CHECK", "1");
            return session.set("systemCheckMsg", msg);
        })
        .exec(ws("시스템 체크 메시지 전송")
            .sendText("#{systemCheckMsg}")
        )
        .pause(Duration.ofMillis(200), Duration.ofMillis(800))
        
        // 3-2. INFO_0 시작
        .exec(session -> {
            String userId = session.getString("userId");
            String msg = createStatusMessage(userId, "INFO_0", "1");
            return session.set("infoStartMsg", msg);
        })
        .exec(ws("안내사항 시작 메시지 전송")
            .sendText("#{infoStartMsg}")
        )
        .pause(Duration.ofMillis(500), Duration.ofMillis(1500))
        
        // 3-3. INFO_0 완료
        .exec(session -> {
            String userId = session.getString("userId");
            String msg = createStatusMessage(userId, "INFO_0", "2", true);
            return session.set("infoCompleteMsg", msg);
        })
        .exec(ws("안내사항 완료 메시지 전송")
            .sendText("#{infoCompleteMsg}")
        )
        .pause(Duration.ofMillis(300), Duration.ofMillis(1000))
        
        // 3-4. TEST_0 시작
        .exec(session -> {
            String userId = session.getString("userId");
            String msg = createStatusMessage(userId, "TEST_0", "1");
            return session.set("testStartMsg", msg);
        })
        .exec(ws("시험 시작 메시지 전송")
            .sendText("#{testStartMsg}")
        )
        .pause(Duration.ofMillis(1000), Duration.ofMillis(3000))
        
        // 3-5. TEST_0 완료
        .exec(session -> {
            String userId = session.getString("userId");
            String msg = createStatusMessage(userId, "TEST_0", "2", true);
            return session.set("testCompleteMsg", msg);
        })
        .exec(ws("시험 완료 메시지 전송")
            .sendText("#{testCompleteMsg}")
        )
        .pause(Duration.ofMillis(500), Duration.ofMillis(1500))
        
        // 4. WebSocket 종료
        .exec(ws("웹소켓 연결 종료").close())
        
        // 5. Security API 로그아웃
        .exec(
            http("보안 로그아웃")
                .post("/security/auth/logout")
                .header("Authorization", "Bearer #{authToken}")
        );

    /**
     * AIDEV-NOTE: 시뮬레이션 설정 및 실행
     * 동적 사용자 수 설정 지원
     */
    {
        System.out.printf("🚀 Gatling 시뮬레이션 시작: plan_id=%s, run_type=%s, user_count=%d\n", 
                         PLAN_ID, RUN_TYPE, USER_COUNT);
        
        setUp(
            normalUserScenario.injectOpen(
                // 점진적 증가로 서버 부담 감소
                nothingFor(Duration.ofSeconds(2)),                    // 시작 전 2초 대기
                atOnceUsers(Math.min(10, USER_COUNT)),               // 워밍업: 10명 또는 전체 사용자
                nothingFor(Duration.ofSeconds(3)),                    // 3초 대기
                rampUsers(Math.min(USER_COUNT / 2, 50)).during(Duration.ofSeconds(10)), // 10초간 절반 사용자 증가
                nothingFor(Duration.ofSeconds(3)),                    // 3초 대기
                rampUsers(USER_COUNT).during(Duration.ofSeconds(20)), // 20초간 전체 사용자 증가
                nothingFor(Duration.ofSeconds(3)),                    // 3초 대기
                constantUsersPerSec(2).during(Duration.ofSeconds(30)), // 30초간 초당 2명
                nothingFor(Duration.ofSeconds(3)),                    // 3초 대기
                rampUsers(Math.min(USER_COUNT / 2, 50)).during(Duration.ofSeconds(10))  // 10초간 절반 사용자 증가
            )
        ).protocols(httpProtocol)
         .assertions(
            global().responseTime().max().lt(10000),
            global().successfulRequests().percent().gt(85.0)  // 목표를 85%로 조정
        );
    }
}