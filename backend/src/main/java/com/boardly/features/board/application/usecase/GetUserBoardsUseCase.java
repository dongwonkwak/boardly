package com.boardly.features.board.application.usecase;

import com.boardly.features.board.application.port.input.GetUserBoardsCommand;
import com.boardly.features.board.domain.model.Board;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

import java.util.List;

/**
 * 사용자 보드 목록 조회 유스케이스 인터페이스
 * 
 * <p>이 인터페이스는 특정 사용자가 소유한 보드 목록을 조회하는 비즈니스 로직을 정의합니다.
 * 클린 아키텍처의 Application Layer에서 도메인 로직의 진입점 역할을 합니다.
 * 
 * <p>구현체는 다음과 같은 책임을 가집니다:
 * <ul>
 *   <li>소유자 ID 유효성 검증</li>
 *   <li>보드 목록 조회 (활성/아카이브 상태에 따른 필터링)</li>
 *   <li>조회 결과 정렬 (최신 생성순/수정순)</li>
 *   <li>오류 상황 처리 및 적절한 응답 반환</li>
 * </ul>
 * 
 * @since 1.0.0
 * @author Boardly Team
 */
public interface GetUserBoardsUseCase {

    /**
     * 사용자가 소유한 보드 목록을 조회합니다.
     * 
     * <p>이 메서드는 보드 목록 조회 프로세스를 처리하며, 다음과 같은 검증과 처리를 수행합니다:
     * <ul>
     *   <li>소유자 ID 유효성 검증</li>
     *   <li>보드 목록 조회 (활성 보드만 또는 전체 보드)</li>
     *   <li>조회 결과를 최신 수정 시간 순으로 정렬</li>
     * </ul>
     * 
     * <p>조회 성공 시 {@link Board} 목록을 반환하고,
     * 실패 시 구체적인 실패 원인을 담은 {@link Failure} 객체를 반환합니다.
     * 
     * @param command 보드 목록 조회에 필요한 정보를 담은 커맨드 객체
     *                <ul>
     *                  <li>ownerId: 보드 소유자 ID (필수)</li>
     *                  <li>includeArchived: 아카이브된 보드 포함 여부</li>
     *                </ul>
     * 
     * @return {@link Either} 타입으로 래핑된 결과:
     *         <ul>
     *           <li>성공 시: {@code Either.right(List<Board>)} - 보드 목록</li>
     *           <li>실패 시: {@code Either.left(Failure)} - 실패 원인을 담은 객체</li>
     *         </ul>
     * 
     * @throws IllegalArgumentException command가 null인 경우
     * 
     * @see GetUserBoardsCommand
     * @see Board
     * @see Failure
     * @see Failure.ValidationFailure 입력 데이터 유효성 검증 실패
     * @see Failure.NotFoundFailure 사용자가 존재하지 않는 경우
     * @see Failure.InternalServerError 시스템 내부 오류
     */
    Either<Failure, List<Board>> getUserBoards(GetUserBoardsCommand command);
} 