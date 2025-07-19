package com.boardly.features.boardlist.application.validation;

import com.boardly.features.boardlist.application.port.input.CreateBoardListCommand;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateBoardListValidator {
    
    private final CommonValidationRules commonValidationRules;

    /**
     * 보드 리스트 생성 검증
     */
    public ValidationResult<CreateBoardListCommand> validateCreateBoardList(CreateBoardListCommand command) {
        return getCreateBoardListValidator().validate(command);
    }

    /**
     * 보드 리스트 생성 검증 (기존 테스트 호환성)
     */
    public ValidationResult<CreateBoardListCommand> validate(CreateBoardListCommand command) {
        return validateCreateBoardList(command);
    }

    private Validator<CreateBoardListCommand> getCreateBoardListValidator() {
        return Validator.combine(
                commonValidationRules.titleComplete(CreateBoardListCommand::title),
                commonValidationRules.descriptionComplete(CreateBoardListCommand::description),
                commonValidationRules.boardIdRequired(CreateBoardListCommand::boardId),
                commonValidationRules.userIdRequired(CreateBoardListCommand::userId),
                commonValidationRules.listColorRequired(CreateBoardListCommand::color)
        );
    }
} 