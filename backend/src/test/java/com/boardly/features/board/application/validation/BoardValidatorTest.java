package com.boardly.features.board.application.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boardly.features.board.application.port.input.AddBoardMemberCommand;
import com.boardly.features.board.application.port.input.ArchiveBoardCommand;
import com.boardly.features.board.application.port.input.CreateBoardCommand;
import com.boardly.features.board.application.port.input.DeleteBoardCommand;
import com.boardly.features.board.application.port.input.RemoveBoardMemberCommand;
import com.boardly.features.board.application.port.input.ToggleStarBoardCommand;
import com.boardly.features.board.application.port.input.UpdateBoardCommand;
import com.boardly.features.board.application.port.input.UpdateBoardMemberRoleCommand;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.model.BoardRole;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardValidator 테스트")
class BoardValidatorTest {

    @Mock
    private CommonValidationRules commonValidationRules;

    private BoardValidator boardValidator;

    @BeforeEach
    void setUp() {
        boardValidator = new BoardValidator(commonValidationRules);
    }

    @Test
    @DisplayName("보드 생성 검증 - 성공 케이스")
    void shouldValidateCreateBoardSuccessfully() {
        // given
        CreateBoardCommand command = new CreateBoardCommand("테스트 보드", "테스트 설명", new UserId("user-1"));

        Validator<Object> mockValidator = cmd -> ValidationResult.valid(cmd);
        when(commonValidationRules.titleComplete(any())).thenReturn(mockValidator);
        when(commonValidationRules.descriptionComplete(any())).thenReturn(mockValidator);
        when(commonValidationRules.userIdRequired(any())).thenReturn(mockValidator);

        // when
        ValidationResult<CreateBoardCommand> result = boardValidator.validateCreate(command);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(command);
    }

    @Test
    @DisplayName("보드 수정 검증 - 성공 케이스")
    void shouldValidateUpdateBoardSuccessfully() {
        // given
        UpdateBoardCommand command = new UpdateBoardCommand(
                new BoardId("board-1"), "수정된 제목", "수정된 설명", new UserId("user-1"));

        Validator<Object> mockValidator = cmd -> ValidationResult.valid(cmd);
        when(commonValidationRules.boardIdRequired(any())).thenReturn(mockValidator);
        when(commonValidationRules.userIdRequired(any())).thenReturn(mockValidator);
        when(commonValidationRules.titleOptional(any())).thenReturn(mockValidator);
        when(commonValidationRules.descriptionComplete(any())).thenReturn(mockValidator);

        // when
        ValidationResult<UpdateBoardCommand> result = boardValidator.validateUpdate(command);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(command);
    }

    @Test
    @DisplayName("보드 삭제 검증 - 성공 케이스")
    void shouldValidateDeleteBoardSuccessfully() {
        // given
        DeleteBoardCommand command = new DeleteBoardCommand(new BoardId("board-1"), new UserId("user-1"));

        Validator<Object> mockValidator = cmd -> ValidationResult.valid(cmd);
        when(commonValidationRules.boardIdRequired(any())).thenReturn(mockValidator);
        when(commonValidationRules.userIdRequired(any())).thenReturn(mockValidator);

        // when
        ValidationResult<DeleteBoardCommand> result = boardValidator.validateDelete(command);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(command);
    }

    @Test
    @DisplayName("보드 아카이브 검증 - 성공 케이스")
    void shouldValidateArchiveBoardSuccessfully() {
        // given
        ArchiveBoardCommand command = new ArchiveBoardCommand(new BoardId("board-1"), new UserId("user-1"));

        Validator<Object> mockValidator = cmd -> ValidationResult.valid(cmd);
        when(commonValidationRules.boardIdRequired(any())).thenReturn(mockValidator);
        when(commonValidationRules.userIdRequired(any())).thenReturn(mockValidator);

        // when
        ValidationResult<ArchiveBoardCommand> result = boardValidator.validateArchive(command);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(command);
    }

    @Test
    @DisplayName("보드 즐겨찾기 토글 검증 - 성공 케이스")
    void shouldValidateToggleStarBoardSuccessfully() {
        // given
        ToggleStarBoardCommand command = new ToggleStarBoardCommand(new BoardId("board-1"), new UserId("user-1"));

        Validator<Object> mockValidator = cmd -> ValidationResult.valid(cmd);
        when(commonValidationRules.boardIdRequired(any())).thenReturn(mockValidator);
        when(commonValidationRules.userIdRequired(any())).thenReturn(mockValidator);

        // when
        ValidationResult<ToggleStarBoardCommand> result = boardValidator.validateToggleStar(command);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(command);
    }

    @Test
    @DisplayName("보드 멤버 추가 검증 - 성공 케이스")
    void shouldValidateAddBoardMemberSuccessfully() {
        // given
        AddBoardMemberCommand command = new AddBoardMemberCommand(
                new BoardId("board-1"), new UserId("user-1"), BoardRole.EDITOR, new UserId("admin-1"));

        Validator<Object> mockValidator = cmd -> ValidationResult.valid(cmd);
        when(commonValidationRules.boardIdRequired(any())).thenReturn(mockValidator);
        when(commonValidationRules.userIdRequired(any())).thenReturn(mockValidator);
        when(commonValidationRules.boardMemberRoleRequired(any())).thenReturn(mockValidator);

        // when
        ValidationResult<AddBoardMemberCommand> result = boardValidator.validateAddMember(command);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(command);
    }

