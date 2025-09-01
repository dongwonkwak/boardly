package com.boardly.api.common;

import com.boardly.application.port.out.MessageResolver;
import com.boardly.domain.common.ErrorCode;
import com.boardly.domain.common.Failure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.boardly.api.common.ApiFailureHandlerLogConstants.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiFailureHandler {

    private final MessageResolver messageResolver;

    // ======================== 상수 정의 ========================
    private static final String UNKNOWN_PATH = "/unknown";

    // 에러 코드 상수
    private static final String INTERNAL_ERROR_CODE = ErrorCode.INTERNAL_ERROR.getCode();

    // 메시지 키 상수
    private static final String VALIDATION_ERROR_MESSAGE_KEY = "error.validation.input";
    private static final String INTERNAL_ERROR_MESSAGE = "내부 서버 오류가 발생했습니다.";

    // ======================== 캐시 ========================
    private static final ThreadLocal<String> REQUEST_PATH_CACHE = new ThreadLocal<>();

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
                log.warn(LOG_UNHANDLED_FAILURE, failure.getClass().getSimpleName());
                yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ErrorResponse.of(INTERNAL_ERROR_CODE, INTERNAL_ERROR_MESSAGE));
            }
        };
    }

    private ResponseEntity<ErrorResponse> handleInputError(Failure.InputError inputError) {
        log.warn(LOG_INPUT_ERROR,
                inputError.getErrorCode(), inputError.getMessageKey(), inputError.getViolations().size());

        String message = messageResolver.resolveMessage(VALIDATION_ERROR_MESSAGE_KEY, inputError.getMessageArgs());
        List<ErrorResponse.ValidationDetail> details = inputError.getViolations().stream()
                .map(violation -> ErrorResponse.ValidationDetail.builder()
                        .field(violation.getField())
                        .message(messageResolver.resolveMessage(violation.getMessageKey(), violation.getMessageArgs()))
                        .rejectedValue(violation.getRejectedValue())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.validation(message, details, getCurrentRequestPath()));
    }

    private ResponseEntity<ErrorResponse> handlePermissionDenied(Failure.PermissionDenied permissionDenied) {
        return createErrorResponse(
                permissionDenied,
                LOG_PREFIX_PERMISSION_DENIED,
                HttpStatus.FORBIDDEN,
                (message, context, path) -> ErrorResponse.forbidden(message, context, path));
    }

    private ResponseEntity<ErrorResponse> handleNotFound(Failure.NotFound notFound) {
        return createErrorResponse(
                notFound,
                LOG_PREFIX_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                (message, context, path) -> ErrorResponse.notFound(message, context, path),
                log::debug // NotFound는 debug 레벨로 로깅
        );
    }

    private ResponseEntity<ErrorResponse> handleResourceConflict(Failure.ResourceConflict resourceConflict) {
        return createErrorResponse(
                resourceConflict,
                LOG_PREFIX_RESOURCE_CONFLICT,
                HttpStatus.CONFLICT,
                (message, context, path) -> ErrorResponse.conflict(message, context, path));
    }

    private ResponseEntity<ErrorResponse> handlePreconditionFailed(Failure.PreconditionFailed preconditionFailed) {
        return createErrorResponse(
                preconditionFailed,
                LOG_PREFIX_PRECONDITION_FAILED,
                HttpStatus.PRECONDITION_FAILED,
                (message, context, path) -> ErrorResponse.preconditionFailed(message, context, path));
    }

    private ResponseEntity<ErrorResponse> handleBusinessRuleViolation(
            Failure.BusinessRuleViolation businessRuleViolation) {
        return createErrorResponse(
                businessRuleViolation,
                LOG_PREFIX_BUSINESS_RULE_VIOLATION,
                HttpStatus.UNPROCESSABLE_ENTITY,
                (message, context, path) -> ErrorResponse.businessRuleViolation(message, context, path));
    }

    private ResponseEntity<ErrorResponse> handleInternalError(Failure.InternalError internalError) {
        return createErrorResponse(
                internalError,
                LOG_PREFIX_INTERNAL_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR,
                (message, context, path) -> ErrorResponse.internal(message, path),
                log::error // InternalError는 error 레벨로 로깅
        );
    }

    /**
     * 공통 에러 응답 생성 메서드
     */
    private ResponseEntity<ErrorResponse> createErrorResponse(
            Failure failure,
            String logPrefix,
            HttpStatus status,
            ErrorResponseCreator responseCreator) {
        return createErrorResponse(failure, logPrefix, status, responseCreator, log::warn);
    }

    /**
     * 공통 에러 응답 생성 메서드 (로깅 레벨 지정 가능)
     */
    private ResponseEntity<ErrorResponse> createErrorResponse(
            Failure failure,
            String logPrefix,
            HttpStatus status,
            ErrorResponseCreator responseCreator,
            LogFunction logFunction) {

        logFunction.log(LOG_FORMAT_ERROR_SUMMARY,
                logPrefix, failure.getErrorCode(), failure.getMessageKey());

        String message = messageResolver.resolveMessage(failure.getMessageKey(), failure.getMessageArgs());
        String path = getCurrentRequestPath();

        ErrorResponse response = responseCreator.create(message, getContext(failure), path);
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Failure에서 context 추출
     */
    private Object getContext(Failure failure) {
        return switch (failure) {
            case Failure.PermissionDenied permissionDenied -> permissionDenied.getContext();
            case Failure.NotFound notFound -> notFound.getContext();
            case Failure.ResourceConflict resourceConflict -> resourceConflict.getContext();
            case Failure.PreconditionFailed preconditionFailed -> preconditionFailed.getContext();
            case Failure.BusinessRuleViolation businessRuleViolation -> businessRuleViolation.getContext();
            case Failure.InternalError internalError -> internalError.getContext();
            default -> null;
        };
    }

    /**
     * 현재 요청 경로 조회 (예외 처리 강화 + 캐시 적용)
     */
    private String getCurrentRequestPath() {
        String cached = REQUEST_PATH_CACHE.get();
        if (cached != null) {
            return cached;
        }

        cached = extractRequestPath();
        REQUEST_PATH_CACHE.set(cached);
        return cached;
    }

    /**
     * 요청 경로 추출 (실제 구현)
     */
    private String extractRequestPath() {
        try {
            var requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes == null) {
                log.debug(LOG_REQUEST_ATTRIBUTES_NULL);
                return UNKNOWN_PATH;
            }

            if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
                var request = servletRequestAttributes.getRequest();
                String requestURI = request.getRequestURI();
                return requestURI != null ? requestURI : UNKNOWN_PATH;
            }

            log.debug(LOG_UNSUPPORTED_REQUEST_ATTRIBUTES,
                    requestAttributes.getClass().getSimpleName());
            return UNKNOWN_PATH;

        } catch (SecurityException e) {
            log.warn(LOG_SECURITY_POLICY_ERROR, e.getMessage());
            return UNKNOWN_PATH;
        } catch (Exception e) {
            log.debug(LOG_REQUEST_PATH_ERROR, e.getMessage());
            return UNKNOWN_PATH;
        }
    }

    /**
     * ErrorResponse 생성 함수형 인터페이스
     */
    @FunctionalInterface
    private interface ErrorResponseCreator {
        ErrorResponse create(String message, Object context, String path);
    }

    /**
     * 로깅 함수형 인터페이스
     */
    @FunctionalInterface
    private interface LogFunction {
        void log(String format, Object... arguments);
    }

    /**
     * ThreadLocal 캐시 정리 (요청 처리 완료 후 호출 권장)
     */
    public void clearCache() {
        REQUEST_PATH_CACHE.remove();
    }
}
