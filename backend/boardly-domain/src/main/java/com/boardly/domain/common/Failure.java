package com.boardly.domain.common;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.util.List;

@Getter
public abstract class Failure {

    private final String messageKey; // 기존 message를 messageKey로 변경
    private final String errorCode; // 공통 errorCode 필드 추가
    private final Object[] messageArgs; // 국제화 메시지 인자

    protected Failure(String messageKey, String errorCode, Object[] messageArgs) {
        this.messageKey = messageKey;
        this.errorCode = errorCode;
        this.messageArgs = messageArgs;
    }

    /**
     * 필드 위반 정보
     */
    @Value
    @Builder
    public static class FieldViolation {
        String field;
        String messageKey; // 기존 message를 messageKey로 변경
        Object rejectedValue;
        Object[] messageArgs; // 국제화 메시지 인자

        // 기존 호환성을 위한 정적 팩토리 메서드
        public static FieldViolation of(String field, String messageKey, Object rejectedValue) {
            return new FieldViolation(field, messageKey, rejectedValue, null);
        }

        public static FieldViolation of(String field, String messageKey, Object rejectedValue, Object... messageArgs) {
            return new FieldViolation(field, messageKey, rejectedValue, messageArgs);
        }
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
            super(messageKey, ErrorCode.VALIDATION_ERROR.getCode(), null);
            this.violations = violations != null ? violations : List.of();
        }

        public InputError(String messageKey, List<FieldViolation> violations, Object... messageArgs) {
            super(messageKey, ErrorCode.VALIDATION_ERROR.getCode(), messageArgs);
            this.violations = violations != null ? violations : List.of();
        }

        public InputError(String messageKey, String errorCode, List<FieldViolation> violations) {
            super(messageKey, errorCode, null);
            this.violations = violations != null ? violations : List.of();
        }

        public InputError(String messageKey, String errorCode, List<FieldViolation> violations, Object... messageArgs) {
            super(messageKey, errorCode, messageArgs);
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
            super(messageKey, ErrorCode.PERMISSION_DENIED.getCode(), null);
            this.context = context;
        }

        public PermissionDenied(String messageKey, Object context, Object... messageArgs) {
            super(messageKey, ErrorCode.PERMISSION_DENIED.getCode(), messageArgs);
            this.context = context;
        }

        public PermissionDenied(String messageKey, String errorCode, Object context) {
            super(messageKey, errorCode, null);
            this.context = context;
        }

        public PermissionDenied(String messageKey, String errorCode, Object context, Object... messageArgs) {
            super(messageKey, errorCode, messageArgs);
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
            super(messageKey, ErrorCode.NOT_FOUND.getCode(), null);
            this.context = context;
        }

        public NotFound(String messageKey, Object context, Object... messageArgs) {
            super(messageKey, ErrorCode.NOT_FOUND.getCode(), messageArgs);
            this.context = context;
        }

        public NotFound(String messageKey, String errorCode, Object context) {
            super(messageKey, errorCode, null);
            this.context = context;
        }

        public NotFound(String messageKey, String errorCode, Object context, Object... messageArgs) {
            super(messageKey, errorCode, messageArgs);
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
            super(messageKey, ErrorCode.RESOURCE_CONFLICT.getCode(), null);
            this.context = context;
        }

        public ResourceConflict(String messageKey, Object context, Object... messageArgs) {
            super(messageKey, ErrorCode.RESOURCE_CONFLICT.getCode(), messageArgs);
            this.context = context;
        }

        public ResourceConflict(String messageKey, String errorCode, Object context) {
            super(messageKey, errorCode, null);
            this.context = context;
        }

        public ResourceConflict(String messageKey, String errorCode, Object context, Object... messageArgs) {
            super(messageKey, errorCode, messageArgs);
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
            super(messageKey, ErrorCode.PRECONDITION_FAILED.getCode(), null);
            this.context = context;
        }

        public PreconditionFailed(String messageKey, Object context, Object... messageArgs) {
            super(messageKey, ErrorCode.PRECONDITION_FAILED.getCode(), messageArgs);
            this.context = context;
        }

