package com.boardly.features.card.infrastructure.persistence;

import java.time.Instant;

import com.boardly.features.card.domain.valueobject.CardMember;
import com.boardly.features.user.domain.model.UserId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

@Entity
@Table(name = "card_members", uniqueConstraints = @UniqueConstraint(columnNames = { "card_id", "user_id" }), indexes = {
        @Index(name = "idx_card_member_card_id", columnList = "card_id"),
        @Index(name = "idx_card_member_user_id", columnList = "user_id")
})
@Getter
public class CardMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_id", nullable = false, length = 36)
    private String cardId;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    protected CardMemberEntity() {
    }

    /**
     * 새 카드 담당자 생성
     */
    public static CardMemberEntity create(String cardId, String userId) {
        CardMemberEntity entity = new CardMemberEntity();
        entity.cardId = cardId;
        entity.userId = userId;
        entity.assignedAt = Instant.now();
        return entity;
    }

    /**
     * 도메인 값 객체로부터 엔티티 생성
     */
    public static CardMemberEntity from(String cardId, CardMember cardMember) {
        CardMemberEntity entity = new CardMemberEntity();
        entity.cardId = cardId;
        entity.userId = cardMember.getUserId().getId();
        entity.assignedAt = Instant.now(); // assignedAt은 현재 시간으로 설정
        return entity;
    }

    /**
     * 엔티티를 도메인 값 객체로 변환
     */
    public CardMember toDomainVO() {
        return new CardMember(new UserId(userId));
    }

    /**
     * 카드 ID와 사용자 ID를 기준으로 동등성 비교
     * 
     * @param obj 비교할 객체
     * @return 동등 여부
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        CardMemberEntity that = (CardMemberEntity) obj;
        return cardId != null && cardId.equals(that.cardId) &&
                userId != null && userId.equals(that.userId);
    }

    /**
     * 카드 ID와 사용자 ID를 기준으로 해시 코드 생성
     * 
     * @return 해시 코드
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(cardId, userId);
    }

    /**
     * 엔티티의 문자열 표현 반환
     * 
     * @return 엔티티 정보를 포함한 문자열
     */
    @Override
    public String toString() {
        return String.format("CardMemberEntity{cardId='%s', userId='%s', assignedAt=%s}",
                cardId, userId, assignedAt);
    }
}
