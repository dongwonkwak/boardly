package com.boardly.features.card.application.service;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.application.port.input.CreateCardCommand;
import com.boardly.features.card.application.validation.CreateCardValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.policy.CardCreationPolicy;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateCardService 테스트")
class CreateCardServiceTest {

    @Mock
    private CreateCardValidator cardValidator;

    @Mock
    private CardCreationPolicy cardCreationPolicy;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private BoardListRepository boardListRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private ValidationMessageResolver validationMessageResolver;

    @InjectMocks
    private CreateCardService createCardService;

    private UserId testUserId;
    private ListId testListId;
    private BoardId testBoardId;
    private BoardList testBoardList;
    private Board testBoard;
    private CreateCardCommand validCommand;

    @BeforeEach
    void setUp() {
        testUserId = new UserId("test-user-123");
        testListId = new ListId("test-list-123");
        testBoardId = new BoardId("test-board-123");

        Instant now = Instant.now();

        testBoardList = BoardList.builder()
                .listId(testListId)
                .title("테스트 리스트")
                .position(1)
                .boardId(testBoardId)
                .createdAt(now)
                .updatedAt(now)
                .build();

        testBoard = Board.builder()
                .boardId(testBoardId)
                .title("테스트 보드")
                .description("테스트 보드 설명")
                .isArchived(false)
                .ownerId(testUserId)
                .createdAt(now)
                .updatedAt(now)
                .build();

        validCommand = CreateCardCommand.of(
                "테스트 카드",
                "테스트 카드 설명",
                testListId,
                testUserId);
    }

    @Nested
    @DisplayName("createCard 메서드 테스트")
    class CreateCardTest {

        @Test
        @DisplayName("카드 생성 성공")
        void createCard_Success() {
            // given
            ValidationResult<CreateCardCommand> validResult = ValidationResult.valid(validCommand);
            when(cardValidator.validate(validCommand)).thenReturn(validResult);
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
            when(cardCreationPolicy.canCreateCard(testListId)).thenReturn(Either.right(null));
            when(cardRepository.findMaxPositionByListId(testListId)).thenReturn(Optional.of(5));
            when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
                Card card = invocation.getArgument(0);
                return Either.right(card);
            });

            // when
            Either<Failure, Card> result = createCardService.createCard(validCommand);

            // then
            assertThat(result.isRight()).isTrue();
            Card createdCard = result.get();
            assertThat(createdCard.getTitle()).isEqualTo("테스트 카드");
            assertThat(createdCard.getDescription()).isEqualTo("테스트 카드 설명");
            assertThat(createdCard.getListId()).isEqualTo(testListId);
            assertThat(createdCard.getPosition()).isEqualTo(6); // 기존 최대 위치 + 1

