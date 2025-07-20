package com.boardly.shared.presentation.response;

import com.boardly.shared.domain.common.Failure;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

/**
 * API 오류 응답을 위한 공통 클래스
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String message,
        String errorCode,
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant timestamp,
        String path,
        List<Failure.FieldViolation> details) {

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
            case Failure.ForbiddenFailure forbiddenFailure ->
                forbidden(forbiddenFailure);
            case Failure.BadRequestFailure badRequestFailure ->
                badRequest(badRequestFailure);
            default ->
                internal(new Failure.InternalServerError("알 수 없는 오류가 발생했습니다."));
        };
    }

    public static ErrorResponse validation(Failure.ValidationFailure failure) {
        return ErrorResponse.builder()
                .message(failure.message())
                .errorCode("VALIDATION_ERROR")
                .timestamp(Instant.now())
                .details(failure.violations().stream().toList())
                .build();
    }

    public static ErrorResponse conflict(Failure.ConflictFailure failure) {
        return ErrorResponse.builder()
                .message(failure.message())
                .errorCode("CONFLICT_ERROR")
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorResponse notFound(Failure.NotFoundFailure failure) {
        return ErrorResponse.builder()
                .message(failure.message())
                .errorCode("NOT_FOUND_ERROR")
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorResponse internal(Failure.InternalServerError failure) {
        return ErrorResponse.builder()
                .message(failure.message())
                .errorCode("INTERNAL_SERVER_ERROR")
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorResponse forbidden(Failure.ForbiddenFailure failure) {
        return ErrorResponse.builder()
                .message(failure.message())
                .errorCode("FORBIDDEN_ERROR")
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorResponse badRequest(Failure.BadRequestFailure failure) {
        return ErrorResponse.builder()
                .message(failure.message())
                .errorCode("BAD_REQUEST")
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorResponse unauthorized(String message) {
        return ErrorResponse.builder()
                .message(message)
                .errorCode("UNAUTHORIZED")
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorResponse methodNotAllowed(String message) {
        return ErrorResponse.builder()
                .message(message)
                .errorCode("METHOD_NOT_ALLOWED")
                .timestamp(Instant.now())
                .build();
    }
}