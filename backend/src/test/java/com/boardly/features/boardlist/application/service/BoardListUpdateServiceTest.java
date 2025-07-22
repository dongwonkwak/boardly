package com.boardly.features.boardlist.application.service;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.application.port.input.UpdateBoardListCommand;
import com.boardly.features.boardlist.application.port.input.UpdateBoardListPositionCommand;
import com.boardly.features.boardlist.application.validation.BoardListValidator;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListColor;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.policy.BoardListMovePolicy;
import com.boardly.features.boardlist.domain.policy.BoardListPolicyConfig;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.domain.common.Failure.FieldViolation;
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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * BoardListUpdateService 테스트
 * 
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BoardListUpdateService 테스트")
class BoardListUpdateServiceTest {

        @Mock
        private BoardListValidator boardListValidator;

        @Mock
        private BoardRepository boardRepository;

        @Mock
        private BoardListRepository boardListRepository;

        @Mock
        private BoardListPolicyConfig boardListPolicyConfig;

        @Mock
        private BoardListMovePolicy boardListMovePolicy;

        @Mock
        private ValidationMessageResolver validationMessageResolver;

        @InjectMocks
        private BoardListUpdateService boardListUpdateService;

        private UserId testUserId;
        private BoardId testBoardId;
        private ListId testListId;
        private Board testBoard;
        private BoardList testBoardList;
        private UpdateBoardListCommand validUpdateCommand;
        private UpdateBoardListPositionCommand validPositionCommand;

        @BeforeEach
        void setUp() {
                testUserId = new UserId("test-user-123");
                testBoardId = new BoardId("test-board-123");
                testListId = new ListId("test-list-123");

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

                testBoardList = BoardList.builder()
                                .listId(testListId)
                                .title("기존 리스트 제목")
                                .description("기존 리스트 설명")
                                .position(1)
                                .color(ListColor.defaultColor())
                                .boardId(testBoardId)
                                .createdAt(now)
                                .updatedAt(now)
                                .build();

                validUpdateCommand = new UpdateBoardListCommand(
                                testListId,
                                testUserId,
                                "새로운 제목",
                                "새로운 설명",
                                ListColor.of("#0079BF"));

                validPositionCommand = new UpdateBoardListPositionCommand(
                                testListId,
                                testUserId,
                                2);
        }

        @Nested
        @DisplayName("updateBoardList 메서드 테스트")
        class UpdateBoardListTest {

                @Test
                @DisplayName("유효한 데이터로 리스트 수정 시 성공해야 한다")
                void updateBoardList_withValidData_shouldReturnUpdatedBoardList() {
                        // given
                        ValidationResult<UpdateBoardListCommand> validResult = ValidationResult
                                        .valid(validUpdateCommand);
                        when(boardListValidator.validateUpdateBoardList(validUpdateCommand)).thenReturn(validResult);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardListPolicyConfig.getMaxTitleLength()).thenReturn(100);
                        when(boardListRepository.save(any(BoardList.class))).thenReturn(testBoardList);

                        // when
                        Either<Failure, BoardList> result = boardListUpdateService.updateBoardList(validUpdateCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        BoardList updatedList = result.get();
                        assertThat(updatedList.getTitle()).isEqualTo("새로운 제목");
                        assertThat(updatedList.getDescription()).isEqualTo("새로운 설명");
                        assertThat(updatedList.getColor()).isEqualTo(ListColor.of("#0079BF"));

                        verify(boardListRepository).save(any(BoardList.class));
                }

                @Test
                @DisplayName("제목만 수정할 때 성공해야 한다")
                void updateBoardList_withTitleOnly_shouldReturnUpdatedBoardList() {
                        // given
                        UpdateBoardListCommand titleOnlyCommand = new UpdateBoardListCommand(
                                        testListId, testUserId, "새로운 제목", null, null);

                        ValidationResult<UpdateBoardListCommand> validResult = ValidationResult.valid(titleOnlyCommand);
                        when(boardListValidator.validateUpdateBoardList(titleOnlyCommand)).thenReturn(validResult);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardListPolicyConfig.getMaxTitleLength()).thenReturn(100);
                        when(boardListRepository.save(any(BoardList.class))).thenReturn(testBoardList);

