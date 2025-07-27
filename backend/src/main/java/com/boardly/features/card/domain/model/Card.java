package com.boardly.features.card.domain.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.card.domain.valueobject.CardMember;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.BaseEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Card extends BaseEntity {
    private CardId cardId;
    private String title;
    private String description;
    private int position;
    private ListId listId;
    private Instant dueDate;
    private boolean isArchived;

    // 담당자들 (Many-to-Many)
    private Set<CardMember> assignedMembers;

    // 연관 엔티티 개수 (성능 최적화용)
    private int commentsCount;
    private int attachmentsCount;
    private int labelsCount;

    @Builder
    private Card(CardId cardId, String title, String description, int position, ListId listId, Instant createdAt,
            Instant updatedAt, Instant dueDate, boolean isArchived, Set<CardMember> assignedMembers, int commentsCount,
            int attachmentsCount, int labelsCount) {
        super(createdAt, updatedAt);
        this.cardId = cardId;
        this.title = title;
        this.description = description;
        this.position = position;
        this.listId = listId;
        this.dueDate = dueDate;
        this.isArchived = isArchived;
    }

    /**
     * 새 카드 생성 (팩토리 메서드)
     */
    public static Card create(String title, String description, int position, ListId listId) {
        return Card.builder()
                .cardId(new CardId())
                .title(title.trim())
                .description(description != null ? description.trim() : null)
                .position(position)
                .listId(listId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .dueDate(null)
                .isArchived(false)
                .assignedMembers(new HashSet<>())
                .commentsCount(0)
                .attachmentsCount(0)
                .labelsCount(0)
                .build();
    }

    /**
     * 기존 카드 복원 (리포지토리용)
     */
    public static Card restore(CardId cardId, String title, String description, int position,
            Instant dueDate, boolean archived, ListId listId,
            Set<CardMember> assignedMembers, int commentsCount,
            int attachmentsCount, int labelsCount,
            Instant createdAt, Instant updatedAt) {
        return Card.builder()
                .cardId(cardId)
                .title(title)
                .description(description)
                .position(position)
                .listId(listId)
                .dueDate(dueDate)
                .isArchived(archived)
                .assignedMembers(assignedMembers != null ? assignedMembers : new HashSet<>())
                .commentsCount(commentsCount)
                .attachmentsCount(attachmentsCount)
                .labelsCount(labelsCount)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    /**
     * 카드 제목을 수정합니다.
     */
    public void updateTitle(String title) {
        this.title = title.trim();
        markAsUpdated();
    }

    /**
     * 카드 설명을 수정합니다.
     */
    public void updateDescription(String description) {
        this.description = description != null ? description.trim() : null;
        markAsUpdated();
    }

    /**
     * 카드 위치를 수정합니다.
     */
    public void updatePosition(int position) {
        this.position = position;
        markAsUpdated();
    }

    /**
     * 카드가 속한 리스트를 변경합니다.
     */
    public void moveToList(ListId newListId) {
        this.listId = newListId;
        markAsUpdated();
    }

    /**
     * 카드를 수정합니다.
     */
    public void update(String title, String description) {
        this.title = title.trim();
        this.description = description != null ? description.trim() : null;
        markAsUpdated();
    }

    /**
     * 카드 마감일을 설정합니다.
     */
    public void setDueDate(Instant dueDate) {
        this.dueDate = dueDate;
        markAsUpdated();
    }

    /**
     * 카드 마감일을 제거합니다.
     */
    public void removeDueDate() {
        this.dueDate = null;
        markAsUpdated();
    }

    /**
     * 카드를 아카이브합니다.
     */
    public void archive() {
        this.isArchived = true;
        markAsUpdated();
    }

    /**
     * 카드를 언아카이브합니다.
     */
    public void unarchive() {
        this.isArchived = false;
        markAsUpdated();
    }

    public void assignMember(UserId userId) {
        if (assignedMembers == null) {
            assignedMembers = new HashSet<>();
        }
        if (assignedMembers.stream().anyMatch(member -> member.getUserId().equals(userId))) {
            return;
        }
        assignedMembers.add(new CardMember(userId));
        markAsUpdated();
    }

    public void unassignMember(UserId userId) {
        if (assignedMembers == null) {
            return;
        }
        assignedMembers.removeIf(member -> member.getUserId().equals(userId));
        markAsUpdated();
    }

    /**
     * 댓글 수 증가
     */
    public void incrementCommentsCount() {
        this.commentsCount++;
        markAsUpdated();
    }

    /**
     * 댓글 수 감소
     */
    public void decrementCommentsCount() {
        if (this.commentsCount > 0) {
            this.commentsCount--;
            markAsUpdated();
        }
    }

    /**
     * 첨부파일 수 증가
     */
    public void incrementAttachmentsCount() {
        this.attachmentsCount++;
        markAsUpdated();
    }

    /**
     * 첨부파일 수 감소
     */
    public void decrementAttachmentsCount() {
        if (this.attachmentsCount > 0) {
            this.attachmentsCount--;
            markAsUpdated();
        }
    }

    /**
     * 라벨 수 증가
     */
    public void incrementLabelsCount() {
        this.labelsCount++;
        markAsUpdated();
    }

    /**
     * 라벨 수 감소
     */
    public void decrementLabelsCount() {
        if (this.labelsCount > 0) {
            this.labelsCount--;
            markAsUpdated();
        }
    }

    /**
     * 마감일 초과 여부 확인
     */
    public boolean isOverdue() {
        if (dueDate == null) {
            return false;
        }
        return dueDate.isBefore(Instant.now());
    }

    /**
     * 특정 사용자가 담당자인지 확인
     */
    public boolean isAssignedTo(UserId userId) {
        if (assignedMembers == null) {
            return false;
        }
        return assignedMembers.stream()
                .anyMatch(member -> member.getUserId().equals(userId));
    }

    /**
     * 카드를 복제합니다.
     * 새로운 카드 ID를 생성하고, 제목과 위치를 변경합니다.
     * 생성 시간은 현재 시간으로 설정합니다.
     * 업데이트 시간은 생성 시간으로 설정합니다.
     */
    public Card clone(String newTitle, int newPosition) {
        return Card.builder()
                .cardId(new CardId())
                .title(newTitle.trim())
                .description(description != null ? description.trim() : null)
                .position(newPosition)
                .listId(listId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * 카드를 새로운 리스트로 이동합니다.
     */
    public void moveToList(ListId newListId, int newPosition) {
        this.listId = newListId;
        this.position = newPosition;
        markAsUpdated();
    }

    /**
     * 카드를 다른 리스트로 복사합니다.
     */
    public Card cloneToList(String newTitle, ListId newListId, int newPosition) {
        return Card.builder()
                .cardId(new CardId())
                .title(newTitle.trim())
                .description(description != null ? description.trim() : null)
                .position(newPosition)
                .listId(newListId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Card card = (Card) obj;
        return Objects.equals(cardId, card.cardId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardId);
    }

    @Override
    public String toString() {
        return String.format("Card{cardId=%s, title='%s', position=%d, listId=%s, createdAt=%s, updatedAt=%s}",
                cardId, title, position, listId, getCreatedAt(), getUpdatedAt());
    }
}
