package com.boardly.features.board.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.boardly.features.attachment.domain.port.AttachmentRepository;
import com.boardly.features.board.application.dto.BoardDetailDto;
import com.boardly.features.board.application.query.GetBoardDetailQuery;
import com.boardly.features.board.application.query.GetUserBoardsQuery;
import com.boardly.features.board.application.validation.BoardValidator;
import com.boardly.features.board.domain.Board;
import com.boardly.features.board.domain.port.BoardRepository;
import com.boardly.features.board.domain.port.GetBoardDetailPort;
import com.boardly.features.board.domain.port.GetBoardDetailPort.BoardDetailData;
import com.boardly.features.comment.domain.port.CommentRepository;
import com.boardly.features.user.domain.port.UserFinder;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.validation.MessageResolver;
import io.vavr.control.Either;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class BoardQueryServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private UserFinder userFinder;

    @Mock
    private GetBoardDetailPort getBoardDetailPort;

    @Mock
    private MessageResolver validationMessageResolver;

    @Mock
    private BoardValidator boardValidator;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private AttachmentRepository attachmentRepository;

    @InjectMocks
    private BoardQueryService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUserBoards_returnsBoardsSorted() {
        var ownerId = new UserId("u-1");
        when(userFinder.userExists(ownerId)).thenReturn(true);
        when(boardRepository.findActiveByOwnerId(ownerId)).thenReturn(
            List.of(Board.create("b", "d", ownerId))
        );

        Either<Failure, List<Board>> result = service.getUserBoards(
            GetUserBoardsQuery.activeOnly(ownerId)
        );
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).hasSize(1);
    }

    @Test
    void getUserBoards_userNotFound() {
        var ownerId = new UserId("u");
        when(userFinder.userExists(ownerId)).thenReturn(false);
        when(validationMessageResolver.getMessage(anyString())).thenReturn(
            "user not found"
        );

        var res = service.getUserBoards(GetUserBoardsQuery.activeOnly(ownerId));
        assertThat(res.isLeft()).isTrue();
    }

    @Test
    void getBoardDetail_success() {
        var boardId = new BoardId("b-1");
        var userId = new UserId("u-1");
        when(boardValidator.validateGetDetail(any())).thenReturn(
            com.boardly.shared.validation.ValidationResult.valid(null)
        );
        Board board = Board.create("t", "d", userId);
        BoardDetailData data = new BoardDetailData(
            board,
            List.of(),
            List.of(),
            List.of(),
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of()
        );
        when(
            getBoardDetailPort.getBoardDetail(eq(boardId), eq(userId))
        ).thenReturn(Either.right(data));
        when(commentRepository.countByCardId(any())).thenReturn(0);
        when(attachmentRepository.countByCardId(any())).thenReturn(0);

        Either<Failure, BoardDetailDto> result = service.getBoardDetail(
            new GetBoardDetailQuery(boardId, userId)
        );
        assertThat(result.isRight()).isTrue();
    }

    @Test
    void getBoardDetail_validationFailure() {
        var boardId = new BoardId("b");
        var userId = new UserId("u");
        when(boardValidator.validateGetDetail(any())).thenReturn(
            com.boardly.shared.validation.ValidationResult.invalid(
                "boardId",
                "required",
                null
            )
        );
        when(validationMessageResolver.getMessage(anyString())).thenReturn(
            "invalid"
        );

        var res = service.getBoardDetail(
            new GetBoardDetailQuery(boardId, userId)
        );
        assertThat(res.isLeft()).isTrue();
    }
}
