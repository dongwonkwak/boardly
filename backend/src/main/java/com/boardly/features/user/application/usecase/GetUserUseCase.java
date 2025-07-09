package com.boardly.features.user.application.usecase;

import com.boardly.shared.domain.common.Failure;
import com.boardly.features.user.domain.model.User;
import com.boardly.features.user.domain.model.UserId;
import io.vavr.control.Either;

public interface GetUserUseCase {

  /**
   * 사용자 ID로 사용자 정보를 조회합니다.
   * 
   * @param userId 조회할 사용자의 ID
   * @return 사용자 정보 조회 결과. 성공 시 User 객체를, 실패 시 Failure 객체를 포함한 Either
   *         - 성공: 사용자 정보가 포함된 User 객체
   *         - 실패: 사용자를 찾을 수 없는 경우 NotFoundFailure, 
   *                데이터베이스 오류 등의 경우 InternalServerError
   */
  Either<Failure, User> get(UserId userId);
}
