package com.boardly.features.board.application.validation;

import java.util.function.Function;
import java.util.regex.Pattern;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.Validator;


public final class BoardValidationRules {
  public static final int TITLE_MAX_LENGTH = 50;
  public static final int DESCRIPTION_MAX_LENGTH = 500;
  public static final Pattern HTML_TAG_PATTERN = Pattern.compile("^<[^>]*>$");

  private BoardValidationRules() {
  }

  public static <T> Validator<T> titleValidator(
          Function<T, String> titleExtractor,
          ValidationMessageResolver messageResolver) {
    return Validator.chain(
      Validator.fieldWithMessage(
        titleExtractor,
        title -> title == null || !title.trim().isEmpty(),
        "title",
        "validation.board.title.min.length",
        messageResolver
      ),
      Validator.fieldWithMessage(
        titleExtractor,
        title -> title == null || title.length() <= TITLE_MAX_LENGTH,
        "title",
        "validation.board.title.max.length",
        messageResolver
      ),
      Validator.fieldWithMessage(
        titleExtractor,
        title -> title == null || isTitleValid(title),
        "title",
        "validation.board.title.invalid",
        messageResolver
      )
    );
  }

  public static <T> Validator<T> descriptionValidator(Function<T, String> descriptionExtractor, ValidationMessageResolver messageResolver) {
    return Validator.chain(
      Validator.fieldWithMessage(
        descriptionExtractor,
        description -> description == null || description.length() <= DESCRIPTION_MAX_LENGTH,
        "description",
        "validation.board.description.max.length",
        messageResolver
      ),
      Validator.fieldWithMessage(
        descriptionExtractor,
        description -> description == null || !isHtmlTag(description),
        "description",
        "validation.board.description.invalid",
        messageResolver
      )
    );
  }

  public static <T> Validator<T> boardIdValidator(Function<T, BoardId> boardIdExtractor, ValidationMessageResolver messageResolver) {
    return Validator.fieldWithMessage(
      boardIdExtractor,
      boardId -> boardId != null,
      "boardId",
      "validation.board.id.required",
      messageResolver
    );
  }

  public static <T> Validator<T> userIdValidator(Function<T, UserId> requestedByExtractor, ValidationMessageResolver messageResolver) {
    return Validator.fieldWithMessage(
      requestedByExtractor,
      requestedBy -> requestedBy != null,
      "userId",
      "validation.user.id.required",
      messageResolver
    );
  }

  private static boolean isHtmlTag(String text) {
    if (text == null || text.trim().isEmpty()) {
      return false;
    }

    return HTML_TAG_PATTERN.matcher(text).matches();
  }

  private static boolean isTitleValid(String title) {
    if (isHtmlTag(title)) {
      return false;
    }

    return title.matches("^[a-zA-Z0-9가-힣\\s\\-_.,!?()]*$");
  }
}
