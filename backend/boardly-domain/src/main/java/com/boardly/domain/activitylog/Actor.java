package com.boardly.domain.activitylog;

import com.boardly.domain.user.Email;
import com.boardly.domain.user.UserId;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 활동을 수행한 사용자 정보
 * User 도메인의 일부 필드만 포함하는 값 객체
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Actor {

    private UserId id;
    private Email email;
    private String displayName;

    @Builder
    public Actor(UserId id, Email email, String displayName) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
    }

    /**
     * User 객체로부터 Actor 생성
     */
    public static Actor from(com.boardly.domain.user.User user) {
        return Actor.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .build();
    }
}
