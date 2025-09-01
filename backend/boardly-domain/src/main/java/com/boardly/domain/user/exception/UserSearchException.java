package com.boardly.domain.user.exception;

/**
 * 사용자 검색 실패 시 발생하는 예외
 */
public class UserSearchException extends Exception {

    public UserSearchException(String message) {
        super(message);
    }

    public UserSearchException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 잘못된 검색 조건으로 인한 실패
     */
    public static class InvalidCriteriaException extends UserSearchException {
        public InvalidCriteriaException(String criteria) {
            super("Invalid search criteria: " + criteria);
        }
    }

    /**
     * 검색 권한 없음으로 인한 실패
     */
    public static class SearchPermissionDeniedException extends UserSearchException {
        public SearchPermissionDeniedException() {
            super("Permission denied for user search");
        }
    }
}
