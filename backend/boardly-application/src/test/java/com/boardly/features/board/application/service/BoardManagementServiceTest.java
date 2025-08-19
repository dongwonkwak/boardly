package com.boardly.features.board.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.board.application.command.CreateBoardCommand;
import com.boardly.features.board.application.command.UpdateBoardCommand;
import com.boardly.features.board.application.validation.BoardValidator;
import com.boardly.features.board.domain.Board;
import com.boardly.features.board.domain.port.BoardRepository;
import com.boardly.features.user.domain.port.UserFinder;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.common.value.WorkspaceId;
import com.boardly.shared.validation.MessageResolver;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BoardManagementServiceTest {

    @Mock
    private BoardValidator boardValidator;
    @Mock
    private BoardRepository boardRepository;
    @Mock
    private BoardPermissionService boardPermissionService;
    @Mock
    private UserFinder userFinder;
    @Mock
    private MessageResolver messageResolver;
    @Mock
    private ActivityHelper activityHelper;

    @InjectMocks
    private BoardManagementService service;

    @BeforeEach
    void setup() {
        // MockitoExtension initializes mocks
    }

    @Test
    void createBoard_success() {
        // given
        var ownerId = new UserId("user-1");
        var workspaceId = new WorkspaceId("workspace-1");
        when(boardValidator.validateCreate(any()))
                .thenReturn(com.boardly.shared.validation.ValidationResult.valid(null));
        when(userFinder.userExists(ownerId)).thenReturn(true);
        Board toSave = Board.create("title", "desc", workspaceId, ownerId);
        when(boardRepository.save(any())).thenReturn(Either.right(toSave));

        // when
        Either<Failure, Board> result = service
                .createBoard(CreateBoardCommand.of("title", "desc", workspaceId, ownerId));

        // then
        assertThat(result.isRight()).isTrue();
        verify(boardRepository).save(any());
        verify(activityHelper).logBoardCreate(eq(ownerId), eq("title"), any());
    }

    @Test
    void updateBoard_notFound() {
        // given
        var boardId = new BoardId("b-1");
        var userId = new UserId("u-1");
        when(userFinder.userExists(userId)).thenReturn(true);
        when(boardValidator.validateUpdate(any()))
                .thenReturn(com.boardly.shared.validation.ValidationResult.valid(null));
        when(boardRepository.findById(boardId)).thenReturn(Optional.empty());
        when(messageResolver.getMessage(anyString())).thenReturn("not found");

        // when
        Either<Failure, Board> result = service.updateBoard(UpdateBoardCommand.of(boardId, "t", "d", userId));

        // then
        assertThat(result.isLeft()).isTrue();
    }

    @Test
    void createBoard_validationFailure() {
        when(boardValidator.validateCreate(any()))
                .thenReturn(com.boardly.shared.validation.ValidationResult.invalid("title", "invalid", null));

        Either<Failure, Board> result = service
                .createBoard(CreateBoardCommand.of(null, null, new WorkspaceId("w-1"), new UserId("u")));

        assertThat(result.isLeft()).isTrue();
    }

    @Test
    void createBoard_userNotFound() {
        var ownerId = new UserId("u-1");
        var workspaceId = new WorkspaceId("w-1");
        when(boardValidator.validateCreate(any()))
                .thenReturn(com.boardly.shared.validation.ValidationResult.valid(null));
        when(userFinder.userExists(ownerId)).thenReturn(false);

        Either<Failure, Board> result = service.createBoard(CreateBoardCommand.of("t", null, workspaceId, ownerId));

        assertThat(result.isLeft()).isTrue();
    }

    @Test
    void updateBoard_permissionDenied() {
        var boardId = new BoardId("b-1");
        var userId = new UserId("u-1");
        var workspaceId = new WorkspaceId("w-1");
        when(userFinder.userExists(userId)).thenReturn(true);
        when(boardValidator.validateUpdate(any()))
                .thenReturn(com.boardly.shared.validation.ValidationResult.valid(null));
        var existing = Board.create("t", "d", workspaceId, userId);
        // ensure same id
        existing = Board.builder()
                .boardId(boardId)
                .title("t")
                .description("d")
                .workspaceId(workspaceId)
                .ownerId(userId)
                .isStarred(false)
                .isArchived(false)
                .createdAt(existing.getCreatedAt())
                .updatedAt(existing.getUpdatedAt())
                .build();
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(existing));
        when(boardPermissionService.canWriteBoard(eq(boardId), eq(userId))).thenReturn(Either.right(false));
        when(messageResolver.getMessage(anyString())).thenReturn("denied");

        Either<Failure, Board> result = service.updateBoard(UpdateBoardCommand.of(boardId, "t2", null, userId));
        assertThat(result.isLeft()).isTrue();
    }
}
