package com.boardly.features.board.application.usecase;

import com.boardly.features.board.application.port.input.UpdateBoardCommand;
import com.boardly.features.board.domain.model.Board;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;

/**
 * 보드 업데이트 유스케이스 인터페이스
 * 
 * <p>이 인터페이스는 보드의 제목과 설명을 수정하는 비즈니스 로직을 정의합니다.
 * 
 * <p>구현체는 다음과 같은 책임을 가집니다:
 * <ul>
 *   <li>보드 존재 여부 확인</li>
 *   <li>보드 수정 권한 검증 (요청자가 보드 소유자인지 확인)</li>
 *   <li>아카이브된 보드 수정 제한 확인</li>
 *   <li>입력 데이터 검증 및 유효성 확인</li>
 *   <li>보드 내용 변경 사항 적용</li>
 *   <li>업데이트된 보드 저장</li>
 * </ul>
 * 
 * <p>아카이브된 보드는 내용 수정이 불가능합니다.
 * 
 * @since 1.0.0
 * @author Boardly Team
 */
public interface UpdateBoardUseCase {

    /**
     * 보드의 제목과 설명을 업데이트합니다.
     * 
     * <p>이 메서드는 보드 업데이트 프로세스를 처리하며, 다음과 같은 검증과 처리를 수행합니다:
     * <ul>
     *   <li>보드 존재 여부 확인</li>
     *   <li>보드 수정 권한 검증 (요청자의 UserId와 보드의 ownerId 비교)</li>
     *   <li>아카이브된 보드 수정 제한 확인</li>
     *   <li>입력 데이터 유효성 검증 (제목 길이, 특수문자 제한 등)</li>
     *   <li>변경된 필드만 선택적으로 업데이트</li>
     *   <li>도메인 객체 상태 변경 및 수정 시간 업데이트</li>
     *   <li>업데이트된 보드 데이터 저장</li>
     * </ul>
     * 
     * @param command 보드 업데이트에 필요한 정보를 담은 커맨드 객체
     * @return 업데이트 결과 (성공 시 업데이트된 보드, 실패 시 실패 정보)
     * 
     * @see UpdateBoardCommand
     * @see Board
     * @see Failure
     */
    Either<Failure, Board> updateBoard(UpdateBoardCommand command);
} 