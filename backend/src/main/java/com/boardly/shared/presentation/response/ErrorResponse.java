// ErrorResponse.java - 개선된 API 오류 응답
package com.boardly.shared.presentation.response;

import com.boardly.shared.domain.common.Failure;
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
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    String code; // 에러 코드
    String message; // 사용자 친화적 메시지
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Builder.Default
    Instant timestamp = Instant.now(); // 오류 발생 시간
    String path; // 요청 경로 (컨트롤러에서 설정)
    List<Failure.FieldViolation> details; // 상세 검증 오류 (400일 때만)
    Object context; // 추가 컨텍스트 정보

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
     * 컨텍스트 정보가 있는 에러 응답 생성
     */
    public static ErrorResponse of(String code, String message, Object context) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .context(context)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * 입력 검증 오류 응답 생성 (400 Bad Request)
     */
    public static ErrorResponse validation(String message, List<Failure.FieldViolation> details) {
        return ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message(message)
                .details(details)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * 기존 호환성을 위한 메서드들
     */
    public static ErrorResponse validation(Failure.InputError inputError) {
        return ErrorResponse.builder()
                .code(inputError.getErrorCode())
                .message(inputError.getMessage())
                .details(inputError.getViolations())
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorResponse forbidden(Failure.PermissionDenied permissionDenied) {
        return ErrorResponse.builder()
                .code(permissionDenied.getErrorCode())
                .message(permissionDenied.getMessage())
                .context(permissionDenied.getContext())
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorResponse notFound(Failure.NotFound notFound) {
        return ErrorResponse.builder()
                .code(notFound.getErrorCode())
                .message(notFound.getMessage())
                .context(notFound.getContext())
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorResponse conflict(Failure.ResourceConflict resourceConflict) {
        return ErrorResponse.builder()
                .code(resourceConflict.getErrorCode())
                .message(resourceConflict.getMessage())
                .context(resourceConflict.getContext())
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorResponse preconditionFailed(Failure.PreconditionFailed preconditionFailed) {
        return ErrorResponse.builder()
                .code(preconditionFailed.getErrorCode())
                .message(preconditionFailed.getMessage())
                .context(preconditionFailed.getContext())
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorResponse businessRuleViolation(Failure.BusinessRuleViolation businessRuleViolation) {
        return ErrorResponse.builder()
                .code(businessRuleViolation.getErrorCode())
                .message(businessRuleViolation.getMessage())
                .context(businessRuleViolation.getContext())
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorResponse internal(Failure.InternalError internalError) {
        return ErrorResponse.builder()
                .code("INTERNAL_ERROR")
                .message(internalError.getMessage())
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