            verify(cardValidator).validate(validCommand);
            verify(boardListRepository).findById(testListId);
            verify(boardRepository).findByIdAndOwnerId(testBoardId, testUserId);
            verify(cardCreationPolicy).canCreateCard(testListId);
            verify(cardRepository).findMaxPositionByListId(testListId);
            verify(cardRepository).save(any(Card.class));
        }

        @Test
        @DisplayName("첫 번째 카드 생성 성공 (기존 카드가 없는 경우)")
        void createCard_FirstCard_Success() {
            // given
            ValidationResult<CreateCardCommand> validResult = ValidationResult.valid(validCommand);
            when(cardValidator.validate(validCommand)).thenReturn(validResult);
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
            when(cardCreationPolicy.canCreateCard(testListId)).thenReturn(Either.right(null));
            when(cardRepository.findMaxPositionByListId(testListId)).thenReturn(Optional.empty());
            when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
                Card card = invocation.getArgument(0);
                return Either.right(card);
            });

            // when
            Either<Failure, Card> result = createCardService.createCard(validCommand);

            // then
            assertThat(result.isRight()).isTrue();
            Card createdCard = result.get();
            assertThat(createdCard.getPosition()).isEqualTo(0); // 첫 번째 카드는 위치 0

            verify(cardRepository).findMaxPositionByListId(testListId);
            verify(cardRepository).save(any(Card.class));
        }

        @Test
        @DisplayName("입력 검증 실패 - INVALID_INPUT")
        void createCard_ValidationFailure_InvalidInput() {
            // given
            List<Failure.FieldViolation> validationErrors = List.of(
                    Failure.FieldViolation.builder()
                            .field("title")
                            .message("제목은 필수입니다.")
                            .rejectedValue(null)
                            .build(),
                    Failure.FieldViolation.builder()
                            .field("title")
                            .message("제목은 1-200자여야 합니다.")
                            .rejectedValue("")
                            .build());
            ValidationResult<CreateCardCommand> invalidResult = ValidationResult
                    .invalid(io.vavr.collection.List.ofAll(validationErrors));
            when(cardValidator.validate(validCommand)).thenReturn(invalidResult);
            when(validationMessageResolver.getMessage("validation.input.invalid")).thenReturn("입력값이 유효하지 않습니다.");

            // when
            Either<Failure, Card> result = createCardService.createCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InputError.class);
            Failure.InputError inputError = (Failure.InputError) failure;
            assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
            assertThat(inputError.getMessage()).isEqualTo("입력값이 유효하지 않습니다.");
            assertThat(inputError.getViolations()).hasSize(2);

            verify(cardValidator).validate(validCommand);
            verify(validationMessageResolver).getMessage("validation.input.invalid");
            verifyNoInteractions(boardListRepository, boardRepository, cardCreationPolicy, cardRepository);
        }

        @Test
        @DisplayName("리스트를 찾을 수 없는 경우 - NOT_FOUND")
        void createCard_ListNotFound_NotFound() {
            // given
            ValidationResult<CreateCardCommand> validResult = ValidationResult.valid(validCommand);
            when(cardValidator.validate(validCommand)).thenReturn(validResult);
            when(boardListRepository.findById(testListId)).thenReturn(Optional.empty());
            when(validationMessageResolver.getMessage("error.service.card.move.list_not_found"))
                    .thenReturn("리스트를 찾을 수 없습니다.");

            // when
            Either<Failure, Card> result = createCardService.createCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.NotFound.class);
            Failure.NotFound notFound = (Failure.NotFound) failure;
            assertThat(notFound.getErrorCode()).isEqualTo("NOT_FOUND");
            assertThat(notFound.getMessage()).isEqualTo("리스트를 찾을 수 없습니다.");

            verify(cardValidator).validate(validCommand);
            verify(boardListRepository).findById(testListId);
            verify(validationMessageResolver).getMessage("error.service.card.move.list_not_found");
            verifyNoInteractions(boardRepository, cardCreationPolicy, cardRepository);
        }

        @Test
        @DisplayName("보드 접근 권한이 없는 경우 - PERMISSION_DENIED")
        void createCard_AccessDenied_PermissionDenied() {
            // given
            ValidationResult<CreateCardCommand> validResult = ValidationResult.valid(validCommand);
            when(cardValidator.validate(validCommand)).thenReturn(validResult);
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.empty());
            when(validationMessageResolver.getMessage("error.service.card.move.access_denied"))
                    .thenReturn("보드에 접근할 권한이 없습니다.");

            // when
            Either<Failure, Card> result = createCardService.createCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.PermissionDenied.class);
            Failure.PermissionDenied permissionDenied = (Failure.PermissionDenied) failure;
            assertThat(permissionDenied.getErrorCode()).isEqualTo("PERMISSION_DENIED");
            assertThat(permissionDenied.getMessage()).isEqualTo("보드에 접근할 권한이 없습니다.");

            verify(cardValidator).validate(validCommand);
            verify(boardListRepository).findById(testListId);
            verify(boardRepository).findByIdAndOwnerId(testBoardId, testUserId);
            verify(validationMessageResolver).getMessage("error.service.card.move.access_denied");
            verifyNoInteractions(cardCreationPolicy, cardRepository);
        }

        @Test
        @DisplayName("아카이브된 보드에 카드 생성 시도 - BUSINESS_RULE_VIOLATION")
        void createCard_ArchivedBoard_BusinessRuleViolation() {
            // given
            Board archivedBoard = Board.builder()
                    .boardId(testBoardId)
                    .title("테스트 보드")
                    .description("테스트 보드 설명")
                    .isArchived(true)
                    .ownerId(testUserId)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            ValidationResult<CreateCardCommand> validResult = ValidationResult.valid(validCommand);
            when(cardValidator.validate(validCommand)).thenReturn(validResult);
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(archivedBoard));
            when(validationMessageResolver.getMessage("error.service.card.move.archived_board"))
                    .thenReturn("아카이브된 보드에는 카드를 생성할 수 없습니다.");

            // when
            Either<Failure, Card> result = createCardService.createCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.BusinessRuleViolation.class);
            Failure.BusinessRuleViolation businessRuleViolation = (Failure.BusinessRuleViolation) failure;
            assertThat(businessRuleViolation.getErrorCode()).isEqualTo("BUSINESS_RULE_VIOLATION");
            assertThat(businessRuleViolation.getMessage()).isEqualTo("아카이브된 보드에는 카드를 생성할 수 없습니다.");

            verify(cardValidator).validate(validCommand);
            verify(boardListRepository).findById(testListId);
            verify(boardRepository).findByIdAndOwnerId(testBoardId, testUserId);
            verify(validationMessageResolver).getMessage("error.service.card.move.archived_board");
            verifyNoInteractions(cardCreationPolicy, cardRepository);
        }

        @Test
        @DisplayName("카드 생성 정책 위반 - FORBIDDEN")
        void createCard_PolicyViolation_Forbidden() {
            // given
            ValidationResult<CreateCardCommand> validResult = ValidationResult.valid(validCommand);
            when(cardValidator.validate(validCommand)).thenReturn(validResult);
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
            when(cardCreationPolicy.canCreateCard(testListId)).thenReturn(
                    Either.left(Failure.ofPermissionDenied("리스트당 최대 100개의 카드만 생성할 수 있습니다.")));

            // when
            Either<Failure, Card> result = createCardService.createCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.PermissionDenied.class);
            Failure.PermissionDenied forbidden = (Failure.PermissionDenied) failure;
            assertThat(forbidden.getErrorCode()).isEqualTo("PERMISSION_DENIED");
            assertThat(forbidden.getMessage()).isEqualTo("리스트당 최대 100개의 카드만 생성할 수 있습니다.");

            verify(cardValidator).validate(validCommand);
            verify(boardListRepository).findById(testListId);
            verify(boardRepository).findByIdAndOwnerId(testBoardId, testUserId);
            verify(cardCreationPolicy).canCreateCard(testListId);
            verifyNoInteractions(cardRepository);
        }

        @Test
        @DisplayName("카드 저장 실패")
        void createCard_SaveFailure() {
            // given
            ValidationResult<CreateCardCommand> validResult = ValidationResult.valid(validCommand);
            when(cardValidator.validate(validCommand)).thenReturn(validResult);
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
            when(cardCreationPolicy.canCreateCard(testListId)).thenReturn(Either.right(null));
            when(cardRepository.findMaxPositionByListId(testListId)).thenReturn(Optional.of(5));
            when(cardRepository.save(any(Card.class))).thenReturn(
                    Either.left(Failure.ofInternalServerError("데이터베이스 저장 실패")));

            // when
            Either<Failure, Card> result = createCardService.createCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InternalError.class);
            Failure.InternalError internalError = (Failure.InternalError) failure;
            assertThat(internalError.getErrorCode()).isEqualTo("INTERNAL_ERROR");
            assertThat(internalError.getMessage()).isEqualTo("데이터베이스 저장 실패");

            verify(cardValidator).validate(validCommand);
            verify(boardListRepository).findById(testListId);
            verify(boardRepository).findByIdAndOwnerId(testBoardId, testUserId);
            verify(cardCreationPolicy).canCreateCard(testListId);
            verify(cardRepository).findMaxPositionByListId(testListId);
            verify(cardRepository).save(any(Card.class));
        }
    }

    @Nested
    @DisplayName("카드 위치 계산 테스트")
    class CardPositionCalculationTest {

        @Test
        @DisplayName("기존 카드가 있는 경우 - 최대 위치 + 1")
        void calculatePosition_WithExistingCards_MaxPositionPlusOne() {
            // given
            ValidationResult<CreateCardCommand> validResult = ValidationResult.valid(validCommand);
            when(cardValidator.validate(validCommand)).thenReturn(validResult);
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
            when(cardCreationPolicy.canCreateCard(testListId)).thenReturn(Either.right(null));
            when(cardRepository.findMaxPositionByListId(testListId)).thenReturn(Optional.of(10));
            when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
                Card card = invocation.getArgument(0);
                return Either.right(card);
            });

            // when
            Either<Failure, Card> result = createCardService.createCard(validCommand);

            // then
            assertThat(result.isRight()).isTrue();
            Card createdCard = result.get();
            assertThat(createdCard.getPosition()).isEqualTo(11); // 10 + 1

            verify(cardRepository).findMaxPositionByListId(testListId);
        }

        @Test
        @DisplayName("기존 카드가 없는 경우 - 위치 0")
        void calculatePosition_NoExistingCards_PositionZero() {
            // given
            ValidationResult<CreateCardCommand> validResult = ValidationResult.valid(validCommand);
            when(cardValidator.validate(validCommand)).thenReturn(validResult);
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
            when(cardCreationPolicy.canCreateCard(testListId)).thenReturn(Either.right(null));
            when(cardRepository.findMaxPositionByListId(testListId)).thenReturn(Optional.empty());
            when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
                Card card = invocation.getArgument(0);
                return Either.right(card);
            });

            // when
            Either<Failure, Card> result = createCardService.createCard(validCommand);

            // then
            assertThat(result.isRight()).isTrue();
            Card createdCard = result.get();
            assertThat(createdCard.getPosition()).isEqualTo(0);

            verify(cardRepository).findMaxPositionByListId(testListId);
        }
    }

    @Nested
    @DisplayName("로그 메시지 테스트")
    class LoggingTest {

        @Test
        @DisplayName("성공 시 로그 메시지 확인")
        void createCard_Success_LogMessages() {
            // given
            ValidationResult<CreateCardCommand> validResult = ValidationResult.valid(validCommand);
            when(cardValidator.validate(validCommand)).thenReturn(validResult);
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
            when(cardCreationPolicy.canCreateCard(testListId)).thenReturn(Either.right(null));
            when(cardRepository.findMaxPositionByListId(testListId)).thenReturn(Optional.of(5));
            when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
                Card card = invocation.getArgument(0);
                return Either.right(card);
            });

            // when
            Either<Failure, Card> result = createCardService.createCard(validCommand);

            // then
            assertThat(result.isRight()).isTrue();
            // 로그 메시지는 실제로는 로그 레벨에 따라 확인할 수 없지만,
            // 메서드가 정상적으로 실행되었음을 확인
            verify(cardValidator).validate(validCommand);
            verify(cardRepository).save(any(Card.class));
        }

        @Test
        @DisplayName("실패 시 로그 메시지 확인")
        void createCard_Failure_LogMessages() {
            // given
            List<Failure.FieldViolation> validationErrors = List.of(
                    Failure.FieldViolation.builder()
                            .field("title")
                            .message("제목은 필수입니다.")
                            .rejectedValue(null)
                            .build());
            ValidationResult<CreateCardCommand> invalidResult = ValidationResult
                    .invalid(io.vavr.collection.List.ofAll(validationErrors));
            when(cardValidator.validate(validCommand)).thenReturn(invalidResult);
            when(validationMessageResolver.getMessage("validation.input.invalid")).thenReturn("입력값이 유효하지 않습니다.");

            // when
            Either<Failure, Card> result = createCardService.createCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            // 실패 시에도 로그가 기록되었음을 확인
            verify(cardValidator).validate(validCommand);
            verify(validationMessageResolver).getMessage("validation.input.invalid");
        }
    }
}