    @Test
    @DisplayName("보드 멤버 제거 검증 - 성공 케이스")
    void shouldValidateRemoveBoardMemberSuccessfully() {
        // given
        RemoveBoardMemberCommand command = new RemoveBoardMemberCommand(
                new BoardId("board-1"), new UserId("user-1"), new UserId("admin-1"));

        Validator<Object> mockValidator = cmd -> ValidationResult.valid(cmd);
        when(commonValidationRules.boardIdRequired(any())).thenReturn(mockValidator);
        when(commonValidationRules.userIdRequired(any())).thenReturn(mockValidator);

        // when
        ValidationResult<RemoveBoardMemberCommand> result = boardValidator.validateRemoveMember(command);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(command);
    }

    @Test
    @DisplayName("보드 멤버 역할 변경 검증 - 성공 케이스")
    void shouldValidateUpdateBoardMemberRoleSuccessfully() {
        // given
        UpdateBoardMemberRoleCommand command = new UpdateBoardMemberRoleCommand(
                new BoardId("board-1"), new UserId("user-1"), BoardRole.ADMIN, new UserId("admin-1"));

        Validator<Object> mockValidator = cmd -> ValidationResult.valid(cmd);
        when(commonValidationRules.boardIdRequired(any())).thenReturn(mockValidator);
        when(commonValidationRules.userIdRequired(any())).thenReturn(mockValidator);
        when(commonValidationRules.boardMemberRoleRequired(any())).thenReturn(mockValidator);

        // when
        ValidationResult<UpdateBoardMemberRoleCommand> result = boardValidator.validateUpdateMemberRole(command);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(command);
    }

    @Test
    @DisplayName("모든 검증 메서드가 존재하는지 확인")
    void shouldHaveAllValidationMethods() {
        // given
        CreateBoardCommand createCommand = new CreateBoardCommand("제목", "설명", new UserId("user-1"));
        UpdateBoardCommand updateCommand = new UpdateBoardCommand(new BoardId("board-1"), "제목", "설명",
                new UserId("user-1"));
        DeleteBoardCommand deleteCommand = new DeleteBoardCommand(new BoardId("board-1"), new UserId("user-1"));
        ArchiveBoardCommand archiveCommand = new ArchiveBoardCommand(new BoardId("board-1"), new UserId("user-1"));
        ToggleStarBoardCommand toggleCommand = new ToggleStarBoardCommand(new BoardId("board-1"), new UserId("user-1"));
        AddBoardMemberCommand addMemberCommand = new AddBoardMemberCommand(new BoardId("board-1"), new UserId("user-1"),
                BoardRole.EDITOR, new UserId("admin-1"));
        RemoveBoardMemberCommand removeMemberCommand = new RemoveBoardMemberCommand(new BoardId("board-1"),
                new UserId("user-1"), new UserId("admin-1"));
        UpdateBoardMemberRoleCommand updateRoleCommand = new UpdateBoardMemberRoleCommand(new BoardId("board-1"),
                new UserId("user-1"), BoardRole.ADMIN, new UserId("admin-1"));

        Validator<Object> mockValidator = cmd -> ValidationResult.valid(cmd);
        when(commonValidationRules.titleComplete(any())).thenReturn(mockValidator);
        when(commonValidationRules.descriptionComplete(any())).thenReturn(mockValidator);
        when(commonValidationRules.userIdRequired(any())).thenReturn(mockValidator);
        when(commonValidationRules.boardIdRequired(any())).thenReturn(mockValidator);
        when(commonValidationRules.titleOptional(any())).thenReturn(mockValidator);
        when(commonValidationRules.boardMemberRoleRequired(any())).thenReturn(mockValidator);

        // when & then
        assertThat(boardValidator.validateCreate(createCommand)).isNotNull();
        assertThat(boardValidator.validateUpdate(updateCommand)).isNotNull();
        assertThat(boardValidator.validateDelete(deleteCommand)).isNotNull();
        assertThat(boardValidator.validateArchive(archiveCommand)).isNotNull();
        assertThat(boardValidator.validateToggleStar(toggleCommand)).isNotNull();
        assertThat(boardValidator.validateAddMember(addMemberCommand)).isNotNull();
        assertThat(boardValidator.validateRemoveMember(removeMemberCommand)).isNotNull();
        assertThat(boardValidator.validateUpdateMemberRole(updateRoleCommand)).isNotNull();
    }

    @Test
    @DisplayName("BoardValidator 인스턴스가 정상적으로 생성되는지 확인")
    void shouldCreateBoardValidatorInstance() {
        // when & then
        assertThat(boardValidator).isNotNull();
        assertThat(boardValidator).isInstanceOf(BoardValidator.class);
    }
}