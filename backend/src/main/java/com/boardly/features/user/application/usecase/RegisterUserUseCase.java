package com.boardly.features.user.application.usecase;


import com.boardly.features.user.application.port.input.RegisterUserCommand;
import com.boardly.features.user.domain.model.User;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;

public interface RegisterUserUseCase {

  /**
   * 새로운 사용자를 시스템에 등록합니다.
   * 
   * <p>이 메서드는 사용자 등록 프로세스를 처리하며, 다음과 같은 검증과 처리를 수행합니다:
   * <ul>
   *   <li>입력 데이터 유효성 검증 (이메일 형식, 비밀번호 강도 등)</li>
   *   <li>이메일 중복 확인</li>
   *   <li>비밀번호 해싱</li>
   *   <li>사용자 데이터 저장</li>
   * </ul>
   * 
   * <p>등록 성공 시 새로 생성된 {@link User} 객체를 반환하고,
   * 실패 시 구체적인 실패 원인을 담은 {@link Failure} 객체를 반환합니다.
   * 
   * @param command 사용자 등록에 필요한 정보를 담은 커맨드 객체
   *                - email: 사용자 이메일 주소 (필수, 유효한 이메일 형식)
   *                - password: 사용자 비밀번호 (필수, 최소 8자 이상)
   *                - firstName: 사용자 이름 (필수)
   *                - lastName: 사용자 성 (필수)
   * 
   * @return {@link Either} 타입으로 래핑된 결과:
   *         <ul>
   *           <li>성공 시: {@code Either.right(User)} - 생성된 사용자 객체</li>
   *           <li>실패 시: {@code Either.left(Failure)} - 실패 원인을 담은 객체</li>
   *         </ul>
   * 
   * @throws IllegalArgumentException command가 null인 경우
   * 
   * @see RegisterUserCommand
   * @see User
   * @see Failure
   * @see Failure.ValidationFailure 입력 데이터 유효성 검증 실패
   * @see Failure.ConflictFailure 이메일 중복 등의 충돌 상황
   * @see Failure.InternalServerError 시스템 내부 오류
   * 
   * @since 1.0.0
   * 
   * @example
   * <pre>{@code
   * RegisterUserCommand command = new RegisterUserCommand(
   *     "user@example.com",
   *     "securePassword123",
   *     "홍",
   *     "길동"
   * );
   * 
   * Either<Failure, User> result = registerUserUseCase.register(command);
   * 
   * result.fold(
   *     failure -> {
   *         // 실패 처리 로직
   *         log.error("사용자 등록 실패: {}", failure.getMessage());
   *         return ResponseEntity.badRequest().body(failure);
   *     },
   *     user -> {
   *         // 성공 처리 로직
   *         log.info("사용자 등록 성공: {}", user.getEmail());
   *         return ResponseEntity.status(HttpStatus.CREATED).body(user);
   *     }
   * );
   * }</pre>
   */
  Either<Failure, User> register(RegisterUserCommand command);

}
