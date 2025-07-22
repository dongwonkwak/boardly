package com.boardly.features.board.application.usecase;

import com.boardly.features.board.application.port.input.AddBoardMemberCommand;
import com.boardly.features.board.domain.model.BoardMember;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;

/**
 * 보드 멤버 추가 UseCase
 */
public interface AddBoardMemberUseCase {

    /**
     * 보드에 새로운 멤버를 추가합니다.
     * 
     * @param command 멤버 추가 명령
     * @return 추가 결과 (성공 시 추가된 멤버, 실패 시 실패 정보)
     */
    Either<Failure, BoardMember> addBoardMember(AddBoardMemberCommand command);
}