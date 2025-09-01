package com.boardly.features.boardlist.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.board.domain.Board;
import com.boardly.features.board.domain.port.BoardRepository;
import com.boardly.features.boardlist.application.command.CreateBoardListCommand;
import com.boardly.features.boardlist.application.validation.BoardListValidator;
import com.boardly.features.boardlist.domain.BoardList;
import com.boardly.features.boardlist.domain.config.BoardListPolicyConfig;
import com.boardly.features.boardlist.domain.port.BoardListRepository;
import com.boardly.features.boardlist.infrastructure.policy.BoardListCreationPolicy;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.ListColor;
import com.boardly.shared.common.value.ListId;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.validation.MessageResolver;
import com.boardly.shared.validation.ValidationResult;
import io.vavr.control.Either;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardListCreateService 테스트")
class BoardListCreateServiceTest {

    @Mock
    private BoardListValidator boardListValidator;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardListRepository boardListRepository;

    @Mock
    private BoardListCreationPolicy boardListCreationPolicy;

    @Mock
    private BoardListPolicyConfig boardListPolicyConfig;

    @Mock
    private MessageResolver messageResolver;

    @Mock
    private ActivityHelper activityHelper;

    @InjectMocks
    private BoardListCreateService boardListCreateService;

    private UserId testUserId;
    private BoardId testBoardId;
    private Board testBoard;
    private CreateBoardListCommand validCommand;
    private BoardList createdBoardList;

    @BeforeEach
    void setUp() {
        testUserId = new UserId("test-user-123");
        testBoardId = new BoardId("test-board-123");

        Instant now = Instant.now();
        testBoard = Board.builder()
            .boardId(testBoardId)
            .title("테스트 보드")
            .description("테스트 보드 설명")
            .isArchived(false)
            .ownerId(testUserId)
            .isStarred(false)
            .createdAt(now)
            .updatedAt(now)
            .build();

        validCommand = new CreateBoardListCommand(
            testBoardId,
            testUserId,
            "테스트 리스트",
            "테스트 리스트 설명",
            ListColor.defaultColor()
        );

        createdBoardList = BoardList.create(
            "테스트 리스트",
            "테스트 리스트 설명",
            0,
            ListColor.defaultColor(),
            testBoardId
        );
    }

    @Nested
    @DisplayName("성공 케이스")
    class SuccessCases {

        @Test
        @DisplayName("유효한 정보로 리스트 생성이 성공해야 한다")
        void createBoardList_withValidData_shouldReturnCreatedBoardList() {
            ValidationResult<CreateBoardListCommand> validResult =
                ValidationResult.valid(validCommand);

            when(
                boardListValidator.validateCreateBoardList(validCommand)
            ).thenReturn(validResult);
            when(boardRepository.findById(testBoardId)).thenReturn(
                Optional.of(testBoard)
            );
            when(
                boardListCreationPolicy.canCreateBoardList(testBoardId)
            ).thenReturn(Either.right(null));
            when(boardListPolicyConfig.getMaxTitleLength()).thenReturn(100);
            when(
                boardListRepository.findMaxPositionByBoardId(testBoardId)
            ).thenReturn(Optional.empty());
            when(boardListRepository.save(any(BoardList.class))).thenReturn(
                createdBoardList
            );

            Either<Failure, BoardList> result =
                boardListCreateService.createBoardList(validCommand);

            assertThat(result.isRight()).isTrue();
            BoardList createdList = result.get();
            assertThat(createdList.getTitle()).isEqualTo("테스트 리스트");
            assertThat(createdList.getDescription()).isEqualTo(
                "테스트 리스트 설명"
            );
            assertThat(createdList.getBoardId()).isEqualTo(testBoardId);
            assertThat(createdList.getColor()).isEqualTo(
                ListColor.defaultColor()
            );
            assertThat(createdList.getPosition()).isEqualTo(0);

            verify(boardListValidator).validateCreateBoardList(validCommand);
            verify(boardRepository).findById(testBoardId);
            verify(boardListCreationPolicy).canCreateBoardList(testBoardId);
            verify(boardListRepository).findMaxPositionByBoardId(testBoardId);
            verify(boardListRepository).save(any(BoardList.class));
            verify(activityHelper).logListCreate(
                eq(testUserId),
                eq("테스트 리스트"),
                eq("테스트 보드"),
                eq(testBoardId),
                any(ListId.class)
            );
        }

