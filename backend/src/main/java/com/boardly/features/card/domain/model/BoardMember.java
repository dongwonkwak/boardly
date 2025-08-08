package com.boardly.features.card.domain.model;

import java.util.List;

import com.boardly.features.user.domain.model.User;
import com.boardly.shared.domain.common.BaseEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 보드 멤버 도메인 모델
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BoardMember extends BaseEntity {

    private User user;
    private String role;
    private List<String> permissions;

    /**
     * 보드 멤버 생성
     */
    public static BoardMember of(
            User user,
            String role,
            List<String> permissions) {

        return BoardMember.builder()
                .user(user)
                .role(role)
                .permissions(permissions)
                .build();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        BoardMember that = (BoardMember) obj;
        return user != null && user.equals(that.user);
    }

    @Override
    public int hashCode() {
        return user != null ? user.hashCode() : 0;
    }
}
