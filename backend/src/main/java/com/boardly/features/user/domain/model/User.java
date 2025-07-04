package com.boardly.features.user.domain.model;

import java.time.LocalDateTime;
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
        UserProfile userProfile, boolean isActive, LocalDateTime createdAt,
        LocalDateTime updatedAt) {

        super(createdAt, updatedAt);
        this.userId = userId;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.userProfile = userProfile;
        this.isActive = isActive;
    }

    /**
     * 새로운 사용자를 생성합니다.
     */
    public static User create(String email, String hashedPassword, UserProfile userProfile) {
        return User.builder()
            .userId(new UserId())
            .email(email)
            .hashedPassword(hashedPassword)
            .userProfile(userProfile)
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
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

    public void deactivate() {
        this.isActive = false;
        markAsUpdated();
    }

    public void activate() {
        this.isActive = true;
        markAsUpdated();
    }

    public String getFullName() {
        return this.userProfile.getFullName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "User{" +
            "userId=" + userId +
            ", email='" + email + '\'' +
            ", isActive=" + isActive +
            '}';
    }
}
