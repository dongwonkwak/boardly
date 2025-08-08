package com.boardly.features.card.presentation.response;

import java.time.Instant;
import java.util.List;

import com.boardly.features.card.domain.model.CardDetail;
import com.boardly.features.label.presentation.response.LabelResponse;
import com.boardly.features.user.presentation.response.UserResponse;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 카드 상세 정보 응답 DTO
 */
@Schema(description = "카드 상세 정보 응답")
public record CardDetailResponse(
        @Schema(description = "카드 ID", example = "card-1") String cardId,

        @Schema(description = "카드 제목", example = "메인 페이지 디자인") String title,

        @Schema(description = "카드 설명", example = "사용자 인터페이스 디자인 작업") String description,

        @Schema(description = "카드 위치", example = "1") int position,

        @Schema(description = "우선순위", example = "high") String priority,

        @Schema(description = "완료 여부", example = "false") boolean isCompleted,

        @Schema(description = "아카이브 여부", example = "false") boolean isArchived,

        @Schema(description = "마감일", example = "2025-08-13T09:21:33.436705Z") Instant dueDate,

        @Schema(description = "시작일", example = "2025-08-01T09:00:00.000Z") Instant startDate,

        @Schema(description = "완료일", example = "null") Instant completedAt,

        @Schema(description = "완료자 정보") UserResponse completedBy,

        @Schema(description = "라벨 목록") List<LabelResponse> labels,

        @Schema(description = "담당자 목록") List<AssigneeResponse> assignees,

        @Schema(description = "첨부파일 목록") List<AttachmentResponse> attachments,

        @Schema(description = "댓글 목록") List<CommentResponse> comments,

        @Schema(description = "보드 멤버 목록") List<BoardMemberResponse> boardMembers,

        @Schema(description = "보드 라벨 목록") List<LabelResponse> boardLabels,

        @Schema(description = "활동 내역 목록") List<ActivityResponse> activities,

        @Schema(description = "생성자 정보") UserResponse createdBy,

        @Schema(description = "생성일", example = "2025-08-06T09:21:33.436705Z") Instant createdAt,

        @Schema(description = "수정일", example = "2025-08-07T16:45:30.000Z") Instant updatedAt) {

    /**
     * CardDetail에서 CardDetailResponse 생성
     */
    public static CardDetailResponse from(CardDetail cardDetail) {
        return new CardDetailResponse(
                cardDetail.getCardId().getId(),
                cardDetail.getTitle(),
                cardDetail.getDescription(),
                cardDetail.getPosition(),
                cardDetail.getPriority(),
                cardDetail.isCompleted(),
                cardDetail.isArchived(),
                cardDetail.getDueDate(),
                cardDetail.getStartDate(),
                cardDetail.getCompletedAt(),
                cardDetail.getCompletedBy() != null ? UserResponse.from(cardDetail.getCompletedBy())
                        : null,
                cardDetail.getLabels() != null ? cardDetail.getLabels().stream()
                        .map(LabelResponse::from)
                        .toList() : List.of(),
                cardDetail.getAssignees() != null ? cardDetail.getAssignees().stream()
                        .map(AssigneeResponse::from)
                        .toList() : List.of(),
                cardDetail.getAttachments() != null ? cardDetail.getAttachments().stream()
                        .map(AttachmentResponse::from)
                        .toList() : List.of(),
                cardDetail.getComments() != null ? cardDetail.getComments().stream()
                        .map(CommentResponse::from)
                        .toList() : List.of(),
                cardDetail.getBoardMembers() != null ? cardDetail.getBoardMembers().stream()
                        .map(BoardMemberResponse::from)
                        .toList() : List.of(),
                cardDetail.getBoardLabels() != null ? cardDetail.getBoardLabels().stream()
                        .map(LabelResponse::from)
                        .toList() : List.of(),
                cardDetail.getActivities() != null ? cardDetail.getActivities().stream()
                        .map(ActivityResponse::from)
                        .toList() : List.of(),
                cardDetail.getCreatedBy() != null ? UserResponse.from(cardDetail.getCreatedBy()) : null,
                cardDetail.getCreatedAt(),
                cardDetail.getUpdatedAt());
    }

    /**
     * 담당자 응답 DTO
     */
    @Schema(description = "담당자 정보")
    public record AssigneeResponse(
            @Schema(description = "사용자 ID", example = "user-2") String userId,

            @Schema(description = "이름", example = "제인") String firstName,

            @Schema(description = "성", example = "스미스") String lastName,

            @Schema(description = "이메일", example = "jane.smith@example.com") String email,

            @Schema(description = "할당일", example = "2025-08-06T09:21:33.436705Z") Instant assignedAt) {
        public static AssigneeResponse from(
                com.boardly.features.card.domain.valueobject.CardMember cardMember) {
            // TODO: User 정보는 별도로 조회해야 함
            return new AssigneeResponse(
                    cardMember.getUserId().getId(),
                    null, // firstName은 별도 조회 필요
                    null, // lastName은 별도 조회 필요
                    null, // email은 별도 조회 필요
                    null); // assignedAt은 CardMember에 없음
        }
    }

    /**
     * 첨부파일 응답 DTO
     */
    @Schema(description = "첨부파일 정보")
    public record AttachmentResponse(
            @Schema(description = "첨부파일 ID", example = "att-1") String attachmentId,

            @Schema(description = "파일명", example = "main-page-wireframe.png") String fileName,

            @Schema(description = "파일 크기", example = "2048576") long fileSize,

            @Schema(description = "MIME 타입", example = "image/png") String mimeType,

            @Schema(description = "업로드일", example = "2025-08-06T14:30:00.000Z") Instant uploadedAt,

            @Schema(description = "업로드자 정보") UserResponse uploadedBy,

            @Schema(description = "다운로드 URL", example = "https://files.example.com/attachments/att-1") String downloadUrl) {
        public static AttachmentResponse from(
                com.boardly.features.card.domain.model.CardAttachment attachment) {
            return new AttachmentResponse(
                    attachment.getAttachmentId().getId(),
                    attachment.getFileName(),
                    attachment.getFileSize(),
                    attachment.getMimeType(),
                    attachment.getUploadedAt(),
                    UserResponse.from(attachment.getUploadedBy()),
                    attachment.getDownloadUrl());
        }
    }

    /**
     * 댓글 응답 DTO
     */
    @Schema(description = "댓글 정보")
    public record CommentResponse(
            @Schema(description = "댓글 ID", example = "comment-1") String commentId,

            @Schema(description = "댓글 내용", example = "와이어프레임 초안 완료했습니다. 검토 부탁드려요.") String content,

            @Schema(description = "생성일", example = "2025-08-06T15:20:00.000Z") Instant createdAt,

            @Schema(description = "작성자 정보") UserResponse createdBy,

            @Schema(description = "수정일", example = "2025-08-06T15:20:00.000Z") Instant updatedAt,

            @Schema(description = "수정 여부", example = "false") boolean isEdited) {
        public static CommentResponse from(com.boardly.features.card.domain.model.CardComment comment) {
            return new CommentResponse(
                    comment.getCommentId().getId(),
                    comment.getContent(),
                    comment.getCreatedAt(),
                    UserResponse.from(comment.getCreatedBy()),
                    comment.getUpdatedAt(),
                    comment.isEdited());
        }
    }

    /**
     * 보드 멤버 응답 DTO
     */
    @Schema(description = "보드 멤버 정보")
    public record BoardMemberResponse(
            @Schema(description = "사용자 ID", example = "user-1") String userId,

            @Schema(description = "이름", example = "John") String firstName,

            @Schema(description = "성", example = "Doe") String lastName,

            @Schema(description = "이메일", example = "john.doe@example.com") String email,

            @Schema(description = "역할", example = "owner") String role,

            @Schema(description = "권한 목록", example = "[\"all\"]") List<String> permissions) {
        public static BoardMemberResponse from(com.boardly.features.card.domain.model.BoardMember boardMember) {
            return new BoardMemberResponse(
                    boardMember.getUser().getUserId().getId(),
                    boardMember.getUser().getFirstName(),
                    boardMember.getUser().getLastName(),
                    boardMember.getUser().getEmail(),
                    boardMember.getRole(),
                    boardMember.getPermissions());
        }
    }

    /**
     * 활동 내역 응답 DTO
     */
    @Schema(description = "활동 내역 정보")
    public record ActivityResponse(
            @Schema(description = "활동 ID", example = "activity-1") String activityId,

            @Schema(description = "활동 타입", example = "card_created") String type,

            @Schema(description = "활동 설명", example = "카드가 생성되었습니다") String description,

            @Schema(description = "생성일", example = "2025-08-06T09:21:33.436705Z") Instant createdAt,

            @Schema(description = "생성자 정보") UserResponse createdBy,

            @Schema(description = "대상 사용자 정보") UserResponse targetUser) {
        public static ActivityResponse from(com.boardly.features.card.domain.model.CardActivity activity) {
            return new ActivityResponse(
                    activity.getActivityId().getId(),
                    activity.getType(),
                    activity.getDescription(),
                    activity.getCreatedAt(),
                    UserResponse.from(activity.getCreatedBy()),
                    activity.getTargetUser() != null ? UserResponse.from(activity.getTargetUser())
                            : null);
        }
    }
}
