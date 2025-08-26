package com.boardly.domain.common;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

@Getter
@RequiredArgsConstructor
public abstract class Failure {

    private final String messageKey; // 기존 message를 messageKey로 변경
    private final String errorCode; // 공통 errorCode 필드 추가

    /**
     * 필드 위반 정보
     */
    @Value
    @Builder
    public static class FieldViolation {
        String field;
        String messageKey; // 기존 message를 messageKey로 변경
        Object rejectedValue;
    }

    // ======================== 400 Bad Request ========================
    /**
     * 입력 형식/데이터 오류 실패 (400 Bad Request)
     * - 이메일 형식 오류
     * - 필수값 누락
     * - 잘못된 데이터 타입
     * - JSON 형식 오류
     */
    @Getter
    public static class InputError extends Failure {
        private final List<FieldViolation> violations;

        public InputError(String messageKey, List<FieldViolation> violations) {
            super(messageKey, ErrorCode.VALIDATION_ERROR.getCode());
            this.violations = violations != null ? violations : List.of();
        }
    }

    // ======================== 403 Forbidden ========================
    /**
     * 권한 기반 비즈니스 룰 위반 (403 Forbidden)
     * - 다른 사용자 카드 수정
     * - 읽기 전용 리소스 수정
     * - 역할 기반 접근 제한
     */
    @Getter
    public static class PermissionDenied extends Failure {
        private final Object context;

        public PermissionDenied(String messageKey, Object context) {
            super(messageKey, ErrorCode.PERMISSION_DENIED.getCode());
            this.context = context;
        }
    }

    // ======================== 404 Not Found ========================
    /**
     * 리소스 미발견 실패 (404 Not Found)
     */
    @Getter
    public static class NotFound extends Failure {
        private final Object context;

        public NotFound(String messageKey, Object context) {
            super(messageKey, ErrorCode.NOT_FOUND.getCode());
            this.context = context;
        }
    }

    // ======================== 409 Conflict ========================
    /**
     * 리소스 충돌 실패 (409 Conflict)
     * - 이메일 중복
     * - 동시 수정 충돌
     * - 중복 생성 시도
     */
    @Getter
    public static class ResourceConflict extends Failure {
        private final Object context;

        public ResourceConflict(String messageKey, Object context) {
            super(messageKey, ErrorCode.RESOURCE_CONFLICT.getCode());
            this.context = context;
        }
    }

    // ======================== 412 Precondition Failed ========================
    /**
     * 전제 조건 실패 (412 Precondition Failed)
     * - 필수 설정 누락
     * - 종속성 미충족
     * - If-Match 헤더 불일치
     */
    @Getter
    public static class PreconditionFailed extends Failure {
        private final Object context;

        public PreconditionFailed(String messageKey, Object context) {
            super(messageKey, ErrorCode.PRECONDITION_FAILED.getCode());
            this.context = context;
        }
    }

    // ======================== 422 Unprocessable Entity ========================
    /**
     * 비즈니스 룰 위반 실패 (422 Unprocessable Entity)
     * - 입력은 유효하지만 비즈니스 규칙 위반
     * - 아카이브된 카드 수정
     * - 카드 개수 제한 초과
     * - 상태 충돌 (다른 사용자가 아닌 리소스 상태 문제)
     */
    @Getter
    public static class BusinessRuleViolation extends Failure {
        private final Object context;

        public BusinessRuleViolation(String messageKey, Object context) {
            super(messageKey, ErrorCode.BUSINESS_RULE_VIOLATION.getCode());
            this.context = context;
        }
    }

    // ======================== 500 Internal Server Error ========================
    /**
     * 내부 서버 오류 (500 Internal Server Error)
     */
    @Getter
    public static class InternalError extends Failure {
        private final Object context;

        public InternalError(String messageKey, Object context) {
            super(messageKey, ErrorCode.INTERNAL_ERROR.getCode());
            this.context = context;
        }

