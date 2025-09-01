package com.boardly.features.board.application.service;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.board.application.command.AddBoardMemberCommand;
import com.boardly.features.board.application.validation.BoardValidator;
import com.boardly.features.board.domain.Board;
import com.boardly.features.board.domain.BoardMember;
import com.boardly.features.board.domain.port.BoardMemberRepository;
import com.boardly.features.board.domain.port.BoardRepository;
import com.boardly.features.user.domain.port.UserFinder;
import com.boardly.features.user.domain.port.UserFinder.UserNameDto;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BoardMemberServiceTest {

    @Mock private BoardRepository boardRepository;
    @Mock private BoardMemberRepository boardMemberRepository;
    @Mock private BoardPermissionService boardPermissionService;
    @Mock private MessageResolver messageResolver;
    @Mock private UserFinder userFinder;
    @Mock private BoardValidator boardValidator;
    @Mock private ActivityHelper activityHelper;

    @InjectMocks private BoardMemberService service;

    @BeforeEach
    void setup() {
        // MockitoExtension initializes mocks
    }

    @Test
    void addBoardMember_success() {
        var boardId = new BoardId("b-1");
        var userId = new UserId("u-1");
        var requestedBy = new UserId("u-2");

        when(boardValidator.validateAddMember(any())).thenReturn(com.boardly.shared.validation.ValidationResult.valid(null));
        when(userFinder.userExists(requestedBy)).thenReturn(true);
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(Board.create("t","d", requestedBy)));
        when(boardPermissionService.canManageBoardMembers(any(BoardId.class), eq(requestedBy))).thenReturn(Either.right(true));
        when(boardMemberRepository.existsByBoardIdAndUserId(eq(boardId), eq(userId))).thenReturn(false);
        when(boardMemberRepository.save(any())).thenAnswer(inv -> Either.right((BoardMember) inv.getArgument(0)));
        when(userFinder.findUserNameById(userId)).thenReturn(Optional.of(new UserNameDto("first","last")));
        when(boardRepository.findBoardNameById(boardId)).thenReturn(Optional.of("Board Name"));

        Either<Failure, BoardMember> result = service.addBoardMember(new AddBoardMemberCommand(boardId, userId, com.boardly.shared.common.value.BoardRole.MEMBER, requestedBy));

        assertThat(result.isRight()).isTrue();
        verify(activityHelper).logBoardActivity(eq(com.boardly.features.activity.domain.ActivityType.BOARD_ADD_MEMBER), eq(requestedBy), anyMap(), eq("Board Name"), eq(boardId));
    }

    @Test
    void addBoardMember_validationFailure() {
        when(boardValidator.validateAddMember(any())).thenReturn(com.boardly.shared.validation.ValidationResult.invalid("userId", "required", null));

        var result = service.addBoardMember(new AddBoardMemberCommand(new BoardId("b"), new UserId("u"), com.boardly.shared.common.value.BoardRole.MEMBER, new UserId("rb")));
        assertThat(result.isLeft()).isTrue();
    }

    @Test
    void addBoardMember_permissionDenied() {
        var boardId = new BoardId("b-1");
        var userId = new UserId("u-1");
        var requestedBy = new UserId("rb");
        when(boardValidator.validateAddMember(any())).thenReturn(com.boardly.shared.validation.ValidationResult.valid(null));
        when(userFinder.userExists(requestedBy)).thenReturn(true);
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(Board.create("t","d", requestedBy)));
        when(boardPermissionService.canManageBoardMembers(any(BoardId.class), eq(requestedBy))).thenReturn(Either.right(false));
        when(messageResolver.getMessage(anyString())).thenReturn("denied");

        var result = service.addBoardMember(new AddBoardMemberCommand(boardId, userId, com.boardly.shared.common.value.BoardRole.MEMBER, requestedBy));
        assertThat(result.isLeft()).isTrue();
    }
}
