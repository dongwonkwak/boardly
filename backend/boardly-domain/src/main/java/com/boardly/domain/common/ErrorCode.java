package com.boardly.domain.common;

/**
 * 도메인 에러코드 정의
 * 모든 Failure 타입에서 사용하는 에러코드를 중앙에서 관리
 */
public enum ErrorCode {
    // ======================== 400 Bad Request ========================
    VALIDATION_ERROR("VALIDATION_ERROR"),

    // ======================== 403 Forbidden ========================
    PERMISSION_DENIED("PERMISSION_DENIED"),

    // ======================== 404 Not Found ========================
    NOT_FOUND("NOT_FOUND"),

    // ======================== 409 Conflict ========================
    RESOURCE_CONFLICT("RESOURCE_CONFLICT"),

    // ======================== 412 Precondition Failed ========================
    PRECONDITION_FAILED("PRECONDITION_FAILED"),

    // ======================== 422 Unprocessable Entity ========================
    BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION"),

    // ======================== 500 Internal Server Error ========================
    INTERNAL_ERROR("INTERNAL_ERROR");

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
