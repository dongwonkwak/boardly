package com.boardly.features.card.domain.model;

import java.time.Instant;
import java.util.List;

import com.boardly.features.card.domain.valueobject.CardMember;
import com.boardly.features.label.domain.model.Label;
import com.boardly.features.user.domain.model.User;
import com.boardly.shared.domain.common.BaseEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카드 상세 정보 도메인 모델
 * 
 * <p>
 * 카드의 모든 상세 정보를 포함하는 집계 루트입니다.
 * 카드, 라벨, 멤버, 첨부파일, 댓글, 활동 내역 등의 정보를 포함합니다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CardDetail extends BaseEntity {

    // 카드 기본 정보
    private CardId cardId;
    private String title;
    private String description;
    private int position;
    private String priority;
    private boolean isCompleted;
    private boolean isArchived;
    private Instant dueDate;
    private Instant startDate;
    private Instant completedAt;
    private User completedBy;

    // 라벨 정보
    private List<Label> labels;

    // 멤버 정보
    private List<CardMember> assignees;

    // 첨부파일 정보
    private List<CardAttachment> attachments;

    // 댓글 정보
    private List<CardComment> comments;

    // 보드 멤버 정보
    private List<BoardMember> boardMembers;

    // 보드 라벨 정보
    private List<Label> boardLabels;

    // 활동 내역
    private List<CardActivity> activities;

    // 생성자 정보
    private User createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * 카드 상세 정보 생성
     */
    public static CardDetail of(
            Card card,
            List<Label> labels,
            List<CardMember> assignees,
            List<CardAttachment> attachments,
            List<CardComment> comments,
            List<BoardMember> boardMembers,
            List<Label> boardLabels,
            List<CardActivity> activities,
            User createdBy) {

        return CardDetail.builder()
                .cardId(card.getCardId())
                .title(card.getTitle())
                .description(card.getDescription())
                .position(card.getPosition())
                .priority(card.getPriority() != null ? card.getPriority().getValue() : null)
                .isCompleted(card.isCompleted())
                .isArchived(card.isArchived())
                .dueDate(card.getDueDate())
                .startDate(card.getStartDate())
                .completedAt(null) // Card 모델에는 completedAt 필드가 없음
                .completedBy(null) // Card 모델에는 completedBy 필드가 없음
                .labels(labels)
                .assignees(assignees)
                .attachments(attachments)
                .comments(comments)
                .boardMembers(boardMembers)
                .boardLabels(boardLabels)
                .activities(activities)
                .createdBy(createdBy)
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .build();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        CardDetail that = (CardDetail) obj;
        return cardId != null && cardId.equals(that.cardId);
    }

    @Override
    public int hashCode() {
        return cardId != null ? cardId.hashCode() : 0;
    }
}
