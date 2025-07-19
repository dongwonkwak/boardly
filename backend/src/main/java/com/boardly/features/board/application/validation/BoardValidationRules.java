package com.boardly.features.board.application.validation;

import java.util.function.Function;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.Validator;


public final class BoardValidationRules {
  public static final int TITLE_MAX_LENGTH = 50;
  public static final int DESCRIPTION_MAX_LENGTH = 500;

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
        title -> title == null || !CommonValidationRules.HTML_TAG_PATTERN.matcher(title).find(),
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
        description -> description == null || !CommonValidationRules.HTML_TAG_PATTERN.matcher(description).find(),
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
}
