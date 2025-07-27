package com.boardly.features.card.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.activity.domain.model.ActivityType;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.application.port.input.AddCardLabelCommand;
import com.boardly.features.card.application.port.input.RemoveCardLabelCommand;
import com.boardly.features.card.application.validation.CardLabelValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.repository.CardLabelRepository;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.label.domain.model.Label;
import com.boardly.features.label.domain.model.LabelId;
import com.boardly.features.label.domain.repository.LabelRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardLabelService 테스트")
class CardLabelServiceTest {

        @Mock
        private CardLabelValidator validator;

        @Mock
        private ValidationMessageResolver messageResolver;

        @Mock
        private CardRepository cardRepository;

        @Mock
        private LabelRepository labelRepository;

        @Mock
        private CardLabelRepository cardLabelRepository;

        @Mock
        private ActivityHelper activityHelper;

        @Mock
        private BoardRepository boardRepository;

        @Mock
        private BoardListRepository boardListRepository;

        private CardLabelService cardLabelService;

        @BeforeEach
        void setUp() {
                cardLabelService = new CardLabelService(
                                validator,
                                messageResolver,
                                cardRepository,
                                labelRepository,
                                cardLabelRepository,
                                activityHelper,
                                boardRepository,
                                boardListRepository);

                // 공통으로 사용되는 메시지 설정
                lenient().when(messageResolver.getMessage("validation.card.label.add.validation"))
                                .thenReturn("카드 라벨 추가 입력 검증에 실패했습니다");
                lenient().when(messageResolver.getMessage("validation.card.label.remove.validation"))
                                .thenReturn("카드 라벨 제거 입력 검증에 실패했습니다");
                lenient().when(messageResolver.getMessage("validation.card.not.found"))
                                .thenReturn("카드를 찾을 수 없습니다");
                lenient().when(messageResolver.getMessage("validation.label.not.found"))
                                .thenReturn("라벨을 찾을 수 없습니다");
                lenient().when(messageResolver.getMessage("validation.boardlist.not.found"))
                                .thenReturn("보드 리스트를 찾을 수 없습니다");
                lenient().when(messageResolver.getMessage("validation.card.label.different.board"))
                                .thenReturn("카드와 라벨이 다른 보드에 속합니다");
                lenient().when(messageResolver.getMessage("error.auth.access.denied"))
                                .thenReturn("접근이 거부되었습니다.");
        }

        @Nested
        @DisplayName("addLabel 메서드 테스트")
        class AddLabelTest {

                private AddCardLabelCommand validCommand;
                private Card card;
                private Label label;
                private BoardList boardList;
                private Board board;
                private UserId userId;
                private CardId cardId;
                private LabelId labelId;
                private ListId listId;
                private BoardId boardId;

                @BeforeEach
                void setUp() {
                        userId = new UserId("user-1");
                        cardId = new CardId("card-1");
                        labelId = new LabelId("label-1");
                        listId = new ListId("list-1");
                        boardId = new BoardId("board-1");

                        validCommand = new AddCardLabelCommand(cardId, labelId, userId);

                        card = Card.builder()
                                        .cardId(cardId)
                                        .title("테스트 카드")
                                        .listId(listId)
                                        .build();

                        label = Label.builder()
                                        .labelId(labelId)
                                        .name("테스트 라벨")
                                        .boardId(boardId)
                                        .color("#FF0000")
                                        .build();

                        boardList = BoardList.builder()
                                        .listId(listId)
                                        .boardId(boardId)
                                        .build();

                        board = Board.builder()
                                        .boardId(boardId)
                                        .title("테스트 보드")
                                        .ownerId(userId)
                                        .build();
                }

