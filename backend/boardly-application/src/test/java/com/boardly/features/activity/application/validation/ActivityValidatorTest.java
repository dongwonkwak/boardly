package com.boardly.features.activity.application.validation;

import static org.junit.jupiter.api.Assertions.*;

import com.boardly.features.activity.application.command.CreateActivityCommand;
import com.boardly.features.activity.application.query.GetActivityQuery;
import com.boardly.features.activity.domain.ActivityType;
import com.boardly.application.validation.CommonValidationRules;
import com.boardly.shared.common.value.*;
import com.boardly.shared.validation.MessageResolver;
import com.boardly.shared.validation.ValidationResult;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ActivityValidatorTest {

  private ActivityValidator validator;
  private MessageResolver messageResolver;

  @BeforeEach
  void setUp() {
    messageResolver = Mockito.mock(MessageResolver.class);
    CommonValidationRules commonRules = new CommonValidationRules(messageResolver);
    validator = new ActivityValidator(commonRules, messageResolver);
  }

  @Test
  @DisplayName("CreateActivityCommand: 카드 활동에 필수 ID 없으면 invalid")
  void validateCreate_cardActivity_requiresIds() {
    var cmd = CreateActivityCommand.forCard(
        ActivityType.CARD_CREATE,
        new UserId("u1"),
        Map.of("k", "v"),
        "Board",
        null, // boardId missing
        new ListId("l1"),
        new CardId("c1")
    );

    var result = validator.validateCreate(cmd);
    assertTrue(result.isInvalid());
  }

  @Test
  @DisplayName("GetActivityQuery: since > until 이면 invalid")
  void validateGet_dateRange() {
    Instant since = Instant.now();
    Instant until = since.minusSeconds(10);
    var query = new GetActivityQuery(null, new BoardId("b1"), since, until, 0, 50);

    ValidationResult<GetActivityQuery> result = validator.validateGet(query);
    assertTrue(result.isInvalid());
  }

  @Test
  @DisplayName("GetActivityQuery: page/size 기본값 허용")
  void validateGet_defaults() {
    var query = GetActivityQuery.forUser(new UserId("u1"));
    var result = validator.validateGet(query);
    assertTrue(result.isValid());
  }
}
