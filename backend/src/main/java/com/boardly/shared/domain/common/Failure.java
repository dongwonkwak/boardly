package com.boardly.shared.domain.common;

import java.util.Collection;

import lombok.Builder;

public interface Failure {
  String message();

  static Failure ofValidation(String message, Collection<FieldViolation> violations) {
    return new ValidationFailure(message, violations);
  }

  static Failure ofConflict(String message) {
    return new ConflictFailure(message);
  }

  static Failure ofInternalServerError(String message) {
    return new InternalServerError(message);
  }

  static Failure ofNotFound(String message) {
    return new NotFoundFailure(message);
  }

  static Failure ofForbidden(String message) {
    return new ForbiddenFailure(message);
  }

  static Failure ofBadRequest(String message) {
    return new BadRequestFailure(message);
  }

  record ValidationFailure(String message, Collection<FieldViolation> violations) implements Failure {
  }

  record ConflictFailure(String message) implements Failure {
  }

  record NotFoundFailure(String message) implements Failure {
  }

  record InternalServerError(String message) implements Failure {
  }

  public record ForbiddenFailure(String message) implements Failure {
  }

  public record BadRequestFailure(String message) implements Failure {
  }

  @Builder
  record FieldViolation(
      String field,
      String message,
      Object rejectedValue) {
  }
}
