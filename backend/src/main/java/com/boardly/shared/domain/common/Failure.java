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

  static Failure ofUnAuthorized(String message) {
    return new Unauthorized(message);
  }

  record ValidationFailure(String message, Collection<FieldViolation> violations) implements Failure {
  }

  record ConflictFailure(String message) implements Failure {
  }

  record NotFoundFailure(String message) implements Failure {
  }

  record InternalServerError(String message) implements Failure {
  }

  record Unauthorized(String message) implements Failure {
  }

  @Builder
  record FieldViolation(
    String field, 
    String message,
    Object rejectedValue) {
  }
}
