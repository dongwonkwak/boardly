package com.boardly.api.common.handler;

import com.boardly.api.common.handler.response.ErrorResponse;
import com.boardly.shared.common.error.Failure;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApiFailureHandler {

    public ResponseEntity<ErrorResponse> handleFailure(Failure failure) {
        return switch (failure) {
            case Failure.InputError inputError -> handleInputError(inputError);
            case Failure.PermissionDenied permissionDenied -> handlePermissionDenied(permissionDenied);
            case Failure.NotFound notFound -> handleNotFound(notFound);
            case Failure.ResourceConflict resourceConflict -> handleResourceConflict(resourceConflict);
            case Failure.PreconditionFailed preconditionFailed -> handlePreconditionFailed(preconditionFailed);
            case Failure.BusinessRuleViolation businessRuleViolation ->
                handleBusinessRuleViolation(businessRuleViolation);
            case Failure.InternalError internalError -> handleInternalError(internalError);
            default -> {
                log.warn("Unhandled Failure type: {}", failure.getClass().getSimpleName());
                yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ErrorResponse.of("INTERNAL_ERROR", "내부 서버 오류가 발생했습니다."));
            }
        };
    }

    private ResponseEntity<ErrorResponse> handleInputError(Failure.InputError inputError) {
        log.warn("입력 오류: errorCode={}, message={}, violations={}",
                inputError.getErrorCode(), inputError.getMessage(), inputError.getViolations().size());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.validation(inputError));
    }

    private ResponseEntity<ErrorResponse> handlePermissionDenied(Failure.PermissionDenied permissionDenied) {
        log.warn("권한 거부: errorCode={}, message={}", permissionDenied.getErrorCode(), permissionDenied.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.forbidden(permissionDenied));
    }

    private ResponseEntity<ErrorResponse> handleNotFound(Failure.NotFound notFound) {
        log.debug("리소스 미발견: errorCode={}, message={}", notFound.getErrorCode(), notFound.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.notFound(notFound));
    }

    private ResponseEntity<ErrorResponse> handleResourceConflict(Failure.ResourceConflict resourceConflict) {
        log.warn("리소스 충돌: errorCode={}, message={}", resourceConflict.getErrorCode(), resourceConflict.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.conflict(resourceConflict));
    }

    private ResponseEntity<ErrorResponse> handlePreconditionFailed(Failure.PreconditionFailed preconditionFailed) {
        log.warn("전제 조건 실패: errorCode={}, message={}", preconditionFailed.getErrorCode(),
                preconditionFailed.getMessage());
        return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED)
                .body(ErrorResponse.preconditionFailed(preconditionFailed));
    }

    private ResponseEntity<ErrorResponse> handleBusinessRuleViolation(
            Failure.BusinessRuleViolation businessRuleViolation) {
        log.warn("비즈니스 룰 위반: errorCode={}, message={}", businessRuleViolation.getErrorCode(),
                businessRuleViolation.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.businessRuleViolation(businessRuleViolation));
    }

    private ResponseEntity<ErrorResponse> handleInternalError(Failure.InternalError internalError) {
        log.error("내부 서버 오류: errorCode={}, message={}", internalError.getErrorCode(), internalError.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.internal(internalError));
    }
}
