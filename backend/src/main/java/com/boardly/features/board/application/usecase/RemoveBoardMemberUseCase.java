package com.boardly.features.board.application.usecase;

import com.boardly.features.board.application.port.input.RemoveBoardMemberCommand;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;

/**
 * 보드 멤버 삭제 UseCase
 */
public interface RemoveBoardMemberUseCase {

    /**
     * 보드에서 멤버를 삭제합니다.
     * 
     * @param command 멤버 삭제 명령
     * @return 삭제 결과 (성공 시 null, 실패 시 실패 정보)
     */
    Either<Failure, Void> removeBoardMember(RemoveBoardMemberCommand command);
}