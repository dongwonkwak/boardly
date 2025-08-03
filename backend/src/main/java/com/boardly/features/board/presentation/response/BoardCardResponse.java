package com.boardly.features.board.presentation.response;

import com.boardly.features.card.domain.model.Card;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.List;

/**
 * 보드 카드 응답 DTO
 * 
 * @param cardId          카드 ID
 * @param title           제목
 * @param description     설명
 * @param position        위치
 * @param priority        우선순위
 * @param isCompleted     완료 여부
 * @param isArchived      아카이브 여부
 * @param dueDate         마감일
 * @param startDate       시작일
 * @param labels          라벨 목록
 * @param assignees       담당자 목록
 * @param attachmentCount 첨부파일 개수
 * @param commentCount    댓글 개수
 * @param lastCommentAt   마지막 댓글 시간
 * @param createdBy       생성자
 * @param createdAt       생성 시간
 * @param updatedAt       수정 시간
 * @param completedAt     완료 시간
 * @param completedBy     완료자
 * 
 * @since 1.0.0
 */
public record BoardCardResponse(
        String cardId,
        String title,
        String description,
        int position,
        String priority,
        boolean isCompleted,
        boolean isArchived,
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant dueDate,
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant startDate,
        List<CardLabelResponse> labels,
        List<CardAssigneeResponse> assignees,
        int attachmentCount,
        int commentCount,
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant lastCommentAt,
        CardUserResponse createdBy,
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant updatedAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant completedAt,
        CardUserResponse completedBy) {

    /**
     * Card 도메인 모델을 BoardCardResponse로 변환합니다.
     * 
     * @param card          변환할 Card 도메인 모델
     * @param labels        라벨 목록
     * @param assignees     담당자 목록
     * @param createdBy     생성자 정보
     * @param completedBy   완료자 정보
     * @param lastCommentAt 마지막 댓글 시간
     * @param completedAt   완료 시간
     * @return BoardCardResponse 객체
     */
    public static BoardCardResponse from(Card card, List<CardLabelResponse> labels,
            List<CardAssigneeResponse> assignees, CardUserResponse createdBy,
            CardUserResponse completedBy, Instant lastCommentAt, Instant completedAt) {
        return new BoardCardResponse(
                card.getCardId().getId(),
                card.getTitle(),
                card.getDescription(),
                card.getPosition(),
                "medium", // 기본값, 실제로는 Card 도메인에 priority 필드가 필요
                false, // 기본값, 실제로는 Card 도메인에 isCompleted 필드가 필요
                card.isArchived(),
                card.getDueDate(),
                null, // Card 도메인에 startDate 필드가 없음
                labels,
                assignees,
                card.getAttachmentsCount(),
                card.getCommentsCount(),
                lastCommentAt,
                createdBy,
                card.getCreatedAt(),
                card.getUpdatedAt(),
                completedAt,
                completedBy);
    }
}