package com.boardly.shared.presentation;

import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.presentation.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * API 실패 처리를 위한 공통 핸들러 클래스
 */
public class ApiFailureHandler {

    /**
     * Failure 객체를 적절한 HTTP 응답으로 변환합니다.
     * 
     * @param failure 실패 정보
     * @return HTTP 응답 엔티티
     */
    public static ResponseEntity<?> handleFailure(Failure failure) {
        return switch (failure) {
            case Failure.ValidationFailure validationFailure -> 
                ResponseEntity.unprocessableEntity().body(ErrorResponse.validation(validationFailure));
            case Failure.ConflictFailure conflictFailure -> 
                ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.conflict(conflictFailure));
            case Failure.NotFoundFailure notFoundFailure -> 
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.notFound(notFoundFailure));
            case Failure.InternalServerError internalServerError -> 
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.internal(internalServerError));
            case Failure.ForbiddenFailure forbiddenFailure -> 
                ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.forbidden(forbiddenFailure));
            default -> 
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.internal(new Failure.InternalServerError("알 수 없는 오류가 발생했습니다.")));
        };
    }
} 