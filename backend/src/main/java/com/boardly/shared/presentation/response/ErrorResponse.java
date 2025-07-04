package com.boardly.shared.presentation.response;

import com.boardly.shared.domain.common.Failure;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * API 오류 응답을 위한 공통 클래스
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String message,
        String errorCode,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp,
        String path,
        Object details
) {
    
    public static ErrorResponse of(Failure failure) {
        return switch (failure) {
            case Failure.ValidationFailure validationFailure -> 
                validation(validationFailure);
            case Failure.ConflictFailure conflictFailure -> 
                conflict(conflictFailure);
            case Failure.NotFoundFailure notFoundFailure -> 
                notFound(notFoundFailure);
            case Failure.InternalServerError internalServerError -> 
                internal(internalServerError);
            default -> 
                internal(new Failure.InternalServerError("알 수 없는 오류가 발생했습니다."));
        };
    }

    public static ErrorResponse of(Failure failure, String path) {
        return switch (failure) {
            case Failure.ValidationFailure validationFailure -> 
                validation(validationFailure, path);
            case Failure.ConflictFailure conflictFailure -> 
                conflict(conflictFailure, path);
            case Failure.NotFoundFailure notFoundFailure -> 
                notFound(notFoundFailure, path);
            case Failure.InternalServerError internalServerError -> 
                internal(internalServerError, path);
            default -> 
                internal(new Failure.InternalServerError("알 수 없는 오류가 발생했습니다."), path);
        };
    }

    public static ErrorResponse validation(Failure.ValidationFailure failure) {
        return validation(failure, null);
    }

    public static ErrorResponse validation(Failure.ValidationFailure failure, String path) {
        return ErrorResponse.builder()
                .message(failure.message())
                .errorCode("VALIDATION_ERROR")
                .timestamp(LocalDateTime.now())
                .path(path)
                .details(failure.violations())
                .build();
    }

    public static ErrorResponse conflict(Failure.ConflictFailure failure) {
        return conflict(failure, null);
    }

    public static ErrorResponse conflict(Failure.ConflictFailure failure, String path) {
        return ErrorResponse.builder()
                .message(failure.message())
                .errorCode("CONFLICT")
                .timestamp(LocalDateTime.now())
                .path(path)
                .details(null)
                .build();
    }

    public static ErrorResponse notFound(Failure.NotFoundFailure failure) {
        return notFound(failure, null);
    }

    public static ErrorResponse notFound(Failure.NotFoundFailure failure, String path) {
        return ErrorResponse.builder()
                .message(failure.message())
                .errorCode("NOT_FOUND")
                .timestamp(LocalDateTime.now())
                .path(path)
                .details(null)
                .build();
    }

    public static ErrorResponse internal(Failure.InternalServerError failure) {
        return internal(failure, null);
    }

    public static ErrorResponse internal(Failure.InternalServerError failure, String path) {
        return ErrorResponse.builder()
                .message(failure.message())
                .errorCode("INTERNAL_SERVER_ERROR")
                .timestamp(LocalDateTime.now())
                .path(path)
                .details(null)
                .build();
    }

    // 커스텀 에러 응답 생성을 위한 메서드들
    public static ErrorResponse badRequest(String message) {
        return badRequest(message, null);
    }

    public static ErrorResponse badRequest(String message, String path) {
        return ErrorResponse.builder()
                .message(message)
                .errorCode("BAD_REQUEST")
                .timestamp(LocalDateTime.now())
                .path(path)
                .details(null)
                .build();
    }

    public static ErrorResponse unauthorized(String message) {
        return unauthorized(message, null);
    }

    public static ErrorResponse unauthorized(String message, String path) {
        return ErrorResponse.builder()
                .message(message)
                .errorCode("UNAUTHORIZED")
                .timestamp(LocalDateTime.now())
                .path(path)
                .details(null)
                .build();
    }

    public static ErrorResponse forbidden(String message) {
        return forbidden(message, null);
    }

    public static ErrorResponse forbidden(String message, String path) {
        return ErrorResponse.builder()
                .message(message)
                .errorCode("FORBIDDEN")
                .timestamp(LocalDateTime.now())
                .path(path)
                .details(null)
                .build();
    }

    public static ErrorResponse custom(String message, String errorCode, Object details) {
        return custom(message, errorCode, details, null);
    }

    public static ErrorResponse custom(String message, String errorCode, Object details, String path) {
        return ErrorResponse.builder()
                .message(message)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .path(path)
                .details(details)
                .build();
    }
} 