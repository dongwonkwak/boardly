package com.boardly.features.boardlist.application.usecase;

import com.boardly.features.boardlist.application.command.DeleteBoardListCommand;
import com.boardly.shared.common.error.Failure;
import io.vavr.control.Either;

/**
 * 보드 리스트 삭제 유스케이스 인터페이스
 *
 * @since 1.0.0
 */
public interface DeleteBoardListUseCase {
  /**
   * 보드 리스트를 삭제합니다.
   *
   * @param command 리스트 삭제에 필요한 정보를 담은 커맨드 객체
   * @return 삭제 결과 (성공 시 void, 실패 시 실패 정보)
   */
  Either<Failure, Void> deleteBoardList(DeleteBoardListCommand command);
}
