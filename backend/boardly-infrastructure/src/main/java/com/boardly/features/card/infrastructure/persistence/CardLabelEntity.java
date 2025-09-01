package com.boardly.features.card.infrastructure.persistence;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Index;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;

/**
 * 카드-라벨 연결 JPA 엔티티 (Many-to-Many 연결 테이블)
 */
@Entity
@Table(name = "card_labels", uniqueConstraints = @UniqueConstraint(columnNames = { "card_id", "label_id" }), indexes = {
        @Index(name = "idx_card_label_card_id", columnList = "card_id"),
        @Index(name = "idx_card_label_label_id", columnList = "label_id")
})
public class CardLabelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_id", nullable = false, length = 50)
    private String cardId;

    @Column(name = "label_id", nullable = false, length = 50)
    private String labelId;

    @Column(name = "applied_at", nullable = false)
    private Instant appliedAt;

    // 기본 생성자 (JPA 필수)
    protected CardLabelEntity() {
    }

    /**
     * 새 카드-라벨 연결 생성
     */
    public static CardLabelEntity create(String cardId, String labelId) {
        CardLabelEntity entity = new CardLabelEntity();
        entity.cardId = cardId;
        entity.labelId = labelId;
        entity.appliedAt = Instant.now();
        return entity;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getLabelId() {
        return labelId;
    }

    public void setLabelId(String labelId) {
        this.labelId = labelId;
    }

    public Instant getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(Instant appliedAt) {
        this.appliedAt = appliedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        CardLabelEntity that = (CardLabelEntity) obj;
        return cardId != null && cardId.equals(that.cardId) &&
                labelId != null && labelId.equals(that.labelId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(cardId, labelId);
    }

    @Override
    public String toString() {
        return String.format("CardLabelEntity{cardId='%s', labelId='%s', appliedAt=%s}",
                cardId, labelId, appliedAt);
    }
}
