package com.boardly.features.boardlist.application.service;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.application.port.input.CreateBoardListCommand;
import com.boardly.features.boardlist.application.validation.BoardListValidator;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListColor;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.policy.BoardListCreationPolicy;
import com.boardly.features.boardlist.domain.policy.BoardListPolicyConfig;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * BoardListCreateService 테스트
 * 
 * @since 1.0.0
 */
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
        private ValidationMessageResolver validationMessageResolver;

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
                                ListColor.defaultColor());

                createdBoardList = BoardList.create(
                                "테스트 리스트",
                                "테스트 리스트 설명",
                                0,
                                ListColor.defaultColor(),
                                testBoardId);
        }

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {

                @Test
                @DisplayName("유효한 정보로 리스트 생성이 성공해야 한다")
                void createBoardList_withValidData_shouldReturnCreatedBoardList() {
                        // given
                        ValidationResult<CreateBoardListCommand> validResult = ValidationResult.valid(validCommand);

                        when(boardListValidator.validateCreateBoardList(validCommand)).thenReturn(validResult);
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardListCreationPolicy.canCreateBoardList(testBoardId)).thenReturn(Either.right(null));
                        when(boardListPolicyConfig.getMaxTitleLength()).thenReturn(100);
                        when(boardListRepository.findMaxPositionByBoardId(testBoardId)).thenReturn(Optional.empty());
                        when(boardListRepository.save(any(BoardList.class))).thenReturn(createdBoardList);

                        // when
                        Either<Failure, BoardList> result = boardListCreateService.createBoardList(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        BoardList createdList = result.get();
                        assertThat(createdList.getTitle()).isEqualTo("테스트 리스트");
                        assertThat(createdList.getDescription()).isEqualTo("테스트 리스트 설명");
                        assertThat(createdList.getBoardId()).isEqualTo(testBoardId);
                        assertThat(createdList.getColor()).isEqualTo(ListColor.defaultColor());
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
                                        any(ListId.class));
                }

                @Test
                @DisplayName("기존 리스트가 있는 경우 다음 위치에 생성되어야 한다")
                void createBoardList_withExistingLists_shouldCreateAtNextPosition() {
                        // given
                        ValidationResult<CreateBoardListCommand> validResult = ValidationResult.valid(validCommand);
                        BoardList createdBoardListAtPosition3 = BoardList.create(
                                        "테스트 리스트",
                                        "테스트 리스트 설명",
                                        3,
                                        ListColor.defaultColor(),
                                        testBoardId);

                        when(boardListValidator.validateCreateBoardList(validCommand)).thenReturn(validResult);
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardListCreationPolicy.canCreateBoardList(testBoardId)).thenReturn(Either.right(null));
                        when(boardListPolicyConfig.getMaxTitleLength()).thenReturn(100);
                        when(boardListRepository.findMaxPositionByBoardId(testBoardId)).thenReturn(Optional.of(2));
                        when(boardListRepository.save(any(BoardList.class))).thenReturn(createdBoardListAtPosition3);

                        // when
                        Either<Failure, BoardList> result = boardListCreateService.createBoardList(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        verify(boardListRepository).findMaxPositionByBoardId(testBoardId);
                        verify(boardListRepository).save(argThat(boardList -> boardList.getPosition() == 3));
                        verify(activityHelper).logListCreate(
                                        eq(testUserId),
                                        eq("테스트 리스트"),
                                        eq("테스트 보드"),
                                        eq(testBoardId),
                                        any(ListId.class));
                }

                @Test
                @DisplayName("빈 설명으로 리스트 생성이 성공해야 한다")
                void createBoardList_withEmptyDescription_shouldSucceed() {
                        // given
                        CreateBoardListCommand emptyDescriptionCommand = new CreateBoardListCommand(
                                        testBoardId,
                                        testUserId,
                                        "테스트 리스트",
                                        "",
                                        ListColor.defaultColor());

                        ValidationResult<CreateBoardListCommand> validResult = ValidationResult
                                        .valid(emptyDescriptionCommand);
                        BoardList createdBoardListWithEmptyDesc = BoardList.create(
                                        "테스트 리스트",
                                        "",
                                        0,
                                        ListColor.defaultColor(),
                                        testBoardId);

                        when(boardListValidator.validateCreateBoardList(emptyDescriptionCommand))
                                        .thenReturn(validResult);
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardListCreationPolicy.canCreateBoardList(testBoardId)).thenReturn(Either.right(null));
                        when(boardListPolicyConfig.getMaxTitleLength()).thenReturn(100);
                        when(boardListRepository.findMaxPositionByBoardId(testBoardId)).thenReturn(Optional.empty());
                        when(boardListRepository.save(any(BoardList.class))).thenReturn(createdBoardListWithEmptyDesc);

                        // when
                        Either<Failure, BoardList> result = boardListCreateService
                                        .createBoardList(emptyDescriptionCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get().getDescription()).isEqualTo("");
                }

                @Test
                @DisplayName("null 설명으로 리스트 생성이 성공해야 한다")
                void createBoardList_withNullDescription_shouldSucceed() {
                        // given
                        CreateBoardListCommand nullDescriptionCommand = new CreateBoardListCommand(
                                        testBoardId,
                                        testUserId,
                                        "테스트 리스트",
                                        null,
                                        ListColor.defaultColor());

                        ValidationResult<CreateBoardListCommand> validResult = ValidationResult
                                        .valid(nullDescriptionCommand);
                        BoardList createdBoardListWithNullDesc = BoardList.create(
                                        "테스트 리스트",
                                        null,
                                        0,
                                        ListColor.defaultColor(),
                                        testBoardId);

                        when(boardListValidator.validateCreateBoardList(nullDescriptionCommand))
                                        .thenReturn(validResult);
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardListCreationPolicy.canCreateBoardList(testBoardId)).thenReturn(Either.right(null));
                        when(boardListPolicyConfig.getMaxTitleLength()).thenReturn(100);
                        when(boardListRepository.findMaxPositionByBoardId(testBoardId)).thenReturn(Optional.empty());
                        when(boardListRepository.save(any(BoardList.class))).thenReturn(createdBoardListWithNullDesc);

                        // when
                        Either<Failure, BoardList> result = boardListCreateService
                                        .createBoardList(nullDescriptionCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get().getDescription()).isNull();
                }

                @Test
                @DisplayName("제목 길이가 정확히 최대 길이일 때 성공해야 한다")
                void createBoardList_withExactMaxTitleLength_shouldSucceed() {
                        // given
                        String maxLengthTitle = "a".repeat(100);
                        CreateBoardListCommand maxLengthCommand = new CreateBoardListCommand(
                                        testBoardId,
                                        testUserId,
                                        maxLengthTitle,
                                        "테스트 리스트 설명",
                                        ListColor.defaultColor());

                        ValidationResult<CreateBoardListCommand> validResult = ValidationResult.valid(maxLengthCommand);
                        BoardList createdBoardListWithMaxTitle = BoardList.create(
                                        maxLengthTitle,
                                        "테스트 리스트 설명",
                                        0,
                                        ListColor.defaultColor(),
                                        testBoardId);

                        when(boardListValidator.validateCreateBoardList(maxLengthCommand)).thenReturn(validResult);
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardListCreationPolicy.canCreateBoardList(testBoardId)).thenReturn(Either.right(null));
                        when(boardListPolicyConfig.getMaxTitleLength()).thenReturn(100);
                        when(boardListRepository.findMaxPositionByBoardId(testBoardId)).thenReturn(Optional.empty());
                        when(boardListRepository.save(any(BoardList.class))).thenReturn(createdBoardListWithMaxTitle);

                        // when
                        Either<Failure, BoardList> result = boardListCreateService.createBoardList(maxLengthCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get().getTitle()).isEqualTo(maxLengthTitle);
                }
        }

        @Nested
        @DisplayName("입력 검증 실패 케이스")
        class InputValidationFailureCases {

                @Test
                @DisplayName("입력 검증 실패 시 InputError를 반환해야 한다")
                void createBoardList_withInvalidData_shouldReturnInputError() {
                        // given
                        ValidationResult<CreateBoardListCommand> invalidResult = ValidationResult.invalid(
                                        "title", "제목은 필수입니다", validCommand);

                        when(validationMessageResolver.getMessage("validation.input.invalid"))
                                        .thenReturn("입력이 유효하지 않습니다");
                        when(boardListValidator.validateCreateBoardList(validCommand)).thenReturn(invalidResult);

                        // when
                        Either<Failure, BoardList> result = boardListCreateService.createBoardList(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                        Failure.InputError inputError = (Failure.InputError) result.getLeft();
                        assertThat(inputError.getMessage()).isEqualTo("입력이 유효하지 않습니다");
                        assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
                        assertThat(inputError.getViolations()).isNotEmpty();

                        verify(boardListValidator).validateCreateBoardList(validCommand);
                        verify(validationMessageResolver).getMessage("validation.input.invalid");
                        verifyNoInteractions(boardRepository, boardListCreationPolicy, boardListRepository,
                                        activityHelper);
                }
        }

        @Nested
        @DisplayName("보드 접근 실패 케이스")
        class BoardAccessFailureCases {

                @Test
                @DisplayName("보드가 존재하지 않을 때 NotFound를 반환해야 한다")
                void createBoardList_withNonExistentBoard_shouldReturnNotFound() {
                        // given
                        ValidationResult<CreateBoardListCommand> validResult = ValidationResult.valid(validCommand);

                        when(validationMessageResolver.getMessage("validation.board.not.found"))
                                        .thenReturn("보드를 찾을 수 없습니다");
                        when(boardListValidator.validateCreateBoardList(validCommand)).thenReturn(validResult);
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.empty());

                        // when
                        Either<Failure, BoardList> result = boardListCreateService.createBoardList(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
                        Failure.NotFound notFound = (Failure.NotFound) result.getLeft();
                        assertThat(notFound.getMessage()).isEqualTo("보드를 찾을 수 없습니다");
                        assertThat(notFound.getErrorCode()).isEqualTo("BOARD_NOT_FOUND");
                        assertThat(notFound.getContext()).isInstanceOf(Map.class);
                        assertThat((Map<String, Object>) notFound.getContext()).containsEntry("boardId",
                                        testBoardId.getId());

                        verify(boardListValidator).validateCreateBoardList(validCommand);
                        verify(boardRepository).findById(testBoardId);
                        verify(validationMessageResolver).getMessage("validation.board.not.found");
                        verifyNoInteractions(boardListCreationPolicy, boardListRepository, activityHelper);
                }

                @Test
                @DisplayName("보드 접근 권한이 없을 때 PermissionDenied를 반환해야 한다")
                void createBoardList_withUnauthorizedAccess_shouldReturnPermissionDenied() {
                        // given
                        UserId unauthorizedUserId = new UserId("unauthorized-user-456");
                        CreateBoardListCommand unauthorizedCommand = new CreateBoardListCommand(
                                        testBoardId,
                                        unauthorizedUserId,
                                        "테스트 리스트",
                                        "테스트 리스트 설명",
                                        ListColor.defaultColor());

                        ValidationResult<CreateBoardListCommand> validResult = ValidationResult
                                        .valid(unauthorizedCommand);

                        when(validationMessageResolver.getMessage("validation.board.modification.access.denied"))
                                        .thenReturn("보드 수정 권한이 없습니다");
                        when(boardListValidator.validateCreateBoardList(unauthorizedCommand)).thenReturn(validResult);
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));

                        // when
                        Either<Failure, BoardList> result = boardListCreateService.createBoardList(unauthorizedCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
                        Failure.PermissionDenied permissionDenied = (Failure.PermissionDenied) result.getLeft();
                        assertThat(permissionDenied.getMessage()).isEqualTo("보드 수정 권한이 없습니다");
                        assertThat(permissionDenied.getErrorCode()).isEqualTo("UNAUTHORIZED_ACCESS");
                        assertThat(permissionDenied.getContext()).isInstanceOf(Map.class);
                        assertThat((Map<String, Object>) permissionDenied.getContext()).containsEntry("boardId",
                                        testBoardId.getId());
                        assertThat((Map<String, Object>) permissionDenied.getContext()).containsEntry("userId",
                                        unauthorizedUserId.getId());

                        verify(boardListValidator).validateCreateBoardList(unauthorizedCommand);
                        verify(boardRepository).findById(testBoardId);
                        verify(validationMessageResolver).getMessage("validation.board.modification.access.denied");
                        verifyNoInteractions(boardListCreationPolicy, boardListRepository, activityHelper);
                }
        }

        @Nested
        @DisplayName("비즈니스 규칙 위반 케이스")
        class BusinessRuleViolationCases {

                @Test
                @DisplayName("리스트 생성 정책 위반 시 BusinessRuleViolation을 반환해야 한다")
                void createBoardList_withPolicyViolation_shouldReturnBusinessRuleViolation() {
                        // given
                        ValidationResult<CreateBoardListCommand> validResult = ValidationResult.valid(validCommand);
                        Failure policyFailure = Failure.ofBusinessRuleViolation("보드당 최대 20개의 리스트만 생성할 수 있습니다");

                        when(boardListValidator.validateCreateBoardList(validCommand)).thenReturn(validResult);
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardListCreationPolicy.canCreateBoardList(testBoardId))
                                        .thenReturn(Either.left(policyFailure));

                        // when
                        Either<Failure, BoardList> result = boardListCreateService.createBoardList(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.BusinessRuleViolation.class);
                        Failure.BusinessRuleViolation businessRuleViolation = (Failure.BusinessRuleViolation) result
                                        .getLeft();
                        assertThat(businessRuleViolation.getMessage()).isEqualTo("보드당 최대 20개의 리스트만 생성할 수 있습니다");
                        assertThat(businessRuleViolation.getErrorCode()).isEqualTo("LIST_CREATION_POLICY_VIOLATION");
                        assertThat(businessRuleViolation.getContext()).isInstanceOf(Map.class);
                        assertThat((Map<String, Object>) businessRuleViolation.getContext()).containsEntry("boardId",
                                        testBoardId.getId());

                        verify(boardListValidator).validateCreateBoardList(validCommand);
                        verify(boardRepository).findById(testBoardId);
                        verify(boardListCreationPolicy).canCreateBoardList(testBoardId);
                        verifyNoInteractions(boardListRepository, activityHelper);
                }

                @Test
                @DisplayName("제목 길이 제한 초과 시 BusinessRuleViolation을 반환해야 한다")
                void createBoardList_withTitleLengthExceeded_shouldReturnBusinessRuleViolation() {
                        // given
                        String longTitle = "a".repeat(101);
                        CreateBoardListCommand longTitleCommand = new CreateBoardListCommand(
                                        testBoardId,
                                        testUserId,
                                        longTitle,
                                        "테스트 리스트 설명",
                                        ListColor.defaultColor());

                        ValidationResult<CreateBoardListCommand> validResult = ValidationResult.valid(longTitleCommand);

                        when(boardListValidator.validateCreateBoardList(longTitleCommand)).thenReturn(validResult);
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardListCreationPolicy.canCreateBoardList(testBoardId)).thenReturn(Either.right(null));
                        when(boardListPolicyConfig.getMaxTitleLength()).thenReturn(100);

                        // when
                        Either<Failure, BoardList> result = boardListCreateService.createBoardList(longTitleCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.BusinessRuleViolation.class);
                        Failure.BusinessRuleViolation businessRuleViolation = (Failure.BusinessRuleViolation) result
                                        .getLeft();
                        assertThat(businessRuleViolation.getMessage()).isEqualTo("리스트 제목은 최대 100자까지 입력할 수 있습니다.");
                        assertThat(businessRuleViolation.getErrorCode()).isEqualTo("TITLE_LENGTH_EXCEEDED");
                        assertThat(businessRuleViolation.getContext()).isInstanceOf(Map.class);
                        assertThat((Map<String, Object>) businessRuleViolation.getContext()).containsEntry("boardId",
                                        testBoardId.getId());
                        assertThat((Map<String, Object>) businessRuleViolation.getContext())
                                        .containsEntry("titleLength", 101);

                        verify(boardListValidator).validateCreateBoardList(longTitleCommand);
                        verify(boardRepository).findById(testBoardId);
                        verify(boardListCreationPolicy).canCreateBoardList(testBoardId);
                        verify(boardListPolicyConfig, times(3)).getMaxTitleLength();
                        verifyNoInteractions(boardListRepository, activityHelper);
                }
        }

        @Nested
        @DisplayName("시스템 오류 케이스")
        class SystemErrorCases {

                @Test
                @DisplayName("리스트 저장 중 예외 발생 시 InternalError를 반환해야 한다")
                void createBoardList_withSaveException_shouldReturnInternalError() {
                        // given
                        ValidationResult<CreateBoardListCommand> validResult = ValidationResult.valid(validCommand);
                        RuntimeException saveException = new RuntimeException("데이터베이스 연결 오류");

                        when(boardListValidator.validateCreateBoardList(validCommand)).thenReturn(validResult);
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardListCreationPolicy.canCreateBoardList(testBoardId)).thenReturn(Either.right(null));
                        when(boardListPolicyConfig.getMaxTitleLength()).thenReturn(100);
                        when(boardListRepository.findMaxPositionByBoardId(testBoardId)).thenReturn(Optional.empty());
                        when(boardListRepository.save(any(BoardList.class))).thenThrow(saveException);

                        // when
                        Either<Failure, BoardList> result = boardListCreateService.createBoardList(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
                        Failure.InternalError internalError = (Failure.InternalError) result.getLeft();
                        assertThat(internalError.getMessage()).isEqualTo("데이터베이스 연결 오류");
                        assertThat(internalError.getErrorCode()).isEqualTo("LIST_CREATION_ERROR");

                        verify(boardListValidator).validateCreateBoardList(validCommand);
                        verify(boardRepository).findById(testBoardId);
                        verify(boardListCreationPolicy).canCreateBoardList(testBoardId);
                        verify(boardListRepository).findMaxPositionByBoardId(testBoardId);
                        verify(boardListRepository).save(any(BoardList.class));
                        verifyNoInteractions(activityHelper);
                }

                @Test
                @DisplayName("최대 위치 조회 중 예외 발생 시 InternalError를 반환해야 한다")
                void createBoardList_withMaxPositionException_shouldReturnInternalError() {
                        // given
                        ValidationResult<CreateBoardListCommand> validResult = ValidationResult.valid(validCommand);
                        RuntimeException maxPositionException = new RuntimeException("위치 조회 오류");

                        when(boardListValidator.validateCreateBoardList(validCommand)).thenReturn(validResult);
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardListCreationPolicy.canCreateBoardList(testBoardId)).thenReturn(Either.right(null));
                        when(boardListPolicyConfig.getMaxTitleLength()).thenReturn(100);
                        when(boardListRepository.findMaxPositionByBoardId(testBoardId)).thenThrow(maxPositionException);

                        // when
                        Either<Failure, BoardList> result = boardListCreateService.createBoardList(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
                        Failure.InternalError internalError = (Failure.InternalError) result.getLeft();
                        assertThat(internalError.getMessage()).isEqualTo("위치 조회 오류");
                        assertThat(internalError.getErrorCode()).isEqualTo("LIST_CREATION_ERROR");

                        verify(boardListValidator).validateCreateBoardList(validCommand);
                        verify(boardRepository).findById(testBoardId);
                        verify(boardListCreationPolicy).canCreateBoardList(testBoardId);
                        verify(boardListRepository).findMaxPositionByBoardId(testBoardId);
                        verify(boardListRepository, never()).save(any(BoardList.class));
                        verifyNoInteractions(activityHelper);
                }
        }

        @Nested
        @DisplayName("ActivityHelper 호출 검증")
        class ActivityHelperVerification {

                @Test
                @DisplayName("리스트 생성 성공 시 ActivityHelper.logListCreate가 호출되어야 한다")
                void createBoardList_success_shouldCallActivityHelper() {
                        // given
                        ValidationResult<CreateBoardListCommand> validResult = ValidationResult.valid(validCommand);

                        when(boardListValidator.validateCreateBoardList(validCommand)).thenReturn(validResult);
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardListCreationPolicy.canCreateBoardList(testBoardId)).thenReturn(Either.right(null));
                        when(boardListPolicyConfig.getMaxTitleLength()).thenReturn(100);
                        when(boardListRepository.findMaxPositionByBoardId(testBoardId)).thenReturn(Optional.empty());
                        when(boardListRepository.save(any(BoardList.class))).thenReturn(createdBoardList);

                        // when
                        Either<Failure, BoardList> result = boardListCreateService.createBoardList(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        verify(activityHelper).logListCreate(
                                        eq(testUserId),
                                        eq("테스트 리스트"),
                                        eq("테스트 보드"),
                                        eq(testBoardId),
                                        any(ListId.class));
                }

                @Test
                @DisplayName("리스트 생성 실패 시 ActivityHelper가 호출되지 않아야 한다")
                void createBoardList_failure_shouldNotCallActivityHelper() {
                        // given
                        ValidationResult<CreateBoardListCommand> invalidResult = ValidationResult.invalid(
                                        "title", "제목은 필수입니다", validCommand);

                        when(validationMessageResolver.getMessage("validation.input.invalid"))
                                        .thenReturn("입력이 유효하지 않습니다");
                        when(boardListValidator.validateCreateBoardList(validCommand)).thenReturn(invalidResult);

                        // when
                        Either<Failure, BoardList> result = boardListCreateService.createBoardList(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        verifyNoInteractions(activityHelper);
                }
        }
}