        public PreconditionFailed(String messageKey, String errorCode, Object context) {
            super(messageKey, errorCode, null);
            this.context = context;
        }

        public PreconditionFailed(String messageKey, String errorCode, Object context, Object... messageArgs) {
            super(messageKey, errorCode, messageArgs);
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
            super(messageKey, ErrorCode.BUSINESS_RULE_VIOLATION.getCode(), null);
            this.context = context;
        }

        public BusinessRuleViolation(String messageKey, Object context, Object... messageArgs) {
            super(messageKey, ErrorCode.BUSINESS_RULE_VIOLATION.getCode(), messageArgs);
            this.context = context;
        }

        public BusinessRuleViolation(String messageKey, String errorCode, Object context) {
            super(messageKey, errorCode, null);
            this.context = context;
        }

        public BusinessRuleViolation(String messageKey, String errorCode, Object context, Object... messageArgs) {
            super(messageKey, errorCode, messageArgs);
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
            super(messageKey, ErrorCode.INTERNAL_ERROR.getCode(), null);
            this.context = context;
        }

        public InternalError(String messageKey) {
            this(messageKey, (Object) null);
        }

        public InternalError(String messageKey, Object context, Object... messageArgs) {
            super(messageKey, ErrorCode.INTERNAL_ERROR.getCode(), messageArgs);
            this.context = context;
        }

        public InternalError(String messageKey, Object... messageArgs) {
            this(messageKey, null, messageArgs);
        }

        public InternalError(String messageKey, String errorCode, Object context) {
            super(messageKey, errorCode, null);
            this.context = context;
        }

        public InternalError(String messageKey, String errorCode) {
            this(messageKey, errorCode, (Object) null);
        }

        public InternalError(String messageKey, String errorCode, Object context, Object... messageArgs) {
            super(messageKey, errorCode, messageArgs);
            this.context = context;
        }

