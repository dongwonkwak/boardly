package com.boardly.features.boardlist.application.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.boardlist.application.port.input.CreateBoardListCommand;
import com.boardly.features.boardlist.application.port.input.DeleteBoardListCommand;
import com.boardly.features.boardlist.application.port.input.GetBoardListsCommand;
import com.boardly.features.boardlist.application.port.input.UpdateBoardListCommand;
import com.boardly.features.boardlist.application.port.input.UpdateBoardListPositionCommand;
import com.boardly.features.boardlist.domain.model.ListColor;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardListValidator 테스트")
class BoardListValidatorTest {

    @Mock
    private MessageSource messageSource;

    private ValidationMessageResolver messageResolver;
    private CommonValidationRules commonValidationRules;
    private BoardListValidator validator;

    @BeforeEach
    void setUp() {
        messageResolver = new ValidationMessageResolver(messageSource);
        commonValidationRules = new CommonValidationRules(messageResolver);
        validator = new BoardListValidator(commonValidationRules, messageResolver);
    }

    @Test
    @DisplayName("BoardListValidator가 올바르게 생성되어야 한다")
    void shouldCreateBoardListValidator() {
        assertThat(validator).isNotNull();
    }

    @Test
    @DisplayName("validateCreateBoardList 메서드가 존재해야 한다")
    void shouldHaveValidateCreateBoardListMethod() {
        // given
        CreateBoardListCommand command = new CreateBoardListCommand(
                new BoardId("board-1"),
                new UserId("user-1"),
                "테스트 리스트");

        // when & then
        assertThat(validator.validateCreateBoardList(command)).isNotNull();
    }

    @Test
    @DisplayName("validate 메서드가 존재해야 한다")
    void shouldHaveValidateMethod() {
        // given
        CreateBoardListCommand command = new CreateBoardListCommand(
                new BoardId("board-1"),
                new UserId("user-1"),
                "테스트 리스트");

        // when & then
        assertThat(validator.validate(command)).isNotNull();
    }

    @Test
    @DisplayName("validateDeleteBoardList 메서드가 존재해야 한다")
    void shouldHaveValidateDeleteBoardListMethod() {
        // given
        DeleteBoardListCommand command = new DeleteBoardListCommand(
                new ListId("list-1"),
                new UserId("user-1"));

        // when & then
        assertThat(validator.validateDeleteBoardList(command)).isNotNull();
    }

    @Test
    @DisplayName("validateGetBoardLists 메서드가 존재해야 한다")
    void shouldHaveValidateGetBoardListsMethod() {
        // given
        GetBoardListsCommand command = new GetBoardListsCommand(
                new BoardId("board-1"),
                new UserId("user-1"));

        // when & then
        assertThat(validator.validateGetBoardLists(command)).isNotNull();
    }

    @Test
    @DisplayName("validateUpdateBoardList 메서드가 존재해야 한다")
    void shouldHaveValidateUpdateBoardListMethod() {
        // given
        UpdateBoardListCommand command = new UpdateBoardListCommand(
                new ListId("list-1"),
                new UserId("user-1"),
                "수정된 리스트",
                "수정된 설명",
                ListColor.of("#D29034"));

        // when & then
        assertThat(validator.validateUpdateBoardList(command)).isNotNull();
    }

    @Test
    @DisplayName("validateUpdateBoardListPosition 메서드가 존재해야 한다")
    void shouldHaveValidateUpdateBoardListPositionMethod() {
        // given
        UpdateBoardListPositionCommand command = new UpdateBoardListPositionCommand(
                new ListId("list-1"),
                new UserId("user-1"),
                5);

        // when & then
        assertThat(validator.validateUpdateBoardListPosition(command)).isNotNull();
    }

    @Test
    @DisplayName("모든 검증 메서드가 ValidationResult를 반환해야 한다")
    void shouldReturnValidationResultForAllMethods() {
        // given
        CreateBoardListCommand createCommand = new CreateBoardListCommand(
                new BoardId("board-1"),
                new UserId("user-1"),
                "테스트 리스트");

        DeleteBoardListCommand deleteCommand = new DeleteBoardListCommand(
                new ListId("list-1"),
                new UserId("user-1"));

        GetBoardListsCommand getCommand = new GetBoardListsCommand(
                new BoardId("board-1"),
                new UserId("user-1"));

        UpdateBoardListCommand updateCommand = new UpdateBoardListCommand(
                new ListId("list-1"),
                new UserId("user-1"),
                "수정된 리스트",
                "수정된 설명");

        UpdateBoardListPositionCommand positionCommand = new UpdateBoardListPositionCommand(
                new ListId("list-1"),
                new UserId("user-1"),
                5);

        // when & then
        assertThat(validator.validateCreateBoardList(createCommand)).isInstanceOf(ValidationResult.class);
        assertThat(validator.validateDeleteBoardList(deleteCommand)).isInstanceOf(ValidationResult.class);
        assertThat(validator.validateGetBoardLists(getCommand)).isInstanceOf(ValidationResult.class);
        assertThat(validator.validateUpdateBoardList(updateCommand)).isInstanceOf(ValidationResult.class);
        assertThat(validator.validateUpdateBoardListPosition(positionCommand)).isInstanceOf(ValidationResult.class);
    }
}