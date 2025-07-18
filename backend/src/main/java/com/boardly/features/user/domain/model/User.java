package com.boardly.features.user.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.boardly.shared.domain.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    private UserId userId;
    private String email;
    @JsonIgnore
    private String hashedPassword;
    private UserProfile userProfile;
    private boolean isActive;

    @Builder
    private User(
        UserId userId, String email, String hashedPassword, 
        UserProfile userProfile, boolean isActive, Instant createdAt,
        Instant updatedAt) {

        super(createdAt, updatedAt);
        this.userId = userId;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.userProfile = userProfile;
        this.isActive = isActive;
    }

    /**
     * 새로운 사용자를 생성합니다. (UTC 기준)
     */
    public static User create(String email, String hashedPassword, UserProfile userProfile) {
        Instant now = Instant.now();
        return User.builder()
            .userId(new UserId())
            .email(email)
            .hashedPassword(hashedPassword)
            .userProfile(userProfile)
            .isActive(true)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    /**
     * 사용자 프로필을 업데이트합니다.
     */
    public void updateProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
        markAsUpdated();
    }

    /**
     * 비밀번호를 변경합니다.
     * 비즈니스 로직 검증은 Domain Service에서 수행
     */
    public void updatePassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
        markAsUpdated();
    }

    /**
     * 사용자를 비활성화합니다.
     */
    public void deactivate() {
        this.isActive = false;
        markAsUpdated();
    }

    /**
     * 사용자를 활성화합니다.
     */
    public void activate() {
        this.isActive = true;
        markAsUpdated();
    }

    public String getFullName() {
        return this.userProfile.getFullName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return String.format("User{userId=%s, email='%s', isActive=%s, createdAt=%s, updatedAt=%s}",
                userId, email, isActive, getCreatedAt(), getUpdatedAt());
    }
}
