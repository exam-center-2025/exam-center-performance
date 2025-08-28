package kr.co.iosys.exam.performance.exception;

/**
 * AIDEV-NOTE: 성능 테스트 관련 예외 클래스
 */
public class PerformanceTestException extends RuntimeException {

    private final String errorCode;

    public PerformanceTestException(String message) {
        super(message);
        this.errorCode = "PERFORMANCE_TEST_ERROR";
    }

    public PerformanceTestException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public PerformanceTestException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "PERFORMANCE_TEST_ERROR";
    }

    public PerformanceTestException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}