                @Test
                @DisplayName("유효한 커맨드로 라벨 추가 시 성공한다")
                void shouldAddLabelSuccessfully() {
                        // given
                        when(validator.validateAdd(validCommand)).thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
                        when(labelRepository.findById(labelId)).thenReturn(Optional.of(label));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(cardLabelRepository.addLabelToCard(cardId, labelId)).thenReturn(Either.right(null));

                        // when
                        Either<Failure, Void> result = cardLabelService.addLabel(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        verify(cardLabelRepository).addLabelToCard(cardId, labelId);
                        verify(activityHelper).logCardActivity(
                                        eq(ActivityType.CARD_ADD_LABEL),
                                        eq(userId),
                                        any(),
                                        eq(board.getTitle()),
                                        eq(boardId),
                                        eq(listId),
                                        eq(cardId));
                }

                @Test
                @DisplayName("입력 검증 실패 시 실패를 반환한다")
                void shouldReturnFailureWhenValidationFails() {
                        // given
                        List<Failure.FieldViolation> validationErrors = List.of(
                                        Failure.FieldViolation.builder()
                                                        .field("title")
                                                        .message("제목은 필수입니다.")
                                                        .rejectedValue(null)
                                                        .build());
                        when(validator.validateAdd(validCommand))
                                        .thenReturn(ValidationResult
                                                        .invalid(io.vavr.collection.List.ofAll(validationErrors)));

                        // when
                        Either<Failure, Void> result = cardLabelService.addLabel(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("카드 라벨 추가 입력 검증에 실패했습니다");
                        verifyNoInteractions(cardRepository, labelRepository, cardLabelRepository, activityHelper);
                }

                @Test
                @DisplayName("카드가 존재하지 않을 때 실패를 반환한다")
                void shouldReturnFailureWhenCardNotFound() {
                        // given
                        when(validator.validateAdd(validCommand)).thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

                        // when
                        Either<Failure, Void> result = cardLabelService.addLabel(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("카드를 찾을 수 없습니다");
                        verifyNoInteractions(labelRepository, cardLabelRepository, activityHelper);
                }

                @Test
                @DisplayName("라벨이 존재하지 않을 때 실패를 반환한다")
                void shouldReturnFailureWhenLabelNotFound() {
                        // given
                        when(validator.validateAdd(validCommand)).thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
                        when(labelRepository.findById(labelId)).thenReturn(Optional.empty());

                        // when
                        Either<Failure, Void> result = cardLabelService.addLabel(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("라벨을 찾을 수 없습니다");
                        verifyNoInteractions(cardLabelRepository, activityHelper);
                }

                @Test
                @DisplayName("카드의 리스트가 존재하지 않을 때 실패를 반환한다")
                void shouldReturnFailureWhenBoardListNotFound() {
                        // given
                        when(validator.validateAdd(validCommand)).thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
                        when(labelRepository.findById(labelId)).thenReturn(Optional.of(label));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.empty());

                        // when
                        Either<Failure, Void> result = cardLabelService.addLabel(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("보드 리스트를 찾을 수 없습니다");
                        verifyNoInteractions(cardLabelRepository, activityHelper);
                }

                @Test
                @DisplayName("카드와 라벨이 다른 보드에 속할 때 실패를 반환한다")
                void shouldReturnFailureWhenCardAndLabelBelongToDifferentBoards() {
                        // given
                        BoardId differentBoardId = new BoardId("board-2");
                        Label differentBoardLabel = Label.builder()
                                        .labelId(labelId)
                                        .name("테스트 라벨")
                                        .boardId(differentBoardId)
                                        .color("#FF0000")
                                        .build();

                        when(validator.validateAdd(validCommand)).thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
                        when(labelRepository.findById(labelId)).thenReturn(Optional.of(differentBoardLabel));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));

                        // when
                        Either<Failure, Void> result = cardLabelService.addLabel(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("카드와 라벨이 다른 보드에 속합니다");
                        verifyNoInteractions(cardLabelRepository, activityHelper);
                }

                @Test
                @DisplayName("카드 접근 권한이 없을 때 실패를 반환한다")
                void shouldReturnFailureWhenCardAccessDenied() {
                        // given
                        UserId differentUserId = new UserId("user-2");
                        Board differentOwnerBoard = Board.builder()
                                        .boardId(boardId)
                                        .title("테스트 보드")
                                        .ownerId(differentUserId)
                                        .build();

                        when(validator.validateAdd(validCommand)).thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
                        when(labelRepository.findById(labelId)).thenReturn(Optional.of(label));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(differentOwnerBoard));

                        // when
                        Either<Failure, Void> result = cardLabelService.addLabel(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("접근이 거부되었습니다.");
                        verifyNoInteractions(cardLabelRepository, activityHelper);
                }

                @Test
                @DisplayName("라벨 추가 실패 시 실패를 반환한다")
                void shouldReturnFailureWhenAddLabelFails() {
                        // given
                        when(validator.validateAdd(validCommand)).thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
                        when(labelRepository.findById(labelId)).thenReturn(Optional.of(label));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(cardLabelRepository.addLabelToCard(cardId, labelId))
                                        .thenReturn(Either.left(
                                                        Failure.ofInternalError("라벨 추가 실패", "INTERNAL_ERROR", null)));

                        // when
                        Either<Failure, Void> result = cardLabelService.addLabel(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        verifyNoInteractions(activityHelper);
                }
        }

        @Nested
        @DisplayName("removeLabel 메서드 테스트")
        class RemoveLabelTest {

                private RemoveCardLabelCommand validCommand;
                private Card card;
                private Label label;
                private BoardList boardList;
                private Board board;
                private UserId userId;
                private CardId cardId;
                private LabelId labelId;
                private ListId listId;
                private BoardId boardId;

                @BeforeEach
                void setUp() {
                        userId = new UserId("user-1");
                        cardId = new CardId("card-1");
                        labelId = new LabelId("label-1");
                        listId = new ListId("list-1");
                        boardId = new BoardId("board-1");

                        validCommand = new RemoveCardLabelCommand(cardId, labelId, userId);

                        card = Card.builder()
                                        .cardId(cardId)
                                        .title("테스트 카드")
                                        .listId(listId)
                                        .build();

                        label = Label.builder()
                                        .labelId(labelId)
                                        .name("테스트 라벨")
                                        .boardId(boardId)
                                        .color("#FF0000")
                                        .build();

                        boardList = BoardList.builder()
                                        .listId(listId)
                                        .boardId(boardId)
                                        .build();

                        board = Board.builder()
                                        .boardId(boardId)
                                        .title("테스트 보드")
                                        .ownerId(userId)
                                        .build();
                }

                @Test
                @DisplayName("유효한 커맨드로 라벨 제거 시 성공한다")
                void shouldRemoveLabelSuccessfully() {
                        // given
                        when(validator.validateRemove(validCommand)).thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
                        when(labelRepository.findById(labelId)).thenReturn(Optional.of(label));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(cardLabelRepository.removeLabelFromCard(cardId, labelId)).thenReturn(Either.right(null));

                        // when
                        Either<Failure, Void> result = cardLabelService.removeLabel(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        verify(cardLabelRepository).removeLabelFromCard(cardId, labelId);
                        verify(activityHelper).logCardActivity(
                                        eq(ActivityType.CARD_REMOVE_LABEL),
                                        eq(userId),
                                        any(),
                                        eq(board.getTitle()),
                                        eq(boardId),
                                        eq(listId),
                                        eq(cardId));
                }

                @Test
                @DisplayName("입력 검증 실패 시 실패를 반환한다")
                void shouldReturnFailureWhenValidationFails() {
                        // given
                        List<Failure.FieldViolation> validationErrors = List.of(
                                        Failure.FieldViolation.builder()
                                                        .field("cardId")
                                                        .message("카드 ID는 필수입니다.")
                                                        .rejectedValue(null)
                                                        .build());
                        when(validator.validateRemove(validCommand))
                                        .thenReturn(ValidationResult
                                                        .invalid(io.vavr.collection.List.ofAll(validationErrors)));

                        // when
                        Either<Failure, Void> result = cardLabelService.removeLabel(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("카드 라벨 제거 입력 검증에 실패했습니다");
                        verifyNoInteractions(cardRepository, labelRepository, cardLabelRepository, activityHelper);
                }

                @Test
                @DisplayName("카드가 존재하지 않을 때 실패를 반환한다")
                void shouldReturnFailureWhenCardNotFound() {
                        // given
                        when(validator.validateRemove(validCommand)).thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

                        // when
                        Either<Failure, Void> result = cardLabelService.removeLabel(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("카드를 찾을 수 없습니다");
                        verifyNoInteractions(labelRepository, cardLabelRepository, activityHelper);
                }

                @Test
                @DisplayName("라벨이 존재하지 않을 때 실패를 반환한다")
                void shouldReturnFailureWhenLabelNotFound() {
                        // given
                        when(validator.validateRemove(validCommand)).thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
                        when(labelRepository.findById(labelId)).thenReturn(Optional.empty());

                        // when
                        Either<Failure, Void> result = cardLabelService.removeLabel(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("라벨을 찾을 수 없습니다");
                        verifyNoInteractions(cardLabelRepository, activityHelper);
                }

                @Test
                @DisplayName("카드 접근 권한이 없을 때 실패를 반환한다")
                void shouldReturnFailureWhenCardAccessDenied() {
                        // given
                        UserId differentUserId = new UserId("user-2");
                        Board differentOwnerBoard = Board.builder()
                                        .boardId(boardId)
                                        .title("테스트 보드")
                                        .ownerId(differentUserId)
                                        .build();

                        when(validator.validateRemove(validCommand)).thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
                        when(labelRepository.findById(labelId)).thenReturn(Optional.of(label));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(differentOwnerBoard));

                        // when
                        Either<Failure, Void> result = cardLabelService.removeLabel(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("접근이 거부되었습니다.");
                        verifyNoInteractions(cardLabelRepository, activityHelper);
                }

                @Test
                @DisplayName("라벨 제거 실패 시 실패를 반환한다")
                void shouldReturnFailureWhenRemoveLabelFails() {
                        // given
                        when(validator.validateRemove(validCommand)).thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
                        when(labelRepository.findById(labelId)).thenReturn(Optional.of(label));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(cardLabelRepository.removeLabelFromCard(cardId, labelId))
                                        .thenReturn(Either.left(
                                                        Failure.ofInternalError("라벨 제거 실패", "INTERNAL_ERROR", null)));

                        // when
                        Either<Failure, Void> result = cardLabelService.removeLabel(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        verifyNoInteractions(activityHelper);
                }
        }

        @Nested
        @DisplayName("getCardLabels 메서드 테스트")
        class GetCardLabelsTest {

                private CardId cardId;
                private UserId userId;
                private Card card;
                private BoardList boardList;
                private Board board;
                private ListId listId;
                private BoardId boardId;

                @BeforeEach
                void setUp() {
                        cardId = new CardId("card-1");
                        userId = new UserId("user-1");
                        listId = new ListId("list-1");
                        boardId = new BoardId("board-1");

                        card = Card.builder()
                                        .cardId(cardId)
                                        .title("테스트 카드")
                                        .listId(listId)
                                        .build();

                        boardList = BoardList.builder()
                                        .listId(listId)
                                        .boardId(boardId)
                                        .build();

                        board = Board.builder()
                                        .boardId(boardId)
                                        .title("테스트 보드")
                                        .ownerId(userId)
                                        .build();
                }

                @Test
                @DisplayName("유효한 요청으로 카드 라벨 조회 시 성공한다")
                void shouldGetCardLabelsSuccessfully() {
                        // given
                        List<LabelId> expectedLabelIds = List.of(
                                        new LabelId("label-1"),
                                        new LabelId("label-2"));

                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(cardLabelRepository.findLabelIdsByCardId(cardId)).thenReturn(expectedLabelIds);

                        // when
                        List<LabelId> result = cardLabelService.getCardLabels(cardId, userId);

                        // then
                        assertThat(result).isEqualTo(expectedLabelIds);
                        verify(cardLabelRepository).findLabelIdsByCardId(cardId);
                }

                @Test
                @DisplayName("카드가 존재하지 않을 때 빈 리스트를 반환한다")
                void shouldReturnEmptyListWhenCardNotFound() {
                        // given
                        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

                        // when
                        List<LabelId> result = cardLabelService.getCardLabels(cardId, userId);

                        // then
                        assertThat(result).isEmpty();
                        verifyNoInteractions(cardLabelRepository);
                }

                @Test
                @DisplayName("카드의 리스트가 존재하지 않을 때 빈 리스트를 반환한다")
                void shouldReturnEmptyListWhenBoardListNotFound() {
                        // given
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.empty());

                        // when
                        List<LabelId> result = cardLabelService.getCardLabels(cardId, userId);

                        // then
                        assertThat(result).isEmpty();
                        verifyNoInteractions(cardLabelRepository);
                }

                @Test
                @DisplayName("보드가 존재하지 않을 때 빈 리스트를 반환한다")
                void shouldReturnEmptyListWhenBoardNotFound() {
                        // given
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

                        // when
                        List<LabelId> result = cardLabelService.getCardLabels(cardId, userId);

                        // then
                        assertThat(result).isEmpty();
                        verifyNoInteractions(cardLabelRepository);
                }

                @Test
                @DisplayName("카드 접근 권한이 없을 때 빈 리스트를 반환한다")
                void shouldReturnEmptyListWhenCardAccessDenied() {
                        // given
                        UserId differentUserId = new UserId("user-2");
                        Board differentOwnerBoard = Board.builder()
                                        .boardId(boardId)
                                        .title("테스트 보드")
                                        .ownerId(differentUserId)
                                        .build();

                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(differentOwnerBoard));

                        // when
                        List<LabelId> result = cardLabelService.getCardLabels(cardId, userId);

                        // then
                        assertThat(result).isEmpty();
                        verifyNoInteractions(cardLabelRepository);
                }

                @Test
                @DisplayName("라벨이 없는 카드의 경우 빈 리스트를 반환한다")
                void shouldReturnEmptyListWhenNoLabels() {
                        // given
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(cardLabelRepository.findLabelIdsByCardId(cardId)).thenReturn(List.of());

                        // when
                        List<LabelId> result = cardLabelService.getCardLabels(cardId, userId);

                        // then
                        assertThat(result).isEmpty();
                        verify(cardLabelRepository).findLabelIdsByCardId(cardId);
                }
        }

        @Nested
        @DisplayName("활동 로그 기록 테스트")
        class ActivityLoggingTest {

                private AddCardLabelCommand addCommand;
                private RemoveCardLabelCommand removeCommand;
                private Card card;
                private Label label;
                private BoardList boardList;
                private Board board;
                private UserId userId;
                private CardId cardId;
                private LabelId labelId;
                private ListId listId;
                private BoardId boardId;

                @BeforeEach
                void setUp() {
                        userId = new UserId("user-1");
                        cardId = new CardId("card-1");
                        labelId = new LabelId("label-1");
                        listId = new ListId("list-1");
                        boardId = new BoardId("board-1");

                        addCommand = new AddCardLabelCommand(cardId, labelId, userId);
                        removeCommand = new RemoveCardLabelCommand(cardId, labelId, userId);

                        card = Card.builder()
                                        .cardId(cardId)
                                        .title("테스트 카드")
                                        .listId(listId)
                                        .build();

                        label = Label.builder()
                                        .labelId(labelId)
                                        .name("테스트 라벨")
                                        .boardId(boardId)
                                        .color("#FF0000")
                                        .build();

                        boardList = BoardList.builder()
                                        .listId(listId)
                                        .boardId(boardId)
                                        .build();

                        board = Board.builder()
                                        .boardId(boardId)
                                        .title("테스트 보드")
                                        .ownerId(userId)
                                        .build();
                }

                @Test
                @DisplayName("라벨 추가 시 활동 로그가 기록된다")
                void shouldLogActivityWhenAddingLabel() {
                        // given
                        when(validator.validateAdd(addCommand)).thenReturn(ValidationResult.valid(addCommand));
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
                        when(labelRepository.findById(labelId)).thenReturn(Optional.of(label));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(cardLabelRepository.addLabelToCard(cardId, labelId)).thenReturn(Either.right(null));

                        // when
                        cardLabelService.addLabel(addCommand);

                        // then
                        verify(activityHelper).logCardActivity(
                                        eq(ActivityType.CARD_ADD_LABEL),
                                        eq(userId),
                                        any(),
                                        eq(board.getTitle()),
                                        eq(boardId),
                                        eq(listId),
                                        eq(cardId));
                }

                @Test
                @DisplayName("라벨 제거 시 활동 로그가 기록된다")
                void shouldLogActivityWhenRemovingLabel() {
                        // given
                        when(validator.validateRemove(removeCommand)).thenReturn(ValidationResult.valid(removeCommand));
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
                        when(labelRepository.findById(labelId)).thenReturn(Optional.of(label));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(cardLabelRepository.removeLabelFromCard(cardId, labelId)).thenReturn(Either.right(null));

                        // when
                        cardLabelService.removeLabel(removeCommand);

                        // then
                        verify(activityHelper).logCardActivity(
                                        eq(ActivityType.CARD_REMOVE_LABEL),
                                        eq(userId),
                                        any(),
                                        eq(board.getTitle()),
                                        eq(boardId),
                                        eq(listId),
                                        eq(cardId));
                }

                @Test
                @DisplayName("활동 로그 기록 실패 시에도 메인 로직은 성공한다")
                void shouldSucceedEvenWhenActivityLoggingFails() {
                        // given
                        when(validator.validateAdd(addCommand)).thenReturn(ValidationResult.valid(addCommand));
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
                        when(labelRepository.findById(labelId)).thenReturn(Optional.of(label));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(cardLabelRepository.addLabelToCard(cardId, labelId)).thenReturn(Either.right(null));
                        doThrow(new RuntimeException("활동 로그 기록 실패"))
                                        .when(activityHelper)
                                        .logCardActivity(any(), any(), any(), any(), any(), any(), any());

                        // when
                        Either<Failure, Void> result = cardLabelService.addLabel(addCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        verify(cardLabelRepository).addLabelToCard(cardId, labelId);
                }
        }
}