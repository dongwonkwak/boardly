package com.boardly.features.boardlist.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.activity.domain.ActivityType;
import com.boardly.features.board.application.service.BoardPermissionService;
import com.boardly.features.board.domain.Board;
import com.boardly.features.board.domain.port.BoardRepository;
import com.boardly.features.boardlist.application.command.DeleteBoardListCommand;
import com.boardly.features.boardlist.application.validation.BoardListValidator;
import com.boardly.features.boardlist.domain.BoardList;
import com.boardly.features.boardlist.domain.port.BoardListRepository;
import com.boardly.features.card.domain.port.CardRepository;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.ListId;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.validation.MessageResolver;
import com.boardly.shared.validation.ValidationResult;
import io.vavr.control.Either;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardListDeleteService 테스트")
class BoardListDeleteServiceTest {

    @Mock private BoardListValidator boardListValidator;
    @Mock private BoardRepository boardRepository;
    @Mock private BoardListRepository boardListRepository;
    @Mock private CardRepository cardRepository;
    @Mock private BoardPermissionService boardPermissionService;
    @Mock private MessageResolver messageResolver;
    @Mock private ActivityHelper activityHelper;

    @InjectMocks private BoardListDeleteService boardListDeleteService;

    private UserId testUserId;
    private BoardId testBoardId;
    private ListId testListId;
    private Board testBoard;
    private BoardList testList;

    @BeforeEach
    void setUp() {
        testUserId = new UserId("user-1");
        testBoardId = new BoardId("board-1");
        testListId = new ListId("list-1");
        Instant now = Instant.now();

        testBoard = Board.builder()
            .boardId(testBoardId)
            .title("보드")
            .description("설명")
            .isArchived(false)
            .ownerId(testUserId)
            .isStarred(false)
            .createdAt(now)
            .updatedAt(now)
            .build();

        testList = BoardList.builder()
            .listId(testListId)
            .title("리스트")
            .description("설명")
            .position(1)
            .color(com.boardly.shared.common.value.ListColor.defaultColor())
            .boardId(testBoardId)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    @Test
    @DisplayName("성공적으로 리스트를 삭제해야 한다")
    void deleteBoardList_success() {
        var command = new DeleteBoardListCommand(testListId, testUserId);

        when(boardListValidator.validateDeleteBoardList(command)).thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testList));
        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
        when(boardPermissionService.canWriteBoard(testBoardId, testUserId)).thenReturn(Either.right(true));
        when(cardRepository.deleteByListId(testListId)).thenReturn(Either.right(null));

        Either<Failure, Void> result = boardListDeleteService.deleteBoardList(command);

        assertThat(result.isRight()).isTrue();
        verify(boardListRepository).deleteById(testListId);
        verify(activityHelper).logListActivity(
            eq(ActivityType.LIST_DELETE),
            eq(testUserId),
            anyMap(),
            eq(testBoard.getTitle()),
            eq(testBoardId),
            eq(testListId)
        );
    }

    @Test
    @DisplayName("리스트가 없으면 NotFound")
    void deleteBoardList_notFoundList() {
        var command = new DeleteBoardListCommand(testListId, testUserId);
        when(boardListValidator.validateDeleteBoardList(command)).thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(testListId)).thenReturn(Optional.empty());

        Either<Failure, Void> result = boardListDeleteService.deleteBoardList(command);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
    }

    @Test
    @DisplayName("권한이 없으면 Forbidden/PermissionDenied")
    void deleteBoardList_forbidden() {
        var command = new DeleteBoardListCommand(testListId, new UserId("other"));
        when(boardListValidator.validateDeleteBoardList(command)).thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testList));
        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
        when(boardPermissionService.canWriteBoard(testBoardId, command.userId())).thenReturn(Either.right(false));

        Either<Failure, Void> result = boardListDeleteService.deleteBoardList(command);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
    }

    @Test
    @DisplayName("카드 삭제 실패 시 동일 실패 전파")
    void deleteBoardList_cardDeleteFailure() {
        var command = new DeleteBoardListCommand(testListId, testUserId);
        when(boardListValidator.validateDeleteBoardList(command)).thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testList));
        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
        when(boardPermissionService.canWriteBoard(testBoardId, testUserId)).thenReturn(Either.right(true));
        when(cardRepository.deleteByListId(testListId)).thenReturn(Either.left(Failure.ofInternalError("card", "CARD_DELETE", null)));

        Either<Failure, Void> result = boardListDeleteService.deleteBoardList(command);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
        verify(boardListRepository, never()).deleteById(any());
    }
}
