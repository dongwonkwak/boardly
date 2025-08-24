package com.boardly.domain.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 사용자 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    private UserId id;
    private String email;
    private String username;
    private String displayName;
    private String profileImageUrl;
    private UserStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    @Builder
    public User(UserId id, String email, String username, String displayName, String profileImageUrl, UserStatus status,
            Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.displayName = displayName;
        this.profileImageUrl = profileImageUrl;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 사용자 생성 팩토리 메서드
     */
    public static User create(String email, String username, String displayName, String profileImageUrl) {
        return User.builder()
                .id(UserId.generate())
                .email(email)
                .username(username)
                .displayName(displayName)
                .profileImageUrl(profileImageUrl)
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * 사용자 정보 업데이트
     */
    public void updateInfo(String username, String displayName, String profileImageUrl) {
        this.username = username;
        this.displayName = displayName;
        this.profileImageUrl = profileImageUrl;
        this.updatedAt = Instant.now();
    }

    /**
     * 사용자 상태 변경
     */
    public void changeStatus(UserStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    /**
     * 활성 사용자인지 확인
     */
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    /**
     * 표시 이름 반환 (displayName이 없으면 username 반환)
     */
    public String getDisplayName() {
        return displayName != null && !displayName.trim().isEmpty() ? displayName : username;
    }

    /**
     * @username 형태로 반환
     */
    public String getUsernameWithAt() {
        return "@" + username;
    }
}
