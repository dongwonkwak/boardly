package com.boardly.features.boardlist.application.usecase;

import com.boardly.features.boardlist.application.command.UpdateBoardListCommand;
import com.boardly.features.boardlist.domain.BoardList;
import com.boardly.shared.common.error.Failure;
import io.vavr.control.Either;

/**
 * 보드 리스트 수정 유스케이스 인터페이스
 *
 * @since 1.0.0
 */
public interface UpdateBoardListUseCase {
  /**
   * 보드 리스트 정보를 수정합니다.
   *
   * @param command 리스트 수정에 필요한 정보를 담은 커맨드 객체
   * @return 수정 결과 (성공 시 수정된 리스트, 실패 시 실패 정보)
   */
  Either<Failure, BoardList> updateBoardList(UpdateBoardListCommand command);
}
