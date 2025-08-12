package com.boardly.features.user.application.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.user.application.usecase.GetUserUseCase;
import com.boardly.features.user.domain.User;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.validation.MessageResolver;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetUserService implements GetUserUseCase {

  private final com.boardly.features.user.domain.port.UserFinder userFinder;
  private final MessageResolver messageResolver;

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
          messageResolver.getMessage("validation.user.email.not.found"),
          "USER_NOT_FOUND",
          context));
    }
  }
}
