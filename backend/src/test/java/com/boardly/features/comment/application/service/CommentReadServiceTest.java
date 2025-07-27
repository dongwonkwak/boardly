package com.boardly.features.comment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.comment.domain.model.Comment;
import com.boardly.features.comment.domain.model.CommentId;
import com.boardly.features.comment.domain.repository.CommentRepository;
import com.boardly.features.user.domain.model.UserId;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentReadService 테스트")
class CommentReadServiceTest {

    @Mock
    private CommentRepository commentRepository;

    private CommentReadService commentReadService;

    @BeforeEach
    void setUp() {
        commentReadService = new CommentReadService(commentRepository);
    }

    @Nested
    @DisplayName("getComment 메서드 테스트")
    class GetCommentTest {

        private CommentId commentId;
        private UserId requesterId;
        private Comment comment;

        @BeforeEach
        void setUp() {
            commentId = new CommentId();
            requesterId = new UserId();
            comment = Comment.restore(
                    commentId,
                    new CardId(),
                    new UserId(),
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
            Optional<Comment> result = commentReadService.getComment(commentId, requesterId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(comment);
            verify(commentRepository, times(1)).findById(commentId);
        }

        @Test
        @DisplayName("존재하지 않는 댓글 ID로 조회 시 빈 Optional을 반환한다")
        void shouldReturnEmptyOptionalWhenCommentDoesNotExist() {
            // given
            when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

            // when
            Optional<Comment> result = commentReadService.getComment(commentId, requesterId);

            // then
            assertThat(result).isEmpty();
            verify(commentRepository, times(1)).findById(commentId);
        }

        @Test
        @DisplayName("댓글 조회 중 예외 발생 시 빈 Optional을 반환한다")
        void shouldReturnEmptyOptionalWhenExceptionOccurs() {
            // given
            when(commentRepository.findById(commentId)).thenThrow(new RuntimeException("데이터베이스 오류"));

            // when
            Optional<Comment> result = commentReadService.getComment(commentId, requesterId);

            // then
            assertThat(result).isEmpty();
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
            cardId = new CardId();
            requesterId = new UserId();

            Comment comment1 = Comment.restore(
                    new CommentId(),
                    cardId,
                    new UserId(),
                    "첫 번째 댓글",
                    false,
                    Instant.now().minusSeconds(60),
                    Instant.now().minusSeconds(60));

            Comment comment2 = Comment.restore(
                    new CommentId(),
                    cardId,
                    new UserId(),
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
            List<Comment> result = commentReadService.getCardComments(cardId, requesterId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).isEqualTo(comments);
            verify(commentRepository, times(1)).findByCardIdOrderByCreatedAt(cardId);
        }

        @Test
        @DisplayName("카드에 댓글이 없을 때 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenCardHasNoComments() {
            // given
            when(commentRepository.findByCardIdOrderByCreatedAt(cardId)).thenReturn(List.of());

            // when
            List<Comment> result = commentReadService.getCardComments(cardId, requesterId);

            // then
            assertThat(result).isEmpty();
            verify(commentRepository, times(1)).findByCardIdOrderByCreatedAt(cardId);
        }

        @Test
        @DisplayName("카드 댓글 조회 중 예외 발생 시 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenExceptionOccurs() {
            // given
            when(commentRepository.findByCardIdOrderByCreatedAt(cardId))
                    .thenThrow(new RuntimeException("데이터베이스 오류"));

            // when
            List<Comment> result = commentReadService.getCardComments(cardId, requesterId);

            // then
            assertThat(result).isEmpty();
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
            userId = new UserId();
            requesterId = new UserId();

            Comment comment1 = Comment.restore(
                    new CommentId(),
                    new CardId(),
                    userId,
                    "사용자의 첫 번째 댓글",
                    false,
                    Instant.now().minusSeconds(60),
                    Instant.now().minusSeconds(60));

            Comment comment2 = Comment.restore(
                    new CommentId(),
                    new CardId(),
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
            List<Comment> result = commentReadService.getUserComments(userId, requesterId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).isEqualTo(comments);
            verify(commentRepository, times(1)).findByAuthorIdOrderByCreatedAtDesc(userId);
        }

        @Test
        @DisplayName("사용자가 댓글을 작성하지 않았을 때 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenUserHasNoComments() {
            // given
            when(commentRepository.findByAuthorIdOrderByCreatedAtDesc(userId)).thenReturn(List.of());

            // when
            List<Comment> result = commentReadService.getUserComments(userId, requesterId);

            // then
            assertThat(result).isEmpty();
            verify(commentRepository, times(1)).findByAuthorIdOrderByCreatedAtDesc(userId);
        }

        @Test
        @DisplayName("사용자 댓글 조회 중 예외 발생 시 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenExceptionOccurs() {
            // given
            when(commentRepository.findByAuthorIdOrderByCreatedAtDesc(userId))
                    .thenThrow(new RuntimeException("데이터베이스 오류"));

            // when
            List<Comment> result = commentReadService.getUserComments(userId, requesterId);

            // then
            assertThat(result).isEmpty();
            verify(commentRepository, times(1)).findByAuthorIdOrderByCreatedAtDesc(userId);
        }
    }

    @Nested
    @DisplayName("통합 테스트")
    class IntegrationTest {

        @Test
        @DisplayName("모든 메서드가 올바른 파라미터로 리포지토리를 호출한다")
        void shouldCallRepositoryWithCorrectParameters() {
            // given
            CommentId commentId = new CommentId();
            CardId cardId = new CardId();
            UserId userId = new UserId();
            UserId requesterId = new UserId();

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
    }
}