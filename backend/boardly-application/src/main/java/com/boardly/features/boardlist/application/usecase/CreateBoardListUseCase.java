package com.boardly.features.boardlist.application.usecase;

import com.boardly.features.boardlist.application.command.CreateBoardListCommand;
import com.boardly.features.boardlist.domain.BoardList;
import com.boardly.shared.common.error.Failure;
import io.vavr.control.Either;

/**
 * 보드 리스트 생성 유스케이스 인터페이스
 *
 * @since 1.0.0
 */
public interface CreateBoardListUseCase {

  /**
   * 새로운 보드 리스트를 생성합니다.
   *
   * @param command 리스트 생성에 필요한 정보를 담은 커맨드 객체
   * @return 생성 결과 (성공 시 생성된 리스트, 실패 시 실패 정보)
   */
  Either<Failure, BoardList> createBoardList(CreateBoardListCommand command);
}