        public InternalError(String messageKey) {
            this(messageKey, null);
        }
    }

    // ======================== Factory Methods ========================

    /**
     * 입력 형식/데이터 오류 생성 (400 Bad Request)
     */
    public static InputError ofInputError(String messageKey, List<FieldViolation> violations) {
        return new InputError(messageKey, violations);
    }

    public static InputError ofInputError(String messageKey) {
        return new InputError(messageKey, null);
    }

    public static InputError ofValidation(String messageKey, List<FieldViolation> violations) {
        return new InputError(messageKey, violations);
    }

    /**
     * 권한 거부 생성 (403 Forbidden)
     */
    public static PermissionDenied ofPermissionDenied(String messageKey, Object context) {
        return new PermissionDenied(messageKey, context);
    }

    public static PermissionDenied ofPermissionDenied(String messageKey) {
        return new PermissionDenied(messageKey, null);
    }

    public static PermissionDenied ofForbidden() {
        return new PermissionDenied("error.access.denied", null);
    }

    /**
     * 리소스 미발견 생성 (404 Not Found)
     */
    public static NotFound ofNotFound(String messageKey, Object context) {
        return new NotFound(messageKey, context);
    }

    public static NotFound ofNotFound(String messageKey) {
        return new NotFound(messageKey, null);
    }

    /**
     * 리소스 충돌 생성 (409 Conflict)
     */
    public static ResourceConflict ofResourceConflict(String messageKey, Object context) {
        return new ResourceConflict(messageKey, context);
    }

    public static ResourceConflict ofConflict(String messageKey) {
        return new ResourceConflict(messageKey, null);
    }

    /**
     * 전제 조건 실패 생성 (412 Precondition Failed)
     */
    public static PreconditionFailed ofPreconditionFailed(String messageKey, Object context) {
        return new PreconditionFailed(messageKey, context);
    }

    public static PreconditionFailed ofPreconditionFailed(String messageKey) {
        return new PreconditionFailed(messageKey, null);
    }

    /**
     * 비즈니스 룰 위반 생성 (422 Unprocessable Entity)
     */
    public static BusinessRuleViolation ofBusinessRuleViolation(String messageKey, Object context) {
        return new BusinessRuleViolation(messageKey, context);
    }

    public static BusinessRuleViolation ofBusinessRuleViolation(String messageKey) {
        return new BusinessRuleViolation(messageKey, null);
    }

    /**
     * 내부 서버 오류 생성 (500 Internal Server Error)
     */
    public static InternalError ofInternalError(String messageKey, Object context) {
        return new InternalError(messageKey, context);
    }

    public static InternalError ofInternalServerError(String messageKey) {
        return new InternalError(messageKey, null);
    }

    // ======================== 기존 메서드 호환성 유지 (Deprecated) ========================
    // 기존 코드가 동작할 수 있도록 유지하되, 파라미터를 messageKey로 해석

    /**
     * @deprecated 이제 message가 아닌 messageKey를 전달해야 합니다
     */
    @Deprecated
    public static InputError ofValidationFailure(String messageKey, List<FieldViolation> violations) {
        return ofValidation(messageKey, violations);
    }

    /**
     * @deprecated 이제 message가 아닌 messageKey를 전달해야 합니다
     */
    @Deprecated
    public static ResourceConflict ofConflictFailure(String messageKey) {
        return ofConflict(messageKey);
    }

    /**
     * @deprecated 이제 message가 아닌 messageKey를 전달해야 합니다
     */
    @Deprecated
    public static NotFound ofNotFoundFailure(String messageKey) {
        return ofNotFound(messageKey);
    }

    /**
     * @deprecated 이제 message가 아닌 messageKey를 전달해야 합니다
     */
    @Deprecated
    public static PermissionDenied ofForbiddenFailure(String messageKey) {
        return ofPermissionDenied(messageKey);
    }
}