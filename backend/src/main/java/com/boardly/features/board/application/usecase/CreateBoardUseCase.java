package com.boardly.features.board.application.usecase;

import com.boardly.features.board.application.port.input.CreateBoardCommand;
import com.boardly.features.board.domain.model.Board;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

/**
 * 보드 생성 유스케이스 인터페이스
 * 
 * <p>이 인터페이스는 새로운 보드를 생성하는 비즈니스 로직을 정의합니다.
 * 클린 아키텍처의 Application Layer에서 도메인 로직의 진입점 역할을 합니다.
 * 
 * <p>구현체는 다음과 같은 책임을 가집니다:
 * <ul>
 *   <li>입력 데이터 검증 및 유효성 확인</li>
 *   <li>비즈니스 규칙 적용 (보드 생성 제한 등)</li>
 *   <li>도메인 객체 생성 및 저장</li>
 *   <li>오류 상황 처리 및 적절한 응답 반환</li>
 * </ul>
 * 
 * @since 1.0.0
 * @author Boardly Team
 */
public interface CreateBoardUseCase {

  /**
   * 새로운 보드를 생성합니다.
   * 
   * <p>이 메서드는 보드 생성 프로세스를 처리하며, 다음과 같은 검증과 처리를 수행합니다:
   * <ul>
   *   <li>입력 데이터 유효성 검증 (제목 필수, 길이 제한, 특수문자 제한 등)</li>
   *   <li>소유자 정보 검증</li>
   *   <li>보드 생성 제한 확인 (사용자당 최대 보드 개수 등)</li>
   *   <li>보드 도메인 객체 생성</li>
   *   <li>보드 데이터 저장</li>
   * </ul>
   * 
   * <p>생성 성공 시 새로 생성된 {@link Board} 객체를 반환하고,
   * 실패 시 구체적인 실패 원인을 담은 {@link Failure} 객체를 반환합니다.
   * 
   * <p>생성된 보드는 다음과 같은 초기 상태를 가집니다:
   * <ul>
   *   <li>고유한 보드 ID 자동 생성</li>
   *   <li>활성 상태 (isArchived = false)</li>
   *   <li>생성 시간 및 수정 시간 설정</li>
   *   <li>지정된 소유자에게 할당</li>
   * </ul>
   * 
   * @param command 보드 생성에 필요한 정보를 담은 커맨드 객체
   *                <ul>
   *                  <li>title: 보드 제목 (필수, 1-50자, HTML 태그 금지)</li>
   *                  <li>description: 보드 설명 (선택사항, 최대 500자, HTML 태그 금지)</li>
   *                  <li>ownerId: 보드 소유자 ID (필수)</li>
   *                </ul>
   * 
   * @return {@link Either} 타입으로 래핑된 결과:
   *         <ul>
   *           <li>성공 시: {@code Either.right(Board)} - 생성된 보드 객체</li>
   *           <li>실패 시: {@code Either.left(Failure)} - 실패 원인을 담은 객체</li>
   *         </ul>
   * 
   * @throws IllegalArgumentException command가 null인 경우
   * 
   * @see CreateBoardCommand
   * @see Board
   * @see Failure
   * @see Failure.ValidationFailure 입력 데이터 유효성 검증 실패
   * @see Failure.ConflictFailure 보드 생성 제한 등의 충돌 상황
   * @see Failure.InternalServerError 시스템 내부 오류
   * 
   * @example
   * <pre>{@code
   * // 기본 보드 생성
   * CreateBoardCommand command = new CreateBoardCommand(
   *     "프로젝트 기획 보드",
   *     "2024년 신규 프로젝트 기획을 위한 보드입니다.",
   *     new UserId("user-123")
   * );
   * 
   * Either<Failure, Board> result = createBoardUseCase.createBoard(command);
   * 
   * result.fold(
   *     failure -> {
   *         // 실패 처리
   *         if (failure instanceof Failure.ValidationFailure) {
   *             log.error("보드 생성 검증 실패: {}", failure.getMessage());
   *             return ResponseEntity.badRequest().body(failure);
   *         } else if (failure instanceof Failure.ConflictFailure) {
   *             log.warn("보드 생성 제한: {}", failure.getMessage());
   *             return ResponseEntity.status(HttpStatus.CONFLICT).body(failure);
   *         } else {
   *             log.error("보드 생성 중 시스템 오류: {}", failure.getMessage());
   *             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(failure);
   *         }
   *     },
   *     board -> {
   *         // 성공 처리
   *         log.info("보드 생성 성공: boardId={}, title={}", 
   *                  board.getBoardId().getId(), board.getTitle());
   *         return ResponseEntity.status(HttpStatus.CREATED).body(board);
   *     }
   * );
   * 
   * // 설명 없는 보드 생성
   * CreateBoardCommand simpleCommand = new CreateBoardCommand(
   *     "간단한 보드",
   *     null, // 설명 생략 가능
   *     new UserId("user-123")
   * );
   * }</pre>
   */
  Either<Failure, Board> createBoard(CreateBoardCommand command);

}
