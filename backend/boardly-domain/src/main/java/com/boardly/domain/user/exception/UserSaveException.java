package com.boardly.domain.user.exception;

/**
 * 사용자 저장 실패 예외
 * 제약 조건 위반, 중복 등으로 인한 저장 실패 시 발생
 */
public class UserSaveException extends Exception {

    public UserSaveException(String message) {
        super(message);
    }

    public UserSaveException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 이메일 중복으로 인한 저장 실패
     */
    public static class EmailDuplicateException extends UserSaveException {
        public EmailDuplicateException(String email) {
            super("Email already exists: " + email);
        }
    }

    /**
     * 사용자명 중복으로 인한 저장 실패
     */
    public static class UsernameDuplicateException extends UserSaveException {
        public UsernameDuplicateException(String username) {
            super("Username already exists: " + username);
        }
    }

    /**
     * 제약 조건 위반으로 인한 저장 실패
     */
    public static class ConstraintViolationException extends UserSaveException {
        public ConstraintViolationException(String constraint) {
            super("Constraint violation: " + constraint);
        }
    }
}
