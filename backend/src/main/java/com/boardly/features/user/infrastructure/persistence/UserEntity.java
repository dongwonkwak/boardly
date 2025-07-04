package com.boardly.features.user.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.boardly.features.user.domain.model.User;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.features.user.domain.model.UserProfile;
import com.boardly.shared.domain.common.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "hashed_password", nullable = false)
    private String hashedPassword;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Version
    @Column(name = "version")
    private Long version;

    @Builder
    private UserEntity(String userId, String email, String hashedPassword, 
                      String firstName, String lastName, boolean isActive,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(createdAt, updatedAt);
        this.userId = userId;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = isActive;
    }

    /**
     * Domain User 객체로 변환
     */
    public User toDomainEntity() {
        return User.builder()
                .userId(new UserId(this.userId))
                .email(this.email)
                .hashedPassword(this.hashedPassword)
                .userProfile(new UserProfile(this.firstName, this.lastName))
                .isActive(this.isActive)
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .build();
    }

    /**
     * Domain User 객체에서 생성
     */
    public static UserEntity fromDomainEntity(User user) {
        return UserEntity.builder()
                .userId(user.getUserId().getId())
                .email(user.getEmail())
                .hashedPassword(user.getHashedPassword())
                .firstName(user.getUserProfile().firstName())
                .lastName(user.getUserProfile().lastName())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * 도메인 객체의 변경사항을 반영
     */
    public void updateFromDomainEntity(User user) {
        this.email = user.getEmail();
        this.hashedPassword = user.getHashedPassword();
        this.firstName = user.getUserProfile().firstName();
        this.lastName = user.getUserProfile().lastName();
        this.isActive = user.isActive();
        markAsUpdated(); // BaseEntity의 메서드 사용
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserEntity that = (UserEntity) obj;
        return userId != null && userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }
}
