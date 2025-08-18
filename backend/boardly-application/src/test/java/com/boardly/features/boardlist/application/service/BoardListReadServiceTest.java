package com.boardly.features.boardlist.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.boardly.features.board.domain.Board;
import com.boardly.features.board.domain.port.BoardRepository;
import com.boardly.features.boardlist.application.query.GetBoardListsQuery;
import com.boardly.features.boardlist.application.validation.BoardListValidator;
import com.boardly.features.boardlist.domain.BoardList;
import com.boardly.features.boardlist.domain.port.BoardListRepository;
import com.boardly.features.boardlist.application.policy.BoardListCreationPolicy;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.ListColor;
import com.boardly.shared.common.value.ListId;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.validation.MessageResolver;
import com.boardly.shared.validation.ValidationResult;
import io.vavr.control.Either;
import java.time.Instant;
import java.util.List;
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
@DisplayName("BoardListReadService 테스트")
class BoardListReadServiceTest {

    @Mock private BoardListValidator boardListValidator;
    @Mock private BoardRepository boardRepository;
    @Mock private BoardListRepository boardListRepository;
    @Mock private BoardListCreationPolicy boardListCreationPolicy;
    @Mock private MessageResolver messageResolver;

    @InjectMocks private BoardListReadService boardListReadService;

    private UserId testUserId;
    private BoardId testBoardId;
    private Board testBoard;
    private GetBoardListsQuery validQuery;
    private List<BoardList> testBoardLists;

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

        validQuery = new GetBoardListsQuery(testBoardId, testUserId);

        testBoardLists = List.of(
            BoardList.builder()
                .listId(new ListId("list-1"))
                .boardId(testBoardId)
                .title("할 일")
                .description("해야 할 일들")
                .color(ListColor.defaultColor())
                .position(1)
                .createdAt(now)
                .updatedAt(now)
                .build(),
            BoardList.builder()
                .listId(new ListId("list-2"))
                .boardId(testBoardId)
                .title("진행 중")
                .description("진행 중인 작업들")
                .color(ListColor.defaultColor())
                .position(2)
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    @Nested
    @DisplayName("getBoardLists 메서드 테스트")
    class GetBoardListsTest {
        @Test
        @DisplayName("유효한 데이터로 보드 리스트 조회가 성공해야 한다")
        void getBoardLists_withValidData_shouldReturnBoardLists() {
            ValidationResult<GetBoardListsQuery> validResult = ValidationResult.valid(validQuery);
            when(boardListValidator.validateGetBoardLists(validQuery)).thenReturn(validResult);
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
            when(boardListRepository.findByBoardIdOrderByPosition(testBoardId)).thenReturn(testBoardLists);
            when(boardListCreationPolicy.getStatus(testBoardId)).thenReturn(
                com.boardly.features.boardlist.application.policy.BoardListCreationPolicy.ListCountStatus.NORMAL);

            Either<Failure, List<BoardList>> result = boardListReadService.getBoardLists(validQuery);

            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(testBoardLists);
            verify(boardListValidator).validateGetBoardLists(validQuery);
            verify(boardRepository).findById(testBoardId);
            verify(boardListRepository).findByBoardIdOrderByPosition(testBoardId);
            verify(boardListCreationPolicy).getStatus(testBoardId);
        }

        @Test
        @DisplayName("보드가 존재하지 않을 때 NotFound를 반환해야 한다")
        void getBoardLists_withNonExistentBoard_shouldReturnNotFound() {
            ValidationResult<GetBoardListsQuery> validResult = ValidationResult.valid(validQuery);
            when(boardListValidator.validateGetBoardLists(validQuery)).thenReturn(validResult);
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.empty());
            when(messageResolver.getMessage("validation.board.not.found")).thenReturn("보드를 찾을 수 없습니다");

            Either<Failure, List<BoardList>> result = boardListReadService.getBoardLists(validQuery);

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
            verify(boardListValidator).validateGetBoardLists(validQuery);
            verify(boardRepository).findById(testBoardId);
            verify(messageResolver).getMessage("validation.board.not.found");
            verifyNoInteractions(boardListRepository, boardListCreationPolicy);
        }

        @Test
        @DisplayName("권한이 없을 때 PermissionDenied를 반환해야 한다")
        void getBoardLists_withUnauthorizedUser_shouldReturnPermissionDenied() {
            UserId otherUser = new UserId("other-user");
            GetBoardListsQuery unauthorized = new GetBoardListsQuery(testBoardId, otherUser);
            when(boardListValidator.validateGetBoardLists(unauthorized)).thenReturn(ValidationResult.valid(unauthorized));
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
            when(messageResolver.getMessage("validation.board.modification.access.denied")).thenReturn("보드에 접근할 권한이 없습니다");

            Either<Failure, List<BoardList>> result = boardListReadService.getBoardLists(unauthorized);

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
        }

        @Test
        @DisplayName("리스트 조회 중 예외 발생 시 InternalError를 반환해야 한다")
        void getBoardLists_withRepositoryException_shouldReturnInternalError() {
            when(boardListValidator.validateGetBoardLists(validQuery)).thenReturn(ValidationResult.valid(validQuery));
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
            when(boardListRepository.findByBoardIdOrderByPosition(testBoardId)).thenThrow(new RuntimeException("DB DOWN"));

            Either<Failure, List<BoardList>> result = boardListReadService.getBoardLists(validQuery);

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
        }
    }
}
