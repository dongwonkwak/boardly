package com.boardly.features.board.application.usecase;

import com.boardly.features.board.application.port.input.UpdateBoardMemberRoleCommand;
import com.boardly.features.board.domain.model.BoardMember;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;

/**
 * 보드 멤버 역할 수정 UseCase
 */
public interface UpdateBoardMemberRoleUseCase {

    /**
     * 보드 멤버의 역할을 수정합니다.
     * 
     * @param command 멤버 역할 수정 명령
     * @return 수정 결과 (성공 시 수정된 멤버, 실패 시 실패 정보)
     */
    Either<Failure, BoardMember> updateBoardMemberRole(UpdateBoardMemberRoleCommand command);
}