package com.boardly.features.card.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
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
                validationMessageResolver);

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

        @BeforeEach
        void setUp() {
            validCommand = CreateCardCommand.of(
                    "테스트 카드",
                    "테스트 설명",
                    new ListId("list-123"),
                    new UserId("user-123"));

            boardList = BoardList.create(
                    "테스트 리스트",
                    0,
                    new BoardId("board-123"));

            board = Board.create(
                    "테스트 보드",
                    "테스트 보드 설명",
                    new UserId("user-123"));

            createdCard = Card.create(
                    "테스트 카드",
                    "테스트 설명",
                    0,
                    new ListId("list-123"));
        }

        @Test
        @DisplayName("유효한 커맨드로 카드 생성 시 성공한다")
        void shouldCreateCardSuccessfully() {
            // given
            when(cardValidator.validateCreate(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(boardListRepository.findById(validCommand.listId()))
                    .thenReturn(Optional.of(boardList));
            when(boardRepository.findByIdAndOwnerId(boardList.getBoardId(), validCommand.userId()))
                    .thenReturn(Optional.of(board));
            when(cardCreationPolicy.canCreateCard(validCommand.listId()))
                    .thenReturn(Either.right(null));
            when(cardRepository.findMaxPositionByListId(validCommand.listId()))
                    .thenReturn(Optional.of(5));
            when(cardRepository.save(any(Card.class)))
                    .thenReturn(Either.right(createdCard));

            // when
            Either<Failure, Card> result = createCardService.createCard(validCommand);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(createdCard);
            verify(cardRepository).findMaxPositionByListId(validCommand.listId());
            verify(cardRepository).save(any(Card.class));
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
                    .thenReturn(ValidationResult.invalid(io.vavr.collection.List.ofAll(validationErrors)));

            // when
            Either<Failure, Card> result = createCardService.createCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
            Failure.InputError inputError = (Failure.InputError) result.getLeft();
            assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
            assertThat(inputError.getViolations()).containsExactlyElementsOf(validationErrors);
        }

        @Test
        @DisplayName("리스트가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenListNotFound() {
            // given
            when(cardValidator.validateCreate(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(boardListRepository.findById(validCommand.listId()))
                    .thenReturn(Optional.empty());

            // when
            Either<Failure, Card> result = createCardService.createCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
            assertThat(((Failure.NotFound) result.getLeft()).getErrorCode()).isEqualTo("NOT_FOUND");
        }

        @Test
        @DisplayName("보드 접근 권한이 없을 때 실패를 반환한다")
        void shouldReturnFailureWhenBoardAccessDenied() {
            // given
            when(cardValidator.validateCreate(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(boardListRepository.findById(validCommand.listId()))
                    .thenReturn(Optional.of(boardList));
            when(boardRepository.findByIdAndOwnerId(boardList.getBoardId(), validCommand.userId()))
                    .thenReturn(Optional.empty());

            // when
            Either<Failure, Card> result = createCardService.createCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
            assertThat(((Failure.PermissionDenied) result.getLeft()).getErrorCode()).isEqualTo("PERMISSION_DENIED");
        }

        @Test
        @DisplayName("아카이브된 보드에 카드 생성 시도 시 실패를 반환한다")
        void shouldReturnFailureWhenBoardIsArchived() {
            // given
            Board archivedBoard = Board.builder()
                    .boardId(new BoardId("board-123"))
                    .title("아카이브된 보드")
                    .description("아카이브된 보드")
                    .isArchived(true)
                    .ownerId(new UserId("user-123"))
                    .isStarred(false)
                    .build();

            when(cardValidator.validateCreate(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(boardListRepository.findById(validCommand.listId()))
                    .thenReturn(Optional.of(boardList));
            when(boardRepository.findByIdAndOwnerId(boardList.getBoardId(), validCommand.userId()))
                    .thenReturn(Optional.of(archivedBoard));

            // when
            Either<Failure, Card> result = createCardService.createCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.BusinessRuleViolation.class);
            assertThat(((Failure.BusinessRuleViolation) result.getLeft()).getErrorCode())
                    .isEqualTo("BUSINESS_RULE_VIOLATION");
        }

        @Test
        @DisplayName("카드 생성 정책 검증 실패 시 실패를 반환한다")
        void shouldReturnFailureWhenCreationPolicyFails() {
            // given
            Failure policyFailure = Failure.ofForbidden("카드 개수 제한을 초과했습니다.");

            when(cardValidator.validateCreate(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(boardListRepository.findById(validCommand.listId()))
                    .thenReturn(Optional.of(boardList));
            when(boardRepository.findByIdAndOwnerId(boardList.getBoardId(), validCommand.userId()))
                    .thenReturn(Optional.of(board));
            when(cardCreationPolicy.canCreateCard(validCommand.listId()))
                    .thenReturn(Either.left(policyFailure));

            // when
            Either<Failure, Card> result = createCardService.createCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isEqualTo(policyFailure);
        }

        @Test
        @DisplayName("카드 저장 실패 시 실패를 반환한다")
        void shouldReturnFailureWhenCardSaveFails() {
            // given
            Failure saveFailure = Failure.ofInternalServerError("저장 중 오류가 발생했습니다.");

            when(cardValidator.validateCreate(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(boardListRepository.findById(validCommand.listId()))
                    .thenReturn(Optional.of(boardList));
            when(boardRepository.findByIdAndOwnerId(boardList.getBoardId(), validCommand.userId()))
                    .thenReturn(Optional.of(board));
            when(cardCreationPolicy.canCreateCard(validCommand.listId()))
                    .thenReturn(Either.right(null));
            when(cardRepository.findMaxPositionByListId(validCommand.listId()))
                    .thenReturn(Optional.of(5));
            when(cardRepository.save(any(Card.class)))
                    .thenReturn(Either.left(saveFailure));

            // when
            Either<Failure, Card> result = createCardService.createCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isEqualTo(saveFailure);
        }

        @Test
        @DisplayName("첫 번째 카드 생성 시 위치가 0으로 설정된다")
        void shouldSetPositionToZeroForFirstCard() {
            // given
            when(cardValidator.validateCreate(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(boardListRepository.findById(validCommand.listId()))
                    .thenReturn(Optional.of(boardList));
            when(boardRepository.findByIdAndOwnerId(boardList.getBoardId(), validCommand.userId()))
                    .thenReturn(Optional.of(board));
            when(cardCreationPolicy.canCreateCard(validCommand.listId()))
                    .thenReturn(Either.right(null));
            when(cardRepository.findMaxPositionByListId(validCommand.listId()))
                    .thenReturn(Optional.empty());
            when(cardRepository.save(any(Card.class)))
                    .thenReturn(Either.right(createdCard));

            // when
            Either<Failure, Card> result = createCardService.createCard(validCommand);

            // then
            assertThat(result.isRight()).isTrue();
            verify(cardRepository).findMaxPositionByListId(validCommand.listId());
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

        @BeforeEach
        void setUp() {
            validCommand = CloneCardCommand.of(
                    new CardId("card-123"),
                    "복제된 카드",
                    new ListId("list-456"),
                    new UserId("user-123"));

            originalCard = Card.create(
                    "원본 카드",
                    "원본 설명",
                    0,
                    new ListId("list-123"));

            sourceBoardList = BoardList.create(
                    "소스 리스트",
                    0,
                    new BoardId("board-123"));

            sourceBoard = Board.create(
                    "소스 보드",
                    "소스 보드 설명",
                    new UserId("user-123"));

            clonedCard = Card.create(
                    "복제된 카드",
                    "원본 설명",
                    0,
                    new ListId("list-456"));
        }

        @Test
        @DisplayName("유효한 커맨드로 카드 복제 시 성공한다")
        void shouldCloneCardSuccessfully() {
            // given
            when(cardValidator.validateClone(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(validCommand.cardId()))
                    .thenReturn(Optional.of(originalCard));
            when(boardListRepository.findById(originalCard.getListId()))
                    .thenReturn(Optional.of(sourceBoardList));
            when(boardRepository.findByIdAndOwnerId(sourceBoardList.getBoardId(), validCommand.userId()))
                    .thenReturn(Optional.of(sourceBoard));
            when(boardListRepository.findById(validCommand.targetListId()))
                    .thenReturn(Optional.of(sourceBoardList));
            when(cardClonePolicy.canCloneToAnotherList(originalCard, validCommand.targetListId()))
                    .thenReturn(Either.right(null));
            when(cardRepository.findMaxPositionByListId(validCommand.targetListId()))
                    .thenReturn(Optional.of(5));
            when(cardRepository.save(any(Card.class)))
                    .thenReturn(Either.right(clonedCard));

            // when
            Either<Failure, Card> result = createCardService.cloneCard(validCommand);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(clonedCard);
            verify(cardRepository).findMaxPositionByListId(validCommand.targetListId());
            verify(cardRepository).save(any(Card.class));
        }

        @Test
        @DisplayName("같은 리스트에 복제 시 성공한다")
        void shouldCloneCardWithinSameListSuccessfully() {
            // given
            CloneCardCommand sameListCommand = CloneCardCommand.of(
                    new CardId("card-123"),
                    "복제된 카드",
                    null, // 같은 리스트에 복제
                    new UserId("user-123"));

            when(cardValidator.validateClone(sameListCommand))
                    .thenReturn(ValidationResult.valid(sameListCommand));
            when(cardRepository.findById(sameListCommand.cardId()))
                    .thenReturn(Optional.of(originalCard));
            when(boardListRepository.findById(originalCard.getListId()))
                    .thenReturn(Optional.of(sourceBoardList));
            when(boardRepository.findByIdAndOwnerId(sourceBoardList.getBoardId(), sameListCommand.userId()))
                    .thenReturn(Optional.of(sourceBoard));
            when(cardClonePolicy.canCloneWithinSameList(originalCard))
                    .thenReturn(Either.right(null));
            when(cardRepository.findMaxPositionByListId(originalCard.getListId()))
                    .thenReturn(Optional.of(5));
            when(cardRepository.save(any(Card.class)))
                    .thenReturn(Either.right(clonedCard));

            // when
            Either<Failure, Card> result = createCardService.cloneCard(sameListCommand);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(clonedCard);
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
                    .thenReturn(ValidationResult.invalid(io.vavr.collection.List.ofAll(validationErrors)));

            // when
            Either<Failure, Card> result = createCardService.cloneCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
            Failure.InputError inputError = (Failure.InputError) result.getLeft();
            assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
            assertThat(inputError.getViolations()).containsExactlyElementsOf(validationErrors);
        }

        @Test
        @DisplayName("원본 카드가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenOriginalCardNotFound() {
            // given
            when(cardValidator.validateClone(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(validCommand.cardId()))
                    .thenReturn(Optional.empty());

            // when
            Either<Failure, Card> result = createCardService.cloneCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
            assertThat(((Failure.NotFound) result.getLeft()).getErrorCode()).isEqualTo("NOT_FOUND");
        }

        @Test
        @DisplayName("원본 카드 접근 권한이 없을 때 실패를 반환한다")
        void shouldReturnFailureWhenSourceBoardAccessDenied() {
            // given
            when(cardValidator.validateClone(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(validCommand.cardId()))
                    .thenReturn(Optional.of(originalCard));
            when(boardListRepository.findById(originalCard.getListId()))
                    .thenReturn(Optional.of(sourceBoardList));
            when(boardRepository.findByIdAndOwnerId(sourceBoardList.getBoardId(), validCommand.userId()))
                    .thenReturn(Optional.empty());

            // when
            Either<Failure, Card> result = createCardService.cloneCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
            assertThat(((Failure.PermissionDenied) result.getLeft()).getErrorCode()).isEqualTo("PERMISSION_DENIED");
        }

        @Test
        @DisplayName("대상 리스트 접근 권한이 없을 때 실패를 반환한다")
        void shouldReturnFailureWhenTargetBoardAccessDenied() {
            // given
            when(cardValidator.validateClone(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(validCommand.cardId()))
                    .thenReturn(Optional.of(originalCard));
            when(boardListRepository.findById(originalCard.getListId()))
                    .thenReturn(Optional.of(sourceBoardList));
            when(boardRepository.findByIdAndOwnerId(sourceBoardList.getBoardId(), validCommand.userId()))
                    .thenReturn(Optional.of(sourceBoard));
            when(boardListRepository.findById(validCommand.targetListId()))
                    .thenReturn(Optional.of(sourceBoardList));
            // 대상 리스트의 보드에 대한 접근 권한이 없는 경우
            when(boardRepository.findByIdAndOwnerId(sourceBoardList.getBoardId(), validCommand.userId()))
                    .thenReturn(Optional.of(sourceBoard)) // 첫 번째 호출 (원본 카드)
                    .thenReturn(Optional.empty()); // 두 번째 호출 (대상 리스트)

            // when
            Either<Failure, Card> result = createCardService.cloneCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
            assertThat(((Failure.PermissionDenied) result.getLeft()).getErrorCode()).isEqualTo("PERMISSION_DENIED");
        }

        @Test
        @DisplayName("아카이브된 보드에서 카드 복제 시도 시 실패를 반환한다")
        void shouldReturnFailureWhenSourceBoardIsArchived() {
            // given
            Board archivedBoard = Board.builder()
                    .boardId(new BoardId("board-123"))
                    .title("아카이브된 보드")
                    .description("아카이브된 보드")
                    .isArchived(true)
                    .ownerId(new UserId("user-123"))
                    .isStarred(false)
                    .build();

            when(cardValidator.validateClone(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(validCommand.cardId()))
                    .thenReturn(Optional.of(originalCard));
            when(boardListRepository.findById(originalCard.getListId()))
                    .thenReturn(Optional.of(sourceBoardList));
            when(boardRepository.findByIdAndOwnerId(sourceBoardList.getBoardId(), validCommand.userId()))
                    .thenReturn(Optional.of(archivedBoard));

            // when
            Either<Failure, Card> result = createCardService.cloneCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.BusinessRuleViolation.class);
            assertThat(((Failure.BusinessRuleViolation) result.getLeft()).getErrorCode())
                    .isEqualTo("BUSINESS_RULE_VIOLATION");
        }

        @Test
        @DisplayName("카드 복제 정책 검증 실패 시 실패를 반환한다")
        void shouldReturnFailureWhenClonePolicyFails() {
            // given
            Failure policyFailure = Failure.ofForbidden("카드 개수 제한을 초과했습니다.");

            when(cardValidator.validateClone(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(validCommand.cardId()))
                    .thenReturn(Optional.of(originalCard));
            when(boardListRepository.findById(originalCard.getListId()))
                    .thenReturn(Optional.of(sourceBoardList));
            when(boardRepository.findByIdAndOwnerId(sourceBoardList.getBoardId(), validCommand.userId()))
                    .thenReturn(Optional.of(sourceBoard));
            when(boardListRepository.findById(validCommand.targetListId()))
                    .thenReturn(Optional.of(sourceBoardList));
            when(cardClonePolicy.canCloneToAnotherList(originalCard, validCommand.targetListId()))
                    .thenReturn(Either.left(policyFailure));

            // when
            Either<Failure, Card> result = createCardService.cloneCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isEqualTo(policyFailure);
        }

        @Test
        @DisplayName("카드 저장 실패 시 실패를 반환한다")
        void shouldReturnFailureWhenClonedCardSaveFails() {
            // given
            Failure saveFailure = Failure.ofInternalServerError("저장 중 오류가 발생했습니다.");

            when(cardValidator.validateClone(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(validCommand.cardId()))
                    .thenReturn(Optional.of(originalCard));
            when(boardListRepository.findById(originalCard.getListId()))
                    .thenReturn(Optional.of(sourceBoardList));
            when(boardRepository.findByIdAndOwnerId(sourceBoardList.getBoardId(), validCommand.userId()))
                    .thenReturn(Optional.of(sourceBoard));
            when(boardListRepository.findById(validCommand.targetListId()))
                    .thenReturn(Optional.of(sourceBoardList));
            when(cardClonePolicy.canCloneToAnotherList(originalCard, validCommand.targetListId()))
                    .thenReturn(Either.right(null));
            when(cardRepository.findMaxPositionByListId(validCommand.targetListId()))
                    .thenReturn(Optional.of(5));
            when(cardRepository.save(any(Card.class)))
                    .thenReturn(Either.left(saveFailure));

            // when
            Either<Failure, Card> result = createCardService.cloneCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isEqualTo(saveFailure);
        }
    }

    @Nested
    @DisplayName("validateBoardAccess 메서드 테스트")
    class ValidateBoardAccessTest {

        private ListId listId;
        private UserId userId;
        private BoardList boardList;
        private Board board;

        @BeforeEach
        void setUp() {
            listId = new ListId("list-123");
            userId = new UserId("user-123");
            boardList = BoardList.create(
                    "테스트 리스트",
                    0,
                    new BoardId("board-123"));
            board = Board.create(
                    "테스트 보드",
                    "테스트 보드 설명",
                    userId);
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
            when(boardRepository.findByIdAndOwnerId(boardList.getBoardId(), userId))
                    .thenReturn(Optional.empty());

            // when
            Either<Failure, Card> result = createCardService.cloneCard(command);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
            assertThat(((Failure.PermissionDenied) result.getLeft()).getErrorCode()).isEqualTo("PERMISSION_DENIED");
        }

        @Test
        @DisplayName("아카이브된 보드일 때 실패를 반환한다")
        void shouldReturnFailureWhenBoardIsArchived() {
            // given
            CloneCardCommand command = CloneCardCommand.of(
                    new CardId("card-123"), "복제된 카드", listId, userId);

            Board archivedBoard = Board.builder()
                    .boardId(new BoardId("board-123"))
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
            when(boardRepository.findByIdAndOwnerId(boardList.getBoardId(), userId))
                    .thenReturn(Optional.of(archivedBoard));

            // when
            Either<Failure, Card> result = createCardService.cloneCard(command);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.BusinessRuleViolation.class);
            assertThat(((Failure.BusinessRuleViolation) result.getLeft()).getErrorCode())
                    .isEqualTo("BUSINESS_RULE_VIOLATION");
        }
    }
}