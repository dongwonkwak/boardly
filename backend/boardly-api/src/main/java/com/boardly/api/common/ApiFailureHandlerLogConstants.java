package com.boardly.api.common;

/**
 * ApiFailureHandler에서 사용하는 로그 관련 상수들
 */
public final class ApiFailureHandlerLogConstants {

    private ApiFailureHandlerLogConstants() {
        // 유틸리티 클래스이므로 인스턴스화 방지
    }

    // ======================== 로그 메시지 상수 ========================
    public static final String LOG_INPUT_ERROR = "입력 오류: errorCode={}, messageKey={}, violations={}";
    public static final String LOG_UNHANDLED_FAILURE = "Unhandled Failure type: {}";
    public static final String LOG_REQUEST_PATH_ERROR = "요청 경로를 가져오는 중 오류 발생: {}";
    public static final String LOG_MESSAGE_RETRIEVAL_ERROR = "메시지 조회 중 오류 발생: messageKey={}, error={}";
    public static final String LOG_REQUEST_ATTRIBUTES_NULL = "RequestContextHolder에서 요청 속성을 가져올 수 없습니다.";
    public static final String LOG_UNSUPPORTED_REQUEST_ATTRIBUTES = "ServletRequestAttributes가 아닌 다른 타입의 RequestAttributes입니다: {}";
    public static final String LOG_SECURITY_POLICY_ERROR = "보안 정책으로 인해 요청 경로를 가져올 수 없습니다: {}";

    // ======================== 로그 접두사 상수 ========================
    public static final String LOG_PREFIX_PERMISSION_DENIED = "권한 거부";
    public static final String LOG_PREFIX_NOT_FOUND = "리소스 미발견";
    public static final String LOG_PREFIX_RESOURCE_CONFLICT = "리소스 충돌";
    public static final String LOG_PREFIX_PRECONDITION_FAILED = "전제 조건 실패";
    public static final String LOG_PREFIX_BUSINESS_RULE_VIOLATION = "비즈니스 룰 위반";
    public static final String LOG_PREFIX_INTERNAL_ERROR = "내부 서버 오류";

    // ======================== 로그 포맷 상수 ========================
    public static final String LOG_FORMAT_ERROR_SUMMARY = "{}: errorCode={}, messageKey={}";
}
