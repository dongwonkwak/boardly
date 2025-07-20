// ApiFailureHandler.java - 개선된 API 실패 처리기
package com.boardly.shared.presentation;

import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.presentation.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * API 실패 처리기
 * 
 * <p>
 * 도메인 Failure를 적절한 HTTP 상태 코드와 응답으로 변환합니다.
 */
@Slf4j
@Component
public class ApiFailureHandler {

    /**
     * Failure를 ResponseEntity로 변환합니다.
     */
    public ResponseEntity<ErrorResponse> handleFailure(Failure failure) {
        return switch (failure) {
            case Failure.InputError inputError ->
                handleInputError(inputError);

            case Failure.PermissionDenied permissionDenied ->
                handlePermissionDenied(permissionDenied);

            case Failure.NotFound notFound ->
                handleNotFound(notFound);

            case Failure.ResourceConflict resourceConflict ->
                handleResourceConflict(resourceConflict);

            case Failure.PreconditionFailed preconditionFailed ->
                handlePreconditionFailed(preconditionFailed);

            case Failure.BusinessRuleViolation businessRuleViolation ->
                handleBusinessRuleViolation(businessRuleViolation);

            case Failure.InternalError internalError ->
                handleInternalError(internalError);

            default -> {
                log.warn("처리되지 않은 Failure 타입: {}", failure.getClass().getSimpleName());
                yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ErrorResponse.of("INTERNAL_ERROR", "내부 서버 오류가 발생했습니다."));
            }
        };
    }

    /**
     * 입력 형식/데이터 오류 처리 (400 Bad Request)
     */
    private ResponseEntity<ErrorResponse> handleInputError(Failure.InputError inputError) {
        log.warn("입력 오류: errorCode={}, message={}, violations={}",
                inputError.getErrorCode(), inputError.getMessage(), inputError.getViolations().size());

        ErrorResponse errorResponse = ErrorResponse.validation(inputError);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 권한 거부 처리 (403 Forbidden)
     */
    private ResponseEntity<ErrorResponse> handlePermissionDenied(Failure.PermissionDenied permissionDenied) {
        log.warn("권한 거부: errorCode={}, message={}",
                permissionDenied.getErrorCode(), permissionDenied.getMessage());

        ErrorResponse errorResponse = ErrorResponse.forbidden(permissionDenied);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * 리소스 미발견 처리 (404 Not Found)
     */
    private ResponseEntity<ErrorResponse> handleNotFound(Failure.NotFound notFound) {
        log.debug("리소스 미발견: errorCode={}, message={}",
                notFound.getErrorCode(), notFound.getMessage());

        ErrorResponse errorResponse = ErrorResponse.notFound(notFound);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 리소스 충돌 처리 (409 Conflict)
     */
    private ResponseEntity<ErrorResponse> handleResourceConflict(Failure.ResourceConflict resourceConflict) {
        log.warn("리소스 충돌: errorCode={}, message={}",
                resourceConflict.getErrorCode(), resourceConflict.getMessage());

        ErrorResponse errorResponse = ErrorResponse.conflict(resourceConflict);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * 전제 조건 실패 처리 (412 Precondition Failed)
     */
    private ResponseEntity<ErrorResponse> handlePreconditionFailed(Failure.PreconditionFailed preconditionFailed) {
        log.warn("전제 조건 실패: errorCode={}, message={}",
                preconditionFailed.getErrorCode(), preconditionFailed.getMessage());

        ErrorResponse errorResponse = ErrorResponse.preconditionFailed(preconditionFailed);

        return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(errorResponse);
    }

    /**
     * 비즈니스 룰 위반 처리 (422 Unprocessable Entity)
     */
    private ResponseEntity<ErrorResponse> handleBusinessRuleViolation(
            Failure.BusinessRuleViolation businessRuleViolation) {
        log.warn("비즈니스 룰 위반: errorCode={}, message={}",
                businessRuleViolation.getErrorCode(), businessRuleViolation.getMessage());

        ErrorResponse errorResponse = ErrorResponse.businessRuleViolation(businessRuleViolation);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }

    /**
     * 내부 서버 오류 처리 (500 Internal Server Error)
     */
    private ResponseEntity<ErrorResponse> handleInternalError(Failure.InternalError internalError) {
        log.error("내부 서버 오류: errorCode={}, message={}",
                internalError.getErrorCode(), internalError.getMessage());

        // 보안상 내부 오류 상세 정보는 클라이언트에 노출하지 않음
        ErrorResponse errorResponse = ErrorResponse.internal(internalError);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}