package com.boardly.features.board.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.boardly.features.board.domain.model.BoardMember;
import com.boardly.features.board.domain.model.BoardMemberId;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.model.BoardRole;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.BaseEntity;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "board_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardMemberEntity extends BaseEntity {

    @Id
    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "board_id", nullable = false)
    private String boardId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private BoardRole role;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Version
    @Column(name = "version")
    private Long version;

    @Builder
    private BoardMemberEntity(String memberId, String boardId, String userId,
            BoardRole role, boolean isActive,
            Instant createdAt, Instant updatedAt) {
        super(createdAt, updatedAt);
        this.memberId = memberId;
        this.boardId = boardId;
        this.userId = userId;
        this.role = role;
        this.isActive = isActive;
    }

    /**
     * Domain BoardMember 객체로 변환
     */
    public BoardMember toDomainEntity() {
        return BoardMember.builder()
                .memberId(new BoardMemberId(memberId))
                .boardId(new BoardId(boardId))
                .userId(new UserId(userId))
                .role(role)
                .isActive(isActive)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }

    /**
     * Domain BoardMember 객체로부터 엔티티 생성
     */
    public static BoardMemberEntity fromDomainEntity(BoardMember boardMember) {
        return BoardMemberEntity.builder()
                .memberId(boardMember.getMemberId().getId())
                .boardId(boardMember.getBoardId().getId())
                .userId(boardMember.getUserId().getId())
                .role(boardMember.getRole())
                .isActive(boardMember.isActive())
                .createdAt(boardMember.getCreatedAt())
                .updatedAt(boardMember.getUpdatedAt())
                .build();
    }

    /**
     * 엔티티 업데이트
     */
    public void updateFromDomain(BoardMember boardMember) {
        this.role = boardMember.getRole();
        this.isActive = boardMember.isActive();
        setUpdatedAt(boardMember.getUpdatedAt());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        BoardMemberEntity that = (BoardMemberEntity) obj;
        return Objects.equals(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId);
    }
}