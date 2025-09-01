package com.boardly.domain.user.exception;

import com.boardly.domain.user.UserId;

/**
 * 사용자를 찾을 수 없는 경우 발생하는 예외
 */
public class UserNotFoundException extends Exception {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(UserId userId) {
        super("User not found with id: " + userId.getValue());
    }

    public UserNotFoundException(String field, String value) {
        super("User not found with " + field + ": " + value);
    }
}