        @Test
        @DisplayName("기존 리스트가 있는 경우 다음 위치에 생성되어야 한다")
        void createBoardList_withExistingLists_shouldCreateAtNextPosition() {
            ValidationResult<CreateBoardListCommand> validResult =
                ValidationResult.valid(validCommand);
            BoardList createdBoardListAtPosition3 = BoardList.create(
                "테스트 리스트",
                "테스트 리스트 설명",
                3,
                ListColor.defaultColor(),
                testBoardId
            );

            when(
                boardListValidator.validateCreateBoardList(validCommand)
            ).thenReturn(validResult);
            when(boardRepository.findById(testBoardId)).thenReturn(
                Optional.of(testBoard)
            );
            when(
                boardListCreationPolicy.canCreateBoardList(testBoardId)
            ).thenReturn(Either.right(null));
            when(boardListPolicyConfig.getMaxTitleLength()).thenReturn(100);
            when(
                boardListRepository.findMaxPositionByBoardId(testBoardId)
            ).thenReturn(Optional.of(2));
            when(boardListRepository.save(any(BoardList.class))).thenReturn(
                createdBoardListAtPosition3
            );

            Either<Failure, BoardList> result =
                boardListCreateService.createBoardList(validCommand);

            assertThat(result.isRight()).isTrue();
            verify(boardListRepository).findMaxPositionByBoardId(testBoardId);
            verify(boardListRepository).save(
                argThat(boardList -> boardList.getPosition() == 3)
            );
            verify(activityHelper).logListCreate(
                eq(testUserId),
                eq("테스트 리스트"),
                eq("테스트 보드"),
                eq(testBoardId),
                any(ListId.class)
            );
        }
    }

    @Nested
    @DisplayName("입력 검증 실패 케이스")
    class InputValidationFailureCases {

        @Test
        @DisplayName("입력 검증 실패 시 InputError를 반환해야 한다")
        void createBoardList_withInvalidData_shouldReturnInputError() {
            ValidationResult<CreateBoardListCommand> invalidResult =
                ValidationResult.invalid(
                    "title",
                    "제목은 필수입니다",
                    validCommand
                );

            when(
                messageResolver.getMessage("validation.input.invalid")
            ).thenReturn("입력이 유효하지 않습니다");
            when(
                boardListValidator.validateCreateBoardList(validCommand)
            ).thenReturn(invalidResult);

            Either<Failure, BoardList> result =
                boardListCreateService.createBoardList(validCommand);

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
            verify(boardListValidator).validateCreateBoardList(validCommand);
            verify(messageResolver).getMessage("validation.input.invalid");
            verifyNoInteractions(
                boardRepository,
                boardListCreationPolicy,
                boardListRepository,
                activityHelper
            );
        }
    }

    @Nested
    @DisplayName("보드 접근 실패 케이스")
    class BoardAccessFailureCases {

        @Test
        @DisplayName("보드가 존재하지 않을 때 NotFound를 반환해야 한다")
        void createBoardList_withNonExistentBoard_shouldReturnNotFound() {
            ValidationResult<CreateBoardListCommand> validResult =
                ValidationResult.valid(validCommand);

            when(
                messageResolver.getMessage("validation.board.not.found")
            ).thenReturn("보드를 찾을 수 없습니다");
            when(
                boardListValidator.validateCreateBoardList(validCommand)
            ).thenReturn(validResult);
            when(boardRepository.findById(testBoardId)).thenReturn(
                Optional.empty()
            );

            Either<Failure, BoardList> result =
                boardListCreateService.createBoardList(validCommand);

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
            verify(boardListValidator).validateCreateBoardList(validCommand);
            verify(boardRepository).findById(testBoardId);
            verify(messageResolver).getMessage("validation.board.not.found");
            verifyNoInteractions(
                boardListCreationPolicy,
                boardListRepository,
                activityHelper
            );
        }

        @Test
        @DisplayName(
            "보드 접근 권한이 없을 때 PermissionDenied를 반환해야 한다"
        )
        void createBoardList_withUnauthorizedAccess_shouldReturnPermissionDenied() {
            Board otherOwnerBoard = Board.builder()
                .boardId(testBoardId)
                .title("테스트 보드")
                .description("설명")
                .isArchived(false)
                .ownerId(new UserId("another-user"))
                .isStarred(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

            when(
                boardListValidator.validateCreateBoardList(validCommand)
            ).thenReturn(ValidationResult.valid(validCommand));
            when(boardRepository.findById(testBoardId)).thenReturn(
                Optional.of(otherOwnerBoard)
            );
            when(
                messageResolver.getMessage(
                    "validation.board.modification.access.denied"
                )
            ).thenReturn("보드 수정 권한이 없습니다");

            Either<Failure, BoardList> result =
                boardListCreateService.createBoardList(validCommand);

            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.PermissionDenied.class);
            Failure.PermissionDenied pd = (Failure.PermissionDenied) failure;
            assertThat(pd.getErrorCode()).isEqualTo("UNAUTHORIZED_ACCESS");
            assertThat(pd.getMessage()).isEqualTo("보드 수정 권한이 없습니다");
        }
    }

