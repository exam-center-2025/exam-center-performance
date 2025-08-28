package kr.co.iosys.exam.performance.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

/**
 * AIDEV-NOTE: 동적 시험 센터 시뮬레이션
 * 기존 ExamCenterSimulation.java를 기반으로 하되, 프로그래매틱 실행을 위해 설정을 동적으로 받음
 */
public class DynamicExamSimulation extends Simulation {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static Map<String, Object> testConfiguration = new HashMap<>();

    /**
     * 테스트 설정을 외부에서 주입받기 위한 메서드
     */
    public static void setTestConfiguration(Map<String, Object> config) {
        testConfiguration = config;
    }

    // HTTP 프로토콜 설정 (동적)
    private HttpProtocolBuilder httpProtocol;
    private HttpProtocolBuilder wsProtocol;

    public DynamicExamSimulation() {
        initializeProtocols();
    }

    private void initializeProtocols() {
        String baseUrl = (String) testConfiguration.getOrDefault("baseUrl", "http://localhost:8091");
        String wsUrl = (String) testConfiguration.getOrDefault("websocketUrl", "ws://localhost:8099");

        httpProtocol = http
            .baseUrl(baseUrl)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling/시험센터-성능테스트");

        wsProtocol = http
            .baseUrl(wsUrl);
    }

    /**
     * 데이터베이스에서 사용자 목록 조회
     */
    private Iterator<Map<String, Object>> getUsersFromDB() {
        List<Map<String, Object>> users = new ArrayList<>();

        // 설정에서 데이터베이스 정보 가져오기
        String dbUrl = (String) testConfiguration.get("dbUrl");
        String dbUsername = (String) testConfiguration.get("dbUsername");
        String dbPassword = (String) testConfiguration.get("dbPassword");
        Long planId = ((Number) testConfiguration.get("planId")).longValue();
        String runType = (String) testConfiguration.getOrDefault("runType", "TEST");

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

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, planId);
            stmt.setString(2, runType);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("userId", rs.getString("user_id"));
                    user.put("userName", rs.getString("user_name"));
                    user.put("accessKey", rs.getString("access_key"));
                    users.add(user);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ 데이터베이스 조회 실패: " + e.getMessage());
            throw new RuntimeException("데이터베이스 연결 실패", e);
        }

        System.out.println("✅ 조회된 테스트 사용자 수: " + users.size());
        return users.iterator();
    }

    /**
     * STATUS_MSG 생성 함수
     */
    private String createStatusMessage(String userId, String stageKey, String stat, boolean isComplete) {
        Date now = new Date();
        Long planId = ((Number) testConfiguration.get("planId")).longValue();
        String runType = (String) testConfiguration.getOrDefault("runType", "TEST");

        Map<String, Object> message = new HashMap<>();
        message.put("name", "STATUS_MSG");
        message.put("from", userId);
        message.put("to", new ArrayList<>());

        Map<String, Object> payload = new HashMap<>();
        payload.put("mode", runType);
        payload.put("taskType", "UPDATE_EXAM_PROGRESS");
        payload.put("planId", planId);
        payload.put("userId", userId);
        payload.put("runType", runType);
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
        trace.put("actor", "성능테스트");
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

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(date);
    }

    private String toISOString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    /**
     * 시나리오 정의 (동적 설정 적용)
     */
    private ScenarioBuilder createNormalUserScenario() {
        String wsUrl = (String) testConfiguration.getOrDefault("websocketUrl", "ws://localhost:8099");
        String runType = (String) testConfiguration.getOrDefault("runType", "TEST");

        return scenario("동적 시험 센터 사용자 시나리오")
            .feed(getUsersFromDB())

            // 사용자별 랜덤 시작 지연
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
                .connect(wsUrl + "/ws?authToken=#{authToken}&runType=" + runType)
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
    }

    {
        // 동적 시뮬레이션 설정
        Integer maxUsers = (Integer) testConfiguration.getOrDefault("maxUsers", 100);
        Integer rampUpSeconds = (Integer) testConfiguration.getOrDefault("rampUpDurationSeconds", 60);
        Integer testDurationSeconds = (Integer) testConfiguration.getOrDefault("testDurationSeconds", 300);

        System.out.println("🚀 동적 시뮬레이션 시작");
        System.out.println("  - 최대 사용자: " + maxUsers);
        System.out.println("  - 램프업 시간: " + rampUpSeconds + "초");
        System.out.println("  - 테스트 시간: " + testDurationSeconds + "초");
        System.out.println("  - 대상 서버: " + testConfiguration.get("baseUrl"));

        setUp(
            createNormalUserScenario().injectOpen(
                nothingFor(Duration.ofSeconds(2)),
                atOnceUsers(Math.min(10, maxUsers / 10)),
                nothingFor(Duration.ofSeconds(3)),
                rampUsers(maxUsers / 2).during(Duration.ofSeconds(rampUpSeconds / 2)),
                nothingFor(Duration.ofSeconds(3)),
                rampUsers(maxUsers).during(Duration.ofSeconds(rampUpSeconds)),
                nothingFor(Duration.ofSeconds(3)),
                constantUsersPerSec(Math.max(1, maxUsers / 50.0))
                    .during(Duration.ofSeconds(Math.min(testDurationSeconds, 60)))
            )
        ).protocols(httpProtocol)
         .assertions(
            global().responseTime().max().lt(15000),
            global().successfulRequests().percent().gt(80.0)
        );
    }
}