package com.boardly.features.boardlist.application.usecase;

import com.boardly.features.boardlist.application.query.GetBoardListsQuery;
import com.boardly.features.boardlist.domain.BoardList;
import com.boardly.shared.common.error.Failure;
import io.vavr.control.Either;
import java.util.List;

/**
 * 보드 리스트 목록 조회 유스케이스 인터페이스
 *
 * @since 1.0.0
 */
public interface GetBoardListsUseCase {
  /**
   * 보드에 속한 모든 리스트를 조회합니다.
   *
   * @param command 리스트 조회에 필요한 정보를 담은 커맨드 객체
   * @return 조회 결과 (성공 시 리스트 목록, 실패 시 실패 정보)
   */
  Either<Failure, List<BoardList>> getBoardLists(GetBoardListsQuery command);
}