                        // when
                        Either<Failure, BoardList> result = boardListUpdateService.updateBoardList(titleOnlyCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        BoardList updatedList = result.get();
                        assertThat(updatedList.getTitle()).isEqualTo("새로운 제목");
                        assertThat(updatedList.getDescription()).isEqualTo("기존 리스트 설명"); // 기존 값 유지
                        assertThat(updatedList.getColor()).isEqualTo(ListColor.defaultColor()); // 기존 값 유지
                }

                @Test
                @DisplayName("입력 검증 실패 시 InputError를 반환해야 한다")
                void updateBoardList_withInvalidData_shouldReturnInputError() {
                        // given
                        List<FieldViolation> validationErrors = List.of(
                                        FieldViolation.builder().field("title").message("제목은 필수입니다").rejectedValue(null)
                                                        .build(),
                                        FieldViolation.builder().field("title").message("제목 길이가 너무 깁니다")
                                                        .rejectedValue("").build());
                        ValidationResult<UpdateBoardListCommand> invalidResult = ValidationResult
                                        .invalid(io.vavr.collection.List.ofAll(validationErrors));
                        when(boardListValidator.validateUpdateBoardList(validUpdateCommand)).thenReturn(invalidResult);
                        when(validationMessageResolver.getMessage("validation.input.invalid"))
                                        .thenReturn("입력값이 유효하지 않습니다");

                        // when
                        Either<Failure, BoardList> result = boardListUpdateService.updateBoardList(validUpdateCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.InputError.class);
                        Failure.InputError inputError = (Failure.InputError) failure;
                        assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
                        assertThat(inputError.getMessage()).isEqualTo("입력값이 유효하지 않습니다");
                        assertThat(inputError.getViolations()).containsExactlyElementsOf(validationErrors);
                }

                @Test
                @DisplayName("리스트가 존재하지 않을 때 NotFound를 반환해야 한다")
                void updateBoardList_withNonExistentList_shouldReturnNotFound() {
                        // given
                        ValidationResult<UpdateBoardListCommand> validResult = ValidationResult
                                        .valid(validUpdateCommand);
                        when(boardListValidator.validateUpdateBoardList(validUpdateCommand)).thenReturn(validResult);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.empty());
                        when(validationMessageResolver.getMessage("error.service.card.move.list_not_found"))
                                        .thenReturn("리스트를 찾을 수 없습니다");

                        // when
                        Either<Failure, BoardList> result = boardListUpdateService.updateBoardList(validUpdateCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.NotFound.class);
                        Failure.NotFound notFound = (Failure.NotFound) failure;
                        assertThat(notFound.getErrorCode()).isEqualTo("LIST_NOT_FOUND");
                        assertThat(notFound.getMessage()).isEqualTo("리스트를 찾을 수 없습니다");
                        assertThat((Map<String, Object>) notFound.getContext()).containsEntry("listId",
                                        testListId.getId());
                }

                @Test
                @DisplayName("보드가 존재하지 않을 때 NotFound를 반환해야 한다")
                void updateBoardList_withNonExistentBoard_shouldReturnNotFound() {
                        // given
                        ValidationResult<UpdateBoardListCommand> validResult = ValidationResult
                                        .valid(validUpdateCommand);
                        when(boardListValidator.validateUpdateBoardList(validUpdateCommand)).thenReturn(validResult);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.empty());
                        when(validationMessageResolver.getMessage("validation.board.not.found"))
                                        .thenReturn("보드를 찾을 수 없습니다");

