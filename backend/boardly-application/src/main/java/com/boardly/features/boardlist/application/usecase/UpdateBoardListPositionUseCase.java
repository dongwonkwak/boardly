package com.boardly.features.boardlist.application.usecase;

import com.boardly.features.boardlist.application.command.UpdateBoardListPositionCommand;
import com.boardly.features.boardlist.domain.BoardList;
import com.boardly.shared.common.error.Failure;
import io.vavr.control.Either;
import java.util.List;

/**
 * 보드 리스트 위치 변경 유스케이스 인터페이스
 *
 * @since 1.0.0
 */
public interface UpdateBoardListPositionUseCase {
  /**
   * 리스트의 위치를 변경합니다.
   *
   * @param command 리스트 위치 변경에 필요한 정보를 담은 커맨드 객체
   * @return 변경 결과 (성공 시 업데이트된 리스트 목록, 실패 시 실패 정보)
   */
  Either<Failure, List<BoardList>> updateBoardListPosition(UpdateBoardListPositionCommand command);
}