    @Nested
    @DisplayName("비즈니스 규칙 위반 케이스")
    class BusinessRuleViolationCases {

        @Test
        @DisplayName(
            "리스트 생성 정책 위반 시 BusinessRuleViolation을 반환해야 한다"
        )
        void createBoardList_withPolicyViolation_shouldReturnBusinessRuleViolation() {
            ValidationResult<CreateBoardListCommand> validResult =
                ValidationResult.valid(validCommand);
            Failure policyFailure = Failure.ofBusinessRuleViolation(
                "보드당 최대 20개의 리스트만 생성할 수 있습니다"
            );

            when(
                boardListValidator.validateCreateBoardList(validCommand)
            ).thenReturn(validResult);
            when(boardRepository.findById(testBoardId)).thenReturn(
                Optional.of(testBoard)
            );
            when(
                boardListCreationPolicy.canCreateBoardList(testBoardId)
            ).thenReturn(Either.left(policyFailure));

            Either<Failure, BoardList> result =
                boardListCreateService.createBoardList(validCommand);

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(
                Failure.BusinessRuleViolation.class
            );
        }

        @Test
        @DisplayName(
            "제목 길이 제한 초과 시 BusinessRuleViolation을 반환해야 한다"
        )
        void createBoardList_withTitleLengthExceeded_shouldReturnBusinessRuleViolation() {
            String longTitle = "a".repeat(101);
            CreateBoardListCommand longTitleCommand =
                new CreateBoardListCommand(
                    testBoardId,
                    testUserId,
                    longTitle,
                    "desc",
                    ListColor.defaultColor()
                );

            when(
                boardListValidator.validateCreateBoardList(longTitleCommand)
            ).thenReturn(ValidationResult.valid(longTitleCommand));
            when(boardRepository.findById(testBoardId)).thenReturn(
                Optional.of(testBoard)
            );
            when(
                boardListCreationPolicy.canCreateBoardList(testBoardId)
            ).thenReturn(Either.right(null));
            when(boardListPolicyConfig.getMaxTitleLength()).thenReturn(100);

            Either<Failure, BoardList> result =
                boardListCreateService.createBoardList(longTitleCommand);

            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(
                Failure.BusinessRuleViolation.class
            );
            Failure.BusinessRuleViolation brv =
                (Failure.BusinessRuleViolation) failure;
            assertThat(brv.getErrorCode()).isEqualTo("TITLE_LENGTH_EXCEEDED");
        }
    }

    @Nested
    @DisplayName("시스템 오류 케이스")
    class SystemErrorCases {

        @Test
        @DisplayName(
            "리스트 저장 중 예외 발생 시 InternalError를 반환해야 한다"
        )
        void createBoardList_withSaveException_shouldReturnInternalError() {
            when(
                boardListValidator.validateCreateBoardList(validCommand)
            ).thenReturn(ValidationResult.valid(validCommand));
            when(boardRepository.findById(testBoardId)).thenReturn(
                Optional.of(testBoard)
            );
            when(
                boardListCreationPolicy.canCreateBoardList(testBoardId)
            ).thenReturn(Either.right(null));
            when(boardListPolicyConfig.getMaxTitleLength()).thenReturn(100);
            when(
                boardListRepository.findMaxPositionByBoardId(testBoardId)
            ).thenReturn(Optional.empty());
            when(boardListRepository.save(any(BoardList.class))).thenThrow(
                new RuntimeException("DB ERROR")
            );

            Either<Failure, BoardList> result =
                boardListCreateService.createBoardList(validCommand);

            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InternalError.class);
            assertThat(
                ((Failure.InternalError) failure).getErrorCode()
            ).isEqualTo("LIST_CREATION_ERROR");
        }

        @Test
        @DisplayName(
            "최대 위치 조회 중 예외 발생 시 InternalError를 반환해야 한다"
        )
        void createBoardList_withMaxPositionException_shouldReturnInternalError() {
            when(
                boardListValidator.validateCreateBoardList(validCommand)
            ).thenReturn(ValidationResult.valid(validCommand));
            when(boardRepository.findById(testBoardId)).thenReturn(
                Optional.of(testBoard)
            );
            when(
                boardListCreationPolicy.canCreateBoardList(testBoardId)
            ).thenReturn(Either.right(null));
            when(boardListPolicyConfig.getMaxTitleLength()).thenReturn(100);
            when(
                boardListRepository.findMaxPositionByBoardId(testBoardId)
            ).thenThrow(new RuntimeException("POS ERROR"));

            Either<Failure, BoardList> result =
                boardListCreateService.createBoardList(validCommand);

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(
                Failure.InternalError.class
            );
        }
    }
}
