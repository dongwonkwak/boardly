package com.boardly.features.card.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
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
import com.boardly.features.card.application.port.input.CreateCardCommand;
import com.boardly.features.card.application.port.input.CloneCardCommand;
import com.boardly.features.card.application.validation.CardValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.policy.CardCreationPolicy;
import com.boardly.features.card.domain.policy.CardClonePolicy;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateCardService 테스트")
class CreateCardServiceTest {

        @Mock
        private CardValidator cardValidator;

        @Mock
        private CardCreationPolicy cardCreationPolicy;

        @Mock
        private CardClonePolicy cardClonePolicy;

        @Mock
        private CardRepository cardRepository;

        @Mock
        private BoardListRepository boardListRepository;

        @Mock
        private BoardRepository boardRepository;

        @Mock
        private ValidationMessageResolver validationMessageResolver;

        @Mock
        private ActivityHelper activityHelper;

        private CreateCardService createCardService;

        @BeforeEach
        void setUp() {
                createCardService = new CreateCardService(
                                cardValidator,
                                cardCreationPolicy,
                                cardClonePolicy,
                                cardRepository,
                                boardListRepository,
                                boardRepository,
                                validationMessageResolver,
                                activityHelper);

                // 공통으로 사용되는 메시지 설정
                lenient().when(validationMessageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("입력값이 유효하지 않습니다.");
                lenient().when(validationMessageResolver.getMessage("error.service.card.create.list_not_found"))
                                .thenReturn("리스트를 찾을 수 없습니다.");
                lenient().when(validationMessageResolver.getMessage("error.service.card.create.access_denied"))
                                .thenReturn("보드 접근 권한이 없습니다.");
                lenient().when(validationMessageResolver.getMessage("error.service.card.create.archived_board"))
                                .thenReturn("아카이브된 보드에는 카드를 생성할 수 없습니다.");
                lenient().when(validationMessageResolver.getMessage("error.service.card.clone.not_found"))
                                .thenReturn("복제할 카드를 찾을 수 없습니다.");
                lenient().when(validationMessageResolver.getMessage("error.service.card.clone.list_not_found"))
                                .thenReturn("리스트를 찾을 수 없습니다.");
                lenient().when(validationMessageResolver.getMessage("error.service.card.clone.access_denied"))
                                .thenReturn("보드 접근 권한이 없습니다.");
                lenient().when(validationMessageResolver.getMessage("error.service.card.clone.archived_board"))
                                .thenReturn("아카이브된 보드에서는 카드를 복제할 수 없습니다.");
        }

        @Nested
        @DisplayName("createCard 메서드 테스트")
        class CreateCardTest {

                private CreateCardCommand validCommand;
                private BoardList boardList;
                private Board board;
                private Card createdCard;
                private UserId userId;
                private ListId listId;
                private BoardId boardId;

                @BeforeEach
                void setUp() {
                        userId = new UserId("user-123");
                        listId = new ListId("list-123");
                        boardId = new BoardId("board-123");

                        validCommand = CreateCardCommand.of(
                                        "테스트 카드",
                                        "테스트 설명",
                                        listId,
                                        userId);

                        boardList = BoardList.create(
                                        "테스트 리스트",
                                        0,
                                        boardId);

                        board = Board.create(
                                        "테스트 보드",
                                        "테스트 보드 설명",
                                        userId);

                        createdCard = Card.create(
                                        "테스트 카드",
                                        "테스트 설명",
                                        0,
                                        listId);
                }

                @Test
                @DisplayName("유효한 커맨드로 카드 생성 시 성공한다")
                void shouldCreateCardSuccessfully() {
                        // given
                        when(cardValidator.validateCreate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(boardListRepository.findById(listId))
                                        .thenReturn(Optional.of(boardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.of(board));
                        when(cardCreationPolicy.canCreateCard(listId))
                                        .thenReturn(Either.right(null));
                        when(cardRepository.findMaxPositionByListId(listId))
                                        .thenReturn(Optional.of(5));
                        when(cardRepository.save(any(Card.class)))
                                        .thenReturn(Either.right(createdCard));

                        // when
                        Either<Failure, Card> result = createCardService.createCard(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get()).isEqualTo(createdCard);

                        // 검증
                        verify(cardValidator).validateCreate(validCommand);
                        verify(boardListRepository).findById(listId);
                        verify(boardRepository).findByIdAndOwnerId(boardId, userId);
                        verify(cardCreationPolicy).canCreateCard(listId);
                        verify(cardRepository).findMaxPositionByListId(listId);
                        verify(cardRepository).save(any(Card.class));
                        verify(activityHelper).logCardCreate(
                                        eq(userId),
                                        eq("테스트 리스트"),
                                        eq("테스트 카드"),
                                        eq(boardId),
                                        eq(listId),
                                        eq(createdCard.getCardId()));
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
                                                        .build(),
                                        Failure.FieldViolation.builder()
                                                        .field("description")
                                                        .message("설명이 너무 깁니다.")
                                                        .rejectedValue("너무 긴 설명...")
                                                        .build());
                        when(cardValidator.validateCreate(validCommand))
                                        .thenReturn(ValidationResult
                                                        .invalid(io.vavr.collection.List.ofAll(validationErrors)));

                        // when
                        Either<Failure, Card> result = createCardService.createCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                        Failure.InputError inputError = (Failure.InputError) result.getLeft();
                        assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
                        assertThat(inputError.getViolations()).containsExactlyElementsOf(validationErrors);
                        assertThat(inputError.getMessage()).isEqualTo("입력값이 유효하지 않습니다.");

                        // 다른 메서드들이 호출되지 않았는지 확인
                        verifyNoInteractions(boardListRepository, boardRepository, cardCreationPolicy,
                                        cardRepository, activityHelper);
                }

                @Test
                @DisplayName("리스트가 존재하지 않을 때 실패를 반환한다")
                void shouldReturnFailureWhenListNotFound() {
                        // given
                        when(cardValidator.validateCreate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(boardListRepository.findById(listId))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, Card> result = createCardService.createCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
                        Failure.NotFound notFound = (Failure.NotFound) result.getLeft();
                        assertThat(notFound.getErrorCode()).isEqualTo("NOT_FOUND");
                        assertThat(notFound.getMessage()).isEqualTo("리스트를 찾을 수 없습니다.");

                        // 다른 메서드들이 호출되지 않았는지 확인
                        verifyNoInteractions(boardRepository, cardCreationPolicy, cardRepository, activityHelper);
                }

                @Test
                @DisplayName("보드 접근 권한이 없을 때 실패를 반환한다")
                void shouldReturnFailureWhenBoardAccessDenied() {
                        // given
                        when(cardValidator.validateCreate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(boardListRepository.findById(listId))
                                        .thenReturn(Optional.of(boardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, Card> result = createCardService.createCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
                        Failure.PermissionDenied permissionDenied = (Failure.PermissionDenied) result.getLeft();
                        assertThat(permissionDenied.getErrorCode()).isEqualTo("PERMISSION_DENIED");
                        assertThat(permissionDenied.getMessage()).isEqualTo("보드 접근 권한이 없습니다.");

                        // 다른 메서드들이 호출되지 않았는지 확인
                        verifyNoInteractions(cardCreationPolicy, cardRepository, activityHelper);
                }

                @Test
                @DisplayName("아카이브된 보드에 카드 생성 시도 시 실패를 반환한다")
                void shouldReturnFailureWhenBoardIsArchived() {
                        // given
                        Board archivedBoard = Board.builder()
                                        .boardId(boardId)
                                        .title("아카이브된 보드")
                                        .description("아카이브된 보드")
                                        .isArchived(true)
                                        .ownerId(userId)
                                        .isStarred(false)
                                        .build();

                        when(cardValidator.validateCreate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(boardListRepository.findById(listId))
                                        .thenReturn(Optional.of(boardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.of(archivedBoard));

                        // when
                        Either<Failure, Card> result = createCardService.createCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.BusinessRuleViolation.class);
                        Failure.BusinessRuleViolation businessRuleViolation = (Failure.BusinessRuleViolation) result
                                        .getLeft();
                        assertThat(businessRuleViolation.getErrorCode()).isEqualTo("BUSINESS_RULE_VIOLATION");
                        assertThat(businessRuleViolation.getMessage()).isEqualTo("아카이브된 보드에는 카드를 생성할 수 없습니다.");

                        // 다른 메서드들이 호출되지 않았는지 확인
                        verifyNoInteractions(cardCreationPolicy, cardRepository, activityHelper);
                }

                @Test
                @DisplayName("카드 생성 정책 검증 실패 시 실패를 반환한다")
                void shouldReturnFailureWhenCreationPolicyFails() {
                        // given
                        Failure policyFailure = Failure.ofForbidden("카드 개수 제한을 초과했습니다.");

                        when(cardValidator.validateCreate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(boardListRepository.findById(listId))
                                        .thenReturn(Optional.of(boardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.of(board));
                        when(cardCreationPolicy.canCreateCard(listId))
                                        .thenReturn(Either.left(policyFailure));

                        // when
                        Either<Failure, Card> result = createCardService.createCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isEqualTo(policyFailure);

                        // 다른 메서드들이 호출되지 않았는지 확인
                        verifyNoInteractions(cardRepository, activityHelper);
                }

                @Test
                @DisplayName("카드 저장 실패 시 실패를 반환한다")
                void shouldReturnFailureWhenCardSaveFails() {
                        // given
                        Failure saveFailure = Failure.ofInternalServerError("저장 중 오류가 발생했습니다.");

                        when(cardValidator.validateCreate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(boardListRepository.findById(listId))
                                        .thenReturn(Optional.of(boardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.of(board));
                        when(cardCreationPolicy.canCreateCard(listId))
                                        .thenReturn(Either.right(null));
                        when(cardRepository.findMaxPositionByListId(listId))
                                        .thenReturn(Optional.of(5));
                        when(cardRepository.save(any(Card.class)))
                                        .thenReturn(Either.left(saveFailure));

                        // when
                        Either<Failure, Card> result = createCardService.createCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isEqualTo(saveFailure);

                        // activityHelper가 호출되지 않았는지 확인
                        verifyNoInteractions(activityHelper);
                }

                @Test
                @DisplayName("첫 번째 카드 생성 시 위치가 0으로 설정된다")
                void shouldSetPositionToZeroForFirstCard() {
                        // given
                        when(cardValidator.validateCreate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(boardListRepository.findById(listId))
                                        .thenReturn(Optional.of(boardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.of(board));
                        when(cardCreationPolicy.canCreateCard(listId))
                                        .thenReturn(Either.right(null));
                        when(cardRepository.findMaxPositionByListId(listId))
                                        .thenReturn(Optional.empty());
                        when(cardRepository.save(any(Card.class)))
                                        .thenReturn(Either.right(createdCard));

                        // when
                        Either<Failure, Card> result = createCardService.createCard(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        verify(cardRepository).findMaxPositionByListId(listId);
                        verify(cardRepository).save(any(Card.class));
                }

                @Test
                @DisplayName("기존 카드가 있을 때 다음 위치에 카드가 생성된다")
                void shouldSetPositionToNextPositionWhenExistingCards() {
                        // given
                        int existingMaxPosition = 10;
                        when(cardValidator.validateCreate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(boardListRepository.findById(listId))
                                        .thenReturn(Optional.of(boardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.of(board));
                        when(cardCreationPolicy.canCreateCard(listId))
                                        .thenReturn(Either.right(null));
                        when(cardRepository.findMaxPositionByListId(listId))
                                        .thenReturn(Optional.of(existingMaxPosition));
                        when(cardRepository.save(any(Card.class)))
                                        .thenReturn(Either.right(createdCard));

                        // when
                        Either<Failure, Card> result = createCardService.createCard(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        verify(cardRepository).findMaxPositionByListId(listId);
                        verify(cardRepository).save(any(Card.class));
                }
        }

        @Nested
        @DisplayName("cloneCard 메서드 테스트")
        class CloneCardTest {

                private CloneCardCommand validCommand;
                private Card originalCard;
                private BoardList sourceBoardList;
                private Board sourceBoard;
                private Card clonedCard;
                private UserId userId;
                private CardId cardId;
                private ListId sourceListId;
                private ListId targetListId;
                private BoardId boardId;

                @BeforeEach
                void setUp() {
                        userId = new UserId("user-123");
                        cardId = new CardId("card-123");
                        sourceListId = new ListId("list-123");
                        targetListId = new ListId("list-456");
                        boardId = new BoardId("board-123");

                        validCommand = CloneCardCommand.of(
                                        cardId,
                                        "복제된 카드",
                                        targetListId,
                                        userId);

                        originalCard = Card.create(
                                        "원본 카드",
                                        "원본 설명",
                                        0,
                                        sourceListId);

                        sourceBoardList = BoardList.create(
                                        "소스 리스트",
                                        0,
                                        boardId);

                        sourceBoard = Board.create(
                                        "소스 보드",
                                        "소스 보드 설명",
                                        userId);

                        clonedCard = Card.create(
                                        "복제된 카드",
                                        "원본 설명",
                                        0,
                                        targetListId);
                }

                @Test
                @DisplayName("유효한 커맨드로 카드 복제 시 성공한다")
                void shouldCloneCardSuccessfully() {
                        // given
                        when(cardValidator.validateClone(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId))
                                        .thenReturn(Optional.of(originalCard));
                        when(boardListRepository.findById(sourceListId))
                                        .thenReturn(Optional.of(sourceBoardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.of(sourceBoard));
                        when(boardListRepository.findById(targetListId))
                                        .thenReturn(Optional.of(sourceBoardList));
                        when(cardClonePolicy.canCloneToAnotherList(originalCard, targetListId))
                                        .thenReturn(Either.right(null));
                        when(cardRepository.findMaxPositionByListId(targetListId))
                                        .thenReturn(Optional.of(5));
                        when(cardRepository.save(any(Card.class)))
                                        .thenReturn(Either.right(clonedCard));
                        // logCardDuplicateActivity에서 사용하는 boardListRepository 호출에 대한 mock 설정
                        when(boardListRepository.findById(clonedCard.getListId()))
                                        .thenReturn(Optional.of(sourceBoardList));

                        // when
                        Either<Failure, Card> result = createCardService.cloneCard(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get()).isEqualTo(clonedCard);

                        // 검증
                        verify(cardValidator).validateClone(validCommand);
                        verify(cardRepository).findById(cardId);
                        verify(boardListRepository, times(3)).findById(any()); // sourceListId, targetListId,
                                                                               // clonedCard.getListId()
                        verify(boardRepository, times(2)).findByIdAndOwnerId(boardId, userId); // source, target
                        verify(cardClonePolicy).canCloneToAnotherList(originalCard, targetListId);
                        verify(cardRepository).findMaxPositionByListId(targetListId);
                        verify(cardRepository).save(any(Card.class));
                        verify(activityHelper).logCardActivity(
                                        eq(ActivityType.CARD_DUPLICATE),
                                        eq(userId),
                                        any(Map.class),
                                        eq(boardId),
                                        eq(targetListId),
                                        eq(clonedCard.getCardId()));
                }

                @Test
                @DisplayName("같은 리스트에 복제 시 성공한다")
                void shouldCloneCardWithinSameListSuccessfully() {
                        // given
                        CloneCardCommand sameListCommand = CloneCardCommand.of(
                                        cardId,
                                        "복제된 카드",
                                        null, // 같은 리스트에 복제
                                        userId);

                        when(cardValidator.validateClone(sameListCommand))
                                        .thenReturn(ValidationResult.valid(sameListCommand));
                        when(cardRepository.findById(cardId))
                                        .thenReturn(Optional.of(originalCard));
                        when(boardListRepository.findById(sourceListId))
                                        .thenReturn(Optional.of(sourceBoardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.of(sourceBoard));
                        when(cardClonePolicy.canCloneWithinSameList(originalCard))
                                        .thenReturn(Either.right(null));
                        when(cardRepository.findMaxPositionByListId(sourceListId))
                                        .thenReturn(Optional.of(5));
                        when(cardRepository.save(any(Card.class)))
                                        .thenReturn(Either.right(clonedCard));
                        // logCardDuplicateActivity에서 사용하는 boardListRepository 호출에 대한 mock 설정
                        when(boardListRepository.findById(clonedCard.getListId()))
                                        .thenReturn(Optional.of(sourceBoardList));

                        // when
                        Either<Failure, Card> result = createCardService.cloneCard(sameListCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get()).isEqualTo(clonedCard);

                        verify(cardClonePolicy).canCloneWithinSameList(originalCard);
                        verify(cardRepository).findMaxPositionByListId(sourceListId);
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
                                                        .build(),
                                        Failure.FieldViolation.builder()
                                                        .field("newTitle")
                                                        .message("제목은 필수입니다.")
                                                        .rejectedValue(null)
                                                        .build());
                        when(cardValidator.validateClone(validCommand))
                                        .thenReturn(ValidationResult
                                                        .invalid(io.vavr.collection.List.ofAll(validationErrors)));

                        // when
                        Either<Failure, Card> result = createCardService.cloneCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                        Failure.InputError inputError = (Failure.InputError) result.getLeft();
                        assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
                        assertThat(inputError.getViolations()).containsExactlyElementsOf(validationErrors);
                        assertThat(inputError.getMessage()).isEqualTo("입력값이 유효하지 않습니다.");

                        // 다른 메서드들이 호출되지 않았는지 확인
                        verifyNoInteractions(cardRepository, boardListRepository, boardRepository,
                                        cardClonePolicy, activityHelper);
                }

                @Test
                @DisplayName("원본 카드가 존재하지 않을 때 실패를 반환한다")
                void shouldReturnFailureWhenOriginalCardNotFound() {
                        // given
                        when(cardValidator.validateClone(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, Card> result = createCardService.cloneCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
                        Failure.NotFound notFound = (Failure.NotFound) result.getLeft();
                        assertThat(notFound.getErrorCode()).isEqualTo("NOT_FOUND");
                        assertThat(notFound.getMessage()).isEqualTo("복제할 카드를 찾을 수 없습니다.");

                        // 다른 메서드들이 호출되지 않았는지 확인
                        verifyNoInteractions(boardListRepository, boardRepository, cardClonePolicy,
                                        activityHelper);
                }

                @Test
                @DisplayName("원본 카드 접근 권한이 없을 때 실패를 반환한다")
                void shouldReturnFailureWhenSourceBoardAccessDenied() {
                        // given
                        when(cardValidator.validateClone(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId))
                                        .thenReturn(Optional.of(originalCard));
                        when(boardListRepository.findById(sourceListId))
                                        .thenReturn(Optional.of(sourceBoardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, Card> result = createCardService.cloneCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
                        Failure.PermissionDenied permissionDenied = (Failure.PermissionDenied) result.getLeft();
                        assertThat(permissionDenied.getErrorCode()).isEqualTo("PERMISSION_DENIED");
                        assertThat(permissionDenied.getMessage()).isEqualTo("보드 접근 권한이 없습니다.");

                        // 다른 메서드들이 호출되지 않았는지 확인
                        verifyNoInteractions(cardClonePolicy, activityHelper);
                }

                @Test
                @DisplayName("대상 리스트 접근 권한이 없을 때 실패를 반환한다")
                void shouldReturnFailureWhenTargetBoardAccessDenied() {
                        // given
                        when(cardValidator.validateClone(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId))
                                        .thenReturn(Optional.of(originalCard));
                        when(boardListRepository.findById(sourceListId))
                                        .thenReturn(Optional.of(sourceBoardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.of(sourceBoard)) // 첫 번째 호출 (원본 카드)
                                        .thenReturn(Optional.empty()); // 두 번째 호출 (대상 리스트)
                        when(boardListRepository.findById(targetListId))
                                        .thenReturn(Optional.of(sourceBoardList));

                        // when
                        Either<Failure, Card> result = createCardService.cloneCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
                        Failure.PermissionDenied permissionDenied = (Failure.PermissionDenied) result.getLeft();
                        assertThat(permissionDenied.getErrorCode()).isEqualTo("PERMISSION_DENIED");
                        assertThat(permissionDenied.getMessage()).isEqualTo("보드 접근 권한이 없습니다.");

                        // 다른 메서드들이 호출되지 않았는지 확인
                        verifyNoInteractions(cardClonePolicy, activityHelper);
                }

                @Test
                @DisplayName("아카이브된 보드에서 카드 복제 시도 시 실패를 반환한다")
                void shouldReturnFailureWhenSourceBoardIsArchived() {
                        // given
                        Board archivedBoard = Board.builder()
                                        .boardId(boardId)
                                        .title("아카이브된 보드")
                                        .description("아카이브된 보드")
                                        .isArchived(true)
                                        .ownerId(userId)
                                        .isStarred(false)
                                        .build();

                        when(cardValidator.validateClone(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId))
                                        .thenReturn(Optional.of(originalCard));
                        when(boardListRepository.findById(sourceListId))
                                        .thenReturn(Optional.of(sourceBoardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.of(archivedBoard));

                        // when
                        Either<Failure, Card> result = createCardService.cloneCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.BusinessRuleViolation.class);
                        Failure.BusinessRuleViolation businessRuleViolation = (Failure.BusinessRuleViolation) result
                                        .getLeft();
                        assertThat(businessRuleViolation.getErrorCode()).isEqualTo("BUSINESS_RULE_VIOLATION");
                        assertThat(businessRuleViolation.getMessage()).isEqualTo("아카이브된 보드에서는 카드를 복제할 수 없습니다.");

                        // 다른 메서드들이 호출되지 않았는지 확인
                        verifyNoInteractions(cardClonePolicy, activityHelper);
                }

                @Test
                @DisplayName("카드 복제 정책 검증 실패 시 실패를 반환한다")
                void shouldReturnFailureWhenClonePolicyFails() {
                        // given
                        Failure policyFailure = Failure.ofForbidden("카드 개수 제한을 초과했습니다.");

                        when(cardValidator.validateClone(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId))
                                        .thenReturn(Optional.of(originalCard));
                        when(boardListRepository.findById(sourceListId))
                                        .thenReturn(Optional.of(sourceBoardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.of(sourceBoard));
                        when(boardListRepository.findById(targetListId))
                                        .thenReturn(Optional.of(sourceBoardList));
                        when(cardClonePolicy.canCloneToAnotherList(originalCard, targetListId))
                                        .thenReturn(Either.left(policyFailure));

                        // when
                        Either<Failure, Card> result = createCardService.cloneCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isEqualTo(policyFailure);

                        // 다른 메서드들이 호출되지 않았는지 확인
                        verifyNoInteractions(activityHelper);
                }

                @Test
                @DisplayName("카드 저장 실패 시 실패를 반환한다")
                void shouldReturnFailureWhenClonedCardSaveFails() {
                        // given
                        Failure saveFailure = Failure.ofInternalServerError("저장 중 오류가 발생했습니다.");

                        when(cardValidator.validateClone(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId))
                                        .thenReturn(Optional.of(originalCard));
                        when(boardListRepository.findById(sourceListId))
                                        .thenReturn(Optional.of(sourceBoardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.of(sourceBoard));
                        when(boardListRepository.findById(targetListId))
                                        .thenReturn(Optional.of(sourceBoardList));
                        when(cardClonePolicy.canCloneToAnotherList(originalCard, targetListId))
                                        .thenReturn(Either.right(null));
                        when(cardRepository.findMaxPositionByListId(targetListId))
                                        .thenReturn(Optional.of(5));
                        when(cardRepository.save(any(Card.class)))
                                        .thenReturn(Either.left(saveFailure));

                        // when
                        Either<Failure, Card> result = createCardService.cloneCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isEqualTo(saveFailure);

                        // activityHelper가 호출되지 않았는지 확인
                        verifyNoInteractions(activityHelper);
                }

                @Test
                @DisplayName("같은 리스트에서 복제 정책 검증 실패 시 실패를 반환한다")
                void shouldReturnFailureWhenSameListClonePolicyFails() {
                        // given
                        CloneCardCommand sameListCommand = CloneCardCommand.of(
                                        cardId, "복제된 카드", null, userId);
                        Failure policyFailure = Failure.ofForbidden("같은 리스트에서 복제할 수 없습니다.");

                        when(cardValidator.validateClone(sameListCommand))
                                        .thenReturn(ValidationResult.valid(sameListCommand));
                        when(cardRepository.findById(cardId))
                                        .thenReturn(Optional.of(originalCard));
                        when(boardListRepository.findById(sourceListId))
                                        .thenReturn(Optional.of(sourceBoardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.of(sourceBoard));
                        when(cardClonePolicy.canCloneWithinSameList(originalCard))
                                        .thenReturn(Either.left(policyFailure));

                        // when
                        Either<Failure, Card> result = createCardService.cloneCard(sameListCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isEqualTo(policyFailure);

                        // 다른 메서드들이 호출되지 않았는지 확인
                        verifyNoInteractions(activityHelper);
                }
        }

        @Nested
        @DisplayName("공통 유틸리티 메서드 테스트")
        class UtilityMethodTest {

                private UserId userId;
                private ListId listId;
                private BoardId boardId;
                private BoardList boardList;

                @BeforeEach
                void setUp() {
                        userId = new UserId("user-123");
                        listId = new ListId("list-123");
                        boardId = new BoardId("board-123");
                        boardList = BoardList.create("테스트 리스트", 0, boardId);
                }

                @Test
                @DisplayName("리스트가 존재하지 않을 때 실패를 반환한다")
                void shouldReturnFailureWhenListNotFound() {
                        // given
                        CloneCardCommand command = CloneCardCommand.of(
                                        new CardId("card-123"), "복제된 카드", listId, userId);

                        when(cardValidator.validateClone(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(cardRepository.findById(command.cardId()))
                                        .thenReturn(Optional.of(Card.create("원본 카드", "원본 설명", 0, listId)));
                        when(boardListRepository.findById(listId))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, Card> result = createCardService.cloneCard(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
                        assertThat(((Failure.NotFound) result.getLeft()).getErrorCode()).isEqualTo("NOT_FOUND");
                        assertThat(((Failure.NotFound) result.getLeft()).getMessage())
                                        .isEqualTo("리스트를 찾을 수 없습니다.");
                }

                @Test
                @DisplayName("보드 접근 권한이 없을 때 실패를 반환한다")
                void shouldReturnFailureWhenBoardAccessDenied() {
                        // given
                        CloneCardCommand command = CloneCardCommand.of(
                                        new CardId("card-123"), "복제된 카드", listId, userId);

                        when(cardValidator.validateClone(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(cardRepository.findById(command.cardId()))
                                        .thenReturn(Optional.of(Card.create("원본 카드", "원본 설명", 0, listId)));
                        when(boardListRepository.findById(listId))
                                        .thenReturn(Optional.of(boardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, Card> result = createCardService.cloneCard(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
                        assertThat(((Failure.PermissionDenied) result.getLeft()).getErrorCode())
                                        .isEqualTo("PERMISSION_DENIED");
                        assertThat(((Failure.PermissionDenied) result.getLeft()).getMessage())
                                        .isEqualTo("보드 접근 권한이 없습니다.");
                }

                @Test
                @DisplayName("아카이브된 보드일 때 실패를 반환한다")
                void shouldReturnFailureWhenBoardIsArchived() {
                        // given
                        CloneCardCommand command = CloneCardCommand.of(
                                        new CardId("card-123"), "복제된 카드", listId, userId);

                        Board archivedBoard = Board.builder()
                                        .boardId(boardId)
                                        .title("아카이브된 보드")
                                        .description("아카이브된 보드")
                                        .isArchived(true)
                                        .ownerId(userId)
                                        .isStarred(false)
                                        .build();

                        when(cardValidator.validateClone(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(cardRepository.findById(command.cardId()))
                                        .thenReturn(Optional.of(Card.create("원본 카드", "원본 설명", 0, listId)));
                        when(boardListRepository.findById(listId))
                                        .thenReturn(Optional.of(boardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.of(archivedBoard));

                        // when
                        Either<Failure, Card> result = createCardService.cloneCard(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.BusinessRuleViolation.class);
                        assertThat(((Failure.BusinessRuleViolation) result.getLeft()).getErrorCode())
                                        .isEqualTo("BUSINESS_RULE_VIOLATION");
                        assertThat(((Failure.BusinessRuleViolation) result.getLeft()).getMessage())
                                        .isEqualTo("아카이브된 보드에서는 카드를 복제할 수 없습니다.");
                }
        }
}