                        // when
                        Either<Failure, BoardList> result = boardListUpdateService.updateBoardList(validUpdateCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.NotFound.class);
                        Failure.NotFound notFound = (Failure.NotFound) failure;
                        assertThat(notFound.getErrorCode()).isEqualTo("BOARD_NOT_FOUND");
                        assertThat(notFound.getMessage()).isEqualTo("보드를 찾을 수 없습니다");
                        assertThat((Map<String, Object>) notFound.getContext()).containsEntry("boardId",
                                        testBoardId.getId());
                }

                @Test
                @DisplayName("권한이 없을 때 PermissionDenied를 반환해야 한다")
                void updateBoardList_withUnauthorizedAccess_shouldReturnPermissionDenied() {
                        // given
                        UserId unauthorizedUserId = new UserId("unauthorized-user-456");
                        UpdateBoardListCommand unauthorizedCommand = new UpdateBoardListCommand(
                                        testListId, unauthorizedUserId, "새로운 제목", null, null);

                        ValidationResult<UpdateBoardListCommand> validResult = ValidationResult
                                        .valid(unauthorizedCommand);
                        when(boardListValidator.validateUpdateBoardList(unauthorizedCommand)).thenReturn(validResult);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(validationMessageResolver.getMessage("validation.board.modification.access.denied"))
                                        .thenReturn("수정 권한이 없습니다");

                        // when
                        Either<Failure, BoardList> result = boardListUpdateService.updateBoardList(unauthorizedCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.PermissionDenied.class);
                        Failure.PermissionDenied permissionDenied = (Failure.PermissionDenied) failure;
                        assertThat(permissionDenied.getErrorCode()).isEqualTo("UNAUTHORIZED_ACCESS");
                        assertThat(permissionDenied.getMessage()).isEqualTo("수정 권한이 없습니다");
                        assertThat((Map<String, Object>) permissionDenied.getContext()).containsEntry("listId",
                                        testListId.getId());
                        assertThat((Map<String, Object>) permissionDenied.getContext()).containsEntry("userId",
                                        unauthorizedUserId.getId());
                }

                @Test
                @DisplayName("제목 길이 제한 초과 시 BusinessRuleViolation을 반환해야 한다")
                void updateBoardList_withTitleLengthExceeded_shouldReturnBusinessRuleViolation() {
                        // given
                        String longTitle = "a".repeat(101);
                        UpdateBoardListCommand longTitleCommand = new UpdateBoardListCommand(
                                        testListId, testUserId, longTitle, null, null);

                        ValidationResult<UpdateBoardListCommand> validResult = ValidationResult.valid(longTitleCommand);
                        when(boardListValidator.validateUpdateBoardList(longTitleCommand)).thenReturn(validResult);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardListPolicyConfig.getMaxTitleLength()).thenReturn(100);

                        // when
                        Either<Failure, BoardList> result = boardListUpdateService.updateBoardList(longTitleCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.BusinessRuleViolation.class);
                        Failure.BusinessRuleViolation businessRuleViolation = (Failure.BusinessRuleViolation) failure;
                        assertThat(businessRuleViolation.getErrorCode()).isEqualTo("TITLE_LENGTH_EXCEEDED");
                        assertThat(businessRuleViolation.getMessage()).contains("리스트 제목은 최대 100자까지 입력할 수 있습니다");
                        assertThat((Map<String, Object>) businessRuleViolation.getContext()).containsEntry("listId",
                                        testListId.getId());
                        assertThat((Map<String, Object>) businessRuleViolation.getContext())
                                        .containsEntry("titleLength", 101);
                }

                @Test
                @DisplayName("저장 중 예외 발생 시 InternalError를 반환해야 한다")
                void updateBoardList_withSaveException_shouldReturnInternalError() {
                        // given
                        ValidationResult<UpdateBoardListCommand> validResult = ValidationResult
                                        .valid(validUpdateCommand);
                        when(boardListValidator.validateUpdateBoardList(validUpdateCommand)).thenReturn(validResult);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardListPolicyConfig.getMaxTitleLength()).thenReturn(100);
                        when(boardListRepository.save(any(BoardList.class))).thenThrow(new RuntimeException("저장 실패"));

                        // when
                        Either<Failure, BoardList> result = boardListUpdateService.updateBoardList(validUpdateCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.InternalError.class);
                        Failure.InternalError internalError = (Failure.InternalError) failure;
                        assertThat(internalError.getErrorCode()).isEqualTo("BOARD_LIST_UPDATE_ERROR");
                        assertThat(internalError.getMessage()).isEqualTo("저장 실패");
                }
        }

        @Nested
        @DisplayName("updateBoardListPosition 메서드 테스트")
        class UpdateBoardListPositionTest {

                @Test
                @DisplayName("유효한 위치 변경 시 성공해야 한다")
                void updateBoardListPosition_withValidPosition_shouldReturnUpdatedLists() {
                        // given
                        List<BoardList> allLists = List.of(
                                        createBoardList("리스트1", 0),
                                        createBoardList("리스트2", 1),
                                        createBoardList("리스트3", 2));

                        ValidationResult<UpdateBoardListPositionCommand> validResult = ValidationResult
                                        .valid(validPositionCommand);
                        when(boardListValidator.validateUpdateBoardListPosition(validPositionCommand))
                                        .thenReturn(validResult);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(allLists.get(1))); // position
                                                                                                                 // 1의
                                                                                                                 // 리스트
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardListRepository.findByBoardIdOrderByPosition(testBoardId)).thenReturn(allLists);
                        when(boardListMovePolicy.canMoveWithinSameBoard(any(), eq(2))).thenReturn(Either.right(null));
                        when(boardListMovePolicy.hasPositionChanged(any(), eq(2))).thenReturn(true);
                        when(boardListRepository.saveAll(anyList())).thenReturn(allLists);

                        // when
                        Either<Failure, List<BoardList>> result = boardListUpdateService
                                        .updateBoardListPosition(validPositionCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        List<BoardList> updatedLists = result.get();
                        assertThat(updatedLists).hasSize(3);
                        verify(boardListRepository).saveAll(anyList());
                }

                @Test
                @DisplayName("위치가 변경되지 않을 때 기존 리스트 목록을 반환해야 한다")
                void updateBoardListPosition_withNoPositionChange_shouldReturnExistingLists() {
                        // given
                        List<BoardList> allLists = List.of(
                                        createBoardList("리스트1", 0),
                                        createBoardList("리스트2", 1),
                                        createBoardList("리스트3", 2));

                        ValidationResult<UpdateBoardListPositionCommand> validResult = ValidationResult
                                        .valid(validPositionCommand);
                        when(boardListValidator.validateUpdateBoardListPosition(validPositionCommand))
                                        .thenReturn(validResult);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(allLists.get(1))); // position
                                                                                                                 // 1의
                                                                                                                 // 리스트
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardListRepository.findByBoardIdOrderByPosition(testBoardId)).thenReturn(allLists);
                        when(boardListMovePolicy.canMoveWithinSameBoard(any(), eq(2))).thenReturn(Either.right(null));
                        when(boardListMovePolicy.hasPositionChanged(any(), eq(2))).thenReturn(false);

                        // when
                        Either<Failure, List<BoardList>> result = boardListUpdateService
                                        .updateBoardListPosition(validPositionCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        List<BoardList> returnedLists = result.get();
                        assertThat(returnedLists).isEqualTo(allLists);
                        verify(boardListRepository, never()).saveAll(anyList());
                }

                @Test
                @DisplayName("입력 검증 실패 시 InputError를 반환해야 한다")
                void updateBoardListPosition_withInvalidData_shouldReturnInputError() {
                        // given
                        List<FieldViolation> validationErrors = List.of(
                                        FieldViolation.builder().field("newPosition").message("위치는 0 이상이어야 합니다")
                                                        .rejectedValue(-1).build());
                        ValidationResult<UpdateBoardListPositionCommand> invalidResult = ValidationResult
                                        .invalid(io.vavr.collection.List.ofAll(validationErrors));
                        when(boardListValidator.validateUpdateBoardListPosition(validPositionCommand))
                                        .thenReturn(invalidResult);
                        when(validationMessageResolver.getMessage("validation.input.invalid"))
                                        .thenReturn("입력값이 유효하지 않습니다");

                        // when
                        Either<Failure, List<BoardList>> result = boardListUpdateService
                                        .updateBoardListPosition(validPositionCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.InputError.class);
                        Failure.InputError inputError = (Failure.InputError) failure;
                        assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
                        assertThat(inputError.getMessage()).isEqualTo("입력값이 유효하지 않습니다");
                        assertThat(inputError.getViolations()).containsExactlyElementsOf(validationErrors);
                }

                @Test
                @DisplayName("리스트가 존재하지 않을 때 NotFound를 반환해야 한다")
                void updateBoardListPosition_withNonExistentList_shouldReturnNotFound() {
                        // given
                        ValidationResult<UpdateBoardListPositionCommand> validResult = ValidationResult
                                        .valid(validPositionCommand);
                        when(boardListValidator.validateUpdateBoardListPosition(validPositionCommand))
                                        .thenReturn(validResult);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.empty());

                        // when
                        Either<Failure, List<BoardList>> result = boardListUpdateService
                                        .updateBoardListPosition(validPositionCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.NotFound.class);
                        Failure.NotFound notFound = (Failure.NotFound) failure;
                        assertThat(notFound.getErrorCode()).isEqualTo("NOT_FOUND");
                }

                @Test
                @DisplayName("보드가 존재하지 않을 때 NotFound를 반환해야 한다")
                void updateBoardListPosition_withNonExistentBoard_shouldReturnNotFound() {
                        // given
                        ValidationResult<UpdateBoardListPositionCommand> validResult = ValidationResult
                                        .valid(validPositionCommand);
                        when(boardListValidator.validateUpdateBoardListPosition(validPositionCommand))
                                        .thenReturn(validResult);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.empty());

                        // when
                        Either<Failure, List<BoardList>> result = boardListUpdateService
                                        .updateBoardListPosition(validPositionCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.NotFound.class);
                        Failure.NotFound notFound = (Failure.NotFound) failure;
                        assertThat(notFound.getErrorCode()).isEqualTo("NOT_FOUND");
                }

                @Test
                @DisplayName("권한이 없을 때 Forbidden을 반환해야 한다")
                void updateBoardListPosition_withUnauthorizedAccess_shouldReturnForbidden() {
                        // given
                        UserId unauthorizedUserId = new UserId("unauthorized-user-456");
                        UpdateBoardListPositionCommand unauthorizedCommand = new UpdateBoardListPositionCommand(
                                        testListId, unauthorizedUserId, 2);

                        ValidationResult<UpdateBoardListPositionCommand> validResult = ValidationResult
                                        .valid(unauthorizedCommand);
                        when(boardListValidator.validateUpdateBoardListPosition(unauthorizedCommand))
                                        .thenReturn(validResult);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));

                        // when
                        Either<Failure, List<BoardList>> result = boardListUpdateService
                                        .updateBoardListPosition(unauthorizedCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.PermissionDenied.class);
                        Failure.PermissionDenied permissionDenied = (Failure.PermissionDenied) failure;
                        assertThat(permissionDenied.getErrorCode()).isEqualTo("UNAUTHORIZED_ACCESS");
                }

                @Test
                @DisplayName("이동 정책 위반 시 BusinessRuleViolation을 반환해야 한다")
                void updateBoardListPosition_withPolicyViolation_shouldReturnBusinessRuleViolation() {
                        // given
                        List<BoardList> allLists = List.of(
                                        createBoardList("리스트1", 0),
                                        createBoardList("리스트2", 1),
                                        createBoardList("리스트3", 2));

                        ValidationResult<UpdateBoardListPositionCommand> validResult = ValidationResult
                                        .valid(validPositionCommand);
                        when(boardListValidator.validateUpdateBoardListPosition(validPositionCommand))
                                        .thenReturn(validResult);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(allLists.get(1)));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardListRepository.findByBoardIdOrderByPosition(testBoardId)).thenReturn(allLists);
                        when(boardListMovePolicy.canMoveWithinSameBoard(any(), eq(2)))
                                        .thenReturn(Either.left(Failure.ofBusinessRuleViolation("이동할 수 없는 위치입니다")));

                        // when
                        Either<Failure, List<BoardList>> result = boardListUpdateService
                                        .updateBoardListPosition(validPositionCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.BusinessRuleViolation.class);
                        Failure.BusinessRuleViolation businessRuleViolation = (Failure.BusinessRuleViolation) failure;
                        assertThat(businessRuleViolation.getErrorCode()).isEqualTo("LIST_MOVE_POLICY_VIOLATION");
                        assertThat(businessRuleViolation.getMessage()).isEqualTo("이동할 수 없는 위치입니다");
                }

                @Test
                @DisplayName("저장 중 예외 발생 시 InternalServerError를 반환해야 한다")
                void updateBoardListPosition_withSaveException_shouldReturnInternalServerError() {
                        // given
                        List<BoardList> allLists = List.of(
                                        createBoardList("리스트1", 0),
                                        createBoardList("리스트2", 1),
                                        createBoardList("리스트3", 2));

                        ValidationResult<UpdateBoardListPositionCommand> validResult = ValidationResult
                                        .valid(validPositionCommand);
                        when(boardListValidator.validateUpdateBoardListPosition(validPositionCommand))
                                        .thenReturn(validResult);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(allLists.get(1)));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardListRepository.findByBoardIdOrderByPosition(testBoardId)).thenReturn(allLists);
                        when(boardListMovePolicy.canMoveWithinSameBoard(any(), eq(2))).thenReturn(Either.right(null));
                        when(boardListMovePolicy.hasPositionChanged(any(), eq(2))).thenReturn(true);
                        when(boardListRepository.saveAll(anyList())).thenThrow(new RuntimeException("저장 실패"));

                        // when
                        Either<Failure, List<BoardList>> result = boardListUpdateService
                                        .updateBoardListPosition(validPositionCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.InternalError.class);
                        Failure.InternalError internalError = (Failure.InternalError) failure;
                        assertThat(internalError.getErrorCode()).isEqualTo("INTERNAL_ERROR");
                        assertThat(internalError.getMessage()).isEqualTo("저장 실패");
                }
        }

        @Nested
        @DisplayName("경계값 테스트")
        class BoundaryValueTest {

                @Test
                @DisplayName("제목 길이가 정확히 최대 길이일 때 성공해야 한다")
                void updateBoardList_withExactMaxTitleLength_shouldSucceed() {
                        // given
                        String exactMaxTitle = "a".repeat(100);
                        UpdateBoardListCommand exactMaxCommand = new UpdateBoardListCommand(
                                        testListId, testUserId, exactMaxTitle, null, null);

                        ValidationResult<UpdateBoardListCommand> validResult = ValidationResult.valid(exactMaxCommand);
                        when(boardListValidator.validateUpdateBoardList(exactMaxCommand)).thenReturn(validResult);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardListPolicyConfig.getMaxTitleLength()).thenReturn(100);
                        when(boardListRepository.save(any(BoardList.class))).thenReturn(testBoardList);

                        // when
                        Either<Failure, BoardList> result = boardListUpdateService.updateBoardList(exactMaxCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                }

                @Test
                @DisplayName("위치 0으로 이동할 때 성공해야 한다")
                void updateBoardListPosition_toPositionZero_shouldSucceed() {
                        // given
                        UpdateBoardListPositionCommand zeroPositionCommand = new UpdateBoardListPositionCommand(
                                        testListId, testUserId, 0);

                        List<BoardList> allLists = List.of(
                                        createBoardList("리스트1", 0),
                                        createBoardList("리스트2", 1),
                                        createBoardList("리스트3", 2));

                        ValidationResult<UpdateBoardListPositionCommand> validResult = ValidationResult
                                        .valid(zeroPositionCommand);
                        when(boardListValidator.validateUpdateBoardListPosition(zeroPositionCommand))
                                        .thenReturn(validResult);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(allLists.get(1)));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardListRepository.findByBoardIdOrderByPosition(testBoardId)).thenReturn(allLists);
                        when(boardListMovePolicy.canMoveWithinSameBoard(any(), eq(0))).thenReturn(Either.right(null));
                        when(boardListMovePolicy.hasPositionChanged(any(), eq(0))).thenReturn(true);
                        when(boardListRepository.saveAll(anyList())).thenReturn(allLists);

                        // when
                        Either<Failure, List<BoardList>> result = boardListUpdateService
                                        .updateBoardListPosition(zeroPositionCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                }

                @Test
                @DisplayName("마지막 위치로 이동할 때 성공해야 한다")
                void updateBoardListPosition_toLastPosition_shouldSucceed() {
                        // given
                        UpdateBoardListPositionCommand lastPositionCommand = new UpdateBoardListPositionCommand(
                                        testListId, testUserId, 2);

                        List<BoardList> allLists = List.of(
                                        createBoardList("리스트1", 0),
                                        createBoardList("리스트2", 1),
                                        createBoardList("리스트3", 2));

                        ValidationResult<UpdateBoardListPositionCommand> validResult = ValidationResult
                                        .valid(lastPositionCommand);
                        when(boardListValidator.validateUpdateBoardListPosition(lastPositionCommand))
                                        .thenReturn(validResult);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(allLists.get(0)));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardListRepository.findByBoardIdOrderByPosition(testBoardId)).thenReturn(allLists);
                        when(boardListMovePolicy.canMoveWithinSameBoard(any(), eq(2))).thenReturn(Either.right(null));
                        when(boardListMovePolicy.hasPositionChanged(any(), eq(2))).thenReturn(true);
                        when(boardListRepository.saveAll(anyList())).thenReturn(allLists);

                        // when
                        Either<Failure, List<BoardList>> result = boardListUpdateService
                                        .updateBoardListPosition(lastPositionCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                }
        }

        // ==================== Helper Methods ====================

        private BoardList createBoardList(String title, int position) {
                return BoardList.builder()
                                .listId(new ListId())
                                .title(title)
                                .description("설명")
                                .position(position)
                                .color(ListColor.defaultColor())
                                .boardId(testBoardId)
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();
        }
}