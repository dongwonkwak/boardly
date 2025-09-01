package com.boardly.api.common;

import com.boardly.domain.common.Failure;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * API 오류 응답을 위한 공통 클래스
 */
@Value
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    String code; // 에러 코드
    String message; // 사용자 친화적 메시지 (국제화 처리된)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Builder.Default
    Instant timestamp = Instant.now(); // 오류 발생 시간
    String path; // 요청 경로
    List<ValidationDetail> details; // 상세 검증 오류 (400일 때만)
    Object context; // 추가 컨텍스트 정보

    /**
     * 검증 상세 오류 정보
     */
    @Value
    @Builder
    public static class ValidationDetail {
        String field;
        String message; // 국제화 처리된 메시지
        Object rejectedValue;
    }

    /**
     * 기본 에러 응답 생성
     */
    public static ErrorResponse of(String code, String message) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * 경로 정보가 있는 에러 응답 생성
     */
    public static ErrorResponse of(String code, String message, String path) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .path(path)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * 컨텍스트 정보가 있는 에러 응답 생성
     */
    public static ErrorResponse of(String code, String message, Object context, String path) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .context(context)
                .path(path)
                .timestamp(Instant.now())
                .build();
    }

    // ======================== 새로운 메서드들 (국제화된 메시지 직접 전달) ========================

    /**
     * 입력 검증 오류 응답 생성 (400 Bad Request)
     */
    public static ErrorResponse validation(String message, List<ValidationDetail> details, String path) {
        return ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message(message)
                .details(details)
                .path(path)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * 권한 거부 응답 생성 (403 Forbidden)
     */
    public static ErrorResponse forbidden(String message, Object context, String path) {
        return ErrorResponse.builder()
                .code("PERMISSION_DENIED")
                .message(message)
                .context(context)
                .path(path)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * 리소스 미발견 응답 생성 (404 Not Found)
     */
    public static ErrorResponse notFound(String message, Object context, String path) {
        return ErrorResponse.builder()
                .code("NOT_FOUND")
                .message(message)
                .context(context)
                .path(path)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * 리소스 충돌 응답 생성 (409 Conflict)
     */
    public static ErrorResponse conflict(String message, Object context, String path) {
        return ErrorResponse.builder()
                .code("RESOURCE_CONFLICT")
                .message(message)
                .context(context)
                .path(path)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * 전제 조건 실패 응답 생성 (412 Precondition Failed)
     */
    public static ErrorResponse preconditionFailed(String message, Object context, String path) {
        return ErrorResponse.builder()
                .code("PRECONDITION_FAILED")
                .message(message)
                .context(context)
                .path(path)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * 비즈니스 룰 위반 응답 생성 (422 Unprocessable Entity)
     */
    public static ErrorResponse businessRuleViolation(String message, Object context, String path) {
        return ErrorResponse.builder()
                .code("BUSINESS_RULE_VIOLATION")
                .message(message)
                .context(context)
                .path(path)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * 내부 서버 오류 응답 생성 (500 Internal Server Error)
     */
    public static ErrorResponse internal(String message, String path) {
        return ErrorResponse.builder()
                .code("INTERNAL_ERROR")
                .message(message)
                .path(path)
                .timestamp(Instant.now())
                .build();
    }

    // ======================== 기존 호환성 메서드들 (Deprecated) ========================

    /**
     * 입력 검증 오류 응답 생성 (400 Bad Request)
     * 
     * @deprecated 새로운 validation(String, List<ValidationDetail>, String) 메서드 사용
     */
    @Deprecated
    public static ErrorResponse validation(String message, List<Failure.FieldViolation> details) {
        List<ValidationDetail> convertedDetails = details != null ? details.stream()
                .map(violation -> ValidationDetail.builder()
                        .field(violation.getField())
                        .message(violation.getMessageKey()) // messageKey를 message로 사용 (임시)
                        .rejectedValue(violation.getRejectedValue())
                        .build())
                .toList() : null;

        return ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message(message)
                .details(convertedDetails)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * @deprecated 새로운 메서드들로 대체
     */
    @Deprecated
    public static ErrorResponse validation(Failure.InputError inputError) {
        return validation(inputError.getMessageKey(), inputError.getViolations()); // messageKey를 message로 임시 사용
    }

    /**
     * @deprecated 새로운 메서드들로 대체
     */
    @Deprecated
    public static ErrorResponse forbidden(Failure.PermissionDenied permissionDenied) {
        return ErrorResponse.builder()
                .code(permissionDenied.getErrorCode())
                .message(permissionDenied.getMessageKey()) // messageKey를 message로 임시 사용
                .context(permissionDenied.getContext())
                .timestamp(Instant.now())
                .build();
    }

    /**
     * @deprecated 새로운 메서드들로 대체
     */
    @Deprecated
    public static ErrorResponse notFound(Failure.NotFound notFound) {
        return ErrorResponse.builder()
                .code(notFound.getErrorCode())
                .message(notFound.getMessageKey()) // messageKey를 message로 임시 사용
                .context(notFound.getContext())
                .timestamp(Instant.now())
                .build();
    }

    /**
     * @deprecated 새로운 메서드들로 대체
     */
    @Deprecated
    public static ErrorResponse conflict(Failure.ResourceConflict resourceConflict) {
        return ErrorResponse.builder()
                .code(resourceConflict.getErrorCode())
                .message(resourceConflict.getMessageKey()) // messageKey를 message로 임시 사용
                .context(resourceConflict.getContext())
                .timestamp(Instant.now())
                .build();
    }

    /**
     * @deprecated 새로운 메서드들로 대체
     */
    @Deprecated
    public static ErrorResponse preconditionFailed(Failure.PreconditionFailed preconditionFailed) {
        return ErrorResponse.builder()
                .code(preconditionFailed.getErrorCode())
                .message(preconditionFailed.getMessageKey()) // messageKey를 message로 임시 사용
                .context(preconditionFailed.getContext())
                .timestamp(Instant.now())
                .build();
    }

    /**
     * @deprecated 새로운 메서드들로 대체
     */
    @Deprecated
    public static ErrorResponse businessRuleViolation(Failure.BusinessRuleViolation businessRuleViolation) {
        return ErrorResponse.builder()
                .code(businessRuleViolation.getErrorCode())
                .message(businessRuleViolation.getMessageKey()) // messageKey를 message로 임시 사용
                .context(businessRuleViolation.getContext())
                .timestamp(Instant.now())
                .build();
    }

    /**
     * @deprecated 새로운 메서드들로 대체
     */
    @Deprecated
    public static ErrorResponse internal(Failure.InternalError internalError) {
        return ErrorResponse.builder()
                .code("INTERNAL_ERROR")
                .message(internalError.getMessageKey()) // messageKey를 message로 임시 사용
                .timestamp(Instant.now())
                .build();
    }

    /**
     * @deprecated 대신 새로운 타입별 메서드 사용
     */
    @Deprecated
    public static ErrorResponse of(Failure failure) {
        return switch (failure) {
            case Failure.InputError inputError -> validation(inputError);
            case Failure.PermissionDenied permissionDenied -> forbidden(permissionDenied);
            case Failure.NotFound notFound -> notFound(notFound);
            case Failure.ResourceConflict resourceConflict -> conflict(resourceConflict);
            case Failure.PreconditionFailed preconditionFailed -> preconditionFailed(preconditionFailed);
            case Failure.BusinessRuleViolation businessRuleViolation -> businessRuleViolation(businessRuleViolation);
            case Failure.InternalError internalError -> internal(internalError);
            default -> ErrorResponse.of("UNKNOWN_ERROR", "알 수 없는 오류가 발생했습니다.");
        };
    }
}