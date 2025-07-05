package com.boardly.features.user.application.usecase;

import com.boardly.features.user.application.port.input.UpdateUserCommand;
import com.boardly.features.user.domain.model.User;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;

public interface UpdateUserUseCase {
    
    /**
     * 기존 사용자의 정보를 업데이트합니다.
     * 
     * <p>이 메서드는 기존 사용자의 정보를 수정하는 프로세스를 처리하며, 다음과 같은 검증과 처리를 수행합니다:
     * <ul>
     *   <li>사용자 존재 여부 확인</li>
     *   <li>권한 검증 (본인 또는 관리자 권한)</li>
     *   <li>변경 가능한 필드 검증</li>
     *   <li>입력 데이터 유효성 검증</li>
     *   <li>이메일 변경 시 중복 확인</li>
     *   <li>비밀번호 변경 시 해싱</li>
     *   <li>업데이트된 사용자 정보 저장</li>
     * </ul>
     * 
     * <p>업데이트 성공 시 수정된 {@link User} 객체를 반환하고,
     * 실패 시 구체적인 실패 원인을 담은 {@link Failure} 객체를 반환합니다.
     * 
     * @param command 사용자 업데이트에 필요한 정보를 담은 커맨드 객체
     *                - userId: 업데이트할 사용자 ID (필수)
     *                - email: 변경할 이메일 주소 (선택, 유효한 이메일 형식)
     *                - password: 변경할 비밀번호 (선택, 최소 8자 이상)
     *                - firstName: 변경할 이름 (선택)
     *                - lastName: 변경할 성 (선택)
     * 
     * @return {@link Either} 타입으로 래핑된 결과:
     *         <ul>
     *           <li>성공 시: {@code Either.right(User)} - 업데이트된 사용자 객체</li>
     *           <li>실패 시: {@code Either.left(Failure)} - 실패 원인을 담은 객체</li>
     *         </ul>
     * 
     * @throws IllegalArgumentException command가 null인 경우
     * 
     * @see UpdateUserCommand
     * @see User
     * @see Failure
     * @see Failure.ValidationFailure 입력 데이터 유효성 검증 실패
     * @see Failure.NotFoundFailure 사용자를 찾을 수 없는 경우
     * @see Failure.ConflictFailure 이메일 중복 등의 충돌 상황
     * @see Failure.UnauthorizedFailure 권한이 없는 경우
     * @see Failure.InternalServerError 시스템 내부 오류
     * 
     * @since 1.0.0
     * 
     * @example
     * <pre>{@code
     * UpdateUserCommand command = new UpdateUserCommand(
     *     new UserId("user-123"),
     *     "newemail@example.com",
     *     null, // 비밀번호는 변경하지 않음
     *     "김",
     *     "철수"
     * );
     * 
     * Either<Failure, User> result = updateUserUseCase.update(command);
     * 
     * result.fold(
     *     failure -> {
     *         // 실패 처리 로직
     *         if (failure instanceof Failure.NotFoundFailure) {
     *             log.error("사용자를 찾을 수 없습니다: {}", failure.getMessage());
     *             return ResponseEntity.notFound().build();
     *         } else if (failure instanceof Failure.UnauthorizedFailure) {
     *             log.error("권한이 없습니다: {}", failure.getMessage());
     *             return ResponseEntity.status(HttpStatus.FORBIDDEN).body(failure);
     *         } else {
     *             log.error("사용자 업데이트 실패: {}", failure.getMessage());
     *             return ResponseEntity.badRequest().body(failure);
     *         }
     *     },
     *     user -> {
     *         // 성공 처리 로직
     *         log.info("사용자 업데이트 성공: {}", user.getId());
     *         return ResponseEntity.ok(user);
     *     }
     * );
     * }</pre>
     */
    Either<Failure, User> update(UpdateUserCommand command);
} 