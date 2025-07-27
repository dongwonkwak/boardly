package com.boardly.features.comment.application.validation;

import org.springframework.stereotype.Component;

import com.boardly.features.comment.application.port.input.CreateCommentCommand;
import com.boardly.features.comment.application.port.input.UpdateCommentCommand;
import com.boardly.features.comment.application.port.input.DeleteCommentCommand;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

/**
 * 댓글 통합 검증기
 * 
 * <p>
 * 모든 댓글 관련 Command들의 입력 검증을 담당합니다.
 * Create, Update, Delete 각각의 검증 로직을 통합하여 관리합니다.
 * 
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class CommentValidator {

    private final CommonValidationRules commonValidationRules;
    private final ValidationMessageResolver messageResolver;

    // 상수 정의
    private static final int COMMENT_CONTENT_MAX_LENGTH = 1000;

    /**
     * 댓글 생성 커맨드 검증
     */
    public ValidationResult<CreateCommentCommand> validateCreate(CreateCommentCommand command) {
        return getCreateValidator().validate(command);
    }

    /**
     * 댓글 수정 커맨드 검증
     */
    public ValidationResult<UpdateCommentCommand> validateUpdate(UpdateCommentCommand command) {
        return getUpdateValidator().validate(command);
    }

    /**
     * 댓글 삭제 커맨드 검증
     */
    public ValidationResult<DeleteCommentCommand> validateDelete(DeleteCommentCommand command) {
        return getDeleteValidator().validate(command);
    }

    /**
     * 댓글 생성 검증 규칙
     */
    private Validator<CreateCommentCommand> getCreateValidator() {
        return Validator.combine(
                // 카드 ID 검증: 필수
                commonValidationRules.cardIdRequired(CreateCommentCommand::cardId),
                // 작성자 ID 검증: 필수
                authorIdRequired(CreateCommentCommand::authorId),
                // 댓글 내용 검증: 필수, 최대 1000자
                commentContentRequired(CreateCommentCommand::content),
                commentContentMaxLength(CreateCommentCommand::content));
    }

    /**
     * 댓글 수정 검증 규칙
     */
    private Validator<UpdateCommentCommand> getUpdateValidator() {
        return Validator.combine(
                // 댓글 ID 검증: 필수
                commonValidationRules.commentIdRequired(UpdateCommentCommand::commentId),
                // 요청자 ID 검증: 필수
                requesterIdRequired(UpdateCommentCommand::requesterId),
                // 댓글 내용 검증: 필수, 최대 1000자
                commentContentRequired(UpdateCommentCommand::content),
                commentContentMaxLength(UpdateCommentCommand::content));
    }

    /**
     * 댓글 삭제 검증 규칙
     */
    private Validator<DeleteCommentCommand> getDeleteValidator() {
        return Validator.combine(
                // 댓글 ID 검증: 필수
                commonValidationRules.commentIdRequired(DeleteCommentCommand::commentId),
                // 요청자 ID 검증: 필수
                requesterIdRequired(DeleteCommentCommand::requesterId));
    }

    /**
     * 댓글 내용 필수 검증
     */
    private <T> Validator<T> commentContentRequired(java.util.function.Function<T, String> contentExtractor) {
        return Validator.fieldWithMessage(
                contentExtractor,
                content -> content != null && !content.trim().isEmpty(),
                "content",
                "validation.comment.content.required",
                messageResolver);
    }

    /**
     * 댓글 내용 최대 길이 검증
     */
    private <T> Validator<T> commentContentMaxLength(java.util.function.Function<T, String> contentExtractor) {
        return Validator.fieldWithMessage(
                contentExtractor,
                content -> content == null || content.length() <= COMMENT_CONTENT_MAX_LENGTH,
                "content",
                "validation.comment.content.max.length",
                messageResolver,
                COMMENT_CONTENT_MAX_LENGTH);
    }

    /**
     * 작성자 ID 필수 검증
     */
    private <T> Validator<T> authorIdRequired(java.util.function.Function<T, Object> authorIdExtractor) {
        return Validator.fieldWithMessage(
                authorIdExtractor,
                authorId -> authorId != null,
                "authorId",
                "validation.comment.author.id.required",
                messageResolver);
    }

    /**
     * 요청자 ID 필수 검증
     */
    private <T> Validator<T> requesterIdRequired(java.util.function.Function<T, Object> requesterIdExtractor) {
        return Validator.fieldWithMessage(
                requesterIdExtractor,
                requesterId -> requesterId != null,
                "requesterId",
                "validation.comment.requester.id.required",
                messageResolver);
    }
}