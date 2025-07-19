package com.boardly.features.boardlist.application.validation;

import com.boardly.features.boardlist.application.port.input.CreateBoardListCommand;
import com.boardly.features.boardlist.domain.model.ListColor;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class CreateBoardListValidator {
    
    private final ValidationMessageResolver messageResolver;
    
    // 상수 정의
    private static final int TITLE_MAX_LENGTH = 100;
    private static final int DESCRIPTION_MAX_LENGTH = 500;
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    
    public ValidationResult<CreateBoardListCommand> validate(CreateBoardListCommand command) {
        return getValidator().validate(command);
    }
    
    private Validator<CreateBoardListCommand> getValidator() {
        return Validator.combine(
            titleValidator(),
            descriptionValidator(),
            boardIdValidator(),
            userIdValidator(),
            colorValidator()
        );
    }
    
    private Validator<CreateBoardListCommand> titleValidator() {
        return Validator.chain(
            Validator.fieldWithMessage(
                CreateBoardListCommand::title,
                title -> title != null && !title.trim().isEmpty(),
                "title",
                "validation.boardlist.title.required",
                messageResolver
            ),
            Validator.fieldWithMessage(
                CreateBoardListCommand::title,
                title -> title == null || title.length() <= TITLE_MAX_LENGTH,
                "title",
                "validation.boardlist.title.max.length",
                messageResolver
            ),
            Validator.fieldWithMessage(
                CreateBoardListCommand::title,
                title -> title == null || isTitleValid(title),
                "title",
                "validation.boardlist.title.invalid",
                messageResolver
            )
        );
    }
    
    private Validator<CreateBoardListCommand> descriptionValidator() {
        return Validator.chain(
            Validator.fieldWithMessage(
                CreateBoardListCommand::description,
                description -> description == null || description.length() <= DESCRIPTION_MAX_LENGTH,
                "description",
                "validation.boardlist.description.max.length",
                messageResolver
            ),
            Validator.fieldWithMessage(
                CreateBoardListCommand::description,
                description -> description == null || !isHtmlTag(description),
                "description",
                "validation.boardlist.description.invalid",
                messageResolver
            )
        );
    }
    
    private Validator<CreateBoardListCommand> boardIdValidator() {
        return Validator.fieldWithMessage(
            CreateBoardListCommand::boardId,
            boardId -> boardId != null,
            "boardId",
            "validation.boardlist.boardId.required",
            messageResolver
        );
    }
    
    private Validator<CreateBoardListCommand> userIdValidator() {
        return Validator.fieldWithMessage(
            CreateBoardListCommand::userId,
            userId -> userId != null,
            "userId",
            "validation.boardlist.userId.required",
            messageResolver
        );
    }
    
    private Validator<CreateBoardListCommand> colorValidator() {
        return Validator.chain(
            Validator.fieldWithMessage(
                CreateBoardListCommand::color,
                color -> color != null,
                "color",
                "validation.boardlist.color.required",
                messageResolver
            ),
            Validator.fieldWithMessage(
                CreateBoardListCommand::color,
                color -> color == null || ListColor.isValidColor(color.color()),
                "color",
                "validation.boardlist.color.invalid",
                messageResolver
            )
        );
    }
    
    private boolean isHtmlTag(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        return HTML_TAG_PATTERN.matcher(text).find();
    }
    
    private boolean isTitleValid(String title) {
        if (isHtmlTag(title)) {
            return false;
        }
        // 제목은 한글, 영문, 숫자, 공백, 특수문자 일부만 허용
        return title.matches("^[a-zA-Z0-9가-힣\\s\\-_.,!?()]*$");
    }
} 