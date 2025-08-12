package com.boardly.features.board.application.validation;

import com.boardly.features.board.application.command.AddBoardMemberCommand;
import com.boardly.features.board.application.command.ArchiveBoardCommand;
import com.boardly.features.board.application.command.CreateBoardCommand;
import com.boardly.features.board.application.command.DeleteBoardCommand;
import com.boardly.features.board.application.command.ToggleStarBoardCommand;
import com.boardly.features.board.application.command.UpdateBoardCommand;
import com.boardly.features.board.application.command.UpdateBoardMemberRoleCommand;
import com.boardly.features.board.application.query.GetBoardDetailQuery;
import com.boardly.infrastructure.validation.CommonValidationRules;
import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.common.value.BoardRole;
import com.boardly.shared.validation.MessageResolver;
import com.boardly.shared.validation.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BoardValidatorTest {

    @Mock private CommonValidationRules commonValidationRules;
    @Mock private MessageResolver messageResolver;

    private BoardValidator validator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        validator = new BoardValidator(commonValidationRules);

        // 기본적으로 모든 Validator 조합이 valid를 반환하도록 설정
        when(commonValidationRules.titleComplete(any())).thenReturn(com.boardly.shared.validation.Validator.of(x -> true, "t", ""));
        when(commonValidationRules.descriptionComplete(any())).thenReturn(com.boardly.shared.validation.Validator.of(x -> true, "d", ""));
        when(commonValidationRules.userIdRequired(any())).thenReturn(com.boardly.shared.validation.Validator.of(x -> true, "u", ""));
        when(commonValidationRules.boardIdRequired(any())).thenReturn(com.boardly.shared.validation.Validator.of(x -> true, "b", ""));
        when(commonValidationRules.boardMemberRoleRequired(any())).thenReturn(com.boardly.shared.validation.Validator.of(x -> true, "r", ""));
        when(commonValidationRules.titleOptional(any())).thenReturn(com.boardly.shared.validation.Validator.of(x -> true, "to", ""));
    }

    @Test
    void validateCreate_valid() {
        ValidationResult<CreateBoardCommand> result = validator.validateCreate(CreateBoardCommand.of("t","d", new UserId("u")));
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void validateUpdate_valid() {
        ValidationResult<UpdateBoardCommand> result = validator.validateUpdate(UpdateBoardCommand.of(new BoardId("b"), "t","d", new UserId("u")));
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void validateUpdate_invalid_missingBoardId() {
        when(commonValidationRules.boardIdRequired(any())).thenReturn(com.boardly.shared.validation.Validator.of(x -> false, "b", "BoardId required"));
        ValidationResult<UpdateBoardCommand> result = validator.validateUpdate(UpdateBoardCommand.of(null, "t","d", new UserId("u")));
        assertThat(result.isInvalid()).isTrue();
    }

    @Test
    void validateDelete_valid() {
        ValidationResult<DeleteBoardCommand> result = validator.validateDelete(new DeleteBoardCommand(new BoardId("b"), new UserId("u")));
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void validateArchive_valid() {
        ValidationResult<ArchiveBoardCommand> result = validator.validateArchive(new ArchiveBoardCommand(new BoardId("b"), new UserId("u")));
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void validateGetDetail_valid() {
        ValidationResult<GetBoardDetailQuery> result = validator.validateGetDetail(new GetBoardDetailQuery(new BoardId("b"), new UserId("u")));
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void validateMemberCommands_valid() {
        assertThat(validator.validateAddMember(new AddBoardMemberCommand(new BoardId("b"), new UserId("m"), BoardRole.MEMBER, new UserId("u"))).isValid()).isTrue();
        assertThat(validator.validateRemoveMember(new com.boardly.features.board.application.command.RemoveBoardMemberCommand(new BoardId("b"), new UserId("m"), new UserId("u"))).isValid()).isTrue();
        assertThat(validator.validateUpdateMemberRole(new UpdateBoardMemberRoleCommand(new BoardId("b"), new UserId("m"), BoardRole.ADMIN, new UserId("u"))).isValid()).isTrue();
    }

    @Test
    void validateToggleStar_valid() {
        ValidationResult<ToggleStarBoardCommand> result = validator.validateToggleStar(new ToggleStarBoardCommand(new BoardId("b"), new UserId("u")));
        assertThat(result.isValid()).isTrue();
    }
}
