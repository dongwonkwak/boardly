package com.boardly.features.card.infrastructure.persistence;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.model.CardPriority;
import com.boardly.features.card.domain.valueobject.CardMember;
import com.boardly.features.user.domain.model.UserId;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cards", indexes = {
        @Index(name = "idx_card_list_id", columnList = "list_id"),
        @Index(name = "idx_card_position", columnList = "list_id, position"),
        @Index(name = "idx_card_due_date", columnList = "due_date"),
        @Index(name = "idx_card_archived", columnList = "archived")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardEntity {
    @Id
    @Column(name = "card_id", nullable = false, length = 36)
    private String cardId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT", length = 2000)
    private String description;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "archived", nullable = false, columnDefinition = "boolean default false")
    private boolean archived = false;

    @Column(name = "priority", nullable = false, length = 20, columnDefinition = "varchar(20) default 'medium'")
    private String priority = "medium";

    @Column(name = "is_completed", nullable = false, columnDefinition = "boolean default false")
    private boolean isCompleted = false;

    @Column(name = "list_id", nullable = false)
    private String listId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // 담당자들 (Many-to-Many 관계)
    @OneToMany(mappedBy = "cardId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<CardMemberEntity> assignedMembers = new HashSet<>();

    @Version
    @Column(name = "version")
    private Long version;

    @Builder
    private CardEntity(String cardId, String title, String description,
            int position, String listId,
            Instant dueDate, Instant startDate, boolean archived, String priority, boolean isCompleted,
            Instant createdAt, Instant updatedAt,
            Set<CardMemberEntity> assignedMembers) {
        this.cardId = cardId;
        this.title = title;
        this.description = description;
        this.position = position;
        this.listId = listId;
        this.dueDate = dueDate;
        this.startDate = startDate;
        this.archived = archived;
        this.priority = priority != null ? priority : "medium";
        this.isCompleted = isCompleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.assignedMembers = assignedMembers;
    }

    /**
     * Domain Card 객체로 변환
     */
    public Card toDomainEntity() {
        Set<CardMember> members = assignedMembers.stream()
                .map(CardMemberEntity::toDomainVO)
                .collect(Collectors.toSet());

        return Card.restore(
                new CardId(this.cardId),
                this.title,
                this.description,
                this.position,
                this.dueDate,
                this.startDate,
                this.archived,
                new ListId(this.listId),
                CardPriority.fromValue(this.priority),
                this.isCompleted,
                members,
                this.createdAt,
                this.updatedAt);
    }

    /**
     * Domain Card 객체로부터 Entity 생성
     */
    public static CardEntity from(Card card) {
        var entity = CardEntity.builder()
                .cardId(card.getCardId().getId())
                .title(card.getTitle())
                .description(card.getDescription())
                .position(card.getPosition())
                .listId(card.getListId().getId())
                .dueDate(card.getDueDate())
                .startDate(card.getStartDate())
                .archived(card.isArchived())
                .priority(card.getPriority().getValue())
                .isCompleted(card.isCompleted())
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .build();

        card.getAssignedMembers().forEach(member -> {
            entity.assignedMembers.add(CardMemberEntity.from(card.getCardId().getId(), member));
        });

        return entity;
    }

    /**
     * 도메인 객체의 변경사항을 반영
     */
    public void updateFromDomainEntity(Card card) {
        this.title = card.getTitle();
        this.description = card.getDescription();
        this.position = card.getPosition();
        this.listId = card.getListId().getId();
        this.dueDate = card.getDueDate();
        this.startDate = card.getStartDate();
        this.priority = card.getPriority().getValue();
        this.isCompleted = card.isCompleted();
        this.updatedAt = Instant.now();
        updateAssignedMembers(card.getAssignedMembers());
    }

    /**
     * 담당자 정보 업데이트
     */
    private void updateAssignedMembers(Set<CardMember> newMembers) {
        // 기존 담당자 제거
        assignedMembers.clear();

        // 새 담당자 추가
        Set<CardMemberEntity> newMemberEntities = newMembers.stream()
                .map(member -> CardMemberEntity.from(cardId, member))
                .collect(Collectors.toSet());

        assignedMembers.addAll(newMemberEntities);
    }

    /**
     * 제목 업데이트
     */
    public void updateTitle(String title) {
        this.title = title;
        this.updatedAt = Instant.now();
    }

    /**
     * 설명 업데이트
     */
    public void updateDescription(String description) {
        this.description = description;
        this.updatedAt = Instant.now();
    }

    /**
     * 위치 업데이트
     */
    public void updatePosition(int position) {
        this.position = position;
        this.updatedAt = Instant.now();
    }

    /**
     * 리스트 ID 업데이트 (카드 이동)
     */
    public void updateListId(String listId) {
        this.listId = listId;
        this.updatedAt = Instant.now();
    }

    /**
     * 담당자 추가
     */
    public void addMember(UserId userId) {
        CardMemberEntity memberEntity = CardMemberEntity.create(cardId, userId.getId());
        assignedMembers.add(memberEntity);
        this.updatedAt = Instant.now();
    }

    /**
     * 담당자 제거
     */
    public void removeMember(UserId userId) {
        assignedMembers.removeIf(member -> member.getUserId().equals(userId.getId()));
        this.updatedAt = Instant.now();
    }

    /**
     * 우선순위 업데이트
     */
    public void updatePriority(String priority) {
        this.priority = priority != null ? priority : "medium";
        this.updatedAt = Instant.now();
    }

    /**
     * 완료 상태 업데이트
     */
    public void updateCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
        this.updatedAt = Instant.now();
    }

    /**
     * 시작일 업데이트
     */
    public void updateStartDate(Instant startDate) {
        this.startDate = startDate;
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        CardEntity that = (CardEntity) obj;
        return cardId != null && cardId.equals(that.cardId);
    }

    @Override
    public int hashCode() {
        return cardId != null ? cardId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format(
                "CardEntity{cardId='%s', title='%s', position=%d, listId='%s', createdAt=%s, updatedAt=%s}",
                cardId, title, position, listId, createdAt, updatedAt);
    }
}
