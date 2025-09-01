package com.boardly.features.board.application.service;

import com.boardly.features.board.application.command.ToggleStarBoardCommand;
import com.boardly.features.board.application.validation.BoardValidator;
import com.boardly.features.board.domain.Board;
import com.boardly.features.board.domain.port.BoardRepository;
import com.boardly.features.user.domain.port.UserFinder;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.validation.MessageResolver;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BoardInteractionServiceTest {

    @Mock private BoardValidator boardValidator;
    @Mock private BoardRepository boardRepository;
    @Mock private MessageResolver messageResolver;
    @Mock private UserFinder userFinder;

    @InjectMocks private BoardInteractionService service;

    @BeforeEach
    void setup() { /* MockitoExtension initializes mocks */ }

    @Test
    void starringBoard_success_changesFlag() {
        var boardId = new BoardId("b-1");
        var userId = new UserId("u-1");
        Board board = Board.create("t","d", userId);
        when(userFinder.userExists(userId)).thenReturn(true);
        when(boardValidator.validateToggleStar(any())).thenReturn(com.boardly.shared.validation.ValidationResult.valid(null));
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(boardRepository.save(any())).thenAnswer(inv -> Either.right((Board) inv.getArgument(0)));

        Either<Failure, Board> result = service.starringBoard(ToggleStarBoardCommand.of(boardId, userId));

        assertThat(result.isRight()).isTrue();
        assertThat(result.get().isStarred()).isTrue();
    }

    @Test
    void starringBoard_userNotFound() {
        var boardId = new BoardId("b");
        var userId = new UserId("u");
        when(userFinder.userExists(userId)).thenReturn(false);
        when(messageResolver.getMessage(anyString())).thenReturn("user not found");

        var res = service.starringBoard(ToggleStarBoardCommand.of(boardId, userId));
        assertThat(res.isLeft()).isTrue();
    }

    @Test
    void starringBoard_boardNotFound() {
        var boardId = new BoardId("b");
        var userId = new UserId("u");
        when(userFinder.userExists(userId)).thenReturn(true);
        when(boardValidator.validateToggleStar(any())).thenReturn(com.boardly.shared.validation.ValidationResult.valid(null));
        when(boardRepository.findById(boardId)).thenReturn(Optional.empty());
        when(messageResolver.getMessage(anyString())).thenReturn("board not found");

        var res = service.starringBoard(ToggleStarBoardCommand.of(boardId, userId));
        assertThat(res.isLeft()).isTrue();
    }
}
