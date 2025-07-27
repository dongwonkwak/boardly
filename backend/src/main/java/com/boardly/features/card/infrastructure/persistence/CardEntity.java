package com.boardly.features.card.infrastructure.persistence;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.Instant;

import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.valueobject.CardMember;
import com.boardly.features.boardlist.domain.model.ListId;
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

    @Column(name = "archived", nullable = false, columnDefinition = "boolean default false")
    private boolean archived = false;

    @Column(name = "list_id", nullable = false)
    private String listId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // 담당자들 (Many-to-Many 관계)
    @OneToMany(mappedBy = "cardId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<CardMemberEntity> assignedMembers = new HashSet<>();

    // 성능 최적화를 위한 카운트 필드들
    @Column(name = "comments_count", nullable = false, columnDefinition = "int default 0")
    private int commentsCount = 0;

    @Column(name = "attachments_count", nullable = false, columnDefinition = "int default 0")
    private int attachmentsCount = 0;

    @Column(name = "labels_count", nullable = false, columnDefinition = "int default 0")
    private int labelsCount = 0;

    @Version
    @Column(name = "version")
    private Long version;

    @Builder
    private CardEntity(String cardId, String title, String description,
            int position, String listId,
            Instant dueDate, boolean archived,
            Instant createdAt, Instant updatedAt,
            Set<CardMemberEntity> assignedMembers,
            int commentsCount, int attachmentsCount, int labelsCount) {
        this.cardId = cardId;
        this.title = title;
        this.description = description;
        this.position = position;
        this.listId = listId;
        this.dueDate = dueDate;
        this.archived = archived;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.assignedMembers = assignedMembers;
        this.commentsCount = commentsCount;
        this.attachmentsCount = attachmentsCount;
        this.labelsCount = labelsCount;
    }

    /**
     * Domain Card 객체로 변환
     */
    public Card toDomainEntity() {
        Set<CardMember> members = assignedMembers.stream()
                .map(CardMemberEntity::toDomainVO)
                .collect(Collectors.toSet());

        return Card.builder()
                .cardId(new CardId(this.cardId))
                .title(this.title)
                .description(this.description)
                .position(this.position)
                .listId(new ListId(this.listId))
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .dueDate(this.dueDate)
                .isArchived(this.archived)
                .assignedMembers(members)
                .commentsCount(this.commentsCount)
                .attachmentsCount(this.attachmentsCount)
                .labelsCount(this.labelsCount)
                .build();
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
                .archived(card.isArchived())
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .commentsCount(card.getCommentsCount())
                .attachmentsCount(card.getAttachmentsCount())
                .labelsCount(card.getLabelsCount())
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
        this.updatedAt = Instant.now();
        updateAssignedMembers(card.getAssignedMembers());
        this.commentsCount = card.getCommentsCount();
        this.attachmentsCount = card.getAttachmentsCount();
        this.labelsCount = card.getLabelsCount();
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
     * 댓글 수 증가
     */
    public void incrementCommentsCount() {
        this.commentsCount++;
        this.updatedAt = Instant.now();
    }

    /**
     * 댓글 수 감소
     */
    public void decrementCommentsCount() {
        if (this.commentsCount > 0) {
            this.commentsCount--;
            this.updatedAt = Instant.now();
        }
    }

    /**
     * 첨부파일 수 증가
     */
    public void incrementAttachmentsCount() {
        this.attachmentsCount++;
        this.updatedAt = Instant.now();
    }

    /**
     * 첨부파일 수 감소
     */
    public void decrementAttachmentsCount() {
        if (this.attachmentsCount > 0) {
            this.attachmentsCount--;
            this.updatedAt = Instant.now();
        }
    }

    /**
     * 라벨 수 증가
     */
    public void incrementLabelsCount() {
        this.labelsCount++;
        this.updatedAt = Instant.now();
    }

    /**
     * 라벨 수 감소
     */
    public void decrementLabelsCount() {
        if (this.labelsCount > 0) {
            this.labelsCount--;
            this.updatedAt = Instant.now();
        }
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
