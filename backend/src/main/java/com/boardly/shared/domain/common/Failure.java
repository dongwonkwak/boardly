// Failure.java - 개선된 실패 타입 분류
package com.boardly.shared.domain.common;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import java.util.List;

@Getter
@RequiredArgsConstructor
public abstract class Failure {

  private final String message;

  /**
   * 필드 위반 정보
   */
  @Value
  @Builder
  public static class FieldViolation {
    String field;
    String message;
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
    private final String errorCode;
    private final List<FieldViolation> violations;

    public InputError(String message, String errorCode, List<FieldViolation> violations) {
      super(message);
      this.errorCode = errorCode;
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
    private final String errorCode;
    private final Object context;

    public PermissionDenied(String message, String errorCode, Object context) {
      super(message);
      this.errorCode = errorCode;
      this.context = context;
    }
  }

  // ======================== 404 Not Found ========================
  /**
   * 리소스 미발견 실패 (404 Not Found)
   */
  @Getter
  public static class NotFound extends Failure {
    private final String errorCode;
    private final Object context;

    public NotFound(String message, String errorCode, Object context) {
      super(message);
      this.errorCode = errorCode;
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
    private final String errorCode;
    private final Object context;

    public ResourceConflict(String message, String errorCode, Object context) {
      super(message);
      this.errorCode = errorCode;
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
    private final String errorCode;
    private final Object context;

    public PreconditionFailed(String message, String errorCode, Object context) {
      super(message);
      this.errorCode = errorCode;
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
    private final String errorCode;
    private final Object context;

    public BusinessRuleViolation(String message, String errorCode, Object context) {
      super(message);
      this.errorCode = errorCode;
      this.context = context;
    }
  }

  // ======================== 500 Internal Server Error ========================
  /**
   * 내부 서버 오류 (500 Internal Server Error)
   */
  @Getter
  public static class InternalError extends Failure {
    private final String errorCode;
    private final Object context;

    public InternalError(String message, String errorCode, Object context) {
      super(message);
      this.errorCode = errorCode;
      this.context = context;
    }

    public InternalError(String message) {
      this(message, "INTERNAL_ERROR", null);
    }
  }

  // ======================== Factory Methods ========================

  /**
   * 입력 형식/데이터 오류 생성 (400 Bad Request)
   */
  public static InputError ofInputError(String message, String errorCode, List<FieldViolation> violations) {
    return new InputError(message, errorCode, violations);
  }

  public static InputError ofInputError(String message) {
    return new InputError(message, "INVALID_INPUT", null);
  }

  public static InputError ofValidation(String message, List<FieldViolation> violations) {
    return new InputError(message, "VALIDATION_ERROR", violations);
  }

  /**
   * 권한 거부 생성 (403 Forbidden)
   */
  public static PermissionDenied ofPermissionDenied(String message, String errorCode, Object context) {
    return new PermissionDenied(message, errorCode, context);
  }

  public static PermissionDenied ofPermissionDenied(String message) {
    return new PermissionDenied(message, "PERMISSION_DENIED", null);
  }

  public static PermissionDenied ofForbidden(String errorCode) {
    return new PermissionDenied("접근이 거부되었습니다.", errorCode, null);
  }

  /**
   * 리소스 미발견 생성 (404 Not Found)
   */
  public static NotFound ofNotFound(String message, String errorCode, Object context) {
    return new NotFound(message, errorCode, context);
  }

  public static NotFound ofNotFound(String message) {
    return new NotFound(message, "NOT_FOUND", null);
  }

  /**
   * 리소스 충돌 생성 (409 Conflict)
   */
  public static ResourceConflict ofResourceConflict(String message, String errorCode, Object context) {
    return new ResourceConflict(message, errorCode, context);
  }

  public static ResourceConflict ofConflict(String message) {
    return new ResourceConflict(message, "RESOURCE_CONFLICT", null);
  }

  /**
   * 전제 조건 실패 생성 (412 Precondition Failed)
   */
  public static PreconditionFailed ofPreconditionFailed(String message, String errorCode, Object context) {
    return new PreconditionFailed(message, errorCode, context);
  }

  public static PreconditionFailed ofPreconditionFailed(String message) {
    return new PreconditionFailed(message, "PRECONDITION_FAILED", null);
  }

  /**
   * 비즈니스 룰 위반 생성 (422 Unprocessable Entity)
   */
  public static BusinessRuleViolation ofBusinessRuleViolation(String message, String errorCode, Object context) {
    return new BusinessRuleViolation(message, errorCode, context);
  }

  public static BusinessRuleViolation ofBusinessRuleViolation(String message) {
    return new BusinessRuleViolation(message, "BUSINESS_RULE_VIOLATION", null);
  }

  /**
   * 내부 서버 오류 생성 (500 Internal Server Error)
   */
  public static InternalError ofInternalError(String message, String errorCode, Object context) {
    return new InternalError(message, errorCode, context);
  }

  public static InternalError ofInternalServerError(String message) {
    return new InternalError(message, "INTERNAL_ERROR", null);
  }

  // ======================== 기존 메서드 호환성 유지 ========================

  /**
   * @deprecated 대신 ofInputError 사용
   */
  @Deprecated
  public static InputError ofValidationFailure(String message, List<FieldViolation> violations) {
    return ofValidation(message, violations);
  }

  /**
   * @deprecated 대신 ofResourceConflict 사용
   */
  @Deprecated
  public static ResourceConflict ofConflictFailure(String message) {
    return ofConflict(message);
  }

  /**
   * @deprecated 대신 ofNotFound 사용
   */
  @Deprecated
  public static NotFound ofNotFoundFailure(String message) {
    return ofNotFound(message);
  }

  /**
   * @deprecated 대신 ofPermissionDenied 사용
   */
  @Deprecated
  public static PermissionDenied ofForbiddenFailure(String message) {
    return ofPermissionDenied(message);
  }
}