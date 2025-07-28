package com.boardly.features.comment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.comment.domain.model.Comment;
import com.boardly.features.comment.domain.model.CommentId;
import com.boardly.features.comment.domain.repository.CommentRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentReadService 테스트")
class CommentReadServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private MessageSource messageSource;

    private CommentReadService commentReadService;
    private ValidationMessageResolver validationMessageResolver;

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(Locale.KOREAN);

        // 기본 메시지 설정 - lenient로 설정하여 불필요한 stubbing 허용
        lenient().when(messageSource.getMessage(anyString(), any(Object[].class), any(Locale.class)))
                .thenAnswer(invocation -> {
                    String code = invocation.getArgument(0);
                    Object[] args = invocation.getArgument(1);
                    StringBuilder message = new StringBuilder(code);
                    if (args != null) {
                        for (Object arg : args) {
                            message.append(" ").append(arg);
                        }
                    }
                    return message.toString();
                });

        validationMessageResolver = new ValidationMessageResolver(messageSource);
        commentReadService = new CommentReadService(commentRepository, validationMessageResolver);
    }

    @Nested
    @DisplayName("getComment 메서드 테스트")
    class GetCommentTest {

        private CommentId commentId;
        private UserId requesterId;
        private Comment comment;

        @BeforeEach
        void setUp() {
            commentId = new CommentId("comment-1");
            requesterId = new UserId("user-1");
            comment = Comment.restore(
                    commentId,
                    new CardId("card-1"),
                    new UserId("author-1"),
                    "테스트 댓글 내용",
                    false,
                    Instant.now(),
                    Instant.now());
        }

        @Test
        @DisplayName("존재하는 댓글 ID로 조회 시 댓글을 반환한다")
        void shouldReturnCommentWhenCommentExists() {
            // given
            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            // when
            Either<Failure, Comment> result = commentReadService.getComment(commentId, requesterId);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(comment);
            verify(commentRepository, times(1)).findById(commentId);
        }

        @Test
        @DisplayName("존재하지 않는 댓글 ID로 조회 시 NotFound Failure를 반환한다")
        void shouldReturnNotFoundFailureWhenCommentDoesNotExist() {
            // given
            when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

            // when
            Either<Failure, Comment> result = commentReadService.getComment(commentId, requesterId);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);

            Failure.NotFound failure = (Failure.NotFound) result.getLeft();
            assertThat(failure.getErrorCode()).isEqualTo("COMMENT_NOT_FOUND");
            assertThat(failure.getContext()).isInstanceOf(Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> context = (Map<String, Object>) failure.getContext();
            assertThat(context).containsEntry("commentId", "comment-1");

            verify(commentRepository, times(1)).findById(commentId);
        }

        @Test
        @DisplayName("댓글 조회 중 예외 발생 시 InternalError Failure를 반환한다")
        void shouldReturnInternalErrorFailureWhenExceptionOccurs() {
            // given
            RuntimeException exception = new RuntimeException("데이터베이스 오류");
            when(commentRepository.findById(commentId)).thenThrow(exception);

            // when
            Either<Failure, Comment> result = commentReadService.getComment(commentId, requesterId);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);

            Failure.InternalError failure = (Failure.InternalError) result.getLeft();
            assertThat(failure.getErrorCode()).isEqualTo("COMMENT_QUERY_ERROR");
            assertThat(failure.getContext()).isInstanceOf(Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> context = (Map<String, Object>) failure.getContext();
            assertThat(context).containsEntry("commentId", "comment-1");
            assertThat(context).containsEntry("error", "데이터베이스 오류");

            verify(commentRepository, times(1)).findById(commentId);
        }
    }

    @Nested
    @DisplayName("getCardComments 메서드 테스트")
    class GetCardCommentsTest {

        private CardId cardId;
        private UserId requesterId;
        private List<Comment> comments;

        @BeforeEach
        void setUp() {
            cardId = new CardId("card-1");
            requesterId = new UserId("user-1");

            Comment comment1 = Comment.restore(
                    new CommentId("comment-1"),
                    cardId,
                    new UserId("author-1"),
                    "첫 번째 댓글",
                    false,
                    Instant.now().minusSeconds(60),
                    Instant.now().minusSeconds(60));

            Comment comment2 = Comment.restore(
                    new CommentId("comment-2"),
                    cardId,
                    new UserId("author-2"),
                    "두 번째 댓글",
                    false,
                    Instant.now(),
                    Instant.now());

            comments = List.of(comment1, comment2);
        }

        @Test
        @DisplayName("카드의 댓글 목록을 생성일시 순으로 반환한다")
        void shouldReturnCardCommentsOrderedByCreatedAt() {
            // given
            when(commentRepository.findByCardIdOrderByCreatedAt(cardId)).thenReturn(comments);

            // when
            Either<Failure, List<Comment>> result = commentReadService.getCardComments(cardId, requesterId);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).hasSize(2);
            assertThat(result.get()).isEqualTo(comments);
            verify(commentRepository, times(1)).findByCardIdOrderByCreatedAt(cardId);
        }

        @Test
        @DisplayName("카드에 댓글이 없을 때 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenCardHasNoComments() {
            // given
            when(commentRepository.findByCardIdOrderByCreatedAt(cardId)).thenReturn(List.of());

            // when
            Either<Failure, List<Comment>> result = commentReadService.getCardComments(cardId, requesterId);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEmpty();
            verify(commentRepository, times(1)).findByCardIdOrderByCreatedAt(cardId);
        }

        @Test
        @DisplayName("카드 댓글 조회 중 예외 발생 시 InternalError Failure를 반환한다")
        void shouldReturnInternalErrorFailureWhenExceptionOccurs() {
            // given
            RuntimeException exception = new RuntimeException("데이터베이스 오류");
            when(commentRepository.findByCardIdOrderByCreatedAt(cardId)).thenThrow(exception);

            // when
            Either<Failure, List<Comment>> result = commentReadService.getCardComments(cardId, requesterId);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);

            Failure.InternalError failure = (Failure.InternalError) result.getLeft();
            assertThat(failure.getErrorCode()).isEqualTo("CARD_COMMENTS_QUERY_ERROR");
            assertThat(failure.getContext()).isInstanceOf(Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> context = (Map<String, Object>) failure.getContext();
            assertThat(context).containsEntry("cardId", "card-1");
            assertThat(context).containsEntry("error", "데이터베이스 오류");

            verify(commentRepository, times(1)).findByCardIdOrderByCreatedAt(cardId);
        }
    }

    @Nested
    @DisplayName("getUserComments 메서드 테스트")
    class GetUserCommentsTest {

        private UserId userId;
        private UserId requesterId;
        private List<Comment> comments;

        @BeforeEach
        void setUp() {
            userId = new UserId("user-1");
            requesterId = new UserId("requester-1");

            Comment comment1 = Comment.restore(
                    new CommentId("comment-1"),
                    new CardId("card-1"),
                    userId,
                    "사용자의 첫 번째 댓글",
                    false,
                    Instant.now().minusSeconds(60),
                    Instant.now().minusSeconds(60));

            Comment comment2 = Comment.restore(
                    new CommentId("comment-2"),
                    new CardId("card-2"),
                    userId,
                    "사용자의 두 번째 댓글",
                    false,
                    Instant.now(),
                    Instant.now());

            comments = List.of(comment1, comment2);
        }

        @Test
        @DisplayName("사용자의 댓글 목록을 최신순으로 반환한다")
        void shouldReturnUserCommentsOrderedByCreatedAtDesc() {
            // given
            when(commentRepository.findByAuthorIdOrderByCreatedAtDesc(userId)).thenReturn(comments);

            // when
            Either<Failure, List<Comment>> result = commentReadService.getUserComments(userId, requesterId);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).hasSize(2);
            assertThat(result.get()).isEqualTo(comments);
            verify(commentRepository, times(1)).findByAuthorIdOrderByCreatedAtDesc(userId);
        }

        @Test
        @DisplayName("사용자가 댓글을 작성하지 않았을 때 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenUserHasNoComments() {
            // given
            when(commentRepository.findByAuthorIdOrderByCreatedAtDesc(userId)).thenReturn(List.of());

            // when
            Either<Failure, List<Comment>> result = commentReadService.getUserComments(userId, requesterId);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEmpty();
            verify(commentRepository, times(1)).findByAuthorIdOrderByCreatedAtDesc(userId);
        }

        @Test
        @DisplayName("사용자 댓글 조회 중 예외 발생 시 InternalError Failure를 반환한다")
        void shouldReturnInternalErrorFailureWhenExceptionOccurs() {
            // given
            RuntimeException exception = new RuntimeException("데이터베이스 오류");
            when(commentRepository.findByAuthorIdOrderByCreatedAtDesc(userId)).thenThrow(exception);

            // when
            Either<Failure, List<Comment>> result = commentReadService.getUserComments(userId, requesterId);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);

            Failure.InternalError failure = (Failure.InternalError) result.getLeft();
            assertThat(failure.getErrorCode()).isEqualTo("USER_COMMENTS_QUERY_ERROR");
            assertThat(failure.getContext()).isInstanceOf(Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> context = (Map<String, Object>) failure.getContext();
            assertThat(context).containsEntry("userId", "user-1");
            assertThat(context).containsEntry("error", "데이터베이스 오류");

            verify(commentRepository, times(1)).findByAuthorIdOrderByCreatedAtDesc(userId);
        }
    }

    @Nested
    @DisplayName("다국어 메시지 테스트")
    class InternationalizationTest {

        @Test
        @DisplayName("댓글 조회 실패 시 올바른 메시지 키를 사용한다")
        void shouldUseCorrectMessageKeyForCommentNotFound() {
            // given
            CommentId commentId = new CommentId("comment-1");
            UserId requesterId = new UserId("user-1");
            when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

            // when
            Either<Failure, Comment> result = commentReadService.getComment(commentId, requesterId);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure.NotFound failure = (Failure.NotFound) result.getLeft();
            assertThat(failure.getMessage()).contains("error.service.comment.get.not.found");
        }

        @Test
        @DisplayName("카드 댓글 조회 실패 시 올바른 메시지 키를 사용한다")
        void shouldUseCorrectMessageKeyForCardCommentsError() {
            // given
            CardId cardId = new CardId("card-1");
            UserId requesterId = new UserId("user-1");
            when(commentRepository.findByCardIdOrderByCreatedAt(cardId))
                    .thenThrow(new RuntimeException("데이터베이스 오류"));

            // when
            Either<Failure, List<Comment>> result = commentReadService.getCardComments(cardId, requesterId);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure.InternalError failure = (Failure.InternalError) result.getLeft();
            assertThat(failure.getMessage()).contains("error.service.comment.get.card.internal");
        }

        @Test
        @DisplayName("사용자 댓글 조회 실패 시 올바른 메시지 키를 사용한다")
        void shouldUseCorrectMessageKeyForUserCommentsError() {
            // given
            UserId userId = new UserId("user-1");
            UserId requesterId = new UserId("requester-1");
            when(commentRepository.findByAuthorIdOrderByCreatedAtDesc(userId))
                    .thenThrow(new RuntimeException("데이터베이스 오류"));

            // when
            Either<Failure, List<Comment>> result = commentReadService.getUserComments(userId, requesterId);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure.InternalError failure = (Failure.InternalError) result.getLeft();
            assertThat(failure.getMessage()).contains("error.service.comment.get.user.internal");
        }
    }

    @Nested
    @DisplayName("통합 테스트")
    class IntegrationTest {

        @Test
        @DisplayName("모든 메서드가 올바른 파라미터로 리포지토리를 호출한다")
        void shouldCallRepositoryWithCorrectParameters() {
            // given
            CommentId commentId = new CommentId("comment-1");
            CardId cardId = new CardId("card-1");
            UserId userId = new UserId("user-1");
            UserId requesterId = new UserId("requester-1");

            Comment comment = Comment.restore(
                    commentId,
                    cardId,
                    userId,
                    "테스트 댓글",
                    false,
                    Instant.now(),
                    Instant.now());

            List<Comment> comments = List.of(comment);

            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
            when(commentRepository.findByCardIdOrderByCreatedAt(cardId)).thenReturn(comments);
            when(commentRepository.findByAuthorIdOrderByCreatedAtDesc(userId)).thenReturn(comments);

            // when & then
            commentReadService.getComment(commentId, requesterId);
            commentReadService.getCardComments(cardId, requesterId);
            commentReadService.getUserComments(userId, requesterId);

            verify(commentRepository).findById(commentId);
            verify(commentRepository).findByCardIdOrderByCreatedAt(cardId);
            verify(commentRepository).findByAuthorIdOrderByCreatedAtDesc(userId);
        }

        @Test
        @DisplayName("모든 메서드가 Either 타입을 올바르게 반환한다")
        void shouldReturnEitherTypeForAllMethods() {
            // given
            CommentId commentId = new CommentId("comment-1");
            CardId cardId = new CardId("card-1");
            UserId userId = new UserId("user-1");
            UserId requesterId = new UserId("requester-1");

            Comment comment = Comment.restore(
                    commentId,
                    cardId,
                    userId,
                    "테스트 댓글",
                    false,
                    Instant.now(),
                    Instant.now());

            List<Comment> comments = List.of(comment);

            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
            when(commentRepository.findByCardIdOrderByCreatedAt(cardId)).thenReturn(comments);
            when(commentRepository.findByAuthorIdOrderByCreatedAtDesc(userId)).thenReturn(comments);

            // when
            Either<Failure, Comment> commentResult = commentReadService.getComment(commentId, requesterId);
            Either<Failure, List<Comment>> cardCommentsResult = commentReadService.getCardComments(cardId, requesterId);
            Either<Failure, List<Comment>> userCommentsResult = commentReadService.getUserComments(userId, requesterId);

            // then
            assertThat(commentResult).isInstanceOf(Either.class);
            assertThat(cardCommentsResult).isInstanceOf(Either.class);
            assertThat(userCommentsResult).isInstanceOf(Either.class);

            assertThat(commentResult.isRight()).isTrue();
            assertThat(cardCommentsResult.isRight()).isTrue();
            assertThat(userCommentsResult.isRight()).isTrue();
        }
    }
}