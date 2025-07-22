package com.boardly.features.board.application.usecase;

import com.boardly.features.board.application.port.input.DeleteBoardCommand;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;

/**
 * 보드 삭제 유스케이스 인터페이스
 * 
 * <p>
 * 이 인터페이스는 보드를 영구적으로 삭제하는 비즈니스 로직을 정의합니다.
 * 보드와 관련된 모든 데이터(BoardList, Card, BoardMember)를 함께 삭제합니다.
 * 
 * <p>
 * 구현체는 다음과 같은 책임을 가집니다:
 * <ul>
 * <li>보드 존재 여부 확인</li>
 * <li>보드 삭제 권한 검증 (요청자가 보드 소유자인지 확인)</li>
 * <li>연관된 모든 데이터 삭제 (BoardList, Card, BoardMember)</li>
 * <li>보드 삭제</li>
 * </ul>
 * 
 * @since 1.0.0
 * @author Boardly Team
 */
public interface DeleteBoardUseCase {

    /**
     * 보드를 영구적으로 삭제합니다.
     * 
     * <p>
     * 보드 삭제 시 다음 데이터들이 함께 삭제됩니다:
     * <ul>
     * <li>보드의 모든 리스트 (BoardList)</li>
     * <li>모든 리스트의 모든 카드 (Card)</li>
     * <li>보드의 모든 멤버 (BoardMember)</li>
     * <li>보드 자체</li>
     * </ul>
     * 
     * @param command 보드 삭제에 필요한 정보를 담은 커맨드 객체
     * @return 삭제 결과 (성공 시 null, 실패 시 실패 정보)
     * 
     * @see DeleteBoardCommand
     * @see Failure
     */
    Either<Failure, Void> deleteBoard(DeleteBoardCommand command);
}