        public InternalError(String messageKey, String errorCode, Object... messageArgs) {
            this(messageKey, errorCode, null, messageArgs);
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

    // messageArgs를 지원하는 오버로드 메서드들
    public static InputError ofInputError(String messageKey, List<FieldViolation> violations, Object... messageArgs) {
        return new InputError(messageKey, violations, messageArgs);
    }

    public static InputError ofInputError(String messageKey, Object... messageArgs) {
        return new InputError(messageKey, null, messageArgs);
    }

    public static InputError ofValidation(String messageKey, List<FieldViolation> violations, Object... messageArgs) {
        return new InputError(messageKey, violations, messageArgs);
    }

    // errorCode를 지원하는 오버로드 메서드들
    public static InputError ofInputErrorWithErrorCode(String messageKey, String errorCode,
            List<FieldViolation> violations) {
        return new InputError(messageKey, errorCode, violations);
    }

    public static InputError ofInputErrorWithErrorCode(String messageKey, String errorCode) {
        return new InputError(messageKey, errorCode, null);
    }

    public static InputError ofValidationWithErrorCode(String messageKey, String errorCode,
            List<FieldViolation> violations) {
        return new InputError(messageKey, errorCode, violations);
    }

    public static InputError ofInputErrorWithErrorCode(String messageKey, String errorCode,
            List<FieldViolation> violations, Object... messageArgs) {
        return new InputError(messageKey, errorCode, violations, messageArgs);
    }

    public static InputError ofInputErrorWithErrorCode(String messageKey, String errorCode, Object... messageArgs) {
        return new InputError(messageKey, errorCode, null, messageArgs);
    }

    public static InputError ofValidationWithErrorCode(String messageKey, String errorCode,
            List<FieldViolation> violations, Object... messageArgs) {
        return new InputError(messageKey, errorCode, violations, messageArgs);
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

    // messageArgs를 지원하는 오버로드 메서드들
    public static PermissionDenied ofPermissionDenied(String messageKey, Object context, Object... messageArgs) {
        return new PermissionDenied(messageKey, context, messageArgs);
    }

    public static PermissionDenied ofPermissionDenied(String messageKey, Object... messageArgs) {
        return new PermissionDenied(messageKey, (Object) null, messageArgs);
    }

    public static PermissionDenied ofForbidden(Object... messageArgs) {
        return new PermissionDenied("error.access.denied", (Object) null, messageArgs);
    }

    // errorCode를 지원하는 오버로드 메서드들
    public static PermissionDenied ofPermissionDeniedWithErrorCode(String messageKey, String errorCode,
            Object context) {
        return new PermissionDenied(messageKey, errorCode, context);
    }

    public static PermissionDenied ofPermissionDeniedWithErrorCode(String messageKey, String errorCode) {
        return new PermissionDenied(messageKey, errorCode, (Object) null);
    }

    public static PermissionDenied ofForbidden(String errorCode) {
        return new PermissionDenied("error.access.denied", errorCode, (Object) null);
    }

    public static PermissionDenied ofPermissionDeniedWithErrorCode(String messageKey, String errorCode, Object context,
            Object... messageArgs) {
        return new PermissionDenied(messageKey, errorCode, context, messageArgs);
    }

    public static PermissionDenied ofPermissionDeniedWithErrorCode(String messageKey, String errorCode,
            Object... messageArgs) {
        return new PermissionDenied(messageKey, errorCode, null, messageArgs);
    }

    public static PermissionDenied ofForbiddenWithErrorCode(String errorCode, Object... messageArgs) {
        return new PermissionDenied("error.access.denied", errorCode, null, messageArgs);
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

    // messageArgs를 지원하는 오버로드 메서드들
    public static NotFound ofNotFound(String messageKey, Object context, Object... messageArgs) {
        return new NotFound(messageKey, context, messageArgs);
    }

    public static NotFound ofNotFound(String messageKey, Object... messageArgs) {
        return new NotFound(messageKey, (Object) null, messageArgs);
    }

    // errorCode를 지원하는 오버로드 메서드들
    public static NotFound ofNotFoundWithErrorCode(String messageKey, String errorCode, Object context) {
        return new NotFound(messageKey, errorCode, context);
    }

    public static NotFound ofNotFoundWithErrorCode(String messageKey, String errorCode) {
        return new NotFound(messageKey, errorCode, (Object) null);
    }

    public static NotFound ofNotFoundWithErrorCode(String messageKey, String errorCode, Object context,
            Object... messageArgs) {
        return new NotFound(messageKey, errorCode, context, messageArgs);
    }

    public static NotFound ofNotFoundWithErrorCode(String messageKey, String errorCode, Object... messageArgs) {
        return new NotFound(messageKey, errorCode, null, messageArgs);
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

    // messageArgs를 지원하는 오버로드 메서드들
    public static ResourceConflict ofResourceConflict(String messageKey, Object context, Object... messageArgs) {
        return new ResourceConflict(messageKey, context, messageArgs);
    }

    public static ResourceConflict ofConflict(String messageKey, Object... messageArgs) {
        return new ResourceConflict(messageKey, (Object) null, messageArgs);
    }

    // errorCode를 지원하는 오버로드 메서드들
    public static ResourceConflict ofResourceConflictWithErrorCode(String messageKey, String errorCode,
            Object context) {
        return new ResourceConflict(messageKey, errorCode, context);
    }

    public static ResourceConflict ofConflictWithErrorCode(String messageKey, String errorCode) {
        return new ResourceConflict(messageKey, errorCode, (Object) null);
    }

    public static ResourceConflict ofResourceConflictWithErrorCode(String messageKey, String errorCode, Object context,
            Object... messageArgs) {
        return new ResourceConflict(messageKey, errorCode, context, messageArgs);
    }

    public static ResourceConflict ofConflictWithErrorCode(String messageKey, String errorCode, Object... messageArgs) {
        return new ResourceConflict(messageKey, errorCode, null, messageArgs);
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

    // messageArgs를 지원하는 오버로드 메서드들
    public static PreconditionFailed ofPreconditionFailed(String messageKey, Object context, Object... messageArgs) {
        return new PreconditionFailed(messageKey, context, messageArgs);
    }

    public static PreconditionFailed ofPreconditionFailed(String messageKey, Object... messageArgs) {
        return new PreconditionFailed(messageKey, (Object) null, messageArgs);
    }

    // errorCode를 지원하는 오버로드 메서드들
    public static PreconditionFailed ofPreconditionFailedWithErrorCode(String messageKey, String errorCode,
            Object context) {
        return new PreconditionFailed(messageKey, errorCode, context);
    }

    public static PreconditionFailed ofPreconditionFailedWithErrorCode(String messageKey, String errorCode) {
        return new PreconditionFailed(messageKey, errorCode, (Object) null);
    }

    public static PreconditionFailed ofPreconditionFailedWithErrorCode(String messageKey, String errorCode,
            Object context, Object... messageArgs) {
        return new PreconditionFailed(messageKey, errorCode, context, messageArgs);
    }

    public static PreconditionFailed ofPreconditionFailedWithErrorCode(String messageKey, String errorCode,
            Object... messageArgs) {
        return new PreconditionFailed(messageKey, errorCode, null, messageArgs);
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

    // messageArgs를 지원하는 오버로드 메서드들
    public static BusinessRuleViolation ofBusinessRuleViolation(String messageKey, Object context,
            Object... messageArgs) {
        return new BusinessRuleViolation(messageKey, context, messageArgs);
    }

    public static BusinessRuleViolation ofBusinessRuleViolation(String messageKey, Object... messageArgs) {
        return new BusinessRuleViolation(messageKey, (Object) null, messageArgs);
    }

    // errorCode를 지원하는 오버로드 메서드들
    public static BusinessRuleViolation ofBusinessRuleViolationWithErrorCode(String messageKey, String errorCode,
            Object context) {
        return new BusinessRuleViolation(messageKey, errorCode, context);
    }

    public static BusinessRuleViolation ofBusinessRuleViolationWithErrorCode(String messageKey, String errorCode) {
        return new BusinessRuleViolation(messageKey, errorCode, (Object) null);
    }

    public static BusinessRuleViolation ofBusinessRuleViolationWithErrorCode(String messageKey, String errorCode,
            Object context,
            Object... messageArgs) {
        return new BusinessRuleViolation(messageKey, errorCode, context, messageArgs);
    }

    public static BusinessRuleViolation ofBusinessRuleViolationWithErrorCode(String messageKey, String errorCode,
            Object... messageArgs) {
        return new BusinessRuleViolation(messageKey, errorCode, null, messageArgs);
    }

    /**
     * 내부 서버 오류 생성 (500 Internal Server Error)
     */
    public static InternalError ofInternalError(String messageKey, Object context) {
        return new InternalError(messageKey, context);
    }

    public static InternalError ofInternalServerError(String messageKey) {
        return new InternalError(messageKey, (Object) null);
    }

    // messageArgs를 지원하는 오버로드 메서드들
    public static InternalError ofInternalError(String messageKey, Object context, Object... messageArgs) {
        return new InternalError(messageKey, context, messageArgs);
    }

    public static InternalError ofInternalServerError(String messageKey, Object... messageArgs) {
        return new InternalError(messageKey, null, messageArgs);
    }

    // errorCode를 지원하는 오버로드 메서드들
    public static InternalError ofInternalErrorWithErrorCode(String messageKey, String errorCode, Object context) {
        return new InternalError(messageKey, errorCode, context);
    }

    public static InternalError ofInternalServerErrorWithErrorCode(String messageKey, String errorCode) {
        return new InternalError(messageKey, errorCode, (Object) null);
    }

    public static InternalError ofInternalErrorWithErrorCode(String messageKey, String errorCode, Object context,
            Object... messageArgs) {
        return new InternalError(messageKey, errorCode, context, messageArgs);
    }

    public static InternalError ofInternalServerErrorWithErrorCode(String messageKey, String errorCode,
            Object... messageArgs) {
        return new InternalError(messageKey, errorCode, null, messageArgs);
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