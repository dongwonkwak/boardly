package com.boardly.features.user.application.service;

import org.springframework.stereotype.Service;

import com.boardly.features.user.application.usecase.GetUserUseCase;
import com.boardly.features.user.domain.model.User;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetUserService implements GetUserUseCase {

  private final UserFinder userFinder;
  private final ValidationMessageResolver validationMessageResolver;

  @Transactional(readOnly = true)
  @Override
  public Either<Failure, User> get(UserId userId) {
    try {
      User user = userFinder.findUserOrThrow(userId);
      return Either.right(user);
    } catch (Exception e) {
      log.error("사용자 조회 실패: userId={}, error={}", userId, e.getMessage());
      Map<String, Object> context = Map.of("userId", userId.getId());
      return Either.left(Failure.ofNotFound(
          validationMessageResolver.getMessage("validation.user.email.not.found"),
          "USER_NOT_FOUND",
          context));
    }
  }
}