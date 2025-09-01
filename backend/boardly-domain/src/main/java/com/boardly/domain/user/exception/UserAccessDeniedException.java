package com.boardly.domain.user.exception;

import com.boardly.domain.user.UserId;

/**
 * 사용자 접근 권한이 없는 경우 발생하는 예외
 */
public class UserAccessDeniedException extends Exception {

    public UserAccessDeniedException(String message) {
        super(message);
    }

    public UserAccessDeniedException(UserId requesterId, UserId targetUserId) {
        super("Access denied for user " + requesterId.getValue() +
                " to access user " + targetUserId.getValue());
    }

    public UserAccessDeniedException(UserId requesterId, String action) {
        super("Access denied for user " + requesterId.getValue() +
                " to perform action: " + action);
    }
}
