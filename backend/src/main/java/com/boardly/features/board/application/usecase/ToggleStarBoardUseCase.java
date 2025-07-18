package com.boardly.features.board.application.usecase;

import com.boardly.features.board.application.port.input.ToggleStarBoardCommand;
import com.boardly.features.board.domain.model.Board;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

/**
 * 보드 즐겨찾기 상태 토글 유스케이스 인터페이스
 * 
 * <p>이 인터페이스는 보드의 즐겨찾기 상태를 토글하는 비즈니스 로직을 정의합니다.
 * 클린 아키텍처의 Application Layer에서 도메인 로직의 진입점 역할을 합니다.
 * 
 * <p>구현체는 다음과 같은 책임을 가집니다:
 * <ul>
 *   <li>입력 데이터 검증 및 유효성 확인</li>
 *   <li>사용자 권한 확인 (보드 소유자인지 검증)</li>
 *   <li>보드 존재 여부 확인</li>
 *   <li>즐겨찾기 상태 토글 처리</li>
 *   <li>오류 상황 처리 및 적절한 응답 반환</li>
 * </ul>
 * 
 * @since 1.0.0
 * @author Boardly Team
 */
public interface ToggleStarBoardUseCase {

    /**
     * 보드의 즐겨찾기 상태를 토글합니다.
     * 
     * <p>이 메서드는 보드 즐겨찾기 토글 프로세스를 처리하며, 다음과 같은 검증과 처리를 수행합니다:
     * <ul>
     *   <li>입력 데이터 유효성 검증 (보드 ID, 사용자 ID 필수)</li>
     *   <li>보드 존재 여부 확인</li>
     *   <li>사용자 권한 확인 (보드 소유자인지 검증)</li>
     *   <li>현재 즐겨찾기 상태 확인 후 반대 상태로 변경</li>
     *   <li>변경된 보드 정보 저장</li>
     * </ul>
     * 
     * <p>토글 성공 시 업데이트된 {@link Board} 객체를 반환하고,
     * 실패 시 구체적인 실패 원인을 담은 {@link Failure} 객체를 반환합니다.
     * 
     * <p>가능한 실패 케이스:
     * <ul>
     *   <li>잘못된 입력 데이터 (보드 ID 또는 사용자 ID가 null)</li>
     *   <li>존재하지 않는 보드</li>
     *   <li>권한 부족 (요청자가 보드 소유자가 아닌 경우)</li>
     *   <li>데이터베이스 오류 등 시스템 오류</li>
     * </ul>
     * 
     * @param command 즐겨찾기 토글 요청 정보 (보드 ID, 요청자 ID 포함)
     * @return 성공 시 업데이트된 Board 객체, 실패 시 Failure 객체
     * 
     * @throws IllegalArgumentException command가 null인 경우
     * 
     * @since 1.0.0
     */
    Either<Failure, Board> starringBoard(ToggleStarBoardCommand command);

    Either<Failure, Board> unstarringBoard(ToggleStarBoardCommand command);
} 