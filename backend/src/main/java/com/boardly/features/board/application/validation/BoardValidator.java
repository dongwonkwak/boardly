package com.boardly.features.board.application.validation;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.boardly.features.board.application.port.input.CreateBoardCommand;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BoardValidator {
  private final ValidationMessageResolver messageResolver;
  private static final int TITLE_MAX_LENGTH = 50;
  private static final int DESCRIPTION_MAX_LENGTH = 500;
  private static final Pattern HTML_TAG_PATTERN = Pattern.compile("^<[^>]*>$");

  /**
   * 보드 생성 검증
   * @param command 보드 생성 명령
   * @return 검증 결과
   */
  public ValidationResult<CreateBoardCommand> validateCreateBoard(CreateBoardCommand command) {
    return Validator.combine(
      getTitleValidator(),
      getDescriptionValidator(),
      getOwnerValidator()
    ).validate(command);
  }

  /**
   * 제목 검증
   * @return 제목 검증 결과
   */
  private Validator<CreateBoardCommand> getTitleValidator() {
    return Validator.chain(
      Validator.fieldWithMessage(
        CreateBoardCommand::title,
        title -> title != null && !title.trim().isEmpty(),
        "title",
        "validation.board.title.required",
        messageResolver
      ),
      Validator.fieldWithMessage(
        CreateBoardCommand::title,
        title -> title.trim().length() >= 1,
        "title",
        "validation.board.title.min.length",
        messageResolver,
        1
      ),
      Validator.fieldWithMessage(
        CreateBoardCommand::title,
        title -> title.length() <= TITLE_MAX_LENGTH,
        "title",
        "validation.board.title.max.length",
        messageResolver,
        TITLE_MAX_LENGTH
      ),
      Validator.fieldWithMessage(
        CreateBoardCommand::title,
        this::isValidTitle,
        "title",
        "validation.board.title.invalid",
        messageResolver
      )
    );
  }

  /**
   * 설명 검증
   * @return 설명 검증 결과
   */
  private Validator<CreateBoardCommand> getDescriptionValidator() {
    return Validator.chain(
      Validator.fieldWithMessage(
        CreateBoardCommand::description,
        description -> description == null || description.length() <= DESCRIPTION_MAX_LENGTH,
        "description",
        "validation.board.description.max.length",
        messageResolver,
        DESCRIPTION_MAX_LENGTH
      ),
      Validator.fieldWithMessage(
        CreateBoardCommand::description,
        description -> !isHtmlTag(description),
        "description",
        "validation.board.description.html.tag",
        messageResolver
      )
    );
  }

  /**
   * 제목 유효성 검증
   * @param title 제목
   * @return 유효성 결과
   */
  private boolean isValidTitle(String title) {
    if (isHtmlTag(title)) {
      return false;
    }

    // 특수문자 제한 (필요에 따라 조정)
    String pattern = "^[a-zA-Z0-9가-힣\\s\\-_.,!?()]*$";
    return title.matches(pattern);
  }

  /**
   * 소유자 검증
   * @return 소유자 검증 결과
   */
  private Validator<CreateBoardCommand> getOwnerValidator() {
    return Validator.fieldWithMessage(
      CreateBoardCommand::ownerId,
      ownerId -> ownerId != null,
      "ownerId",
      "validation.board.owner.required",
      messageResolver
    );
  }

  /**
   * HTML 태그 검증
   * @param text 텍스트
   * @return 검증 결과
   */
  private boolean isHtmlTag(String text) {
    if (text == null || text.trim().isEmpty()) {
      return false;
    }

    return HTML_TAG_PATTERN.matcher(text).matches();
  }
}
