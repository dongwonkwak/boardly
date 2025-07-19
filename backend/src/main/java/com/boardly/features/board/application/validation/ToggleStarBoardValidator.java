package com.boardly.features.board.application.validation;

import com.boardly.features.board.application.port.input.ToggleStarBoardCommand;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 보드 즐겨찾기 토글 명령 검증기
 * 
 * <p>보드 즐겨찾기 토글 요청의 입력 데이터 유효성을 검증합니다.
 * 
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class ToggleStarBoardValidator {

    private final CommonValidationRules commonValidationRules;

    public ValidationResult<ToggleStarBoardCommand> validate(ToggleStarBoardCommand command) {
        return getValidator().validate(command);
    }

    private Validator<ToggleStarBoardCommand> getValidator() {
        return Validator.combine(
            commonValidationRules.boardIdRequired(ToggleStarBoardCommand::boardId),
            commonValidationRules.userIdRequired(ToggleStarBoardCommand::requestedBy)
        );
    }
}
